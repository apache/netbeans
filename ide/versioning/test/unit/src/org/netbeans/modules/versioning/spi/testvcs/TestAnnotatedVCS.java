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
package org.netbeans.modules.versioning.spi.testvcs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VCSAnnotator;

import java.io.File;
import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * Test versioning system.
 * 
 * @author Maros Sandor
 */
@VersioningSystem.Registration(
        actionsCategory="TestVCS", 
        displayName="TestVCSDisplay", 
        menuLabel="TestVCSMenu", 
        metadataFolderNames={TestAnnotatedVCS.TEST_VCS_METADATA, "set:getenv:PATH:notnull", "notset:getenv:SOMENOTSETVARIABLE:notnull", "null:getenv:whatever:null"})
public class TestAnnotatedVCS extends VersioningSystem {

    public static TestAnnotatedVCS INSTANCE;
    private VCSInterceptor interceptor;
    private VCSAnnotator annotator;
    private VCSVisibilityQuery vq;

    public static final String TEST_VCS_METADATA = ".testvcs";
    public static final String VERSIONED_FOLDER_SUFFIX = "-test-versioned-annotated";

    public TestAnnotatedVCS() {
        INSTANCE = this;
        interceptor = new TestVCSInterceptor();
        annotator = new TestVCSAnnotator();
        vq = new TestVCSVisibilityQuery();
        putProperty(PROP_DISPLAY_NAME, "TestVCSDisplay");
        putProperty(PROP_MENU_LABEL, "TestVCSMenu");
    }

    public File getTopmostManagedAncestor(File file) {
        File topmost = null;
        for (; file != null; file = file.getParentFile()) {
            if (file.getName().endsWith(VERSIONED_FOLDER_SUFFIX)) {
                topmost = file;
            }
        }
        return topmost;
    }

    public VCSInterceptor getVCSInterceptor() {
        return interceptor;
    }

    public VCSAnnotator getVCSAnnotator() {
        return annotator;
    }

    @Override
    public VCSVisibilityQuery getVisibilityQuery() {
        return vq;
    }

    public void fire() {
        File file = null;
        super.fireStatusChanged(file);
    }
    
    @ActionID(id = "vcs.delegatetest.init", category = "TestVCS")
    @ActionRegistration(displayName = "InitAction", popupText="InitActionPopup", menuText="InitActionMenu")
    @ActionReferences({@ActionReference(path="Versioning/TestVCS/Actions/Unversioned")})
    public static class InitAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) { }
    }
    
    @ActionID(id = "vcs.delegatetest.global", category = "TestVCS")
    @ActionRegistration(displayName = "GobalAction", popupText="GlobalActionPopup", menuText="GlobalActionMenu")
    @ActionReferences({@ActionReference(path="Versioning/TestVCS/Actions/Global")})
    public static class GlobalAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) { }
    }
}
