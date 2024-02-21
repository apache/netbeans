#Signature file v4.1
#Version 2.18

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

CLSS public abstract interface org.netbeans.modules.fish.payara.micro.plugin.Constants
fld public final static java.lang.String ARCHETYPE_ARTIFACT_ID = "payara-micro-maven-archetype"
fld public final static java.lang.String ARCHETYPE_GROUP_ID = "fish.payara.maven.archetypes"
fld public final static java.lang.String ARCHETYPE_REPOSITORY = "https://oss.sonatype.org/content/repositories/snapshots"
fld public final static java.lang.String BUILD_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-build.png"
fld public final static java.lang.String CLEAN_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-clean.png"
fld public final static java.lang.String COMMAND_EXPLODE = "explode"
fld public final static java.lang.String COMPILE_EXPLODE_ACTION = "micro-complie-explode"
fld public final static java.lang.String COMPILE_GOAL = "compiler:compile"
fld public final static java.lang.String DEBUG_ACTION = "debug"
fld public final static java.lang.String DEBUG_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-debug.png"
fld public final static java.lang.String DEBUG_SINGLE_ACTION = "debug.single.deploy"
fld public final static java.lang.String EXPLODED_GOAL = "war:exploded"
fld public final static java.lang.String EXPLODE_ACTION = "micro-explode"
fld public final static java.lang.String HOT_DEPLOY = "hotDeploy"
fld public final static java.lang.String MAVEN_WAR_PROJECT_TYPE = "org-netbeans-modules-maven/war"
fld public final static java.lang.String PAYARA_MICRO_MAVEN_PLUGIN = "fish.payara.maven.plugins:payara-micro-maven-plugin"
fld public final static java.lang.String POM_TEMPLATE = "org/netbeans/modules/fish/payara/micro/plugin/resources/pom.xml.ftl"
fld public final static java.lang.String PROFILE_ACTION = "profile"
fld public final static java.lang.String PROFILE_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-profile.png"
fld public final static java.lang.String PROFILE_SINGLE_ACTION = "profile.single.deploy"
fld public final static java.lang.String PROJECT_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro.png"
fld public final static java.lang.String PROP_ARTIFACT_ID = "artifactId"
fld public final static java.lang.String PROP_AUTO_BIND_HTTP = "autoBindHttp"
fld public final static java.lang.String PROP_CONTEXT_ROOT = "contextRoot"
fld public final static java.lang.String PROP_GROUP_ID = "groupId"
fld public final static java.lang.String PROP_JAVA_EE_VERSION = "javaeeVersion"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_PACKAGE = "package"
fld public final static java.lang.String PROP_PAYARA_MICRO_VERSION = "payaraMicroVersion"
fld public final static java.lang.String PROP_VERSION = "version"
fld public final static java.lang.String REBUILD_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-clean-build.png"
fld public final static java.lang.String RELOAD_FILE = ".reload"
fld public final static java.lang.String RELOAD_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-reload.png"
fld public final static java.lang.String RESOURCES_GOAL = "resources:resources"
fld public final static java.lang.String RESTART_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-restart.png"
fld public final static java.lang.String RUN_ACTION = "run"
fld public final static java.lang.String RUN_SINGLE_ACTION = "run.single.deploy"
fld public final static java.lang.String START_GOAL = "payara-micro:start"
fld public final static java.lang.String START_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-start.png"
fld public final static java.lang.String STOP_ACTION = "micro-stop"
fld public final static java.lang.String STOP_GOAL = "payara-micro:stop"
fld public final static java.lang.String VERSION = "version"
fld public final static java.lang.String WAR_GOAL = "war:war"
fld public final static java.lang.String WAR_PACKAGING = "war"
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type PROJECT_TYPE

CLSS public final org.netbeans.modules.fish.payara.micro.plugin.MicroPluginWizardDescriptor
cons public init()
intf org.openide.WizardDescriptor$InstantiatingIterator<org.openide.WizardDescriptor>
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public java.lang.String name()
meth public java.util.Set instantiate() throws java.io.IOException
meth public org.openide.WizardDescriptor$Panel current()
meth public static void updateMicroMavenPlugin(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void initialize(org.openide.WizardDescriptor)
meth public void nextPanel()
meth public void previousPanel()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void uninitialize(org.openide.WizardDescriptor)
supr java.lang.Object
hfds descriptor,index,panels,project

CLSS public org.netbeans.modules.fish.payara.micro.plugin.POMManager
cons public init(java.io.Reader,org.netbeans.api.project.Project)
cons public init(java.lang.String,org.netbeans.api.project.Project)
cons public init(org.netbeans.api.project.Project)
cons public init(org.netbeans.api.project.Project,boolean)
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getVersion()
meth public static boolean isMavenProject(org.netbeans.api.project.Project)
meth public static void reload(org.netbeans.api.project.Project)
meth public void addProperties(java.lang.String,java.util.Properties)
meth public void addProperties(java.util.Properties)
meth public void commit()
meth public void fixDistributionProperties()
meth public void registerDependency(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public void reload()
meth public void setExtensionOverrideFilter(java.util.function.BiFunction<org.codehaus.plexus.util.xml.Xpp3Dom,org.netbeans.modules.maven.model.pom.POMExtensibilityElement,java.lang.Boolean>)
meth public void setSourceVersion(java.lang.String)
supr java.lang.Object
hfds RP,extensionOverrideFilter,mavenProject,operations,pomFileObject,pomModel,project,sourceModel

CLSS public org.netbeans.modules.fish.payara.micro.plugin.TemplateUtil
cons public init()
meth public static java.io.InputStream loadResource(java.lang.String)
meth public static java.lang.String expandTemplate(java.io.Reader,java.util.Map<java.lang.String,java.lang.Object>)
supr java.lang.Object
hfds ENCODING_PROPERTY_NAME

CLSS public final org.netbeans.modules.fish.payara.micro.plugin.Wizards
meth public static void mergeSteps(org.openide.WizardDescriptor,org.openide.WizardDescriptor$Panel[],java.lang.String[])
supr java.lang.Object
hfds WIZARD_PANEL_CONTENT_DATA,WIZARD_PANEL_CONTENT_SELECTED_INDEX

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

CLSS public abstract interface static org.openide.WizardDescriptor$InstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$Iterator<{org.openide.WizardDescriptor$InstantiatingIterator%0}>
meth public abstract java.util.Set instantiate() throws java.io.IOException
meth public abstract void initialize(org.openide.WizardDescriptor)
meth public abstract void uninitialize(org.openide.WizardDescriptor)

CLSS public abstract interface static org.openide.WizardDescriptor$Iterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean hasNext()
meth public abstract boolean hasPrevious()
meth public abstract java.lang.String name()
meth public abstract org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$Iterator%0}> current()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void nextPanel()
meth public abstract void previousPanel()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

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

