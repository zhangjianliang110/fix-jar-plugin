package com.stupidbird.fixjar;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created by zhangjianliang on 2019/7/18
 */
public class MethodInfo {
    public String methodname;
    public int injectLine;
    public String injectCode;

    public List<String> methodArgs = new ArrayList<>();

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String arg : methodArgs) {
            sb.append(arg).append(",");
        }
        return "["
                + "methodName:" + methodname
                + ",methodArgs:" + sb.toString()
                + "injectLine:" + injectLine
                + ",injectCode:" + injectCode
                + "]";
    }
}
