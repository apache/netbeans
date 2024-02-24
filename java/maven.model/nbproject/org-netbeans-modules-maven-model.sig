#Signature file v4.1
#Version 1.67

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface javax.swing.event.UndoableEditListener
intf java.util.EventListener
meth public abstract void undoableEditHappened(javax.swing.event.UndoableEditEvent)

CLSS public abstract interface org.netbeans.modules.maven.model.ModelOperation<%0 extends org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<? extends org.netbeans.modules.xml.xam.dom.DocumentComponent<?>>>
meth public abstract void performOperation({org.netbeans.modules.maven.model.ModelOperation%0})

CLSS public org.netbeans.modules.maven.model.Utilities
meth public static org.netbeans.modules.xml.xam.ModelSource createModelSource(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.xml.xam.ModelSource createModelSource(org.openide.filesystems.FileObject,javax.swing.text.Document)
meth public static org.netbeans.modules.xml.xam.ModelSource createModelSource(org.openide.filesystems.FileObject,org.openide.loaders.DataObject,org.netbeans.editor.BaseDocument)
meth public static org.netbeans.modules.xml.xam.ModelSource createModelSourceForMissingFile(java.io.File,boolean,java.lang.String,java.lang.String)
meth public static void openAtPosition(org.netbeans.modules.maven.model.pom.POMModel,int)
meth public static void performPOMModelOperations(org.netbeans.modules.xml.xam.ModelSource,java.util.List<? extends org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.pom.POMModel>>)
meth public static void performPOMModelOperations(org.openide.filesystems.FileObject,java.util.List<? extends org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.pom.POMModel>>)
meth public static void performSettingsModelOperations(org.openide.filesystems.FileObject,java.util.List<? extends org.netbeans.modules.maven.model.ModelOperation<org.netbeans.modules.maven.model.settings.SettingsModel>>)
meth public static void saveChanges(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<?>) throws java.io.IOException
supr java.lang.Object
hfds logger

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Activation
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract org.netbeans.modules.maven.model.pom.ActivationCustom getActivationCustom()
meth public abstract org.netbeans.modules.maven.model.pom.ActivationFile getActivationFile()
meth public abstract org.netbeans.modules.maven.model.pom.ActivationOS getActivationOS()
meth public abstract org.netbeans.modules.maven.model.pom.ActivationProperty getActivationProperty()
meth public abstract void setActivationCustom(org.netbeans.modules.maven.model.pom.ActivationCustom)
meth public abstract void setActivationFile(org.netbeans.modules.maven.model.pom.ActivationFile)
meth public abstract void setActivationOS(org.netbeans.modules.maven.model.pom.ActivationOS)
meth public abstract void setActivationProperty(org.netbeans.modules.maven.model.pom.ActivationProperty)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.ActivationCustom
intf org.netbeans.modules.maven.model.pom.POMComponent

CLSS public abstract interface org.netbeans.modules.maven.model.pom.ActivationFile
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getExists()
meth public abstract java.lang.String getMissing()
meth public abstract void setExists(java.lang.String)
meth public abstract void setMissing(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.ActivationOS
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getArch()
meth public abstract java.lang.String getFamily()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getVersion()
meth public abstract void setArch(java.lang.String)
meth public abstract void setFamily(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setVersion(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.ActivationProperty
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Build
intf org.netbeans.modules.maven.model.pom.BuildBase
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getOutputDirectory()
meth public abstract java.lang.String getScriptSourceDirectory()
meth public abstract java.lang.String getSourceDirectory()
meth public abstract java.lang.String getTestOutputDirectory()
meth public abstract java.lang.String getTestSourceDirectory()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Extension> getExtensions()
meth public abstract void addExtension(org.netbeans.modules.maven.model.pom.Extension)
meth public abstract void removeExtension(org.netbeans.modules.maven.model.pom.Extension)
meth public abstract void setOutputDirectory(java.lang.String)
meth public abstract void setScriptSourceDirectory(java.lang.String)
meth public abstract void setSourceDirectory(java.lang.String)
meth public abstract void setTestOutputDirectory(java.lang.String)
meth public abstract void setTestSourceDirectory(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.BuildBase
intf org.netbeans.modules.maven.model.pom.PluginContainer
meth public abstract java.lang.String getDefaultGoal()
meth public abstract java.lang.String getDirectory()
meth public abstract java.lang.String getFinalName()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Resource> getResources()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Resource> getTestResources()
meth public abstract org.netbeans.modules.maven.model.pom.PluginManagement getPluginManagement()
meth public abstract void addResource(org.netbeans.modules.maven.model.pom.Resource)
meth public abstract void addTestResource(org.netbeans.modules.maven.model.pom.Resource)
meth public abstract void removeResource(org.netbeans.modules.maven.model.pom.Resource)
meth public abstract void removeTestResource(org.netbeans.modules.maven.model.pom.Resource)
meth public abstract void setDefaultGoal(java.lang.String)
meth public abstract void setDirectory(java.lang.String)
meth public abstract void setFinalName(java.lang.String)
meth public abstract void setPluginManagement(org.netbeans.modules.maven.model.pom.PluginManagement)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.CiManagement
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getSystem()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Notifier> getNotifiers()
meth public abstract void addNotifier(org.netbeans.modules.maven.model.pom.Notifier)
meth public abstract void removeNotifier(org.netbeans.modules.maven.model.pom.Notifier)
meth public abstract void setSystem(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Configuration
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getSimpleParameter(java.lang.String)
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.POMExtensibilityElement> getConfigurationElements()
meth public abstract void setSimpleParameter(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Contributor
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getEmail()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOrganization()
meth public abstract java.lang.String getOrganizationUrl()
meth public abstract java.lang.String getTimezone()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.List<java.lang.String> getRoles()
meth public abstract void addRole(java.lang.String)
meth public abstract void removeRole(java.lang.String)
meth public abstract void setEmail(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setOrganization(java.lang.String)
meth public abstract void setOrganizationUrl(java.lang.String)
meth public abstract void setTimezone(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Dependency
intf org.netbeans.modules.maven.model.pom.VersionablePOMComponent
meth public abstract java.lang.Boolean isOptional()
meth public abstract java.lang.String getClassifier()
meth public abstract java.lang.String getScope()
meth public abstract java.lang.String getSystemPath()
meth public abstract java.lang.String getType()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Exclusion> getExclusions()
meth public abstract org.netbeans.modules.maven.model.pom.Exclusion findExclusionById(java.lang.String,java.lang.String)
meth public abstract void addExclusion(org.netbeans.modules.maven.model.pom.Exclusion)
meth public abstract void removeExclusion(org.netbeans.modules.maven.model.pom.Exclusion)
meth public abstract void setClassifier(java.lang.String)
meth public abstract void setOptional(java.lang.Boolean)
meth public abstract void setScope(java.lang.String)
meth public abstract void setSystemPath(java.lang.String)
meth public abstract void setType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.DependencyContainer
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Dependency> getDependencies()
meth public abstract org.netbeans.modules.maven.model.pom.Dependency findDependencyById(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void addDependency(org.netbeans.modules.maven.model.pom.Dependency)
meth public abstract void removeDependency(org.netbeans.modules.maven.model.pom.Dependency)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.DependencyManagement
intf org.netbeans.modules.maven.model.pom.DependencyContainer
intf org.netbeans.modules.maven.model.pom.POMComponent

CLSS public abstract interface org.netbeans.modules.maven.model.pom.DeploymentRepository
intf org.netbeans.modules.maven.model.pom.IdPOMComponent
meth public abstract java.lang.String getLayout()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract void setLayout(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Developer
intf org.netbeans.modules.maven.model.pom.IdPOMComponent
meth public abstract java.lang.String getEmail()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOrganization()
meth public abstract java.lang.String getOrganizationUrl()
meth public abstract java.lang.String getTimezone()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.List<java.lang.String> getRoles()
meth public abstract void addRole(java.lang.String)
meth public abstract void removeRole(java.lang.String)
meth public abstract void setEmail(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setOrganization(java.lang.String)
meth public abstract void setOrganizationUrl(java.lang.String)
meth public abstract void setTimezone(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.DistributionManagement
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getDownloadUrl()
meth public abstract org.netbeans.modules.maven.model.pom.DeploymentRepository getRepository()
meth public abstract org.netbeans.modules.maven.model.pom.DeploymentRepository getSnapshotRepository()
meth public abstract org.netbeans.modules.maven.model.pom.Site getSite()
meth public abstract void setDownloadUrl(java.lang.String)
meth public abstract void setRepository(org.netbeans.modules.maven.model.pom.DeploymentRepository)
meth public abstract void setSite(org.netbeans.modules.maven.model.pom.Site)
meth public abstract void setSnapshotRepository(org.netbeans.modules.maven.model.pom.DeploymentRepository)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Exclusion
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getArtifactId()
meth public abstract java.lang.String getGroupId()
meth public abstract void setArtifactId(java.lang.String)
meth public abstract void setGroupId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Extension
intf org.netbeans.modules.maven.model.pom.VersionablePOMComponent

CLSS public abstract interface org.netbeans.modules.maven.model.pom.IdPOMComponent
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getId()
meth public abstract void setId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.IssueManagement
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getSystem()
meth public abstract java.lang.String getUrl()
meth public abstract void setSystem(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.License
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getComments()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract void setComments(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.MailingList
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getArchive()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPost()
meth public abstract java.lang.String getSubscribe()
meth public abstract java.lang.String getUnsubscribe()
meth public abstract void setArchive(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setPost(java.lang.String)
meth public abstract void setSubscribe(java.lang.String)
meth public abstract void setUnsubscribe(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.ModelList<%0 extends org.netbeans.modules.maven.model.pom.POMComponent>
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.Class<{org.netbeans.modules.maven.model.pom.ModelList%0}> getListClass()
meth public abstract java.util.List<{org.netbeans.modules.maven.model.pom.ModelList%0}> getListChildren()
meth public abstract void addListChild({org.netbeans.modules.maven.model.pom.ModelList%0})
meth public abstract void removeListChild({org.netbeans.modules.maven.model.pom.ModelList%0})

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Notifier
intf org.netbeans.modules.maven.model.pom.POMComponent

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Organization
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract void setName(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.POMComponent
fld public final static java.lang.String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement"
intf org.netbeans.modules.xml.xam.dom.DocumentComponent2<org.netbeans.modules.maven.model.pom.POMComponent>
meth public abstract <%0 extends org.netbeans.modules.maven.model.pom.POMExtensibilityElement> java.util.List<{%%0}> getExtensibilityElements(java.lang.Class<{%%0}>)
meth public abstract int findChildElementPosition(javax.xml.namespace.QName)
meth public abstract java.lang.String getChildElementText(javax.xml.namespace.QName)
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.POMExtensibilityElement> getExtensibilityElements()
meth public abstract org.netbeans.modules.maven.model.pom.POMModel getModel()
meth public abstract void accept(org.netbeans.modules.maven.model.pom.POMComponentVisitor)
meth public abstract void addExtensibilityElement(org.netbeans.modules.maven.model.pom.POMExtensibilityElement)
meth public abstract void removeExtensibilityElement(org.netbeans.modules.maven.model.pom.POMExtensibilityElement)
meth public abstract void setChildElementText(java.lang.String,java.lang.String,javax.xml.namespace.QName)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.POMComponentFactory
intf org.netbeans.modules.xml.xam.dom.ComponentFactory<org.netbeans.modules.maven.model.pom.POMComponent>
meth public abstract org.netbeans.modules.maven.model.pom.Activation createActivation()
meth public abstract org.netbeans.modules.maven.model.pom.ActivationCustom createActivationCustom()
meth public abstract org.netbeans.modules.maven.model.pom.ActivationFile createActivationFile()
meth public abstract org.netbeans.modules.maven.model.pom.ActivationOS createActivationOS()
meth public abstract org.netbeans.modules.maven.model.pom.ActivationProperty createActivationProperty()
meth public abstract org.netbeans.modules.maven.model.pom.Build createBuild()
meth public abstract org.netbeans.modules.maven.model.pom.BuildBase createBuildBase()
meth public abstract org.netbeans.modules.maven.model.pom.CiManagement createCiManagement()
meth public abstract org.netbeans.modules.maven.model.pom.Configuration createConfiguration()
meth public abstract org.netbeans.modules.maven.model.pom.Contributor createContributor()
meth public abstract org.netbeans.modules.maven.model.pom.Dependency createDependency()
meth public abstract org.netbeans.modules.maven.model.pom.DependencyManagement createDependencyManagement()
meth public abstract org.netbeans.modules.maven.model.pom.DeploymentRepository createDistRepository()
meth public abstract org.netbeans.modules.maven.model.pom.DeploymentRepository createDistSnapshotRepository()
meth public abstract org.netbeans.modules.maven.model.pom.Developer createDeveloper()
meth public abstract org.netbeans.modules.maven.model.pom.DistributionManagement createDistributionManagement()
meth public abstract org.netbeans.modules.maven.model.pom.Exclusion createExclusion()
meth public abstract org.netbeans.modules.maven.model.pom.Extension createExtension()
meth public abstract org.netbeans.modules.maven.model.pom.IssueManagement createIssueManagement()
meth public abstract org.netbeans.modules.maven.model.pom.License createLicense()
meth public abstract org.netbeans.modules.maven.model.pom.MailingList createMailingList()
meth public abstract org.netbeans.modules.maven.model.pom.Notifier createNotifier()
meth public abstract org.netbeans.modules.maven.model.pom.Organization createOrganization()
meth public abstract org.netbeans.modules.maven.model.pom.POMComponent create(org.netbeans.modules.maven.model.pom.POMComponent,javax.xml.namespace.QName)
meth public abstract org.netbeans.modules.maven.model.pom.POMExtensibilityElement createPOMExtensibilityElement(javax.xml.namespace.QName)
meth public abstract org.netbeans.modules.maven.model.pom.Parent createParent()
meth public abstract org.netbeans.modules.maven.model.pom.Plugin createPlugin()
meth public abstract org.netbeans.modules.maven.model.pom.PluginExecution createExecution()
meth public abstract org.netbeans.modules.maven.model.pom.PluginManagement createPluginManagement()
meth public abstract org.netbeans.modules.maven.model.pom.Prerequisites createPrerequisites()
meth public abstract org.netbeans.modules.maven.model.pom.Profile createProfile()
meth public abstract org.netbeans.modules.maven.model.pom.Project createProject()
meth public abstract org.netbeans.modules.maven.model.pom.Properties createProperties()
meth public abstract org.netbeans.modules.maven.model.pom.ReportPlugin createReportPlugin()
meth public abstract org.netbeans.modules.maven.model.pom.ReportSet createReportSet()
meth public abstract org.netbeans.modules.maven.model.pom.Reporting createReporting()
meth public abstract org.netbeans.modules.maven.model.pom.Repository createPluginRepository()
meth public abstract org.netbeans.modules.maven.model.pom.Repository createRepository()
meth public abstract org.netbeans.modules.maven.model.pom.RepositoryPolicy createReleaseRepositoryPolicy()
meth public abstract org.netbeans.modules.maven.model.pom.RepositoryPolicy createSnapshotRepositoryPolicy()
meth public abstract org.netbeans.modules.maven.model.pom.Resource createResource()
meth public abstract org.netbeans.modules.maven.model.pom.Resource createTestResource()
meth public abstract org.netbeans.modules.maven.model.pom.Scm createScm()
meth public abstract org.netbeans.modules.maven.model.pom.Site createSite()

CLSS public abstract interface org.netbeans.modules.maven.model.pom.POMComponentVisitor
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Activation)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.ActivationCustom)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.ActivationFile)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.ActivationOS)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.ActivationProperty)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Build)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.BuildBase)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.CiManagement)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Configuration)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Contributor)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Dependency)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.DependencyManagement)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.DeploymentRepository)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Developer)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.DistributionManagement)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Exclusion)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Extension)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.IssueManagement)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.License)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.MailingList)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.ModelList)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Notifier)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Organization)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.POMExtensibilityElement)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Parent)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Plugin)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.PluginExecution)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.PluginManagement)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Prerequisites)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Profile)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Project)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Properties)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.ReportPlugin)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.ReportSet)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Reporting)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Repository)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.RepositoryPolicy)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Resource)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Scm)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.Site)
meth public abstract void visit(org.netbeans.modules.maven.model.pom.StringList)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.POMExtensibilityElement
fld public final static java.lang.String CONTENT_FRAGMENT_PROPERTY = "content"
innr public abstract interface static EmbeddedModel
innr public abstract interface static UpdaterProvider
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getContentFragment()
meth public abstract java.lang.String getElementText()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.POMExtensibilityElement> getAnyElements()
meth public abstract javax.xml.namespace.QName getQName()
meth public abstract void addAnyElement(org.netbeans.modules.maven.model.pom.POMExtensibilityElement,int)
meth public abstract void removeAnyElement(org.netbeans.modules.maven.model.pom.POMExtensibilityElement)
meth public abstract void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.String)
meth public abstract void setContentFragment(java.lang.String) throws java.io.IOException
meth public abstract void setElementText(java.lang.String)

CLSS public abstract interface static org.netbeans.modules.maven.model.pom.POMExtensibilityElement$EmbeddedModel
 outer org.netbeans.modules.maven.model.pom.POMExtensibilityElement
intf org.netbeans.modules.maven.model.pom.POMExtensibilityElement
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentModel getEmbeddedModel()

CLSS public abstract interface static org.netbeans.modules.maven.model.pom.POMExtensibilityElement$UpdaterProvider
 outer org.netbeans.modules.maven.model.pom.POMExtensibilityElement
intf org.netbeans.modules.maven.model.pom.POMExtensibilityElement
meth public abstract <%0 extends org.netbeans.modules.maven.model.pom.POMExtensibilityElement> org.netbeans.modules.xml.xam.ComponentUpdater<{%%0}> getComponentUpdater()

CLSS public abstract org.netbeans.modules.maven.model.pom.POMModel
cons protected init(org.netbeans.modules.xml.xam.ModelSource)
meth public <%0 extends java.lang.Object> {%%0} findComponent(int,java.lang.Class<{%%0}>,boolean)
meth public abstract org.netbeans.modules.maven.model.pom.POMComponentFactory getFactory()
meth public abstract org.netbeans.modules.maven.model.pom.POMQNames getPOMQNames()
meth public abstract org.netbeans.modules.maven.model.pom.Project getProject()
meth public void refresh()
supr org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<org.netbeans.modules.maven.model.pom.POMComponent>

CLSS public org.netbeans.modules.maven.model.pom.POMModelFactory
meth protected org.netbeans.modules.maven.model.pom.POMModel createModel(org.netbeans.modules.xml.xam.ModelSource)
meth public org.netbeans.modules.maven.model.pom.POMModel getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public static org.netbeans.modules.maven.model.pom.POMModelFactory getDefault()
supr org.netbeans.modules.xml.xam.AbstractModelFactory<org.netbeans.modules.maven.model.pom.POMModel>
hfds modelFactory

CLSS public final org.netbeans.modules.maven.model.pom.POMQName
fld public final static java.lang.String NS_PREFIX = "pom"
fld public final static java.lang.String NS_URI = "http://maven.apache.org/POM/4.0.0"
meth public java.lang.String getName()
meth public javax.xml.namespace.QName getQName()
meth public static javax.xml.namespace.QName createQName(java.lang.String)
meth public static javax.xml.namespace.QName createQName(java.lang.String,boolean)
supr java.lang.Object
hfds qName

CLSS public final org.netbeans.modules.maven.model.pom.POMQNames
cons public init(boolean)
fld public final org.netbeans.modules.maven.model.pom.POMQName ACTIVATION
fld public final org.netbeans.modules.maven.model.pom.POMQName ACTIVATIONCUSTOM
fld public final org.netbeans.modules.maven.model.pom.POMQName ACTIVATIONFILE
fld public final org.netbeans.modules.maven.model.pom.POMQName ACTIVATIONOS
fld public final org.netbeans.modules.maven.model.pom.POMQName ACTIVATIONPROPERTY
fld public final org.netbeans.modules.maven.model.pom.POMQName ARCH
fld public final org.netbeans.modules.maven.model.pom.POMQName ARCHIVE
fld public final org.netbeans.modules.maven.model.pom.POMQName ARTIFACTID
fld public final org.netbeans.modules.maven.model.pom.POMQName BUILD
fld public final org.netbeans.modules.maven.model.pom.POMQName CHECKSUMPOLICY
fld public final org.netbeans.modules.maven.model.pom.POMQName CIMANAGEMENT
fld public final org.netbeans.modules.maven.model.pom.POMQName CIMANAG_SYSTEM
fld public final org.netbeans.modules.maven.model.pom.POMQName CLASSIFIER
fld public final org.netbeans.modules.maven.model.pom.POMQName COMMENTS
fld public final org.netbeans.modules.maven.model.pom.POMQName CONFIGURATION
fld public final org.netbeans.modules.maven.model.pom.POMQName CONNECTION
fld public final org.netbeans.modules.maven.model.pom.POMQName CONTRIBUTOR
fld public final org.netbeans.modules.maven.model.pom.POMQName CONTRIBUTORS
fld public final org.netbeans.modules.maven.model.pom.POMQName DEFAULTGOAL
fld public final org.netbeans.modules.maven.model.pom.POMQName DEPENDENCIES
fld public final org.netbeans.modules.maven.model.pom.POMQName DEPENDENCY
fld public final org.netbeans.modules.maven.model.pom.POMQName DEPENDENCYMANAGEMENT
fld public final org.netbeans.modules.maven.model.pom.POMQName DESCRIPTION
fld public final org.netbeans.modules.maven.model.pom.POMQName DEVELOPER
fld public final org.netbeans.modules.maven.model.pom.POMQName DEVELOPERCONNECTION
fld public final org.netbeans.modules.maven.model.pom.POMQName DEVELOPERS
fld public final org.netbeans.modules.maven.model.pom.POMQName DIRECTORY
fld public final org.netbeans.modules.maven.model.pom.POMQName DISTRIBUTIONMANAGEMENT
fld public final org.netbeans.modules.maven.model.pom.POMQName DIST_REPOSITORY
fld public final org.netbeans.modules.maven.model.pom.POMQName DIST_SNAPSHOTREPOSITORY
fld public final org.netbeans.modules.maven.model.pom.POMQName DOWNLOADURL
fld public final org.netbeans.modules.maven.model.pom.POMQName EMAIL
fld public final org.netbeans.modules.maven.model.pom.POMQName ENABLED
fld public final org.netbeans.modules.maven.model.pom.POMQName EXCLUDE
fld public final org.netbeans.modules.maven.model.pom.POMQName EXCLUDEDEFAULTS
fld public final org.netbeans.modules.maven.model.pom.POMQName EXCLUDES
fld public final org.netbeans.modules.maven.model.pom.POMQName EXCLUSION
fld public final org.netbeans.modules.maven.model.pom.POMQName EXCLUSIONS
fld public final org.netbeans.modules.maven.model.pom.POMQName EXECUTION
fld public final org.netbeans.modules.maven.model.pom.POMQName EXECUTIONS
fld public final org.netbeans.modules.maven.model.pom.POMQName EXISTS
fld public final org.netbeans.modules.maven.model.pom.POMQName EXTENSION
fld public final org.netbeans.modules.maven.model.pom.POMQName EXTENSIONS
fld public final org.netbeans.modules.maven.model.pom.POMQName FAMILY
fld public final org.netbeans.modules.maven.model.pom.POMQName FILTERING
fld public final org.netbeans.modules.maven.model.pom.POMQName FINALNAME
fld public final org.netbeans.modules.maven.model.pom.POMQName GOAL
fld public final org.netbeans.modules.maven.model.pom.POMQName GOALS
fld public final org.netbeans.modules.maven.model.pom.POMQName GROUPID
fld public final org.netbeans.modules.maven.model.pom.POMQName ID
fld public final org.netbeans.modules.maven.model.pom.POMQName INCEPTIONYEAR
fld public final org.netbeans.modules.maven.model.pom.POMQName INCLUDE
fld public final org.netbeans.modules.maven.model.pom.POMQName INCLUDES
fld public final org.netbeans.modules.maven.model.pom.POMQName INHERITED
fld public final org.netbeans.modules.maven.model.pom.POMQName ISSUEMANAGEMENT
fld public final org.netbeans.modules.maven.model.pom.POMQName LAYOUT
fld public final org.netbeans.modules.maven.model.pom.POMQName LICENSE
fld public final org.netbeans.modules.maven.model.pom.POMQName LICENSES
fld public final org.netbeans.modules.maven.model.pom.POMQName MAILINGLIST
fld public final org.netbeans.modules.maven.model.pom.POMQName MAILINGLISTS
fld public final org.netbeans.modules.maven.model.pom.POMQName MAVEN
fld public final org.netbeans.modules.maven.model.pom.POMQName MISSING
fld public final org.netbeans.modules.maven.model.pom.POMQName MODELVERSION
fld public final org.netbeans.modules.maven.model.pom.POMQName MODULE
fld public final org.netbeans.modules.maven.model.pom.POMQName MODULES
fld public final org.netbeans.modules.maven.model.pom.POMQName NAME
fld public final org.netbeans.modules.maven.model.pom.POMQName NOTIFIER
fld public final org.netbeans.modules.maven.model.pom.POMQName OPTIONAL
fld public final org.netbeans.modules.maven.model.pom.POMQName ORGANIZATION
fld public final org.netbeans.modules.maven.model.pom.POMQName ORGANIZATIONURL
fld public final org.netbeans.modules.maven.model.pom.POMQName OUTPUTDIRECTORY
fld public final org.netbeans.modules.maven.model.pom.POMQName PACKAGING
fld public final org.netbeans.modules.maven.model.pom.POMQName PARENT
fld public final org.netbeans.modules.maven.model.pom.POMQName PHASE
fld public final org.netbeans.modules.maven.model.pom.POMQName PLUGIN
fld public final org.netbeans.modules.maven.model.pom.POMQName PLUGINMANAGEMENT
fld public final org.netbeans.modules.maven.model.pom.POMQName PLUGINREPOSITORIES
fld public final org.netbeans.modules.maven.model.pom.POMQName PLUGINREPOSITORY
fld public final org.netbeans.modules.maven.model.pom.POMQName PLUGINS
fld public final org.netbeans.modules.maven.model.pom.POMQName POST
fld public final org.netbeans.modules.maven.model.pom.POMQName PREREQUISITES
fld public final org.netbeans.modules.maven.model.pom.POMQName PROFILE
fld public final org.netbeans.modules.maven.model.pom.POMQName PROFILES
fld public final org.netbeans.modules.maven.model.pom.POMQName PROJECT
fld public final org.netbeans.modules.maven.model.pom.POMQName PROPERTIES
fld public final org.netbeans.modules.maven.model.pom.POMQName RELATIVEPATH
fld public final org.netbeans.modules.maven.model.pom.POMQName RELEASES
fld public final org.netbeans.modules.maven.model.pom.POMQName REPORT
fld public final org.netbeans.modules.maven.model.pom.POMQName REPORTING
fld public final org.netbeans.modules.maven.model.pom.POMQName REPORTPLUGIN
fld public final org.netbeans.modules.maven.model.pom.POMQName REPORTPLUGINS
fld public final org.netbeans.modules.maven.model.pom.POMQName REPORTS
fld public final org.netbeans.modules.maven.model.pom.POMQName REPORTSET
fld public final org.netbeans.modules.maven.model.pom.POMQName REPORTSETS
fld public final org.netbeans.modules.maven.model.pom.POMQName REPOSITORIES
fld public final org.netbeans.modules.maven.model.pom.POMQName REPOSITORY
fld public final org.netbeans.modules.maven.model.pom.POMQName RESOURCE
fld public final org.netbeans.modules.maven.model.pom.POMQName RESOURCES
fld public final org.netbeans.modules.maven.model.pom.POMQName ROLE
fld public final org.netbeans.modules.maven.model.pom.POMQName ROLES
fld public final org.netbeans.modules.maven.model.pom.POMQName SCM
fld public final org.netbeans.modules.maven.model.pom.POMQName SCOPE
fld public final org.netbeans.modules.maven.model.pom.POMQName SCRIPTSOURCEDIRECTORY
fld public final org.netbeans.modules.maven.model.pom.POMQName SITE
fld public final org.netbeans.modules.maven.model.pom.POMQName SNAPSHOTS
fld public final org.netbeans.modules.maven.model.pom.POMQName SOURCEDIRECTORY
fld public final org.netbeans.modules.maven.model.pom.POMQName SUBSCRIBE
fld public final org.netbeans.modules.maven.model.pom.POMQName SYSTEM
fld public final org.netbeans.modules.maven.model.pom.POMQName SYSTEMPATH
fld public final org.netbeans.modules.maven.model.pom.POMQName TAG
fld public final org.netbeans.modules.maven.model.pom.POMQName TARGETPATH
fld public final org.netbeans.modules.maven.model.pom.POMQName TESTOUTPUTDIRECTORY
fld public final org.netbeans.modules.maven.model.pom.POMQName TESTRESOURCE
fld public final org.netbeans.modules.maven.model.pom.POMQName TESTRESOURCES
fld public final org.netbeans.modules.maven.model.pom.POMQName TESTSOURCEDIRECTORY
fld public final org.netbeans.modules.maven.model.pom.POMQName TIMEZONE
fld public final org.netbeans.modules.maven.model.pom.POMQName TYPE
fld public final org.netbeans.modules.maven.model.pom.POMQName UNSUBSCRIBE
fld public final org.netbeans.modules.maven.model.pom.POMQName UPDATEPOLICY
fld public final org.netbeans.modules.maven.model.pom.POMQName URL
fld public final org.netbeans.modules.maven.model.pom.POMQName VALUE
fld public final org.netbeans.modules.maven.model.pom.POMQName VERSION
meth public boolean isNSAware()
meth public java.util.Set<javax.xml.namespace.QName> getElementQNames()
supr java.lang.Object
hfds ns

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Parent
intf org.netbeans.modules.maven.model.pom.VersionablePOMComponent
meth public abstract java.lang.String getRelativePath()
meth public abstract void setRelativePath(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Plugin
intf org.netbeans.modules.maven.model.pom.DependencyContainer
intf org.netbeans.modules.maven.model.pom.VersionablePOMComponent
meth public abstract java.lang.Boolean isExtensions()
meth public abstract java.lang.Boolean isInherited()
meth public abstract java.util.List<java.lang.String> getGoals()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.PluginExecution> getExecutions()
meth public abstract org.netbeans.modules.maven.model.pom.Configuration getConfiguration()
meth public abstract org.netbeans.modules.maven.model.pom.PluginExecution findExecutionById(java.lang.String)
meth public abstract void addExecution(org.netbeans.modules.maven.model.pom.PluginExecution)
meth public abstract void addGoal(java.lang.String)
meth public abstract void removeExecution(org.netbeans.modules.maven.model.pom.PluginExecution)
meth public abstract void removeGoal(java.lang.String)
meth public abstract void setConfiguration(org.netbeans.modules.maven.model.pom.Configuration)
meth public abstract void setExtensions(java.lang.Boolean)
meth public abstract void setInherited(java.lang.Boolean)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.PluginContainer
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Plugin> getPlugins()
meth public abstract org.netbeans.modules.maven.model.pom.Plugin findPluginById(java.lang.String,java.lang.String)
meth public abstract void addPlugin(org.netbeans.modules.maven.model.pom.Plugin)
meth public abstract void removePlugin(org.netbeans.modules.maven.model.pom.Plugin)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.PluginExecution
intf org.netbeans.modules.maven.model.pom.IdPOMComponent
meth public abstract java.lang.Boolean isInherited()
meth public abstract java.lang.String getPhase()
meth public abstract java.util.List<java.lang.String> getGoals()
meth public abstract org.netbeans.modules.maven.model.pom.Configuration getConfiguration()
meth public abstract void addGoal(java.lang.String)
meth public abstract void removeGoal(java.lang.String)
meth public abstract void setConfiguration(org.netbeans.modules.maven.model.pom.Configuration)
meth public abstract void setInherited(java.lang.Boolean)
meth public abstract void setPhase(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.PluginManagement
intf org.netbeans.modules.maven.model.pom.PluginContainer

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Prerequisites
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getMaven()
meth public abstract void setMaven(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Profile
intf org.netbeans.modules.maven.model.pom.DependencyContainer
intf org.netbeans.modules.maven.model.pom.IdPOMComponent
intf org.netbeans.modules.maven.model.pom.RepositoryContainer
meth public abstract java.util.List<java.lang.String> getModules()
meth public abstract org.netbeans.modules.maven.model.pom.Activation getActivation()
meth public abstract org.netbeans.modules.maven.model.pom.BuildBase getBuildBase()
meth public abstract org.netbeans.modules.maven.model.pom.DependencyManagement getDependencyManagement()
meth public abstract org.netbeans.modules.maven.model.pom.DistributionManagement getDistributionManagement()
meth public abstract org.netbeans.modules.maven.model.pom.Properties getProperties()
meth public abstract org.netbeans.modules.maven.model.pom.Reporting getReporting()
meth public abstract void addModule(java.lang.String)
meth public abstract void removeModule(java.lang.String)
meth public abstract void setActivation(org.netbeans.modules.maven.model.pom.Activation)
meth public abstract void setBuildBase(org.netbeans.modules.maven.model.pom.BuildBase)
meth public abstract void setDependencyManagement(org.netbeans.modules.maven.model.pom.DependencyManagement)
meth public abstract void setDistributionManagement(org.netbeans.modules.maven.model.pom.DistributionManagement)
meth public abstract void setProperties(org.netbeans.modules.maven.model.pom.Properties)
meth public abstract void setReporting(org.netbeans.modules.maven.model.pom.Reporting)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Project
intf org.netbeans.modules.maven.model.pom.DependencyContainer
intf org.netbeans.modules.maven.model.pom.RepositoryContainer
intf org.netbeans.modules.maven.model.pom.VersionablePOMComponent
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getInceptionYear()
meth public abstract java.lang.String getModelVersion()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPackaging()
meth public abstract java.lang.String getURL()
meth public abstract java.util.List<java.lang.String> getModules()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Contributor> getContributors()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Developer> getDevelopers()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.License> getLicenses()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.MailingList> getMailingLists()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Profile> getProfiles()
meth public abstract org.netbeans.modules.maven.model.pom.Build getBuild()
meth public abstract org.netbeans.modules.maven.model.pom.CiManagement getCiManagement()
meth public abstract org.netbeans.modules.maven.model.pom.DependencyManagement getDependencyManagement()
meth public abstract org.netbeans.modules.maven.model.pom.DistributionManagement getDistributionManagement()
meth public abstract org.netbeans.modules.maven.model.pom.IssueManagement getIssueManagement()
meth public abstract org.netbeans.modules.maven.model.pom.Organization getOrganization()
meth public abstract org.netbeans.modules.maven.model.pom.Parent getPomParent()
meth public abstract org.netbeans.modules.maven.model.pom.Prerequisites getPrerequisites()
meth public abstract org.netbeans.modules.maven.model.pom.Profile findProfileById(java.lang.String)
meth public abstract org.netbeans.modules.maven.model.pom.Properties getProperties()
meth public abstract org.netbeans.modules.maven.model.pom.Reporting getReporting()
meth public abstract org.netbeans.modules.maven.model.pom.Scm getScm()
meth public abstract void addContributor(org.netbeans.modules.maven.model.pom.Contributor)
meth public abstract void addDeveloper(org.netbeans.modules.maven.model.pom.Developer)
meth public abstract void addLicense(org.netbeans.modules.maven.model.pom.License)
meth public abstract void addMailingList(org.netbeans.modules.maven.model.pom.MailingList)
meth public abstract void addModule(java.lang.String)
meth public abstract void addProfile(org.netbeans.modules.maven.model.pom.Profile)
meth public abstract void removeContributor(org.netbeans.modules.maven.model.pom.Contributor)
meth public abstract void removeDeveloper(org.netbeans.modules.maven.model.pom.Developer)
meth public abstract void removeLicense(org.netbeans.modules.maven.model.pom.License)
meth public abstract void removeMailingList(org.netbeans.modules.maven.model.pom.MailingList)
meth public abstract void removeModule(java.lang.String)
meth public abstract void removeProfile(org.netbeans.modules.maven.model.pom.Profile)
meth public abstract void setBuild(org.netbeans.modules.maven.model.pom.Build)
meth public abstract void setCiManagement(org.netbeans.modules.maven.model.pom.CiManagement)
meth public abstract void setDependencyManagement(org.netbeans.modules.maven.model.pom.DependencyManagement)
meth public abstract void setDescription(java.lang.String)
meth public abstract void setDistributionManagement(org.netbeans.modules.maven.model.pom.DistributionManagement)
meth public abstract void setInceptionYear(java.lang.String)
meth public abstract void setIssueManagement(org.netbeans.modules.maven.model.pom.IssueManagement)
meth public abstract void setName(java.lang.String)
meth public abstract void setOrganization(org.netbeans.modules.maven.model.pom.Organization)
meth public abstract void setPackaging(java.lang.String)
meth public abstract void setPomParent(org.netbeans.modules.maven.model.pom.Parent)
meth public abstract void setPrerequisites(org.netbeans.modules.maven.model.pom.Prerequisites)
meth public abstract void setProperties(org.netbeans.modules.maven.model.pom.Properties)
meth public abstract void setReporting(org.netbeans.modules.maven.model.pom.Reporting)
meth public abstract void setScm(org.netbeans.modules.maven.model.pom.Scm)
meth public abstract void setURL(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Properties
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract void setProperty(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.ReportPlugin
intf org.netbeans.modules.maven.model.pom.VersionablePOMComponent
meth public abstract java.lang.Boolean isInherited()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.ReportSet> getReportSets()
meth public abstract org.netbeans.modules.maven.model.pom.Configuration getConfiguration()
meth public abstract void addReportSet(org.netbeans.modules.maven.model.pom.ReportSet)
meth public abstract void removeReportSet(org.netbeans.modules.maven.model.pom.ReportSet)
meth public abstract void setConfiguration(org.netbeans.modules.maven.model.pom.Configuration)
meth public abstract void setInherited(java.lang.Boolean)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.ReportPluginContainer
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.ReportPlugin> getReportPlugins()
meth public abstract org.netbeans.modules.maven.model.pom.ReportPlugin findReportPluginById(java.lang.String,java.lang.String)
meth public abstract void addReportPlugin(org.netbeans.modules.maven.model.pom.ReportPlugin)
meth public abstract void removeReportPlugin(org.netbeans.modules.maven.model.pom.ReportPlugin)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.ReportSet
intf org.netbeans.modules.maven.model.pom.IdPOMComponent
meth public abstract java.lang.Boolean isInherited()
meth public abstract java.util.List<java.lang.String> getReports()
meth public abstract org.netbeans.modules.maven.model.pom.Configuration getConfiguration()
meth public abstract void addReport(java.lang.String)
meth public abstract void removeReport(java.lang.String)
meth public abstract void setConfiguration(org.netbeans.modules.maven.model.pom.Configuration)
meth public abstract void setInherited(java.lang.Boolean)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Reporting
intf org.netbeans.modules.maven.model.pom.POMComponent
intf org.netbeans.modules.maven.model.pom.ReportPluginContainer
meth public abstract java.lang.Boolean isExcludeDefaults()
meth public abstract java.lang.String getOutputDirectory()
meth public abstract void setExcludeDefaults(java.lang.Boolean)
meth public abstract void setOutputDirectory(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Repository
intf org.netbeans.modules.maven.model.pom.IdPOMComponent
meth public abstract java.lang.String getLayout()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract org.netbeans.modules.maven.model.pom.RepositoryPolicy getReleases()
meth public abstract org.netbeans.modules.maven.model.pom.RepositoryPolicy getSnapshots()
meth public abstract void setLayout(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setReleases(org.netbeans.modules.maven.model.pom.RepositoryPolicy)
meth public abstract void setSnapshots(org.netbeans.modules.maven.model.pom.RepositoryPolicy)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.RepositoryContainer
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Repository> getPluginRepositories()
meth public abstract java.util.List<org.netbeans.modules.maven.model.pom.Repository> getRepositories()
meth public abstract void addPluginRepository(org.netbeans.modules.maven.model.pom.Repository)
meth public abstract void addRepository(org.netbeans.modules.maven.model.pom.Repository)
meth public abstract void removePluginRepository(org.netbeans.modules.maven.model.pom.Repository)
meth public abstract void removeRepository(org.netbeans.modules.maven.model.pom.Repository)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.RepositoryPolicy
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.Boolean isEnabled()
meth public abstract java.lang.String getChecksumPolicy()
meth public abstract java.lang.String getUpdatePolicy()
meth public abstract void setChecksumPolicy(java.lang.String)
meth public abstract void setEnabled(java.lang.Boolean)
meth public abstract void setUpdatePolicy(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Resource
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.Boolean isFiltering()
meth public abstract java.lang.String getDirectory()
meth public abstract java.lang.String getTargetPath()
meth public abstract java.util.List<java.lang.String> getExcludes()
meth public abstract java.util.List<java.lang.String> getIncludes()
meth public abstract void addExclude(java.lang.String)
meth public abstract void addInclude(java.lang.String)
meth public abstract void removeExclude(java.lang.String)
meth public abstract void removeInclude(java.lang.String)
meth public abstract void setDirectory(java.lang.String)
meth public abstract void setFiltering(java.lang.Boolean)
meth public abstract void setTargetPath(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Scm
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getConnection()
meth public abstract java.lang.String getDeveloperConnection()
meth public abstract java.lang.String getTag()
meth public abstract java.lang.String getUrl()
meth public abstract void setConnection(java.lang.String)
meth public abstract void setDeveloperConnection(java.lang.String)
meth public abstract void setTag(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.Site
intf org.netbeans.modules.maven.model.pom.IdPOMComponent
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract void setName(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.StringList
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.util.List<java.lang.String> getListChildren()
meth public abstract void addListChild(java.lang.String)
meth public abstract void removeListChild(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.pom.VersionablePOMComponent
intf org.netbeans.modules.maven.model.pom.POMComponent
meth public abstract java.lang.String getArtifactId()
meth public abstract java.lang.String getGroupId()
meth public abstract java.lang.String getVersion()
meth public abstract void setArtifactId(java.lang.String)
meth public abstract void setGroupId(java.lang.String)
meth public abstract void setVersion(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Activation
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract org.netbeans.modules.maven.model.settings.ActivationCustom getActivationCustom()
meth public abstract org.netbeans.modules.maven.model.settings.ActivationFile getActivationFile()
meth public abstract org.netbeans.modules.maven.model.settings.ActivationOS getActivationOS()
meth public abstract org.netbeans.modules.maven.model.settings.ActivationProperty getActivationProperty()
meth public abstract void setActivationCustom(org.netbeans.modules.maven.model.settings.ActivationCustom)
meth public abstract void setActivationFile(org.netbeans.modules.maven.model.settings.ActivationFile)
meth public abstract void setActivationOS(org.netbeans.modules.maven.model.settings.ActivationOS)
meth public abstract void setActivationProperty(org.netbeans.modules.maven.model.settings.ActivationProperty)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.ActivationCustom
intf org.netbeans.modules.maven.model.settings.SettingsComponent

CLSS public abstract interface org.netbeans.modules.maven.model.settings.ActivationFile
intf org.netbeans.modules.maven.model.settings.SettingsComponent

CLSS public abstract interface org.netbeans.modules.maven.model.settings.ActivationOS
intf org.netbeans.modules.maven.model.settings.SettingsComponent

CLSS public abstract interface org.netbeans.modules.maven.model.settings.ActivationProperty
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Configuration
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.String getSimpleParameter(java.lang.String)
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement> getConfigurationElements()
meth public abstract void setSimpleParameter(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Mirror
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getLayout()
meth public abstract java.lang.String getMirrorOf()
meth public abstract java.lang.String getMirrorOfLayouts()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract void setId(java.lang.String)
meth public abstract void setLayout(java.lang.String)
meth public abstract void setMirrorOf(java.lang.String)
meth public abstract void setMirrorOfLayouts(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.ModelList<%0 extends org.netbeans.modules.maven.model.settings.SettingsComponent>
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.Class<{org.netbeans.modules.maven.model.settings.ModelList%0}> getListClass()
meth public abstract java.util.List<{org.netbeans.modules.maven.model.settings.ModelList%0}> getListChildren()
meth public abstract void addListChild({org.netbeans.modules.maven.model.settings.ModelList%0})
meth public abstract void removeListChild({org.netbeans.modules.maven.model.settings.ModelList%0})

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Profile
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.String getId()
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.Repository> getPluginRepositories()
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.Repository> getRepositories()
meth public abstract org.netbeans.modules.maven.model.settings.Activation getActivation()
meth public abstract org.netbeans.modules.maven.model.settings.Properties getProperties()
meth public abstract void addPluginRepository(org.netbeans.modules.maven.model.settings.Repository)
meth public abstract void addRepository(org.netbeans.modules.maven.model.settings.Repository)
meth public abstract void removePluginRepository(org.netbeans.modules.maven.model.settings.Repository)
meth public abstract void removeRepository(org.netbeans.modules.maven.model.settings.Repository)
meth public abstract void setActivation(org.netbeans.modules.maven.model.settings.Activation)
meth public abstract void setId(java.lang.String)
meth public abstract void setProperties(org.netbeans.modules.maven.model.settings.Properties)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Properties
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract void setProperty(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Proxy
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.Boolean getActive()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getNonProxyHosts()
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getPort()
meth public abstract java.lang.String getProtocol()
meth public abstract java.lang.String getUsername()
meth public abstract void setActive(java.lang.Boolean)
meth public abstract void setHost(java.lang.String)
meth public abstract void setId(java.lang.String)
meth public abstract void setNonProxyHosts(java.lang.String)
meth public abstract void setPassword(java.lang.String)
meth public abstract void setPort(java.lang.String)
meth public abstract void setProtocol(java.lang.String)
meth public abstract void setUsername(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Repository
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getLayout()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract org.netbeans.modules.maven.model.settings.RepositoryPolicy getReleases()
meth public abstract org.netbeans.modules.maven.model.settings.RepositoryPolicy getSnapshots()
meth public abstract void setId(java.lang.String)
meth public abstract void setLayout(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setReleases(org.netbeans.modules.maven.model.settings.RepositoryPolicy)
meth public abstract void setSnapshots(org.netbeans.modules.maven.model.settings.RepositoryPolicy)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.RepositoryPolicy
intf org.netbeans.modules.maven.model.settings.SettingsComponent

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Server
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getPassphrase()
meth public abstract java.lang.String getPrivateKey()
meth public abstract java.lang.String getUsername()
meth public abstract org.netbeans.modules.maven.model.settings.Configuration getConfiguration()
meth public abstract void setConfiguration(org.netbeans.modules.maven.model.settings.Configuration)
meth public abstract void setId(java.lang.String)
meth public abstract void setPassphrase(java.lang.String)
meth public abstract void setPrivateKey(java.lang.String)
meth public abstract void setUsername(java.lang.String)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.Settings
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.Boolean isInteractiveMode()
meth public abstract java.lang.Boolean isOffline()
meth public abstract java.lang.Boolean isUsePluginRegistry()
meth public abstract java.lang.String getLocalRepository()
meth public abstract java.util.List<java.lang.String> getActiveProfiles()
meth public abstract java.util.List<java.lang.String> getPluginGroups()
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.Mirror> getMirrors()
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.Profile> getProfiles()
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.Proxy> getProxies()
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.Server> getServers()
meth public abstract org.netbeans.modules.maven.model.settings.Mirror findMirrorById(java.lang.String)
meth public abstract org.netbeans.modules.maven.model.settings.Profile findProfileById(java.lang.String)
meth public abstract void addActiveProfile(java.lang.String)
meth public abstract void addMirror(org.netbeans.modules.maven.model.settings.Mirror)
meth public abstract void addPluginGroup(java.lang.String)
meth public abstract void addProfile(org.netbeans.modules.maven.model.settings.Profile)
meth public abstract void addProxy(org.netbeans.modules.maven.model.settings.Proxy)
meth public abstract void addServer(org.netbeans.modules.maven.model.settings.Server)
meth public abstract void removeActiveProfile(java.lang.String)
meth public abstract void removeMirror(org.netbeans.modules.maven.model.settings.Mirror)
meth public abstract void removePluginGroup(java.lang.String)
meth public abstract void removeProfile(org.netbeans.modules.maven.model.settings.Profile)
meth public abstract void removeProxy(org.netbeans.modules.maven.model.settings.Proxy)
meth public abstract void removeServer(org.netbeans.modules.maven.model.settings.Server)
meth public abstract void setInteractiveMode(java.lang.Boolean)
meth public abstract void setLocalRepository(java.lang.String)
meth public abstract void setOffline(java.lang.Boolean)
meth public abstract void setUsePluginRegistry(java.lang.Boolean)

CLSS public final !enum org.netbeans.modules.maven.model.settings.SettingsAttribute
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.modules.maven.model.settings.SettingsAttribute NS
intf org.netbeans.modules.xml.xam.dom.Attribute
meth public java.lang.Class getMemberType()
meth public java.lang.Class getType()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public javax.xml.namespace.QName getQName()
meth public static org.netbeans.modules.maven.model.settings.SettingsAttribute valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.model.settings.SettingsAttribute[] values()
supr java.lang.Enum<org.netbeans.modules.maven.model.settings.SettingsAttribute>
hfds name,subtype,type

CLSS public abstract interface org.netbeans.modules.maven.model.settings.SettingsComponent
fld public final static java.lang.String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement"
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<org.netbeans.modules.maven.model.settings.SettingsComponent>
meth public abstract <%0 extends org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement> java.util.List<{%%0}> getExtensibilityElements(java.lang.Class<{%%0}>)
meth public abstract java.lang.String getChildElementText(javax.xml.namespace.QName)
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement> getExtensibilityElements()
meth public abstract org.netbeans.modules.maven.model.settings.SettingsModel getModel()
meth public abstract void accept(org.netbeans.modules.maven.model.settings.SettingsComponentVisitor)
meth public abstract void addExtensibilityElement(org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement)
meth public abstract void removeExtensibilityElement(org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement)
meth public abstract void setChildElementText(java.lang.String,java.lang.String,javax.xml.namespace.QName)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.SettingsComponentFactory
intf org.netbeans.modules.xml.xam.dom.ComponentFactory<org.netbeans.modules.maven.model.settings.SettingsComponent>
meth public abstract org.netbeans.modules.maven.model.settings.Activation createActivation()
meth public abstract org.netbeans.modules.maven.model.settings.ActivationCustom createActivationCustom()
meth public abstract org.netbeans.modules.maven.model.settings.ActivationFile createActivationFile()
meth public abstract org.netbeans.modules.maven.model.settings.ActivationOS createActivationOS()
meth public abstract org.netbeans.modules.maven.model.settings.ActivationProperty createActivationProperty()
meth public abstract org.netbeans.modules.maven.model.settings.Configuration createConfiguration()
meth public abstract org.netbeans.modules.maven.model.settings.Mirror createMirror()
meth public abstract org.netbeans.modules.maven.model.settings.Profile createProfile()
meth public abstract org.netbeans.modules.maven.model.settings.Properties createProperties()
meth public abstract org.netbeans.modules.maven.model.settings.Proxy createProxy()
meth public abstract org.netbeans.modules.maven.model.settings.Repository createPluginRepository()
meth public abstract org.netbeans.modules.maven.model.settings.Repository createRepository()
meth public abstract org.netbeans.modules.maven.model.settings.RepositoryPolicy createReleaseRepositoryPolicy()
meth public abstract org.netbeans.modules.maven.model.settings.RepositoryPolicy createSnapshotRepositoryPolicy()
meth public abstract org.netbeans.modules.maven.model.settings.Server createServer()
meth public abstract org.netbeans.modules.maven.model.settings.Settings createSettings()
meth public abstract org.netbeans.modules.maven.model.settings.SettingsComponent create(org.netbeans.modules.maven.model.settings.SettingsComponent,javax.xml.namespace.QName)
meth public abstract org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement createSettingsExtensibilityElement(javax.xml.namespace.QName)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.SettingsComponentVisitor
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Activation)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.ActivationCustom)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.ActivationFile)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.ActivationOS)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.ActivationProperty)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Configuration)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Mirror)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.ModelList)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Profile)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Properties)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Proxy)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Repository)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.RepositoryPolicy)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Server)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.Settings)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement)
meth public abstract void visit(org.netbeans.modules.maven.model.settings.StringList)

CLSS public abstract interface org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement
fld public final static java.lang.String CONTENT_FRAGMENT_PROPERTY = "content"
innr public abstract interface static EmbeddedModel
innr public abstract interface static UpdaterProvider
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getContentFragment()
meth public abstract java.lang.String getElementText()
meth public abstract java.util.List<org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement> getAnyElements()
meth public abstract javax.xml.namespace.QName getQName()
meth public abstract void addAnyElement(org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement,int)
meth public abstract void removeAnyElement(org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement)
meth public abstract void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.String)
meth public abstract void setContentFragment(java.lang.String) throws java.io.IOException
meth public abstract void setElementText(java.lang.String)

CLSS public abstract interface static org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement$EmbeddedModel
 outer org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement
intf org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentModel getEmbeddedModel()

CLSS public abstract interface static org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement$UpdaterProvider
 outer org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement
intf org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement
meth public abstract <%0 extends org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement> org.netbeans.modules.xml.xam.ComponentUpdater<{%%0}> getComponentUpdater()

CLSS public abstract org.netbeans.modules.maven.model.settings.SettingsModel
cons protected init(org.netbeans.modules.xml.xam.ModelSource)
meth public abstract org.netbeans.modules.maven.model.settings.Settings getSettings()
meth public abstract org.netbeans.modules.maven.model.settings.SettingsComponentFactory getFactory()
meth public abstract org.netbeans.modules.maven.model.settings.SettingsQNames getSettingsQNames()
meth public void refresh()
supr org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<org.netbeans.modules.maven.model.settings.SettingsComponent>

CLSS public org.netbeans.modules.maven.model.settings.SettingsModelFactory
meth protected org.netbeans.modules.maven.model.settings.SettingsModel createModel(org.netbeans.modules.xml.xam.ModelSource)
meth public org.netbeans.modules.maven.model.settings.SettingsModel getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public static org.netbeans.modules.maven.model.settings.SettingsModelFactory getDefault()
supr org.netbeans.modules.xml.xam.AbstractModelFactory<org.netbeans.modules.maven.model.settings.SettingsModel>
hfds modelFactory

CLSS public final org.netbeans.modules.maven.model.settings.SettingsQName
fld public final static java.lang.String NS_PREFIX = "profile"
fld public final static java.lang.String NS_URI = "http://maven.apache.org/POM/4.0.0"
innr public final static !enum Version
meth public java.lang.String getName()
meth public javax.xml.namespace.QName getQName()
meth public static javax.xml.namespace.QName createQName(java.lang.String,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public static javax.xml.namespace.QName createQName(java.lang.String,org.netbeans.modules.maven.model.settings.SettingsQName$Version)
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds qName

CLSS public final static !enum org.netbeans.modules.maven.model.settings.SettingsQName$Version
 outer org.netbeans.modules.maven.model.settings.SettingsQName
fld public final static org.netbeans.modules.maven.model.settings.SettingsQName$Version NEW_100
fld public final static org.netbeans.modules.maven.model.settings.SettingsQName$Version NEW_110
fld public final static org.netbeans.modules.maven.model.settings.SettingsQName$Version NONE
fld public final static org.netbeans.modules.maven.model.settings.SettingsQName$Version OLD
meth public java.lang.String getNamespace()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.maven.model.settings.SettingsQName$Version valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.model.settings.SettingsQName$Version[] values()
supr java.lang.Enum<org.netbeans.modules.maven.model.settings.SettingsQName$Version>
hfds namespace

CLSS public final org.netbeans.modules.maven.model.settings.SettingsQNames
cons public init(boolean,boolean)
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.maven.model.settings.SettingsQName$Version)
 anno 1 org.netbeans.api.annotations.common.NonNull()
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ACTIVATION
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ACTIVATIONCUSTOM
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ACTIVATIONFILE
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ACTIVATIONOS
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ACTIVATIONPROPERTY
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ACTIVE
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ACTIVEPROFILE
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ACTIVEPROFILES
fld public final org.netbeans.modules.maven.model.settings.SettingsQName CONFIGURATION
fld public final org.netbeans.modules.maven.model.settings.SettingsQName HOST
fld public final org.netbeans.modules.maven.model.settings.SettingsQName ID
fld public final org.netbeans.modules.maven.model.settings.SettingsQName INTERACTIVEMODE
fld public final org.netbeans.modules.maven.model.settings.SettingsQName LAYOUT
fld public final org.netbeans.modules.maven.model.settings.SettingsQName LOCALREPOSITORY
fld public final org.netbeans.modules.maven.model.settings.SettingsQName MIRROR
fld public final org.netbeans.modules.maven.model.settings.SettingsQName MIRROROF
fld public final org.netbeans.modules.maven.model.settings.SettingsQName MIRRORS
fld public final org.netbeans.modules.maven.model.settings.SettingsQName MIRROR_LAYOUT_110
 anno 0 org.netbeans.api.annotations.common.NullAllowed()
fld public final org.netbeans.modules.maven.model.settings.SettingsQName MIRROR_OF_LAYOUTS_110
 anno 0 org.netbeans.api.annotations.common.NullAllowed()
fld public final org.netbeans.modules.maven.model.settings.SettingsQName NAME
fld public final org.netbeans.modules.maven.model.settings.SettingsQName NONPROXYHOSTS
fld public final org.netbeans.modules.maven.model.settings.SettingsQName OFFLINE
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PASSPHRASE
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PASSWORD
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PLUGINGROUP
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PLUGINGROUPS
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PLUGINREPOSITORIES
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PLUGINREPOSITORY
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PORT
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PRIVATEKEY
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PROFILE
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PROFILES
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PROPERTIES
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PROTOCOL
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PROXIES
fld public final org.netbeans.modules.maven.model.settings.SettingsQName PROXY
fld public final org.netbeans.modules.maven.model.settings.SettingsQName RELEASES
fld public final org.netbeans.modules.maven.model.settings.SettingsQName REPOSITORIES
fld public final org.netbeans.modules.maven.model.settings.SettingsQName REPOSITORY
fld public final org.netbeans.modules.maven.model.settings.SettingsQName SERVER
fld public final org.netbeans.modules.maven.model.settings.SettingsQName SERVERS
fld public final org.netbeans.modules.maven.model.settings.SettingsQName SETTINGS
fld public final org.netbeans.modules.maven.model.settings.SettingsQName SNAPSHOTS
fld public final org.netbeans.modules.maven.model.settings.SettingsQName URL
fld public final org.netbeans.modules.maven.model.settings.SettingsQName USEPLUGINREGISTRY
fld public final org.netbeans.modules.maven.model.settings.SettingsQName USERNAME
fld public final org.netbeans.modules.maven.model.settings.SettingsQName VALUE
meth public boolean isNSAware()
meth public boolean isOldNS()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<javax.xml.namespace.QName> getElementQNames()
meth public org.netbeans.modules.maven.model.settings.SettingsQName$Version getNamespaceVersion()
supr java.lang.Object
hfds version

CLSS public abstract interface org.netbeans.modules.maven.model.settings.StringList
intf org.netbeans.modules.maven.model.settings.SettingsComponent
meth public abstract java.util.List<java.lang.String> getListChildren()
meth public abstract void addListChild(java.lang.String)
meth public abstract void removeListChild(java.lang.String)

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModel<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractModel%0}>>
cons public init(org.netbeans.modules.xml.xam.ModelSource)
fld protected org.netbeans.modules.xml.xam.AbstractModel$ModelUndoableEditSupport ues
innr protected ModelUndoableEdit
innr protected ModelUndoableEditSupport
intf javax.swing.event.UndoableEditListener
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.AbstractModel%0}>
meth protected boolean needsSync()
meth protected javax.swing.undo.CompoundEdit createModelUndoableEdit()
meth protected void endTransaction(boolean)
meth protected void finishTransaction()
meth protected void refresh()
meth protected void setInSync(boolean)
meth protected void setInUndoRedo(boolean)
meth protected void setState(org.netbeans.modules.xml.xam.Model$State)
meth protected void syncCompleted()
meth protected void syncStarted()
meth protected void transactionCompleted()
meth protected void transactionStarted()
meth public abstract org.netbeans.modules.xml.xam.ModelAccess getAccess()
meth public boolean inSync()
meth public boolean inUndoRedo()
meth public boolean isAutoSyncActive()
meth public boolean isIntransaction()
meth public boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public boolean startedFiringEvents()
meth public org.netbeans.modules.xml.xam.Model$State getState()
meth public org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public void endTransaction()
meth public void fireComponentChangedEvent(org.netbeans.modules.xml.xam.ComponentEvent)
meth public void firePropertyChangeEvent(java.beans.PropertyChangeEvent)
meth public void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public void rollbackTransaction()
meth public void setAutoSyncActive(boolean)
meth public void sync() throws java.io.IOException
meth public void undoableEditHappened(javax.swing.event.UndoableEditEvent)
meth public void validateWrite()
supr java.lang.Object
hfds RP,componentListeners,inSync,inUndoRedo,logger,pcs,savedUndoableEditListeners,source,status,transaction
hcls Transaction

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModelFactory<%0 extends org.netbeans.modules.xml.xam.Model>
cons public init()
fld public final static int DELAY_DIRTY = 1000
fld public final static int DELAY_SYNCER = 2000
fld public final static java.lang.String MODEL_LOADED_PROPERTY = "modelLoaded"
meth protected abstract {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createModel(org.netbeans.modules.xml.xam.ModelSource)
meth protected java.lang.Object getKey(org.netbeans.modules.xml.xam.ModelSource)
meth protected {org.netbeans.modules.xml.xam.AbstractModelFactory%0} getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractModelFactory%0}> getModels()
meth public static org.netbeans.modules.xml.xam.spi.ModelAccessProvider getAccessProvider()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createFreshModel(org.netbeans.modules.xml.xam.ModelSource)
supr java.lang.Object
hfds LOG,SYNCER,cachedModels,factories,propSupport

CLSS public abstract interface org.netbeans.modules.xml.xam.Component<%0 extends org.netbeans.modules.xml.xam.Component>
meth public abstract <%0 extends {org.netbeans.modules.xml.xam.Component%0}> java.util.List<{%%0}> getChildren(java.lang.Class<{%%0}>)
meth public abstract boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren()
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren(java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.Component%0}>>)
meth public abstract org.netbeans.modules.xml.xam.Component copy({org.netbeans.modules.xml.xam.Component%0})
meth public abstract org.netbeans.modules.xml.xam.Model getModel()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract {org.netbeans.modules.xml.xam.Component%0} getParent()

CLSS public abstract interface org.netbeans.modules.xml.xam.Model<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Model%0}>>
fld public final static java.lang.String STATE_PROPERTY = "state"
innr public final static !enum State
intf org.netbeans.modules.xml.xam.Referenceable
meth public abstract boolean inSync()
meth public abstract boolean isIntransaction()
meth public abstract boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public abstract org.netbeans.modules.xml.xam.Model$State getState()
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public abstract void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public abstract void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void endTransaction()
meth public abstract void removeChildComponent(org.netbeans.modules.xml.xam.Component)
meth public abstract void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void sync() throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>>
cons public init(org.netbeans.modules.xml.xam.ModelSource)
fld protected org.netbeans.modules.xml.xam.dom.DocumentModelAccess access
intf org.netbeans.modules.xml.xam.dom.DocumentModel<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>
meth protected abstract org.netbeans.modules.xml.xam.ComponentUpdater<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}> getComponentUpdater()
meth protected boolean isDomainElement(org.w3c.dom.Node)
meth protected boolean needsSync()
meth protected static java.lang.String toLocalName(java.lang.String)
meth protected void firePropertyChangedEvents(org.netbeans.modules.xml.xam.dom.SyncUnit)
meth protected void firePropertyChangedEvents(org.netbeans.modules.xml.xam.dom.SyncUnit,org.w3c.dom.Element)
meth protected void refresh()
meth protected void setIdentifyingAttributes()
meth protected void syncCompleted()
meth protected void syncStarted()
meth public abstract {org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0} createRootComponent(org.w3c.dom.Element)
meth public boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>> getQNameValuedAttributes()
meth public java.util.Set<java.lang.String> getElementNames()
meth public java.util.Set<javax.xml.namespace.QName> getQNames()
meth public javax.swing.text.Document getBaseDocument()
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent findComponent(org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent,java.util.List<org.w3c.dom.Element>,int)
meth public org.netbeans.modules.xml.xam.dom.ChangeInfo prepareChangeInfo(java.util.List<? extends org.w3c.dom.Node>,java.util.List<? extends org.w3c.dom.Node>)
meth public org.netbeans.modules.xml.xam.dom.ChangeInfo prepareChangeInfo(java.util.List<org.w3c.dom.Node>)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(java.util.List<org.w3c.dom.Element>)
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(org.w3c.dom.Element)
meth public org.netbeans.modules.xml.xam.dom.DocumentModelAccess getAccess()
meth public org.netbeans.modules.xml.xam.dom.SyncUnit prepareSyncUnit(org.netbeans.modules.xml.xam.dom.ChangeInfo,org.netbeans.modules.xml.xam.dom.SyncUnit)
meth public org.w3c.dom.Document getDocument()
meth public static org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider getAccessProvider()
meth public void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public void processSyncUnit(org.netbeans.modules.xml.xam.dom.SyncUnit)
meth public void removeChildComponent(org.netbeans.modules.xml.xam.Component)
supr org.netbeans.modules.xml.xam.AbstractModel<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>
hfds accessPrivate,docListener,elementNames,getAccessLock,needsSync,swingDocument
hcls DocumentChangeListener,WeakDocumentListener

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.Attribute
meth public abstract java.lang.Class getMemberType()
meth public abstract java.lang.Class getType()
meth public abstract java.lang.String getName()

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.ComponentFactory<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.ComponentFactory%0}>>
meth public abstract {org.netbeans.modules.xml.xam.dom.ComponentFactory%0} create(org.w3c.dom.Element,{org.netbeans.modules.xml.xam.dom.ComponentFactory%0})

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentComponent<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent>
fld public final static java.lang.String TEXT_CONTENT_PROPERTY = "textContent"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.dom.DocumentComponent%0}>
meth public abstract boolean isInDocumentModel()
meth public abstract boolean referencesSameNode(org.w3c.dom.Node)
meth public abstract int findAttributePosition(java.lang.String)
meth public abstract int findPosition()
meth public abstract java.lang.String getAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
meth public abstract org.w3c.dom.Element getPeer()
meth public abstract void setAttribute(java.lang.String,org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentComponent%0} findChildComponent(org.w3c.dom.Element)

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentComponent2<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent>
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentComponent2%0}>
meth public abstract int findEndPosition()

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>>
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public abstract org.w3c.dom.Document getDocument()
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} createComponent({org.netbeans.modules.xml.xam.dom.DocumentModel%0},org.w3c.dom.Element)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} getRootComponent()

