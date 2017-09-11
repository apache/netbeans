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
package org.netbeans.modules.versioning.core.spi.testvcs;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.*;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
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
        metadataFolderNames={
                TestVCS.TEST_VCS_METADATA, 
                "set:getenv:PATH:notnull", 
                "notset:getenv:SOMENOTSETVARIABLE:notnull", 
                "null:getenv:whatever:null"}
)
public class TestVCS extends VersioningSystem {

    private static TestVCS instance;
    
    private VCSInterceptor interceptor;
    private VCSAnnotator annotator;
    private VCSHistoryProvider historyProvider;
    private VCSVisibilityQuery vq;
    private TestVCSCollocationQuery vcq;

    public static final String TEST_VCS_METADATA = ".testvcs";
    public static final String VERSIONED_FOLDER_SUFFIX = "-test-versioned";
    public static String ALWAYS_WRITABLE_PREFIX = "alwayswritable-";

    public static TestVCS getInstance() {
        return instance;
    }
    
    public static void resetInstance() {
        instance = null;
    }
    
    public TestVCS() {
        instance = this;
        interceptor = new TestVCSInterceptor();
        annotator = new TestVCSAnnotator();
        historyProvider = new TestVCSHistoryProvider();
        vq = new TestVCSVisibilityQuery();
        vcq = new TestVCSCollocationQuery();
    }

    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
        VCSFileProxy topmost = null;
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

    @Override
    public CollocationQueryImplementation2 getCollocationQueryImplementation() {
        return vcq;
    }

    public void fire() {
        // do not fire a null here. Some tests may fail because:
        // java.lang.NullPointerException
	// at org.netbeans.modules.versioning.core.VersioningAnnotationProvider.refreshAnnotations(VersioningAnnotationProvider.java:345)
	// at org.netbeans.modules.versioning.core.VersioningAnnotationProvider.refreshAnnotations(VersioningAnnotationProvider.java:325)
        // at org.netbeans.modules.versioning.core.VersioningManager.propertyChange(VersioningManager.java:560)
	// at java.beans.PropertyChangeSupport.fire(PropertyChangeSupport.java:335)
	// at java.beans.PropertyChangeSupport.firePropertyChange(PropertyChangeSupport.java:327)
	// at java.beans.PropertyChangeSupport.firePropertyChange(PropertyChangeSupport.java:263)
	// at org.netbeans.modules.versioning.core.spi.VersioningSystem.fireStatusChanged(VersioningSystem.java:193)
	// at org.netbeans.modules.versioning.core.spi.VersioningSystem.fireStatusChanged(VersioningSystem.java:211)
	// at org.netbeans.modules.versioning.core.spi.testvcs.TestVCS.fire(TestVCS.java:133)
	// at org.netbeans.modules.versioning.core.DelegatingVCSTest.testListeners(DelegatingVCSTest.java:161)
        VCSFileProxy file = VCSFileProxy.createFileProxy(new File("dummy"));
        super.fireStatusChanged(file);
    }
    
    public void setVCSInterceptor(VCSInterceptor externalyMovedInterceptor) {
        interceptor = externalyMovedInterceptor;
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
    
    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return historyProvider;
    }    
}
