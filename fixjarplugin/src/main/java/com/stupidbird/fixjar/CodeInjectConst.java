package com.stupidbird.fixjar;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;

/**
 * Description:
 * Created by zhangjianliang on 2019/7/18
 */
public class CodeInjectConst {
    public final static String CODE_INJECT_XML = "codeInject.xml";

    public static boolean enablePlugin;//插件是否开启

    public static boolean keepTempFile;//是否保留输出的临时代码

    public static List<CodeInjectBean> codeInjectList = new ArrayList<>();

    public static CodeInjectBean getInjectBeanByClass(CtClass clazz) {
        String className = clazz.getName();
        for (CodeInjectBean bean : CodeInjectConst.codeInjectList) {
            if (bean == null || !bean.classname.equals(className)) {
                continue;
            }
            return bean;
        }
        return null;
    }
}
