#Signature file v4.1
#Version 1.97.0

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

CLSS public final org.netbeans.modules.web.project.api.WebProjectCreateData
cons public init()
meth public boolean getJavaSourceBased()
meth public boolean isCDIEnabled()
meth public boolean isWebXmlRequired()
meth public boolean skipTests()
meth public java.io.File getProjectDir()
meth public java.io.File[] getSourceFolders()
meth public java.io.File[] getTestFolders()
meth public java.lang.String getBuildfile()
meth public java.lang.String getContextPath()
meth public java.lang.String getJavaEEVersion()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getJavaPlatformName()
meth public java.lang.String getLibrariesDefinition()
meth public java.lang.String getName()
meth public java.lang.String getServerInstanceID()
meth public java.lang.String getSourceLevel()
meth public java.lang.String getSourceStructure()
meth public org.netbeans.api.j2ee.core.Profile getJavaEEProfile()
meth public org.openide.filesystems.FileObject getDocBase()
meth public org.openide.filesystems.FileObject getLibFolder()
meth public org.openide.filesystems.FileObject getWebInfFolder()
meth public org.openide.filesystems.FileObject getWebModuleFO()
meth public void setBuildfile(java.lang.String)
meth public void setCDIEnabled(boolean)
meth public void setContextPath(java.lang.String)
meth public void setDocBase(org.openide.filesystems.FileObject)
meth public void setJavaEEProfile(org.netbeans.api.j2ee.core.Profile)
meth public void setJavaEEVersion(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setJavaPlatformName(java.lang.String)
meth public void setJavaSourceBased(boolean)
meth public void setLibFolder(org.openide.filesystems.FileObject)
meth public void setLibrariesDefinition(java.lang.String)
meth public void setName(java.lang.String)
meth public void setProjectDir(java.io.File)
meth public void setServerInstanceID(java.lang.String)
meth public void setSourceFolders(java.io.File[])
meth public void setSourceLevel(java.lang.String)
meth public void setSourceStructure(java.lang.String)
meth public void setTestFolders(java.io.File[])
meth public void setWebInfFolder(org.openide.filesystems.FileObject)
meth public void setWebModuleFO(org.openide.filesystems.FileObject)
meth public void setWebXmlRequired(boolean)
supr java.lang.Object
hfds buildfile,cdiEnabled,contextPath,docBase,javaEEProfile,javaPlatformName,javaSourceBased,libFolder,librariesDefinition,name,projectDir,serverInstanceID,sourceFolders,sourceLevel,sourceStructure,testFolders,webInfFolder,webXmlRequired,wmFO

CLSS public abstract interface org.netbeans.modules.web.project.api.WebProjectLibrariesModifier2
meth public abstract boolean addPackageAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],java.lang.String) throws java.io.IOException
meth public abstract boolean addPackageLibraries(org.netbeans.api.project.libraries.Library[],java.lang.String) throws java.io.IOException
meth public abstract boolean addPackageRoots(java.net.URL[],java.lang.String) throws java.io.IOException
meth public abstract boolean removePackageAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],java.lang.String) throws java.io.IOException
meth public abstract boolean removePackageLibraries(org.netbeans.api.project.libraries.Library[],java.lang.String) throws java.io.IOException
meth public abstract boolean removePackageRoots(java.net.URL[],java.lang.String) throws java.io.IOException

CLSS public org.netbeans.modules.web.project.api.WebProjectUtilities
fld public final static java.lang.String MINIMUM_ANT_VERSION = "1.6.5"
fld public final static java.lang.String SRC_STRUCT_BLUEPRINTS = "BluePrints"
fld public final static java.lang.String SRC_STRUCT_JAKARTA = "Jakarta"
meth public static java.util.Set<org.openide.filesystems.FileObject> ensureWelcomePage(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
meth public static java.util.Set<org.openide.filesystems.FileObject> ensureWelcomePage(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.netbeans.api.j2ee.core.Profile) throws java.io.IOException
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(java.io.File,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(org.netbeans.modules.web.project.api.WebProjectCreateData) throws java.io.IOException
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper importProject(java.io.File,java.lang.String,org.openide.filesystems.FileObject,java.io.File[],java.io.File[],org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper importProject(java.io.File,java.lang.String,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper importProject(org.netbeans.modules.web.project.api.WebProjectCreateData) throws java.io.IOException
meth public static void upgradeJ2EEProfile(org.netbeans.modules.web.project.WebProject)
supr java.lang.Object
hfds DEFAULT_BUILD_DIR,DEFAULT_CONF_FOLDER,DEFAULT_DOC_BASE_FOLDER,DEFAULT_JAVA_FOLDER,DEFAULT_RESOURCE_FOLDER,DEFAULT_SRC_FOLDER,DEFAULT_TEST_FOLDER,LOGGER,RESOURCE_FOLDER,SOURCE_ROOT_REF,WEB_INF

CLSS public abstract interface org.netbeans.modules.web.project.api.WebPropertyEvaluator
meth public abstract org.netbeans.spi.project.support.ant.PropertyEvaluator evaluator()

CLSS public abstract interface org.netbeans.modules.web.project.spi.BrokenLibraryRefFilter
meth public abstract boolean removeLibraryReference(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.project.spi.BrokenLibraryRefFilterProvider
meth public abstract org.netbeans.modules.web.project.spi.BrokenLibraryRefFilter createFilter(org.netbeans.api.project.Project)

CLSS public abstract interface org.netbeans.modules.web.project.spi.WebProjectImplementationFactory
meth public abstract boolean acceptProject(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException
meth public abstract org.netbeans.api.project.Project createProject(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException

