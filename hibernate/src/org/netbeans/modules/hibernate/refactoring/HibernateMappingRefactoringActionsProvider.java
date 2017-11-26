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
package org.netbeans.modules.hibernate.refactoring;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;

/**
 * Refactoring actions for Hibernate mapping files
 * 
 * @author Dongmei Cao
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class)
public class HibernateMappingRefactoringActionsProvider extends ActionsImplementationProvider {

    @Override
    public boolean canRename(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        return isHibernateMappingFile(lookup);
    }

    @Override
    public void doRename(final Lookup lookup) {
        if(canRename(lookup)) {
            Runnable task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] fileObjects) {
                    String newName = getNewName(lookup);
                    return new RenameMappingFileRefactoringUI(fileObjects[0], newName);
                }
            };
            task.run();
        }
    }

    @Override
    public boolean canMove(Lookup lookup) {
        return isHibernateMappingFile(lookup);
    }

    @Override
    public void doMove(final Lookup lookup) {
        if(canMove(lookup)) {
            Runnable task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] fileObjects) {
                    // are other parameters specified e.g. due to drag and drop or copy paste
                    PasteType pasteType = getPaste(lookup);
                    FileObject targetFolder = getTarget(lookup);
                    if (fileObjects.length == 1) {
                        return new MoveMappingFilesRefactoringUI(fileObjects, targetFolder, pasteType);
                    } else {
                        return new MoveMappingFilesRefactoringUI(fileObjects, targetFolder, pasteType);
                    }
                }
            };
            task.run();
        }
    }

    @Override
    public boolean canFindUsages(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        return isHibernateMappingFile(lookup);
    }

    @Override
    public void doFindUsages(final Lookup lookup) {
        if(canFindUsages(lookup)){
            if(canRename(lookup)) {
            Runnable task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] fileObjects) {
                    return new HibernateMappingWhereUsedQueryUI(fileObjects[0]);
                }
            };
            task.run();
        }
        }
    }

    private static String getNewName(Lookup look) {
        ExplorerContext ren = look.lookup(ExplorerContext.class);
        if (ren == null) {
            return null;
        }
        return ren.getNewName(); //NOI18N
    }

    private FileObject getTarget(Lookup look) {
        ExplorerContext drop = look.lookup(ExplorerContext.class);
        if (drop == null) {
            return null;
        }
        Node n = (Node) drop.getTargetNode();
        if (n == null) {
            return null;
        }
        DataObject dob = n.getCookie(DataObject.class);
        if (dob != null) {
            return dob.getPrimaryFile();
        }
        return null;
    }

    private PasteType getPaste(Lookup look) {
        ExplorerContext drop = look.lookup(ExplorerContext.class);
        if (drop == null) {
            return null;
        }
        Transferable orig = drop.getTransferable();
        if (orig == null) {
            return null;
        }
        Node n = drop.getTargetNode();
        if (n == null) {
            return null;
        }
        PasteType[] pt = n.getPasteTypes(orig);
        if (pt.length == 1) {
            return null;
        }
        return pt[1];
    }

    private boolean isHibernateMappingFile(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        for (Node node : nodes) {
            if (node != null) {
                // Get the DataObject
                DataObject dataObject = node.getLookup().lookup(DataObject.class);
                if (dataObject != null) {
                    // Get the primary FileObject
                    FileObject fileObject = dataObject.getPrimaryFile();

                    if (fileObject.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
                        // Yes. It can be Renamed 
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static abstract class NodeToFileObjectTask implements Runnable {

        private Collection<? extends Node> nodes;

        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }

        public void run() {
            List<FileObject> fileObjects = new ArrayList<FileObject>(nodes.size());
            for (Node node : nodes) {
                DataObject dataObject = node.getLookup().lookup(DataObject.class);
                if (dataObject != null) {
                    FileObject primaryFileObject = dataObject.getPrimaryFile();
                    if (primaryFileObject != null) {
                        fileObjects.add(primaryFileObject);
                    }
                }
            }
            UI.openRefactoringUI(createRefactoringUI(fileObjects.toArray(new FileObject[fileObjects.size()])));
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement);
    }
    
    
}
