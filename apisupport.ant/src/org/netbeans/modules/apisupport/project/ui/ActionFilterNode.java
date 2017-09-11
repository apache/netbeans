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

package org.netbeans.modules.apisupport.project.ui;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.actions.EditAction;
import org.openide.actions.FindAction;
import org.openide.loaders.DataObject;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;

// XXX this class is simplified version of the same class in the j2seproject.
// Get rid of it as soon as "some" Libraries Node API is provided.

/**
 * This class decorates package nodes and file nodes under the Libraries Nodes.
 * It removes all actions from these nodes except of file node's {@link OpenAction}
 * and package node's {@link FindAction}. It also adds the {@link ShowJavadocAction}
 * to both file and package nodes.
 */
class ActionFilterNode extends FilterNode {
    
    private static final int MODE_PACKAGE = 2;
    private static final int MODE_FILE = 3;
    private static final int MODE_FILE_CONTENT = 4;
    
    private final int mode;
    private Action[] actionCache;
    
    /**
     * Creates new ActionFilterNode for class path root.
     * @param original the original node
     * @return ActionFilterNode
     */
    static ActionFilterNode create(Node original) {
        DataObject dobj = original.getLookup().lookup(DataObject.class);
        FileObject root;
        Lookup lkp;
        if (dobj != null) {
            root =  dobj.getPrimaryFile();
            lkp = new ProxyLookup(original.getLookup(), Lookups.singleton(new JavadocProvider(root, root)));
        } else {
            // #169568: dummy node
            root = null;
            lkp = new ProxyLookup(original.getLookup());
            Logger.getLogger(ActionFilterNode.class.getName())
                    .log(Level.WARNING, "DataObject not found in lookup of " + original.getDisplayName() + ", returning dummy node.");
        }
        return new ActionFilterNode(original, MODE_PACKAGE, root, lkp);
    }
    
    private ActionFilterNode(Node original, int mode, FileObject cpRoot, FileObject resource) {
        this(original, mode, cpRoot,
                new ProxyLookup(original.getLookup(),Lookups.singleton(new JavadocProvider(cpRoot,resource))));
    }
    
    private ActionFilterNode(Node original, int mode) {
        super(original, original.isLeaf() ? Children.LEAF : new ActionFilterChildren(original, mode, null));
        this.mode = mode;
    }
    
    private ActionFilterNode(Node original, int mode, FileObject root, Lookup lkp) {
        super(original, original.isLeaf() ? Children.LEAF : new ActionFilterChildren(original, mode,root),lkp);
        this.mode = mode;
    }
    
    public Action[] getActions(boolean context) {
        Action[] result = initActions();
        return result;
    }
    
    public Action getPreferredAction() {
        if (mode == MODE_FILE) {
            Action[] actions = initActions();
            if (actions.length > 0 && (isOpenAction(actions[0]))) {
                return actions[0];
            }
        }
        return null;
    }
    
    private Action[] initActions() {
        if (actionCache == null) {
            List<Action> result = new ArrayList<Action>(2);
            if (mode == MODE_FILE) {
                for (Action superAction : super.getActions(false)) {
                    if (isOpenAction(superAction)) {
                        result.add(superAction);
                    }
                }
                result.add(SystemAction.get(ShowJavadocAction.class));
            } else if (mode == MODE_PACKAGE) {
                result.add(SystemAction.get(ShowJavadocAction.class));
                for (Action superAction : super.getActions(false)) {
                    if (isFindAction(superAction)) {
                        result.add(superAction);
                    }
                }
            }
            actionCache = result.toArray(new Action[result.size()]);
        }
        return actionCache;
    }
    
    private static boolean isOpenAction(final Action action) {
        if (action == null) {
            return false;
        }
        if (action instanceof OpenAction || action instanceof EditAction) {
            return true;
        }
        if ("org.netbeans.api.actions.Openable".equals(action.getValue("type"))) { //NOI18N
            return true;
        }
        return false;
    }
    
    private static boolean isFindAction(final Action action) {
        if (action == null) {
            return false;
        }
        if (action instanceof FindAction) {
            return true;
        }
        if ("org.openide.actions.FindAction".equals(action.getValue("key"))) { //NOI18N
            return true;
        }
        return false;
    }
    
    private static class ActionFilterChildren extends FilterNode.Children {
        
        private final int mode;
        private final FileObject cpRoot;
        
        ActionFilterChildren(Node original, int mode, FileObject cpRooot) {
            super(original);
            this.mode = mode;
            this.cpRoot = cpRooot;
        }
        
        protected Node[] createNodes(Node n) {
            switch (mode) {
                case MODE_PACKAGE:
                    FileObject fobj = n.getLookup().lookup(FileObject.class);
                    if (fobj == null) {
                        return super.createNodes(n); // "Please wait..." perhaps
                    } else if (fobj.isFolder()) {
                        return new Node[] {new ActionFilterNode(n, MODE_PACKAGE, cpRoot, fobj)};
                    } else {
                        return new Node[] {new ActionFilterNode(n, MODE_FILE, cpRoot, fobj)};
                    }
                case MODE_FILE:
                case MODE_FILE_CONTENT:
                    return new Node[] {new ActionFilterNode(n, MODE_FILE_CONTENT)};
                default:
                    assert false : "Unknown mode";  //NOI18N
                    return new Node[0];
            }
        }
        
    }
    
    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {
        
        private final FileObject cpRoot;
        private final FileObject resource;
        
        JavadocProvider(FileObject cpRoot, FileObject resource) {
            this.cpRoot = cpRoot;
            this.resource = resource;
        }
        
        public boolean hasJavadoc() {
            boolean rNotNull = resource != null;
            int jLength = JavadocForBinaryQuery.findJavadoc(cpRoot.toURL()).getRoots().length;
            return  rNotNull && jLength > 0;
        }
        
        public void showJavadoc() {
                String relativeName = FileUtil.getRelativePath(cpRoot, resource);
                URL[] urls = JavadocForBinaryQuery.findJavadoc(cpRoot.toURL()).getRoots();
                URL pageURL;
                if (relativeName.length() == 0) {
                    pageURL = ShowJavadocAction.findJavadoc("overview-summary.html",urls); //NOI18N
                    if (pageURL == null) {
                        pageURL = ShowJavadocAction.findJavadoc("index.html",urls); //NOI18N
                    }
                } else if (resource.isFolder()) {
                    //XXX Are the names the same also in the localized javadoc?
                    pageURL = ShowJavadocAction.findJavadoc(relativeName + "/package-summary.html", urls); //NOI18N
                } else {
                    String javadocFileName = relativeName.substring(0, relativeName.lastIndexOf('.')) + ".html"; //NOI18Ns
                    pageURL = ShowJavadocAction.findJavadoc(javadocFileName, urls);
                }
                ShowJavadocAction.showJavaDoc(pageURL,relativeName.replace('/','.'));  //NOI18N
        }
        
    }
    
}
