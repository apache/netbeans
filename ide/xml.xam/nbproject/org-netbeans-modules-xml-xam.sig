#Signature file v4.1
#Version 1.55.0

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface javax.swing.event.UndoableEditListener
intf java.util.EventListener
meth public abstract void undoableEditHappened(javax.swing.event.UndoableEditEvent)

CLSS public javax.swing.undo.AbstractUndoableEdit
cons public init()
fld protected final static java.lang.String RedoName = "Redo"
fld protected final static java.lang.String UndoName = "Undo"
intf java.io.Serializable
intf javax.swing.undo.UndoableEdit
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isSignificant()
meth public boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void die()
meth public void redo()
meth public void undo()
supr java.lang.Object

CLSS public javax.swing.undo.CompoundEdit
cons public init()
fld protected java.util.Vector<javax.swing.undo.UndoableEdit> edits
meth protected javax.swing.undo.UndoableEdit lastEdit()
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public boolean canRedo()
meth public boolean canUndo()
meth public boolean isInProgress()
meth public boolean isSignificant()
meth public java.lang.String getPresentationName()
meth public java.lang.String getRedoPresentationName()
meth public java.lang.String getUndoPresentationName()
meth public java.lang.String toString()
meth public void die()
meth public void end()
meth public void redo()
meth public void undo()
supr javax.swing.undo.AbstractUndoableEdit

CLSS public abstract interface javax.swing.undo.UndoableEdit
meth public abstract boolean addEdit(javax.swing.undo.UndoableEdit)
meth public abstract boolean canRedo()
meth public abstract boolean canUndo()
meth public abstract boolean isSignificant()
meth public abstract boolean replaceEdit(javax.swing.undo.UndoableEdit)
meth public abstract java.lang.String getPresentationName()
meth public abstract java.lang.String getRedoPresentationName()
meth public abstract java.lang.String getUndoPresentationName()
meth public abstract void die()
meth public abstract void redo()
meth public abstract void undo()

CLSS public javax.swing.undo.UndoableEditSupport
cons public init()
cons public init(java.lang.Object)
fld protected int updateLevel
fld protected java.lang.Object realSource
fld protected java.util.Vector<javax.swing.event.UndoableEditListener> listeners
fld protected javax.swing.undo.CompoundEdit compoundEdit
meth protected javax.swing.undo.CompoundEdit createCompoundEdit()
meth protected void _postEdit(javax.swing.undo.UndoableEdit)
meth public int getUpdateLevel()
meth public java.lang.String toString()
meth public javax.swing.event.UndoableEditListener[] getUndoableEditListeners()
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void beginUpdate()
meth public void endUpdate()
meth public void postEdit(javax.swing.undo.UndoableEdit)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.xml.xam.AbstractComponent<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractComponent%0}>>
cons public init(org.netbeans.modules.xml.xam.AbstractModel)
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractComponent%0}>
meth protected <%0 extends {org.netbeans.modules.xml.xam.AbstractComponent%0}> {%%0} getChild(java.lang.Class<{%%0}>)
meth protected abstract void appendChildQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected abstract void insertAtIndexQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>,int)
meth protected abstract void populateChildren(java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected abstract void removeChildQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected final boolean isChildrenInitialized()
meth protected void addAfter(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void addBefore(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void appendChild(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void checkNullOrDuplicateChild({org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void fireChildAdded()
meth protected void fireChildRemoved()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void fireValueChanged()
meth protected void insertAtIndex(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},int,java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected void setChild(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setChild(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>,boolean)
meth protected void setChildAfter(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setChildBefore(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setModel(org.netbeans.modules.xml.xam.AbstractModel)
meth protected void setParent({org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void verifyWrite()
meth public <%0 extends {org.netbeans.modules.xml.xam.AbstractComponent%0}> java.util.List<{%%0}> getChildren(java.lang.Class<{%%0}>)
meth public boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int getChildrenCount()
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}> getChildren()
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}> getChildren(java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth public org.netbeans.modules.xml.xam.AbstractModel getModel()
meth public void checkChildrenPopulated()
meth public void insertAtIndex(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},int)
meth public void removeChild(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0})
meth public void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public {org.netbeans.modules.xml.xam.AbstractComponent%0} getParent()
supr java.lang.Object
hfds children,model,parent
hcls DelegateListener

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModel<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractModel%0}>>
cons public init(org.netbeans.modules.xml.xam.ModelSource)
fld protected org.netbeans.modules.xml.xam.AbstractModel$ModelUndoableEditSupport ues
innr protected ModelUndoableEdit
innr protected ModelUndoableEditSupport
intf javax.swing.event.UndoableEditListener
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.AbstractModel%0}>
meth protected boolean needsSync()
meth protected javax.swing.undo.CompoundEdit createModelUndoableEdit()
meth protected void endTransaction(boolean)
meth protected void finishTransaction()
meth protected void refresh()
meth protected void setInSync(boolean)
meth protected void setInUndoRedo(boolean)
meth protected void setState(org.netbeans.modules.xml.xam.Model$State)
meth protected void syncCompleted()
meth protected void syncStarted()
meth protected void transactionCompleted()
meth protected void transactionStarted()
meth public abstract org.netbeans.modules.xml.xam.ModelAccess getAccess()
meth public boolean inSync()
meth public boolean inUndoRedo()
meth public boolean isAutoSyncActive()
meth public boolean isIntransaction()
meth public boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public boolean startedFiringEvents()
meth public org.netbeans.modules.xml.xam.Model$State getState()
meth public org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public void endTransaction()
meth public void fireComponentChangedEvent(org.netbeans.modules.xml.xam.ComponentEvent)
meth public void firePropertyChangeEvent(java.beans.PropertyChangeEvent)
meth public void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public void rollbackTransaction()
meth public void setAutoSyncActive(boolean)
meth public void sync() throws java.io.IOException
meth public void undoableEditHappened(javax.swing.event.UndoableEditEvent)
meth public void validateWrite()
supr java.lang.Object
hfds RP,componentListeners,inSync,inUndoRedo,logger,pcs,savedUndoableEditListeners,source,status,transaction
hcls Transaction

CLSS protected org.netbeans.modules.xml.xam.AbstractModel$ModelUndoableEdit
 outer org.netbeans.modules.xml.xam.AbstractModel
cons protected init(org.netbeans.modules.xml.xam.AbstractModel)
meth public boolean addEdit(javax.swing.undo.UndoableEdit)
meth public void justUndo()
meth public void redo()
meth public void undo()
supr javax.swing.undo.CompoundEdit
hfds serialVersionUID

CLSS protected org.netbeans.modules.xml.xam.AbstractModel$ModelUndoableEditSupport
 outer org.netbeans.modules.xml.xam.AbstractModel
cons protected init(org.netbeans.modules.xml.xam.AbstractModel)
meth protected javax.swing.undo.CompoundEdit createCompoundEdit()
meth protected void abortUpdate()
supr javax.swing.undo.UndoableEditSupport

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModelFactory<%0 extends org.netbeans.modules.xml.xam.Model>
cons public init()
fld public final static int DELAY_DIRTY = 1000
fld public final static int DELAY_SYNCER = 2000
fld public final static java.lang.String MODEL_LOADED_PROPERTY = "modelLoaded"
meth protected abstract {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createModel(org.netbeans.modules.xml.xam.ModelSource)
meth protected java.lang.Object getKey(org.netbeans.modules.xml.xam.ModelSource)
meth protected {org.netbeans.modules.xml.xam.AbstractModelFactory%0} getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractModelFactory%0}> getModels()
meth public static org.netbeans.modules.xml.xam.spi.ModelAccessProvider getAccessProvider()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createFreshModel(org.netbeans.modules.xml.xam.ModelSource)
supr java.lang.Object
hfds LOG,SYNCER,cachedModels,factories,propSupport

CLSS public abstract org.netbeans.modules.xml.xam.AbstractReference<%0 extends org.netbeans.modules.xml.xam.Referenceable>
cons public init(java.lang.Class<{org.netbeans.modules.xml.xam.AbstractReference%0}>,org.netbeans.modules.xml.xam.AbstractComponent,java.lang.String)
cons public init({org.netbeans.modules.xml.xam.AbstractReference%0},java.lang.Class<{org.netbeans.modules.xml.xam.AbstractReference%0}>,org.netbeans.modules.xml.xam.AbstractComponent)
fld protected java.lang.String refString
intf org.netbeans.modules.xml.xam.Reference<{org.netbeans.modules.xml.xam.AbstractReference%0}>
meth protected org.netbeans.modules.xml.xam.AbstractComponent getParent()
meth protected void setReferenced({org.netbeans.modules.xml.xam.AbstractReference%0})
meth protected {org.netbeans.modules.xml.xam.AbstractReference%0} getReferenced()
meth public boolean equals(java.lang.Object)
meth public boolean isBroken()
meth public boolean references({org.netbeans.modules.xml.xam.AbstractReference%0})
meth public int hashCode()
meth public java.lang.Class<{org.netbeans.modules.xml.xam.AbstractReference%0}> getType()
meth public java.lang.String getRefString()
meth public java.lang.String toString()
supr java.lang.Object
hfds classType,parent,referenced

CLSS public abstract interface org.netbeans.modules.xml.xam.Component<%0 extends org.netbeans.modules.xml.xam.Component>
meth public abstract <%0 extends {org.netbeans.modules.xml.xam.Component%0}> java.util.List<{%%0}> getChildren(java.lang.Class<{%%0}>)
meth public abstract boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren()
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren(java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.Component%0}>>)
meth public abstract org.netbeans.modules.xml.xam.Component copy({org.netbeans.modules.xml.xam.Component%0})
meth public abstract org.netbeans.modules.xml.xam.Model getModel()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract {org.netbeans.modules.xml.xam.Component%0} getParent()

CLSS public org.netbeans.modules.xml.xam.ComponentEvent
cons public init(java.lang.Object,org.netbeans.modules.xml.xam.ComponentEvent$EventType)
innr public abstract static !enum EventType
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.xam.ComponentEvent$EventType getEventType()
supr java.util.EventObject
hfds event,serialVersionUID

CLSS public abstract static !enum org.netbeans.modules.xml.xam.ComponentEvent$EventType
 outer org.netbeans.modules.xml.xam.ComponentEvent
fld public final static org.netbeans.modules.xml.xam.ComponentEvent$EventType CHILD_ADDED
fld public final static org.netbeans.modules.xml.xam.ComponentEvent$EventType CHILD_REMOVED
fld public final static org.netbeans.modules.xml.xam.ComponentEvent$EventType VALUE_CHANGED
meth public abstract void fireEvent(org.netbeans.modules.xml.xam.ComponentEvent,org.netbeans.modules.xml.xam.ComponentListener)
meth public static org.netbeans.modules.xml.xam.ComponentEvent$EventType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xam.ComponentEvent$EventType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xam.ComponentEvent$EventType>

CLSS public abstract interface org.netbeans.modules.xml.xam.ComponentListener
intf java.util.EventListener
meth public abstract void childrenAdded(org.netbeans.modules.xml.xam.ComponentEvent)
meth public abstract void childrenDeleted(org.netbeans.modules.xml.xam.ComponentEvent)
meth public abstract void valueChanged(org.netbeans.modules.xml.xam.ComponentEvent)

CLSS public abstract interface org.netbeans.modules.xml.xam.ComponentUpdater<%0 extends org.netbeans.modules.xml.xam.Component>
innr public abstract interface static Query
innr public final static !enum Operation
meth public abstract void update({org.netbeans.modules.xml.xam.ComponentUpdater%0},{org.netbeans.modules.xml.xam.ComponentUpdater%0},int,org.netbeans.modules.xml.xam.ComponentUpdater$Operation)
meth public abstract void update({org.netbeans.modules.xml.xam.ComponentUpdater%0},{org.netbeans.modules.xml.xam.ComponentUpdater%0},org.netbeans.modules.xml.xam.ComponentUpdater$Operation)

CLSS public final static !enum org.netbeans.modules.xml.xam.ComponentUpdater$Operation
 outer org.netbeans.modules.xml.xam.ComponentUpdater
fld public final static org.netbeans.modules.xml.xam.ComponentUpdater$Operation ADD
fld public final static org.netbeans.modules.xml.xam.ComponentUpdater$Operation REMOVE
meth public static org.netbeans.modules.xml.xam.ComponentUpdater$Operation valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xam.ComponentUpdater$Operation[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xam.ComponentUpdater$Operation>

CLSS public abstract interface static org.netbeans.modules.xml.xam.ComponentUpdater$Query<%0 extends org.netbeans.modules.xml.xam.Component>
 outer org.netbeans.modules.xml.xam.ComponentUpdater
meth public abstract boolean canAdd({org.netbeans.modules.xml.xam.ComponentUpdater$Query%0},org.netbeans.modules.xml.xam.Component)

CLSS public abstract interface org.netbeans.modules.xml.xam.EmbeddableRoot
innr public abstract interface static ForeignParent
meth public abstract org.netbeans.modules.xml.xam.Component getForeignParent()
meth public abstract void setForeignParent(org.netbeans.modules.xml.xam.Component)

CLSS public abstract interface static org.netbeans.modules.xml.xam.EmbeddableRoot$ForeignParent
 outer org.netbeans.modules.xml.xam.EmbeddableRoot
meth public abstract java.util.List<org.netbeans.modules.xml.xam.EmbeddableRoot> getAdoptedChildren()

CLSS public abstract interface org.netbeans.modules.xml.xam.Model<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Model%0}>>
fld public final static java.lang.String STATE_PROPERTY = "state"
innr public final static !enum State
intf org.netbeans.modules.xml.xam.Referenceable
meth public abstract boolean inSync()
meth public abstract boolean isIntransaction()
meth public abstract boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public abstract org.netbeans.modules.xml.xam.Model$State getState()
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public abstract void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public abstract void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void endTransaction()
meth public abstract void removeChildComponent(org.netbeans.modules.xml.xam.Component)
meth public abstract void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void sync() throws java.io.IOException

CLSS public final static !enum org.netbeans.modules.xml.xam.Model$State
 outer org.netbeans.modules.xml.xam.Model
fld public final static org.netbeans.modules.xml.xam.Model$State NOT_SYNCED
fld public final static org.netbeans.modules.xml.xam.Model$State NOT_WELL_FORMED
fld public final static org.netbeans.modules.xml.xam.Model$State VALID
meth public static org.netbeans.modules.xml.xam.Model$State valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xam.Model$State[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xam.Model$State>

CLSS public abstract org.netbeans.modules.xml.xam.ModelAccess
cons public init()
meth public abstract org.netbeans.modules.xml.xam.Model$State sync() throws java.io.IOException
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void finishUndoRedo()
meth public abstract void flush()
meth public abstract void prepareForUndoRedo()
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public boolean isAutoSync()
meth public long dirtyIntervalMillis()
meth public void prepareSync()
meth public void setAutoSync(boolean)
meth public void unsetDirty()
supr java.lang.Object
hfds autoSync

CLSS public org.netbeans.modules.xml.xam.ModelSource
cons public init(org.openide.util.Lookup,boolean)
intf org.openide.util.Lookup$Provider
meth public boolean isEditable()
meth public org.openide.util.Lookup getLookup()
supr java.lang.Object
hfds creation,editable,lookup

CLSS public abstract interface org.netbeans.modules.xml.xam.Nameable<%0 extends org.netbeans.modules.xml.xam.Component>
intf org.netbeans.modules.xml.xam.Named<{org.netbeans.modules.xml.xam.Nameable%0}>
meth public abstract void setName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.xam.Named<%0 extends org.netbeans.modules.xml.xam.Component>
fld public final static java.lang.String NAME_PROPERTY = "name"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Named%0}>
meth public abstract java.lang.String getName()

CLSS public abstract interface org.netbeans.modules.xml.xam.NamedReferenceable<%0 extends org.netbeans.modules.xml.xam.Component>
intf org.netbeans.modules.xml.xam.Named<{org.netbeans.modules.xml.xam.NamedReferenceable%0}>
intf org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract interface org.netbeans.modules.xml.xam.Reference<%0 extends org.netbeans.modules.xml.xam.Referenceable>
meth public abstract boolean isBroken()
meth public abstract boolean references({org.netbeans.modules.xml.xam.Reference%0})
meth public abstract java.lang.Class<{org.netbeans.modules.xml.xam.Reference%0}> getType()
meth public abstract java.lang.String getRefString()
meth public abstract {org.netbeans.modules.xml.xam.Reference%0} get()

CLSS public abstract interface org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>>
cons public init(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel,org.w3c.dom.Element)
innr public static PrefixAttribute
intf org.netbeans.modules.xml.xam.dom.DocumentComponent2<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>
intf org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater
meth protected <%0 extends org.w3c.dom.Node> void updateReference(org.w3c.dom.Element,java.util.List<{%%0}>)
meth protected abstract java.lang.Object getAttributeValueOf(org.netbeans.modules.xml.xam.dom.Attribute,java.lang.String)
meth protected abstract void populateChildren(java.util.List<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>)
meth protected int findDomainIndex(org.w3c.dom.Element)
meth protected int getNodeIndexOf(org.w3c.dom.Node,org.w3c.dom.Node)
meth protected java.lang.String ensureUnique(java.lang.String,java.lang.String)
meth protected java.lang.String getChildElementText(javax.xml.namespace.QName)
meth protected java.lang.String getLeadingText({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth protected java.lang.String getNamespaceURI()
meth protected java.lang.String getPrefixedName(java.lang.String,java.lang.String)
meth protected java.lang.String getPrefixedName(java.lang.String,java.lang.String,java.lang.String,boolean)
meth protected java.lang.String getPrefixedName(javax.xml.namespace.QName,boolean)
meth protected java.lang.String getText()
meth protected java.lang.String getText({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},boolean,boolean)
meth protected java.lang.String getTrailingText({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth protected java.lang.String getXmlFragment()
meth protected org.netbeans.modules.xml.xam.ModelSource resolveModel(java.lang.String) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth protected org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent getEffectiveParent()
meth protected org.netbeans.modules.xml.xam.dom.Attribute createPrefixAttribute(java.lang.String)
meth protected org.netbeans.modules.xml.xam.dom.DocumentModelAccess getAccess()
meth protected org.w3c.dom.Element getChildElement(javax.xml.namespace.QName)
meth protected void appendChildQuietly({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},java.util.List<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>)
meth protected void ensureValueNamespaceDeclared(java.lang.String,java.lang.String,java.lang.String)
meth protected void fireChildAdded()
meth protected void fireChildRemoved()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void fireValueChanged()
meth protected void insertAtIndexQuietly({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},java.util.List<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>,int)
meth protected void removeAttributeQuietly(org.w3c.dom.Element,java.lang.String)
meth protected void removeChildQuietly({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},java.util.List<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>)
meth protected void setAttributeQuietly(org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth protected void setChildElementText(java.lang.String,java.lang.String,javax.xml.namespace.QName)
meth protected void setLeadingText(java.lang.String,java.lang.String,{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth protected void setQNameAttribute(java.lang.String,javax.xml.namespace.QName,java.lang.String)
meth protected void setText(java.lang.String,java.lang.String)
meth protected void setText(java.lang.String,java.lang.String,{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0},boolean,boolean)
meth protected void setTrailingText(java.lang.String,java.lang.String,{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth protected void setXmlFragment(java.lang.String,java.lang.String) throws java.io.IOException
meth protected void updatePeer(java.lang.String,org.w3c.dom.Element)
meth protected void verifyWrite()
meth public <%0 extends org.w3c.dom.Node> void updateReference(java.util.List<{%%0}>)
meth public boolean isInDocumentModel()
meth public boolean referencesSameNode(org.w3c.dom.Node)
meth public int findAttributePosition(java.lang.String)
meth public int findEndPosition()
meth public int findPosition()
meth public java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public java.lang.String getAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
meth public java.lang.String getXmlFragmentInclusive()
meth public java.lang.String lookupNamespaceURI(java.lang.String)
meth public java.lang.String lookupNamespaceURI(java.lang.String,boolean)
meth public java.lang.String lookupPrefix(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getPrefixes()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getAttributeMap()
meth public javax.xml.namespace.QName getQName()
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentModel getModel()
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent copy({org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0})
meth public org.w3c.dom.Element getPeer()
meth public static java.lang.String getText(org.w3c.dom.Element)
meth public static javax.xml.namespace.QName getQName(org.w3c.dom.Node)
meth public void addPrefix(java.lang.String,java.lang.String)
meth public void removePrefix(java.lang.String)
meth public void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public void setAttribute(java.lang.String,org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth public void updateReference(org.w3c.dom.Element)
meth public {org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0} findChildComponent(org.w3c.dom.Element)
meth public {org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0} findChildComponentByIdentity(org.w3c.dom.Element)
supr org.netbeans.modules.xml.xam.AbstractComponent<{org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent%0}>
hfds node

CLSS public static org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent$PrefixAttribute
 outer org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent
cons public init(java.lang.String)
intf org.netbeans.modules.xml.xam.dom.Attribute
meth public java.lang.Class getMemberType()
meth public java.lang.Class getType()
meth public java.lang.String getName()
supr java.lang.Object
hfds prefix

CLSS public abstract org.netbeans.modules.xml.xam.dom.AbstractDocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>>
cons public init(org.netbeans.modules.xml.xam.ModelSource)
fld protected org.netbeans.modules.xml.xam.dom.DocumentModelAccess access
intf org.netbeans.modules.xml.xam.dom.DocumentModel<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>
meth protected abstract org.netbeans.modules.xml.xam.ComponentUpdater<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}> getComponentUpdater()
meth protected boolean isDomainElement(org.w3c.dom.Node)
meth protected boolean needsSync()
meth protected static java.lang.String toLocalName(java.lang.String)
meth protected void firePropertyChangedEvents(org.netbeans.modules.xml.xam.dom.SyncUnit)
meth protected void firePropertyChangedEvents(org.netbeans.modules.xml.xam.dom.SyncUnit,org.w3c.dom.Element)
meth protected void refresh()
meth protected void setIdentifyingAttributes()
meth protected void syncCompleted()
meth protected void syncStarted()
meth public abstract {org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0} createRootComponent(org.w3c.dom.Element)
meth public boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>> getQNameValuedAttributes()
meth public java.util.Set<java.lang.String> getElementNames()
meth public java.util.Set<javax.xml.namespace.QName> getQNames()
meth public javax.swing.text.Document getBaseDocument()
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent findComponent(org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent,java.util.List<org.w3c.dom.Element>,int)
meth public org.netbeans.modules.xml.xam.dom.ChangeInfo prepareChangeInfo(java.util.List<? extends org.w3c.dom.Node>,java.util.List<? extends org.w3c.dom.Node>)
meth public org.netbeans.modules.xml.xam.dom.ChangeInfo prepareChangeInfo(java.util.List<org.w3c.dom.Node>)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(java.util.List<org.w3c.dom.Element>)
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(org.w3c.dom.Element)
meth public org.netbeans.modules.xml.xam.dom.DocumentModelAccess getAccess()
meth public org.netbeans.modules.xml.xam.dom.SyncUnit prepareSyncUnit(org.netbeans.modules.xml.xam.dom.ChangeInfo,org.netbeans.modules.xml.xam.dom.SyncUnit)
meth public org.w3c.dom.Document getDocument()
meth public static org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider getAccessProvider()
meth public void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public void processSyncUnit(org.netbeans.modules.xml.xam.dom.SyncUnit)
meth public void removeChildComponent(org.netbeans.modules.xml.xam.Component)
supr org.netbeans.modules.xml.xam.AbstractModel<{org.netbeans.modules.xml.xam.dom.AbstractDocumentModel%0}>
hfds accessPrivate,docListener,elementNames,getAccessLock,needsSync,swingDocument
hcls DocumentChangeListener,WeakDocumentListener

CLSS public abstract org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference<%0 extends org.netbeans.modules.xml.xam.NamedReferenceable>
cons public init(java.lang.Class<{org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference%0}>,org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent,java.lang.String)
cons public init({org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference%0},java.lang.Class<{org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference%0}>,org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent)
fld protected java.lang.String localName
fld protected java.lang.String prefix
fld protected javax.xml.namespace.QName qname
intf org.netbeans.modules.xml.xam.dom.NamedComponentReference<{org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference%0}>
meth protected java.lang.String getLocalName()
meth protected java.lang.String getPrefix()
meth protected javax.xml.namespace.QName calculateQNameLocally()
meth protected org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent getParent()
meth protected void checkParentNotRemovedFromModel()
meth protected void checkParentPartOfModel()
meth protected {org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference%0} getReferenced()
meth public boolean equals(java.lang.Object)
meth public boolean references({org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference%0})
meth public int hashCode()
meth public java.lang.String getRefString()
meth public javax.xml.namespace.QName getQName()
meth public void refresh()
supr org.netbeans.modules.xml.xam.AbstractReference<{org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference%0}>

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.Attribute
meth public abstract java.lang.Class getMemberType()
meth public abstract java.lang.Class getType()
meth public abstract java.lang.String getName()

CLSS public org.netbeans.modules.xml.xam.dom.ChangeInfo
cons public init(org.w3c.dom.Element,org.w3c.dom.Node,boolean,java.util.List<org.w3c.dom.Element>,java.util.List<org.w3c.dom.Node>)
meth public boolean isAdded()
meth public boolean isDomainElement()
meth public boolean isDomainElementAdded()
meth public java.lang.String toString()
meth public java.util.List<org.w3c.dom.Element> getParentToRootPath()
meth public java.util.List<org.w3c.dom.Element> getRootToParentPath()
meth public java.util.List<org.w3c.dom.Node> getOtherNonDomainElementNodes()
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent getParentComponent()
meth public org.w3c.dom.Element getChangedElement()
meth public org.w3c.dom.Element getParent()
meth public org.w3c.dom.Node getActualChangedNode()
meth public org.w3c.dom.Node getChangedNode()
meth public void markNonDomainChildAsChanged()
meth public void markParentAsChanged()
meth public void setAdded(boolean)
meth public void setDomainElement(boolean)
meth public void setParentComponent(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public void setRootToParentPath(java.util.List<org.w3c.dom.Element>)
supr java.lang.Object
hfds added,changed,domainElement,otherNonDomainElementNodes,parent,parentComponent,rootToParent

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.ComponentFactory<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.ComponentFactory%0}>>
meth public abstract {org.netbeans.modules.xml.xam.dom.ComponentFactory%0} create(org.w3c.dom.Element,{org.netbeans.modules.xml.xam.dom.ComponentFactory%0})

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentComponent<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent>
fld public final static java.lang.String TEXT_CONTENT_PROPERTY = "textContent"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.dom.DocumentComponent%0}>
meth public abstract boolean isInDocumentModel()
meth public abstract boolean referencesSameNode(org.w3c.dom.Node)
meth public abstract int findAttributePosition(java.lang.String)
meth public abstract int findPosition()
meth public abstract java.lang.String getAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
meth public abstract org.w3c.dom.Element getPeer()
meth public abstract void setAttribute(java.lang.String,org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentComponent%0} findChildComponent(org.w3c.dom.Element)

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentComponent2<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent>
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentComponent2%0}>
meth public abstract int findEndPosition()

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>>
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public abstract org.w3c.dom.Document getDocument()
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} createComponent({org.netbeans.modules.xml.xam.dom.DocumentModel%0},org.w3c.dom.Element)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} getRootComponent()

CLSS public abstract org.netbeans.modules.xml.xam.dom.DocumentModelAccess
cons public init()
innr public abstract interface static NodeUpdater
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract int findPosition(org.w3c.dom.Node)
meth public abstract int getElementIndexOf(org.w3c.dom.Node,org.w3c.dom.Element)
meth public abstract java.lang.String getXPath(org.w3c.dom.Document,org.w3c.dom.Element)
meth public abstract java.lang.String getXmlFragment(org.w3c.dom.Element)
meth public abstract java.util.List<org.w3c.dom.Element> getPathFromRoot(org.w3c.dom.Document,org.w3c.dom.Element)
meth public abstract java.util.List<org.w3c.dom.Node> findNodes(org.w3c.dom.Document,java.lang.String)
meth public abstract java.util.Map<javax.xml.namespace.QName,java.lang.String> getAttributeMap(org.w3c.dom.Element)
meth public abstract org.netbeans.modules.xml.xam.dom.ElementIdentity getElementIdentity()
meth public abstract org.w3c.dom.Document getDocumentRoot()
meth public abstract org.w3c.dom.Element duplicate(org.w3c.dom.Element)
meth public abstract org.w3c.dom.Element getContainingElement(int)
meth public abstract org.w3c.dom.Node findNode(org.w3c.dom.Document,java.lang.String)
meth public abstract org.w3c.dom.Node getNewEventNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getNewEventParentNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getOldEventNode(java.beans.PropertyChangeEvent)
meth public abstract org.w3c.dom.Node getOldEventParentNode(java.beans.PropertyChangeEvent)
meth public abstract void addMergeEventHandler(java.beans.PropertyChangeListener)
meth public abstract void appendChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void insertBefore(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeAttribute(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void removeMergeEventHandler(java.beans.PropertyChangeListener)
meth public abstract void replaceChild(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setAttribute(org.w3c.dom.Element,java.lang.String,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setPrefix(org.w3c.dom.Element,java.lang.String)
meth public abstract void setText(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public abstract void setXmlFragment(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater) throws java.io.IOException
meth public java.lang.String getCurrentDocumentText()
meth public java.lang.String getXmlFragmentInclusive(org.w3c.dom.Element)
meth public java.lang.String lookupNamespaceURI(org.w3c.dom.Node,java.util.List<? extends org.w3c.dom.Node>)
meth public java.lang.String normalizeUndefinedAttributeValue(java.lang.String)
meth public long dirtyIntervalMillis()
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentModel getModel()
meth public void addQNameValuedAttributes(java.util.Map<javax.xml.namespace.QName,java.util.List<javax.xml.namespace.QName>>)
meth public void removeChildren(org.w3c.dom.Node,java.util.Collection<org.w3c.dom.Node>,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void reorderChildren(org.w3c.dom.Element,int[],org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setDirty()
meth public void unsetDirty()
supr org.netbeans.modules.xml.xam.ModelAccess
hfds dirtyTimeMillis

CLSS public abstract interface static org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater
 outer org.netbeans.modules.xml.xam.dom.DocumentModelAccess
meth public abstract <%0 extends org.w3c.dom.Node> void updateReference(java.util.List<{%%0}>)
meth public abstract void updateReference(org.w3c.dom.Element)

CLSS public abstract org.netbeans.modules.xml.xam.dom.DocumentModelAccess2
cons public init()
meth public abstract int findEndPosition(org.w3c.dom.Node)
supr org.netbeans.modules.xml.xam.dom.DocumentModelAccess

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.ElementIdentity
meth public abstract boolean compareElement(org.w3c.dom.Element,org.w3c.dom.Element,org.w3c.dom.Document,org.w3c.dom.Document)
meth public abstract java.util.List getIdentifiers()
meth public abstract void addIdentifier(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.NamedComponentReference<%0 extends org.netbeans.modules.xml.xam.NamedReferenceable>
intf org.netbeans.modules.xml.xam.Reference<{org.netbeans.modules.xml.xam.dom.NamedComponentReference%0}>
meth public abstract java.lang.String getEffectiveNamespace()
meth public abstract javax.xml.namespace.QName getQName()

CLSS public org.netbeans.modules.xml.xam.dom.ReadOnlyAccess
cons public init(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel)
innr public static Provider
meth protected org.w3c.dom.Element findElement(int,java.lang.String,org.w3c.dom.Element,int)
meth public boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public int findPosition(org.w3c.dom.Node)
meth public int getElementIndexOf(org.w3c.dom.Node,org.w3c.dom.Element)
meth public java.lang.String getXPath(org.w3c.dom.Document,org.w3c.dom.Element)
meth public java.lang.String getXmlFragment(org.w3c.dom.Element)
meth public java.lang.String normalizeUndefinedAttributeValue(java.lang.String)
meth public java.util.List<org.w3c.dom.Element> getPathFromRoot(org.w3c.dom.Document,org.w3c.dom.Element)
meth public java.util.List<org.w3c.dom.Node> findNodes(org.w3c.dom.Document,java.lang.String)
meth public java.util.Map<javax.xml.namespace.QName,java.lang.String> getAttributeMap(org.w3c.dom.Element)
meth public org.netbeans.modules.xml.xam.Model$State sync() throws java.io.IOException
meth public org.netbeans.modules.xml.xam.dom.AbstractDocumentModel getModel()
meth public org.netbeans.modules.xml.xam.dom.ElementIdentity getElementIdentity()
meth public org.w3c.dom.Document getDocumentRoot()
meth public org.w3c.dom.Element duplicate(org.w3c.dom.Element)
meth public org.w3c.dom.Element getContainingElement(int)
meth public org.w3c.dom.Node findNode(org.w3c.dom.Document,java.lang.String)
meth public org.w3c.dom.Node getNewEventNode(java.beans.PropertyChangeEvent)
meth public org.w3c.dom.Node getNewEventParentNode(java.beans.PropertyChangeEvent)
meth public org.w3c.dom.Node getOldEventNode(java.beans.PropertyChangeEvent)
meth public org.w3c.dom.Node getOldEventParentNode(java.beans.PropertyChangeEvent)
meth public static boolean isXmlnsAttribute(org.w3c.dom.Attr)
meth public void addMergeEventHandler(java.beans.PropertyChangeListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void appendChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void finishUndoRedo()
meth public void flush()
meth public void insertBefore(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void prepareForUndoRedo()
meth public void removeAttribute(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void removeChild(org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void removeMergeEventHandler(java.beans.PropertyChangeListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void replaceChild(org.w3c.dom.Node,org.w3c.dom.Node,org.w3c.dom.Node,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setAttribute(org.w3c.dom.Element,java.lang.String,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setPrefix(org.w3c.dom.Element,java.lang.String)
meth public void setText(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater)
meth public void setXmlFragment(org.w3c.dom.Element,java.lang.String,org.netbeans.modules.xml.xam.dom.DocumentModelAccess$NodeUpdater) throws java.io.IOException
supr org.netbeans.modules.xml.xam.dom.DocumentModelAccess
hfds model,rootDoc
hcls StringScanner

CLSS public static org.netbeans.modules.xml.xam.dom.ReadOnlyAccess$Provider
 outer org.netbeans.modules.xml.xam.dom.ReadOnlyAccess
cons protected init()
intf org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider
meth public java.lang.Object getModelSourceKey(org.netbeans.modules.xml.xam.ModelSource)
meth public javax.swing.text.Document loadSwingDocument(java.io.InputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth public org.netbeans.modules.xml.xam.dom.DocumentModelAccess createModelAccess(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel)
meth public static org.netbeans.modules.xml.xam.dom.ReadOnlyAccess$Provider getInstance()
supr java.lang.Object
hfds instance

CLSS public org.netbeans.modules.xml.xam.dom.SyncUnit
cons public init(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public boolean hasTextContentChanges()
meth public boolean hasWhitespaceChangeOnly()
meth public boolean isComponentChanged()
meth public java.util.List<org.netbeans.modules.xml.xam.dom.ChangeInfo> getChanges()
meth public java.util.List<org.netbeans.modules.xml.xam.dom.DocumentComponent> getToAddList()
meth public java.util.List<org.netbeans.modules.xml.xam.dom.DocumentComponent> getToRemoveList()
meth public java.util.List<org.w3c.dom.Element> getParentToRootPath()
meth public java.util.Map<java.lang.String,org.w3c.dom.Attr> getAddedAttributes()
meth public java.util.Map<java.lang.String,org.w3c.dom.Attr> getRemovedAttributes()
meth public java.util.Set<java.lang.String> getNonDomainedElementChanges()
meth public org.netbeans.modules.xml.xam.dom.ChangeInfo getLastChange()
meth public org.netbeans.modules.xml.xam.dom.DocumentComponent getTarget()
meth public void addChange(org.netbeans.modules.xml.xam.dom.ChangeInfo)
meth public void addNonDomainedElementChange(org.netbeans.modules.xml.xam.dom.ChangeInfo)
meth public void addToAddList(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public void addToAddedAttributes(org.w3c.dom.Attr)
meth public void addToRemoveList(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public void addToRemovedAttributes(org.w3c.dom.Attr)
meth public void merge(org.netbeans.modules.xml.xam.dom.SyncUnit)
meth public void setComponentChanged(boolean)
meth public void setHasTextContentChanges(boolean)
meth public void updateTargetReference()
supr java.lang.Object
hfds addedAttributes,changes,componentChanged,hasTextContentChanges,nonDomainedChanges,removedAttributes,target,toAdd,toRemove

CLSS public final org.netbeans.modules.xml.xam.dom.Utils
cons public init()
meth public static boolean isValidNCName(java.lang.String)
supr java.lang.Object
hfds BASECHARS,BASECHARS_RANGES,BASECHARS_VALUES,COMBININGS,COMBININGS_RANGES,COMBININGS_VALUES,EXTENDERS,IDEOGHAPHICS

CLSS public abstract interface org.netbeans.modules.xml.xam.locator.CatalogModel
intf org.w3c.dom.ls.LSResourceResolver
intf org.xml.sax.EntityResolver
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource(java.net.URI) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource(java.net.URI,org.netbeans.modules.xml.xam.ModelSource) throws org.netbeans.modules.xml.xam.locator.CatalogModelException

CLSS public org.netbeans.modules.xml.xam.locator.CatalogModelException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract org.netbeans.modules.xml.xam.locator.CatalogModelFactory
cons public init()
innr public static Default
meth public abstract org.netbeans.modules.xml.xam.locator.CatalogModel getCatalogModel(org.netbeans.modules.xml.xam.ModelSource) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public abstract org.w3c.dom.ls.LSResourceResolver getLSResourceResolver()
meth public static org.netbeans.modules.xml.xam.locator.CatalogModelFactory getDefault()
supr java.lang.Object
hfds implObj

CLSS public static org.netbeans.modules.xml.xam.locator.CatalogModelFactory$Default
 outer org.netbeans.modules.xml.xam.locator.CatalogModelFactory
cons public init()
meth public org.netbeans.modules.xml.xam.locator.CatalogModel getCatalogModel(org.netbeans.modules.xml.xam.ModelSource) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public org.w3c.dom.ls.LSResourceResolver getLSResourceResolver()
supr org.netbeans.modules.xml.xam.locator.CatalogModelFactory

CLSS public abstract interface org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider
intf org.netbeans.modules.xml.xam.spi.ModelAccessProvider
meth public abstract javax.swing.text.Document loadSwingDocument(java.io.InputStream) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentModelAccess createModelAccess(org.netbeans.modules.xml.xam.dom.AbstractDocumentModel)

CLSS public abstract interface org.netbeans.modules.xml.xam.spi.ModelAccessProvider
meth public abstract java.lang.Object getModelSourceKey(org.netbeans.modules.xml.xam.ModelSource)

CLSS public org.netbeans.modules.xml.xam.spi.Validation
cons public init()
innr public final static !enum ValidationType
meth public java.util.List<org.netbeans.modules.xml.xam.Model> getValidatedModels()
meth public java.util.List<org.netbeans.modules.xml.xam.spi.Validator$ResultItem> getValidationResult()
meth public static void stop()
meth public void validate(org.netbeans.modules.xml.xam.Model,org.netbeans.modules.xml.xam.spi.Validation$ValidationType)
supr java.lang.Object
hfds isStopped,myValidatedModels,myValidationResult,ourValidators

CLSS public final static !enum org.netbeans.modules.xml.xam.spi.Validation$ValidationType
 outer org.netbeans.modules.xml.xam.spi.Validation
fld public final static org.netbeans.modules.xml.xam.spi.Validation$ValidationType COMPLETE
fld public final static org.netbeans.modules.xml.xam.spi.Validation$ValidationType PARTIAL
meth public static org.netbeans.modules.xml.xam.spi.Validation$ValidationType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xam.spi.Validation$ValidationType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xam.spi.Validation$ValidationType>

CLSS public org.netbeans.modules.xml.xam.spi.ValidationResult
cons public init(java.util.Collection<org.netbeans.modules.xml.xam.spi.Validator$ResultItem>,java.util.Collection<org.netbeans.modules.xml.xam.Model>)
meth public java.util.Collection<org.netbeans.modules.xml.xam.Model> getValidatedModels()
meth public java.util.Collection<org.netbeans.modules.xml.xam.spi.Validator$ResultItem> getValidationResult()
supr java.lang.Object
hfds validatedModels,validationResult

CLSS public abstract interface org.netbeans.modules.xml.xam.spi.Validator
innr public final static !enum ResultType
innr public static ResultItem
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.xml.xam.spi.ValidationResult validate(org.netbeans.modules.xml.xam.Model,org.netbeans.modules.xml.xam.spi.Validation,org.netbeans.modules.xml.xam.spi.Validation$ValidationType)

CLSS public static org.netbeans.modules.xml.xam.spi.Validator$ResultItem
 outer org.netbeans.modules.xml.xam.spi.Validator
cons public init(org.netbeans.modules.xml.xam.spi.Validator,org.netbeans.modules.xml.xam.spi.Validator$ResultType,java.lang.String,int,int,org.netbeans.modules.xml.xam.Model)
cons public init(org.netbeans.modules.xml.xam.spi.Validator,org.netbeans.modules.xml.xam.spi.Validator$ResultType,org.netbeans.modules.xml.xam.Component,java.lang.String)
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getDescription()
meth public org.netbeans.modules.xml.xam.Component getComponents()
meth public org.netbeans.modules.xml.xam.Model getModel()
meth public org.netbeans.modules.xml.xam.spi.Validator getValidator()
meth public org.netbeans.modules.xml.xam.spi.Validator$ResultType getType()
meth public void setDescription(java.lang.String)
supr java.lang.Object
hfds columnNumber,component,description,lineNumber,model,type,validator

CLSS public final static !enum org.netbeans.modules.xml.xam.spi.Validator$ResultType
 outer org.netbeans.modules.xml.xam.spi.Validator
fld public final static org.netbeans.modules.xml.xam.spi.Validator$ResultType ADVICE
fld public final static org.netbeans.modules.xml.xam.spi.Validator$ResultType ERROR
fld public final static org.netbeans.modules.xml.xam.spi.Validator$ResultType WARNING
meth public static org.netbeans.modules.xml.xam.spi.Validator$ResultType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.xam.spi.Validator$ResultType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.xam.spi.Validator$ResultType>

CLSS public abstract org.netbeans.modules.xml.xam.spi.XsdBasedValidator
cons public init()
innr protected Handler
intf org.netbeans.modules.xml.xam.spi.Validator
meth protected abstract javax.xml.validation.Schema getSchema(org.netbeans.modules.xml.xam.Model)
meth protected javax.xml.transform.Source getSource(org.netbeans.modules.xml.xam.Model,org.netbeans.modules.xml.xam.spi.XsdBasedValidator$Handler)
meth protected javax.xml.validation.Schema getCompiledSchema(java.io.InputStream[],org.w3c.dom.ls.LSResourceResolver)
meth protected javax.xml.validation.Schema getCompiledSchema(javax.xml.transform.Source[],org.w3c.dom.ls.LSResourceResolver,org.xml.sax.ErrorHandler)
meth protected void validate(org.netbeans.modules.xml.xam.Model,javax.xml.validation.Schema,org.netbeans.modules.xml.xam.spi.XsdBasedValidator$Handler)
meth public org.netbeans.modules.xml.xam.dom.DocumentModel resolveResource(java.lang.String,org.netbeans.modules.xml.xam.Model)
meth public org.netbeans.modules.xml.xam.spi.ValidationResult validate(org.netbeans.modules.xml.xam.Model,org.netbeans.modules.xml.xam.spi.Validation,org.netbeans.modules.xml.xam.spi.Validation$ValidationType)
supr java.lang.Object

CLSS protected org.netbeans.modules.xml.xam.spi.XsdBasedValidator$Handler
 outer org.netbeans.modules.xml.xam.spi.XsdBasedValidator
cons public init(org.netbeans.modules.xml.xam.spi.XsdBasedValidator,org.netbeans.modules.xml.xam.Model)
intf org.xml.sax.ErrorHandler
meth public java.util.Collection<org.netbeans.modules.xml.xam.spi.Validator$ResultItem> getResultItems()
meth public void addResultsFromHandlers(java.util.Collection<org.netbeans.modules.xml.xam.spi.XsdBasedValidator$Handler>)
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void logValidationErrors(org.netbeans.modules.xml.xam.spi.Validator$ResultType,java.lang.String)
meth public void logValidationErrors(org.netbeans.modules.xml.xam.spi.Validator$ResultType,java.lang.String,int,int)
meth public void logValidationErrors(org.netbeans.modules.xml.xam.spi.Validator$ResultType,org.xml.sax.SAXParseException)
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object
hfds linePositions,model,relatedHandlers,resultItems

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

CLSS public abstract interface org.w3c.dom.ls.LSResourceResolver
meth public abstract org.w3c.dom.ls.LSInput resolveResource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

