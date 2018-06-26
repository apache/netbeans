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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

class ArchiveViews {
        
    private ArchiveViews() {
    }
    
    static final class LogicalViewChildren extends Children.Keys<String>  implements FileChangeListener {
        
        // XXX does not react correctly to addition or removal of src/ subdir

        private static final String KEY_DOC_BASE = "docBase"; //NOI18N
        private static final String KEY_SETUP_DIR = "setupDir"; //NOI18N
        
        private final Project project;
        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final FileObject projectDir;

        public LogicalViewChildren (Project project, AntProjectHelper helper, PropertyEvaluator evaluator) {
            assert project != null;
            this.project = project;
            assert helper != null;
            this.helper = helper;
            projectDir = helper.getProjectDirectory();
            this.evaluator = evaluator;
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            projectDir.addFileChangeListener(FileUtil.weakFileChangeListener(this, projectDir));
            createNodes();
        }
        
        private void createNodes() {
            List<String> keys = new ArrayList<String>();
           
            DataFolder docBaseDir = getFolder(EarProjectProperties.META_INF);
            if (docBaseDir != null) {
                keys.add(KEY_DOC_BASE);
            }
            keys.add(KEY_SETUP_DIR);
            
            setKeys(keys);
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<String>emptySet());
            projectDir.removeFileChangeListener(this);
            super.removeNotify();
        }
        
        protected Node[] createNodes(String key) {
            Node n = null;
            if (KEY_DOC_BASE.equals(key)) {
                n = new DocBaseNode (getFolder(EarProjectProperties.META_INF));
            } else if (KEY_SETUP_DIR.equals(key)) {
                n = J2eeProjectView.createServerResourcesNode(project);
            }
            return n == null ? new Node[0] : new Node[] {n};
        }
            
        private DataFolder getFolder(String propName) {
            String prop = evaluator.getProperty (propName);
            if (prop != null) {
                FileObject fo = helper.resolveFileObject(prop);
                if (fo != null && fo.isValid() && fo.isFolder()) {
                    DataFolder df = DataFolder.findFolder(fo);
                    return df;
                }
            }
            return null;
        }
        
        // file change events in the project directory
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
        
        public void fileChanged(FileEvent fe) {
        }
        
        public void fileDataCreated(FileEvent fe) {
        }
        
        public void fileDeleted(FileEvent fe) {
            // setup folder deleted
           createNodes();
        }
        
        public void fileFolderCreated(FileEvent fe) {
            // setup folder could be created
            createNodes();
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            // setup folder could be renamed
            createNodes();
        }
    }
    
    private static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        private static final long serialVersionUID = 1L;
        
        final ChangeSupport changeSupport = new ChangeSupport (this );
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return VisibilityQuery.getDefault().isVisible( fo );
        }
        
        public void stateChanged( ChangeEvent e) {            
            changeSupport.fireChange();
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            changeSupport.addChangeListener( listener );
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            changeSupport.removeChangeListener( listener );
        }
        
    }

    private static final class DocBaseNode extends FilterNode {

        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
        private final Image CONFIGURATION_FILES_BADGE = ImageUtilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/archive.gif", true ); // NOI18N
        
        public DocBaseNode(DataFolder folder) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
        }
        
        @Override
        public Image getIcon( int type ) {
            return computeIcon( false, type );
        }
        
        @Override
        public Image getOpenedIcon( int type ) {
            return computeIcon( true, type );
        }
        
        private Image computeIcon( boolean opened, int type ) {
            Node folderNode = getOriginal();
            Image image = opened ? folderNode.getOpenedIcon( type ) : folderNode.getIcon( type );
            return ImageUtilities.mergeImages( image, CONFIGURATION_FILES_BADGE, 7, 7 );
        }
        
        @Override
        public boolean canCopy() {
            return false;
        }
        
        @Override
        public boolean canCut() {
            return false;
        }
        
        @Override
        public boolean canRename() {
            return false;
        }
        
        @Override
        public boolean canDestroy() {
            return false;
        }
        
        @Override
        public Action[] getActions( boolean context ) {
            return new Action[] {
//                CommonProjectActions.newFileAction(),
//                null,
//                SystemAction.get(FileSystemRefreshAction.class),
//                null,
                SystemAction.get(FindAction.class),
//                null,
//                SystemAction.get(PasteAction.class),
//                null,
//                SystemAction.get(ToolsAction.class),
            };
        }
        
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ArchiveViews.class, "LBL_Node_DocBase"); //NOI18N
        }
    }
}
