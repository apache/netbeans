#Signature file v4.1
#Version 1.69

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

CLSS public abstract interface org.netbeans.modules.ant.freeform.spi.HelpIDFragmentProvider
meth public abstract java.lang.String getHelpIDFragment()

CLSS public final org.netbeans.modules.ant.freeform.spi.ProjectAccessor
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getHelper()
meth public org.netbeans.spi.project.support.ant.PropertyEvaluator getEvaluator()
supr java.lang.Object
hfds p

CLSS public org.netbeans.modules.ant.freeform.spi.ProjectConstants
fld public final static java.lang.String PROJECT_LOCATION_PREFIX = "${project.dir}/"
fld public final static java.lang.String PROP_ANT_SCRIPT = "ant.script"
fld public final static java.lang.String PROP_PROJECT_LOCATION = "project.dir"
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.ant.freeform.spi.ProjectNature
meth public abstract java.util.List<org.netbeans.modules.ant.freeform.spi.TargetDescriptor> getExtraTargets(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.project.AuxiliaryConfiguration)
meth public abstract java.util.Set<java.lang.String> getSourceFolderViewStyles()
meth public abstract org.openide.nodes.Node createSourceFolderView(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.openide.nodes.Node findSourceFolderViewPath(org.netbeans.api.project.Project,org.openide.nodes.Node,java.lang.Object)

CLSS public final org.netbeans.modules.ant.freeform.spi.TargetDescriptor
cons public init(java.lang.String,java.util.List<java.lang.String>,java.lang.String,java.lang.String)
meth public java.lang.String getAccessibleLabel()
meth public java.lang.String getIDEActionLabel()
meth public java.lang.String getIDEActionName()
meth public java.util.List<java.lang.String> getDefaultTargets()
supr java.lang.Object
hfds accessibleLabel,actionLabel,actionName,defaultTargets

CLSS public org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport
fld public final static java.lang.String PROP_ANT_SCRIPT = "antScript"
fld public final static java.lang.String PROP_PROJECT_FOLDER = "projectFolder"
fld public final static java.lang.String PROP_PROJECT_LOCATION = "projectLocation"
fld public final static java.lang.String PROP_PROJECT_NAME = "projectName"
meth public final static org.netbeans.spi.project.support.ant.AntProjectHelper instantiateBasicProjectInfoWizardPanel(org.openide.WizardDescriptor) throws java.io.IOException
meth public final static org.openide.WizardDescriptor$Panel createBasicProjectInfoWizardPanel()
meth public final static org.openide.WizardDescriptor$Panel createTargetMappingWizardPanel(java.util.List<org.netbeans.modules.ant.freeform.spi.TargetDescriptor>)
meth public final static void instantiateTargetMappingWizardPanel(org.netbeans.spi.project.support.ant.AntProjectHelper,org.openide.WizardDescriptor)
meth public static void uninitializeBasicProjectInfoWizardPanel(org.openide.WizardDescriptor)
meth public static void uninitializeTargetMappingWizardPanel(org.openide.WizardDescriptor)
supr java.lang.Object

CLSS public org.netbeans.modules.ant.freeform.spi.support.Util
fld public final static java.lang.String NAMESPACE = "http://www.netbeans.org/ns/freeform-project/2"
meth public static java.io.File getProjectLocation(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
meth public static java.io.File resolveFile(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.io.File,java.lang.String)
meth public static java.lang.String relativizeLocation(java.io.File,java.io.File,java.io.File)
meth public static org.netbeans.spi.project.AuxiliaryConfiguration getAuxiliaryConfiguration(org.netbeans.spi.project.support.ant.AntProjectHelper)
meth public static org.openide.filesystems.FileObject getDefaultAntScript(org.netbeans.api.project.Project)
meth public static org.w3c.dom.Element getPrimaryConfigurationData(org.netbeans.spi.project.support.ant.AntProjectHelper)
meth public static void putPrimaryConfigurationData(org.netbeans.spi.project.support.ant.AntProjectHelper,org.w3c.dom.Element)
supr java.lang.Object
hfds SCHEMA_1,SCHEMA_2

