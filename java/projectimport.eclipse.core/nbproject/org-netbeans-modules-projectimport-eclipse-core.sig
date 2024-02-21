#Signature file v4.1
#Version 2.53

CLSS public abstract interface java.io.Serializable

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

CLSS public final org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry
cons public init(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
fld public final static java.lang.String ATTRIBUTE_EXPORTED = "exported"
fld public final static java.lang.String ATTRIBUTE_JAVADOC = "javadoc_location"
fld public final static java.lang.String ATTRIBUTE_KIND = "kind"
fld public final static java.lang.String ATTRIBUTE_PATH = "path"
fld public final static java.lang.String ATTRIBUTE_SOURCEPATH = "sourcepath"
fld public final static java.lang.String ATTRIBUTE_SOURCE_EXCLUDES = "excluding"
fld public final static java.lang.String ATTRIBUTE_SOURCE_INCLUDES = "including"
innr public final static !enum Kind
meth public boolean isExported()
meth public java.lang.Boolean getImportSuccessful()
meth public java.lang.String getAbsolutePath()
meth public java.lang.String getContainerMapping()
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String getRawPath()
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getPropertyNames()
meth public org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind getKind()
meth public void setAbsolutePath(java.lang.String)
meth public void setContainerMapping(java.lang.String)
meth public void setImportSuccessful(java.lang.Boolean)
meth public void updateJavadoc(java.lang.String)
meth public void updateSourcePath(java.lang.String)
meth public void updateVariableValue(java.lang.String)
supr java.lang.Object
hfds absolutePath,containerMapping,importSuccessful,linkName,properties

CLSS public final static !enum org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind
 outer org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry
fld public final static org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind CONTAINER
fld public final static org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind LIBRARY
fld public final static org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind OUTPUT
fld public final static org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind PROJECT
fld public final static org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind SOURCE
fld public final static org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind VARIABLE
meth public static org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind valueOf(java.lang.String)
meth public static org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind[] values()
supr java.lang.Enum<org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry$Kind>

CLSS public final org.netbeans.modules.projectimport.eclipse.core.spi.Facets
cons public init(java.util.List<org.netbeans.modules.projectimport.eclipse.core.spi.Facets$Facet>)
innr public final static Facet
meth public boolean hasInstalledFacet(java.lang.String)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.projectimport.eclipse.core.spi.Facets$Facet> getInstalled()
meth public org.netbeans.modules.projectimport.eclipse.core.spi.Facets$Facet getFacet(java.lang.String)
supr java.lang.Object
hfds installed

CLSS public final static org.netbeans.modules.projectimport.eclipse.core.spi.Facets$Facet
 outer org.netbeans.modules.projectimport.eclipse.core.spi.Facets
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
supr java.lang.Object
hfds name,version

CLSS public final org.netbeans.modules.projectimport.eclipse.core.spi.LaunchConfiguration
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
fld public final static java.lang.String TYPE_LOCAL_JAVA_APPLICATION = "org.eclipse.jdt.launching.localJavaApplication"
meth public java.lang.String getMainType()
meth public java.lang.String getName()
meth public java.lang.String getProgramArguments()
meth public java.lang.String getProjectName()
meth public java.lang.String getType()
meth public java.lang.String getVmArguments()
supr java.lang.Object
hfds mainType,name,programArguments,projectName,type,vmArguments

CLSS public org.netbeans.modules.projectimport.eclipse.core.spi.ProjectFactorySupport
cons public init()
meth public static boolean areSourceRootsOwned(org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel,java.io.File,java.util.List<java.lang.String>)
meth public static java.lang.String calculateKey(org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel)
meth public static java.lang.String synchronizeProjectClassPath(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel,java.lang.String,java.lang.String,java.util.List<java.lang.String>) throws java.io.IOException
meth public static void setupSourceExcludes(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel,java.util.List<java.lang.String>)
meth public static void updateProjectClassPath(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel,java.util.List<java.lang.String>) throws java.io.IOException
meth public static void updateSourceRootLabels(java.util.List<org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry>,org.netbeans.modules.java.api.common.SourceRoots)
supr java.lang.Object
hfds LOG

CLSS public final org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel
cons public init(org.netbeans.modules.projectimport.eclipse.core.EclipseProject,java.io.File,org.netbeans.api.java.platform.JavaPlatform,java.util.List<org.netbeans.api.project.Project>)
cons public init(org.netbeans.modules.projectimport.eclipse.core.EclipseProject,java.io.File,org.netbeans.api.java.platform.JavaPlatform,java.util.List<org.netbeans.api.project.Project>,java.util.List<org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>>)
meth public boolean isDebug()
meth public boolean isDeprecation()
meth public java.io.File getEclipseProjectFolder()
meth public java.io.File getEclipseWorkspaceFolder()
meth public java.io.File getNetBeansProjectLocation()
meth public java.io.File[] getEclipseSourceRootsAsFileArray()
meth public java.io.File[] getEclipseTestSourceRootsAsFileArray()
meth public java.lang.String getCompilerArgs()
meth public java.lang.String getEncoding()
meth public java.lang.String getProjectName()
meth public java.lang.String getSourceLevel()
meth public java.lang.String getTargetLevel()
meth public java.util.Collection<org.netbeans.modules.projectimport.eclipse.core.spi.LaunchConfiguration> getLaunchConfigurations()
meth public java.util.List<org.netbeans.api.project.Project> getAlreadyImportedProjects()
meth public java.util.List<org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry> getEclipseClassPathEntries()
meth public java.util.List<org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry> getEclipseSourceRoots()
meth public java.util.List<org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry> getEclipseTestSourceRoots()
meth public java.util.List<org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>> getExtraWizardPanels()
meth public java.util.Set<java.lang.String> getEclipseNatures()
meth public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform()
meth public org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry getOutput()
meth public org.netbeans.modules.projectimport.eclipse.core.spi.Facets getFacets()
supr java.lang.Object
hfds LOG,alreadyImportedProjects,extraWizardPanels,looksLikeTests,platform,project,projectLocation

CLSS public abstract interface org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory
fld public final static java.lang.String FILE_LOCATION_TOKEN_WEBINF = "webinf"
innr public final static ProjectDescriptor
meth public abstract boolean canHandle(org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory$ProjectDescriptor)
meth public abstract java.io.File getProjectFileLocation(org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory$ProjectDescriptor,java.lang.String)
meth public abstract java.lang.String getProjectTypeName()
meth public abstract java.util.List<org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>> getAdditionalImportWizardPanels()
meth public abstract javax.swing.Icon getProjectTypeIcon()
meth public abstract org.netbeans.api.project.Project createProject(org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel,java.util.List<java.lang.String>) throws java.io.IOException

CLSS public final static org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory$ProjectDescriptor
 outer org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory
cons public init(java.io.File,java.util.Set<java.lang.String>,org.netbeans.modules.projectimport.eclipse.core.spi.Facets)
meth public java.io.File getEclipseProjectFolder()
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getNatures()
meth public org.netbeans.modules.projectimport.eclipse.core.spi.Facets getFacets()
supr java.lang.Object
hfds eclipseProject,facets,natures

CLSS public abstract interface org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater
intf org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory
meth public abstract java.lang.String calculateKey(org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel)
meth public abstract java.lang.String update(org.netbeans.api.project.Project,org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel,java.lang.String,java.util.List<java.lang.String>) throws java.io.IOException

CLSS public final org.netbeans.modules.projectimport.eclipse.core.spi.UpgradableProjectLookupProvider
cons public init()
intf org.netbeans.spi.project.LookupProvider
meth public org.openide.util.Lookup createAdditionalLookup(org.openide.util.Lookup)
meth public static boolean isRegistered(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.LookupProvider
innr public abstract interface static !annotation Registration
meth public abstract org.openide.util.Lookup createAdditionalLookup(org.openide.util.Lookup)

