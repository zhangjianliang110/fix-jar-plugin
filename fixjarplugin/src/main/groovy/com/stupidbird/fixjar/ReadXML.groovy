package com.stupidbird.fixjar
/**读取 codeInject.xml 配置*/
class ReadXML {

    private static codeInject;

    public static void readXMl(String path) {
        System.out.println("=======================开始解析:" + CodeInjectConst.codeInjectList.size())
        codeInject = new XmlSlurper().parse(new File("${path}${File.separator}${CodeInjectConst.CODE_INJECT_XML}"))
        CodeInjectConst.enablePlugin = codeInject.switch.enable != null && "true" == String.valueOf(codeInject.switch.enable.text())
        CodeInjectConst.keepTempFile = codeInject.switch.keepTempFile != null && "true" == String.valueOf(codeInject.switch.keepTempFile.text())
        CodeInjectConst.codeInjectList.clear()
        /*读取xml，获取要注入的代码信息*/
        for (classInfo in codeInject.fixjar.classInfo) {
            CodeInjectBean bean = new CodeInjectBean();
            bean.classname = classInfo.name.text()
            System.out.println("=============解析到className：" + bean.classname)
            bean.methodInfoList = new ArrayList<>();
            for (methodInfo in classInfo.method) {
                MethodInfo method = new MethodInfo();
                method.methodname = methodInfo.name.text()
                System.out.println("=============解析到methodName：" + method.methodname)
                try {
                    method.injectLine = Integer.parseInt(methodInfo.line.text())
                } catch (Exception ignored) {
                    method.injectLine = -1
                }
                System.out.println("=============解析到injectLine：" + method.injectLine)
                if (method.methodArgs == null) {
                    method.methodArgs = new ArrayList<>()
                }
                for (argname in methodInfo.args.argname) {
                    method.methodArgs.add(argname)
                    System.out.println("=============解析到argname：" + argname)
                }

                method.injectCode = methodInfo.code.text()
                System.out.println("=============解析到injectCode：" + method.injectCode)
                bean.methodInfoList.add(method)
            }
            CodeInjectConst.codeInjectList.add(bean)
        }
        System.out.println("=======================解析结束 codeInjectList size:" + CodeInjectConst.codeInjectList.size())
    }
}
