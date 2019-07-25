## 插件功能
1.对apk进行V2签名  执行命令 v2Sign
2.对apk写入渠道信息  执行命令 localWriteChannel

### 主要针对360加固，公司购买的360天机加固服务，加固后的apk需要手动签名
    在Akulaku中，除了要手动签名，还要重新写入渠道信息。

### 注意事项
    1.在项目根目录下，创建文件名为 autoSignConfig.txt 的签名配置文件
    内容：
    inputDir=./autotool-input/
    outputDir=./autotool-output/
    signatureFile=./keystore/xxx-keystore
    storePass=xxx
    storeAlias=xxx
    keyPass=xxx

    以上字段
    inputDir 是输入目录，要签名的apk所在目录
    outputDir 最终发布包所在的输出目录
    signatureFile  签名文件  keystore文件相对路径
    storePass  签名密码
    storeAlias  签名Alias
    keyPass     密码

    2.如果需要多渠道打包，需要创建文件名为 channel.txt 的渠道配置文件
    文件内容：
    googlePlay:
    xiaomi:kztt0in
    oppo:2esh8cn
    huawei:y5z9vk6

    左边是渠道名，右边是渠道值

