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
