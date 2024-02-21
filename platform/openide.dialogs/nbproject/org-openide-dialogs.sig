#Signature file v4.1
#Version 7.70

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

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

CLSS public abstract org.openide.DialogDisplayer
cons protected init()
meth public <%0 extends org.openide.NotifyDescriptor> java.util.concurrent.CompletableFuture<{%%0}> notifyFuture({%%0})
meth public abstract java.awt.Dialog createDialog(org.openide.DialogDescriptor)
meth public abstract java.lang.Object notify(org.openide.NotifyDescriptor)
meth public java.awt.Dialog createDialog(org.openide.DialogDescriptor,java.awt.Frame)
meth public static org.openide.DialogDisplayer getDefault()
meth public void notifyLater(org.openide.NotifyDescriptor)
supr java.lang.Object
hcls Trivial

CLSS public abstract org.openide.ErrorManager
cons public init()
fld public final static int ERROR = 65536
fld public final static int EXCEPTION = 4096
fld public final static int INFORMATIONAL = 1
fld public final static int UNKNOWN = 0
fld public final static int USER = 256
fld public final static int WARNING = 16
innr public abstract interface static Annotation
meth public abstract java.lang.Throwable annotate(java.lang.Throwable,int,java.lang.String,java.lang.String,java.lang.Throwable,java.util.Date)
meth public abstract java.lang.Throwable attachAnnotations(java.lang.Throwable,org.openide.ErrorManager$Annotation[])
meth public abstract org.openide.ErrorManager getInstance(java.lang.String)
meth public abstract org.openide.ErrorManager$Annotation[] findAnnotations(java.lang.Throwable)
meth public abstract void log(int,java.lang.String)
meth public abstract void notify(int,java.lang.Throwable)
meth public boolean isLoggable(int)
meth public boolean isNotifiable(int)
meth public final java.lang.Throwable annotate(java.lang.Throwable,java.lang.String)
meth public final java.lang.Throwable annotate(java.lang.Throwable,java.lang.Throwable)
meth public final java.lang.Throwable copyAnnotation(java.lang.Throwable,java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public final void log(java.lang.String)
meth public final void notify(java.lang.Throwable)
meth public static org.openide.ErrorManager getDefault()
supr java.lang.Object
hfds current
hcls AnnException,DelegatingErrorManager,OwnLevel

CLSS public abstract interface static org.openide.ErrorManager$Annotation
 outer org.openide.ErrorManager
meth public abstract int getSeverity()
meth public abstract java.lang.String getLocalizedMessage()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.Throwable getStackTrace()
meth public abstract java.util.Date getDate()

CLSS public abstract org.openide.LifecycleManager
cons protected init()
meth public abstract void exit()
meth public abstract void saveAll()
meth public static org.openide.LifecycleManager getDefault()
meth public void exit(int)
meth public void markForRestart()
supr java.lang.Object
hcls Trivial

CLSS public final org.openide.NotificationLineSupport
meth public final java.lang.String getErrorMessage()
meth public final java.lang.String getInformationMessage()
meth public final java.lang.String getWarningMessage()
meth public final void clearMessages()
meth public final void setErrorMessage(java.lang.String)
meth public final void setInformationMessage(java.lang.String)
meth public final void setWarningMessage(java.lang.String)
supr java.lang.Object
hfds nd

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

CLSS public final static org.openide.NotifyDescriptor$ComposedInput
 outer org.openide.NotifyDescriptor
cons public init(java.lang.String,int,org.openide.NotifyDescriptor$ComposedInput$Callback)
fld public final static java.lang.String PROP_ESTIMATED_NUMBER_OF_INPUTS = "estimatedNumberOfInputs"
innr public abstract interface static Callback
meth public int getEstimatedNumberOfInputs()
meth public java.lang.Object getMessage()
meth public org.openide.NotifyDescriptor createInput(int)
meth public org.openide.NotifyDescriptor[] getInputs()
meth public void setEstimatedNumberOfInputs(int)
supr org.openide.NotifyDescriptor
hfds callback,estimatedNumberOfInputs,inputs

CLSS public abstract interface static org.openide.NotifyDescriptor$ComposedInput$Callback
 outer org.openide.NotifyDescriptor$ComposedInput
meth public abstract org.openide.NotifyDescriptor createInput(org.openide.NotifyDescriptor$ComposedInput,int)

CLSS public static org.openide.NotifyDescriptor$Confirmation
 outer org.openide.NotifyDescriptor
cons public init(java.lang.Object)
cons public init(java.lang.Object,int)
cons public init(java.lang.Object,int,int)
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.String,int)
cons public init(java.lang.Object,java.lang.String,int,int)
supr org.openide.NotifyDescriptor

CLSS public final static org.openide.NotifyDescriptor$Exception
 outer org.openide.NotifyDescriptor
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Throwable)
cons public init(java.lang.Throwable,java.lang.Object)
supr org.openide.NotifyDescriptor$Confirmation
hfds serialVersionUID

CLSS public static org.openide.NotifyDescriptor$InputLine
 outer org.openide.NotifyDescriptor
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,int,int)
fld protected javax.swing.JTextField textField
fld public final static java.lang.String PROP_INPUT_TEXT = "inputText"
meth protected java.awt.Component createDesign(java.lang.String)
meth public java.lang.String getInputText()
meth public void setInputText(java.lang.String)
meth public void setInputTextEventEnabled(boolean)
supr org.openide.NotifyDescriptor
hfds inputTextEventEnabled,inputTextEventSuppressed

CLSS public static org.openide.NotifyDescriptor$Message
 outer org.openide.NotifyDescriptor
cons public init(java.lang.Object)
cons public init(java.lang.Object,int)
supr org.openide.NotifyDescriptor

CLSS public final static org.openide.NotifyDescriptor$PasswordLine
 outer org.openide.NotifyDescriptor
cons public init(java.lang.String,java.lang.String)
supr org.openide.NotifyDescriptor$InputLine

CLSS public final static org.openide.NotifyDescriptor$QuickPick
 outer org.openide.NotifyDescriptor
cons public init(java.lang.String,java.lang.String,java.util.List<org.openide.NotifyDescriptor$QuickPick$Item>,boolean)
innr public final static Item
meth public boolean isMultipleSelection()
meth public java.lang.Object getMessage()
meth public java.lang.String getLabel()
meth public java.util.List<org.openide.NotifyDescriptor$QuickPick$Item> getItems()
supr org.openide.NotifyDescriptor
hfds items,multipleSelection,text

CLSS public final static org.openide.NotifyDescriptor$QuickPick$Item
 outer org.openide.NotifyDescriptor$QuickPick
cons public init(java.lang.String,java.lang.String)
meth public boolean isSelected()
meth public java.lang.String getDescription()
meth public java.lang.String getLabel()
meth public void setSelected(boolean)
supr java.lang.Object
hfds description,label,selected

CLSS public abstract org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static Registry
innr public final static Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String displayName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public final org.openide.ServiceType createClone()
 anno 0 java.lang.Deprecated()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds err,name,serialVersionUID,supp

CLSS public final static org.openide.ServiceType$Handle
 outer org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init(org.openide.ServiceType)
intf java.io.Serializable
meth public java.lang.String toString()
meth public org.openide.ServiceType getServiceType()
supr java.lang.Object
hfds className,name,serialVersionUID,serviceType

CLSS public abstract static org.openide.ServiceType$Registry
 outer org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
intf java.io.Serializable
meth public <%0 extends org.openide.ServiceType> java.util.Enumeration<{%%0}> services(java.lang.Class<{%%0}>)
meth public abstract java.util.Enumeration<org.openide.ServiceType> services()
meth public abstract java.util.List getServiceTypes()
meth public abstract void setServiceTypes(java.util.List)
 anno 0 java.lang.Deprecated()
meth public org.openide.ServiceType find(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public org.openide.ServiceType find(java.lang.String)
supr java.lang.Object
hfds serialVersionUID

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

CLSS public static org.openide.WizardDescriptor$ArrayIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
cons public init()
cons public init(java.util.List<org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$ArrayIterator%0}>>)
cons public init(org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$ArrayIterator%0}>[])
intf org.openide.WizardDescriptor$Iterator<{org.openide.WizardDescriptor$ArrayIterator%0}>
meth protected org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$ArrayIterator%0}>[] initializePanels()
meth protected void reset()
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public java.lang.String name()
meth public org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$ArrayIterator%0}> current()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void nextPanel()
meth public void previousPanel()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds index,panels

CLSS public abstract interface static org.openide.WizardDescriptor$AsynchronousInstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$InstantiatingIterator<{org.openide.WizardDescriptor$AsynchronousInstantiatingIterator%0}>
meth public abstract java.util.Set instantiate() throws java.io.IOException

CLSS public abstract interface static org.openide.WizardDescriptor$AsynchronousValidatingPanel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$ValidatingPanel<{org.openide.WizardDescriptor$AsynchronousValidatingPanel%0}>
meth public abstract void prepareValidation()
meth public abstract void validate() throws org.openide.WizardValidationException

CLSS public abstract interface static org.openide.WizardDescriptor$BackgroundInstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$AsynchronousInstantiatingIterator<{org.openide.WizardDescriptor$BackgroundInstantiatingIterator%0}>
meth public abstract java.util.Set instantiate() throws java.io.IOException

CLSS public abstract interface static org.openide.WizardDescriptor$ExtendedAsynchronousValidatingPanel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$AsynchronousValidatingPanel<{org.openide.WizardDescriptor$ExtendedAsynchronousValidatingPanel%0}>
meth public abstract void finishValidation()
meth public abstract void prepareValidation()
meth public abstract void validate() throws org.openide.WizardValidationException

CLSS public abstract interface static org.openide.WizardDescriptor$FinishPanel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
 anno 0 java.lang.Deprecated()
intf org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$FinishPanel%0}>

CLSS public abstract interface static org.openide.WizardDescriptor$FinishablePanel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$FinishablePanel%0}>
meth public abstract boolean isFinishPanel()

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

CLSS public abstract interface static org.openide.WizardDescriptor$Panel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean isValid()
meth public abstract java.awt.Component getComponent()
meth public abstract org.openide.util.HelpCtx getHelp()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void readSettings({org.openide.WizardDescriptor$Panel%0})
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void storeSettings({org.openide.WizardDescriptor$Panel%0})

CLSS public abstract interface static org.openide.WizardDescriptor$ProgressInstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$AsynchronousInstantiatingIterator<{org.openide.WizardDescriptor$ProgressInstantiatingIterator%0}>
meth public abstract java.util.Set instantiate(org.netbeans.api.progress.ProgressHandle) throws java.io.IOException

CLSS public abstract interface static org.openide.WizardDescriptor$ValidatingPanel<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$ValidatingPanel%0}>
meth public abstract void validate() throws org.openide.WizardValidationException

CLSS public final org.openide.WizardValidationException
cons public init(javax.swing.JComponent,java.lang.String,java.lang.String)
meth public java.lang.String getLocalizedMessage()
meth public javax.swing.JComponent getSource()
supr java.lang.Exception
hfds localizedMessage,source

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

