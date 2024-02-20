#Signature file v4.1
#Version 1.76

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

CLSS public final org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectCreateData
cons public init()
meth public boolean isCDIEnabled()
meth public boolean skipTests()
meth public java.io.File getConfigFilesBase()
meth public java.io.File getLibFolder()
meth public java.io.File getProjectDir()
meth public java.io.File[] getSourceFolders()
meth public java.io.File[] getTestFolders()
meth public java.lang.String getLibrariesDefinition()
meth public java.lang.String getName()
meth public java.lang.String getServerInstanceID()
meth public org.netbeans.api.j2ee.core.Profile getJavaEEProfile()
meth public void setCDIEnabled(boolean)
meth public void setConfigFilesBase(java.io.File)
meth public void setJavaEEProfile(org.netbeans.api.j2ee.core.Profile)
meth public void setLibFolder(java.io.File)
meth public void setLibrariesDefinition(java.lang.String)
meth public void setName(java.lang.String)
meth public void setProjectDir(java.io.File)
meth public void setServerInstanceID(java.lang.String)
meth public void setSourceFolders(java.io.File[])
meth public void setTestFolders(java.io.File[])
supr java.lang.Object
hfds cdiEnabled,configFilesBase,javaEEProfile,libFolder,librariesDefinition,name,projectDir,serverInstanceID,sourceFolders,testFolders

CLSS public org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator
fld public final static java.lang.String MINIMUM_ANT_VERSION = "1.6.5"
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(java.io.File,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(java.io.File,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectCreateData) throws java.io.IOException
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper importProject(java.io.File,java.lang.String,java.io.File[],java.io.File[],java.io.File,java.io.File,java.lang.String,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper importProject(java.io.File,java.lang.String,java.io.File[],java.io.File[],java.io.File,java.io.File,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper importProject(java.io.File,java.lang.String,java.io.File[],java.io.File[],java.io.File,java.io.File,java.lang.String,java.lang.String,boolean,java.lang.String) throws java.io.IOException
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper importProject(java.io.File,java.lang.String,java.io.File[],java.io.File[],java.io.File,java.io.File,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper importProject(org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectCreateData) throws java.io.IOException
meth public static void setPlatform(org.netbeans.spi.project.support.ant.AntProjectHelper,java.lang.String,java.lang.String)
supr java.lang.Object
hfds DEFAULT_BUILD_DIR,DEFAULT_DOC_BASE_FOLDER,DEFAULT_JAVA_FOLDER,DEFAULT_RESOURCE_FOLDER,DEFAULT_SRC_FOLDER,DEFAULT_TEST_FOLDER

