/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.common.ui.refactoring;

import java.util.Collection;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Generic refactoring UI for folder rename for web-like projects
 *
 * @author marekfukala
 */
@ServiceProvider(service = ActionsImplementationProvider.class, position = 100000)
public class FolderActionsImplementationProvider extends ActionsImplementationProvider {

    @Override
    public boolean canRename(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        //we are able to rename only one node selection [at least for now ;-) ]
        if (nodes.size() != 1) {
            return false;
        }

        //apply only on supported mimetypes and if not invoked in editor context
        Node node = nodes.iterator().next();
        EditorCookie ec = getEditorCookie(node);
        if (ec == null || !isFromEditor(ec)) {
            FileObject fo = getFileObjectFromNode(node);
            if (fo != null && fo.isValid() && fo.isFolder()) {
                //check if the folder is a part of a web-like project using the
                //web root query
                if (ProjectWebRootQuery.getWebRoot(fo) != null) {

                    // In Maven based projects the rename action need to do something different
                    // See issue #219887
                    FileObject pom = fo.getFileObject("pom.xml"); //NOI18N
                    if (pom != null) {
                        return false;
                    }

                    //looks like the file is a web like project
                    try {
                        return !fo.getFileSystem().isReadOnly();
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        return false;

    }

    @Override
    public void doRename(Lookup selectedNodes) {
        Collection<? extends Node> nodes = selectedNodes.lookupAll(Node.class);
        assert nodes.size() == 1;
        Node node = nodes.iterator().next();
        FileObject file = getFileObjectFromNode(node);
        String newName = getName(selectedNodes);
        UI.openRefactoringUI(new RenameRefactoringUI(file, newName != null ? newName : file.getName()));
    }

    private static FileObject getFileObjectFromNode(Node node) {
        DataObject dobj = node.getLookup().lookup(DataObject.class);
        return dobj != null ? dobj.getPrimaryFile() : null;
    }

    private static boolean isFromEditor(final EditorCookie ec) {
        return Mutex.EVENT.readAccess(new Action<Boolean>() {
            @Override
            public Boolean run() {
                if (ec != null && ec.getOpenedPanes() != null) {
                    TopComponent activetc = TopComponent.getRegistry().getActivated();
                    if (activetc instanceof CloneableEditorSupport.Pane) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private static EditorCookie getEditorCookie(Node node) {
        return node.getLookup().lookup(EditorCookie.class);
    }

    private static String getName(Lookup look) {
        ExplorerContext ren = look.lookup(ExplorerContext.class);
        if (ren==null)
            return null;
        return ren.getNewName(); //NOI18N
    }

}
