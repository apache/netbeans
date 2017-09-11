/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.gsf.testrunner.ui.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class UICommonUtils {
    
    private static final Logger LOG = Logger.getLogger(UICommonUtils.class.getName());
    
    /**
     * Extracts {@code FileObject}s from the given nodes.
     * Nodes that have (direct or indirect) parent nodes among the given
     * nodes are ignored.
     *
     * @return  a non-empty array of {@code FileObject}s
     *          represented by the given nodes;
     *          or {@code null} if no {@code FileObject} was found;
     */
    public static FileObject[] getFileObjectsFromNodes(final Node[] nodes){
        List<FileObject> fos = new ArrayList<FileObject>(nodes.length);

        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            final FileObject fo;
            if (!hasParentAmongNodes(nodes, i)
                    && ((fo = getFileObject(node, true)) != null)) {
                fos.add(fo);
            }
        }
        return fos.isEmpty() ? null : fos.toArray(new FileObject[fos.size()]);
    }

    private static boolean hasParentAmongNodes(final Node[] nodes,
                                               final int idx) {
        Node node;

        node = nodes[idx].getParentNode();
        while (null != node) {
            for (int i = 0; i < nodes.length; i++) {
                if (i == idx) {
                    continue;
                }
                if (node == nodes[i]) {
                    return true;
                }
            }
            node = node.getParentNode();
        }
        return false;
    }

    /**
     * Grabs and checks a <code>FileObject</code> from the given node.
     * If either the file could not be grabbed or the file does not pertain
     * to any project, a message is displayed.
     *
     * @param  node  node to get a <code>FileObject</code> from.
     * @return  the grabbed <code>FileObject</code>,
     *          or <code>null</code> in case of failure
     */
    @NbBundle.Messages({"# {0} - selected node's display name",
        "MSG_file_from_node_failed=File cannot be found for selected node: {0}.",
        "# {0} - source file",
        "MSG_no_project=Source file {0} does not belong to any project."})
    private static FileObject getFileObject(final Node node, boolean justLogIt) {
        final FileObject fo = getFileObjectFromNode(node);
        if (fo == null) {
            if(justLogIt) {
                LOG.info(Bundle.MSG_file_from_node_failed(node.getDisplayName()));
            } else {
                notifyUser(Bundle.MSG_file_from_node_failed(node.getDisplayName()));
            }
            return null;
        }
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            if(justLogIt) {
                LOG.info(Bundle.MSG_no_project(fo));
            } else {
                notifyUser(Bundle.MSG_no_project(fo));
            }
            return null;
        }
        return fo;
    }
    
    public static FileObject getFileObjectFromNode(Node node) {
        FileObject fo = node.getLookup().lookup(FileObject.class);
        if(fo == null) {
            Children children = node.getChildren();
            for(Node child : children.getNodes()) {
                fo = child.getLookup().lookup(FileObject.class);
                if(fo != null) {
                    return child.getDisplayName().equals("<default package>") ? fo : fo.getParent();
                }
            }
        }
        return fo;
    }

    /**
     * Show error message box. 
     */
    public static void notifyUser(String msg) {
        notifyUser(msg, NotifyDescriptor.ERROR_MESSAGE);
    }
    
    /**
     * Show message box of the specified severity. 
     */
    public static void notifyUser(String msg, int messageType) {
        NotifyDescriptor descr = new NotifyDescriptor.Message(msg, messageType);
        DialogDisplayer.getDefault().notify(descr);
    }
    
}
