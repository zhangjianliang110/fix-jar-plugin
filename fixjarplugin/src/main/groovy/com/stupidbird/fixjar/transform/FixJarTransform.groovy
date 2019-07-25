package com.stupidbird.fixjar.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.stupidbird.fixjar.CodeInjectConst
import com.stupidbird.fixjar.utils.SafeKeyCreator
import com.stupidbird.fixjar.utils.ConvertUtils
import com.stupidbird.fixjar.utils.MyFileUtil
import com.stupidbird.fixjar.utils.TextUtil
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension

import java.util.jar.JarFile
import java.util.zip.ZipFile

/**
 * 扫描并注入代码
 */
class FixJarTransform extends Transform implements Plugin<Project> {

    public static final String FIXED_JAR_DIR   = "fixed_code"
    public static final String CLASS      = ".class"
    public static final String JAR        = ".jar"

    def mVersionName;

    Project project

    public FixJarTransform(Project project) {
        this.project = project;
        def android = project.extensions.findByType(AppExtension.class)
        mVersionName = android.defaultConfig.versionName
    }

    @Override
    void apply(Project target) {
    }

    /**
     *
     * @param context
     * @param inputs 消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
     * @param referencedInputs
     * @param outputProvider OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
     * @param isIncremental 当前是否是增量编译(由isIncremental() 方法的返回和当前编译是否有增量基础)
     * @throws IOException* @throws TransformException* @throws InterruptedException
     */
    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        System.out.println('=================FixJarTransform transform===============')
        System.out.println '================开始扫描代码================'
        outputProvider.deleteAll()
        File jarFile = outputProvider.getContentLocation("main", getOutputTypes(), getScopes(), Format.JAR);
        if (!jarFile.getParentFile().exists()) {
            jarFile.getParentFile().mkdirs();
        }
        if (jarFile.exists()) {
            jarFile.delete();
        }
        ClassPool classPool = new ClassPool()
        project.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }
        inputs.each {
            it.directoryInputs.each { dirInput ->
                appendClassPath(classPool, dirInput.file)
            }
            it.jarInputs.each { jarInput ->
                appendClassPath(classPool, jarInput.file)
            }
        }
        appendDefaultClassPath(classPool)
        def ctClasses = ConvertUtils.toCtClasses(inputs, classPool)
        for (TransformInput input : inputs) {
            if (null == input) {
                continue
            };
            for (DirectoryInput directoryInput : input.directoryInputs) {//文件夹类型，直接拷贝到输出目录
                if (directoryInput) {
                    if (null != directoryInput.file && directoryInput.file.exists()) {
                        //获取输出目录，拷贝文件到输出目录
                        File dest = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                        FileUtils.copyDirectory(directoryInput.file, dest);
                    }
                }
            }
            for (JarInput jarInput : input.jarInputs) {//jar包类型的输入文件，需要注入代码
                if (jarInput) {
                    if (jarInput.file && jarInput.file.exists()) {
                        String jarName = jarInput.name;
                        if (jarName.endsWith(JAR)) {
                            jarName = jarName.substring(0, jarName.length() - JAR.length());
                        }
                        String safeKey = SafeKeyCreator.getSafeKey(jarInput.file.absolutePath);
                        // 在这里jar文件进行动态修复
                        File injectedJarFile = null
                        try {
                            injectedJarFile = jarBugFix(classPool, jarInput.file)
                        } catch (Throwable ignored) {
                            injectedJarFile = null
                            System.err.println("==============injectedJarFile = null====" + ignored.getMessage())
                        }
                        //修改后的代码jar包存放位置
                        File dest = outputProvider.getContentLocation(SafeKeyCreator.getSafeKey(jarName + safeKey), jarInput.contentTypes, jarInput.scopes, Format.JAR);
                        if (dest) {
                            if (dest.parentFile) {
                                if (!dest.parentFile.exists()) {
                                    dest.parentFile.mkdirs();
                                }
                            }
                            if (!dest.exists()) {
                                dest.createNewFile();
                            }
                            if (null != injectedJarFile && injectedJarFile.exists()) {//代码动态修复成功，把修复后的jar拷贝到输出目录
                                FileUtils.copyFile(injectedJarFile, dest)
                                System.err.println(jarInput.file.name + " has successful hooked !!!")
                                if (!CodeInjectConst.keepTempFile) {
                                    injectedJarFile.delete()
                                }
                            } else {//代码动态修复失败，把原jar拷贝到输出目录
                                FileUtils.copyFile(jarInput.file, dest)
                            }
                        }
                    }
                }
            }
        }
    }

    /**修复jar中的bug*/
    public synchronized File jarBugFix(ClassPool classPool, File jar) {
        File destFile = null
        if (null == jar) {
            System.err.println("===========jar File is null before injecting !!!")
            return destFile
        }
        if (!jar.exists()) {
            System.err.println(jar.name + "=========== not exits !!!")
            return destFile
        }
        try {
            ZipFile zipFile = new ZipFile(jar)
            zipFile.close()
            zipFile = null
        } catch (Exception ignored) {
            System.err.println(jar.name + "=========== not a valid jar file !!!")
            return destFile
        }
        def jarName = jar.name.substring(0, jar.name.length() - JAR.length())
        System.out.println("=========jarName:" + jarName)
        def baseDir = new StringBuilder().append(project.projectDir.absolutePath)
                .append(File.separator).append(FIXED_JAR_DIR)
                .append(File.separator).append(mVersionName)
                .append(File.separator).append(jarName).toString()

        File rootFile = new File(baseDir)
        MyFileUtil.clearFile(rootFile)
        if (!rootFile.mkdirs()) {
            System.err.println("===========mkdirs ${rootFile.absolutePath} failure")
        }
        File unzipDir = new File(rootFile, "classes")
        File jarDir = new File(rootFile, "jar")
        JarFile jarFile = new JarFile(jar)
        for (codeInjectBean in CodeInjectConst.codeInjectList) {

            if (null != codeInjectBean && !TextUtil.isEmpty(codeInjectBean.classname)) {
                def className = codeInjectBean.classname
                System.out.println("===============className:" + className)
                if (MyFileUtil.containsClass(jarFile, className)) {
                    if (!MyFileUtil.hasFiles(unzipDir)) {// 1、判断是否进行过解压缩操作
                        MyFileUtil.unzipJarFile(jarFile, unzipDir)
                        // 2、开始注入文件，需要注意的是，appendClassPath后边跟的根目录，没有后缀，className后完整类路径，也没有后缀
                        classPool.appendClassPath(unzipDir.absolutePath)
                    }
                    // 3、开始注入代码，去除.class后缀
                    if (className.endsWith(CLASS)) {
                        className = className.substring(0, className.length() - CLASS.length())
                    }
                    CtClass ctClass = classPool.getCtClass(className)
                    System.out.println("===============CtClass:" + ctClass == null ? "" : ctClass.getName())
                    if (!ctClass.isInterface() && !ctClass.isAnnotation() && !ctClass.isEnum()) {
                        codeInjectBean.methodInfoList.each { methodInfo ->
                            def methodName = methodInfo.methodname
                            def methodArgs = methodInfo.methodArgs
                            def injectLine = methodInfo.injectLine
                            def injectValue = methodInfo.injectCode
                            CtMethod ctMethod
                            if (methodArgs.isEmpty()) {
                                ctMethod = ctClass.getDeclaredMethod(methodName)
                            } else {
                                def size = methodArgs.size()
                                CtClass[] params = new CtClass[size]
                                for (int i = 0; i < size; i++) {
                                    String param = methodArgs.get(i)
                                    System.out.println("===========param:" + param)
                                    CtClass paramCtClass = classPool.getCtClass(param);
                                    params[i] = paramCtClass;
                                    System.out.println("===========paramCtClass:" + paramCtClass == null ? "" : paramCtClass.getName())
                                }
                                ctMethod = ctClass.getDeclaredMethod(methodName, params)
                            }
                            System.out.println("===============methodName:" + ctMethod == null ? "" : ctMethod.getName())
                            if ("{}".equals(injectValue)) {
                                CtClass exceptionType = classPool.get("java.lang.Throwable")
                                String returnValue = "{\$e.printStackTrace();return null;}"
                                CtClass returnType = ctMethod.getReturnType()
                                if (CtClass.booleanType == returnType) {
                                    returnValue = "{\$e.printStackTrace();return false;}"
                                } else if (CtClass.voidType == returnType) {
                                    returnValue = "{\$e.printStackTrace();return;}"
                                } else if (CtClass.byteType == returnType || CtClass.shortType == returnType || CtClass.charType == returnType || CtClass.intType == returnType || CtClass.floatType ==
                                        returnType || CtClass.doubleType == returnType || CtClass.longType == returnType) {
                                    returnValue = "{\$e.printStackTrace();return 0;}"
                                } else {
                                    returnValue = "{\$e.printStackTrace();return null;}"
                                }
                                ctMethod.addCatch(returnValue, exceptionType)
                            } else {
                                if (injectLine > 0) {
                                    ctMethod.insertAt(injectLine, injectValue)
                                } else if (injectLine == 0) {
                                    ctMethod.insertBefore(injectValue)
                                } else {
                                    if (!injectValue.startsWith("{")) {
                                        injectValue = "{" + injectValue
                                    }
                                    if (!injectValue.endsWith("}")) {
                                        injectValue = injectValue + "}"
                                    }
                                    ctMethod.setBody(injectValue)
                                }
                            }
                        }
                        // 4、循环完毕，写入文件
                        ctClass.writeFile(unzipDir.absolutePath)
                        ctClass.detach()
                    }
                }
            }
        }

        // 5、循环体结束，判断classes文件夹下是否有文件
        if (MyFileUtil.hasFiles(unzipDir)) {
            destFile = new File(jarDir, jar.name)
            MyFileUtil.clearFile(destFile)
            MyFileUtil.zipJarFile(unzipDir, destFile)
            if (!CodeInjectConst.keepTempFile) {
                MyFileUtil.clearFile(unzipDir)
            }
        } else {
            MyFileUtil.clearFile(rootFile)
        }
        jarFile.close()
        return destFile
    }

    private void appendClassPath(ClassPool classPool, File path) {
        if (null != path) {
            if (path.directory) {
                classPool.appendPathList(path.absolutePath)
            } else {
                classPool.appendClassPath(path.absolutePath)
            }
        }
    }

    private void appendDefaultClassPath(ClassPool classPool) {
        if (null == project) {
            return
        }
        def androidJar = new StringBuffer().append(project.android.getSdkDirectory())
                .append(File.separator).append("platforms")
                .append(File.separator).append(project.android.compileSdkVersion)
                .append(File.separator).append("android.jar").toString()

        File file = new File(androidJar);
        if (!file.exists()) {
            androidJar = new StringBuffer().append(project.rootDir.absolutePath)
                    .append(File.separator).append("local.properties").toString()
            Properties properties = new Properties()
            properties.load(new File(androidJar).newDataInputStream())
            def sdkDir = properties.getProperty("sdk.dir")
            androidJar = new StringBuffer().append(sdkDir)
                    .append(File.separator).append("platforms")
                    .append(File.separator).append(mProject.android.compileSdkVersion)
                    .append(File.separator).append("android.jar").toString()
            file = new File(androidJar)
        }
        if (file.exists()) {
            classPool.appendClassPath(androidJar);
        } else {
            System.err.println("couldn't find android.jar file !!!")
        }
    }

    @Override
    String getName() {
        return "FixJarTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS  //Transform输入类型
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT  //该Transform作用域
    }

    @Override
    boolean isIncremental() {
        return false  //返回Transform是否可以执行增量工作
    }
}