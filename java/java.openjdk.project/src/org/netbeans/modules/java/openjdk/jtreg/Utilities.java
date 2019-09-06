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
package org.netbeans.modules.java.openjdk.jtreg;

import java.io.File;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.netbeans.modules.java.openjdk.common.ShortcutUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class Utilities {

    public static boolean isJDKRepository(FileObject root) {
        if (root == null)
            return false;

        FileObject srcDir = BuildUtils.getFileObject(root, "src");

        if (srcDir == null)
            return false;

        if (BuildUtils.getFileObject(srcDir, "share/classes") != null)
            return true;

        for (FileObject mod : srcDir.getChildren()) {
            if (BuildUtils.getFileObject(mod, "share/classes") != null)
                return true;
        }

        return false;
    }

    public static boolean isLangtoolsRepository(FileObject root) {
        return (BuildUtils.getFileObject(root, "src/share/classes/com/sun/tools/javac/main/Main.java") != null ||
                BuildUtils.getFileObject(root, "src/jdk.compiler/share/classes/com/sun/tools/javac/main/Main.java") != null) &&
                BuildUtils.getFileObject(root, "src/java.base/share/classes/java/lang/Object.java") == null;
    }

    public static FileObject getLangtoolsKeyRoot(FileObject root) {
        FileObject jdkCompiler = BuildUtils.getFileObject(root, "src/jdk.compiler/share/classes");

        if (jdkCompiler != null)
            return jdkCompiler;

        jdkCompiler = root.getFileObject("src/jdk.compiler/share/classes");

        if (jdkCompiler != null)
            return jdkCompiler;

        return BuildUtils.getFileObject(root, "src/share/classes");
    }
    
    public static File jtregOutputDir(FileObject testFile) {
        File buildDir = BuildUtils.getBuildTargetDir(testFile);
        Project prj = FileOwnerQuery.getOwner(testFile);

        if (buildDir != null) {
            FileObject repo = prj.getProjectDirectory().getParent().getParent();
            if (repo.getNameExt().equals("langtools") &&
                ShortcutUtils.getDefault().shouldUseCustomTest(repo.getNameExt(), FileUtil.getRelativePath(repo, testFile))) {
                buildDir = new File(FileUtil.toFile(prj.getProjectDirectory()), "../../build");
            } else if ("langtools".equals(ShortcutUtils.getDefault().inferLegacyRepository(prj)) &&
                ShortcutUtils.getDefault().shouldUseCustomTest(repo.getNameExt(), FileUtil.getRelativePath(repo, testFile))) {
                buildDir = new File(FileUtil.toFile(prj.getProjectDirectory()), "../../build/langtools");
            }
        } else {
            buildDir = new File(FileUtil.toFile(prj.getProjectDirectory()), "../../../build");
        }

        return new File(buildDir, "nb-jtreg").toPath().normalize().toFile();
    }

}
