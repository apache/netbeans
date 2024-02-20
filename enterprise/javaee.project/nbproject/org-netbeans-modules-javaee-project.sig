#Signature file v4.1
#Version 1.42

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public java.beans.FeatureDescriptor
cons public init()
meth public boolean isExpert()
meth public boolean isHidden()
meth public boolean isPreferred()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public java.lang.String toString()
meth public java.util.Enumeration<java.lang.String> attributeNames()
meth public void setDisplayName(java.lang.String)
meth public void setExpert(boolean)
meth public void setHidden(boolean)
meth public void setName(java.lang.String)
meth public void setPreferred(boolean)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface java.util.EventListener

CLSS public abstract javax.swing.AbstractAction
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
fld protected boolean enabled
fld protected javax.swing.event.SwingPropertyChangeSupport changeSupport
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.Action
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object[] getKeys()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public abstract interface org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier
meth public abstract boolean supportsDefaultProvider()
meth public abstract java.util.List<org.netbeans.modules.j2ee.persistence.provider.Provider> getSupportedProviders()

CLSS public final org.netbeans.modules.java.api.common.project.ui.LibrariesNode
cons public init(java.lang.String,org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String,java.lang.String[],java.lang.String,javax.swing.Action[],java.lang.String,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Callback)
fld public final static org.openide.util.RequestProcessor rp
innr public abstract interface static Callback
innr public final static Builder
innr public final static Key
meth public boolean canCopy()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public javax.swing.Action[] getActions(boolean)
meth public static javax.swing.Action createAddFolderAction(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.java.api.common.SourceRoots)
meth public static javax.swing.Action createAddLibraryAction(org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.api.project.libraries.LibraryChooser$Filter)
meth public static javax.swing.Action createAddProjectAction(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.SourceRoots)
supr org.openide.nodes.AbstractNode
hfds ICON_BADGE,displayName,folderIconCache,librariesNodeActions,openedFolderIconCache
hcls AddFolderAction,AddLibraryAction,AddProjectAction,LibrariesChildren,PathFinder,RootsListener,SimpleFileFilter

CLSS public abstract interface static org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Callback
 outer org.netbeans.modules.java.api.common.project.ui.LibrariesNode
meth public abstract java.util.List<org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key> getExtraKeys()
meth public abstract org.openide.nodes.Node[] createNodes(org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key)

CLSS public abstract interface org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2
intf org.netbeans.spi.project.ui.LogicalViewProvider
meth public abstract void testBroken()

CLSS public final org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport
innr public static Pattern
intf org.netbeans.modules.web.browser.spi.PageInspectorCustomizer
intf org.netbeans.modules.web.browser.spi.URLDisplayerImplementation
intf org.netbeans.modules.web.common.spi.ServerURLMappingImplementation
meth public boolean canReload()
meth public boolean isHighlightSelectionEnabled()
meth public java.net.URL toServer(int,org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject fromServer(int,java.net.URL)
meth public static org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport createInstance(org.netbeans.api.project.Project,java.lang.String,java.lang.String)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void close()
meth public void reload(org.openide.filesystems.FileObject)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void resetBrowserSupport()
meth public void setWebProject(org.netbeans.api.project.Project)
meth public void showURL(java.net.URL,java.net.URL,org.openide.filesystems.FileObject)
supr java.lang.Object
hfds browserSupport,browserSupportInitialized,browserUsageLogger,initialized,project,projectRootURL,projectType,servletURLPatterns,webDocumentRoot,webProject,welcomeFiles

CLSS public static org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern
 outer org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport
cons public init(org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern$Type,java.lang.String)
innr public final static !enum Type
meth public java.lang.String getPattern()
meth public org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern$Type getType()
supr java.lang.Object
hfds pattern,type

CLSS public final static !enum org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern$Type
 outer org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern
fld public final static org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern$Type PREFIX
fld public final static org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern$Type SUFFIX
meth public static org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern$Type[] values()
supr java.lang.Enum<org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern$Type>

CLSS public final org.netbeans.modules.javaee.project.api.JavaEEProjectSettingConstants
fld public final static java.lang.String J2EE_PLATFORM = "j2ee.platform"
fld public final static java.lang.String J2EE_SERVER_INSTANCE = "j2ee.server.instance"
fld public final static java.lang.String SELECTED_BROWSER = "selected.browser"
supr java.lang.Object

CLSS public final org.netbeans.modules.javaee.project.api.JavaEEProjectSettings
meth public static java.lang.String getBrowserID(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getServerInstanceID(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.j2ee.core.Profile getProfile(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void setBrowserID(org.netbeans.api.project.Project,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void setProfile(org.netbeans.api.project.Project,org.netbeans.api.j2ee.core.Profile)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void setServerInstanceID(org.netbeans.api.project.Project,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls UnsupportedProjectTypeException

CLSS public final org.netbeans.modules.javaee.project.api.PersistenceProviderSupplierImpl
cons public init(org.netbeans.api.project.Project)
intf org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier
meth public boolean supportsDefaultProvider()
meth public java.util.List<org.netbeans.modules.j2ee.persistence.provider.Provider> getSupportedProviders()
supr java.lang.Object
hfds project

CLSS public abstract org.netbeans.modules.javaee.project.api.WhiteListUpdater
cons public init(org.netbeans.api.project.Project)
fld protected org.netbeans.api.project.Project project
meth protected abstract void addSettingListener()
meth protected java.lang.String getServerWhiteList()
meth protected void updateWhitelist(java.lang.String,java.lang.String)
meth public static boolean isWhitelistViolated(org.netbeans.api.project.Project)
meth public void checkWhiteLists()
supr java.lang.Object
hfds lastWhiteList,rp

CLSS public org.netbeans.modules.javaee.project.api.ant.AntProjectConstants
cons public init()
fld public final static java.lang.String DESTINATION_DIRECTORY = "destinationDirectory"
fld public final static java.lang.String DESTINATION_DIRECTORY_DO_NOT_COPY = "300"
fld public final static java.lang.String DESTINATION_DIRECTORY_LIB = "200"
fld public final static java.lang.String DESTINATION_DIRECTORY_ROOT = "100"
fld public final static java.lang.String ENDORSED_LIBRARY_CLASSPATH_6 = "${libs.javaee-endorsed-api-6.0.classpath}"
fld public final static java.lang.String ENDORSED_LIBRARY_CLASSPATH_7 = "${libs.javaee-endorsed-api-7.0.classpath}"
fld public final static java.lang.String ENDORSED_LIBRARY_CLASSPATH_8 = "${libs.javaee-endorsed-api-8.0.classpath}"
fld public final static java.lang.String ENDORSED_LIBRARY_NAME_6 = "javaee-endorsed-api-6.0"
fld public final static java.lang.String ENDORSED_LIBRARY_NAME_7 = "javaee-endorsed-api-7.0"
fld public final static java.lang.String ENDORSED_LIBRARY_NAME_8 = "javaee-endorsed-api-8.0"
supr java.lang.Object

CLSS public org.netbeans.modules.javaee.project.api.ant.AntProjectUtil
cons public init()
meth public static void backupBuildImplFile(org.netbeans.modules.java.api.common.ant.UpdateHelper) throws java.io.IOException
meth public static void updateDirsAttributeInCPSItem(org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item,org.w3c.dom.Element)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport
cons public init(java.lang.String,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.AntProjectHelper)
innr public final static !enum RelocationType
innr public final static Item
innr public final static ItemDescription
intf java.beans.PropertyChangeListener
intf org.netbeans.spi.project.support.ant.AntProjectListener
intf org.openide.filesystems.FileChangeListener
meth protected abstract java.util.List<org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$Item> getArtifacts()
meth protected org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact filterArtifact(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact,org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType)
meth public final void addArtifactListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener)
meth public final void close()
meth public final void configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
meth public final void initialize()
meth public final void propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
meth public final void propertyChange(java.beans.PropertyChangeEvent)
meth public final void reload()
meth public final void removeArtifactListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener)
meth public void enableArtifactSynchronization(boolean)
meth public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void fileChanged(org.openide.filesystems.FileEvent)
meth public void fileDataCreated(org.openide.filesystems.FileEvent)
meth public void fileDeleted(org.openide.filesystems.FileEvent)
meth public void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void fileRenamed(org.openide.filesystems.FileRenameEvent)
supr java.lang.Object
hfds LOGGER,antHelper,destDir,destDirProperty,evaluator,listeners,listeningTo,synchronize

CLSS public final static org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$Item
 outer org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport
cons public init(org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item,org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$ItemDescription)
meth public org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item getItem()
meth public org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$ItemDescription getDescription()
supr java.lang.Object
hfds description,item

CLSS public final static org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$ItemDescription
 outer org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport
cons public init(java.lang.String,org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType)
meth public java.lang.String getPathInDeployment()
meth public org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType getRelocationType()
supr java.lang.Object
hfds pathInDeployment,type

CLSS public final static !enum org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType
 outer org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport
fld public final static org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType LIB
fld public final static org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType NONE
fld public final static org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType ROOT
meth public static org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType fromString(java.lang.String)
meth public static org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType valueOf(java.lang.String)
meth public static org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType[] values()
supr java.lang.Enum<org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport$RelocationType>

CLSS public final org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils
innr public abstract interface static CustomizerPresenter
meth public static boolean containsIdeArtifacts(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper,java.lang.String)
meth public static boolean showBuildActionWarning(org.netbeans.api.project.Project,org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils$CustomizerPresenter)
meth public static java.awt.Image badgeDisabledDeployOnSave(java.awt.Image)
meth public static java.lang.String isDeployOnSaveSupported(java.lang.String)
meth public static void performCleanup(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.modules.java.api.common.ant.UpdateHelper,java.lang.String,boolean)
supr java.lang.Object
hfds COS_MARK,DEPLOY_ON_SAVE_DISABLED_BADGE,DEPLOY_ON_SAVE_DISABLED_BADGE_PATH

CLSS public abstract interface static org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils$CustomizerPresenter
 outer org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils
meth public abstract void showCustomizer(java.lang.String)

CLSS public final org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties
cons public init()
fld public final static java.lang.String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"
fld public final static java.lang.String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"
fld public final static java.lang.String J2EE_DOMAIN_HOME = "j2ee.server.domain"
fld public final static java.lang.String J2EE_MIDDLEWARE_HOME = "j2ee.server.middleware"
fld public final static java.lang.String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"
fld public final static java.lang.String J2EE_PLATFORM_EMBEDDABLE_EJB_CLASSPATH = "j2ee.platform.embeddableejb.classpath"
fld public final static java.lang.String J2EE_PLATFORM_JSR109_SUPPORT = "j2ee.platform.is.jsr109"
fld public final static java.lang.String J2EE_PLATFORM_JWSDP_CLASSPATH = "j2ee.platform.jwsdp.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSCOMPILE_CLASSPATH = "j2ee.platform.wscompile.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSGEN_CLASSPATH = "j2ee.platform.wsgen.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSIMPORT_CLASSPATH = "j2ee.platform.wsimport.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSIT_CLASSPATH = "j2ee.platform.wsit.classpath"
fld public final static java.lang.String J2EE_SERVER_HOME = "j2ee.server.home"
fld public final static java.lang.String J2EE_SERVER_INSTANCE = "j2ee.server.instance"
fld public final static java.lang.String J2EE_SERVER_TYPE = "j2ee.server.type"
innr public abstract interface static Callback
innr public abstract interface static SetServerInstanceCallback
meth public static boolean checkSelectedServer(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.api.j2ee.core.Profile,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type,org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties$SetServerInstanceCallback,boolean,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String getMatchingInstance(java.lang.String,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type,org.netbeans.api.j2ee.core.Profile)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.lang.String toClasspathString(java.io.File[],java.util.Map<java.lang.String,java.lang.String>)
meth public static java.util.Map<java.lang.String,java.lang.String> extractPlatformLibrariesRoot(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
meth public static void createDeploymentScript(org.openide.filesystems.FileObject,org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.spi.project.support.ant.EditableProperties,java.lang.String,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
meth public static void removeObsoleteLibraryLocations(org.netbeans.spi.project.support.ant.EditableProperties)
meth public static void setServerProperties(org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,java.lang.Iterable<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item>,java.lang.String,org.netbeans.api.j2ee.core.Profile,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
meth public static void updateServerProperties(org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.spi.project.support.ant.EditableProperties,java.lang.String,org.netbeans.modules.java.api.common.classpath.ClassPathSupport,java.lang.Iterable<org.netbeans.modules.java.api.common.classpath.ClassPathSupport$Item>,org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties$Callback,org.netbeans.api.project.Project,org.netbeans.api.j2ee.core.Profile,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
supr java.lang.Object
hfds LOGGER

CLSS public abstract interface static org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties$Callback
 outer org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties
meth public abstract void registerJ2eePlatformListener(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)
meth public abstract void unregisterJ2eePlatformListener(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform)

CLSS public abstract interface static org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties$SetServerInstanceCallback
 outer org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties
meth public abstract void setServerInstance(java.lang.String)

CLSS public final org.netbeans.modules.javaee.project.api.ant.ui.customizer.LicensePanelSupport
cons public init(org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.AntProjectHelper,java.lang.String,java.lang.String)
fld public final static java.lang.String LICENSE_NAME = "project.license"
fld public final static java.lang.String LICENSE_PATH = "project.licensePath"
intf org.netbeans.spi.project.support.ant.ui.CustomizerUtilities$LicensePanelContentHandler
meth public java.lang.String getDefaultProjectLicenseLocation()
meth public java.lang.String getGlobalLicenseName()
meth public java.lang.String getProjectLicenseLocation()
meth public org.openide.filesystems.FileObject resolveProjectLocation(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void saveLicenseFile() throws java.io.IOException
meth public void setGlobalLicenseName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void setProjectLicenseContent(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void setProjectLicenseLocation(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void updateProperties(org.netbeans.spi.project.support.ant.EditableProperties)
supr java.lang.Object
hfds antHelper,evaluator,licenseContent,licenseName,licensePath

CLSS public abstract org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider
cons protected init(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
fld public final static java.lang.String JAVA_PLATFORM = "platform.active"
innr public final LogicalViewRootNode
innr public final static VerifyAction
intf org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2
meth protected abstract void setServerInstance(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,java.lang.String)
meth protected boolean isInitialized()
meth protected final java.lang.String[] createListOfBreakableProperties(org.netbeans.modules.java.api.common.SourceRoots,org.netbeans.modules.java.api.common.SourceRoots,java.lang.String[])
meth protected final org.netbeans.api.project.Project getProject()
meth protected org.openide.nodes.Node findNodeInDocBase(org.openide.nodes.Node,org.openide.filesystems.FileObject,java.lang.String)
meth protected org.openide.nodes.Node findPath(org.openide.nodes.Node,org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public abstract java.lang.String[] getBreakableProperties()
meth public boolean hasBrokenLinks()
meth public java.lang.String[] getPlatformProperties()
meth public org.openide.nodes.Node findPath(org.openide.nodes.Node,java.lang.Object)
meth public static javax.swing.Action brokenDataSourceActionFactory()
meth public static javax.swing.Action brokenLinksActionFactory()
meth public static javax.swing.Action brokenServerActionFactory()
meth public static javax.swing.Action brokenServerLibraryActionFactory()
meth public static javax.swing.Action redeploy()
meth public static org.netbeans.api.java.platform.JavaPlatform getActivePlatform(java.lang.String)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void testBroken()
supr java.lang.Object
hfds LOGGER,RP,activeLibManLocs,broken,brokenDataSource,brokenServer,brokenServerLibrary,cfl,changeSupport,cl,deployOnSaveDisabled,evaluator,helper,il,listensOnProblems,pcl,project,resolver,task
hcls ActionFactory,BrokenDataSourceAction,BrokenLinksAction,BrokenServerAction,BrokenServerLibraryAction,OpenManagersWeakListener

CLSS public final org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider$LogicalViewRootNode
 outer org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider
cons public init(org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Class)
intf javax.swing.event.ChangeListener
meth public boolean canRename()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String getShortDescription()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void setName(java.lang.String)
meth public void stateChanged(javax.swing.event.ChangeEvent)
supr org.openide.nodes.AbstractNode
hfds actionsFolderLayer,helpContext,shortDesc

CLSS public final static org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider$VerifyAction
 outer org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider
cons public init()
intf org.openide.util.ContextAwareAction
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction

CLSS public abstract org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider2
cons protected init(org.netbeans.api.project.Project,org.netbeans.modules.java.api.common.ant.UpdateHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth protected boolean isInitialized()
meth public void initialize()
supr org.netbeans.modules.javaee.project.api.ant.ui.logicalview.AbstractLogicalViewProvider
hfds initialized

CLSS public final org.netbeans.modules.javaee.project.api.ant.ui.logicalview.ExtraLibrariesNode
cons public init(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,org.netbeans.modules.java.api.common.classpath.ClassPathSupport)
intf org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Callback
meth public java.util.List<org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key> getExtraKeys()
meth public org.openide.nodes.Node[] createNodes(org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key)
supr java.lang.Object
hfds cs,eval,j2eePlatformProperty,p

CLSS public final org.netbeans.modules.javaee.project.api.ant.ui.logicalview.ExtraLibrariesTestNode
cons public init(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,org.netbeans.modules.java.api.common.classpath.ClassPathSupport)
intf org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Callback
meth public java.util.List<org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key> getExtraKeys()
meth public org.openide.nodes.Node[] createNodes(org.netbeans.modules.java.api.common.project.ui.LibrariesNode$Key)
supr java.lang.Object
hfds cs,eval,j2eePlatformProperty,p

CLSS public final org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectImportLocationWizardPanel
cons public init(java.lang.Object,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.Object,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
fld public final static java.lang.String SOURCE_ROOT = "sourceRoot"
intf org.openide.WizardDescriptor$FinishablePanel
intf org.openide.WizardDescriptor$ValidatingPanel
meth protected void fireChangeEvent()
meth public boolean isFinishPanel()
meth public boolean isValid()
meth public java.awt.Component getComponent()
meth public java.lang.String getBuildFile()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(java.lang.Object)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void storeSettings(java.lang.Object)
meth public void validate() throws org.openide.WizardValidationException
supr java.lang.Object
hfds allowAlternativeBuildXml,buildFile,changeSupport,defaultNameFormatter,importLabel,j2eeModuleType,name,panel,title,wizardDescriptor

CLSS public final org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectLocationWizardPanel
cons public init(java.lang.Object,java.lang.String,java.lang.String,java.lang.String)
fld public final static java.lang.String NAME = "name"
fld public final static java.lang.String PROJECT_DIR = "projdir"
fld public final static java.lang.String SHARED_LIBRARIES = "sharedLibraries"
intf org.openide.WizardDescriptor$FinishablePanel
intf org.openide.WizardDescriptor$Panel
meth protected void fireChangeEvent()
meth public boolean isFinishPanel()
meth public boolean isValid()
meth public java.awt.Component getComponent()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(java.lang.Object)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void storeSettings(java.lang.Object)
supr java.lang.Object
hfds changeSupport,component,defaultNameFormatter,j2eeModuleType,name,title,wizardDescriptor

CLSS public final org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel
cons public init(java.lang.Object,java.lang.String,java.lang.String,boolean,boolean,boolean,boolean,boolean,boolean)
fld public final static java.lang.String CAR_NAME = "carName"
fld public final static java.lang.String CDI = "cdi"
fld public final static java.lang.String CONTEXT_PATH = "contextPath"
fld public final static java.lang.String CREATE_CAR = "createCAR"
fld public final static java.lang.String CREATE_JAR = "createJAR"
fld public final static java.lang.String CREATE_WAR = "createWAR"
fld public final static java.lang.String EAR_APPLICATION = "earApplication"
fld public final static java.lang.String J2EE_LEVEL = "j2eeLevel"
fld public final static java.lang.String JAR_NAME = "jarName"
fld public final static java.lang.String JAVA_PLATFORM = "setJavaPlatform"
fld public final static java.lang.String MAIN_CLASS = "mainClass"
fld public final static java.lang.String SERVER_INSTANCE_ID = "serverInstanceID"
fld public final static java.lang.String SOURCE_LEVEL = "setSourceLevel"
fld public final static java.lang.String WAR_NAME = "warName"
fld public final static java.lang.String WIZARD_SHARED_LIBRARIES = "sharedLibraries"
intf org.openide.WizardDescriptor$FinishablePanel
intf org.openide.WizardDescriptor$Panel
meth protected void fireChangeEvent()
meth public boolean isFinishPanel()
meth public boolean isValid()
meth public java.awt.Component getComponent()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings(java.lang.Object)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void storeSettings(java.lang.Object)
supr java.lang.Object
hfds changeSupport,component,createProjects,finishable,importScenario,j2eeModuleType,mainAppClientClass,name,showAddToEar,showContextPath,title,wizardDescriptor

CLSS public final org.netbeans.modules.javaee.project.api.ear.EarDDGenerator
meth public static org.openide.filesystems.FileObject setupDD(org.netbeans.api.project.Project,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.modules.javaee.project.api.problems.PlatformUpdatedCallBackImpl
intf org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack
meth public static org.netbeans.modules.javaee.project.api.problems.PlatformUpdatedCallBackImpl create(java.lang.String,org.netbeans.modules.java.api.common.ant.UpdateHelper)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.javaee.project.api.problems.PlatformUpdatedCallBackImpl create(java.lang.String,org.netbeans.modules.java.api.common.ant.UpdateHelper,boolean,java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public void platformPropertyUpdated(org.netbeans.api.java.platform.JavaPlatform)
supr java.lang.Object
hfds DEFAULT_PRE_ELEMENTS,helper,nameSpace,precedingElements,setExplicitSourceSupported

CLSS public final org.netbeans.modules.javaee.project.api.ui.UserProjectSettings
cons public init()
meth public boolean isAgreedSetJdk14()
meth public boolean isAgreedSetJdk15()
meth public boolean isAgreedSetSourceLevel14()
meth public boolean isAgreedSetSourceLevel15()
meth public boolean isShowAgainBrokenRefAlert()
meth public boolean isShowAgainBrokenServerAlert()
meth public int getNewApplicationCount()
meth public int getNewProjectCount()
meth public java.io.File getLastChooserLocation()
meth public java.io.File getLastUsedArtifactFolder(java.io.File)
meth public java.io.File getLastUsedClassPathFolder()
meth public java.io.File getLastUsedImportLocation()
meth public java.lang.String displayName()
meth public java.lang.String getLastUsedServer()
meth public static java.util.prefs.Preferences getPreferences()
meth public static org.netbeans.modules.javaee.project.api.ui.UserProjectSettings getDefault()
meth public void setAgreedSetJdk14(boolean)
meth public void setAgreedSetJdk15(boolean)
meth public void setAgreedSetSourceLevel14(boolean)
meth public void setAgreedSetSourceLevel15(boolean)
meth public void setLastChooserLocation(java.io.File)
meth public void setLastUsedArtifactFolder(java.io.File)
meth public void setLastUsedClassPathFolder(java.io.File)
meth public void setLastUsedImportLocation(java.io.File)
meth public void setLastUsedServer(java.lang.String)
meth public void setNewApplicationCount(int)
meth public void setNewProjectCount(int)
meth public void setShowAgainBrokenRefAlert(boolean)
meth public void setShowAgainBrokenServerAlert(boolean)
supr java.lang.Object
hfds AGREED_SET_JDK_14,AGREED_SET_JDK_15,AGREED_SET_SOURCE_LEVEL_14,AGREED_SET_SOURCE_LEVEL_15,INSTANCE,LAST_USED_ARTIFACT_FOLDER,LAST_USED_CHOOSER_LOCATIONS,LAST_USED_CP_FOLDER,LAST_USED_IMPORT_LOCATION,LAST_USED_SERVER,NEW_APP_COUNT,NEW_PROJECT_COUNT,SHOW_AGAIN_BROKEN_REF_ALERT,SHOW_AGAIN_BROKEN_SERVER_ALERT

CLSS public org.netbeans.modules.javaee.project.api.ui.utils.J2eePlatformUiSupport
meth public static java.lang.String getServerInstanceID(java.lang.Object)
meth public static javax.swing.ComboBoxModel createPlatformComboBoxModel(java.lang.String,org.netbeans.api.j2ee.core.Profile,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
meth public static javax.swing.ComboBoxModel createSpecVersionComboBoxModel(org.netbeans.api.j2ee.core.Profile)
meth public static org.netbeans.api.j2ee.core.Profile getJavaEEProfile(java.lang.Object)
supr java.lang.Object
hcls J2eePlatformAdapter,J2eePlatformComboBoxItem,J2eePlatformComboBoxModel,J2eeSpecVersionComboBoxModel

CLSS public final org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils
innr public abstract static !enum MessageType
meth public static void clear(javax.swing.JLabel)
meth public static void setMessage(javax.swing.JLabel,org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils$MessageType,java.lang.String)
supr java.lang.Object

CLSS public abstract static !enum org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils$MessageType
 outer org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils
fld public final static org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils$MessageType ERROR
fld public final static org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils$MessageType WARNING
meth protected abstract java.awt.Color getColor()
meth protected abstract javax.swing.Icon getIcon()
meth public static org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils$MessageType valueOf(java.lang.String)
meth public static org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils$MessageType[] values()
supr java.lang.Enum<org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils$MessageType>

CLSS public org.netbeans.modules.javaee.project.api.ui.utils.UIUtil
cons public init()
meth public static void initTwoColumnTableVisualProperties(java.awt.Component,javax.swing.JTable)
meth public static void updateColumnWidths(javax.swing.JTable)
supr java.lang.Object
hcls TableColumnSizeComponentAdapter

CLSS public abstract interface org.netbeans.modules.javaee.project.spi.FrameworkServerURLMapping
meth public abstract java.lang.String convertFileToRelativeURL(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract org.openide.filesystems.FileObject convertURLtoFile(org.openide.filesystems.FileObject,org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport$Pattern,java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation
meth public abstract java.lang.String getBrowserID()
meth public abstract java.lang.String getServerInstanceID()
meth public abstract org.netbeans.api.j2ee.core.Profile getProfile()
meth public abstract void setBrowserID(java.lang.String)
meth public abstract void setProfile(org.netbeans.api.j2ee.core.Profile)
meth public abstract void setServerInstanceID(java.lang.String)

CLSS public abstract interface org.netbeans.modules.javaee.project.spi.ear.EarDDGeneratorImplementation
meth public abstract org.openide.filesystems.FileObject setupDD(boolean)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.PageInspectorCustomizer
fld public final static java.lang.String PROPERTY_HIGHLIGHT_SELECTION = "highlight.selection"
meth public abstract boolean isHighlightSelectionEnabled()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.URLDisplayerImplementation
meth public abstract void showURL(java.net.URL,java.net.URL,org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.web.common.spi.ServerURLMappingImplementation
meth public abstract java.net.URL toServer(int,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.openide.filesystems.FileObject fromServer(int,java.net.URL)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport
innr public abstract interface static LibraryDefiner
innr public abstract interface static PlatformUpdatedCallBack
meth public !varargs static org.netbeans.spi.project.ui.ProjectProblemsProvider createPlatformVersionProblemProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack,java.lang.String,java.lang.String,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static org.netbeans.spi.project.ui.ProjectProblemsProvider createPlatformVersionProblemProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack,java.lang.String,org.openide.modules.SpecificationVersion,java.lang.String,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static org.netbeans.spi.project.ui.ProjectProblemsProvider createProfileProblemProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isBroken(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String[],java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider createReferenceProblemsProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider createReferenceProblemsProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack,java.lang.String[],java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static void showAlert()
 anno 0 java.lang.Deprecated()
meth public static void showAlert(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static void showCustomizer(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String[],java.lang.String[])
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hcls ProjectDecorator

CLSS public abstract interface static org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack
 outer org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport
meth public abstract void platformPropertyUpdated(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.project.support.ant.AntProjectListener
intf java.util.EventListener
meth public abstract void configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
meth public abstract void propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)

CLSS public final org.netbeans.spi.project.support.ant.ui.CustomizerUtilities
cons public init()
innr public abstract interface static LicensePanelContentHandler
meth public static javax.swing.JComponent createLicenseHeaderCustomizerPanel(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category,org.netbeans.spi.project.support.ant.ui.CustomizerUtilities$LicensePanelContentHandler)
meth public static org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler getLibraryChooserImportHandler(java.io.File)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler getLibraryChooserImportHandler(org.netbeans.spi.project.support.ant.ReferenceHelper)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.spi.project.support.ant.ui.CustomizerUtilities$LicensePanelContentHandler
 outer org.netbeans.spi.project.support.ant.ui.CustomizerUtilities
meth public abstract java.lang.String getDefaultProjectLicenseLocation()
meth public abstract java.lang.String getGlobalLicenseName()
meth public abstract java.lang.String getProjectLicenseLocation()
meth public abstract org.openide.filesystems.FileObject resolveProjectLocation(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setGlobalLicenseName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void setProjectLicenseContent(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void setProjectLicenseLocation(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()

CLSS public abstract interface org.netbeans.spi.project.ui.LogicalViewProvider
intf org.netbeans.spi.project.ui.PathFinder
meth public abstract org.openide.nodes.Node createLogicalView()

CLSS public abstract interface org.netbeans.spi.project.ui.PathFinder
meth public abstract org.openide.nodes.Node findPath(org.openide.nodes.Node,java.lang.Object)

CLSS public org.openide.DialogDescriptor
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.String,boolean,int,java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,int,java.lang.Object,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.lang.Object[],java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.lang.Object[],java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener,boolean)
fld public final static int BOTTOM_ALIGN = 0
fld public final static int DEFAULT_ALIGN = 0
fld public final static int RIGHT_ALIGN = 1
fld public final static java.lang.String PROP_BUTTON_LISTENER = "buttonListener"
fld public final static java.lang.String PROP_CLOSING_OPTIONS = "closingOptions"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_MODAL = "modal"
fld public final static java.lang.String PROP_OPTIONS_ALIGN = "optionsAlign"
intf org.openide.util.HelpCtx$Provider
meth public boolean isLeaf()
meth public boolean isModal()
meth public int getOptionsAlign()
meth public java.awt.event.ActionListener getButtonListener()
meth public java.lang.Object[] getClosingOptions()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void setButtonListener(java.awt.event.ActionListener)
meth public void setClosingOptions(java.lang.Object[])
meth public void setHelpCtx(org.openide.util.HelpCtx)
meth public void setLeaf(boolean)
meth public void setModal(boolean)
meth public void setOptionsAlign(int)
supr org.openide.NotifyDescriptor
hfds DEFAULT_CLOSING_OPTIONS,buttonListener,closingOptions,helpCtx,leaf,modal,optionsAlign

CLSS public org.openide.NotifyDescriptor
cons public init(java.lang.Object,java.lang.String,int,int,java.lang.Object[],java.lang.Object)
fld public final static int DEFAULT_OPTION = -1
fld public final static int ERROR_MESSAGE = 0
fld public final static int INFORMATION_MESSAGE = 1
fld public final static int OK_CANCEL_OPTION = 2
fld public final static int PLAIN_MESSAGE = -1
fld public final static int QUESTION_MESSAGE = 3
fld public final static int WARNING_MESSAGE = 2
fld public final static int YES_NO_CANCEL_OPTION = 1
fld public final static int YES_NO_OPTION = 0
fld public final static java.lang.Object CANCEL_OPTION
fld public final static java.lang.Object CLOSED_OPTION
fld public final static java.lang.Object NO_OPTION
fld public final static java.lang.Object OK_OPTION
fld public final static java.lang.Object YES_OPTION
fld public final static java.lang.String PROP_DETAIL = "detail"
fld public final static java.lang.String PROP_ERROR_NOTIFICATION = "errorNotification"
fld public final static java.lang.String PROP_INFO_NOTIFICATION = "infoNotification"
fld public final static java.lang.String PROP_MESSAGE = "message"
fld public final static java.lang.String PROP_MESSAGE_TYPE = "messageType"
fld public final static java.lang.String PROP_NO_DEFAULT_CLOSE = "noDefaultClose"
fld public final static java.lang.String PROP_OPTIONS = "options"
fld public final static java.lang.String PROP_OPTION_TYPE = "optionType"
fld public final static java.lang.String PROP_TITLE = "title"
fld public final static java.lang.String PROP_VALID = "valid"
fld public final static java.lang.String PROP_VALUE = "value"
fld public final static java.lang.String PROP_WARNING_NOTIFICATION = "warningNotification"
innr public final static ComposedInput
innr public final static Exception
innr public final static PasswordLine
innr public final static QuickPick
innr public static Confirmation
innr public static InputLine
innr public static Message
meth protected static java.lang.String getTitleForType(int)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth public boolean isNoDefaultClose()
meth public final boolean isValid()
meth public final org.openide.NotificationLineSupport createNotificationLineSupport()
meth public final org.openide.NotificationLineSupport getNotificationLineSupport()
meth public final void setValid(boolean)
meth public int getMessageType()
meth public int getOptionType()
meth public java.lang.Object getDefaultValue()
meth public java.lang.Object getMessage()
meth public java.lang.Object getValue()
meth public java.lang.Object[] getAdditionalOptions()
meth public java.lang.Object[] getOptions()
meth public java.lang.String getTitle()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAdditionalOptions(java.lang.Object[])
meth public void setMessage(java.lang.Object)
meth public void setMessageType(int)
meth public void setNoDefaultClose(boolean)
meth public void setOptionType(int)
meth public void setOptions(java.lang.Object[])
meth public void setTitle(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds MAXIMUM_TEXT_WIDTH,SIZE_PREFERRED_HEIGHT,SIZE_PREFERRED_WIDTH,adOptions,changeSupport,defaultValue,errMsg,infoMsg,message,messageType,noDefaultClose,notificationLineSupport,optionType,options,title,valid,value,warnMsg

CLSS public org.openide.WizardDescriptor
cons protected init()
cons public <%0 extends java.lang.Object> init(org.openide.WizardDescriptor$Iterator<{%%0}>,{%%0})
cons public <%0 extends java.lang.Object> init(org.openide.WizardDescriptor$Panel<{%%0}>[],{%%0})
cons public init(org.openide.WizardDescriptor$Iterator<org.openide.WizardDescriptor>)
cons public init(org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>[])
fld public final static java.lang.Object FINISH_OPTION
fld public final static java.lang.Object NEXT_OPTION
fld public final static java.lang.Object PREVIOUS_OPTION
fld public final static java.lang.String PROP_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle"
fld public final static java.lang.String PROP_CONTENT_BACK_COLOR = "WizardPanel_contentBackColor"
fld public final static java.lang.String PROP_CONTENT_DATA = "WizardPanel_contentData"
fld public final static java.lang.String PROP_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"
fld public final static java.lang.String PROP_CONTENT_FOREGROUND_COLOR = "WizardPanel_contentForegroundColor"
fld public final static java.lang.String PROP_CONTENT_NUMBERED = "WizardPanel_contentNumbered"
fld public final static java.lang.String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"
fld public final static java.lang.String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"
fld public final static java.lang.String PROP_HELP_DISPLAYED = "WizardPanel_helpDisplayed"
fld public final static java.lang.String PROP_HELP_URL = "WizardPanel_helpURL"
fld public final static java.lang.String PROP_IMAGE = "WizardPanel_image"
fld public final static java.lang.String PROP_IMAGE_ALIGNMENT = "WizardPanel_imageAlignment"
fld public final static java.lang.String PROP_INFO_MESSAGE = "WizardPanel_infoMessage"
fld public final static java.lang.String PROP_LEFT_DIMENSION = "WizardPanel_leftDimension"
fld public final static java.lang.String PROP_WARNING_MESSAGE = "WizardPanel_warningMessage"
innr public abstract interface static AsynchronousInstantiatingIterator
innr public abstract interface static AsynchronousValidatingPanel
innr public abstract interface static BackgroundInstantiatingIterator
innr public abstract interface static ExtendedAsynchronousValidatingPanel
innr public abstract interface static FinishPanel
innr public abstract interface static FinishablePanel
innr public abstract interface static InstantiatingIterator
innr public abstract interface static Iterator
innr public abstract interface static Panel
innr public abstract interface static ProgressInstantiatingIterator
innr public abstract interface static ValidatingPanel
innr public static ArrayIterator
meth protected void initialize()
meth protected void updateState()
meth public final <%0 extends java.lang.Object> void setPanelsAndSettings(org.openide.WizardDescriptor$Iterator<{%%0}>,{%%0})
meth public final void doCancelClick()
meth public final void doFinishClick()
meth public final void doNextClick()
meth public final void doPreviousClick()
meth public final void setPanels(org.openide.WizardDescriptor$Iterator)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object getValue()
meth public java.text.MessageFormat getTitleFormat()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.Set getInstantiatedObjects()
meth public void putProperty(java.lang.String,java.lang.Object)
meth public void setAdditionalOptions(java.lang.Object[])
meth public void setClosingOptions(java.lang.Object[])
meth public void setHelpCtx(org.openide.util.HelpCtx)
meth public void setOptions(java.lang.Object[])
meth public void setTitleFormat(java.text.MessageFormat)
meth public void setValue(java.lang.Object)
supr org.openide.DialogDescriptor
hfds ASYNCHRONOUS_JOBS_RP,CLOSE_PREVENTER,PROGRESS_BAR_DISPLAY_NAME,addedWindowListener,autoWizardStyle,backgroundValidationTask,baseListener,bundle,cancelButton,changeStateInProgress,contentBackColor,contentData,contentForegroundColor,contentSelectedIndex,currentPanelWasChangedWhileStoreSettings,data,err,escapeActionListener,finishButton,finishOption,handle,helpURL,image,imageAlignment,init,initialized,isWizardWideHelpSet,logged,newObjects,nextButton,previousButton,propListener,properties,titleFormat,validationRuns,waitingComponent,weakCancelButtonListener,weakChangeListener,weakFinishButtonListener,weakNextButtonListener,weakPreviousButtonListener,weakPropertyChangeListener,wizardPanel
hcls BoundedHtmlBrowser,EmptyPanel,FinishAction,FixedHeightLabel,FixedHeightPane,ImagedPanel,Listener,PropL,SettingsAndIterator,WizardPanel,WrappedCellRenderer

CLSS public abstract interface static org.openide.WizardDescriptor$FinishablePanel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$FinishablePanel%0}>
meth public abstract boolean isFinishPanel()

CLSS public abstract interface static org.openide.WizardDescriptor$Panel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean isValid()
meth public abstract java.awt.Component getComponent()
meth public abstract org.openide.util.HelpCtx getHelp()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void readSettings({org.openide.WizardDescriptor$Panel%0})
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void storeSettings({org.openide.WizardDescriptor$Panel%0})

CLSS public abstract interface static org.openide.WizardDescriptor$ValidatingPanel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$ValidatingPanel%0}>
meth public abstract void validate() throws org.openide.WizardValidationException

CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileRenamed(org.openide.filesystems.FileRenameEvent)

CLSS public org.openide.nodes.AbstractNode
cons public init(org.openide.nodes.Children)
cons public init(org.openide.nodes.Children,org.openide.util.Lookup)
fld protected java.text.MessageFormat displayFormat
fld protected org.openide.util.actions.SystemAction[] systemActions
 anno 0 java.lang.Deprecated()
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final org.openide.nodes.Sheet getSheet()
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth protected final void setSheet(org.openide.nodes.Sheet)
meth protected org.openide.nodes.Sheet createSheet()
meth protected org.openide.util.actions.SystemAction[] createActions()
 anno 0 java.lang.Deprecated()
meth protected void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public boolean hasCustomizer()
meth public final org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public final void setIconBaseWithExtension(java.lang.String)
meth public java.awt.Component getCustomizer()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public javax.swing.Action getPreferredAction()
meth public org.openide.nodes.Node cloneNode()
meth public org.openide.nodes.Node$Handle getHandle()
meth public org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public void setDefaultAction(org.openide.util.actions.SystemAction)
 anno 0 java.lang.Deprecated()
meth public void setIconBase(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
supr org.openide.nodes.Node
hfds DEFAULT_ICON,DEFAULT_ICON_BASE,DEFAULT_ICON_EXTENSION,ICON_BASE,NO_NEW_TYPES,NO_PASTE_TYPES,OPENED_ICON_BASE,iconBase,iconExtension,icons,lookup,overridesGetDefaultAction,preferredAction,sheet,sheetCookieL
hcls SheetAndCookieListener

CLSS public abstract org.openide.nodes.Node
cons protected init(org.openide.nodes.Children)
cons protected init(org.openide.nodes.Children,org.openide.util.Lookup)
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_ICON = "icon"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_OPENED_ICON = "openedIcon"
fld public final static java.lang.String PROP_PARENT_NODE = "parentNode"
fld public final static java.lang.String PROP_PROPERTY_SETS = "propertySets"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
fld public final static org.openide.nodes.Node EMPTY
innr public abstract interface static Cookie
innr public abstract interface static Handle
innr public abstract static IndexedProperty
innr public abstract static Property
innr public abstract static PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean hasPropertyChangeListener()
meth protected final void fireCookieChange()
meth protected final void fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void fireIconChange()
meth protected final void fireNameChange(java.lang.String,java.lang.String)
meth protected final void fireNodeDestroyed()
meth protected final void fireOpenedIconChange()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void firePropertySetsChange(org.openide.nodes.Node$PropertySet[],org.openide.nodes.Node$PropertySet[])
meth protected final void fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void setChildren(org.openide.nodes.Children)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean canCopy()
meth public abstract boolean canCut()
meth public abstract boolean canDestroy()
meth public abstract boolean canRename()
meth public abstract boolean hasCustomizer()
meth public abstract java.awt.Component getCustomizer()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.awt.Image getOpenedIcon(int)
meth public abstract java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public abstract org.openide.nodes.Node cloneNode()
meth public abstract org.openide.nodes.Node$Handle getHandle()
meth public abstract org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract org.openide.util.datatransfer.NewType[] getNewTypes()
meth public abstract org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public boolean equals(java.lang.Object)
meth public final boolean isLeaf()
meth public final javax.swing.JPopupMenu getContextMenu()
meth public final org.openide.nodes.Children getChildren()
meth public final org.openide.nodes.Node getParentNode()
meth public final org.openide.util.Lookup getLookup()
meth public final void addNodeListener(org.openide.nodes.NodeListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeNodeListener(org.openide.nodes.NodeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int hashCode()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String toString()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setHidden(boolean)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
hfds BLOCK_EVENTS,INIT_LOCK,LOCK,TEMPL_COOKIE,err,hierarchy,listeners,lookups,parent,warnedBadProperties
hcls LookupEventList,PropertyEditorRef

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

