/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

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

package org.netbeans.modules.gradle.javaee.web;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.javaee.api.GradleWebProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.CssPreprocessor;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.CssPreprocessorsListener;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(service = CssPreprocessorsListener.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/war")
public class CssPreprocessorsSupport implements CssPreprocessorsListener {

    private final Project project;

    public CssPreprocessorsSupport(Project project) {
        this.project = project;
    }

    public void recompileSources(CssPreprocessor cssPreprocessor) {
        assert cssPreprocessor != null;
        GradleWebProject wp = GradleWebProject.get(project);
        if (wp != null) {
            FileObject docBase = FileUtil.toFileObject(wp.getWebAppDir());
            if (docBase != null) {
                CssPreprocessors.getDefault().process(cssPreprocessor, project, docBase);
            }
        }
    }

    @Override
    public void preprocessorsChanged() {
    }

    @Override
    public void optionsChanged(CssPreprocessor cssPreprocessor) {
        recompileSources(cssPreprocessor);
    }

    @Override
    public void customizerChanged(Project project, CssPreprocessor cssPreprocessor) {
        if (project.equals(project)) {
            recompileSources(cssPreprocessor);
        }
    }

    @Override
    public void processingErrorOccured(Project project, CssPreprocessor cssPreprocessor, String error) {
    }
}
