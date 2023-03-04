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
