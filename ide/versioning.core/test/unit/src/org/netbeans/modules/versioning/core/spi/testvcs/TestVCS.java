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
