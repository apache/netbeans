#Signature file v4.1
#Version 1.110

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

CLSS public final org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator
meth public static org.netbeans.api.project.Project createProject(org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties
cons public init(org.openide.filesystems.FileObject,java.lang.String)
meth public boolean isAutoconfigured()
meth public java.lang.String getJsTestingProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getPlatformProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getProjectName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getProjectUrl()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getSiteRootFolder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getSourceFolder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getStartFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getTestFolder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getTestSeleniumFolder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setAutoconfigured(boolean)
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setJsTestingProvider(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setPlatformProvider(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setProjectUrl(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setSiteRootFolder(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setSourceFolder(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setStartFile(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setTestFolder(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties setTestSeleniumFolder(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.openide.filesystems.FileObject getProjectDir()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds autoconfigured,jsTestingProvider,platformProvider,projectDir,projectName,projectUrl,siteRootFolder,sourceFolder,startFile,testFolder,testSeleniumFolder

CLSS public final org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils
fld public final static java.lang.String PROJECT_DIRECTORY = "PROJECT_DIRECTORY"
fld public final static java.lang.String PROJECT_NAME = "NAME"
innr public final static Tools
meth public static java.util.Set<org.openide.filesystems.FileObject> instantiateTools(org.netbeans.api.project.Project,org.openide.WizardDescriptor$FinishablePanel<org.openide.WizardDescriptor>) throws java.io.IOException
meth public static org.openide.util.Pair<org.openide.WizardDescriptor$FinishablePanel<org.openide.WizardDescriptor>,java.lang.String> createBaseWizardPanel(java.lang.String)
meth public static org.openide.util.Pair<org.openide.WizardDescriptor$FinishablePanel<org.openide.WizardDescriptor>,java.lang.String> createToolsWizardPanel(org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils$Tools)
supr java.lang.Object

CLSS public final static org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils$Tools
 outer org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils
cons public init()
meth public boolean isBower()
meth public boolean isGrunt()
meth public boolean isGulp()
meth public boolean isNpm()
meth public java.lang.String toString()
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils$Tools setBower(boolean)
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils$Tools setGrunt(boolean)
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils$Tools setGulp(boolean)
meth public org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils$Tools setNpm(boolean)
meth public static org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils$Tools all()
supr java.lang.Object
hfds bower,grunt,gulp,npm

