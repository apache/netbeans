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

package org.netbeans.modules.javaee.project.api.ant.ui.logicalview;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.common.ui.BrokenDatasourceSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ChangeSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.w3c.dom.Element;

/**
 * Support for creating logical views.
 */
public abstract class AbstractLogicalViewProvider implements LogicalViewProvider2 {

    private static final RequestProcessor RP = new RequestProcessor("AbstractLogicalViewProvider.RP"); // NOI18N
    private static final Logger LOGGER = Logger.getLogger(AbstractLogicalViewProvider.class.getName());
    
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N
    
    private final Project project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper resolver;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final AtomicBoolean listensOnProblems = new AtomicBoolean();
    private PropertyChangeListener pcl;
    private ConnectionListener cl;
    private InstanceListener il;
    private ConfigurationFilesListener cfl;
    private Map<URL,Object[]> activeLibManLocs;

    protected AbstractLogicalViewProvider(Project project, UpdateHelper helper,
            PropertyEvaluator evaluator, ReferenceHelper resolver, J2eeModuleProvider j2eeModuleProvider) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.resolver = resolver;
        assert j2eeModuleProvider != null;
        registerListeners(j2eeModuleProvider);
    }

    private void registerListeners(J2eeModuleProvider j2eeModuleProvider) {
        pcl = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                testBroken();
            }
        };
        il = new InstanceListener() {

            @Override
            public void instanceAdded(String serverInstanceID) {
                testBroken();
            }

            @Override
            public void instanceRemoved(String serverInstanceID) {
                testBroken();
            }
        };
        cfl = new ConfigurationFilesListener() {

            @Override
            public void fileCreated(FileObject added) {
                testBroken();
            }

            @Override
            public void fileDeleted(FileObject removed) {
                testBroken();
            }
        };
        cl = new ConnectionListener() {

            @Override
            public void connectionsChanged() {
                testBroken();
            }
        };
        evaluator.addPropertyChangeListener(pcl);
        j2eeModuleProvider.addInstanceListener(WeakListeners.create(InstanceListener.class, il, j2eeModuleProvider));
//        j2eeModuleProvider.addConfigurationFilesListener((ConfigurationFilesListener)WeakListeners.create(
//                    ConfigurationFilesListener.class, cfl, j2eeModuleProvider));
        ConnectionManager.getDefault().addConnectionListener(WeakListeners.create(
                ConnectionListener.class, cl, ConnectionManager.getDefault()));
    }

    /**
     * Returns <code>true</code> if the provider has been initialized and broken
     * checks may proceed.
     *
     * @since 1.54
     * @return <code>true</code> if the provider has been initialized and broken
     *            checks may proceed
     */
    protected boolean isInitialized() {
        // #187910 - ignore events which are received too early, that is before
        // project lookup was constructed
        return project.getLookup() != null;
    }

    @Override
    public Node findPath(Node root, Object target) {
        Project proj = root.getLookup().lookup(Project.class);
        if (proj == null) {
            return null;
        }

        if (target instanceof FileObject) {
            return findPath(root, proj, (FileObject) target);
        }
        return null;
    }

    protected Node findPath(Node root, Project proj, FileObject fo) {
        if (isOtherProjectSource(fo, proj)) {
            return null; // Don't waste time if project does not own the fo
        }

        // trying to find node in sources
        for (Node n : root.getChildren().getNodes(true)) {
            Node result = PackageView.findPath(n, fo);
            if (result != null) {
                return result;
            }
        }

        // trying to find node in config files
        Node result = findNodeInConfigFiles(root, fo);
        if (result != null) {
            return result;
        }
        
        // trying to find node in setup/server files
        result = findNodeInServerFiles(root, fo);
        if (result != null) {
            return result;
        }
//
// leave this out for subclasses to handle:
//        // trying to find node in docbase
//        result = findNodeInDocBase(root, fo, WebProjectProperties.WEB_DOCBASE_DIR);
//        if (result != null)
//            return result;
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
        final Sources sources = ProjectUtils.getSources(owner);
        if (isInSourceGroup(fo, sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA))) {
            return true;
        }
        if (isInSourceGroup(fo, sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT))) {
            return true;
        }
        if (isInSourceGroup(fo, sources.getSourceGroups(WebProjectConstants.TYPE_WEB_INF))) {
            return true;
        }
        return false;
    }

    private static boolean isInSourceGroup(
            @NonNull final FileObject fo,
            @NonNull final SourceGroup... sgs) {
        for (SourceGroup sg : sgs) {
            if (FileUtil.isParentOf(sg.getRootFolder(), fo)) {
                return true;
            }
        }
        return false;
    }
    
    protected Node findNodeInDocBase(Node root, FileObject fo, String docBasePropertyName) {
        String prop = evaluator.getProperty (docBasePropertyName);
        if (prop == null) {
            // if project does not have web document root:
            return null;
        }
        FileObject rootfo = helper.getAntProjectHelper().resolveFileObject(prop);
        if (rootfo == null) {
            return null;
        }
        String relPath = FileUtil.getRelativePath(rootfo, fo);
        if (relPath == null) {
            return null;
        }
        int idx = relPath.lastIndexOf('/'); //NOI18N
        if (idx != -1)
        {
            relPath = relPath.substring(0, idx);
        }
        else
        {
            relPath = "";
        }

        StringTokenizer st = new StringTokenizer(relPath, "/"); //NOI18N
        Node result = NodeOp.findChild(root,rootfo.getName());
        if (result == null) {
            return null;
        }

        while (st.hasMoreTokens()) {
            result = NodeOp.findChild(result, st.nextToken());
            if (result == null) {
                return null;
            }
        }

        for (Node child : result.getChildren().getNodes(true))
        {
           DataObject dobj = child.getLookup().lookup(DataObject.class);
           if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt()))
           {
               result = child;
               break;
           }
        }
        
        return result;
    }
    
    private Node findNodeInConfigFiles(Node root, FileObject fo) {
        // XXX ugly, some node names contain the extension and other don't
        // so retrieving the node name from the corresp. DataObject
        String nodeName;
        try {
            DataObject dobj = DataObject.find(fo);
            nodeName = dobj.getName();
        } catch (DataObjectNotFoundException e) {
            nodeName = fo.getName();
        }
        Node configFiles = root.getChildren().findChild("configurationFiles"); // NOI18N
        if (configFiles == null) {
            return null;
        }
        return NodeOp.findChild(configFiles, nodeName);
    }
    
    private Node findNodeInServerFiles(Node root, FileObject fo) {
        Node configFiles = root.getChildren().findChild("setup"); // NOI18N
        if (configFiles == null) {
            return null;
        }
        return NodeOp.findChild(configFiles, fo.getName());
    }
    
    public void addChangeListener (ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener (ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private final RequestProcessor.Task task = RP.create(new Runnable() {
        public @Override void run() {
            if (!ProjectManager.getDefault().isValid(project)) {
                return;
            }
            boolean old = broken;
            boolean _broken = hasBrokenLinks();
            if (old != _broken) {
                setBroken(_broken);
            }
            old = deployOnSaveDisabled;
            boolean _deployOnSaveDisabled = isDeployOnSaveSupportedAndDisabled();
            if (old != _deployOnSaveDisabled) {
                setDeployOnSaveDisabled(_deployOnSaveDisabled);
            }
            old = brokenServer;
            boolean _brokenServer = hasBrokenServer();
            if (old != _brokenServer) {
                setBrokenServer(_brokenServer);
            }
            old = brokenDataSource;
            boolean _brokenDataSource = hasBrokenDataSource();
            if (old != _brokenDataSource) {
                setBrokenDataSource(_brokenDataSource);
            }
            old = brokenServerLibrary;
            boolean _brokenServerLibrary = hasBrokenServerLibrary();
            if (old != _brokenServerLibrary) {
                setBrokenServerLibrary(_brokenServerLibrary);
            }
        }
    });

    /**
     * Used by WebProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advises him to use BrokenLinksAction to correct it.
     *
     */
    @Override
    public void testBroken () {
        if (isInitialized()) {
            if (listensOnProblems.compareAndSet(false,true)) {
                final ProjectProblemsProvider problems = project.getLookup().lookup(ProjectProblemsProvider.class);
                if (problems != null) {
                    problems.addPropertyChangeListener(pcl);
                }
            }
            task.schedule(500);
        }
    }
    
    public boolean hasBrokenLinks() {
        return ProjectProblems.isBroken(project);
    }
    
    abstract public String[] getBreakableProperties();

    public String[] getPlatformProperties() {
        return new String[] {JAVA_PLATFORM};
    }

    protected final Project getProject() {
        return project;
    }

    protected final String[] createListOfBreakableProperties(SourceRoots sources, SourceRoots tests, String[] otherBreakableProperties) {
        String[] srcRootProps = sources.getRootProperties();
        String[] testRootProps = tests.getRootProperties();
        String[] result = new String [otherBreakableProperties.length + srcRootProps.length + testRootProps.length];
        System.arraycopy(otherBreakableProperties, 0, result, 0, otherBreakableProperties.length);
        System.arraycopy(srcRootProps, 0, result, otherBreakableProperties.length, srcRootProps.length);
        System.arraycopy(testRootProps, 0, result, otherBreakableProperties.length + srcRootProps.length, testRootProps.length);
        return result;
    }        
    
    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    public static JavaPlatform getActivePlatform (final String activePlatformId) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (activePlatformId == null) {
            return pm.getDefaultPlatform();
        }
        else {
            JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification ("j2se",null));   //NOI18N
            for (JavaPlatform p : installedPlatforms) {
                String antName = p.getProperties().get("platform.ant.name"); // NOI18N
                if (antName != null && antName.equals(activePlatformId)) {
                    return p;
                }
            }
            return null;
        }
    }

    private boolean isDeployOnSaveSupportedAndDisabled() {
        boolean deployOnSaveEnabled = Boolean.valueOf(evaluator.getProperty(
                "j2ee.deploy.on.save"));
        if (deployOnSaveEnabled) {
            return false;
        }

        boolean deployOnSaveSupported = false;
        try {
            String instanceId = evaluator.getProperty("j2ee.server.instance");
            if (instanceId != null) {
                deployOnSaveSupported = Deployment.getDefault().getServerInstance(instanceId)
                        .isDeployOnSaveSupported();
            }
        } catch (InstanceRemovedException ex) {
            // false
        }
        return deployOnSaveSupported;
    }

    abstract protected void setServerInstance(final Project project, final UpdateHelper helper, final String serverInstanceID);

    
    /** Filter node containin additional features for the J2SE physical
     */
    public final class LogicalViewRootNode extends AbstractNode implements ChangeListener {

        private String shortDesc;
        private String actionsFolderLayer;
        private Class helpContext;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public LogicalViewRootNode(String childrenFolderLayer, String actionsFolderLayer,
                String iconBase, String shortDesc, Class helpContext) {
            super(NodeFactorySupport.createCompositeChildren(project, childrenFolderLayer), 
                    Lookups.fixed(project, project.getProjectDirectory()));
            this.shortDesc = shortDesc;
            this.actionsFolderLayer = actionsFolderLayer;
            this.helpContext = helpContext;
            setIconBaseWithExtension(iconBase); //NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            addChangeListener(WeakListeners.change(this, AbstractLogicalViewProvider.this));
            testBroken();
        }

        @Override                                                                                                            
        public String getShortDescription() {                                                                                
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());                              
            return MessageFormat.format(shortDesc, prjDirDispName);
        }   

        @Override
        public Image getIcon( int type ) {
            Image original = super.getIcon( type );
            if (isBroken()) {
                return ImageUtilities.mergeImages(original, ProjectProperties.ICON_BROKEN_BADGE.getImage(), 8, 0);
            } else if (deployOnSaveDisabled) {
                return DeployOnSaveUtils.badgeDisabledDeployOnSave(original);
            } else {
                return original;
            }
        }

        @Override
        public Image getOpenedIcon( int type ) {
            Image original = super.getOpenedIcon(type);
            if (isBroken()) {
                return ImageUtilities.mergeImages(original, ProjectProperties.ICON_BROKEN_BADGE.getImage(), 8, 0);
            } else if (deployOnSaveDisabled) {
                return DeployOnSaveUtils.badgeDisabledDeployOnSave(original);
            } else {
                return original;
            }
        }

        @Override
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            return isBroken() ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }

        @Override
        public Action[] getActions( boolean context ) {
            return CommonProjectActions.forType(actionsFolderLayer); // NOI18N
        }

        @Override
        public boolean canRename() {
            return true;
        }
        
        @Override
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(helpContext);
        }
        
        // Private methods -------------------------------------------------

        private boolean isBroken() {
            return broken || brokenServer || brokenDataSource || brokenServerLibrary;
        }

        public @Override void stateChanged(ChangeEvent e) {
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }

    }
    
    private boolean broken;         //Represents a state where project has a broken reference repairable by broken reference support
    private boolean deployOnSaveDisabled;  //true iff Deploy-on-Save is disabled
    private boolean brokenServer;
    private boolean brokenDataSource;
    private boolean brokenServerLibrary;

    // Private methods -------------------------------------------------

    private void setBroken(boolean broken) {
        this.broken = broken;
        changeSupport.fireChange();
    }

    private void setDeployOnSaveDisabled (boolean value) {
        this.deployOnSaveDisabled = value;
        changeSupport.fireChange();
    }

    private void setBrokenDataSource(boolean brokenDataSource) {
        this.brokenDataSource = brokenDataSource;
        changeSupport.fireChange();
    }

    private void setBrokenServer(boolean brokenServer) {
        this.brokenServer = brokenServer;
        changeSupport.fireChange();
    }

    private void setBrokenServerLibrary(boolean brokenServerLibrary) {
        this.brokenServerLibrary = brokenServerLibrary;
        changeSupport.fireChange();
    }
        
    /** This action is created only when project has broken references.
     * Once these are resolved the action is disabled.
     */
    private static final class ActionFactory extends AbstractAction implements ContextAwareAction {

        private int mode;

        private ActionFactory(int mode) {
            this.mode = mode;
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        public @Override Action createContextAwareInstance(Lookup actionContext) {
            Collection<? extends Project> p = actionContext.lookupAll(Project.class);
            if (p.size() != 1) {
                return this;
            }
            AbstractLogicalViewProvider lvp = p.iterator().next().getLookup().lookup(AbstractLogicalViewProvider.class);
            if (lvp == null) {
                return this;
            }
            switch (mode) {
                case 1: return lvp.new BrokenLinksAction();
                case 2: return lvp.new BrokenServerAction();
                case 3: return lvp.new BrokenDataSourceAction();
                default: return lvp.new BrokenServerLibraryAction();
            }
            
        }

    }

    public static Action brokenLinksActionFactory() {
        return new ActionFactory(1);
    }
        
    public static Action brokenServerActionFactory() {
        return new ActionFactory(2);
    }

    public static Action brokenDataSourceActionFactory() {
        return new ActionFactory(3);
    }

    public static Action brokenServerLibraryActionFactory() {
        return new ActionFactory(4);
    }

    public static Action redeploy() {
        return ProjectSensitiveActions.projectCommandAction( "redeploy", NbBundle.getBundle(AbstractLogicalViewProvider.class).getString( "LBL_RedeployAction_Name" ), null ); // NOI18N
    }

    public static final class VerifyAction extends AbstractAction implements ContextAwareAction {

        public VerifyAction() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            putValue(Action.NAME, NbBundle.getMessage(AbstractLogicalViewProvider.class, "LBL_VerifyAction_Name"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        public @Override Action createContextAwareInstance(Lookup actionContext) {
            Project p = actionContext.lookup(Project.class);
            if (p == null) {
                return this;
            }
            J2eeModuleProvider provider = p.getLookup().lookup(J2eeModuleProvider.class);
            if (provider == null) {
                return this;
            }
            if (!provider.hasVerifierSupport()) {
                return this;
            }
            return ProjectSensitiveActions.projectCommandAction( "verify", NbBundle.getMessage(AbstractLogicalViewProvider.class, "LBL_VerifyAction_Name" ), null );
        }

    }

    /** This action is created only when project has broken references.
     * Once these are resolved the action is disabled.
     */
    private class BrokenLinksAction extends AbstractAction {

        public BrokenLinksAction() {
            putValue(Action.NAME, NbBundle.getMessage(AbstractLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
            setEnabled(broken);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                helper.requestUpdate();
                ProjectProblems.showCustomizer(project);
                testBroken();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }

    }

    private class BrokenServerAction extends AbstractAction {

        public BrokenServerAction() {
            putValue(Action.NAME, NbBundle.getMessage(AbstractLogicalViewProvider.class, "LBL_Fix_Missing_Server_Action"));
            setEnabled(brokenServer);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        @NbBundle.Messages("ERR_ProjectReadOnly=The project folder is read-only.")
        public void actionPerformed(ActionEvent e) {
            try {
                helper.requestUpdate();
                Profile j2eeProfile = Profile.fromPropertiesString(helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).
                        getProperty("j2ee.platform"));
                if (j2eeProfile == null) {
                    j2eeProfile = Profile.JAVA_EE_5; // NOI18N
                    Logger.getLogger(AbstractLogicalViewProvider.class.getName()).warning(
                            "project ["+project.getProjectDirectory()+"] is missing "+"j2ee.platform"+". " + // NOI18N
                            "default value will be used instead: "+j2eeProfile); // NOI18N
                    updateJ2EESpec(project, helper.getAntProjectHelper(), j2eeProfile);
                }
                String instance = BrokenServerSupport.selectServer(j2eeProfile, J2eeModule.Type.WAR);
                if (instance != null) {
                    setServerInstance(
                            project, helper, instance);
                }
                testBroken();
            } catch (IOException ioe) {
                // #222721 - Provide a better error message in case of read-only location of project.
                if (!project.getProjectDirectory().canWrite()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.ERR_ProjectReadOnly());
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        private void updateJ2EESpec(final Project project, final AntProjectHelper helper, final Profile j2eeProfile) {
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                @Override
                public void run() {
                    try {
                        EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        projectProps.put("j2ee.platform", j2eeProfile.toPropertiesString());
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        }

    }

    private class BrokenDataSourceAction extends AbstractAction {

        public BrokenDataSourceAction() {
            putValue(Action.NAME, NbBundle.getMessage(AbstractLogicalViewProvider.class, "LBL_Fix_Broken_Datasource_Action"));
            setEnabled(brokenDataSource);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                helper.requestUpdate();
                BrokenDatasourceSupport.fixDatasources(project);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }

    }

    private class BrokenServerLibraryAction extends AbstractAction {

        public BrokenServerLibraryAction() {
            putValue(Action.NAME, NbBundle.getMessage(AbstractLogicalViewProvider.class, "LBL_Fix_Broken_Server_Library_Action"));
            setEnabled(brokenServerLibrary);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BrokenServerLibrarySupport.fixServerLibraries(project, new Runnable() {
                @Override
                public void run() {
                    testBroken();
                }
            });
        }

    }

    private boolean hasBrokenServer() {
        String servInstID = evaluator.getProperty("j2ee.server.instance");
        return BrokenServerSupport.isBroken(servInstID);
    }

    // Used to check to see if project is a visualweb or Creator project
    private boolean isVisualWebLegacyProject() {
        boolean isLegacyProject = false;

        // Check if Web module is a visualweb 5.5.x or Creator project
        AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration(project);
        Element auxElement = ac.getConfigurationFragment("creator-data", "http://www.sun.com/creator/ns", true); //NOI18N

        // if project is a visualweb or creator project then find out whether it is a legacy project
        if (auxElement != null) {
            String version = auxElement.getAttribute("jsf.project.version"); //NOI18N
            if (version != null) {
                if (!version.equals("4.0")) { //NOI18N
                    isLegacyProject = true;
                }
            }
        }

        return isLegacyProject;
    }
            
    /*
     * Badge project node, change font color of display name to red and
     * post an alert dialog if not the first time
     */
    private boolean hasBrokenDataSource() {
        if (isVisualWebLegacyProject()) {
            return false;
        }
        // if the project has any broken data sources then set the brokenDatasource flag to true
        return !BrokenDatasourceSupport.getBrokenDatasources(project).isEmpty();
    }

    private boolean hasBrokenServerLibrary() {
        return BrokenServerLibrarySupport.isBroken(project);
    }
            
    private static class OpenManagersWeakListener extends WeakReference<PropertyChangeListener> implements Runnable, PropertyChangeListener {

        public OpenManagersWeakListener(final PropertyChangeListener listener) {
            super(listener, Utilities.activeReferenceQueue());
        }

        @Override
        public void run() {
            LibraryManager.removeOpenManagersPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final PropertyChangeListener listener = get();
            if (listener != null) {
                listener.propertyChange(evt);
            }
        }

    }
}
