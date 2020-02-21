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

package org.netbeans.modules.cnd.classview;

import java.io.File;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbPreferences;

/**
 * base class for class view golden tests
 *
 */
public class BaseTestCase extends TraceModelTestBase implements CsmModelListener {
    private boolean isReparsed;
    
    public BaseTestCase(String testName, boolean isReparsed) {
        super(testName);
        this.isReparsed = isReparsed;
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.classview.no-loading-node","true"); // NOI18N
	Preferences ps = NbPreferences.forModule(ClassViewTopComponent.class);
	ps.putBoolean(ClassViewTopComponent.OPENED_PREFERENCE, true);
	super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected @Override void postTest(String[] args, Object... params) {
        CsmProject project = getCsmProject();
        assertNotNull("Project not found",project); // NOI18N
        childrenUpdater = new ChildrenUpdater();
        CsmNamespace globalNamespace = project.getGlobalNamespace();
        NamespaceKeyArray global = new NamespaceKeyArray(childrenUpdater, globalNamespace);
        dump(global,"", !isReparsed);
        CsmListeners.getDefault().addModelListener(this);
        for(CsmFile file : project.getHeaderFiles()){
            reparseFile(file);
        }
        dump(global,"", isReparsed);
    }    

    private void dump(final HostKeyArray children, String ident, boolean trace){
        final Node[][] nodes = new Node[][] { null };
        try {
            // let NB to do remained work on children
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            // skip
        }
        HostKeyArray.MUTEX.writeAccess(new Runnable() {
            public void run() {
                nodes[0] = children.getNodes();
            }
        });
        for(Node node : nodes[0]){
            String res = ident+node.getDisplayName()+" / "+getNodeIcon(node); // NOI18N
            if (trace) {
                System.out.println(res);
            }
            Children child = node.getChildren();
            if (child instanceof HostKeyArray){
                dump((HostKeyArray)child, ident+"\t", trace); // NOI18N
            }
        }
    }
    private String getNodeIcon(Node node){
        CsmObject obj = ((AbstractCsmNode)node).getCsmObject();
        String path = CsmImageLoader.getImagePath(obj);
        return new File(path).getName();
    }

    private ChildrenUpdater childrenUpdater;

    public void projectOpened(CsmProject project) {
    }

    public void projectClosed(CsmProject project) {
    }

    public void modelChanged(CsmChangeEvent e) {
        childrenUpdater.update(new SmartChangeEvent(e));
    }
}
