#Signature file v4.1
#Version 6.62

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface java.util.EventListener

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

CLSS public abstract org.openide.actions.ActionManager
cons public init()
fld public final static java.lang.String PROP_CONTEXT_ACTIONS = "contextActions"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract org.openide.util.actions.SystemAction[] getContextActions()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static org.openide.actions.ActionManager getDefault()
meth public void invokeAction(javax.swing.Action,java.awt.event.ActionEvent)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds supp
hcls Trivial

CLSS public org.openide.actions.CloneViewAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.CloseViewAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.CopyAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.CustomizeAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction

CLSS public org.openide.actions.CutAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.DeleteAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.EditAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean surviveFocusChange()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.FindAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.GarbageCollectAction
cons public init()
meth protected boolean asynchronous()
meth public java.awt.Component getToolbarPresenter()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds NIMBUS_LAF,RP
hcls HeapViewWrapper

CLSS public org.openide.actions.GotoAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public final org.openide.actions.MoveDownAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void initialize()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds PROP_ORDER_LISTENER,curIndexCookie
hcls OrderingListener

CLSS public final org.openide.actions.MoveUpAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void initialize()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds PROP_ORDER_LISTENER,curIndexCookie,err
hcls OrderingListener

CLSS public final org.openide.actions.NewAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds model
hcls ActSubMenuModel,DelegateAction

CLSS public org.openide.actions.NextTabAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.OpenAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean surviveFocusChange()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public final org.openide.actions.OpenLocalExplorerAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction

CLSS public final org.openide.actions.PageSetupAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction

CLSS public final org.openide.actions.PasteAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public java.lang.Object getActionMapKey()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.datatransfer.PasteType[] getPasteTypes()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void setPasteTypes(org.openide.util.datatransfer.PasteType[])
 anno 0 java.lang.Deprecated()
supr org.openide.util.actions.CallbackSystemAction
hfds globalModel,types
hcls ActSubMenuModel,ActionPT,DelegateAction,NodeSelector

CLSS public final org.openide.actions.PopupAction
cons public init()
meth protected boolean asynchronous()
meth protected void initialize()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.PreviousTabAction
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.PrintAction
cons public init()
meth protected boolean asynchronous()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.PropertiesAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hcls DelegateAction

CLSS public org.openide.actions.RedoAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public boolean isEnabled()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds SWING_DEFAULT_LABEL

CLSS public org.openide.actions.RenameAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected boolean surviveFocusChange()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds RP

CLSS public org.openide.actions.ReorderAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean surviveFocusChange()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.ReplaceAction
cons public init()
meth protected boolean asynchronous()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.SaveAction
cons public init()
meth protected boolean asynchronous()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected java.lang.String iconResource()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction
hfds dataObject,getNodeDelegate
hcls Delegate

CLSS public org.openide.actions.ToolsAction
cons public init()
innr public abstract interface static Model
intf org.openide.util.ContextAwareAction
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
meth protected void initialize()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void setModel(org.openide.actions.ToolsAction$Model)
 anno 0 java.lang.Deprecated()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds serialVersionUID,taskGl
hcls DelegateAction,G,Inline,Popup

CLSS public abstract interface static org.openide.actions.ToolsAction$Model
 outer org.openide.actions.ToolsAction
 anno 0 java.lang.Deprecated()
meth public abstract org.openide.util.actions.SystemAction[] getActions()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.openide.actions.UndoAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public boolean isEnabled()
meth public java.lang.String getName()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction
hfds SWING_DEFAULT_LABEL,last,listener,redoAction,undoAction
hcls Listener

CLSS public org.openide.actions.UndockAction
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean asynchronous()
meth protected java.lang.String iconResource()
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CallbackSystemAction

CLSS public org.openide.actions.ViewAction
cons public init()
meth protected boolean asynchronous()
meth protected boolean surviveFocusChange()
meth protected int mode()
meth protected java.lang.Class[] cookieClasses()
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.CookieAction

CLSS public org.openide.actions.WorkspaceSwitchAction
 anno 0 java.lang.Deprecated()
cons public init()
meth public java.lang.String getName()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void performAction()
supr org.openide.util.actions.CallableSystemAction

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

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

CLSS public abstract org.openide.util.SharedClassObject
cons protected init()
intf java.io.Externalizable
meth protected boolean clearSharedData()
meth protected final java.lang.Object getLock()
meth protected final java.lang.Object getProperty(java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void finalize() throws java.lang.Throwable
meth protected java.lang.Object writeReplace()
meth protected void addNotify()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth protected void removeNotify()
meth protected void reset()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>,boolean)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds PROP_SUPPORT,addNotifySuper,alreadyWarnedAboutDupes,dataEntry,err,first,firstTrace,inReadExternal,initializeSuper,instancesBeingCreated,lock,prematureSystemOptionMutation,removeNotifySuper,serialVersionUID,systemOption,values,waitingOnSystemOption
hcls DataEntry,SetAccessibleAction,WriteReplace

CLSS public abstract org.openide.util.actions.CallableSystemAction
cons public init()
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean asynchronous()
meth public abstract void performAction()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds DEFAULT_ASYNCH,serialVersionUID,warnedAsynchronousActions

CLSS public abstract org.openide.util.actions.CallbackSystemAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected void initialize()
meth public boolean getSurviveFocusChange()
meth public java.lang.Object getActionMapKey()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.actions.ActionPerformer getActionPerformer()
 anno 0 java.lang.Deprecated()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void performAction()
 anno 0 java.lang.Deprecated()
meth public void setActionPerformer(org.openide.util.actions.ActionPerformer)
 anno 0 java.lang.Deprecated()
meth public void setSurviveFocusChange(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds LISTENER,PROP_ACTION_PERFORMER,err,notSurviving,serialVersionUID,surviving
hcls ActionDelegateListener,DelegateAction,GlobalManager,WeakAction

CLSS public abstract org.openide.util.actions.CookieAction
cons public init()
fld public final static int MODE_ALL = 4
fld public final static int MODE_ANY = 7
fld public final static int MODE_EXACTLY_ONE = 8
fld public final static int MODE_ONE = 1
fld public final static int MODE_SOME = 2
meth protected abstract int mode()
meth protected abstract java.lang.Class<?>[] cookieClasses()
meth protected boolean enable(org.openide.nodes.Node[])
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
supr org.openide.util.actions.NodeAction
hfds PROP_COOKIES,listener,serialVersionUID
hcls CookieDelegateAction,CookiesChangeListener

CLSS public abstract org.openide.util.actions.NodeAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected abstract boolean enable(org.openide.nodes.Node[])
meth protected abstract void performAction(org.openide.nodes.Node[])
meth protected boolean surviveFocusChange()
meth protected void addNotify()
meth protected void initialize()
meth protected void removeNotify()
meth public boolean isEnabled()
meth public final org.openide.nodes.Node[] getActivatedNodes()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public void actionPerformed(java.awt.event.ActionEvent)
 anno 0 java.lang.Deprecated()
meth public void performAction()
 anno 0 java.lang.Deprecated()
meth public void setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds PROP_HAS_LISTENERS,PROP_LAST_ENABLED,PROP_LAST_NODES,l,listeningActions,serialVersionUID
hcls DelegateAction,NodesL

CLSS public abstract interface org.openide.util.actions.Presenter
innr public abstract interface static Menu
innr public abstract interface static Popup
innr public abstract interface static Toolbar

CLSS public abstract interface static org.openide.util.actions.Presenter$Menu
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getMenuPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Popup
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getPopupPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Toolbar
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract java.awt.Component getToolbarPresenter()

CLSS public abstract org.openide.util.actions.SystemAction
cons public init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_ICON = "icon"
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
meth protected boolean clearSharedData()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public abstract java.lang.String getName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void actionPerformed(java.awt.event.ActionEvent)
meth public boolean isEnabled()
meth public final java.lang.Object getValue(java.lang.String)
meth public final javax.swing.Icon getIcon()
meth public final javax.swing.Icon getIcon(boolean)
meth public final void putValue(java.lang.String,java.lang.Object)
meth public final void setIcon(javax.swing.Icon)
meth public static <%0 extends org.openide.util.actions.SystemAction> {%%0} get(java.lang.Class<{%%0}>)
meth public static javax.swing.JPopupMenu createPopupMenu(org.openide.util.actions.SystemAction[])
 anno 0 java.lang.Deprecated()
meth public static javax.swing.JToolBar createToolbarPresenter(org.openide.util.actions.SystemAction[])
meth public static org.openide.util.actions.SystemAction[] linkActions(org.openide.util.actions.SystemAction[],org.openide.util.actions.SystemAction[])
meth public void setEnabled(boolean)
supr org.openide.util.SharedClassObject
hfds LOG,PROP_ICON_TEXTUAL,relativeIconResourceClasses,serialVersionUID
hcls ComponentIcon

