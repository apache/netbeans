#Signature file v4.1
#Version 1.57

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

CLSS public final org.netbeans.modules.j2ee.core.api.support.SourceGroups
meth public static boolean isFolderWritable(org.netbeans.api.project.SourceGroup,java.lang.String)
meth public static java.lang.String getPackageForFolder(org.netbeans.api.project.SourceGroup,org.openide.filesystems.FileObject)
meth public static org.netbeans.api.project.SourceGroup getClassSourceGroup(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.SourceGroup getFolderSourceGroup(org.netbeans.api.project.SourceGroup[],org.openide.filesystems.FileObject)
meth public static org.netbeans.api.project.SourceGroup[] getJavaSourceGroups(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject getFolderForPackage(org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getFolderForPackage(org.netbeans.api.project.SourceGroup,java.lang.String,boolean) throws java.io.IOException
supr java.lang.Object
hfds LOGGER

CLSS public final org.netbeans.modules.j2ee.core.api.support.Strings
meth public static boolean isEmpty(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier
fld public final static java.lang.String API_ANNOTATION = "annotation"
fld public final static java.lang.String API_EJB = "ejb"
fld public final static java.lang.String API_J2EE = "j2ee-api"
fld public final static java.lang.String API_JSF = "jsf-api"
fld public final static java.lang.String API_JSP = "jsp-api"
fld public final static java.lang.String API_PERSISTENCE = "persistence"
fld public final static java.lang.String API_SERVLET = "servlet-api"
fld public final static java.lang.String API_TRANSACTION = "transaction"
meth public abstract void extendClasspath(org.openide.filesystems.FileObject,java.lang.String[])

CLSS public final org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils
meth public com.sun.source.tree.AnnotationTree createAnnotation(java.lang.String)
meth public com.sun.source.tree.AnnotationTree createAnnotation(java.lang.String,java.util.List<? extends com.sun.source.tree.ExpressionTree>)
meth public com.sun.source.tree.ClassTree addAnnotation(com.sun.source.tree.ClassTree,com.sun.source.tree.AnnotationTree)
meth public com.sun.source.tree.ClassTree addClassFields(com.sun.source.tree.ClassTree,java.util.List<? extends com.sun.source.tree.VariableTree>)
meth public com.sun.source.tree.ClassTree addImplementsClause(com.sun.source.tree.ClassTree,java.lang.String)
meth public com.sun.source.tree.ClassTree ensureNoArgConstructor(com.sun.source.tree.ClassTree)
meth public com.sun.source.tree.ExpressionTree createAnnotationArgument(java.lang.String,java.lang.Object)
meth public com.sun.source.tree.ExpressionTree createAnnotationArgument(java.lang.String,java.lang.String,java.lang.String)
meth public com.sun.source.tree.ExpressionTree createAnnotationArgument(java.lang.String,java.util.List<? extends com.sun.source.tree.ExpressionTree>)
meth public com.sun.source.tree.MethodTree addAnnotation(com.sun.source.tree.MethodTree,com.sun.source.tree.AnnotationTree)
meth public com.sun.source.tree.MethodTree createAssignmentConstructor(com.sun.source.tree.ModifiersTree,java.lang.String,java.util.List<com.sun.source.tree.VariableTree>)
meth public com.sun.source.tree.MethodTree createPropertyGetterMethod(com.sun.source.tree.ModifiersTree,java.lang.String,com.sun.source.tree.Tree)
meth public com.sun.source.tree.MethodTree createPropertyGetterMethod(javax.lang.model.element.TypeElement,com.sun.source.tree.ModifiersTree,java.lang.String,java.lang.String)
meth public com.sun.source.tree.MethodTree createPropertySetterMethod(com.sun.source.tree.ModifiersTree,java.lang.String,com.sun.source.tree.Tree)
meth public com.sun.source.tree.MethodTree createPropertySetterMethod(javax.lang.model.element.TypeElement,com.sun.source.tree.ModifiersTree,java.lang.String,java.lang.String)
meth public com.sun.source.tree.ModifiersTree createModifiers(javax.lang.model.element.Modifier)
meth public com.sun.source.tree.Tree createType(java.lang.String,javax.lang.model.element.TypeElement)
meth public com.sun.source.tree.VariableTree addAnnotation(com.sun.source.tree.VariableTree,com.sun.source.tree.AnnotationTree)
meth public com.sun.source.tree.VariableTree createField(javax.lang.model.element.TypeElement,com.sun.source.tree.ModifiersTree,java.lang.String,java.lang.String,com.sun.source.tree.ExpressionTree)
meth public com.sun.source.tree.VariableTree createVariable(java.lang.String,com.sun.source.tree.Tree)
meth public com.sun.source.tree.VariableTree createVariable(javax.lang.model.element.TypeElement,java.lang.String,java.lang.String)
meth public com.sun.source.tree.VariableTree removeModifiers(com.sun.source.tree.VariableTree)
meth public static org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils newInstance(org.netbeans.api.java.source.WorkingCopy)
meth public static org.openide.filesystems.FileObject createClass(java.lang.String,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public static org.openide.filesystems.FileObject createClass(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject createInterface(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds CLASS_TEMPLATE,INTERFACE_TEMPLATE,copy

CLSS public final org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers
meth public static boolean isValidPackageName(java.lang.String)
meth public static java.lang.String getPackageName(java.lang.String)
meth public static java.lang.String getQualifiedName(org.openide.filesystems.FileObject)
meth public static java.lang.String unqualify(java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.core.api.support.java.SourceUtils
meth public static boolean isSubtype(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement,java.lang.String)
meth public static com.sun.source.tree.ClassTree getPublicTopLevelTree(org.netbeans.api.java.source.CompilationController)
meth public static javax.lang.model.element.TypeElement getPublicTopLevelElement(org.netbeans.api.java.source.CompilationController)
meth public static org.openide.filesystems.FileObject getFileObject(java.lang.String,org.netbeans.api.java.source.ClasspathInfo) throws java.io.IOException
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer
cons protected init(java.lang.String,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.netbeans.api.java.source.ClasspathInfo,boolean,boolean,boolean,boolean,boolean,java.lang.String,boolean,boolean,boolean,boolean,java.lang.String,java.util.Collection<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel>)
cons protected init(java.lang.String,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.netbeans.api.java.source.ClasspathInfo,boolean,boolean,boolean,boolean,boolean,java.lang.String,boolean,boolean,boolean,java.lang.String,java.util.Collection<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel>)
meth public boolean customizeMethod()
meth public boolean finderReturnIsSingle()
meth public boolean publishToLocal()
meth public boolean publishToRemote()
meth public java.lang.String getEjbQL()
meth public org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel getMethodModel()
supr java.lang.Object
hfds existingMethods,panel,prefix,title

CLSS public final org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizerFactory
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer businessMethod(java.lang.String,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.netbeans.api.java.source.ClasspathInfo,boolean,boolean,boolean,boolean,boolean,java.util.Collection<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel>)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer createMethod(java.lang.String,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.netbeans.api.java.source.ClasspathInfo,boolean,boolean,boolean,boolean,java.util.Collection<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel>)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer finderMethod(java.lang.String,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.netbeans.api.java.source.ClasspathInfo,boolean,boolean,boolean,boolean,java.lang.String,java.util.Collection<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel>)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer homeMethod(java.lang.String,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.netbeans.api.java.source.ClasspathInfo,boolean,boolean,boolean,boolean,java.util.Collection<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel>)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer operationMethod(java.lang.String,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.netbeans.api.java.source.ClasspathInfo,java.util.Collection<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel>)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer selectMethod(java.lang.String,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,org.netbeans.api.java.source.ClasspathInfo,java.lang.String,java.util.Collection<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel>)
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel
innr public final static Annotation
innr public final static Variable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBody()
meth public java.lang.String getName()
meth public java.lang.String getReturnType()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getExceptions()
meth public java.util.List<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Annotation> getAnnotations()
meth public java.util.List<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Variable> getParameters()
meth public java.util.Set<javax.lang.model.element.Modifier> getModifiers()
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel create(java.lang.String,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Variable>,java.util.List<java.lang.String>,java.util.Set<javax.lang.model.element.Modifier>)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel create(java.lang.String,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Variable>,java.util.List<java.lang.String>,java.util.Set<javax.lang.model.element.Modifier>,java.util.List<org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Annotation>)
supr java.lang.Object
hfds annotations,body,exceptions,modifiers,name,parameters,returnType

CLSS public final static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Annotation
 outer org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel
meth public java.lang.String getType()
meth public java.util.Map<java.lang.String,java.lang.Object> getArguments()
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Annotation create(java.lang.String)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Annotation create(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object
hfds arguments,type

CLSS public final static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Variable
 outer org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel
meth public boolean equals(java.lang.Object)
meth public boolean getFinalModifier()
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Variable create(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Variable create(java.lang.String,java.lang.String,boolean)
supr java.lang.Object
hfds finalModifier,name,type

CLSS public final org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport
meth public static boolean isSameMethod(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.ExecutableElement,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel)
meth public static com.sun.source.tree.MethodTree createMethodTree(org.netbeans.api.java.source.WorkingCopy,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel)
meth public static com.sun.source.tree.MethodTree createMethodTree(org.netbeans.api.java.source.WorkingCopy,org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel,boolean)
meth public static java.lang.String getDefaultReturnValue(org.netbeans.api.java.source.WorkingCopy,java.lang.String)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel createMethodModel(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.ExecutableElement)
meth public static org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel$Variable createVariable(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.VariableElement)
supr java.lang.Object
hfds CAPTURED_WILDCARD,LOG,UNKNOWN
hcls TypeNameVisitor

CLSS public final org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport
innr public abstract static Action
innr public abstract static BackgroundAction
innr public abstract static EventThreadAction
innr public final static Context
meth public static boolean invoke(java.util.Collection<? extends org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$Action>,boolean)
meth public static void invoke(java.util.Collection<? extends org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$Action>)
supr java.lang.Object
hfds LOGGER
hcls ActionInvoker

CLSS public abstract static org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$Action
 outer org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport
cons public init()
meth protected abstract boolean isBackground()
meth protected abstract void run(org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$Context)
meth protected boolean cancel()
meth protected boolean isCancellable()
meth protected boolean isEnabled()
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$BackgroundAction
 outer org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport
cons public init()
cons public init(boolean)
meth protected final boolean isCancellable()
meth public final boolean isBackground()
supr org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$Action
hfds cancellable

CLSS public final static org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$Context
 outer org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport
meth public void progress(int)
meth public void progress(java.lang.String)
meth public void switchToDeterminate(int)
supr java.lang.Object
hfds handle,panel

CLSS public abstract static org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$EventThreadAction
 outer org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport
cons public init()
cons public init(boolean)
meth protected final boolean isCancellable()
meth public final boolean isBackground()
supr org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport$Action
hfds cancellable

CLSS public org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel<%0 extends java.lang.Object>
cons public init(org.openide.WizardDescriptor$Panel<{org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel%0}>)
intf org.openide.WizardDescriptor$FinishablePanel<{org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel%0}>
meth protected org.netbeans.api.project.Project getProject()
meth protected org.openide.WizardDescriptor getWizardDescriptor()
meth public boolean isFinishPanel()
meth public boolean isValid()
meth public java.awt.Component getComponent()
meth public org.openide.util.HelpCtx getHelp()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void readSettings({org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel%0})
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void storeSettings({org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel%0})
supr java.lang.Object
hfds delegate,project,wizardDescriptor

CLSS public final org.netbeans.modules.j2ee.core.api.support.wizard.Wizards
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

