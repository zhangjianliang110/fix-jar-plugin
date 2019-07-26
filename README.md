# fix-jar-plugin
编译时修复第三方库的插件

### 使用方式
#### 1. 在项目app module下创建配置文件  codeInject.xml
#### 2. 在 codeInjet.xml 中，配置要修复的类名、方法名、方法参数、方法体、代码插入方法的行号
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <switch>
        <!--是否关闭插件功能-->
        <enable>true</enable>
        <!--是否保留注入代码后生成的临时代码文件-->
        <keepTempFile>true</keepTempFile>
    </switch>
    <!--需要注入代码的类/方法/方法体-->
    <fixjar>
        <classInfo>
            <!-- 要修复的代码所在的类 -->
            <name>com.google.gson.Gson</name>
            <method>
                <!-- 要修复的代码所在的方法 -->
                <name>toJson</name>
                <!-- 新增代码在该方法中插入的行号，
                      0：表示插入方法最前面；
                      -1：表示新增代码整体替换该方法中原有代码
                       其他任意 > 0 的数字：表示插入该方法的具体行数
                       -->
                <line>1</line>
                <!-- 方法参数，可配置多个参数，每个<argname>标签表示一个参数，argname标签的上下顺序代表方法中参数的先后顺序 -->
                <args>
                    <argname>java.lang.Object</argname>
                </args>
                <!-- 具体要插入的代码 -->
                <code>java.lang.System.out.println("Gson.toJson(Object)注入代码成功");</code>
            </method>
        </classInfo>
    </fixjar>
</resources>
```
#### 3.配置依赖
1)项目根目录下 build.gradle 中 repositories 尾部添加：
<pre><code>
allprojects {
    repositories {
        ...
        maven {url 'https://bintray.com/zhangjianliang/fix-jar-plugin/'}
    }
}
</code></pre>
2)根目录下 build.gradle 中  dependencies 中添加依赖：
<pre><code>
dependencies {
    classpath 'com.stupidbird.fixjar:fixjar:1.0.0'
}
</code></pre>
3)在 app module目录下 build.gradle 中，最底部，引入插件
<pre><code>
apply plugin: 'com.stupidbird.fixjar.fixjarplugin'
</code></pre>



## 注意事项
   1.参数、注入的代码，要带上包名，否则代码注入失败  
   2.当代码注入失败时，会继续使用修改前的代码
