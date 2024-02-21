#Signature file v4.1
#Version 1.61

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public org.netbeans.modules.web.api.webmodule.ExtenderController
innr public static Properties
meth public java.lang.String getErrorMessage()
meth public org.netbeans.modules.web.api.webmodule.ExtenderController$Properties getProperties()
meth public static org.netbeans.modules.web.api.webmodule.ExtenderController create()
meth public void setErrorMessage(java.lang.String)
supr java.lang.Object
hfds errorMessage,properties

CLSS public static org.netbeans.modules.web.api.webmodule.ExtenderController$Properties
 outer org.netbeans.modules.web.api.webmodule.ExtenderController
cons public init()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public void setProperty(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds properties

CLSS public final org.netbeans.modules.web.api.webmodule.RequestParametersQuery
cons public init()
meth public static java.lang.String getFileAndParameters(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds implementations

CLSS public final org.netbeans.modules.web.api.webmodule.WebFrameworks
meth public static java.util.List<org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider> getFrameworks()
supr java.lang.Object
hfds FRAMEWORK_PATH

CLSS public final org.netbeans.modules.web.api.webmodule.WebModule
fld public final static java.lang.String J2EE_13_LEVEL = "1.3"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String J2EE_14_LEVEL = "1.4"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String JAVA_EE_5_LEVEL = "1.5"
 anno 0 java.lang.Deprecated()
meth public java.lang.String getContextPath()
meth public java.lang.String getJ2eePlatformVersion()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.j2ee.core.Profile getJ2eeProfile()
meth public org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata> getMetadataModel()
meth public org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public org.openide.filesystems.FileObject getDocumentBase()
meth public org.openide.filesystems.FileObject getWebInf()
meth public org.openide.filesystems.FileObject[] getJavaSources()
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.web.api.webmodule.WebModule getWebModule(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds impl,impl2,implementations

CLSS public final org.netbeans.modules.web.api.webmodule.WebProjectConstants
fld public final static java.lang.String ARTIFACT_TYPE_WAR = "war"
fld public final static java.lang.String ARTIFACT_TYPE_WAR_EAR_ARCHIVE = "j2ee_ear_archive"
fld public final static java.lang.String COMMAND_REDEPLOY = "redeploy"
fld public final static java.lang.String TYPE_DOC_ROOT = "doc_root"
fld public final static java.lang.String TYPE_WEB_INF = "web_inf"
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel
 anno 0 java.lang.Deprecated()
intf org.openide.WizardDescriptor$Panel
meth public abstract void enableComponents(boolean)

CLSS public abstract interface org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation
meth public abstract java.lang.String getFileAndParameters(org.openide.filesystems.FileObject)

CLSS public abstract org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider
cons public init(java.lang.String,java.lang.String)
meth public abstract boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule)
meth public abstract java.io.File[] getConfigurationFiles(org.netbeans.modules.web.api.webmodule.WebModule)
meth public boolean requiresWebXml()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String getServletPath(org.openide.filesystems.FileObject)
meth public java.util.Set extend(org.netbeans.modules.web.api.webmodule.WebModule)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel getConfigurationPanel(org.netbeans.modules.web.api.webmodule.WebModule)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.web.spi.webmodule.WebModuleExtender createWebModuleExtender(org.netbeans.modules.web.api.webmodule.WebModule,org.netbeans.modules.web.api.webmodule.ExtenderController)
supr java.lang.Object
hfds description,name

CLSS public abstract org.netbeans.modules.web.spi.webmodule.WebModuleExtender
cons public init()
innr public abstract interface static Savable
meth public abstract boolean isValid()
meth public abstract java.util.Set<org.openide.filesystems.FileObject> extend(org.netbeans.modules.web.api.webmodule.WebModule)
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.HelpCtx getHelp()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void update()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.web.spi.webmodule.WebModuleExtender$Savable
 outer org.netbeans.modules.web.spi.webmodule.WebModuleExtender
meth public abstract void save(org.netbeans.modules.web.api.webmodule.WebModule)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.web.spi.webmodule.WebModuleFactory
meth public static org.netbeans.modules.web.api.webmodule.WebModule createWebModule(org.netbeans.modules.web.spi.webmodule.WebModuleImplementation)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.web.api.webmodule.WebModule createWebModule(org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.spi.webmodule.WebModuleImplementation
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getContextPath()
meth public abstract java.lang.String getJ2eePlatformVersion()
meth public abstract org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata> getMetadataModel()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getDocumentBase()
meth public abstract org.openide.filesystems.FileObject getWebInf()
meth public abstract org.openide.filesystems.FileObject[] getJavaSources()
 anno 0 java.lang.Deprecated()

CLSS public abstract interface org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2
fld public final static java.lang.String PROPERTY_DOCUMENT_BASE = "documentBase"
fld public final static java.lang.String PROPERTY_WEB_INF = "webInf"
meth public abstract java.lang.String getContextPath()
meth public abstract org.netbeans.api.j2ee.core.Profile getJ2eeProfile()
meth public abstract org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata> getMetadataModel()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getDocumentBase()
meth public abstract org.openide.filesystems.FileObject getWebInf()
meth public abstract org.openide.filesystems.FileObject[] getJavaSources()
 anno 0 java.lang.Deprecated()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.web.spi.webmodule.WebModuleProvider
meth public abstract org.netbeans.modules.web.api.webmodule.WebModule findWebModule(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.web.spi.webmodule.WebPrivilegedTemplates
meth public abstract java.lang.String[] getPrivilegedTemplates(org.netbeans.modules.web.api.webmodule.WebModule)

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

CLSS public abstract interface static org.openide.WizardDescriptor$Panel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean isValid()
meth public abstract java.awt.Component getComponent()
meth public abstract org.openide.util.HelpCtx getHelp()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void readSettings({org.openide.WizardDescriptor$Panel%0})
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void storeSettings({org.openide.WizardDescriptor$Panel%0})

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

