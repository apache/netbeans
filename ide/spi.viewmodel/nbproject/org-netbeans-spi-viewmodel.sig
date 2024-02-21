#Signature file v4.1
#Version 1.74

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

CLSS public abstract interface org.netbeans.spi.viewmodel.AsynchronousModelFilter
fld public final static java.util.concurrent.Executor CURRENT_THREAD
fld public final static java.util.concurrent.Executor DEFAULT
innr public final static !enum CALL
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.util.concurrent.Executor asynchronous(java.util.concurrent.Executor,org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public final static !enum org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL
 outer org.netbeans.spi.viewmodel.AsynchronousModelFilter
fld public final static org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL CHILDREN
fld public final static org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL DISPLAY_NAME
fld public final static org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL SHORT_DESCRIPTION
fld public final static org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL VALUE
meth public static org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL valueOf(java.lang.String)
meth public static org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL[] values()
supr java.lang.Enum<org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL>

CLSS public abstract org.netbeans.spi.viewmodel.CachedChildrenTreeModel
cons public init()
intf org.netbeans.spi.viewmodel.AsynchronousModelFilter
intf org.netbeans.spi.viewmodel.TreeModel
meth protected abstract java.lang.Object[] computeChildren(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth protected boolean cacheChildrenOf(java.lang.Object)
meth protected final void clearCache()
meth protected final void recomputeChildren() throws org.netbeans.spi.viewmodel.UnknownTypeException
meth protected final void recomputeChildren(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth protected final void refreshCache(java.lang.Object)
meth protected java.lang.Object[] reorder(java.lang.Object[])
meth public final java.lang.Object[] getChildren(java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.util.concurrent.Executor asynchronous(java.util.concurrent.Executor,org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object
hfds childrenCache,childrenToRefresh
hcls ChildrenTree

CLSS public abstract interface org.netbeans.spi.viewmodel.CheckNodeModel
intf org.netbeans.spi.viewmodel.NodeModel
meth public abstract boolean isCheckEnabled(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean isCheckable(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Boolean isSelected(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setSelected(java.lang.Object,java.lang.Boolean) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.CheckNodeModelFilter
intf org.netbeans.spi.viewmodel.NodeModelFilter
meth public abstract boolean isCheckEnabled(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean isCheckable(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Boolean isSelected(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setSelected(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object,java.lang.Boolean) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract org.netbeans.spi.viewmodel.ColumnModel
cons public init()
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.Class getType()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getID()
meth public boolean isSortable()
meth public boolean isSorted()
meth public boolean isSortedDescending()
meth public boolean isVisible()
meth public int getColumnWidth()
meth public int getCurrentOrderNumber()
meth public java.beans.PropertyEditor getPropertyEditor()
meth public java.lang.Character getDisplayedMnemonic()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getNextColumnID()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getPreviuosColumnID()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getShortDescription()
meth public void setColumnWidth(int)
meth public void setCurrentOrderNumber(int)
meth public void setSorted(boolean)
meth public void setSortedDescending(boolean)
meth public void setVisible(boolean)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.viewmodel.DnDNodeModel
intf org.netbeans.spi.viewmodel.NodeModel
meth public abstract int getAllowedDragActions()
meth public abstract int getAllowedDropActions(java.awt.datatransfer.Transferable)
meth public abstract java.awt.datatransfer.Transferable drag(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract org.openide.util.datatransfer.PasteType getDropType(java.lang.Object,java.awt.datatransfer.Transferable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.DnDNodeModelFilter
intf org.netbeans.spi.viewmodel.NodeModelFilter
meth public abstract int getAllowedDragActions(org.netbeans.spi.viewmodel.DnDNodeModel)
meth public abstract int getAllowedDropActions(org.netbeans.spi.viewmodel.DnDNodeModel,java.awt.datatransfer.Transferable)
meth public abstract java.awt.datatransfer.Transferable drag(org.netbeans.spi.viewmodel.DnDNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract org.openide.util.datatransfer.PasteType getDropType(org.netbeans.spi.viewmodel.DnDNodeModel,java.lang.Object,java.awt.datatransfer.Transferable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.ExtendedNodeModel
intf org.netbeans.spi.viewmodel.NodeModel
meth public abstract boolean canCopy(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canCut(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canRename(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCopy(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCut(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBaseWithExtension(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setName(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.ExtendedNodeModelFilter
intf org.netbeans.spi.viewmodel.NodeModelFilter
meth public abstract boolean canCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canRename(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCopy(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.awt.datatransfer.Transferable clipboardCut(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBaseWithExtension(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setName(org.netbeans.spi.viewmodel.ExtendedNodeModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.Model

CLSS public org.netbeans.spi.viewmodel.ModelEvent
innr public static NodeChanged
innr public static SelectionChanged
innr public static TableValueChanged
innr public static TreeChanged
supr java.util.EventObject

CLSS public static org.netbeans.spi.viewmodel.ModelEvent$NodeChanged
 outer org.netbeans.spi.viewmodel.ModelEvent
cons public init(java.lang.Object,java.lang.Object)
cons public init(java.lang.Object,java.lang.Object,int)
fld public final static int CHILDREN_MASK = 8
fld public final static int DISPLAY_NAME_MASK = 1
fld public final static int EXPANSION_MASK = 16
fld public final static int ICON_MASK = 2
fld public final static int SHORT_DESCRIPTION_MASK = 4
meth public int getChange()
meth public java.lang.Object getNode()
meth public java.lang.String toString()
supr org.netbeans.spi.viewmodel.ModelEvent
hfds change,node

CLSS public static org.netbeans.spi.viewmodel.ModelEvent$SelectionChanged
 outer org.netbeans.spi.viewmodel.ModelEvent
cons public !varargs init(java.lang.Object,java.lang.Object[])
meth public java.lang.Object[] getNodes()
meth public java.lang.String toString()
supr org.netbeans.spi.viewmodel.ModelEvent
hfds nodes

CLSS public static org.netbeans.spi.viewmodel.ModelEvent$TableValueChanged
 outer org.netbeans.spi.viewmodel.ModelEvent
cons public init(java.lang.Object,java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.Object,java.lang.String,int)
fld public final static int HTML_VALUE_MASK = 2
fld public final static int IS_READ_ONLY_MASK = 4
fld public final static int VALUE_MASK = 1
meth public int getChange()
meth public java.lang.Object getNode()
meth public java.lang.String getColumnID()
supr org.netbeans.spi.viewmodel.ModelEvent
hfds change,columnID,node

CLSS public static org.netbeans.spi.viewmodel.ModelEvent$TreeChanged
 outer org.netbeans.spi.viewmodel.ModelEvent
cons public init(java.lang.Object)
supr org.netbeans.spi.viewmodel.ModelEvent

CLSS public abstract interface org.netbeans.spi.viewmodel.ModelListener
intf java.util.EventListener
meth public abstract void modelChanged(org.netbeans.spi.viewmodel.ModelEvent)

CLSS public final org.netbeans.spi.viewmodel.Models
cons public init()
fld public static int MULTISELECTION_TYPE_ALL
fld public static int MULTISELECTION_TYPE_ANY
fld public static int MULTISELECTION_TYPE_EXACTLY_ONE
fld public static org.netbeans.spi.viewmodel.Models$CompoundModel EMPTY_MODEL
innr public abstract interface static ActionPerformer
innr public abstract static TreeFeatures
innr public final static CompoundModel
meth public static javax.swing.Action createAction(java.lang.String,org.netbeans.spi.viewmodel.Models$ActionPerformer,int)
meth public static javax.swing.JComponent createView(org.netbeans.spi.viewmodel.Models$CompoundModel)
meth public static org.netbeans.spi.viewmodel.Models$CompoundModel createCompoundModel(java.util.List)
meth public static org.netbeans.spi.viewmodel.Models$CompoundModel createCompoundModel(java.util.List,java.lang.String)
meth public static org.netbeans.spi.viewmodel.Models$TreeFeatures treeFeatures(javax.swing.JComponent)
meth public static org.openide.nodes.Node createNodes(org.netbeans.spi.viewmodel.Models$CompoundModel,org.openide.explorer.view.TreeView)
meth public static void setModelsToView(javax.swing.JComponent,org.netbeans.spi.viewmodel.Models$CompoundModel)
supr java.lang.Object
hfds DEFAULT_DRAG_DROP_ALLOWED_ACTIONS,defaultExpansionModels,verbose
hcls ActionSupport,CompoundAsynchronousModel,CompoundNodeActionsProvider,CompoundNodeModel,CompoundTableModel,CompoundTablePropertyEditorsModel,CompoundTableRendererModel,CompoundTreeExpansionModel,CompoundTreeModel,DefaultAsynchronousModel,DefaultTreeExpansionModel,DefaultTreeFeatures,DelegatingNodeActionsProvider,DelegatingNodeModel,DelegatingTableModel,DelegatingTablePropertyEditorsModel,DelegatingTableRendererModel,DelegatingTreeExpansionModel,DelegatingTreeModel,EmptyNodeActionsProvider,EmptyNodeModel,EmptyTableModel,EmptyTreeModel,ModelLists

CLSS public abstract interface static org.netbeans.spi.viewmodel.Models$ActionPerformer
 outer org.netbeans.spi.viewmodel.Models
meth public abstract boolean isEnabled(java.lang.Object)
meth public abstract void perform(java.lang.Object[])

CLSS public final static org.netbeans.spi.viewmodel.Models$CompoundModel
 outer org.netbeans.spi.viewmodel.Models
intf org.netbeans.spi.viewmodel.CheckNodeModel
intf org.netbeans.spi.viewmodel.DnDNodeModel
intf org.netbeans.spi.viewmodel.ExtendedNodeModel
intf org.netbeans.spi.viewmodel.NodeActionsProvider
intf org.netbeans.spi.viewmodel.ReorderableTreeModel
intf org.netbeans.spi.viewmodel.TableHTMLModel
intf org.netbeans.spi.viewmodel.TablePropertyEditorsModel
intf org.netbeans.spi.viewmodel.TableRendererModel
intf org.netbeans.spi.viewmodel.TreeExpansionModel
meth public boolean canCopy(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canCut(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canEditCell(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canRename(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canRenderCell(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean canReorder(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean hasHTMLValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isCheckEnabled(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isCheckable(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isExpanded(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isLeaf(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isReadOnly(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public int getAllowedDragActions()
meth public int getAllowedDropActions(java.awt.datatransfer.Transferable)
meth public int getChildrenCount(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable clipboardCopy(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable clipboardCut(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.awt.datatransfer.Transferable drag(java.lang.Object) throws java.io.IOException,org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.beans.PropertyEditor getPropertyEditor(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Boolean isSelected(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object getRoot()
meth public java.lang.Object getValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object[] getChildren(java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getHTMLValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getHelpId()
meth public java.lang.String getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getIconBaseWithExtension(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String toString()
meth public java.util.concurrent.Executor asynchronous(org.netbeans.spi.viewmodel.AsynchronousModelFilter$CALL,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public javax.swing.Action[] getActions(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public javax.swing.table.TableCellEditor getCellEditor(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public javax.swing.table.TableCellRenderer getCellRenderer(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public org.netbeans.spi.viewmodel.ColumnModel[] getColumns()
meth public org.openide.util.datatransfer.PasteType getDropType(java.lang.Object,java.awt.datatransfer.Transferable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public org.openide.util.datatransfer.PasteType[] getPasteTypes(java.lang.Object,java.awt.datatransfer.Transferable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void nodeCollapsed(java.lang.Object)
meth public void nodeExpanded(java.lang.Object)
meth public void performDefaultAction(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public void reorder(java.lang.Object,int[]) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void setName(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void setSelected(java.lang.Object,java.lang.Boolean) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void setValueAt(java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object
hfds asynchModel,cnodeModel,columnModels,dndNodeModel,mainSubModel,nodeActionsProvider,nodeModel,propertiesHelpID,subModels,subModelsFilter,tableModel,tablePropertyEditorsModel,tableRendererModel,treeExpansionModel,treeModel,treeNodeDisplayFormat

CLSS public abstract static org.netbeans.spi.viewmodel.Models$TreeFeatures
 outer org.netbeans.spi.viewmodel.Models
cons public init()
meth public abstract boolean isExpanded(java.lang.Object)
meth public abstract void collapseNode(java.lang.Object)
meth public abstract void expandNode(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeActionsProvider
intf org.netbeans.spi.viewmodel.Model
meth public abstract javax.swing.Action[] getActions(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void performDefaultAction(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeActionsProviderFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.String getDisplayName(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBase(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getShortDescription(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.NodeModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.lang.String getDisplayName(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBase(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getShortDescription(org.netbeans.spi.viewmodel.NodeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.ReorderableTreeModel
intf org.netbeans.spi.viewmodel.TreeModel
meth public abstract boolean canReorder(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void reorder(java.lang.Object,int[]) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.ReorderableTreeModelFilter
intf org.netbeans.spi.viewmodel.TreeModelFilter
meth public abstract boolean canReorder(org.netbeans.spi.viewmodel.ReorderableTreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void reorder(org.netbeans.spi.viewmodel.ReorderableTreeModel,java.lang.Object,int[]) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TableHTMLModel
intf org.netbeans.spi.viewmodel.TableModel
meth public abstract boolean hasHTMLValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getHTMLValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TableHTMLModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean hasHTMLValueAt(org.netbeans.spi.viewmodel.TableHTMLModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getHTMLValueAt(org.netbeans.spi.viewmodel.TableHTMLModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TableModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isReadOnly(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getValueAt(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void setValueAt(java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TableModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isReadOnly(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void setValueAt(org.netbeans.spi.viewmodel.TableModel,java.lang.Object,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TablePropertyEditorsModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.beans.PropertyEditor getPropertyEditor(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TablePropertyEditorsModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract java.beans.PropertyEditor getPropertyEditor(org.netbeans.spi.viewmodel.TablePropertyEditorsModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException

CLSS public abstract interface org.netbeans.spi.viewmodel.TableRendererModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean canEditCell(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canRenderCell(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract javax.swing.table.TableCellEditor getCellEditor(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract javax.swing.table.TableCellRenderer getCellRenderer(java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TableRendererModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean canEditCell(org.netbeans.spi.viewmodel.TableRendererModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean canRenderCell(org.netbeans.spi.viewmodel.TableRendererModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract javax.swing.table.TableCellEditor getCellEditor(org.netbeans.spi.viewmodel.TableRendererModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract javax.swing.table.TableCellRenderer getCellRenderer(org.netbeans.spi.viewmodel.TableRendererModel,java.lang.Object,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeExpansionModel
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isExpanded(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void nodeCollapsed(java.lang.Object)
meth public abstract void nodeExpanded(java.lang.Object)

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeExpansionModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isExpanded(org.netbeans.spi.viewmodel.TreeExpansionModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void nodeCollapsed(java.lang.Object)
meth public abstract void nodeExpanded(java.lang.Object)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeModel
fld public final static java.lang.String ROOT = "Root"
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isLeaf(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int getChildrenCount(java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getRoot()
meth public abstract java.lang.Object[] getChildren(java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public abstract interface org.netbeans.spi.viewmodel.TreeModelFilter
intf org.netbeans.spi.viewmodel.Model
meth public abstract boolean isLeaf(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getRoot(org.netbeans.spi.viewmodel.TreeModel)
meth public abstract java.lang.Object[] getChildren(org.netbeans.spi.viewmodel.TreeModel,java.lang.Object,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void addModelListener(org.netbeans.spi.viewmodel.ModelListener)
meth public abstract void removeModelListener(org.netbeans.spi.viewmodel.ModelListener)

CLSS public org.netbeans.spi.viewmodel.UnknownTypeException
cons public init(java.lang.Object)
meth public java.lang.String getMessage()
supr java.lang.Exception
hfds node

