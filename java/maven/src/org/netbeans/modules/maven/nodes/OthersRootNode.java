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

package org.netbeans.modules.maven.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.LogicalViewProviderImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  Milos Kleint
 */
class OthersRootNode extends AnnotatedAbstractNode {
    private FileObject file;
    private static final String SHOW_AS_PACKAGES = "show.as.packages"; //NOI18N
    private static final String PREF_RESOURCES_UI = "org/netbeans/modules/maven/resources/ui"; //NOI18N
    private static final @StaticResource String OTHERS_BADGE = "org/netbeans/modules/maven/others-badge.png";
    
    
    @Messages({"LBL_Other_Test_Sources=Other Test Sources", "LBL_Other_Sources=Other Sources"})
    OthersRootNode(NbMavenProjectImpl mavproject, boolean testResource, FileObject fo) {
        super(new OthersRootChildren(mavproject, testResource), Lookups.fixed(fo, DataFolder.findFolder(fo), new ChildDelegateFind()));
        setName(testResource ? "OtherTestRoots" : "OtherRoots"); //NOI18N
        setDisplayName(testResource ? LBL_Other_Test_Sources() : LBL_Other_Sources());
        file = fo;
    }
    
    @Override
    public Action[] getActions(boolean context) {
            List<Action> supers = Arrays.asList(super.getActions(context));
            List<Action> lst = new ArrayList<Action>(supers.size() + 5);
            lst.addAll(supers);
            lst.add(new ShowAsPackagesAction());

            Action[] retValue = new Action[lst.size()];
            retValue = lst.toArray(retValue);
            return retValue;

    }
    
    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage(OTHERS_BADGE, true); //NOI18N
        return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
    }

    @Override
    protected Image getIconImpl(int param) {
        return getIcon(false);
    }

    @Override
    protected Image getOpenedIconImpl(int param) {
        return getIcon(true);
    }
    
    
    @Override
    public String getDisplayName () {
        String s = super.getDisplayName ();
        try {            
            s = file.getFileSystem ().getDecorator ().annotateName (s, Collections.singleton(file));
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return s;
    }

    @Override
    public String getHtmlDisplayName() {
         try {
            String result = file.getFileSystem().getDecorator().annotateNameHtml (
                super.getDisplayName(), Collections.singleton(file));

            //Make sure the super string was really modified
            if (result != null && !super.getDisplayName().equals(result)) {
                return result;
            }
         } catch (FileStateInvalidException e) {
             ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
         }
         return super.getHtmlDisplayName();
    }

    static boolean showAsPackages() {
        Preferences prefs = NbPreferences.root().node(PREF_RESOURCES_UI); //NOI18N
        boolean b = prefs.getBoolean(SHOW_AS_PACKAGES, true); //NOI18N
        return b;
    }


    @SuppressWarnings("serial")
    private class ShowAsPackagesAction extends AbstractAction implements Presenter.Popup {

        @Messages("LBL_ShowAsPackages=Show Resources as Packages")
        public ShowAsPackagesAction() {
            String s = LBL_ShowAsPackages();
            putValue(Action.NAME, s);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean b = showAsPackages();
            Preferences prefs = NbPreferences.root().node(PREF_RESOURCES_UI); //NOI18N
            prefs.putBoolean(SHOW_AS_PACKAGES, !b); //NOI18N
            try {
                prefs.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
            ((OthersRootChildren)getChildren()).doRefresh();
        }

        @Override
        public JMenuItem getPopupPresenter() {
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(this);
            mi.setSelected(showAsPackages());
            return mi;
        }

    }

    static class ChildDelegateFind implements LogicalViewProviderImpl.FindDelegate {
        @Override
        public Node[] getDelegates(Node current) {
            return current.getChildren().getNodes(true);
        }
    }
}

