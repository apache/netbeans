#Signature file v4.1
#Version 0.48

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract javax.swing.AbstractAction
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
fld protected boolean enabled
fld protected javax.swing.event.SwingPropertyChangeSupport changeSupport
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.Action
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object[] getKeys()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public final org.netbeans.modules.php.api.framework.BadgeIcon
cons public init(java.awt.Image,java.net.URL)
meth public java.awt.Image getImage()
meth public java.net.URL getUrl()
supr java.lang.Object
hfds image,url

CLSS public final org.netbeans.modules.php.api.framework.PhpFrameworks
fld public final static java.lang.String FRAMEWORK_PATH = "PHP/Frameworks"
meth public static java.util.List<org.netbeans.modules.php.spi.framework.PhpFrameworkProvider> getFrameworks()
meth public static void addFrameworksListener(org.openide.util.LookupListener)
meth public static void removeFrameworksListener(org.openide.util.LookupListener)
supr java.lang.Object
hfds FRAMEWORKS

CLSS public abstract org.netbeans.modules.php.spi.framework.PhpFrameworkProvider
cons public init(java.lang.String,java.lang.String,java.lang.String)
innr public abstract interface static !annotation Registration
meth public abstract boolean isInPhpModule(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public abstract org.netbeans.modules.php.api.phpmodule.PhpModuleProperties getPhpModuleProperties(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public abstract org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender getActionsExtender(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public abstract org.netbeans.modules.php.spi.framework.PhpModuleExtender createPhpModuleExtender(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public abstract org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public abstract org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport getFrameworkCommandSupport(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public final java.lang.String getDescription()
meth public final java.lang.String getIdentifier()
meth public final java.lang.String getName()
meth public java.io.File[] getConfigurationFiles(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getName(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider> getAnnotationsCompletionTagProviders(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public org.netbeans.modules.php.api.framework.BadgeIcon getBadgeIcon()
meth public org.netbeans.modules.php.spi.editor.EditorExtender getEditorExtender(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation getConfigurationFiles2(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void phpModuleClosed(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public void phpModuleOpened(org.netbeans.modules.php.api.phpmodule.PhpModule)
supr java.lang.Object
hfds description,identifier,name

CLSS public abstract interface static !annotation org.netbeans.modules.php.spi.framework.PhpFrameworkProvider$Registration
 outer org.netbeans.modules.php.spi.framework.PhpFrameworkProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()

CLSS public abstract org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender
cons public init()
meth public boolean isActionWithView(org.openide.filesystems.FileObject)
meth public boolean isViewWithAction(org.openide.filesystems.FileObject)
meth public java.lang.String getMenuName()
meth public java.util.List<? extends javax.swing.Action> getActions()
meth public org.netbeans.modules.php.spi.framework.actions.GoToActionAction getGoToActionAction(org.openide.filesystems.FileObject,int)
meth public org.netbeans.modules.php.spi.framework.actions.GoToViewAction getGoToViewAction(org.openide.filesystems.FileObject,int)
meth public org.netbeans.modules.php.spi.framework.actions.RunCommandAction getRunCommandAction()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender
cons public init()
innr public final static !enum Change
meth public abstract boolean isValid()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getErrorMessage()
meth public abstract java.util.EnumSet<org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change> save(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.HelpCtx getHelp()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public java.lang.String getDisplayName(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getWarningMessage()
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change
 outer org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender
fld public final static org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change FRAMEWORK_CHANGE
fld public final static org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change IGNORED_FILES_CHANGE
fld public final static org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change SELENIUM_CHANGE
fld public final static org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change SOURCES_CHANGE
fld public final static org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change TESTS_CHANGE
meth public static org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change valueOf(java.lang.String)
meth public static org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change[] values()
supr java.lang.Enum<org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender$Change>

CLSS public abstract org.netbeans.modules.php.spi.framework.PhpModuleExtender
cons public init()
innr public final static ExtendingException
meth public abstract boolean isValid()
meth public abstract java.lang.String getErrorMessage()
meth public abstract java.lang.String getWarningMessage()
meth public abstract java.util.Set<org.openide.filesystems.FileObject> extend(org.netbeans.modules.php.api.phpmodule.PhpModule) throws org.netbeans.modules.php.spi.framework.PhpModuleExtender$ExtendingException
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.HelpCtx getHelp()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public final static org.netbeans.modules.php.spi.framework.PhpModuleExtender$ExtendingException
 outer org.netbeans.modules.php.spi.framework.PhpModuleExtender
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth public java.lang.String getFailureMessage()
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender
cons public init()
meth public abstract java.util.Set<java.io.File> getIgnoredFiles()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.php.spi.framework.actions.BaseAction
cons protected init()
intf org.openide.util.HelpCtx$Provider
meth protected abstract java.lang.String getFullName()
meth protected abstract java.lang.String getPureName()
meth protected abstract void actionPerformed(org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public org.openide.util.HelpCtx getHelpCtx()
supr javax.swing.AbstractAction

CLSS public abstract org.netbeans.modules.php.spi.framework.actions.GoToActionAction
cons public init()
fld protected final static int DEFAULT_OFFSET = 0
meth public abstract boolean goToAction()
meth public final void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds RP

CLSS public abstract org.netbeans.modules.php.spi.framework.actions.GoToViewAction
cons public init()
fld protected final static int DEFAULT_OFFSET = 0
meth public abstract boolean goToView()
meth public final void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds RP

CLSS public abstract org.netbeans.modules.php.spi.framework.actions.RunCommandAction
cons public init()
meth protected final java.lang.String getPureName()
meth public abstract void actionPerformed(org.netbeans.modules.php.api.phpmodule.PhpModule)
supr org.netbeans.modules.php.spi.framework.actions.BaseAction

CLSS public abstract org.netbeans.modules.php.spi.framework.commands.FrameworkCommand
cons protected init(java.lang.String,java.lang.String,java.lang.String)
cons protected init(java.lang.String[],java.lang.String,java.lang.String)
intf java.lang.Comparable<org.netbeans.modules.php.spi.framework.commands.FrameworkCommand>
meth protected abstract java.lang.String getHelpInternal()
meth public final boolean equals(java.lang.Object)
meth public final boolean hasHelp()
meth public final int compareTo(org.netbeans.modules.php.spi.framework.commands.FrameworkCommand)
meth public final int hashCode()
meth public final java.lang.String getDescription()
meth public final java.lang.String getDisplayName()
meth public final java.lang.String getHelp()
meth public final java.lang.String toString()
meth public final java.lang.String[] getCommands()
meth public java.lang.String getPreview()
supr java.lang.Object
hfds commands,description,displayName,help

CLSS public abstract org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport
cons protected init(org.netbeans.modules.php.api.phpmodule.PhpModule)
fld protected final org.netbeans.modules.php.api.phpmodule.PhpModule phpModule
innr public final static CommandDescriptor
meth protected abstract java.io.File getPluginsDirectory()
meth protected abstract java.lang.String getOptionsPath()
meth protected abstract java.util.List<org.netbeans.modules.php.spi.framework.commands.FrameworkCommand> getFrameworkCommandsInternal()
meth public abstract java.lang.String getFrameworkName()
meth public abstract void runCommand(org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport$CommandDescriptor,java.lang.Runnable)
meth public final java.util.List<org.netbeans.modules.php.spi.framework.commands.FrameworkCommand> getFrameworkCommands()
meth public final org.netbeans.modules.php.api.phpmodule.PhpModule getPhpModule()
meth public final void openPanel()
meth public final void refreshFrameworkCommandsLater(java.lang.Runnable)
supr java.lang.Object
hfds COMMANDS_CACHE,LOGGER,RP,pluginListener
hcls PluginListener

CLSS public final static org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport$CommandDescriptor
 outer org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport
cons public init(org.netbeans.modules.php.spi.framework.commands.FrameworkCommand,java.lang.String,boolean)
meth public boolean isDebug()
meth public java.lang.String[] getCommandParams()
meth public org.netbeans.modules.php.spi.framework.commands.FrameworkCommand getFrameworkCommand()
supr java.lang.Object
hfds debug,params,task

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

