/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.openjdk.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class BuildUtils {

    private BuildUtils() {}

    public static File findTargetJavaHome(FileObject file) {
        File buildDir = getBuildTargetDir(file);

        if (buildDir != null) {
            File candidate = new File(buildDir, "images/j2sdk-image");

            if (candidate.isDirectory()) {
                return candidate;
            } else {
                return new File(buildDir, "jdk");
           }
        }

        Project prj = FileOwnerQuery.getOwner(file);
        File projectDirFile = FileUtil.toFile(prj.getProjectDirectory());
        File userHome = new File(System.getProperty("user.home"));
        List<PropertyProvider> properties = new ArrayList<>();

        properties.add(PropertyUtils.propertiesFilePropertyProvider(new File(projectDirFile, "build.properties")));
        properties.add(PropertyUtils.propertiesFilePropertyProvider(new File(userHome, ".openjdk/langtools-build.properties")));
        properties.add(PropertyUtils.propertiesFilePropertyProvider(new File(userHome, ".openjdk/build.properties")));
        properties.add(PropertyUtils.propertiesFilePropertyProvider(new File(projectDirFile, "make/build.properties")));

        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.globalPropertyProvider(), properties.toArray(new PropertyProvider[0]));

        return new File(evaluator.evaluate("${target.java.home}"));
    }

    public static File getBuildTargetDir(FileObject file) {
        return getBuildTargetDir(FileOwnerQuery.getOwner(file));
    }

    public static File getBuildTargetDir(Project prj) {
        for (String possibleRootLocation : new String[] {"../../..", "../.."}) {
            FileObject possibleJDKRoot = BuildUtils.getFileObject(prj.getProjectDirectory(), possibleRootLocation);
            Object buildAttr = possibleJDKRoot != null ? possibleJDKRoot.getAttribute(NB_JDK_PROJECT_BUILD) : null;

            if (buildAttr instanceof File) {
                return (File) buildAttr;
            }
        }

        return null;
    }

    public static final String NB_JDK_PROJECT_BUILD = "nb-jdk-project-build";

    @SuppressWarnings("org.netbeans.modules.java.openjdk.common.BuildUtils.getFileObject")
    public static FileObject getFileObject(FileObject dir, String relpath) {
        int pos = 0;

        while ((relpath.startsWith("../", pos)) || (relpath.endsWith("..") && pos + 2 == relpath.length())) {
            dir = dir.getParent();
            if (dir == null) {
                return null;
            }
            pos += 3;
        }

        return pos < relpath.length() ? dir.getFileObject(relpath.substring(pos)) : dir;
    }
}
