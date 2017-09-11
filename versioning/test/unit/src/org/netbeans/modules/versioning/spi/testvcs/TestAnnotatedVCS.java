/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
