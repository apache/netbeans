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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.maven.cli.MavenCli;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.M2AuxilaryConfigImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.configurations.M2Configuration;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 * maven project related aggregator node..
 * @author Milos Kleint
 */
public class ProjectFilesNode extends AnnotatedAbstractNode {
    
    private static final @StaticResource String PF_BADGE = "org/netbeans/modules/maven/projectfiles-badge.png";
    private final NbMavenProjectImpl project;

    @Messages("LBL_Project_Files=Project Files")
    public ProjectFilesNode(NbMavenProjectImpl project) {
        super(Children.create(new ProjectFilesChildren(project), true), Lookups.fixed(project.getProjectDirectory(), new OthersRootNode.ChildDelegateFind()));
        setName("projectfiles"); //NOI18N
        setDisplayName(LBL_Project_Files());
        this.project = project;
        setMyFiles();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Collection<Action> col = new ArrayList<Action>();
        if (!MavenCli.DEFAULT_USER_SETTINGS_FILE.exists()) {
            col.add(new AddSettingsXmlAction());
        }
        return col.toArray(new Action[col.size()]);
    }
    
    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage(PF_BADGE, true); //NOI18N
        Image img = ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
        return img;
    }
    
    @Override
    protected Image getIconImpl(int param) {
        return getIcon(false);
    }

    @Override
    protected Image getOpenedIconImpl(int param) {
        return getIcon(true);
    }
    
    private void setMyFiles() {
        Set<FileObject> fobs = new HashSet<FileObject>();
        FileObject fo = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
        if (fo != null) {
            //#119134 for some unknown reason, the pom.xml might be missing from the project directory in some cases.
            // prevent passing null to the list that causes problems down the stream.
            fobs.add(fo);
        }
        setFiles(fobs);
    }
    
    private static class ProjectFilesChildren extends ChildFactory.Detachable<FileObject> implements PropertyChangeListener {

        private final NbMavenProjectImpl project;
        private final FileChangeAdapter fileChangeListener;
        
        ProjectFilesChildren(NbMavenProjectImpl proj) {
            project = proj;
            fileChangeListener = new FileChangeAdapter() {
                @Override public void fileDataCreated(FileEvent fe) {
                    refresh(false);
                }
                @Override public void fileDeleted(FileEvent fe) {
                    refresh(false);
                }
            };
        }

        @Override protected Node createNodeForKey(FileObject key) {
            try {
                return DataObject.find(key).getNodeDelegate().cloneNode();
            } catch (DataObjectNotFoundException e) {
                return null;
            }
        }
        
        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                refresh(false);
            }
        }
        
        @Override protected void addNotify() {
            NbMavenProject.addPropertyChangeListener(project, this);
            project.getProjectDirectory().addFileChangeListener(fileChangeListener);
        }
        
        @Override protected void removeNotify() {
            NbMavenProject.removePropertyChangeListener(project, this);
            project.getProjectDirectory().removeFileChangeListener(fileChangeListener);
        }

        @Override protected boolean createKeys(List<FileObject> keys) {
            FileObject d = project.getProjectDirectory();
            keys.add(d.getFileObject("pom.xml")); // NOI18N
            keys.add(d.getFileObject(M2Configuration.FILENAME));
            for (FileObject kid : d.getChildren()) {
                String n = kid.getNameExt();
                if (n.startsWith(M2Configuration.FILENAME_PREFIX) && n.endsWith(M2Configuration.FILENAME_SUFFIX)) {
                    keys.add(kid);
                }
            }
            keys.add(d.getFileObject(M2AuxilaryConfigImpl.CONFIG_FILE_NAME));
            keys.add(FileUtil.toFileObject(MavenCli.DEFAULT_USER_SETTINGS_FILE));
            keys.removeAll(Collections.singleton(null));
            return true;
        }
    }

    private static class AddSettingsXmlAction extends AbstractAction {
        @Messages("BTN_Create_settings_xml=Create settings.xml")
        AddSettingsXmlAction() {
            super(BTN_Create_settings_xml());
        }
        
        public @Override void actionPerformed(ActionEvent e) {
            try {
                File sf = FileUtilities.getUserSettingsFile(true);
                EditCookie cook = DataObject.find(FileUtil.toFileObject(sf)).getLookup().lookup(EditCookie.class);
                if (cook != null) {
                    cook.edit();
                }
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(ProjectFilesNode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

}
