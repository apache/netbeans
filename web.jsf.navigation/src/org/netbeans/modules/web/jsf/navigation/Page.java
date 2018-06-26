/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author joelle
 */
public class Page extends PageFlowSceneElement implements SaveCookie {

    public final PageFlowController pc;
    private Node original;
    private PageContentModel pageContentModel = null;

    /**
     * Creates a PageFlowNode
     * @param pc
     * @param original
     */
    public Page(PageFlowController pc, Node original) {
        super();
        this.pc = pc;
        setNode(original);
        /* Update the page only at request
        updateContentModel();
        initListeners();
         */
    }

    public void updateContentModel() {
        if (!isDataNode()) {
            return;
        }

        FileObject fileObject = ((DataNode) original).getDataObject().getPrimaryFile();

        destroyListeners(); /* Otherwise initlisteners will not work */
        
        for (PageContentModelProvider provider : PageFlowController.getPageContentModelProviders()) {
            pageContentModel = provider.getPageContentModel(fileObject);
            //exit when you find one.
            if (pageContentModel != null) {
                initListeners();
                return;
            }
        }
    }
    //    public Node getWrappedNode() {
    //        return original;
    //    }
    private String nodeDisplayName;

    private void setNode(Node newNode) {
        String oldDisplayName = nodeDisplayName;
        original = newNode;
        nodeDisplayName = original.getDisplayName();
        //HACK sometimes the datanode name isn't updated as fast as the filename.
        if (original instanceof DataNode) {
            assert pc != null;

            FileObject fileObj = ((DataNode) original).getDataObject().getPrimaryFile();
            assert fileObj != null;
            nodeDisplayName = getFolderDisplayName(pc.getWebFolder(), fileObj);
        }
        if (!nodeDisplayName.equals(oldDisplayName)) {
            if (oldDisplayName != null) {
                pc.removePageName2Page(oldDisplayName, false);
            }
            pc.putPageName2Page(nodeDisplayName, this);
        }
    }

    public void updateNode_HACK() {
        setNode(original);
    }

    /* We may want this to notify listeners of changes.*/
    public void replaceWrappedNode(Node newNode) {
        //        pc.pageName2Node.remove(getDisplayName());
        //pc.removePageName2Page(getDisplayName(), false);
        setNode(newNode);
        pc.putPageName2Page(getDisplayName(), this);
        //        pc.putPageName2Node(getDisplayName(), this);
    }
    private boolean renaming = false;
    {
    }

    public boolean isRenaming() {
        return renaming;
    }

    @Override
    public void setName(String s) {

        String oldDisplayName = getDisplayName();
        try {
            if (!pc.isPageInAnyFacesConfig(oldDisplayName)) {
                original.setName(s);
            } else {
                renaming = true;
                original.setName(s);
                String newDisplayName = original.getDisplayName();
                if (isDataNode()) {
                    newDisplayName = getFolderDisplayName(pc.getWebFolder(), ((DataNode) original).getDataObject().getPrimaryFile());
                }
                pc.saveLocation(oldDisplayName, newDisplayName);
                renaming = false;
                pc.renamePageInModel(oldDisplayName, newDisplayName);
            }
        } catch (IllegalArgumentException iae) {

            // determine if "printStackTrace"  and  "new annotation" of this exception is needed
            boolean needToAnnotate = Exceptions.findLocalizedMessage(iae) == null;

            // annotate new localized message only if there is no localized message yet
            if (needToAnnotate) {
                Exceptions.attachLocalizedMessage(iae, NbBundle.getMessage(Page.class, "MSG_BadFormat", oldDisplayName, s));
            }

            Exceptions.printStackTrace(iae);
        }
    }

    public String getDisplayName() {
        return nodeDisplayName;
        //        return original.getDisplayName();
    }

    @Override
    public String getName() {
        //        Thread.dumpStack();
        return original.getName();
        //        return nodeDisplayName;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean canRename() {
        return isModifiable();
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    /* Joelle: Temporarily I need not use destroy for the other purpose.  I plan to fix after stabilization */
    public void destroy2() {
        destroyListeners();
        if ( original instanceof NonDataNode) { 
            original = null;
        }
        pccl = null;
        pageContentModel = null;
    }

    public void destroy() throws IOException {

        Object input = DialogDescriptor.NO_OPTION; //This should be the default option especially if not a DataNode.
        boolean removePageName2NodeReference = true; //By default remove it.
        if (isDataNode()) {
            //Don't even ask unless DataNode.
            DialogDescriptor dialog = new DialogDescriptor(NbBundle.getMessage(Page.class, "MSG_DELETE_QUESTION", getDisplayName()), NbBundle.getMessage(Page.class, "MSG_DELETE_TITLE"), true, DialogDescriptor.YES_NO_CANCEL_OPTION, DialogDescriptor.NO_OPTION, null);
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            input = dialog.getValue();
            if (pc != null && pc.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT)) {
                removePageName2NodeReference = false; //if it is a data node and we are in project scope make sure to not remove it.
            }
        }

        String displayName = getDisplayName();
        // Would you like to delete this file too?
        if (input == DialogDescriptor.YES_OPTION) {
            pc.removeSceneNodeEdges(this);
            original.destroy();
            destroyListeners();
        } else if (input == DialogDescriptor.NO_OPTION) {
            // XXX #142726 In case of deleting the node via PageFlowDeleteAction, do not delete the edges.
//            pc.removeSceneNodeEdges(this);
            //            if ( removePageName2NodeReference ) {  //HACK Should I remove the node myself until Petr fixes this bug?
            //                //                pc.removePageName2Node(displayName);
            //                destroy();
            //            }
            //            System.out.println("Only Node Removed");
        } else if (input == DialogDescriptor.CANCEL_OPTION) {
            //            System.out.println("Cancel... Do Nothing.");
        }
        //        destroyListeners();
        //
        //        original.destroy();
        //        pc.pageName2Node.remove(getDisplayName());
    }
    private static final Image ABSTRACTNODE = ImageUtilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/abstract.gif"); // NOI18N

    public Image getIcon(int type) {
        if (!isDataNode()) {
            return ABSTRACTNODE;
        }
        return original.getIcon(type);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return original.getHelpCtx();
    }

    public Node getNode() {
        if (isDataNode()) {
            return original;
        } else {
            return new NonDataNode(original.getName());
        }
    }

    public boolean isDataNode() {
        return original instanceof DataNode;
    }

    public void save() throws IOException {
        //            pc.getConfigDataObject().getEditorSupport().saveDocument();
        getCookie(SaveCookie.class).save();
    }

    //    @Override
    public <T extends Cookie> T getCookie(Class<T> type) {
        if (type.equals(SaveCookie.class)) {
            return pc.getConfigDataObject().getCookie(type);
        }
        return original.getCookie(type);
    }

    /**
     * Solves a fileobjects display name.
     * @param webFolder
     * @param fileObject
     * @return
     */
    public static String getFolderDisplayName(FileObject webFolder, FileObject fileObject) {
        String folderpath = webFolder.getPath();
        String filepath = fileObject.getPath();
        return filepath.replaceFirst(folderpath + "/", "");
    }

    public static String getFolderDisplayName(FileObject webFolder, String path, String fileNameExt) {
        String folderpath = webFolder.getPath();
        return path.replaceFirst(folderpath + "/", "") + fileNameExt;
    }

    public Collection<PageContentItem> getPageContentItems() {
        if (pageContentModel == null) {
            return new ArrayList<PageContentItem>();
        }
        return pageContentModel.getPageContentItems();
    }
    private boolean hasPageContentModelBeenChecked = false;

    public Collection<Pin> getPinNodes() {
        if (!hasPageContentModelBeenChecked) {
            updateContentModel();
            hasPageContentModelBeenChecked = true;
        }
        if (pageContentModel == null) {
            return Collections.emptyList();
        }
        Collection<PageContentItem> pageContentItems = pageContentModel.getPageContentItems();
        Collection<Pin> pinNodes = new ArrayList<Pin>(pageContentItems.size());
        for (PageContentItem pageContentItem : pageContentItems) {
            pinNodes.add(new Pin(this, pageContentItem));
        }
        return pinNodes;
    }
    private PageContentChangeListener pccl;
    /**
     * Before using this method, it is good to make sure all listeners
     * are destroyed.  Use destroyListeners().
     **/
    private void initListeners() {
        if (pageContentModel != null && pccl == null) {
            pccl = new PageContentChangeListener();
            pageContentModel.addChangeListener(pccl);
        }
    }

    /**
     * Removes any content model listeners
     **/
    private void destroyListeners() {
        if (pccl != null && pageContentModel != null) {
            try {
                pageContentModel.removeChangeListener(pccl);
                pageContentModel.destroy();
                pageContentModel = null;
                pccl = null;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private final Page getInstance() {
        return this;
    }
    private class PageContentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent arg0) {
            if( !pc.getView().isShowing()){  //Don't do anything except let PFC know that the graph is dirty if the graph is not showing.
                pc.setGraphDirty();
            } else {
                pc.updatePageItems(getInstance());
            }
        }
    }

    public Action[] getActions(boolean context) {
        if (pageContentModel != null) {
            return pageContentModel.getActions();
        }
        return new SystemAction[]{};
        //        if( pageContentModel != null ){
        //            SystemAction[] pageModelActions = pageContentModel.getActions();
        //            SystemAction[] nodeActions = super.getActions();
        //
        //            if( pageModelActions == null || pageModelActions.length == 0 ){
        //                return nodeActions;
        //            } else if ( nodeActions == null || nodeActions.length == 0 ){
        //                return pageModelActions;
        //            } else {
        //                int size = pageModelActions.length + nodeActions.length;
        //                SystemAction[] sysActions = new SystemAction[size];
        //                System.arraycopy(nodeActions, 0, sysActions, 0, nodeActions.length);
        //                System.arraycopy(pageModelActions, 0, sysActions, nodeActions.length, pageModelActions.length);
        //                return sysActions;
        //            }
        //        } else {
        //            return super.getActions();
        //        }
    }

    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    //
    //    public final NonDataNode createNonDataNode() {
    //        return new NonDataNode(this);
    //    }

    
    
    public final class NonDataNode extends AbstractNode {


        public NonDataNode( String pageName) {
            super(Children.LEAF);
            super.setName(pageName);
        }

        @Override
        public boolean canRename() {
            return true;
        }

        @Override
        public String getName() {
            return getInstance().getName();
        }

        @Override
        public void setName(String s) {
            super.setName(s);
            getInstance().setName(s);
        }
    }
}
