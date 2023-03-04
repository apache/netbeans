/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gradle.java.execute;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.spi.actions.AfterBuildActionHook;
import java.io.File;
import java.io.PrintWriter;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

import static org.netbeans.api.java.project.JavaProjectConstants.*;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Laszlo Kishalmi
 */
public class ShowJavadocHook implements AfterBuildActionHook {

    final Project project;

    public ShowJavadocHook(Project project) {
        this.project = project;
    }

    @Override
    public void afterAction(String action, Lookup context, int result, PrintWriter out) {
        if (COMMAND_JAVADOC.equals(action) && (result == 0)) {
            GradleBaseProject gbp = GradleBaseProject.get(project);
            File javadoc = new File(gbp.getBuildDir(), "docs/javadoc/index.html"); //NOI18N
            FileObject fo = FileUtil.toFileObject(javadoc);
            if (fo != null) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(fo.toURL());
            }
        }
    }

}
