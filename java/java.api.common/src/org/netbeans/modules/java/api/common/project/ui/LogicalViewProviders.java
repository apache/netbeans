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

package org.netbeans.modules.java.api.common.project.ui;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.net.URL;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.XMLUtil;

/**
 * Support for creating default {@link LogicalViewProvider2} for Ant Based Project.
 * @author Tomas Zezula
 * @since 1.62
 */
public final class LogicalViewProviders {

    private LogicalViewProviders() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }


    /**
     * Creates a new {@link LogicalViewProviderBuilder}.
     * @param project the project for which the builder should be created
     * @param eval the project's {@link PropertyEvaluator}
     * @param extensionFolder the extension point name
     * @return the new {@link LogicalViewProviderBuilder}
     */
    @NonNull
    public static LogicalViewProviderBuilder createBuilder(
        @NonNull final Project project,
        @NonNull final PropertyEvaluator eval,
        @NonNull final String extensionFolder) {
        return new LogicalViewProviderBuilder(
            project,
            eval,
            extensionFolder);
    }


    /**
     * Builder for creating a new configured {@link LogicalViewProvider2} instance.
     */
    public static class LogicalViewProviderBuilder {

        private final Project project;
        private final PropertyEvaluator eval;
        private final String projectType;
        private HelpCtx helpContext;
        private CompileOnSaveBadge badgeStatus;
        

        private LogicalViewProviderBuilder(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String projectType) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("projectType", projectType); //NOI18N
            this.project = project;
            this.eval = eval;
            this.projectType = projectType;
        }

        /**
         * Sets a {@link HelpCtx}.
         * @param helpContext the root node {@link HelpCtx}
         * @return the {@link LogicalViewProviderBuilder}
         */
        @NonNull
        public LogicalViewProviderBuilder setHelpCtx(@NonNull final HelpCtx helpContext) {
            Parameters.notNull("helpContext", helpContext); //NOI18N
            this.helpContext = helpContext;
            return this;
        }

        /**
         * Sets a compile on save badge status.
         * @param badgeStatus the compile on save badge status
         * @return the {@link LogicalViewProviderBuilder}
         */
        @NonNull
        public LogicalViewProviderBuilder setCompileOnSaveBadge(@NonNull final CompileOnSaveBadge badgeStatus) {
            Parameters.notNull("badgeStatus", badgeStatus); //NOI18N
            this.badgeStatus = badgeStatus;
            return this;
        }

        /**
         * Creates a new configured {@link LogicalViewProvider2} instance.
         * @return the {@link LogicalViewProvider2} instance
         */
        @NonNull
        public LogicalViewProvider2 build() {
            return new LogicalViewProviderImpl(
                project,
                eval,
                projectType,
                helpContext,
                badgeStatus);
        }
        
    }


    /**
     * Compile on Save badge status.
     * The {@link CompileOnSaveBadge} is used by {@link LogicalViewProvider2}
     * to enable or disable the project badge notifying an user about disabled
     * compile on save.
     */
    public static interface CompileOnSaveBadge {
        /**
         * Badge visibility check.
         * @return true if the badge should be visible
         */
        boolean isBadgeVisible();
        /**
         * Checks if the changed property affects the compile on save visibility.
         * @param propertyName the name of changed property
         * @return true if compile on save badge should be recalculated
         */
        boolean isImportant(@NonNull String propertyName);
    }


    private static class LogicalViewProviderImpl implements LogicalViewProvider2 {

        private static final RequestProcessor RP = new RequestProcessor(LogicalViewProviders.class);
        private static final String COMPILE_ON_SAVE_DISABLED_BADGE_PATH = "org/netbeans/modules/java/api/common/project/ui/resources/compileOnSaveDisabledBadge.gif";   //NOI18N
        private static final Image compileOnSaveDisabledBadge;
        static {
            URL errorBadgeIconURL = LogicalViewProviders.class.getClassLoader().getResource(COMPILE_ON_SAVE_DISABLED_BADGE_PATH);
            String compileOnSaveDisabledTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + NbBundle.getMessage(LogicalViewProviders.class, "TP_CompileOnSaveDisabled");
            compileOnSaveDisabledBadge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(COMPILE_ON_SAVE_DISABLED_BADGE_PATH), compileOnSaveDisabledTP);
        }

        private final Project project;
        private final PropertyEvaluator evaluator;
        private final String projectType;
        private final HelpCtx helpContext;
        private final CompileOnSaveBadge badgeStatus;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final PropertyChangeListener pcl;
        private final RequestProcessor.Task task = RP.create(new Runnable() {
            public @Override void run() {
                setBroken(ProjectProblems.isBroken(project));
                setCompileOnSaveDisabled(isCompileOnSaveDisabled());
            }
        });

        private volatile boolean listenersInited;
        private volatile boolean broken;         //Represents a state where project has a broken reference repairable by broken reference support
        private volatile boolean compileOnSaveDisabled;  //true iff Compile-on-Save is disabled


        public LogicalViewProviderImpl(
                @NonNull final Project project,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final String projectType,
                @NullAllowed final HelpCtx helpContext,
                @NullAllowed final CompileOnSaveBadge badgeStatus) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("evaluator", evaluator); //NOI18N
            Parameters.notNull("projectType", projectType); //NOI18N
            this.project = project;
            this.evaluator = evaluator;
            this.projectType = projectType;
            this.helpContext = helpContext;
            this.badgeStatus = badgeStatus;
            pcl = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    final String propName = evt.getPropertyName();
                    if (ProjectProblemsProvider.PROP_PROBLEMS.equals(evt.getPropertyName()) ||
                        (badgeStatus != null && (propName == null || badgeStatus.isImportant(propName)))) {
                        testBroken();
                    }
                }
            };
        }

        private void initListeners() {
            if (listenersInited) {
                return;
            }
            ProjectManager.mutex().readAccess(new Runnable() {
                @Override
                public void run() {
                    synchronized (LogicalViewProviderImpl.class) {
                        if (!listenersInited) {
                            evaluator.addPropertyChangeListener(pcl);
                            final ProjectProblemsProvider ppp = project.getLookup().lookup(ProjectProblemsProvider.class);
                            if (ppp != null) {
                                ppp.addPropertyChangeListener(pcl);
                            }
                            listenersInited = true;
                        }
                    }
                }
            });
        }

        @Override
        public Node createLogicalView() {
            initListeners();
            final InstanceContent ic = new InstanceContent();
            ic.add(project);
            ic.add(project, new InstanceContent.Convertor<Project, FileObject>() {
                @Override
                public FileObject convert(Project obj) {
                    return obj.getProjectDirectory();
                }
                @Override
                public Class<? extends FileObject> type(Project obj) {
                    return FileObject.class;
                }
                @Override
                public String id(Project obj) {
                    final FileObject fo = obj.getProjectDirectory();
                    return fo == null ? "" : fo.getPath();  //NOI18N
                }
                @Override
                public String displayName(Project obj) {
                    return obj.toString();
                }
            });
            ic.add(project, new InstanceContent.Convertor<Project, DataObject>() {
                @Override
                public DataObject convert(Project obj) {
                    try {
                        final FileObject fo = obj.getProjectDirectory();
                        return fo == null ? null : DataObject.find(fo);
                    } catch (DataObjectNotFoundException ex) {
                        return null;
                    }
                }
                @Override
                public Class<? extends DataObject> type(Project obj) {
                    return DataObject.class;
                }
                @Override
                public String id(Project obj) {
                    final FileObject fo = obj.getProjectDirectory();
                    return fo == null ? "" : fo.getPath();  //NOI18N
                }
                @Override
                public String displayName(Project obj) {
                    return obj.toString();
                }
            });
            return new LogicalViewRootNode(new AbstractLookup(ic));
        }    

        @Override
        public Node findPath(Node root, Object target) {
            Project prj = root.getLookup().lookup(Project.class);
            if (prj == null) {
                return null;
            }

            if (target instanceof FileObject) {
                FileObject fo = (FileObject) target;
                if (isOtherProjectSource(fo, prj)) {
                    return null; // Don't waste time if project does not own the fo among sources
                }

                for (Node n : root.getChildren().getNodes(true)) {
                    Node result = PackageView.findPath(n, target);
                    if (result != null) {
                        return result;
                    }
                }
            }

            return null;
        }

        private static boolean isOtherProjectSource(
                @NonNull final FileObject fo,
                @NonNull final Project me) {
            final Project owner = FileOwnerQuery.getOwner(fo);
            if (owner == null) {
                return false;
            }
            if (me.equals(owner)) {
                return false;
            }
            for (SourceGroup sg : ProjectUtils.getSources(owner).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                if (FileUtil.isParentOf(sg.getRootFolder(), fo)) {
                    return true;
                }
            }
            return false;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public void testBroken() {
            task.schedule(500);
        }

        private boolean isCompileOnSaveDisabled() {
            return badgeStatus != null && badgeStatus.isBadgeVisible();
        }

        private final class LogicalViewRootNode extends AbstractNode implements ChangeListener, PropertyChangeListener {

            private final ProjectInformation info;

            @SuppressWarnings("LeakingThisInConstructor")
            LogicalViewRootNode(@NonNull final Lookup lkp) {
                super(NodeFactorySupport.createCompositeChildren(
                  project,
                  String.format("Projects/%s/Nodes",projectType)),  //NOI18N
                  lkp);
                broken = ProjectProblems.isBroken(project);
                compileOnSaveDisabled = isCompileOnSaveDisabled();
                addChangeListener(WeakListeners.change(this, LogicalViewProviderImpl.this));
                final ProjectInformation pi = project.getLookup().lookup(ProjectInformation.class);
                info = pi != null ? pi : new SimpleInfo(project);
                info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
            }

            @Override
            public String getShortDescription() {
                String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
                return NbBundle.getMessage(LogicalViewProviderImpl.class, "HINT_project_root_node", prjDirDispName);
            }

            @Override
            public String getHtmlDisplayName() {
                String dispName = super.getDisplayName();
                try {
                    dispName = XMLUtil.toElementContent(dispName);
                } catch (CharConversionException ex) {
                    return dispName;
                }
                return broken ? "<font color=\"#"+Integer.toHexString(getErrorForeground().getRGB() & 0xffffff) +"\">" + dispName + "</font>" : null; //NOI18N
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                fireIconChange();
                fireOpenedIconChange();
                fireDisplayNameChange(null, null);
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                RP.post(new Runnable() {
                    public @Override void run() {
                        fireNameChange(null, null);
                        fireDisplayNameChange(null, null);
                    }
                });
            }

            @Override
            public Action[] getActions( boolean context ) {
                return CommonProjectActions.forType(projectType);
            }

            @Override
            public boolean canRename() {
                return true;
            }

            @Override
            public String getName() {
                return info.getDisplayName();
            }

            @Override
            public void setName(String s) {
                DefaultProjectOperations.performDefaultRenameOperation(project, s);
            }

            @Override
            public Image getIcon(int type) {
                final Icon icon = info.getIcon();
                final Image img = icon == null ?
                    super.getIcon(type) :
                    ImageUtilities.icon2Image(icon);
                return !broken && compileOnSaveDisabled ?
                    ImageUtilities.mergeImages(img, compileOnSaveDisabledBadge, 8, 0) :
                    img;
            }

            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }

            @Override
            public HelpCtx getHelpCtx() {
                return helpContext == null ?
                    super.getHelpCtx() :
                    helpContext;
            }

        }

        // Private methods -------------------------------------------------

        private void setBroken(boolean broken) {
            //Weak consistent, only visibility required
            if (this.broken != broken) {
                this.broken = broken;
                changeSupport.fireChange();
            }
        }

        private void setCompileOnSaveDisabled (boolean value) {
            //Weak consistent, only visibility required
            if (this.compileOnSaveDisabled != value) {
                this.compileOnSaveDisabled = value;
                changeSupport.fireChange();
            }
        }


        @NonNull
        private static Color getErrorForeground() {
            Color result = UIManager.getDefaults().getColor("nb.errorForeground");  //NOI18N
            if (result == null) {
                result = Color.RED;
            }
            return result;
        }
    }

    private static final class SimpleInfo implements ProjectInformation {

        private final Project project;

        SimpleInfo(@NonNull final Project project) {
            Parameters.notNull("project", project); //NOI18N
            this.project = project;
        }

        @Override
        public String getName() {
            return project.getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            //Immutable
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            //Immutable
        }
    }
}
