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

package org.netbeans.modules.web.jsf.navigation;

import java.awt.EventQueue;
import java.util.Collection;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class WebFolderListener extends FileChangeAdapter {

    private final PageFlowController pfc;
    private final FileObject webFolder;

    /**
     * This web folder listener listens to any modifications related to WebFolder and updates the faces config accordingly.
     * @param pfc
     */
    public WebFolderListener(PageFlowController pfc) {
        super();
        this.pfc = pfc;
        webFolder = pfc.getWebFolder();
    }
    private final Collection<? extends PageContentModelProvider> impls = PageFlowController.getPageContentModelProviders();

    private boolean isKnownFileEvent(FileObject potentialChild) {
        if (FileUtil.isParentOf(webFolder, potentialChild)) {
            if (potentialChild.isFolder()) {
                return pfc.isKnownFolder(potentialChild);
            } else {
                return pfc.isKnownFile(potentialChild);
            }
        }
        return false;
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        // XXX #131525 PageFlowController could be destroyed already. Revise.
        if (pfc.getView() == null) {
            return;
        }

        if( !pfc.getView().isShowing() ) {
            pfc.setFilesDirty();
            return;
        }
        
        final FileObject fileObj = fe.getFile();
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                fileCreatedEventHandler(fileObj);
            }
        });
    }


    @Override
    public void fileDeleted(FileEvent fe) {
        // XXX #131525 PageFlowController could be destroyed already. Revise.
        if (pfc.getView() == null) {
            return;
        }

        if( !pfc.getView().isShowing() ) {
            pfc.setFilesDirty();
            return;
        }
        final FileObject fileObj = fe.getFile();
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                fileDeletedEventHandler(fileObj);
            }
        });
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        // XXX #131525 PageFlowController could be destroyed already. Revise.
        if (pfc.getView() == null) {
            return;
        }

        if( !pfc.getView().isShowing() ) {
            pfc.setFilesDirty();
            return;
        }
        /* fileRenamed should not modify the faces-config because it should
         * be up to refactoring to do this. If that is the case, FacesModelPropertyChangeListener
         * should reload it.
         */
        final FileObject fileObj = fe.getFile();
        final FileRenameEvent event = fe;
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                fileRenamedEventHandler(fileObj, event.getName(), event.getExt());
            }
        });
    }

    private void fileDeletedEventHandler(FileObject fileObj) {
        PageFlowView view = pfc.getView();
        if (!pfc.removeWebFile(fileObj)) {
            return;
        }

        //DISPLAYNAME:
        String pageDisplayName = Page.getFolderDisplayName(webFolder, fileObj);

        Page oldNode = pfc.getPageName2Page(pageDisplayName);
        if (oldNode != null) {
            if (pfc.isPageInAnyFacesConfig(oldNode.getDisplayName())) {
                //                Node tmpNode = new AbstractNode(Children.LEAF);
                //                tmpNode.setName(pageDisplayName);
                //                oldNode.replaceWrappedNode(tmpNode);
                //                view.resetNodeWidget(oldNode, false);  /* If I add a listener to PageFlowNode, then I won't have to do this*/
                pfc.changeToAbstractNode(oldNode, pageDisplayName);
            } else {
                view.removeNodeWithEdges(oldNode);
                pfc.removePageName2Page(oldNode, true);
            }
            view.validateGraph(); //Either action validate graph
        }
    }

    private void fileCreatedEventHandler(FileObject fileObj) {
        PageFlowView view = pfc.getView();
        if (!isKnownFileEvent(fileObj)) {

            /* Semi Hack: The point is to update a model if a new model is created
            for a given page as a sideeffect of an "UNKNOWN" page being created */
            for (PageContentModelProvider provider : impls) {
                FileObject jspFileObject = provider.isNewPageContentModel(fileObj);
                if (jspFileObject != null && isKnownFileEvent(jspFileObject)) {

                    //DataObject dobj = DataObject.find(jspFileObject);
                    //DISPLAYNAME:
                    //System.out.println("webFolder: " + webFolder);
                    String pageDisplayName = Page.getFolderDisplayName(webFolder, jspFileObject);

                    Page relevantPage = pfc.getPageName2Page(pageDisplayName); /* I can't remember if this*/
                    if (relevantPage != null) {
                        relevantPage.updateContentModel();
                        view.resetNodeWidget(relevantPage, true);
                        view.validateGraph();
                    }
                }
            }
            return;
        }

        try {
            if (pfc.isKnownFile(fileObj)) {
                pfc.addWebFile(fileObj);
                DataObject dataObj = DataObject.find(fileObj);
                Node dataNode = dataObj.getNodeDelegate();
                //                    PageFlowNode pageNode = pageName2Node.get(dataNode.getDisplayName());
                //DISPLAYNAME:
                Page pageNode = pfc.getPageName2Page(Page.getFolderDisplayName(webFolder, fileObj));
                if (pageNode != null) {
                    pageNode.replaceWrappedNode(dataNode);
                    view.resetNodeWidget(pageNode, false);
                    view.validateGraph();
                } else if (pfc.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT)) {
                    Page node = pfc.createPage(dataNode);
                    view.createNode(node, null, null);
                    view.validateGraph();
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    private String oldFolderName;
    private String newFolderName;

    private void fileRenamedEventHandler(FileObject fileObj, String oldName, String extName) {
        if (!pfc.containsWebFile(fileObj) && !pfc.isKnownFolder(fileObj)) {
            return;
        }

        if (fileObj.isFolder()) {
            //I may still need to modify display names.
            if (oldName.equals(oldFolderName) && fileObj.getName().equals(newFolderName)) {
                //Folder rename triggers two listeners.  Only pay attention to the first one.
                return;
            }
            oldFolderName = oldName;
            newFolderName = fileObj.getName();
            renameFolder(fileObj, oldFolderName, newFolderName);
        } else {
            //DISPLAYNAME:
            String newDisplayName = Page.getFolderDisplayName(webFolder, fileObj);
            String path = fileObj.getPath().replace(fileObj.getNameExt(), "");
            String oldDisplayName = Page.getFolderDisplayName(webFolder, path, oldName + "." + extName);

            renameFile(fileObj, oldDisplayName, newDisplayName);
        }
        pfc.getView().validateGraph();
    }

    private void renameFolder(FileObject folderObject, String oldFolderName, String newFolderName) {
        FileObject[] fileObjs = folderObject.getChildren();
        for (FileObject file : fileObjs) {

            if (file.isFolder()) {
                renameFolder(file, oldFolderName, newFolderName);
            } else {
                String newDisplayName = Page.getFolderDisplayName(webFolder, file);
                String oldDisplayName = newDisplayName.replaceFirst(newFolderName, oldFolderName);
                renameFile(file, oldDisplayName, newDisplayName);
            }
        }
    }

    private void renameFile(FileObject fileObj, String oldDisplayName, String newDisplayName) {
        PageFlowView view = pfc.getView();
        Page oldNode = pfc.getPageName2Page(oldDisplayName);
        Page abstractNode = pfc.getPageName2Page(newDisplayName); // I know I do this twice, but I am trying to keep it less confusing.
        if (oldNode == null && abstractNode != null) {
            /* Probably a refactoring scenario */
            Node dataNode = getNodeDelegate(fileObj);
            abstractNode.replaceWrappedNode(dataNode);
            view.resetNodeWidget(abstractNode, true);
            view.validateGraph();
            return;
        }

        if (oldNode != null && oldNode.isRenaming()) {
            return;
        }

        Node newNodeDelegate = getNodeDelegate(fileObj);

        //If we are in project view scope
        if (pfc.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT)) {
//            assert oldNode != null;
            if (oldNode == null) {
                // XXX #152498 Avoiding the assertion error.
                return;
            }
        }

        if (abstractNode != null) {
            //            assert !abstractNode.isDataNode();  //Never should this have already been a file node.
            if (abstractNode.isDataNode()) {
                System.err.println("So Called Abstract Node: " + abstractNode);
                Thread.dumpStack();
            }


            //Figure out what to do with old node.
            if (pfc.isPageInAnyFacesConfig(oldDisplayName)) {
                pfc.changeToAbstractNode(oldNode, oldDisplayName);
            } else if (oldNode != null) {
                view.removeNodeWithEdges(oldNode);
                pfc.removePageName2Page(oldNode, true);
            }
            abstractNode.replaceWrappedNode(newNodeDelegate);
            view.resetNodeWidget(abstractNode, true);
        } else if (oldNode != null) {
            if (pfc.isPageInAnyFacesConfig(oldDisplayName)) {
                pfc.changeToAbstractNode(oldNode, oldDisplayName);
                if (pfc.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT)) {
                    Page newNode = pfc.createPage(newNodeDelegate);
                    view.createNode(newNode, null, null);
                }
            } else {
                view.resetNodeWidget(oldNode, false);
//                pfc.removePageName2Page(oldDisplayName, false);
            }
        }
//        view.validateGraph();
    }

    private Node getNodeDelegate(FileObject fileObj) {
        Node newNodeDelegate = null;
        try {
            newNodeDelegate = (DataObject.find(fileObj)).getNodeDelegate();
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        }
        return newNodeDelegate;
    }
}
