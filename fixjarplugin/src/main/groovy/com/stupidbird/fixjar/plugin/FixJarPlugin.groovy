package com.stupidbird.fixjar.plugin

import com.stupidbird.fixjar.ReadXML
import com.stupidbird.fixjar.transform.FixJarTransform
import com.android.build.gradle.AppExtension
import com.stupidbird.fixjar.CodeInjectConst
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * 代码注入插件
 */
public class FixJarPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new GradleException("CodeInjectPlugin: Android Application plugin required")
        }
        def android = project.extensions.findByType(AppExtension.class)
        ReadXML.readXMl(project.projectDir.path)
        if (CodeInjectConst.enablePlugin) {
            android.registerTransform(new FixJarTransform(project))
        }
    }
}