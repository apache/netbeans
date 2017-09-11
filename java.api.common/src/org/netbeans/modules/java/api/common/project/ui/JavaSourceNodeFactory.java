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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.java.api.common.project.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * Java sources node factory.
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class JavaSourceNodeFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(JavaSourceNodeFactory.class.getName());

    public JavaSourceNodeFactory() {
    }
    
    @Override
    public NodeList createNodes(Project p) {
        Project project = p.getLookup().lookup(Project.class);
        assert project != null;
        return new SourcesNodeList(project);
    }
    
    private static class SourcesNodeList implements NodeList<SourceGroupKey>, ChangeListener {
        
        private final Project project;
        private final File genSrcDir;
        private final FileChangeListener genSrcDirListener;
        private final FileChangeListener genContentListener;
        private final List<File> listensOn = Collections.synchronizedList(new LinkedList<File>());
        private final Runnable changeTask;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public SourcesNodeList(Project proj) {
            project = proj;
            changeTask = new Runnable() {
                @Override
                public void run() {
                    stateChanged(null);
                }
            };
            genSrcDirListener = new FileChangeAdapter() {
                public @Override void fileFolderCreated(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                public @Override void fileDeleted(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                public @Override void fileRenamed(FileRenameEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
            };
            genContentListener = new FileChangeAdapter() {
                @Override
                public void fileFolderCreated(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                @Override
                public void fileDataCreated(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
                @Override
                public void fileDeleted(FileEvent fe) {
                    fe.runWhenDeliveryOver(changeTask);
                }
            };
            File d = FileUtil.toFile(proj.getProjectDirectory());
            // XXX hardcodes the value of ${build.generated.sources.dir}, since we have no access to evaluator
            genSrcDir = d != null ? new File(d, "build/generated-sources") : null;
        }

        @Override
        public List<SourceGroupKey> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.<SourceGroupKey>emptyList();
            }
            List<SourceGroupKey> result =  new ArrayList<SourceGroupKey>();
            final SourceGroup[] groups = getSources().getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "Java source groups: {0}",  //NOI18N
                        Arrays.toString(groups));
            }
            for (SourceGroup group : groups) {
                result.add(new SourceGroupKey(group, true));
            }
            File[] removeFrom;
            synchronized (listensOn) {
                removeFrom = listensOn.toArray(new File[listensOn.size()]);
                listensOn.clear();
            }
            for (File file : removeFrom) {
                FileUtil.removeFileChangeListener(genContentListener, file);
            }
            FileObject genSrc = FileUtil.toFileObject(genSrcDir);
            if (genSrc != null) {
                final VisibilityQuery vq = VisibilityQuery.getDefault();
                for (final FileObject child : genSrc.getChildren()) {
                    if (!child.isFolder()) {
                        continue;
                    }                    
                    if (!vq.isVisible(child)) {
                        continue;
                    }                    
                    final File childFile = FileUtil.toFile(child);
                    if (childFile == null) {
                        continue;
                    }
                    FileUtil.addFileChangeListener(genContentListener,childFile);
                    listensOn.add(childFile);
                    if (child.getChildren().length > 0) {
                        result.add(new SourceGroupKey(new GeneratedSourceGroup(child), false));
                    }
                }
            }
            return result;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            synchronized (changeSupport) {
                final boolean shouldAdd = !changeSupport.hasListeners();
                changeSupport.addChangeListener(l);
                if (shouldAdd) {
                    FileUtil.addFileChangeListener(genSrcDirListener, genSrcDir);
                }
            }
        }
        
        @Override
        public void removeChangeListener(ChangeListener l) {            
            synchronized (changeSupport) {
                final boolean hadListeners = changeSupport.hasListeners();
                changeSupport.removeChangeListener(l);            
                if (hadListeners ^ changeSupport.hasListeners())  {
                    FileUtil.removeFileChangeListener(genSrcDirListener, genSrcDir);
                }
            }
        }
        
        @Override
        public Node node(SourceGroupKey key) {
            return new PackageViewFilterNode(key, project);
        }
        
        @Override
        public void addNotify() {
            getSources().addChangeListener(this);
        }
        
        @Override
        public void removeNotify() {
            getSources().removeChangeListener(this);
        }
        
        @Override
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }

    }
    
    private static class SourceGroupKey {
        
        public final SourceGroup group;
        public final FileObject fileObject;
        public final boolean trueSource;
        
        SourceGroupKey(SourceGroup group, boolean trueSource) {
            this.group = group;
            this.fileObject = group.getRootFolder();
            this.trueSource = trueSource;
        }
        
        @Override
        public int hashCode() {
            int hash = 5;
            String disp = this.group.getDisplayName();
            hash = 79 * hash + (fileObject != null ? fileObject.hashCode() : 0);
            hash = 79 * hash + (disp != null ? disp.hashCode() : 0);
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceGroupKey)) {
                return false;
            } else {
                SourceGroupKey otherKey = (SourceGroupKey) obj;
                
                if (fileObject != otherKey.fileObject && (fileObject == null || !fileObject.equals(otherKey.fileObject))) {
                    return false;
                }
                String thisDisplayName = this.group.getDisplayName();
                String otherDisplayName = otherKey.group.getDisplayName();
                boolean oneNull = thisDisplayName == null;
                boolean twoNull = otherDisplayName == null;
                if (oneNull != twoNull || !thisDisplayName.equals(otherDisplayName)) {
                    return false;
                }
                return true;
            }
        }
        
    }
    
    private static class GeneratedSourceGroup implements SourceGroup {

        private final FileObject child;

        GeneratedSourceGroup(FileObject child) {
            this.child = child;
        }

        @Override
        public FileObject getRootFolder() {
            return child;
        }

        @Override
        public String getName() {
            return child.getNameExt();
        }

        @Override
        public String getDisplayName() {
            try {
                // Modules can provide dedicated localizable labels for well-known root names.
                return NbBundle.getBundle("org.netbeans.modules.java.api.common.project.ui.gensrc-" + getName()).getString("label");
            } catch (MissingResourceException x) {
                // Fallback, including for user-defined root names.
                return NbBundle.getMessage(JavaSourceNodeFactory.class, "JavaSourceNodeFactory.gensrc", getName());
            }
        }

        @Override
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override
        public boolean contains(FileObject file) {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

    }

    /**
     * Adjusts some display characteristics of source group root node.
     */
    private static class PackageViewFilterNode extends FilterNode {
        
        private final String nodeName;
        private final Project project;
        private final boolean trueSource;
        
        public PackageViewFilterNode(SourceGroupKey sourceGroupKey, Project project) {
            super(PackageView.createPackageView(sourceGroupKey.group));
            this.project = project;
            this.nodeName = "Sources";
            trueSource = sourceGroupKey.trueSource;
        }
        
        public @Override Action[] getActions(boolean context) {
            List<Action> actions = new ArrayList<Action>(Arrays.asList(super.getActions(context)));
            if (trueSource) {
                actions.add(null);
                actions.add(new PreselectPropertiesAction(project, nodeName));
            } else {
                // Just take out "New File..." as this would be misleading.
                Iterator<Action> scan = actions.iterator();
                while (scan.hasNext()) {
                    Action a = scan.next();
                    if (a != null && a.getClass().getName().equals("org.netbeans.modules.project.ui.actions.NewFile$WithSubMenu")) { // NOI18N
                        scan.remove();
                    }
                }
            }
            return actions.toArray(new Action[actions.size()]);
        }

        public @Override String getHtmlDisplayName() {
            if (trueSource) {
                return super.getHtmlDisplayName();
            }
            String htmlName = getOriginal().getHtmlDisplayName();
            if (htmlName == null) {
                try {
                    htmlName = XMLUtil.toElementContent(super.getDisplayName());
                } catch (CharConversionException x) {
                    return null; // never mind
                }
            }
            return "<font color='!controlShadow'>" + htmlName + "</font>"; // NOI18N
        }
        
    }
    
    
    /** The special properties action
     */
    static class PreselectPropertiesAction extends AbstractAction {
        
        private final Project project;
        private final String nodeName;
        private final String panelName;
        
        public PreselectPropertiesAction(Project project, String nodeName) {
            this(project, nodeName, null);
        }
        
        public PreselectPropertiesAction(Project project, String nodeName, String panelName) {
            super(NbBundle.getMessage(JavaSourceNodeFactory.class, "LBL_Properties_Action")); //NOI18N
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            CustomizerProvider2 cp2 = project.getLookup().lookup(CustomizerProvider2.class);
            if (cp2 != null) {
                cp2.showCustomizer(nodeName, panelName);
            } else {
                CustomizerProvider cp = project.getLookup().lookup(CustomizerProvider.class);
                if (cp != null) {
                    cp.showCustomizer();
                }
            }            
        }
    }
    
}
