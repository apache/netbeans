#Signature file v4.1
#Version 2.4.1

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

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
hfds RECONFIGURE_ON_NULL,arrayTable

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

CLSS public abstract interface javax.swing.text.Document
fld public final static java.lang.String StreamDescriptionProperty = "stream"
fld public final static java.lang.String TitleProperty = "title"
meth public abstract int getLength()
meth public abstract java.lang.Object getProperty(java.lang.Object)
meth public abstract java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element[] getRootElements()
meth public abstract javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Position getEndPosition()
meth public abstract javax.swing.text.Position getStartPosition()
meth public abstract void addDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public abstract void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public abstract void putProperty(java.lang.Object,java.lang.Object)
meth public abstract void remove(int,int) throws javax.swing.text.BadLocationException
meth public abstract void removeDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void render(java.lang.Runnable)

CLSS public abstract javax.swing.text.TextAction
cons public init(java.lang.String)
meth protected final javax.swing.text.JTextComponent getFocusedComponent()
meth protected final javax.swing.text.JTextComponent getTextComponent(java.awt.event.ActionEvent)
meth public final static javax.swing.Action[] augmentList(javax.swing.Action[],javax.swing.Action[])
supr javax.swing.AbstractAction

CLSS public final org.netbeans.api.editor.DialogBinding
cons public init()
meth public static void bindComponentToDocument(javax.swing.text.Document,int,int,int,javax.swing.text.JTextComponent)
meth public static void bindComponentToDocument(javax.swing.text.Document,int,int,javax.swing.text.JTextComponent)
meth public static void bindComponentToFile(org.openide.filesystems.FileObject,int,int,int,javax.swing.text.JTextComponent)
meth public static void bindComponentToFile(org.openide.filesystems.FileObject,int,int,javax.swing.text.JTextComponent)
supr java.lang.Object
hfds LOG

CLSS public final org.netbeans.api.editor.EditorActionNames
fld public final static java.lang.String gotoDeclaration = "goto-declaration"
fld public final static java.lang.String moveCodeElementDown = "move-code-element-down"
fld public final static java.lang.String moveCodeElementUp = "move-code-element-up"
fld public final static java.lang.String organizeImports = "organize-imports"
fld public final static java.lang.String organizeMembers = "organize-members"
fld public final static java.lang.String removeSurroundingCode = "remove-surrounding-code"
fld public final static java.lang.String toggleLineNumbers = "toggle-line-numbers"
fld public final static java.lang.String toggleNonPrintableCharacters = "toggle-non-printable-characters"
fld public final static java.lang.String toggleRectangularSelection = "toggle-rectangular-selection"
fld public final static java.lang.String toggleToolbar = "toggle-toolbar"
fld public final static java.lang.String transposeLetters = "transpose-letters"
fld public final static java.lang.String zoomTextIn = "zoom-text-in"
fld public final static java.lang.String zoomTextOut = "zoom-text-out"
supr java.lang.Object

CLSS public abstract interface !annotation org.netbeans.api.editor.EditorActionRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean noIconInMenu()
meth public abstract !hasdefault boolean noKeyBinding()
meth public abstract !hasdefault boolean preferencesDefault()
meth public abstract !hasdefault int menuPosition()
meth public abstract !hasdefault int popupPosition()
meth public abstract !hasdefault int toolBarPosition()
meth public abstract !hasdefault int weight()
meth public abstract !hasdefault java.lang.String iconResource()
meth public abstract !hasdefault java.lang.String menuPath()
meth public abstract !hasdefault java.lang.String menuText()
meth public abstract !hasdefault java.lang.String mimeType()
meth public abstract !hasdefault java.lang.String popupPath()
meth public abstract !hasdefault java.lang.String popupText()
meth public abstract !hasdefault java.lang.String preferencesKey()
meth public abstract !hasdefault java.lang.String shortDescription()
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation org.netbeans.api.editor.EditorActionRegistrations
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.api.editor.EditorActionRegistration[] value()

CLSS public final org.netbeans.api.editor.EditorRegistry
fld public final static java.lang.String COMPONENT_REMOVED_PROPERTY = "componentRemoved"
fld public final static java.lang.String FOCUSED_DOCUMENT_PROPERTY = "focusedDocument"
fld public final static java.lang.String FOCUS_GAINED_PROPERTY = "focusGained"
fld public final static java.lang.String FOCUS_LOST_PROPERTY = "focusLost"
fld public final static java.lang.String LAST_FOCUSED_REMOVED_PROPERTY = "lastFocusedRemoved"
meth public static java.util.List<? extends javax.swing.text.JTextComponent> componentList()
meth public static javax.swing.text.JTextComponent focusedComponent()
meth public static javax.swing.text.JTextComponent lastFocusedComponent()
meth public static void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOG,USED_BY_CLONEABLE_EDITOR_PROPERTY,ignoredAncestorClass,items,pcs
hcls AncestorL,CloneableEditorUsageL,FocusL,Item,PackageAccessor,PropertyDocL

CLSS public final org.netbeans.api.editor.EditorUtilities
meth public static javax.swing.Action getAction(javax.swing.text.EditorKit,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.api.editor.NavigationHistory
fld public final static java.lang.String PROP_WAYPOINTS = "NavigationHHistory.PROP_WAYPOINTS"
innr public final static Waypoint
meth public boolean hasNextWaypoints()
meth public boolean hasPreviousWaypoints()
meth public java.util.List<org.netbeans.api.editor.NavigationHistory$Waypoint> getNextWaypoints()
meth public java.util.List<org.netbeans.api.editor.NavigationHistory$Waypoint> getPreviousWaypoints()
meth public org.netbeans.api.editor.NavigationHistory$Waypoint getCurrentWaypoint()
meth public org.netbeans.api.editor.NavigationHistory$Waypoint markWaypoint(javax.swing.text.JTextComponent,int,boolean,boolean) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.editor.NavigationHistory$Waypoint navigateBack()
meth public org.netbeans.api.editor.NavigationHistory$Waypoint navigateFirst()
meth public org.netbeans.api.editor.NavigationHistory$Waypoint navigateForward()
meth public org.netbeans.api.editor.NavigationHistory$Waypoint navigateLast()
meth public org.netbeans.api.editor.NavigationHistory$Waypoint navigateTo(org.netbeans.api.editor.NavigationHistory$Waypoint)
meth public static org.netbeans.api.editor.NavigationHistory getEdits()
meth public static org.netbeans.api.editor.NavigationHistory getNavigations()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOCK,LOG,PCS,id,instances,pointer,sublistsCache,waypoints
hcls RingBuffer

CLSS public final static org.netbeans.api.editor.NavigationHistory$Waypoint
 outer org.netbeans.api.editor.NavigationHistory
meth public int getOffset()
meth public java.net.URL getUrl()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds compRef,navigationHistory,pos,rawIndex,url

CLSS public abstract interface org.netbeans.api.editor.document.AtomicLockDocument
meth public abstract javax.swing.text.Document getDocument()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void atomicUndo()
meth public abstract void removeAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void runAtomic(java.lang.Runnable)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void runAtomicAsUser(java.lang.Runnable)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.api.editor.document.AtomicLockEvent
cons public init(javax.swing.text.Document)
meth public javax.swing.text.Document getDocument()
meth public org.netbeans.api.editor.document.AtomicLockDocument getAtomicLock()
supr java.util.EventObject

CLSS public abstract interface org.netbeans.api.editor.document.AtomicLockListener
intf java.util.EventListener
meth public abstract void atomicLock(org.netbeans.api.editor.document.AtomicLockEvent)
meth public abstract void atomicUnlock(org.netbeans.api.editor.document.AtomicLockEvent)

CLSS public abstract interface org.netbeans.api.editor.document.DocumentLockState
meth public abstract boolean isReadLocked()
meth public abstract boolean isWriteLocked()

CLSS public final org.netbeans.api.editor.document.EditorDocumentUtils
meth public static org.openide.filesystems.FileObject getFileObject(javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void runExclusive(javax.swing.text.Document,java.lang.Runnable)
supr java.lang.Object
hfds LOG

CLSS public final org.netbeans.api.editor.document.EditorMimeTypes
fld public final static java.lang.String PROP_SUPPORTED_MIME_TYPES = "supportedMimeTypes"
meth public java.util.Set<java.lang.String> getSupportedMimeTypes()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.editor.document.EditorMimeTypes getDefault()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds impl,instance,listener,listeners

CLSS public abstract interface org.netbeans.api.editor.document.LineDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public final org.netbeans.api.editor.document.LineDocumentUtils
meth public static <%0 extends java.lang.Object> {%%0} as(javax.swing.text.Document,java.lang.Class<{%%0}>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static <%0 extends java.lang.Object> {%%0} asRequired(javax.swing.text.Document,java.lang.Class<{%%0}>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isLineEmpty(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isLineWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineCount(org.netbeans.api.editor.document.LineDocument)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineCount(org.netbeans.api.editor.document.LineDocument,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineEnd(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineFirstNonWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineIndex(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineLastNonWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineStart(org.netbeans.api.editor.document.LineDocument,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineStartFromIndex(org.netbeans.api.editor.document.LineDocument,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextNonNewline(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextNonWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextNonWhitespace(org.netbeans.api.editor.document.LineDocument,int,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextWhitespace(org.netbeans.api.editor.document.LineDocument,int,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextWordStart(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousNonNewline(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousNonWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousNonWhitespace(org.netbeans.api.editor.document.LineDocument,int,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousWhitespace(org.netbeans.api.editor.document.LineDocument,int,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousWordEnd(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousWordStart(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getWordEnd(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getWordStart(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getWord(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.editor.document.LineDocument createDocument(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds NOT_FOUND,WRONG_POSITION_LOCALE
hcls V

CLSS public abstract org.netbeans.modules.editor.lib2.document.DocumentSpiPackageAccessor
cons public init()
meth public abstract org.netbeans.spi.editor.document.OnSaveTask$Context createContext(javax.swing.text.Document)
meth public abstract void setTaskStarted(org.netbeans.spi.editor.document.OnSaveTask$Context,boolean)
meth public abstract void setUndoEdit(org.netbeans.spi.editor.document.OnSaveTask$Context,javax.swing.undo.UndoableEdit)
meth public static org.netbeans.modules.editor.lib2.document.DocumentSpiPackageAccessor get()
meth public static void register(org.netbeans.modules.editor.lib2.document.DocumentSpiPackageAccessor)
supr java.lang.Object
hfds INSTANCE

CLSS public abstract org.netbeans.spi.editor.AbstractEditorAction
cons protected init()
cons protected init(java.util.Map<java.lang.String,?>)
fld public final static java.lang.String ASYNCHRONOUS_KEY = "asynchronous"
fld public final static java.lang.String DISPLAY_NAME_KEY = "displayName"
fld public final static java.lang.String ICON_RESOURCE_KEY = "iconBase"
fld public final static java.lang.String MENU_TEXT_KEY = "menuText"
fld public final static java.lang.String MIME_TYPE_KEY = "mimeType"
fld public final static java.lang.String MULTI_ACCELERATOR_LIST_KEY = "MultiAcceleratorListKey"
fld public final static java.lang.String NO_ICON_IN_MENU = "noIconInMenu"
fld public final static java.lang.String NO_KEY_BINDING = "no-keybinding"
fld public final static java.lang.String POPUP_TEXT_KEY = "popupText"
fld public final static java.lang.String PREFERENCES_DEFAULT_KEY = "preferencesDefault"
fld public final static java.lang.String PREFERENCES_KEY_KEY = "preferencesKey"
fld public final static java.lang.String PREFERENCES_NODE_KEY = "preferencesNode"
fld public final static java.lang.String WRAPPER_ACTION_KEY = "WrapperActionKey"
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected abstract void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
meth protected boolean asynchronous()
meth protected final java.lang.String actionName()
meth protected final void resetCaretMagicPosition(javax.swing.text.JTextComponent)
meth protected java.lang.Object createValue(java.lang.String)
meth protected void valuesUpdated()
meth public boolean isEnabled()
meth public final java.lang.Object getValue(java.lang.String)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public final void putValue(java.lang.String,java.lang.Object)
meth public java.awt.Component getToolbarPresenter()
meth public java.lang.Object[] getKeys()
meth public java.lang.String toString()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void setEnabled(boolean)
supr javax.swing.text.TextAction
hfds LOG,LOGGED_ACTION_NAMES,MASK_NULL_VALUE,UILOG,UNITIALIZED_ACTION,attrs,delegateAction,preferencesNodeAndListener,properties,serialVersionUID
hcls DelegateActionPropertyChangeListener,PreferencesNodeAndListener

CLSS public abstract interface org.netbeans.spi.editor.SideBarFactory
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="SideBar")
meth public abstract javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent)

CLSS public abstract interface org.netbeans.spi.editor.codegen.CodeGenerator
innr public abstract interface static Factory
meth public abstract java.lang.String getDisplayName()
meth public abstract void invoke()

CLSS public abstract interface static org.netbeans.spi.editor.codegen.CodeGenerator$Factory
 outer org.netbeans.spi.editor.codegen.CodeGenerator
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="CodeGenerators")
meth public abstract java.util.List<? extends org.netbeans.spi.editor.codegen.CodeGenerator> create(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="CodeGeneratorContextProviders")
innr public abstract interface static Task
meth public abstract void runTaskWithinContext(org.openide.util.Lookup,org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider$Task)

CLSS public abstract interface static org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider$Task
 outer org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider
meth public abstract void run(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.spi.editor.document.DocumentFactory
meth public abstract javax.swing.text.Document createDocument(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.swing.text.Document getDocument(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFileObject(javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.editor.document.EditorMimeTypesImplementation
fld public final static java.lang.String PROP_SUPPORTED_MIME_TYPES = "supportedMimeTypes"
meth public abstract java.util.Set<java.lang.String> getSupportedMimeTypes()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.editor.document.OnSaveTask
innr public abstract interface static Factory
innr public final static Context
innr public final static PackageAccessor
intf org.openide.util.Cancellable
meth public abstract boolean cancel()
meth public abstract void performTask()
meth public abstract void runLocked(java.lang.Runnable)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.spi.editor.document.OnSaveTask$Context
 outer org.netbeans.spi.editor.document.OnSaveTask
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.Element getModificationsRootElement()
meth public void addUndoEdit(javax.swing.undo.UndoableEdit)
supr java.lang.Object
hfds doc,taskStarted,undoEdit

CLSS public abstract interface static org.netbeans.spi.editor.document.OnSaveTask$Factory
 outer org.netbeans.spi.editor.document.OnSaveTask
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="OnSave")
meth public abstract org.netbeans.spi.editor.document.OnSaveTask createTask(org.netbeans.spi.editor.document.OnSaveTask$Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.spi.editor.document.OnSaveTask$PackageAccessor
 outer org.netbeans.spi.editor.document.OnSaveTask
cons public init()
meth public org.netbeans.spi.editor.document.OnSaveTask$Context createContext(javax.swing.text.Document)
meth public void setTaskStarted(org.netbeans.spi.editor.document.OnSaveTask$Context,boolean)
meth public void setUndoEdit(org.netbeans.spi.editor.document.OnSaveTask$Context,javax.swing.undo.UndoableEdit)
supr org.netbeans.modules.editor.lib2.document.DocumentSpiPackageAccessor

CLSS public abstract interface org.netbeans.spi.editor.document.UndoableEditWrapper
meth public abstract javax.swing.undo.UndoableEdit wrap(javax.swing.undo.UndoableEdit,javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightAttributeValue<%0 extends java.lang.Object>
meth public abstract {org.netbeans.spi.editor.highlighting.HighlightAttributeValue%0} getValue(javax.swing.text.JTextComponent,javax.swing.text.Document,java.lang.Object,int,int)

CLSS public final org.netbeans.spi.editor.highlighting.HighlightsChangeEvent
cons public init(org.netbeans.spi.editor.highlighting.HighlightsContainer,int,int)
meth public int getEndOffset()
meth public int getStartOffset()
supr java.util.EventObject
hfds endOffset,startOffset

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightsChangeListener
intf java.util.EventListener
meth public abstract void highlightChanged(org.netbeans.spi.editor.highlighting.HighlightsChangeEvent)

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightsContainer
fld public final static java.lang.String ATTR_EXTENDS_EMPTY_LINE = "org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE"
fld public final static java.lang.String ATTR_EXTENDS_EOL = "org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EOL"
meth public abstract org.netbeans.spi.editor.highlighting.HighlightsSequence getHighlights(int,int)
meth public abstract void addHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)
meth public abstract void removeHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)

CLSS public final org.netbeans.spi.editor.highlighting.HighlightsLayer
meth public java.lang.String toString()
meth public static org.netbeans.spi.editor.highlighting.HighlightsLayer create(java.lang.String,org.netbeans.spi.editor.highlighting.ZOrder,boolean,org.netbeans.spi.editor.highlighting.HighlightsContainer)
supr java.lang.Object
hfds accessor,container,fixedSize,layerTypeId,zOrder
hcls PackageAccessor

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightsLayerFactory
innr public final static Context
meth public abstract org.netbeans.spi.editor.highlighting.HighlightsLayer[] createLayers(org.netbeans.spi.editor.highlighting.HighlightsLayerFactory$Context)

CLSS public final static org.netbeans.spi.editor.highlighting.HighlightsLayerFactory$Context
 outer org.netbeans.spi.editor.highlighting.HighlightsLayerFactory
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds component,document

CLSS public abstract interface org.netbeans.spi.editor.highlighting.HighlightsSequence
fld public final static org.netbeans.spi.editor.highlighting.HighlightsSequence EMPTY
meth public abstract boolean moveNext()
meth public abstract int getEndOffset()
meth public abstract int getStartOffset()
meth public abstract javax.swing.text.AttributeSet getAttributes()

CLSS public abstract interface org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer
intf org.netbeans.spi.editor.highlighting.HighlightsContainer
meth public abstract void released()

CLSS public final org.netbeans.spi.editor.highlighting.ZOrder
fld public final static org.netbeans.spi.editor.highlighting.ZOrder BOTTOM_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder CARET_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder DEFAULT_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder SHOW_OFF_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder SYNTAX_RACK
fld public final static org.netbeans.spi.editor.highlighting.ZOrder TOP_RACK
meth public java.lang.String toString()
meth public org.netbeans.spi.editor.highlighting.ZOrder forPosition(int)
supr java.lang.Object
hfds COMPARATOR,LOG,position,rack

CLSS public abstract org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer
cons protected init()
intf org.netbeans.spi.editor.highlighting.HighlightsContainer
meth protected final void fireHighlightsChange(int,int)
meth public abstract org.netbeans.spi.editor.highlighting.HighlightsSequence getHighlights(int,int)
meth public final void addHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)
meth public final void removeHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)
supr java.lang.Object
hfds listeners

CLSS public final org.netbeans.spi.editor.highlighting.support.OffsetsBag
cons public init(javax.swing.text.Document)
cons public init(javax.swing.text.Document,boolean)
meth public org.netbeans.spi.editor.highlighting.HighlightsSequence getHighlights(int,int)
meth public void addAllHighlights(org.netbeans.spi.editor.highlighting.HighlightsSequence)
meth public void addHighlight(int,int,javax.swing.text.AttributeSet)
meth public void clear()
meth public void discard()
meth public void removeHighlights(int,int,boolean)
meth public void setHighlights(org.netbeans.spi.editor.highlighting.HighlightsSequence)
meth public void setHighlights(org.netbeans.spi.editor.highlighting.support.OffsetsBag)
supr org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer
hfds LOG,discardCaller,discardThreadId,docListener,document,lastAddIndex,lastMoveNextIndex,marks,mergeHighlights,version
hcls DocL,Mark,Seq

CLSS public final org.netbeans.spi.editor.highlighting.support.PositionsBag
cons public init(javax.swing.text.Document)
cons public init(javax.swing.text.Document,boolean)
meth public org.netbeans.spi.editor.highlighting.HighlightsSequence getHighlights(int,int)
meth public void addAllHighlights(org.netbeans.spi.editor.highlighting.support.PositionsBag)
meth public void addHighlight(javax.swing.text.Position,javax.swing.text.Position,javax.swing.text.AttributeSet)
meth public void clear()
meth public void removeHighlights(int,int)
meth public void removeHighlights(javax.swing.text.Position,javax.swing.text.Position,boolean)
meth public void setHighlights(org.netbeans.spi.editor.highlighting.support.PositionsBag)
supr org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer
hfds LOG,attributes,document,marks,mergeHighlights,version
hcls Seq

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

CLSS public abstract interface org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor
innr public abstract interface static Factory
innr public final static MutableContext
meth public abstract boolean beforeChange(org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor$MutableContext) throws javax.swing.text.BadLocationException
meth public abstract void afterChange(org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor$MutableContext) throws javax.swing.text.BadLocationException
meth public abstract void cancelled(org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor$MutableContext)
meth public abstract void change(org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor$MutableContext) throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor$Factory
 outer org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor
meth public abstract org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor createCamelCaseInterceptor(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public final static org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor$MutableContext
 outer org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor
meth public boolean isBackward()
meth public int getOffset()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
meth public void setNextWordOffset(int)
supr java.lang.Object
hfds backward,component,document,nextWordOffset,offset

CLSS public abstract interface org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor
innr public abstract interface static Factory
innr public final static Context
meth public abstract boolean beforeRemove(org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void afterRemove(org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void cancelled(org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context)
meth public abstract void remove(org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context) throws javax.swing.text.BadLocationException

CLSS public final static org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Context
 outer org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor
meth public boolean isBackwardDelete()
meth public int getOffset()
meth public java.lang.String getText()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds backwardDelete,component,document,offset,removedText

CLSS public abstract interface static org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor$Factory
 outer org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor
meth public abstract org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor createDeletedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public abstract interface org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
innr public abstract interface static Factory
innr public final static MutableContext
innr public static Context
meth public abstract boolean beforeInsert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void afterInsert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void cancelled(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context)
meth public abstract void insert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$MutableContext) throws javax.swing.text.BadLocationException

CLSS public static org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context
 outer org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
meth public int getBreakInsertOffset()
meth public int getCaretOffset()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds breakInsertOffset,caretOffset,component,document

CLSS public abstract interface static org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Factory
 outer org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
meth public abstract org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor createTypedBreakInterceptor(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public final static org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$MutableContext
 outer org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
meth public !varargs void setText(java.lang.String,int,int,int[])
supr org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context
hfds breakInsertPosition,caretPosition,insertionText,reindentBlocks

CLSS public abstract interface org.netbeans.spi.editor.typinghooks.TypedTextInterceptor
innr public abstract interface static Factory
innr public final static MutableContext
innr public static Context
meth public abstract boolean beforeInsert(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void afterInsert(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void cancelled(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context)
meth public abstract void insert(org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$MutableContext) throws javax.swing.text.BadLocationException

CLSS public static org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context
 outer org.netbeans.spi.editor.typinghooks.TypedTextInterceptor
meth public int getOffset()
meth public java.lang.String getText()
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getComponent()
supr java.lang.Object
hfds component,document,offset,originallyTypedText

CLSS public abstract interface static org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Factory
 outer org.netbeans.spi.editor.typinghooks.TypedTextInterceptor
meth public abstract org.netbeans.spi.editor.typinghooks.TypedTextInterceptor createTypedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public final static org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$MutableContext
 outer org.netbeans.spi.editor.typinghooks.TypedTextInterceptor
meth public java.lang.String getReplacedText()
meth public java.lang.String getText()
meth public void setText(java.lang.String,int)
supr org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Context
hfds caretPosition,insertionText,replacedText
hcls Accessor

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

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

