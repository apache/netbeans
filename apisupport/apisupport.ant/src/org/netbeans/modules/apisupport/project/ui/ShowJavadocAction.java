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

package org.netbeans.modules.apisupport.project.ui;

import java.net.URL;
import java.net.MalformedURLException;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

// XXX this class is more or less copy-pasted PlatformNode from j2seproject.
// Get rid of it as soon as "some" Libraries Node API is provided.

/**
 * Action for showing Javadoc. The action looks up
 * the {@link ShowJavadocAction.JavadocProvider} in the
 * activated node's Lookup and delegates to it.
 * @author Tomas Zezula
 */
final class ShowJavadocAction extends NodeAction {
    
    /**
     * Implementation of this interfaces has to be placed
     * into the node's Lookup to allow {@link ShowJavadocAction}
     * on the node.
     */
    public static interface JavadocProvider {
        
        /**
         * Checks if the node can provide Javaodc
         * @return true if the action should be enabled
         */
        public abstract boolean hasJavadoc();
        
        /**
         * Opens javadoc page in the browser
         */
        public abstract void showJavadoc();
    }
    
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length!=1) {
            return;
        }
        JavadocProvider jd = activatedNodes[0].getLookup().lookup(JavadocProvider.class);
        if (jd == null) {
            return;
        }
        jd.showJavadoc();
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length!=1) {
            return false;
        }
        JavadocProvider jd = activatedNodes[0].getLookup().lookup(JavadocProvider.class);
        if (jd == null) {
            return false;
        }
        return jd.hasJavadoc();
    }
    
    public final String getName() {
        return NbBundle.getMessage(ShowJavadocAction.class,"CTL_ShowJavadoc");
    }
    
    public final HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.apisupport.project.ui.ShowJavadocAction");
    }
    
    public @Override final boolean asynchronous() {
        return false;
    }
    
    /**
     * Opens the IDE default browser with given URL
     * @param javadoc URL of the javadoc page
     * @param displayName the name of file to be displayed, typically the package name for class
     * or project name for project.
     */
    static void showJavaDoc(URL javadoc, String displayName) {
        if (javadoc!=null) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(javadoc);
        } else {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(ShowJavadocAction.class, "TXT_NoJavadoc",
                    displayName));
        }
    }
    
    /**
     * Locates a javadoc page by a relative name and an array of javadoc roots
     * @param resource the relative name of javadoc page
     * @param urls the array of javadoc roots
     * @return the URL of found javadoc page or null if there is no such a page.
     */
    static URL findJavadoc(String resource, URL urls[]) {
        for (int i = 0; i < urls.length; i++) {
            String base = urls[i].toExternalForm();
            if (!base.endsWith("/")) { // NOI18N
                base+="/"; // NOI18N
            }
            try {
                URL u = new URL(base+resource);
                FileObject fo = URLMapper.findFileObject(u);
                if (fo != null) {
                    return u;
                }
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        "Cannot create URL for " + base + resource + ". " + ex.toString()); //NOI18N
                continue;
            }
        }
        return null;
    }
    
}
