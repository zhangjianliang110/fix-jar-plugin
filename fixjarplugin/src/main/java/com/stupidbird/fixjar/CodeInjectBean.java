package com.stupidbird.fixjar;

import java.util.List;

/**
 * Description:
 * Created by zhangjianliang on 2019/7/18
 */
public class CodeInjectBean {

    public String classname;

    public List<MethodInfo> methodInfoList;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (methodInfoList != null && !methodInfoList.isEmpty()) {
            for (MethodInfo method : methodInfoList) {
                if (method != null) {
                    sb.append(method.toString()).append("\n");
                }
            }
        }
        return "{"
                + "className:" + classname
                + ",methodInfo:" + sb.toString()
                + "}";
    }
}
