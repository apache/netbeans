#Signature file v4.1
#Version 2.71

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

CLSS public abstract interface org.netbeans.api.visual.action.AcceptProvider
meth public abstract org.netbeans.api.visual.action.ConnectorState isAcceptable(org.netbeans.api.visual.widget.Widget,java.awt.Point,java.awt.datatransfer.Transferable)
meth public abstract void accept(org.netbeans.api.visual.widget.Widget,java.awt.Point,java.awt.datatransfer.Transferable)

CLSS public final org.netbeans.api.visual.action.ActionFactory
meth public static <%0 extends javax.swing.JComponent> org.netbeans.api.visual.action.WidgetAction createInplaceEditorAction(org.netbeans.api.visual.action.InplaceEditorProvider<{%%0}>)
meth public static org.netbeans.api.visual.action.AlignWithMoveDecorator createDefaultAlignWithMoveDecorator()
meth public static org.netbeans.api.visual.action.ConnectDecorator createDefaultConnectDecorator()
meth public static org.netbeans.api.visual.action.InplaceEditorProvider$EditorController getInplaceEditorController(org.netbeans.api.visual.action.WidgetAction)
meth public static org.netbeans.api.visual.action.MoveControlPointProvider createFreeMoveControlPointProvider()
meth public static org.netbeans.api.visual.action.MoveControlPointProvider createOrthogonalMoveControlPointProvider()
meth public static org.netbeans.api.visual.action.MoveProvider createDefaultMoveProvider()
meth public static org.netbeans.api.visual.action.MoveStrategy createFreeMoveStrategy()
meth public static org.netbeans.api.visual.action.MoveStrategy createSnapToGridMoveStrategy(int,int)
meth public static org.netbeans.api.visual.action.ReconnectDecorator createDefaultReconnectDecorator()
meth public static org.netbeans.api.visual.action.RectangularSelectDecorator createDefaultRectangularSelectDecorator(org.netbeans.api.visual.widget.Scene)
meth public static org.netbeans.api.visual.action.RectangularSelectProvider createObjectSceneRectangularSelectProvider(org.netbeans.api.visual.model.ObjectScene)
meth public static org.netbeans.api.visual.action.ResizeControlPointResolver createDefaultResizeControlPointResolver()
meth public static org.netbeans.api.visual.action.ResizeProvider createDefaultResizeProvider()
meth public static org.netbeans.api.visual.action.ResizeStrategy createFreeResizeStategy()
meth public static org.netbeans.api.visual.action.WidgetAction createAcceptAction(org.netbeans.api.visual.action.AcceptProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createActionMapAction()
meth public static org.netbeans.api.visual.action.WidgetAction createActionMapAction(javax.swing.InputMap,javax.swing.ActionMap)
meth public static org.netbeans.api.visual.action.WidgetAction createAddRemoveControlPointAction()
meth public static org.netbeans.api.visual.action.WidgetAction createAddRemoveControlPointAction(double,double)
meth public static org.netbeans.api.visual.action.WidgetAction createAddRemoveControlPointAction(double,double,org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy)
meth public static org.netbeans.api.visual.action.WidgetAction createAlignWithMoveAction(org.netbeans.api.visual.action.AlignWithWidgetCollector,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.AlignWithMoveDecorator)
meth public static org.netbeans.api.visual.action.WidgetAction createAlignWithMoveAction(org.netbeans.api.visual.action.AlignWithWidgetCollector,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.AlignWithMoveDecorator,boolean)
meth public static org.netbeans.api.visual.action.WidgetAction createAlignWithMoveAction(org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.AlignWithMoveDecorator)
meth public static org.netbeans.api.visual.action.WidgetAction createAlignWithMoveAction(org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.AlignWithMoveDecorator,boolean)
meth public static org.netbeans.api.visual.action.WidgetAction createAlignWithResizeAction(org.netbeans.api.visual.action.AlignWithWidgetCollector,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.AlignWithMoveDecorator)
meth public static org.netbeans.api.visual.action.WidgetAction createAlignWithResizeAction(org.netbeans.api.visual.action.AlignWithWidgetCollector,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.AlignWithMoveDecorator,boolean)
meth public static org.netbeans.api.visual.action.WidgetAction createAlignWithResizeAction(org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.AlignWithMoveDecorator)
meth public static org.netbeans.api.visual.action.WidgetAction createAlignWithResizeAction(org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.AlignWithMoveDecorator,boolean)
meth public static org.netbeans.api.visual.action.WidgetAction createCenteredZoomAction(double)
meth public static org.netbeans.api.visual.action.WidgetAction createConnectAction(org.netbeans.api.visual.action.ConnectDecorator,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.ConnectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createConnectAction(org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.ConnectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createContiguousSelectAction(org.netbeans.api.visual.action.ContiguousSelectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createCycleFocusAction(org.netbeans.api.visual.action.CycleFocusProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createCycleObjectSceneFocusAction()
meth public static org.netbeans.api.visual.action.WidgetAction createEditAction(org.netbeans.api.visual.action.EditProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createExtendedConnectAction(org.netbeans.api.visual.action.ConnectDecorator,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.ConnectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createExtendedConnectAction(org.netbeans.api.visual.action.ConnectDecorator,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.ConnectProvider,int)
meth public static org.netbeans.api.visual.action.WidgetAction createExtendedConnectAction(org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.ConnectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createForwardKeyEventsAction(org.netbeans.api.visual.widget.Widget,java.lang.String)
meth public static org.netbeans.api.visual.action.WidgetAction createFreeMoveControlPointAction()
meth public static org.netbeans.api.visual.action.WidgetAction createHoverAction(org.netbeans.api.visual.action.HoverProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createHoverAction(org.netbeans.api.visual.action.TwoStateHoverProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createInplaceEditorAction(org.netbeans.api.visual.action.TextFieldInplaceEditor)
meth public static org.netbeans.api.visual.action.WidgetAction createInplaceEditorAction(org.netbeans.api.visual.action.TextFieldInplaceEditor,java.util.EnumSet<org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection>)
meth public static org.netbeans.api.visual.action.WidgetAction createMouseCenteredZoomAction(double)
meth public static org.netbeans.api.visual.action.WidgetAction createMoveAction()
meth public static org.netbeans.api.visual.action.WidgetAction createMoveAction(org.netbeans.api.visual.action.MoveStrategy,org.netbeans.api.visual.action.MoveProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createMoveControlPointAction(org.netbeans.api.visual.action.MoveControlPointProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createMoveControlPointAction(org.netbeans.api.visual.action.MoveControlPointProvider,org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy)
meth public static org.netbeans.api.visual.action.WidgetAction createOrthogonalMoveControlPointAction()
meth public static org.netbeans.api.visual.action.WidgetAction createPanAction()
meth public static org.netbeans.api.visual.action.WidgetAction createPopupMenuAction(org.netbeans.api.visual.action.PopupMenuProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createReconnectAction(org.netbeans.api.visual.action.ReconnectDecorator,org.netbeans.api.visual.action.ReconnectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createReconnectAction(org.netbeans.api.visual.action.ReconnectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createRectangularSelectAction(org.netbeans.api.visual.action.RectangularSelectDecorator,org.netbeans.api.visual.widget.LayerWidget,org.netbeans.api.visual.action.RectangularSelectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createRectangularSelectAction(org.netbeans.api.visual.model.ObjectScene,org.netbeans.api.visual.widget.LayerWidget)
meth public static org.netbeans.api.visual.action.WidgetAction createResizeAction()
meth public static org.netbeans.api.visual.action.WidgetAction createResizeAction(org.netbeans.api.visual.action.ResizeStrategy,org.netbeans.api.visual.action.ResizeControlPointResolver,org.netbeans.api.visual.action.ResizeProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createResizeAction(org.netbeans.api.visual.action.ResizeStrategy,org.netbeans.api.visual.action.ResizeProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createSelectAction(org.netbeans.api.visual.action.SelectProvider)
meth public static org.netbeans.api.visual.action.WidgetAction createSelectAction(org.netbeans.api.visual.action.SelectProvider,boolean)
meth public static org.netbeans.api.visual.action.WidgetAction createSwitchCardAction(org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.action.WidgetAction createWheelPanAction()
meth public static org.netbeans.api.visual.action.WidgetAction createZoomAction()
meth public static org.netbeans.api.visual.action.WidgetAction createZoomAction(double,boolean)
supr java.lang.Object
hfds ACTION_MAP_ACTION,ALIGN_WITH_MOVE_DECORATOR_DEFAULT,CONNECT_DECORATOR_DEFAULT,CYCLE_FOCUS_OBJECT_SCENE,MOVE_ACTION,MOVE_CONTROL_POINT_ACTION_FREE,MOVE_CONTROL_POINT_ACTION_ORTHOGONAL,MOVE_CONTROL_POINT_PROVIDER_FREE,MOVE_CONTROL_POINT_PROVIDER_ORTHOGONAL,MOVE_PROVIDER_DEFAULT,MOVE_STRATEGY_FREE,PAN_ACTION,RECONNECT_DECORATOR_DEFAULT,RESIZE_ACTION,RESIZE_CONTROL_POINT_RESOLVER_DEFAULT,RESIZE_PROVIDER_DEFAULT,RESIZE_STRATEGY_FREE,STROKE,WHEEL_PAN_ACTION

CLSS public abstract interface org.netbeans.api.visual.action.AlignWithMoveDecorator
meth public abstract org.netbeans.api.visual.widget.ConnectionWidget createLineWidget(org.netbeans.api.visual.widget.Scene)

CLSS public abstract interface org.netbeans.api.visual.action.AlignWithWidgetCollector
meth public abstract java.util.Collection<java.awt.Rectangle> getRegions(org.netbeans.api.visual.widget.Widget)

CLSS public abstract interface org.netbeans.api.visual.action.ConnectDecorator
meth public abstract org.netbeans.api.visual.anchor.Anchor createFloatAnchor(java.awt.Point)
meth public abstract org.netbeans.api.visual.anchor.Anchor createSourceAnchor(org.netbeans.api.visual.widget.Widget)
meth public abstract org.netbeans.api.visual.anchor.Anchor createTargetAnchor(org.netbeans.api.visual.widget.Widget)
meth public abstract org.netbeans.api.visual.widget.ConnectionWidget createConnectionWidget(org.netbeans.api.visual.widget.Scene)

CLSS public abstract interface org.netbeans.api.visual.action.ConnectProvider
meth public abstract boolean hasCustomTargetWidgetResolver(org.netbeans.api.visual.widget.Scene)
meth public abstract boolean isSourceWidget(org.netbeans.api.visual.widget.Widget)
meth public abstract org.netbeans.api.visual.action.ConnectorState isTargetWidget(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.widget.Widget)
meth public abstract org.netbeans.api.visual.widget.Widget resolveTargetWidget(org.netbeans.api.visual.widget.Scene,java.awt.Point)
meth public abstract void createConnection(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.widget.Widget)

CLSS public final !enum org.netbeans.api.visual.action.ConnectorState
fld public final static org.netbeans.api.visual.action.ConnectorState ACCEPT
fld public final static org.netbeans.api.visual.action.ConnectorState REJECT
fld public final static org.netbeans.api.visual.action.ConnectorState REJECT_AND_STOP
meth public static org.netbeans.api.visual.action.ConnectorState valueOf(java.lang.String)
meth public static org.netbeans.api.visual.action.ConnectorState[] values()
supr java.lang.Enum<org.netbeans.api.visual.action.ConnectorState>

CLSS public final org.netbeans.api.visual.action.ContiguousSelectEvent
innr public final static !enum SelectionType
meth public java.awt.Point getChoosenLocalLocation()
meth public java.awt.Point getPreviouslyChoosenLocalLocation()
meth public org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType getSelectionType()
meth public org.netbeans.api.visual.widget.Widget getChoosenWidget()
meth public org.netbeans.api.visual.widget.Widget getPreviouslyChoosenWidget()
meth public static org.netbeans.api.visual.action.ContiguousSelectEvent create(org.netbeans.api.visual.widget.Widget,java.awt.Point,org.netbeans.api.visual.widget.Widget,java.awt.Point,org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType)
supr java.lang.Object
hfds choosenLocalLocation,choosenWidget,previouslyChoosenLocalLocation,previouslyChoosenWidget,selectionType

CLSS public final static !enum org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType
 outer org.netbeans.api.visual.action.ContiguousSelectEvent
fld public final static org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType ADDITIVE_CONTIGUOUS
fld public final static org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType ADDITIVE_NON_CONTIGUOUS
fld public final static org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType REPLACE_CONTIGUOUS
fld public final static org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType REPLACE_NON_CONTIGUOUS
meth public static org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType valueOf(java.lang.String)
meth public static org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType[] values()
supr java.lang.Enum<org.netbeans.api.visual.action.ContiguousSelectEvent$SelectionType>

CLSS public abstract interface org.netbeans.api.visual.action.ContiguousSelectProvider
meth public abstract boolean isSelectionAllowed(org.netbeans.api.visual.action.ContiguousSelectEvent)
meth public abstract void select(org.netbeans.api.visual.action.ContiguousSelectEvent)

CLSS public abstract interface org.netbeans.api.visual.action.CycleFocusProvider
meth public abstract boolean switchNextFocus(org.netbeans.api.visual.widget.Widget)
meth public abstract boolean switchPreviousFocus(org.netbeans.api.visual.widget.Widget)

CLSS public abstract interface org.netbeans.api.visual.action.EditProvider
meth public abstract void edit(org.netbeans.api.visual.widget.Widget)

CLSS public abstract interface org.netbeans.api.visual.action.HoverProvider
meth public abstract void widgetHovered(org.netbeans.api.visual.widget.Widget)

CLSS public abstract interface org.netbeans.api.visual.action.InplaceEditorProvider<%0 extends javax.swing.JComponent>
innr public abstract interface static EditorController
innr public abstract interface static TypedEditorController
innr public final static !enum EditorInvocationType
innr public final static !enum ExpansionDirection
meth public abstract java.awt.Rectangle getInitialEditorComponentBounds(org.netbeans.api.visual.action.InplaceEditorProvider$EditorController,org.netbeans.api.visual.widget.Widget,{org.netbeans.api.visual.action.InplaceEditorProvider%0},java.awt.Rectangle)
meth public abstract java.util.EnumSet<org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection> getExpansionDirections(org.netbeans.api.visual.action.InplaceEditorProvider$EditorController,org.netbeans.api.visual.widget.Widget,{org.netbeans.api.visual.action.InplaceEditorProvider%0})
meth public abstract void notifyClosing(org.netbeans.api.visual.action.InplaceEditorProvider$EditorController,org.netbeans.api.visual.widget.Widget,{org.netbeans.api.visual.action.InplaceEditorProvider%0},boolean)
meth public abstract void notifyOpened(org.netbeans.api.visual.action.InplaceEditorProvider$EditorController,org.netbeans.api.visual.widget.Widget,{org.netbeans.api.visual.action.InplaceEditorProvider%0})
meth public abstract {org.netbeans.api.visual.action.InplaceEditorProvider%0} createEditorComponent(org.netbeans.api.visual.action.InplaceEditorProvider$EditorController,org.netbeans.api.visual.widget.Widget)

CLSS public abstract interface static org.netbeans.api.visual.action.InplaceEditorProvider$EditorController
 outer org.netbeans.api.visual.action.InplaceEditorProvider
meth public abstract boolean isEditorVisible()
meth public abstract boolean openEditor(org.netbeans.api.visual.widget.Widget)
meth public abstract void closeEditor(boolean)
meth public abstract void notifyEditorComponentBoundsChanged()

CLSS public final static !enum org.netbeans.api.visual.action.InplaceEditorProvider$EditorInvocationType
 outer org.netbeans.api.visual.action.InplaceEditorProvider
fld public final static org.netbeans.api.visual.action.InplaceEditorProvider$EditorInvocationType CODE
fld public final static org.netbeans.api.visual.action.InplaceEditorProvider$EditorInvocationType KEY
fld public final static org.netbeans.api.visual.action.InplaceEditorProvider$EditorInvocationType MOUSE
meth public static org.netbeans.api.visual.action.InplaceEditorProvider$EditorInvocationType valueOf(java.lang.String)
meth public static org.netbeans.api.visual.action.InplaceEditorProvider$EditorInvocationType[] values()
supr java.lang.Enum<org.netbeans.api.visual.action.InplaceEditorProvider$EditorInvocationType>

CLSS public final static !enum org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection
 outer org.netbeans.api.visual.action.InplaceEditorProvider
fld public final static org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection BOTTOM
fld public final static org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection LEFT
fld public final static org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection RIGHT
fld public final static org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection TOP
meth public static org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection valueOf(java.lang.String)
meth public static org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection[] values()
supr java.lang.Enum<org.netbeans.api.visual.action.InplaceEditorProvider$ExpansionDirection>

CLSS public abstract interface static org.netbeans.api.visual.action.InplaceEditorProvider$TypedEditorController
 outer org.netbeans.api.visual.action.InplaceEditorProvider
intf org.netbeans.api.visual.action.InplaceEditorProvider$EditorController
meth public abstract org.netbeans.api.visual.action.InplaceEditorProvider$EditorInvocationType getEditorInvocationType()

CLSS public abstract interface org.netbeans.api.visual.action.MoveControlPointProvider
meth public abstract java.util.List<java.awt.Point> locationSuggested(org.netbeans.api.visual.widget.ConnectionWidget,int,java.awt.Point)

CLSS public abstract interface org.netbeans.api.visual.action.MoveProvider
meth public abstract java.awt.Point getOriginalLocation(org.netbeans.api.visual.widget.Widget)
meth public abstract void movementFinished(org.netbeans.api.visual.widget.Widget)
meth public abstract void movementStarted(org.netbeans.api.visual.widget.Widget)
meth public abstract void setNewLocation(org.netbeans.api.visual.widget.Widget,java.awt.Point)

CLSS public abstract interface org.netbeans.api.visual.action.MoveStrategy
meth public abstract java.awt.Point locationSuggested(org.netbeans.api.visual.widget.Widget,java.awt.Point,java.awt.Point)

CLSS public abstract interface org.netbeans.api.visual.action.PopupMenuProvider
meth public abstract javax.swing.JPopupMenu getPopupMenu(org.netbeans.api.visual.widget.Widget,java.awt.Point)

CLSS public abstract interface org.netbeans.api.visual.action.ReconnectDecorator
meth public abstract org.netbeans.api.visual.anchor.Anchor createFloatAnchor(java.awt.Point)
meth public abstract org.netbeans.api.visual.anchor.Anchor createReplacementWidgetAnchor(org.netbeans.api.visual.widget.Widget)

CLSS public abstract interface org.netbeans.api.visual.action.ReconnectProvider
meth public abstract boolean hasCustomReplacementWidgetResolver(org.netbeans.api.visual.widget.Scene)
meth public abstract boolean isSourceReconnectable(org.netbeans.api.visual.widget.ConnectionWidget)
meth public abstract boolean isTargetReconnectable(org.netbeans.api.visual.widget.ConnectionWidget)
meth public abstract org.netbeans.api.visual.action.ConnectorState isReplacementWidget(org.netbeans.api.visual.widget.ConnectionWidget,org.netbeans.api.visual.widget.Widget,boolean)
meth public abstract org.netbeans.api.visual.widget.Widget resolveReplacementWidget(org.netbeans.api.visual.widget.Scene,java.awt.Point)
meth public abstract void reconnect(org.netbeans.api.visual.widget.ConnectionWidget,org.netbeans.api.visual.widget.Widget,boolean)
meth public abstract void reconnectingFinished(org.netbeans.api.visual.widget.ConnectionWidget,boolean)
meth public abstract void reconnectingStarted(org.netbeans.api.visual.widget.ConnectionWidget,boolean)

CLSS public abstract interface org.netbeans.api.visual.action.RectangularSelectDecorator
meth public abstract org.netbeans.api.visual.widget.Widget createSelectionWidget()

CLSS public abstract interface org.netbeans.api.visual.action.RectangularSelectProvider
meth public abstract void performSelection(java.awt.Rectangle)

CLSS public abstract interface org.netbeans.api.visual.action.ResizeControlPointResolver
meth public abstract org.netbeans.api.visual.action.ResizeProvider$ControlPoint resolveControlPoint(org.netbeans.api.visual.widget.Widget,java.awt.Point)

CLSS public abstract interface org.netbeans.api.visual.action.ResizeProvider
innr public final static !enum ControlPoint
meth public abstract void resizingFinished(org.netbeans.api.visual.widget.Widget)
meth public abstract void resizingStarted(org.netbeans.api.visual.widget.Widget)

CLSS public final static !enum org.netbeans.api.visual.action.ResizeProvider$ControlPoint
 outer org.netbeans.api.visual.action.ResizeProvider
fld public final static org.netbeans.api.visual.action.ResizeProvider$ControlPoint BOTTOM_CENTER
fld public final static org.netbeans.api.visual.action.ResizeProvider$ControlPoint BOTTOM_LEFT
fld public final static org.netbeans.api.visual.action.ResizeProvider$ControlPoint BOTTOM_RIGHT
fld public final static org.netbeans.api.visual.action.ResizeProvider$ControlPoint CENTER_LEFT
fld public final static org.netbeans.api.visual.action.ResizeProvider$ControlPoint CENTER_RIGHT
fld public final static org.netbeans.api.visual.action.ResizeProvider$ControlPoint TOP_CENTER
fld public final static org.netbeans.api.visual.action.ResizeProvider$ControlPoint TOP_LEFT
fld public final static org.netbeans.api.visual.action.ResizeProvider$ControlPoint TOP_RIGHT
meth public static org.netbeans.api.visual.action.ResizeProvider$ControlPoint valueOf(java.lang.String)
meth public static org.netbeans.api.visual.action.ResizeProvider$ControlPoint[] values()
supr java.lang.Enum<org.netbeans.api.visual.action.ResizeProvider$ControlPoint>

CLSS public abstract interface org.netbeans.api.visual.action.ResizeStrategy
meth public abstract java.awt.Rectangle boundsSuggested(org.netbeans.api.visual.widget.Widget,java.awt.Rectangle,java.awt.Rectangle,org.netbeans.api.visual.action.ResizeProvider$ControlPoint)

CLSS public abstract interface org.netbeans.api.visual.action.SelectProvider
meth public abstract boolean isAimingAllowed(org.netbeans.api.visual.widget.Widget,java.awt.Point,boolean)
meth public abstract boolean isSelectionAllowed(org.netbeans.api.visual.widget.Widget,java.awt.Point,boolean)
meth public abstract void select(org.netbeans.api.visual.widget.Widget,java.awt.Point,boolean)

CLSS public abstract interface org.netbeans.api.visual.action.TextFieldInplaceEditor
meth public abstract boolean isEnabled(org.netbeans.api.visual.widget.Widget)
meth public abstract java.lang.String getText(org.netbeans.api.visual.widget.Widget)
meth public abstract void setText(org.netbeans.api.visual.widget.Widget,java.lang.String)

CLSS public abstract interface org.netbeans.api.visual.action.TwoStateHoverProvider
meth public abstract void setHovering(org.netbeans.api.visual.widget.Widget)
meth public abstract void unsetHovering(org.netbeans.api.visual.widget.Widget)

CLSS public abstract interface org.netbeans.api.visual.action.WidgetAction
innr public abstract interface static WidgetEvent
innr public abstract interface static WidgetLocationEvent
innr public abstract static LockedAdapter
innr public abstract static State
innr public final static Chain
innr public final static WidgetDropTargetDragEvent
innr public final static WidgetDropTargetDropEvent
innr public final static WidgetDropTargetEvent
innr public final static WidgetFocusEvent
innr public final static WidgetKeyEvent
innr public final static WidgetMouseEvent
innr public final static WidgetMouseWheelEvent
innr public static Adapter
meth public abstract org.netbeans.api.visual.action.WidgetAction$State dragEnter(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State dragExit(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State dragOver(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State drop(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDropEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State dropActionChanged(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State focusGained(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State focusLost(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State keyPressed(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State keyReleased(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State keyTyped(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State mouseClicked(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State mouseDragged(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State mouseEntered(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State mouseExited(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State mouseMoved(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State mousePressed(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State mouseReleased(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public abstract org.netbeans.api.visual.action.WidgetAction$State mouseWheelMoved(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseWheelEvent)

CLSS public static org.netbeans.api.visual.action.WidgetAction$Adapter
 outer org.netbeans.api.visual.action.WidgetAction
cons public init()
intf org.netbeans.api.visual.action.WidgetAction
meth public org.netbeans.api.visual.action.WidgetAction$State dragEnter(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dragExit(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dragOver(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State drop(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDropEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dropActionChanged(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State focusGained(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State focusLost(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyPressed(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyReleased(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyTyped(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseClicked(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseDragged(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseEntered(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseExited(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseMoved(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mousePressed(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseReleased(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseWheelMoved(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseWheelEvent)
supr java.lang.Object

CLSS public final static org.netbeans.api.visual.action.WidgetAction$Chain
 outer org.netbeans.api.visual.action.WidgetAction
cons public init()
intf org.netbeans.api.visual.action.WidgetAction
meth public java.util.List<org.netbeans.api.visual.action.WidgetAction> getActions()
meth public org.netbeans.api.visual.action.WidgetAction$State dragEnter(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dragExit(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dragOver(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State drop(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDropEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dropActionChanged(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State focusGained(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State focusLost(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyPressed(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyReleased(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyTyped(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseClicked(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseDragged(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseEntered(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseExited(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseMoved(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mousePressed(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseReleased(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseWheelMoved(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseWheelEvent)
meth public void addAction(int,org.netbeans.api.visual.action.WidgetAction)
meth public void addAction(org.netbeans.api.visual.action.WidgetAction)
meth public void removeAction(int)
meth public void removeAction(org.netbeans.api.visual.action.WidgetAction)
supr java.lang.Object
hfds actions,actionsUm

CLSS public abstract static org.netbeans.api.visual.action.WidgetAction$LockedAdapter
 outer org.netbeans.api.visual.action.WidgetAction
cons public init()
intf org.netbeans.api.visual.action.WidgetAction
meth protected abstract boolean isLocked()
meth public org.netbeans.api.visual.action.WidgetAction$State dragEnter(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dragExit(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dragOver(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State drop(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDropEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State dropActionChanged(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State focusGained(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State focusLost(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyPressed(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyReleased(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State keyTyped(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseClicked(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseDragged(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseEntered(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseExited(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseMoved(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mousePressed(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseReleased(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent)
meth public org.netbeans.api.visual.action.WidgetAction$State mouseWheelMoved(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction$WidgetMouseWheelEvent)
supr java.lang.Object

CLSS public abstract static org.netbeans.api.visual.action.WidgetAction$State
 outer org.netbeans.api.visual.action.WidgetAction
fld public final static org.netbeans.api.visual.action.WidgetAction$State CHAIN_ONLY
fld public final static org.netbeans.api.visual.action.WidgetAction$State CONSUMED
fld public final static org.netbeans.api.visual.action.WidgetAction$State REJECTED
meth public abstract boolean isConsumed()
meth public abstract boolean isLockedInChain()
meth public abstract org.netbeans.api.visual.action.WidgetAction getLockedAction()
meth public abstract org.netbeans.api.visual.widget.Widget getLockedWidget()
meth public static org.netbeans.api.visual.action.WidgetAction$State createLocked(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.action.WidgetAction)
supr java.lang.Object

CLSS public final static org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDragEvent
 outer org.netbeans.api.visual.action.WidgetAction
cons public init(long,java.awt.dnd.DropTargetDragEvent)
intf org.netbeans.api.visual.action.WidgetAction$WidgetLocationEvent
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public int getDropAction()
meth public int getSourceActions()
meth public java.awt.Point getPoint()
meth public java.awt.datatransfer.DataFlavor[] getCurrentDataFlavors()
meth public java.awt.datatransfer.Transferable getTransferable()
meth public java.awt.dnd.DropTargetContext getDropTargetContext()
meth public java.util.List<java.awt.datatransfer.DataFlavor> getCurrentDataFlavorsAsList()
meth public long getEventID()
meth public void acceptDrag(int)
meth public void rejectDrag()
meth public void setPoint(java.awt.Point)
meth public void translatePoint(int,int)
supr java.lang.Object
hfds event,id,x,y

CLSS public final static org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetDropEvent
 outer org.netbeans.api.visual.action.WidgetAction
cons public init(long,java.awt.dnd.DropTargetDropEvent)
intf org.netbeans.api.visual.action.WidgetAction$WidgetLocationEvent
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public boolean isLocalTransfer()
meth public int getDropAction()
meth public int getSourceActions()
meth public java.awt.Point getPoint()
meth public java.awt.datatransfer.DataFlavor[] getCurrentDataFlavors()
meth public java.awt.datatransfer.Transferable getTransferable()
meth public java.awt.dnd.DropTargetContext getDropTargetContext()
meth public java.util.List<java.awt.datatransfer.DataFlavor> getCurrentDataFlavorsAsList()
meth public long getEventID()
meth public void acceptDrop(int)
meth public void rejectDrop()
meth public void setPoint(java.awt.Point)
meth public void translatePoint(int,int)
supr java.lang.Object
hfds event,id,x,y

CLSS public final static org.netbeans.api.visual.action.WidgetAction$WidgetDropTargetEvent
 outer org.netbeans.api.visual.action.WidgetAction
cons public init(long,java.awt.dnd.DropTargetEvent)
intf org.netbeans.api.visual.action.WidgetAction$WidgetEvent
meth public java.awt.dnd.DropTargetContext getDropTargetContext()
meth public long getEventID()
supr java.lang.Object
hfds event,id

CLSS public abstract interface static org.netbeans.api.visual.action.WidgetAction$WidgetEvent
 outer org.netbeans.api.visual.action.WidgetAction
meth public abstract long getEventID()

CLSS public final static org.netbeans.api.visual.action.WidgetAction$WidgetFocusEvent
 outer org.netbeans.api.visual.action.WidgetAction
cons public init(long,java.awt.event.FocusEvent)
intf org.netbeans.api.visual.action.WidgetAction$WidgetEvent
meth public boolean isTemporary()
meth public java.lang.Object getOppositeComponent()
meth public java.lang.String paramString()
meth public long getEventID()
supr java.lang.Object
hfds event,id

CLSS public final static org.netbeans.api.visual.action.WidgetAction$WidgetKeyEvent
 outer org.netbeans.api.visual.action.WidgetAction
cons public init(long,java.awt.event.KeyEvent)
intf org.netbeans.api.visual.action.WidgetAction$WidgetEvent
meth public boolean isActionKey()
meth public boolean isAltDown()
meth public boolean isAltGraphDown()
meth public boolean isControlDown()
meth public boolean isMetaDown()
meth public boolean isShiftDown()
meth public char getKeyChar()
meth public int getKeyCode()
meth public int getKeyLocation()
meth public int getModifiers()
meth public int getModifiersEx()
meth public long getEventID()
meth public long getWhen()
supr java.lang.Object
hfds event,id

CLSS public abstract interface static org.netbeans.api.visual.action.WidgetAction$WidgetLocationEvent
 outer org.netbeans.api.visual.action.WidgetAction
intf org.netbeans.api.visual.action.WidgetAction$WidgetEvent
meth public abstract java.awt.Point getPoint()
meth public abstract void setPoint(java.awt.Point)
meth public abstract void translatePoint(int,int)

CLSS public final static org.netbeans.api.visual.action.WidgetAction$WidgetMouseEvent
 outer org.netbeans.api.visual.action.WidgetAction
cons public init(long,java.awt.event.MouseEvent)
intf org.netbeans.api.visual.action.WidgetAction$WidgetLocationEvent
meth public boolean isAltDown()
meth public boolean isAltGraphDown()
meth public boolean isControlDown()
meth public boolean isMetaDown()
meth public boolean isPopupTrigger()
meth public boolean isShiftDown()
meth public int getButton()
meth public int getClickCount()
meth public int getModifiers()
meth public int getModifiersEx()
meth public java.awt.Point getPoint()
meth public long getEventID()
meth public long getWhen()
meth public void setPoint(java.awt.Point)
meth public void translatePoint(int,int)
supr java.lang.Object
hfds event,id,x,y

CLSS public final static org.netbeans.api.visual.action.WidgetAction$WidgetMouseWheelEvent
 outer org.netbeans.api.visual.action.WidgetAction
cons public init(long,java.awt.event.MouseWheelEvent)
intf org.netbeans.api.visual.action.WidgetAction$WidgetLocationEvent
meth public boolean isAltDown()
meth public boolean isAltGraphDown()
meth public boolean isControlDown()
meth public boolean isMetaDown()
meth public boolean isPopupTrigger()
meth public boolean isShiftDown()
meth public int getButton()
meth public int getClickCount()
meth public int getModifiers()
meth public int getModifiersEx()
meth public int getScrollAmount()
meth public int getScrollType()
meth public int getUnitsToScroll()
meth public int getWheelRotation()
meth public java.awt.Point getPoint()
meth public long getEventID()
meth public long getWhen()
meth public void setPoint(java.awt.Point)
meth public void translatePoint(int,int)
supr java.lang.Object
hfds event,id,x,y

CLSS public abstract org.netbeans.api.visual.anchor.Anchor
cons protected init(org.netbeans.api.visual.widget.Widget)
fld public final static java.util.EnumSet<org.netbeans.api.visual.anchor.Anchor$Direction> DIRECTION_ANY
innr public abstract interface static Entry
innr public final Result
innr public final static !enum Direction
intf org.netbeans.api.visual.widget.Widget$Dependency
meth protected final boolean isUsed()
meth protected void notifyEntryAdded(org.netbeans.api.visual.anchor.Anchor$Entry)
meth protected void notifyEntryRemoved(org.netbeans.api.visual.anchor.Anchor$Entry)
meth protected void notifyRevalidate()
meth protected void notifyUnused()
meth protected void notifyUsed()
meth public abstract org.netbeans.api.visual.anchor.Anchor$Result compute(org.netbeans.api.visual.anchor.Anchor$Entry)
meth public boolean allowsArbitraryConnectionPlacement()
meth public final java.util.List<org.netbeans.api.visual.anchor.Anchor$Entry> getEntries()
meth public final void addEntries(java.util.List<org.netbeans.api.visual.anchor.Anchor$Entry>)
meth public final void addEntry(org.netbeans.api.visual.anchor.Anchor$Entry)
meth public final void removeEntries(java.util.List<org.netbeans.api.visual.anchor.Anchor$Entry>)
meth public final void removeEntry(org.netbeans.api.visual.anchor.Anchor$Entry)
meth public final void revalidateDependency()
meth public java.awt.Point getOppositeSceneLocation(org.netbeans.api.visual.anchor.Anchor$Entry)
meth public java.awt.Point getRelatedSceneLocation()
meth public java.util.List<java.awt.Point> compute(java.util.List<java.awt.Point>)
meth public org.netbeans.api.visual.widget.Widget getRelatedWidget()
supr java.lang.Object
hfds attachedToWidget,entries,relatedWidget

CLSS public final static !enum org.netbeans.api.visual.anchor.Anchor$Direction
 outer org.netbeans.api.visual.anchor.Anchor
fld public final static org.netbeans.api.visual.anchor.Anchor$Direction BOTTOM
fld public final static org.netbeans.api.visual.anchor.Anchor$Direction LEFT
fld public final static org.netbeans.api.visual.anchor.Anchor$Direction RIGHT
fld public final static org.netbeans.api.visual.anchor.Anchor$Direction TOP
meth public static org.netbeans.api.visual.anchor.Anchor$Direction valueOf(java.lang.String)
meth public static org.netbeans.api.visual.anchor.Anchor$Direction[] values()
supr java.lang.Enum<org.netbeans.api.visual.anchor.Anchor$Direction>

CLSS public abstract interface static org.netbeans.api.visual.anchor.Anchor$Entry
 outer org.netbeans.api.visual.anchor.Anchor
meth public abstract boolean isAttachedToConnectionSource()
meth public abstract org.netbeans.api.visual.anchor.Anchor getAttachedAnchor()
meth public abstract org.netbeans.api.visual.anchor.Anchor getOppositeAnchor()
meth public abstract org.netbeans.api.visual.widget.ConnectionWidget getAttachedConnectionWidget()
meth public abstract void revalidateEntry()

CLSS public final org.netbeans.api.visual.anchor.Anchor$Result
 outer org.netbeans.api.visual.anchor.Anchor
cons public init(java.awt.Point,java.util.EnumSet<org.netbeans.api.visual.anchor.Anchor$Direction>)
cons public init(org.netbeans.api.visual.anchor.Anchor,java.awt.Point,org.netbeans.api.visual.anchor.Anchor$Direction)
meth public java.awt.Point getAnchorSceneLocation()
meth public java.util.EnumSet<org.netbeans.api.visual.anchor.Anchor$Direction> getDirections()
supr java.lang.Object
hfds anchorSceneLocation,directions

CLSS public final org.netbeans.api.visual.anchor.AnchorFactory
innr public final static !enum DirectionalAnchorKind
meth public !varargs static org.netbeans.api.visual.anchor.Anchor createProxyAnchor(org.netbeans.api.visual.model.StateModel,org.netbeans.api.visual.anchor.Anchor[])
meth public static org.netbeans.api.visual.anchor.Anchor createCenterAnchor(org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.anchor.Anchor createCircularAnchor(org.netbeans.api.visual.widget.Widget,int)
meth public static org.netbeans.api.visual.anchor.Anchor createDirectionalAnchor(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.anchor.AnchorFactory$DirectionalAnchorKind)
meth public static org.netbeans.api.visual.anchor.Anchor createDirectionalAnchor(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.anchor.AnchorFactory$DirectionalAnchorKind,int)
meth public static org.netbeans.api.visual.anchor.Anchor createFixedAnchor(java.awt.Point)
meth public static org.netbeans.api.visual.anchor.Anchor createFreeRectangularAnchor(org.netbeans.api.visual.widget.Widget,boolean)
meth public static org.netbeans.api.visual.anchor.Anchor createRectangularAnchor(org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.anchor.Anchor createRectangularAnchor(org.netbeans.api.visual.widget.Widget,boolean)
supr java.lang.Object

CLSS public final static !enum org.netbeans.api.visual.anchor.AnchorFactory$DirectionalAnchorKind
 outer org.netbeans.api.visual.anchor.AnchorFactory
fld public final static org.netbeans.api.visual.anchor.AnchorFactory$DirectionalAnchorKind HORIZONTAL
fld public final static org.netbeans.api.visual.anchor.AnchorFactory$DirectionalAnchorKind VERTICAL
meth public static org.netbeans.api.visual.anchor.AnchorFactory$DirectionalAnchorKind valueOf(java.lang.String)
meth public static org.netbeans.api.visual.anchor.AnchorFactory$DirectionalAnchorKind[] values()
supr java.lang.Enum<org.netbeans.api.visual.anchor.AnchorFactory$DirectionalAnchorKind>

CLSS public abstract interface org.netbeans.api.visual.anchor.AnchorShape
fld public final static org.netbeans.api.visual.anchor.AnchorShape NONE
fld public final static org.netbeans.api.visual.anchor.AnchorShape TRIANGLE_FILLED
fld public final static org.netbeans.api.visual.anchor.AnchorShape TRIANGLE_HOLLOW
fld public final static org.netbeans.api.visual.anchor.AnchorShape TRIANGLE_OUT
meth public abstract boolean isLineOriented()
meth public abstract double getCutDistance()
meth public abstract int getRadius()
meth public abstract void paint(java.awt.Graphics2D,boolean)

CLSS public org.netbeans.api.visual.anchor.AnchorShapeFactory
innr public final static !enum ConnectionEnd
meth public static org.netbeans.api.visual.anchor.AnchorShape createAdjustableAnchorShape(org.netbeans.api.visual.anchor.AnchorShape,org.netbeans.api.visual.anchor.AnchorShapeLocationResolver)
meth public static org.netbeans.api.visual.anchor.AnchorShape createAdjustableAnchorShape(org.netbeans.api.visual.anchor.AnchorShape,org.netbeans.api.visual.widget.ConnectionWidget,org.netbeans.api.visual.anchor.AnchorShapeFactory$ConnectionEnd,org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.anchor.AnchorShape createArrowAnchorShape(int,int)
meth public static org.netbeans.api.visual.anchor.AnchorShape createImageAnchorShape(java.awt.Image)
meth public static org.netbeans.api.visual.anchor.AnchorShape createImageAnchorShape(java.awt.Image,boolean)
meth public static org.netbeans.api.visual.anchor.AnchorShape createTriangleAnchorShape(int,boolean,boolean)
meth public static org.netbeans.api.visual.anchor.AnchorShape createTriangleAnchorShape(int,boolean,boolean,int)
meth public static org.netbeans.api.visual.anchor.AnchorShapeLocationResolver createWidgetResolver(org.netbeans.api.visual.widget.ConnectionWidget,org.netbeans.api.visual.anchor.AnchorShapeFactory$ConnectionEnd,org.netbeans.api.visual.widget.Widget)
supr java.lang.Object

CLSS public final static !enum org.netbeans.api.visual.anchor.AnchorShapeFactory$ConnectionEnd
 outer org.netbeans.api.visual.anchor.AnchorShapeFactory
fld public final static org.netbeans.api.visual.anchor.AnchorShapeFactory$ConnectionEnd SOURCE
fld public final static org.netbeans.api.visual.anchor.AnchorShapeFactory$ConnectionEnd TARGET
meth public static org.netbeans.api.visual.anchor.AnchorShapeFactory$ConnectionEnd valueOf(java.lang.String)
meth public static org.netbeans.api.visual.anchor.AnchorShapeFactory$ConnectionEnd[] values()
supr java.lang.Enum<org.netbeans.api.visual.anchor.AnchorShapeFactory$ConnectionEnd>

CLSS public abstract interface org.netbeans.api.visual.anchor.AnchorShapeLocationResolver
meth public abstract int getEndLocation()

CLSS public abstract interface org.netbeans.api.visual.anchor.PointShape
fld public final static org.netbeans.api.visual.anchor.PointShape NONE
fld public final static org.netbeans.api.visual.anchor.PointShape SQUARE_FILLED_BIG
fld public final static org.netbeans.api.visual.anchor.PointShape SQUARE_FILLED_SMALL
meth public abstract int getRadius()
meth public abstract void paint(java.awt.Graphics2D)

CLSS public org.netbeans.api.visual.anchor.PointShapeFactory
meth public static org.netbeans.api.visual.anchor.PointShape createImagePointShape(java.awt.Image)
meth public static org.netbeans.api.visual.anchor.PointShape createPointShape(int,boolean)
supr java.lang.Object

CLSS public abstract org.netbeans.api.visual.animator.Animator
cons protected init(org.netbeans.api.visual.animator.SceneAnimator)
meth protected abstract void tick(double)
meth protected final org.netbeans.api.visual.widget.Scene getScene()
meth protected final void start()
meth public final boolean isRunning()
meth public void addAnimatorListener(org.netbeans.api.visual.animator.AnimatorListener)
meth public void removeAnimatorListener(org.netbeans.api.visual.animator.AnimatorListener)
supr java.lang.Object
hfds listeners,sceneAnimator

CLSS public final org.netbeans.api.visual.animator.AnimatorEvent
meth public double getProgress()
meth public org.netbeans.api.visual.animator.Animator getAnimator()
supr java.lang.Object
hfds animator,progress

CLSS public abstract interface org.netbeans.api.visual.animator.AnimatorListener
meth public abstract void animatorFinished(org.netbeans.api.visual.animator.AnimatorEvent)
meth public abstract void animatorPostTick(org.netbeans.api.visual.animator.AnimatorEvent)
meth public abstract void animatorPreTick(org.netbeans.api.visual.animator.AnimatorEvent)
meth public abstract void animatorReset(org.netbeans.api.visual.animator.AnimatorEvent)
meth public abstract void animatorStarted(org.netbeans.api.visual.animator.AnimatorEvent)

CLSS public final org.netbeans.api.visual.animator.SceneAnimator
cons public init(org.netbeans.api.visual.widget.Scene)
meth public boolean isAnimatingBackgroundColor(org.netbeans.api.visual.widget.Widget)
meth public boolean isAnimatingForegroundColor(org.netbeans.api.visual.widget.Widget)
meth public boolean isAnimatingPreferredBounds(org.netbeans.api.visual.widget.Widget)
meth public boolean isAnimatingPreferredLocation(org.netbeans.api.visual.widget.Widget)
meth public boolean isAnimatingZoomFactor()
meth public double getTargetZoomFactor()
meth public org.netbeans.api.visual.animator.Animator getColorAnimator()
meth public org.netbeans.api.visual.animator.Animator getPreferredBoundsAnimator()
meth public org.netbeans.api.visual.animator.Animator getPreferredLocationAnimator()
meth public org.netbeans.api.visual.animator.Animator getZoomAnimator()
meth public org.netbeans.api.visual.widget.Scene getScene()
meth public void animateBackgroundColor(org.netbeans.api.visual.widget.Widget,java.awt.Color)
meth public void animateForegroundColor(org.netbeans.api.visual.widget.Widget,java.awt.Color)
meth public void animatePreferredBounds(org.netbeans.api.visual.widget.Widget,java.awt.Rectangle)
meth public void animatePreferredLocation(org.netbeans.api.visual.widget.Widget,java.awt.Point)
meth public void animateZoomFactor(double)
supr java.lang.Object
hfds RP,SLEEP,TIME_PERIOD,animators,cache,colorAnimator,preferredBoundsAnimator,preferredLocationAnimator,scene,task,taskAlive,zoomAnimator
hcls UpdateTask

CLSS public abstract interface org.netbeans.api.visual.border.Border
meth public abstract boolean isOpaque()
meth public abstract java.awt.Insets getInsets()
meth public abstract void paint(java.awt.Graphics2D,java.awt.Rectangle)

CLSS public final org.netbeans.api.visual.border.BorderFactory
meth public !varargs static org.netbeans.api.visual.border.Border createCompositeBorder(org.netbeans.api.visual.border.Border[])
meth public static org.netbeans.api.visual.border.Border createBevelBorder(boolean)
meth public static org.netbeans.api.visual.border.Border createBevelBorder(boolean,java.awt.Color)
meth public static org.netbeans.api.visual.border.Border createBevelBorder(boolean,java.lang.String,org.netbeans.api.visual.widget.ResourceTable)
meth public static org.netbeans.api.visual.border.Border createBevelBorder(boolean,java.lang.String,org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.border.Border createDashedBorder(java.awt.Color,int,int)
meth public static org.netbeans.api.visual.border.Border createDashedBorder(java.awt.Color,int,int,boolean)
meth public static org.netbeans.api.visual.border.Border createDashedBorder(java.lang.String,org.netbeans.api.visual.widget.ResourceTable,int,int)
meth public static org.netbeans.api.visual.border.Border createDashedBorder(java.lang.String,org.netbeans.api.visual.widget.ResourceTable,int,int,boolean)
meth public static org.netbeans.api.visual.border.Border createDashedBorder(java.lang.String,org.netbeans.api.visual.widget.Widget,int,int)
meth public static org.netbeans.api.visual.border.Border createDashedBorder(java.lang.String,org.netbeans.api.visual.widget.Widget,int,int,boolean)
meth public static org.netbeans.api.visual.border.Border createEmptyBorder()
meth public static org.netbeans.api.visual.border.Border createEmptyBorder(int)
meth public static org.netbeans.api.visual.border.Border createEmptyBorder(int,int)
meth public static org.netbeans.api.visual.border.Border createEmptyBorder(int,int,int,int)
meth public static org.netbeans.api.visual.border.Border createFancyDashedBorder(java.awt.Color,int,int)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.visual.border.Border createImageBorder(java.awt.Insets,java.awt.Image)
meth public static org.netbeans.api.visual.border.Border createImageBorder(java.awt.Insets,java.awt.Insets,java.awt.Image)
meth public static org.netbeans.api.visual.border.Border createLineBorder()
meth public static org.netbeans.api.visual.border.Border createLineBorder(int)
meth public static org.netbeans.api.visual.border.Border createLineBorder(int,int,int,int,java.awt.Color)
meth public static org.netbeans.api.visual.border.Border createLineBorder(int,int,int,int,java.lang.String,org.netbeans.api.visual.widget.ResourceTable)
meth public static org.netbeans.api.visual.border.Border createLineBorder(int,int,int,int,java.lang.String,org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.border.Border createLineBorder(int,java.awt.Color)
meth public static org.netbeans.api.visual.border.Border createLineBorder(int,java.lang.String,org.netbeans.api.visual.widget.ResourceTable)
meth public static org.netbeans.api.visual.border.Border createLineBorder(int,java.lang.String,org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.border.Border createLineBorder(java.lang.String,org.netbeans.api.visual.widget.ResourceTable)
meth public static org.netbeans.api.visual.border.Border createLineBorder(java.lang.String,org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.border.Border createOpaqueBorder(int,int,int,int)
meth public static org.netbeans.api.visual.border.Border createResizeBorder(int)
meth public static org.netbeans.api.visual.border.Border createResizeBorder(int,java.awt.Color,boolean)
meth public static org.netbeans.api.visual.border.Border createResizeBorder(int,java.lang.String,org.netbeans.api.visual.widget.ResourceTable)
meth public static org.netbeans.api.visual.border.Border createResizeBorder(int,java.lang.String,org.netbeans.api.visual.widget.ResourceTable,boolean)
meth public static org.netbeans.api.visual.border.Border createResizeBorder(int,java.lang.String,org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.border.Border createResizeBorder(int,java.lang.String,org.netbeans.api.visual.widget.Widget,boolean)
meth public static org.netbeans.api.visual.border.Border createRoundedBorder(int,int,int,int,java.awt.Color,java.awt.Color)
meth public static org.netbeans.api.visual.border.Border createRoundedBorder(int,int,int,int,java.lang.String,java.lang.String,org.netbeans.api.visual.widget.ResourceTable)
meth public static org.netbeans.api.visual.border.Border createRoundedBorder(int,int,int,int,java.lang.String,java.lang.String,org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.border.Border createRoundedBorder(int,int,java.awt.Color,java.awt.Color)
meth public static org.netbeans.api.visual.border.Border createRoundedBorder(int,int,java.lang.String,java.lang.String,org.netbeans.api.visual.widget.ResourceTable)
meth public static org.netbeans.api.visual.border.Border createRoundedBorder(int,int,java.lang.String,java.lang.String,org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.border.Border createSwingBorder(org.netbeans.api.visual.widget.Scene,javax.swing.border.Border)
supr java.lang.Object
hfds BORDER_EMPTY,BORDER_LINE

CLSS public final org.netbeans.api.visual.border.BorderSupport
meth public static boolean isOuterResizeBorder(org.netbeans.api.visual.border.Border)
meth public static javax.swing.border.Border getSwingBorder(org.netbeans.api.visual.border.Border)
supr java.lang.Object

CLSS public final org.netbeans.api.visual.export.SceneExporter
innr public final static !enum ImageType
innr public final static !enum ZoomType
meth public static java.awt.image.BufferedImage createImage(org.netbeans.api.visual.widget.Scene,java.io.File,org.netbeans.api.visual.export.SceneExporter$ImageType,org.netbeans.api.visual.export.SceneExporter$ZoomType,boolean,boolean,int,int,int) throws java.io.IOException
meth public static java.util.ArrayList<org.netbeans.api.visual.export.WidgetPolygonalCoordinates> createImageMap(org.netbeans.api.visual.widget.Scene,java.io.File,org.netbeans.api.visual.export.SceneExporter$ImageType,org.netbeans.api.visual.export.SceneExporter$ZoomType,boolean,boolean,int,int,int,int) throws java.io.IOException
supr java.lang.Object

CLSS public final static !enum org.netbeans.api.visual.export.SceneExporter$ImageType
 outer org.netbeans.api.visual.export.SceneExporter
fld public final static org.netbeans.api.visual.export.SceneExporter$ImageType JPG
fld public final static org.netbeans.api.visual.export.SceneExporter$ImageType PNG
meth public static org.netbeans.api.visual.export.SceneExporter$ImageType valueOf(java.lang.String)
meth public static org.netbeans.api.visual.export.SceneExporter$ImageType[] values()
supr java.lang.Enum<org.netbeans.api.visual.export.SceneExporter$ImageType>

CLSS public final static !enum org.netbeans.api.visual.export.SceneExporter$ZoomType
 outer org.netbeans.api.visual.export.SceneExporter
fld public final static org.netbeans.api.visual.export.SceneExporter$ZoomType ACTUAL_SIZE
fld public final static org.netbeans.api.visual.export.SceneExporter$ZoomType CURRENT_ZOOM_LEVEL
fld public final static org.netbeans.api.visual.export.SceneExporter$ZoomType CUSTOM_SIZE
fld public final static org.netbeans.api.visual.export.SceneExporter$ZoomType FIT_IN_WINDOW
meth public static org.netbeans.api.visual.export.SceneExporter$ZoomType valueOf(java.lang.String)
meth public static org.netbeans.api.visual.export.SceneExporter$ZoomType[] values()
supr java.lang.Enum<org.netbeans.api.visual.export.SceneExporter$ZoomType>

CLSS public final org.netbeans.api.visual.export.WidgetPolygonalCoordinates
cons public init(org.netbeans.api.visual.widget.Widget,java.awt.Polygon)
meth public java.awt.Polygon getPolygon()
meth public org.netbeans.api.visual.widget.Widget getWidget()
supr java.lang.Object
hfds polygon,widget

CLSS public abstract org.netbeans.api.visual.graph.GraphPinScene<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init()
innr public abstract static StringGraph
meth protected abstract org.netbeans.api.visual.widget.Widget attachEdgeWidget({org.netbeans.api.visual.graph.GraphPinScene%1})
meth protected abstract org.netbeans.api.visual.widget.Widget attachNodeWidget({org.netbeans.api.visual.graph.GraphPinScene%0})
meth protected abstract org.netbeans.api.visual.widget.Widget attachPinWidget({org.netbeans.api.visual.graph.GraphPinScene%0},{org.netbeans.api.visual.graph.GraphPinScene%2})
meth protected abstract void attachEdgeSourceAnchor({org.netbeans.api.visual.graph.GraphPinScene%1},{org.netbeans.api.visual.graph.GraphPinScene%2},{org.netbeans.api.visual.graph.GraphPinScene%2})
meth protected abstract void attachEdgeTargetAnchor({org.netbeans.api.visual.graph.GraphPinScene%1},{org.netbeans.api.visual.graph.GraphPinScene%2},{org.netbeans.api.visual.graph.GraphPinScene%2})
meth protected void detachEdgeWidget({org.netbeans.api.visual.graph.GraphPinScene%1},org.netbeans.api.visual.widget.Widget)
meth protected void detachNodeWidget({org.netbeans.api.visual.graph.GraphPinScene%0},org.netbeans.api.visual.widget.Widget)
meth protected void detachPinWidget({org.netbeans.api.visual.graph.GraphPinScene%2},org.netbeans.api.visual.widget.Widget)
meth protected void notifyEdgeAdded({org.netbeans.api.visual.graph.GraphPinScene%1},org.netbeans.api.visual.widget.Widget)
meth protected void notifyNodeAdded({org.netbeans.api.visual.graph.GraphPinScene%0},org.netbeans.api.visual.widget.Widget)
meth protected void notifyPinAdded({org.netbeans.api.visual.graph.GraphPinScene%0},{org.netbeans.api.visual.graph.GraphPinScene%2},org.netbeans.api.visual.widget.Widget)
meth public boolean isEdge(java.lang.Object)
meth public boolean isNode(java.lang.Object)
meth public boolean isPin(java.lang.Object)
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphPinScene%0}> getNodes()
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphPinScene%1}> findEdgesBetween({org.netbeans.api.visual.graph.GraphPinScene%2},{org.netbeans.api.visual.graph.GraphPinScene%2})
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphPinScene%1}> findPinEdges({org.netbeans.api.visual.graph.GraphPinScene%2},boolean,boolean)
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphPinScene%1}> getEdges()
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphPinScene%2}> getNodePins({org.netbeans.api.visual.graph.GraphPinScene%0})
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphPinScene%2}> getPins()
meth public final org.netbeans.api.visual.widget.Widget addEdge({org.netbeans.api.visual.graph.GraphPinScene%1})
meth public final org.netbeans.api.visual.widget.Widget addNode({org.netbeans.api.visual.graph.GraphPinScene%0})
meth public final org.netbeans.api.visual.widget.Widget addPin({org.netbeans.api.visual.graph.GraphPinScene%0},{org.netbeans.api.visual.graph.GraphPinScene%2})
meth public final void removeEdge({org.netbeans.api.visual.graph.GraphPinScene%1})
meth public final void removeNode({org.netbeans.api.visual.graph.GraphPinScene%0})
meth public final void removeNodeWithEdges({org.netbeans.api.visual.graph.GraphPinScene%0})
meth public final void removePin({org.netbeans.api.visual.graph.GraphPinScene%2})
meth public final void removePinWithEdges({org.netbeans.api.visual.graph.GraphPinScene%2})
meth public final void setEdgeSource({org.netbeans.api.visual.graph.GraphPinScene%1},{org.netbeans.api.visual.graph.GraphPinScene%2})
meth public final void setEdgeTarget({org.netbeans.api.visual.graph.GraphPinScene%1},{org.netbeans.api.visual.graph.GraphPinScene%2})
meth public final {org.netbeans.api.visual.graph.GraphPinScene%0} getPinNode({org.netbeans.api.visual.graph.GraphPinScene%2})
meth public final {org.netbeans.api.visual.graph.GraphPinScene%2} getEdgeSource({org.netbeans.api.visual.graph.GraphPinScene%1})
meth public final {org.netbeans.api.visual.graph.GraphPinScene%2} getEdgeTarget({org.netbeans.api.visual.graph.GraphPinScene%1})
supr org.netbeans.api.visual.model.ObjectScene
hfds edgeSourcePins,edgeTargetPins,edges,edgesUm,nodePins,nodes,nodesUm,pinInputEdges,pinNodes,pinOutputEdges,pins,pinsUm

CLSS public abstract static org.netbeans.api.visual.graph.GraphPinScene$StringGraph
 outer org.netbeans.api.visual.graph.GraphPinScene
cons public init()
supr org.netbeans.api.visual.graph.GraphPinScene<java.lang.String,java.lang.String,java.lang.String>

CLSS public abstract org.netbeans.api.visual.graph.GraphScene<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
innr public abstract static StringGraph
meth protected abstract org.netbeans.api.visual.widget.Widget attachEdgeWidget({org.netbeans.api.visual.graph.GraphScene%1})
meth protected abstract org.netbeans.api.visual.widget.Widget attachNodeWidget({org.netbeans.api.visual.graph.GraphScene%0})
meth protected abstract void attachEdgeSourceAnchor({org.netbeans.api.visual.graph.GraphScene%1},{org.netbeans.api.visual.graph.GraphScene%0},{org.netbeans.api.visual.graph.GraphScene%0})
meth protected abstract void attachEdgeTargetAnchor({org.netbeans.api.visual.graph.GraphScene%1},{org.netbeans.api.visual.graph.GraphScene%0},{org.netbeans.api.visual.graph.GraphScene%0})
meth protected void detachEdgeWidget({org.netbeans.api.visual.graph.GraphScene%1},org.netbeans.api.visual.widget.Widget)
meth protected void detachNodeWidget({org.netbeans.api.visual.graph.GraphScene%0},org.netbeans.api.visual.widget.Widget)
meth protected void notifyEdgeAdded({org.netbeans.api.visual.graph.GraphScene%1},org.netbeans.api.visual.widget.Widget)
meth protected void notifyNodeAdded({org.netbeans.api.visual.graph.GraphScene%0},org.netbeans.api.visual.widget.Widget)
meth public boolean isEdge(java.lang.Object)
meth public boolean isNode(java.lang.Object)
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphScene%0}> getNodes()
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphScene%1}> findEdgesBetween({org.netbeans.api.visual.graph.GraphScene%0},{org.netbeans.api.visual.graph.GraphScene%0})
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphScene%1}> findNodeEdges({org.netbeans.api.visual.graph.GraphScene%0},boolean,boolean)
meth public final java.util.Collection<{org.netbeans.api.visual.graph.GraphScene%1}> getEdges()
meth public final org.netbeans.api.visual.widget.Widget addEdge({org.netbeans.api.visual.graph.GraphScene%1})
meth public final org.netbeans.api.visual.widget.Widget addNode({org.netbeans.api.visual.graph.GraphScene%0})
meth public final void removeEdge({org.netbeans.api.visual.graph.GraphScene%1})
meth public final void removeNode({org.netbeans.api.visual.graph.GraphScene%0})
meth public final void removeNodeWithEdges({org.netbeans.api.visual.graph.GraphScene%0})
meth public final void setEdgeSource({org.netbeans.api.visual.graph.GraphScene%1},{org.netbeans.api.visual.graph.GraphScene%0})
meth public final void setEdgeTarget({org.netbeans.api.visual.graph.GraphScene%1},{org.netbeans.api.visual.graph.GraphScene%0})
meth public final {org.netbeans.api.visual.graph.GraphScene%0} getEdgeSource({org.netbeans.api.visual.graph.GraphScene%1})
meth public final {org.netbeans.api.visual.graph.GraphScene%0} getEdgeTarget({org.netbeans.api.visual.graph.GraphScene%1})
supr org.netbeans.api.visual.model.ObjectScene
hfds edgeSourceNodes,edgeTargetNodes,edges,edgesUm,nodeInputEdges,nodeOutputEdges,nodes,nodesUm

CLSS public abstract static org.netbeans.api.visual.graph.GraphScene$StringGraph
 outer org.netbeans.api.visual.graph.GraphScene
cons public init()
supr org.netbeans.api.visual.graph.GraphScene<java.lang.String,java.lang.String>

CLSS public abstract org.netbeans.api.visual.graph.layout.GraphLayout<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth protected abstract void performGraphLayout(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1}>)
meth protected abstract void performNodesLayout(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1}>,java.util.Collection<{org.netbeans.api.visual.graph.layout.GraphLayout%0}>)
meth protected final void setResolvedNodeLocation(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1}>,{org.netbeans.api.visual.graph.layout.GraphLayout%0},java.awt.Point)
meth public final boolean isAnimated()
meth public final void addGraphLayoutListener(org.netbeans.api.visual.graph.layout.GraphLayoutListener<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1}>)
meth public final void layoutGraph(org.netbeans.api.visual.graph.GraphPinScene<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1},?>)
meth public final void layoutGraph(org.netbeans.api.visual.graph.GraphScene<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1}>)
meth public final void layoutNodes(org.netbeans.api.visual.graph.GraphPinScene<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1},?>,java.util.Collection<{org.netbeans.api.visual.graph.layout.GraphLayout%0}>)
meth public final void layoutNodes(org.netbeans.api.visual.graph.GraphScene<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1}>,java.util.Collection<{org.netbeans.api.visual.graph.layout.GraphLayout%0}>)
meth public final void removeGraphLayoutListener(org.netbeans.api.visual.graph.layout.GraphLayoutListener<{org.netbeans.api.visual.graph.layout.GraphLayout%0},{org.netbeans.api.visual.graph.layout.GraphLayout%1}>)
meth public final void setAnimated(boolean)
supr java.lang.Object
hfds animated,listeners

CLSS public org.netbeans.api.visual.graph.layout.GraphLayoutFactory
cons public init()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}> createHierarchicalGraphLayout(org.netbeans.api.visual.graph.GraphScene<{%%0},{%%1}>,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}> createHierarchicalGraphLayout(org.netbeans.api.visual.graph.GraphScene<{%%0},{%%1}>,boolean,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}> createHierarchicalGraphLayout(org.netbeans.api.visual.graph.GraphScene<{%%0},{%%1}>,boolean,boolean,int,int)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}> createOrthogonalGraphLayout(org.netbeans.api.visual.graph.GraphScene<{%%0},{%%1}>,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}> createTreeGraphLayout(int,int,int,int,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}> createTreeGraphLayout(int,int,int,int,boolean,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}> createTreeGraphLayout(int,int,int,int,boolean,boolean,org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment)
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.visual.graph.layout.GraphLayoutListener<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract void graphLayoutFinished(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.GraphLayoutListener%0},{org.netbeans.api.visual.graph.layout.GraphLayoutListener%1}>)
meth public abstract void graphLayoutStarted(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.GraphLayoutListener%0},{org.netbeans.api.visual.graph.layout.GraphLayoutListener%1}>)
meth public abstract void nodeLocationChanged(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.GraphLayoutListener%0},{org.netbeans.api.visual.graph.layout.GraphLayoutListener%1}>,{org.netbeans.api.visual.graph.layout.GraphLayoutListener%0},java.awt.Point,java.awt.Point)

CLSS public org.netbeans.api.visual.graph.layout.GraphLayoutSupport
cons public init()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> void setTreeGraphLayoutProperties(org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}>,int,int,int,int,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> void setTreeGraphLayoutProperties(org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}>,int,int,int,int,boolean,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> void setTreeGraphLayoutProperties(org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}>,int,int,int,int,boolean,boolean,org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> void setTreeGraphLayoutRootNode(org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}>,{%%0})
supr java.lang.Object

CLSS public final org.netbeans.api.visual.graph.layout.GridGraphLayout<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth protected void performGraphLayout(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.GridGraphLayout%0},{org.netbeans.api.visual.graph.layout.GridGraphLayout%1}>)
meth protected void performNodesLayout(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.GridGraphLayout%0},{org.netbeans.api.visual.graph.layout.GridGraphLayout%1}>,java.util.Collection<{org.netbeans.api.visual.graph.layout.GridGraphLayout%0}>)
meth public org.netbeans.api.visual.graph.layout.GridGraphLayout<{org.netbeans.api.visual.graph.layout.GridGraphLayout%0},{org.netbeans.api.visual.graph.layout.GridGraphLayout%1}> setChecker(boolean)
meth public org.netbeans.api.visual.graph.layout.GridGraphLayout<{org.netbeans.api.visual.graph.layout.GridGraphLayout%0},{org.netbeans.api.visual.graph.layout.GridGraphLayout%1}> setGaps(int,int)
supr org.netbeans.api.visual.graph.layout.GraphLayout<{org.netbeans.api.visual.graph.layout.GridGraphLayout%0},{org.netbeans.api.visual.graph.layout.GridGraphLayout%1}>
hfds checker,horizontalGap,verticalGap

CLSS public org.netbeans.api.visual.graph.layout.TreeGraphLayout<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(org.netbeans.api.visual.graph.GraphScene<{org.netbeans.api.visual.graph.layout.TreeGraphLayout%0},{org.netbeans.api.visual.graph.layout.TreeGraphLayout%1}>,int,int,int,int,boolean)
meth protected java.util.Collection<{org.netbeans.api.visual.graph.layout.TreeGraphLayout%0}> resolveChildren({org.netbeans.api.visual.graph.layout.TreeGraphLayout%0})
meth protected void performGraphLayout(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.TreeGraphLayout%0},{org.netbeans.api.visual.graph.layout.TreeGraphLayout%1}>)
meth protected void performNodesLayout(org.netbeans.api.visual.graph.layout.UniversalGraph<{org.netbeans.api.visual.graph.layout.TreeGraphLayout%0},{org.netbeans.api.visual.graph.layout.TreeGraphLayout%1}>,java.util.Collection<{org.netbeans.api.visual.graph.layout.TreeGraphLayout%0}>)
meth public final void layout({org.netbeans.api.visual.graph.layout.TreeGraphLayout%0})
supr org.netbeans.api.visual.graph.layout.GraphLayout<{org.netbeans.api.visual.graph.layout.TreeGraphLayout%0},{org.netbeans.api.visual.graph.layout.TreeGraphLayout%1}>
hfds horizontalGap,originX,originY,scene,vertical,verticalGap
hcls Node

CLSS public final !enum org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment
fld public final static org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment BOTTOM
fld public final static org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment CENTER
fld public final static org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment TOP
meth public static org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment valueOf(java.lang.String)
meth public static org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment[] values()
supr java.lang.Enum<org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment>

CLSS public abstract org.netbeans.api.visual.graph.layout.UniversalGraph<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public abstract java.util.Collection<{org.netbeans.api.visual.graph.layout.UniversalGraph%0}> getNodes()
meth public abstract java.util.Collection<{org.netbeans.api.visual.graph.layout.UniversalGraph%1}> findNodeEdges({org.netbeans.api.visual.graph.layout.UniversalGraph%0},boolean,boolean)
meth public abstract java.util.Collection<{org.netbeans.api.visual.graph.layout.UniversalGraph%1}> getEdges()
meth public abstract org.netbeans.api.visual.model.ObjectScene getScene()
meth public abstract {org.netbeans.api.visual.graph.layout.UniversalGraph%0} getEdgeSource({org.netbeans.api.visual.graph.layout.UniversalGraph%1})
meth public abstract {org.netbeans.api.visual.graph.layout.UniversalGraph%0} getEdgeTarget({org.netbeans.api.visual.graph.layout.UniversalGraph%1})
supr java.lang.Object

CLSS public final org.netbeans.api.visual.laf.InputBindings
meth public int getPanActionButton()
meth public int getZoomActionModifiers()
meth public static org.netbeans.api.visual.laf.InputBindings create()
meth public void setPanActionButton(int)
meth public void setZoomActionModifiers(int)
supr java.lang.Object
hfds panActionButton,zoomActionModifiers

CLSS public abstract org.netbeans.api.visual.laf.LookFeel
cons public init()
meth public abstract boolean getOpaque(org.netbeans.api.visual.model.ObjectState)
meth public abstract int getMargin()
meth public abstract java.awt.Color getForeground()
meth public abstract java.awt.Color getForeground(org.netbeans.api.visual.model.ObjectState)
meth public abstract java.awt.Color getLineColor(org.netbeans.api.visual.model.ObjectState)
meth public abstract java.awt.Paint getBackground()
meth public abstract java.awt.Paint getBackground(org.netbeans.api.visual.model.ObjectState)
meth public abstract org.netbeans.api.visual.border.Border getBorder(org.netbeans.api.visual.model.ObjectState)
meth public abstract org.netbeans.api.visual.border.Border getMiniBorder(org.netbeans.api.visual.model.ObjectState)
meth public static org.netbeans.api.visual.laf.LookFeel createDefaultLookFeel()
supr java.lang.Object
hfds DEFAULT

CLSS public abstract interface org.netbeans.api.visual.layout.Layout
meth public abstract boolean requiresJustification(org.netbeans.api.visual.widget.Widget)
meth public abstract void justify(org.netbeans.api.visual.widget.Widget)
meth public abstract void layout(org.netbeans.api.visual.widget.Widget)

CLSS public final org.netbeans.api.visual.layout.LayoutFactory
innr public final static !enum ConnectionWidgetLayoutAlignment
innr public final static !enum SerialAlignment
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.layout.SceneLayout createSceneGraphLayout(org.netbeans.api.visual.graph.GraphPinScene<{%%0},{%%1},?>,org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.api.visual.layout.SceneLayout createSceneGraphLayout(org.netbeans.api.visual.graph.GraphScene<{%%0},{%%1}>,org.netbeans.api.visual.graph.layout.GraphLayout<{%%0},{%%1}>)
meth public static org.netbeans.api.visual.layout.Layout createAbsoluteLayout()
meth public static org.netbeans.api.visual.layout.Layout createCardLayout(org.netbeans.api.visual.widget.Widget)
meth public static org.netbeans.api.visual.layout.Layout createFillLayout()
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.visual.layout.Layout createHorizontalFlowLayout()
meth public static org.netbeans.api.visual.layout.Layout createHorizontalFlowLayout(org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment,int)
meth public static org.netbeans.api.visual.layout.Layout createHorizontalLayout()
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.visual.layout.Layout createHorizontalLayout(org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment,int)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.visual.layout.Layout createOverlayLayout()
meth public static org.netbeans.api.visual.layout.Layout createVerticalFlowLayout()
meth public static org.netbeans.api.visual.layout.Layout createVerticalFlowLayout(org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment,int)
meth public static org.netbeans.api.visual.layout.Layout createVerticalLayout()
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.visual.layout.Layout createVerticalLayout(org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment,int)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.visual.layout.SceneLayout createDevolveWidgetLayout(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.layout.Layout,boolean)
meth public static org.netbeans.api.visual.widget.Widget getActiveCard(org.netbeans.api.visual.widget.Widget)
meth public static void setActiveCard(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.widget.Widget)
supr java.lang.Object
hfds LAYOUT_ABSOLUTE,LAYOUT_OVERLAY

CLSS public final static !enum org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment
 outer org.netbeans.api.visual.layout.LayoutFactory
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment BOTTOM_CENTER
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment BOTTOM_LEFT
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment BOTTOM_RIGHT
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment BOTTOM_SOURCE
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment BOTTOM_TARGET
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment CENTER
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment CENTER_LEFT
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment CENTER_RIGHT
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment CENTER_SOURCE
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment CENTER_TARGET
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment NONE
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment TOP_CENTER
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment TOP_LEFT
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment TOP_RIGHT
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment TOP_SOURCE
fld public final static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment TOP_TARGET
meth public static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment valueOf(java.lang.String)
meth public static org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment[] values()
supr java.lang.Enum<org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment>

CLSS public final static !enum org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment
 outer org.netbeans.api.visual.layout.LayoutFactory
fld public final static org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment CENTER
fld public final static org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment JUSTIFY
fld public final static org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment LEFT_TOP
fld public final static org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment RIGHT_BOTTOM
meth public static org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment valueOf(java.lang.String)
meth public static org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment[] values()
supr java.lang.Enum<org.netbeans.api.visual.layout.LayoutFactory$SerialAlignment>

CLSS public abstract org.netbeans.api.visual.layout.SceneLayout
cons protected init(org.netbeans.api.visual.widget.Scene)
meth protected abstract void performLayout()
meth public final void invokeLayout()
meth public final void invokeLayoutImmediately()
supr java.lang.Object
hfds attached,listener,scene
hcls LayoutSceneListener

CLSS public org.netbeans.api.visual.model.ObjectScene
cons public init()
meth protected org.netbeans.api.visual.model.ObjectState findObjectState(java.lang.Object)
meth public !varargs final void addObject(java.lang.Object,org.netbeans.api.visual.widget.Widget[])
meth public !varargs final void addObjectSceneListener(org.netbeans.api.visual.model.ObjectSceneListener,org.netbeans.api.visual.model.ObjectSceneEventType[])
meth public !varargs final void removeObjectSceneListener(org.netbeans.api.visual.model.ObjectSceneListener,org.netbeans.api.visual.model.ObjectSceneEventType[])
meth public final boolean isObject(java.lang.Object)
meth public final java.lang.Object findObject(org.netbeans.api.visual.widget.Widget)
meth public final java.lang.Object findStoredObject(java.lang.Object)
meth public final java.lang.Object getFocusedObject()
meth public final java.lang.Object getHoveredObject()
meth public final java.util.List<org.netbeans.api.visual.widget.Widget> findWidgets(java.lang.Object)
meth public final java.util.Set<?> getHighlightedObjects()
meth public final java.util.Set<?> getObjects()
meth public final java.util.Set<?> getSelectedObjects()
meth public final org.netbeans.api.visual.action.WidgetAction createObjectHoverAction()
meth public final org.netbeans.api.visual.action.WidgetAction createSelectAction()
meth public final org.netbeans.api.visual.model.ObjectState getObjectState(java.lang.Object)
meth public final org.netbeans.api.visual.widget.Widget findWidget(java.lang.Object)
meth public final void removeObject(java.lang.Object)
meth public final void removeObjectMapping(java.lang.Object)
meth public final void setFocusedObject(java.lang.Object)
meth public final void setHighlightedObjects(java.util.Set<?>)
meth public final void setHoveredObject(java.lang.Object)
meth public final void setSelectedObjects(java.util.Set<?>)
meth public java.lang.Comparable getIdentityCode(java.lang.Object)
meth public void clearObjectState(java.lang.Object)
meth public void userSelectionSuggested(java.util.Set<?>,boolean)
supr org.netbeans.api.visual.widget.Scene
hfds EMPTY_LISTENERS,EMPTY_SET,EMPTY_WIDGETS_ARRAY,EMPTY_WIDGETS_LIST,event,focusedObject,highlightedObjects,highlightedObjectsUm,hoveredObject,listeners,object2widget,object2widgets,objectHoverAction,objectStates,objects,objectsUm,selectAction,selectedObjects,selectedObjectsUm,widget2object
hcls ObjectHoverProvider,ObjectSelectProvider

CLSS public org.netbeans.api.visual.model.ObjectSceneEvent
meth public org.netbeans.api.visual.model.ObjectScene getObjectScene()
supr java.lang.Object
hfds objectScene

CLSS public final !enum org.netbeans.api.visual.model.ObjectSceneEventType
fld public final static org.netbeans.api.visual.model.ObjectSceneEventType OBJECT_ADDED
fld public final static org.netbeans.api.visual.model.ObjectSceneEventType OBJECT_FOCUS_CHANGED
fld public final static org.netbeans.api.visual.model.ObjectSceneEventType OBJECT_HIGHLIGHTING_CHANGED
fld public final static org.netbeans.api.visual.model.ObjectSceneEventType OBJECT_HOVER_CHANGED
fld public final static org.netbeans.api.visual.model.ObjectSceneEventType OBJECT_REMOVED
fld public final static org.netbeans.api.visual.model.ObjectSceneEventType OBJECT_SELECTION_CHANGED
fld public final static org.netbeans.api.visual.model.ObjectSceneEventType OBJECT_STATE_CHANGED
meth public static org.netbeans.api.visual.model.ObjectSceneEventType valueOf(java.lang.String)
meth public static org.netbeans.api.visual.model.ObjectSceneEventType[] values()
supr java.lang.Enum<org.netbeans.api.visual.model.ObjectSceneEventType>

CLSS public abstract interface org.netbeans.api.visual.model.ObjectSceneListener
meth public abstract void focusChanged(org.netbeans.api.visual.model.ObjectSceneEvent,java.lang.Object,java.lang.Object)
meth public abstract void highlightingChanged(org.netbeans.api.visual.model.ObjectSceneEvent,java.util.Set<java.lang.Object>,java.util.Set<java.lang.Object>)
meth public abstract void hoverChanged(org.netbeans.api.visual.model.ObjectSceneEvent,java.lang.Object,java.lang.Object)
meth public abstract void objectAdded(org.netbeans.api.visual.model.ObjectSceneEvent,java.lang.Object)
meth public abstract void objectRemoved(org.netbeans.api.visual.model.ObjectSceneEvent,java.lang.Object)
meth public abstract void objectStateChanged(org.netbeans.api.visual.model.ObjectSceneEvent,java.lang.Object,org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth public abstract void selectionChanged(org.netbeans.api.visual.model.ObjectSceneEvent,java.util.Set<java.lang.Object>,java.util.Set<java.lang.Object>)

CLSS public org.netbeans.api.visual.model.ObjectState
meth public boolean isFocused()
meth public boolean isHighlighted()
meth public boolean isHovered()
meth public boolean isObjectFocused()
meth public boolean isObjectHovered()
meth public boolean isSelected()
meth public boolean isWidgetAimed()
meth public boolean isWidgetFocused()
meth public boolean isWidgetHovered()
meth public org.netbeans.api.visual.model.ObjectState deriveHighlighted(boolean)
meth public org.netbeans.api.visual.model.ObjectState deriveObjectFocused(boolean)
meth public org.netbeans.api.visual.model.ObjectState deriveObjectHovered(boolean)
meth public org.netbeans.api.visual.model.ObjectState deriveSelected(boolean)
meth public org.netbeans.api.visual.model.ObjectState deriveWidgetAimed(boolean)
meth public org.netbeans.api.visual.model.ObjectState deriveWidgetFocused(boolean)
meth public org.netbeans.api.visual.model.ObjectState deriveWidgetHovered(boolean)
meth public static org.netbeans.api.visual.model.ObjectState createNormal()
supr java.lang.Object
hfds NORMAL,objectFocused,objectHighlighted,objectHovered,objectSelected,widgetAimed,widgetFocused,widgetHovered

CLSS public final org.netbeans.api.visual.model.StateModel
cons public init()
cons public init(int)
innr public abstract interface static Listener
meth public boolean getBooleanState()
meth public int getMaxStates()
meth public int getState()
meth public void addListener(org.netbeans.api.visual.model.StateModel$Listener)
meth public void decrease()
meth public void increase()
meth public void removeListener(org.netbeans.api.visual.model.StateModel$Listener)
meth public void setBooleanState(boolean)
meth public void setState(int)
meth public void toggleBooleanState()
supr java.lang.Object
hfds listeners,maxStates,state

CLSS public abstract interface static org.netbeans.api.visual.model.StateModel$Listener
 outer org.netbeans.api.visual.model.StateModel
meth public abstract void stateChanged()

CLSS public final org.netbeans.api.visual.print.ScenePrinter
innr public final static !enum ScaleStrategy
meth public static void print(org.netbeans.api.visual.widget.Scene)
meth public static void print(org.netbeans.api.visual.widget.Scene,double,double)
meth public static void print(org.netbeans.api.visual.widget.Scene,java.awt.print.PageFormat)
meth public static void print(org.netbeans.api.visual.widget.Scene,java.awt.print.PageFormat,java.awt.Rectangle)
meth public static void print(org.netbeans.api.visual.widget.Scene,java.awt.print.PageFormat,org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy)
meth public static void print(org.netbeans.api.visual.widget.Scene,java.awt.print.PageFormat,org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy,double,double,boolean,boolean,java.awt.Rectangle,java.util.List<org.netbeans.api.visual.widget.LayerWidget>)
meth public static void print(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy)
supr java.lang.Object

CLSS public final static !enum org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy
 outer org.netbeans.api.visual.print.ScenePrinter
fld public final static org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy NO_SCALING
fld public final static org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy SCALE_CURRENT_ZOOM
fld public final static org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy SCALE_PERCENT
fld public final static org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy SCALE_TO_FIT
fld public final static org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy SCALE_TO_FIT_X
fld public final static org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy SCALE_TO_FIT_Y
meth public static org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy valueOf(java.lang.String)
meth public static org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy[] values()
supr java.lang.Enum<org.netbeans.api.visual.print.ScenePrinter$ScaleStrategy>

CLSS public abstract interface org.netbeans.api.visual.router.CollisionsCollector
meth public abstract void collectCollisions(java.util.List<java.awt.Rectangle>,java.util.List<java.awt.Rectangle>)

CLSS public abstract interface org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector
meth public abstract void collectCollisions(org.netbeans.api.visual.widget.ConnectionWidget,java.util.List<java.awt.Rectangle>,java.util.List<java.awt.Rectangle>)

CLSS public abstract interface org.netbeans.api.visual.router.Router
meth public abstract java.util.List<java.awt.Point> routeConnection(org.netbeans.api.visual.widget.ConnectionWidget)

CLSS public final org.netbeans.api.visual.router.RouterFactory
meth public !varargs static org.netbeans.api.visual.router.Router createOrthogonalSearchRouter(org.netbeans.api.visual.widget.LayerWidget[])
meth public static org.netbeans.api.visual.router.Router createDirectRouter()
meth public static org.netbeans.api.visual.router.Router createFreeRouter()
meth public static org.netbeans.api.visual.router.Router createOrthogonalSearchRouter(org.netbeans.api.visual.router.CollisionsCollector)
meth public static org.netbeans.api.visual.router.Router createOrthogonalSearchRouter(org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector)
supr java.lang.Object
hfds ROUTER_DIRECT,ROUTER_FREE

CLSS public abstract org.netbeans.api.visual.vmd.VMDColorScheme
cons protected init()
meth public abstract boolean isNodeMinimizeButtonOnRight(org.netbeans.api.visual.vmd.VMDNodeWidget)
meth public abstract int getNodeAnchorGap(org.netbeans.api.visual.vmd.VMDNodeAnchor)
meth public abstract java.awt.Image getMinimizeWidgetImage(org.netbeans.api.visual.vmd.VMDNodeWidget)
meth public abstract org.netbeans.api.visual.widget.Widget createPinCategoryWidget(org.netbeans.api.visual.vmd.VMDNodeWidget,java.lang.String)
meth public abstract void installUI(org.netbeans.api.visual.vmd.VMDConnectionWidget)
meth public abstract void installUI(org.netbeans.api.visual.vmd.VMDNodeWidget)
meth public abstract void installUI(org.netbeans.api.visual.vmd.VMDPinWidget)
meth public abstract void updateUI(org.netbeans.api.visual.vmd.VMDConnectionWidget,org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth public abstract void updateUI(org.netbeans.api.visual.vmd.VMDNodeWidget,org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth public abstract void updateUI(org.netbeans.api.visual.vmd.VMDPinWidget,org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
supr java.lang.Object

CLSS public org.netbeans.api.visual.vmd.VMDConnectionWidget
cons public init(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.router.Router)
cons public init(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.vmd.VMDColorScheme)
meth public void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
supr org.netbeans.api.visual.widget.ConnectionWidget
hfds scheme

CLSS public final org.netbeans.api.visual.vmd.VMDFactory
meth public static org.netbeans.api.visual.border.Border createVMDNodeBorder()
meth public static org.netbeans.api.visual.border.Border createVMDNodeBorder(java.awt.Color,int,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color,java.awt.Color)
meth public static org.netbeans.api.visual.vmd.VMDColorScheme getNetBeans60Scheme()
meth public static org.netbeans.api.visual.vmd.VMDColorScheme getOriginalScheme()
supr java.lang.Object
hfds SCHEME_NB60,SCHEME_ORIGINAL

CLSS public org.netbeans.api.visual.vmd.VMDGlyphSetWidget
cons public init(org.netbeans.api.visual.widget.Scene)
meth public void setGlyphs(java.util.List<java.awt.Image>)
supr org.netbeans.api.visual.widget.Widget

CLSS public org.netbeans.api.visual.vmd.VMDGraphScene
cons public init()
cons public init(org.netbeans.api.visual.vmd.VMDColorScheme)
fld public final static java.lang.String PIN_ID_DEFAULT_SUFFIX = "#default"
meth protected org.netbeans.api.visual.widget.Widget attachEdgeWidget(java.lang.String)
meth protected org.netbeans.api.visual.widget.Widget attachNodeWidget(java.lang.String)
meth protected org.netbeans.api.visual.widget.Widget attachPinWidget(java.lang.String,java.lang.String)
meth protected void attachEdgeSourceAnchor(java.lang.String,java.lang.String,java.lang.String)
meth protected void attachEdgeTargetAnchor(java.lang.String,java.lang.String,java.lang.String)
meth public void layoutScene()
supr org.netbeans.api.visual.graph.GraphPinScene<java.lang.String,java.lang.String,java.lang.String>
hfds backgroundLayer,connectionLayer,mainLayer,moveAction,moveControlPointAction,router,sceneLayout,scheme,upperLayer

CLSS public abstract interface org.netbeans.api.visual.vmd.VMDMinimizeAbility
meth public abstract void collapseWidget()
meth public abstract void expandWidget()

CLSS public org.netbeans.api.visual.vmd.VMDNodeAnchor
cons public init(org.netbeans.api.visual.widget.Widget)
cons public init(org.netbeans.api.visual.widget.Widget,boolean)
cons public init(org.netbeans.api.visual.widget.Widget,boolean,org.netbeans.api.visual.vmd.VMDColorScheme)
meth protected void notifyEntryAdded(org.netbeans.api.visual.anchor.Anchor$Entry)
meth protected void notifyEntryRemoved(org.netbeans.api.visual.anchor.Anchor$Entry)
meth protected void notifyRevalidate()
meth public org.netbeans.api.visual.anchor.Anchor$Result compute(org.netbeans.api.visual.anchor.Anchor$Entry)
supr org.netbeans.api.visual.anchor.Anchor
hfds requiresRecalculation,results,scheme,vertical

CLSS public org.netbeans.api.visual.vmd.VMDNodeWidget
cons public init(org.netbeans.api.visual.widget.Scene)
cons public init(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.vmd.VMDColorScheme)
intf org.netbeans.api.visual.model.StateModel$Listener
intf org.netbeans.api.visual.vmd.VMDMinimizeAbility
meth protected boolean isMinimizableWidget(org.netbeans.api.visual.widget.Widget)
meth protected void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth public boolean isMinimized()
meth public java.lang.String getNodeName()
meth public org.netbeans.api.visual.anchor.Anchor createAnchorPin(org.netbeans.api.visual.anchor.Anchor)
meth public org.netbeans.api.visual.anchor.Anchor getNodeAnchor()
meth public org.netbeans.api.visual.widget.LabelWidget getNodeNameWidget()
meth public org.netbeans.api.visual.widget.Widget getHeader()
meth public org.netbeans.api.visual.widget.Widget getMinimizeButton()
meth public org.netbeans.api.visual.widget.Widget getPinsSeparator()
meth public void attachPinWidget(org.netbeans.api.visual.widget.Widget)
meth public void collapseWidget()
meth public void expandWidget()
meth public void setGlyphs(java.util.List<java.awt.Image>)
meth public void setMinimized(boolean)
meth public void setNodeImage(java.awt.Image)
meth public void setNodeName(java.lang.String)
meth public void setNodeProperties(java.awt.Image,java.lang.String,java.lang.String,java.util.List<java.awt.Image>)
meth public void setNodeType(java.lang.String)
meth public void sortPins(java.util.HashMap<java.lang.String,java.util.List<org.netbeans.api.visual.widget.Widget>>)
meth public void stateChanged()
meth public void toggleMinimized()
supr org.netbeans.api.visual.widget.Widget
hfds glyphSetWidget,header,imageWidget,minimizeWidget,nameWidget,nodeAnchor,pinCategoryWidgets,pinsSeparator,proxyAnchorCache,scheme,stateModel,typeWidget
hcls ToggleMinimizedAction

CLSS public org.netbeans.api.visual.vmd.VMDPinWidget
cons public init(org.netbeans.api.visual.widget.Scene)
cons public init(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.vmd.VMDColorScheme)
meth protected void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth public java.lang.String getPinName()
meth public org.netbeans.api.visual.anchor.Anchor createAnchor()
meth public org.netbeans.api.visual.widget.Widget getPinNameWidget()
meth public void setGlyphs(java.util.List<java.awt.Image>)
meth public void setPinName(java.lang.String)
meth public void setProperties(java.lang.String,java.util.List<java.awt.Image>)
supr org.netbeans.api.visual.widget.Widget
hfds anchor,glyphsWidget,nameWidget,scheme

CLSS public final org.netbeans.api.visual.widget.BirdViewController
meth public void hide()
meth public void setWindowSize(java.awt.Dimension)
meth public void setZoomFactor(double)
meth public void show()
supr java.lang.Object
hfds birdView

CLSS public org.netbeans.api.visual.widget.ComponentWidget
cons public init(org.netbeans.api.visual.widget.Scene,java.awt.Component)
meth protected final java.awt.Rectangle calculateClientArea()
meth protected final void notifyAdded()
meth protected final void notifyRemoved()
meth protected final void paintWidget()
meth public final boolean isComponentVisible()
meth public final java.awt.Component getComponent()
meth public final void setComponentVisible(boolean)
supr org.netbeans.api.visual.widget.Widget
hfds component,componentAdded,componentListener,componentVisible,componentWrapper,validateListener,widgetAdded,zoomFactor
hcls ComponentComponentListener,ComponentSceneListener,ComponentWrapper

CLSS public org.netbeans.api.visual.widget.ConnectionWidget
cons public init(org.netbeans.api.visual.widget.Scene)
innr public final static !enum RoutingPolicy
meth protected java.awt.Cursor getCursorAt(java.awt.Point)
meth protected java.awt.Rectangle calculateClientArea()
meth protected void paintWidget()
meth public boolean isHitAt(java.awt.Point)
meth public boolean isValidated()
meth public double getTargetAnchorShapeRotation()
meth public final boolean isFirstControlPointHitAt(java.awt.Point)
meth public final boolean isLastControlPointHitAt(java.awt.Point)
meth public final boolean isPaintControlPoints()
meth public final boolean isRouted()
meth public final int getControlPointHitAt(java.awt.Point)
meth public final java.awt.Color getLineColor()
meth public final java.awt.Cursor getControlPointsCursor()
meth public final java.awt.Point getFirstControlPoint()
meth public final java.awt.Point getLastControlPoint()
meth public final java.awt.Stroke getStroke()
meth public final org.netbeans.api.visual.anchor.Anchor getSourceAnchor()
meth public final org.netbeans.api.visual.anchor.Anchor getTargetAnchor()
meth public final org.netbeans.api.visual.router.Router getRouter()
meth public final org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy getRoutingPolicy()
meth public final void calculateRouting()
meth public final void reroute()
meth public final void setControlPointsCursor(java.awt.Cursor)
meth public final void setLineColor(java.awt.Color)
meth public final void setPaintControlPoints(boolean)
meth public final void setRouter(org.netbeans.api.visual.router.Router)
meth public final void setRoutingPolicy(org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy)
meth public final void setSourceAnchor(org.netbeans.api.visual.anchor.Anchor)
meth public final void setStroke(java.awt.Stroke)
meth public final void setTargetAnchor(org.netbeans.api.visual.anchor.Anchor)
meth public int getControlPointCutDistance()
meth public java.awt.Point getControlPoint(int)
meth public java.util.List<java.awt.Point> getControlPoints()
meth public org.netbeans.api.visual.anchor.Anchor$Entry getSourceAnchorEntry()
meth public org.netbeans.api.visual.anchor.Anchor$Entry getTargetAnchorEntry()
meth public org.netbeans.api.visual.anchor.AnchorShape getSourceAnchorShape()
meth public org.netbeans.api.visual.anchor.AnchorShape getTargetAnchorShape()
meth public org.netbeans.api.visual.anchor.PointShape getControlPointShape()
meth public org.netbeans.api.visual.anchor.PointShape getEndPointShape()
meth public void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth public void removeConstraint(org.netbeans.api.visual.widget.Widget)
meth public void setConstraint(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment,float)
meth public void setConstraint(org.netbeans.api.visual.widget.Widget,org.netbeans.api.visual.layout.LayoutFactory$ConnectionWidgetLayoutAlignment,int)
meth public void setControlPointCutDistance(int)
meth public void setControlPointShape(org.netbeans.api.visual.anchor.PointShape)
meth public void setControlPoints(java.util.Collection<java.awt.Point>,boolean)
meth public void setEndPointShape(org.netbeans.api.visual.anchor.PointShape)
meth public void setSourceAnchorShape(org.netbeans.api.visual.anchor.AnchorShape)
meth public void setTargetAnchorShape(org.netbeans.api.visual.anchor.AnchorShape)
supr org.netbeans.api.visual.widget.Widget
hfds HIT_DISTANCE_SQUARE,STROKE_DEFAULT,connectionWidgetLayout,controlPointCutDistance,controlPointShape,controlPoints,controlPointsCursor,controlPointsUm,endPointShape,lineColor,paintControlPoints,router,routingPolicy,routingRequired,sourceAnchor,sourceAnchorShape,sourceEntry,stroke,targetAnchor,targetAnchorShape,targetEntry
hcls ConnectionEntry

CLSS public final static !enum org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy
 outer org.netbeans.api.visual.widget.ConnectionWidget
fld public final static org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy ALWAYS_ROUTE
fld public final static org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy DISABLE_ROUTING
fld public final static org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy DISABLE_ROUTING_UNTIL_END_POINT_IS_MOVED
fld public final static org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy UPDATE_END_POINTS_ONLY
meth public static org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy valueOf(java.lang.String)
meth public static org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy[] values()
supr java.lang.Enum<org.netbeans.api.visual.widget.ConnectionWidget$RoutingPolicy>

CLSS public org.netbeans.api.visual.widget.ConvolveWidget
cons public init(org.netbeans.api.visual.widget.Scene,java.awt.image.ConvolveOp)
meth protected void paintChildren()
meth public java.awt.image.ConvolveOp getConvolveOp()
meth public void clearCache()
meth public void setConvolveOp(java.awt.image.ConvolveOp)
supr org.netbeans.api.visual.widget.Widget
hfds TRANSPARENT,convolveOp,image,imageGraphics

CLSS public final !enum org.netbeans.api.visual.widget.EventProcessingType
fld public final static org.netbeans.api.visual.widget.EventProcessingType ALL_WIDGETS
fld public final static org.netbeans.api.visual.widget.EventProcessingType FOCUSED_WIDGET_AND_ITS_CHILDREN
fld public final static org.netbeans.api.visual.widget.EventProcessingType FOCUSED_WIDGET_AND_ITS_CHILDREN_AND_ITS_PARENTS
fld public final static org.netbeans.api.visual.widget.EventProcessingType FOCUSED_WIDGET_AND_ITS_PARENTS
meth public static org.netbeans.api.visual.widget.EventProcessingType valueOf(java.lang.String)
meth public static org.netbeans.api.visual.widget.EventProcessingType[] values()
supr java.lang.Enum<org.netbeans.api.visual.widget.EventProcessingType>

CLSS public org.netbeans.api.visual.widget.FreeConnectionWidget
cons public init(org.netbeans.api.visual.widget.Scene)
cons public init(org.netbeans.api.visual.widget.Scene,double,double)
meth public void addRemoveControlPoint(java.awt.Point)
meth public void setSensitivity(double,double)
supr org.netbeans.api.visual.widget.ConnectionWidget
hfds createSensitivity,deleteSensitivity

CLSS public org.netbeans.api.visual.widget.ImageWidget
cons public init(org.netbeans.api.visual.widget.Scene)
cons public init(org.netbeans.api.visual.widget.Scene,java.awt.Image)
meth protected java.awt.Rectangle calculateClientArea()
meth protected void paintWidget()
meth public boolean isPaintAsDisabled()
meth public java.awt.Image getImage()
meth public void setImage(java.awt.Image)
meth public void setPaintAsDisabled(boolean)
supr org.netbeans.api.visual.widget.Widget
hfds disabledImage,height,image,observer,paintAsDisabled,width

CLSS public org.netbeans.api.visual.widget.LabelWidget
cons public init(org.netbeans.api.visual.widget.Scene)
cons public init(org.netbeans.api.visual.widget.Scene,java.lang.String)
innr public final static !enum Alignment
innr public final static !enum Orientation
innr public final static !enum VerticalAlignment
meth protected java.awt.Rectangle calculateClientArea()
meth protected void paintWidget()
meth public boolean isPaintAsDisabled()
meth public boolean isUseGlyphVector()
meth public java.lang.String getLabel()
meth public org.netbeans.api.visual.widget.LabelWidget$Alignment getAlignment()
meth public org.netbeans.api.visual.widget.LabelWidget$Orientation getOrientation()
meth public org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment getVerticalAlignment()
meth public void setAlignment(org.netbeans.api.visual.widget.LabelWidget$Alignment)
meth public void setLabel(java.lang.String)
meth public void setOrientation(org.netbeans.api.visual.widget.LabelWidget$Orientation)
meth public void setPaintAsDisabled(boolean)
meth public void setUseGlyphVector(boolean)
meth public void setVerticalAlignment(org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment)
supr org.netbeans.api.visual.widget.Widget
hfds alignment,cacheFont,cacheGlyphVector,cacheLabel,label,orientation,paintAsDisabled,useGlyphVector,verticalAlignment

CLSS public final static !enum org.netbeans.api.visual.widget.LabelWidget$Alignment
 outer org.netbeans.api.visual.widget.LabelWidget
fld public final static org.netbeans.api.visual.widget.LabelWidget$Alignment BASELINE
fld public final static org.netbeans.api.visual.widget.LabelWidget$Alignment CENTER
fld public final static org.netbeans.api.visual.widget.LabelWidget$Alignment LEFT
fld public final static org.netbeans.api.visual.widget.LabelWidget$Alignment RIGHT
meth public static org.netbeans.api.visual.widget.LabelWidget$Alignment valueOf(java.lang.String)
meth public static org.netbeans.api.visual.widget.LabelWidget$Alignment[] values()
supr java.lang.Enum<org.netbeans.api.visual.widget.LabelWidget$Alignment>

CLSS public final static !enum org.netbeans.api.visual.widget.LabelWidget$Orientation
 outer org.netbeans.api.visual.widget.LabelWidget
fld public final static org.netbeans.api.visual.widget.LabelWidget$Orientation NORMAL
fld public final static org.netbeans.api.visual.widget.LabelWidget$Orientation ROTATE_90
meth public static org.netbeans.api.visual.widget.LabelWidget$Orientation valueOf(java.lang.String)
meth public static org.netbeans.api.visual.widget.LabelWidget$Orientation[] values()
supr java.lang.Enum<org.netbeans.api.visual.widget.LabelWidget$Orientation>

CLSS public final static !enum org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment
 outer org.netbeans.api.visual.widget.LabelWidget
fld public final static org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment BASELINE
fld public final static org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment BOTTOM
fld public final static org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment CENTER
fld public final static org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment TOP
meth public static org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment valueOf(java.lang.String)
meth public static org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment[] values()
supr java.lang.Enum<org.netbeans.api.visual.widget.LabelWidget$VerticalAlignment>

CLSS public org.netbeans.api.visual.widget.LayerWidget
cons public init(org.netbeans.api.visual.widget.Scene)
meth protected boolean isRepaintRequiredForRevalidating()
meth public boolean isHitAt(java.awt.Point)
supr org.netbeans.api.visual.widget.Widget

CLSS public org.netbeans.api.visual.widget.LevelOfDetailsWidget
cons public init(org.netbeans.api.visual.widget.Scene,double,double,double,double)
meth public boolean isHitAt(java.awt.Point)
meth public void paintChildren()
supr org.netbeans.api.visual.widget.Widget
hfds hardMaximalZoom,hardMinimalZoom,softMaximalZoom,softMinimalZoom

CLSS public org.netbeans.api.visual.widget.ResourceTable
cons public init()
cons public init(org.netbeans.api.visual.widget.ResourceTable)
fld public final static java.lang.String PARENT_RESOURCE_TABLE = "ParentResourceTable"
meth public java.lang.Object getProperty(java.lang.String)
meth public java.util.Set<java.lang.String> getLocalPropertyNames()
meth public org.netbeans.api.visual.widget.ResourceTable getParentTable()
meth public void addProperty(java.lang.String,java.lang.Object)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void clear()
meth public void removeParent()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void setParentTable(org.netbeans.api.visual.widget.ResourceTable)
supr java.lang.Object
hfds childrenTables,listeners,parentTable,properties,propertyListeners

CLSS public org.netbeans.api.visual.widget.Scene
cons public init()
innr public abstract interface static SceneListener
meth protected boolean isRepaintRequiredForRevalidating()
meth public boolean isValidated()
meth public final double getZoomFactor()
meth public final java.awt.Graphics2D getGraphics()
meth public final java.awt.Point convertSceneToView(java.awt.Point)
meth public final java.awt.Rectangle convertSceneToView(java.awt.Rectangle)
meth public final java.awt.Rectangle getMaximumBounds()
meth public final java.lang.String getActiveTool()
meth public final org.netbeans.api.visual.action.WidgetAction$Chain getPriorActions()
meth public final org.netbeans.api.visual.animator.SceneAnimator getSceneAnimator()
meth public final org.netbeans.api.visual.laf.InputBindings getInputBindings()
meth public final org.netbeans.api.visual.laf.LookFeel getLookFeel()
meth public final org.netbeans.api.visual.widget.EventProcessingType getKeyEventProcessingType()
meth public final org.netbeans.api.visual.widget.Widget getFocusedWidget()
meth public final void addSceneListener(org.netbeans.api.visual.widget.Scene$SceneListener)
meth public final void paint(java.awt.Graphics2D)
meth public final void removeSceneListener(org.netbeans.api.visual.widget.Scene$SceneListener)
meth public final void setFocusedWidget(org.netbeans.api.visual.widget.Widget)
meth public final void setKeyEventProcessingType(org.netbeans.api.visual.widget.EventProcessingType)
meth public final void setLookFeel(org.netbeans.api.visual.laf.LookFeel)
meth public final void setMaximumBounds(java.awt.Rectangle)
meth public final void setZoomFactor(double)
meth public final void validate()
meth public final void validate(java.awt.Graphics2D)
meth public java.awt.Font getDefaultFont()
meth public java.awt.Point convertViewToScene(java.awt.Point)
meth public java.awt.Rectangle convertViewToScene(java.awt.Rectangle)
meth public javax.swing.JComponent createSatelliteView()
meth public javax.swing.JComponent createView()
meth public javax.swing.JComponent getView()
meth public org.netbeans.api.visual.action.WidgetAction createWidgetHoverAction()
meth public org.netbeans.api.visual.widget.BirdViewController createBirdView()
meth public org.netbeans.api.visual.widget.ResourceTable getResourceTable()
meth public void setActiveTool(java.lang.String)
meth public void setResourceTable(org.netbeans.api.visual.widget.ResourceTable)
supr org.netbeans.api.visual.widget.Widget
hfds activeTool,component,defaultFont,extendSceneOnly,focusedWidget,graphics,inputBindings,keyEventProcessingType,lookFeel,maximumBounds,paintEverything,priorActions,repaintRegion,repaintWidgets,resourceTable,sceneAnimator,sceneListeners,viewShowing,widgetHoverAction,zoomFactor
hcls WidgetHoverAction

CLSS public abstract interface static org.netbeans.api.visual.widget.Scene$SceneListener
 outer org.netbeans.api.visual.widget.Scene
meth public abstract void sceneRepaint()
meth public abstract void sceneValidated()
meth public abstract void sceneValidating()

CLSS public org.netbeans.api.visual.widget.ScrollWidget
cons public init(org.netbeans.api.visual.widget.Scene)
cons public init(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.widget.Widget)
meth protected java.awt.Rectangle calculateClientArea()
meth public final org.netbeans.api.visual.widget.Widget getView()
meth public final void setView(org.netbeans.api.visual.widget.Widget)
supr org.netbeans.api.visual.widget.Widget
hfds BAR_HORIZONTAL_SIZE,BAR_VERTICAL_SIZE,BORDER_LOWERED,BORDER_RAISED,POINT_EMPTY,RECTANGLE_EMPTY,downArrow,horizontalSlider,leftArrow,rightArrow,upArrow,verticalSlider,view,viewport
hcls BlockScrollAction,ButtonWidget,ScrollLayout,SliderAction,SliderWidget,UnitScrollProvider

CLSS public org.netbeans.api.visual.widget.SeparatorWidget
cons public init(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.widget.SeparatorWidget$Orientation)
innr public final static !enum Orientation
meth protected java.awt.Rectangle calculateClientArea()
meth protected void paintWidget()
meth public int getThickness()
meth public org.netbeans.api.visual.widget.SeparatorWidget$Orientation getOrientation()
meth public void setOrientation(org.netbeans.api.visual.widget.SeparatorWidget$Orientation)
meth public void setThickness(int)
supr org.netbeans.api.visual.widget.Widget
hfds orientation,thickness

CLSS public final static !enum org.netbeans.api.visual.widget.SeparatorWidget$Orientation
 outer org.netbeans.api.visual.widget.SeparatorWidget
fld public final static org.netbeans.api.visual.widget.SeparatorWidget$Orientation HORIZONTAL
fld public final static org.netbeans.api.visual.widget.SeparatorWidget$Orientation VERTICAL
meth public static org.netbeans.api.visual.widget.SeparatorWidget$Orientation valueOf(java.lang.String)
meth public static org.netbeans.api.visual.widget.SeparatorWidget$Orientation[] values()
supr java.lang.Enum<org.netbeans.api.visual.widget.SeparatorWidget$Orientation>

CLSS public org.netbeans.api.visual.widget.SwingScrollWidget
cons public init(org.netbeans.api.visual.widget.Scene)
cons public init(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.widget.Widget)
meth protected java.awt.Rectangle calculateClientArea()
meth public final org.netbeans.api.visual.widget.Widget getView()
meth public final void setView(org.netbeans.api.visual.widget.Widget)
supr org.netbeans.api.visual.widget.Widget
hfds BAR_HORIZONTAL_SIZE,BAR_VERTICAL_SIZE,horizontalListener,horizontalScroll,horizontalWidget,verticalListener,verticalScroll,verticalWidget,view,viewport
hcls MyAdjustmentListener,ScrollLayout

CLSS public org.netbeans.api.visual.widget.Widget
cons public init(org.netbeans.api.visual.widget.Scene)
innr public abstract interface static Dependency
intf javax.accessibility.Accessible
intf org.openide.util.Lookup$Provider
meth protected boolean isRepaintRequiredForRevalidating()
meth protected final void updateResources(org.netbeans.api.visual.widget.Widget,boolean)
meth protected java.awt.Cursor getCursorAt(java.awt.Point)
meth protected java.awt.Graphics2D getGraphics()
meth protected java.awt.Rectangle calculateClientArea()
meth protected void notifyAdded()
meth protected void notifyBackgroundChanged(java.awt.Paint)
meth protected void notifyFontChanged(java.awt.Font)
meth protected void notifyForegroundChanged(java.awt.Color)
meth protected void notifyRemoved()
meth protected void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth protected void paintBackground()
meth protected void paintBorder()
meth protected void paintChildren()
meth protected void paintWidget()
meth public boolean isHitAt(java.awt.Point)
meth public boolean isValidated()
meth public final boolean equals(java.lang.Object)
meth public final boolean isCheckClipping()
meth public final boolean isEnabled()
meth public final boolean isOpaque()
meth public final boolean isPreferredBoundsSet()
meth public final boolean isVisible()
meth public final int hashCode()
meth public final java.awt.Color getForeground()
meth public final java.awt.Cursor getCursor()
meth public final java.awt.Dimension getMaximumSize()
meth public final java.awt.Dimension getMinimumSize()
meth public final java.awt.Dimension getPreferredSize()
meth public final java.awt.Font getFont()
meth public final java.awt.Paint getBackground()
meth public final java.awt.Point convertLocalToScene(java.awt.Point)
meth public final java.awt.Point convertSceneToLocal(java.awt.Point)
meth public final java.awt.Point getLocation()
meth public final java.awt.Point getPreferredLocation()
meth public final java.awt.Rectangle convertLocalToScene(java.awt.Rectangle)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final java.awt.Rectangle convertSceneToLocal(java.awt.Rectangle)
meth public final java.awt.Rectangle getBounds()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final java.awt.Rectangle getClientArea()
meth public final java.awt.Rectangle getPreferredBounds()
meth public final java.lang.Object getChildConstraint(org.netbeans.api.visual.widget.Widget)
meth public final java.lang.String getToolTipText()
meth public final java.util.Collection<org.netbeans.api.visual.widget.Widget$Dependency> getDependencies()
meth public final java.util.List<org.netbeans.api.visual.widget.Widget> getChildren()
meth public final javax.accessibility.AccessibleContext getAccessibleContext()
meth public final org.netbeans.api.visual.action.WidgetAction$Chain createActions(java.lang.String)
meth public final org.netbeans.api.visual.action.WidgetAction$Chain getActions()
meth public final org.netbeans.api.visual.action.WidgetAction$Chain getActions(java.lang.String)
meth public final org.netbeans.api.visual.border.Border getBorder()
meth public final org.netbeans.api.visual.layout.Layout getLayout()
meth public final org.netbeans.api.visual.model.ObjectState getState()
meth public final org.netbeans.api.visual.widget.Scene getScene()
meth public final org.netbeans.api.visual.widget.Widget getParentWidget()
meth public final void addChild(int,org.netbeans.api.visual.widget.Widget)
meth public final void addChild(int,org.netbeans.api.visual.widget.Widget,java.lang.Object)
meth public final void addChild(org.netbeans.api.visual.widget.Widget)
meth public final void addChild(org.netbeans.api.visual.widget.Widget,java.lang.Object)
meth public final void addChildren(java.util.List<? extends org.netbeans.api.visual.widget.Widget>)
meth public final void addDependency(org.netbeans.api.visual.widget.Widget$Dependency)
meth public final void bringToBack()
meth public final void bringToFront()
meth public final void paint()
meth public final void removeChild(org.netbeans.api.visual.widget.Widget)
meth public final void removeChildren()
meth public final void removeChildren(java.util.List<org.netbeans.api.visual.widget.Widget>)
meth public final void removeDependency(org.netbeans.api.visual.widget.Widget$Dependency)
meth public final void removeFromParent()
meth public final void repaint()
meth public final void resolveBounds(java.awt.Point,java.awt.Rectangle)
meth public final void revalidate()
meth public final void revalidate(boolean)
meth public final void setAccessibleContext(javax.accessibility.AccessibleContext)
meth public final void setBackground(java.awt.Paint)
meth public final void setBackgroundFromResource(java.lang.String)
meth public final void setBorder(javax.swing.border.Border)
meth public final void setBorder(org.netbeans.api.visual.border.Border)
meth public final void setCheckClipping(boolean)
meth public final void setChildConstraint(org.netbeans.api.visual.widget.Widget,java.lang.Object)
meth public final void setCursor(java.awt.Cursor)
meth public final void setEnabled(boolean)
meth public final void setFont(java.awt.Font)
meth public final void setFontFromResource(java.lang.String)
meth public final void setForeground(java.awt.Color)
meth public final void setForegroundFromResource(java.lang.String)
meth public final void setLayout(org.netbeans.api.visual.layout.Layout)
meth public final void setMaximumSize(java.awt.Dimension)
meth public final void setMinimumSize(java.awt.Dimension)
meth public final void setOpaque(boolean)
meth public final void setPreferredBounds(java.awt.Rectangle)
meth public final void setPreferredLocation(java.awt.Point)
meth public final void setPreferredSize(java.awt.Dimension)
meth public final void setState(org.netbeans.api.visual.model.ObjectState)
meth public final void setToolTipText(java.lang.String)
meth public final void setVisible(boolean)
meth public org.netbeans.api.visual.widget.ResourceTable getResourceTable()
meth public org.openide.util.Lookup getLookup()
meth public void setResourceTable(org.netbeans.api.visual.widget.ResourceTable)
supr java.lang.Object
hfds EMPTY_HASH_MAP,MESSAGE_NULL_BOUNDS,accessibleContext,actionsChain,background,backgroundListener,backgroundProperty,border,bounds,calculatedPreferredBounds,checkClipping,children,childrenUm,constraints,cursor,dependencies,enabled,font,fontListener,fontProperties,foreground,foregroundListener,foregroundProperty,layout,location,maximumSize,minimumSize,opaque,parentWidget,preferredBounds,preferredLocation,preferredSize,requiresFullJustification,requiresFullValidation,requiresPartJustification,requiresPartValidation,resourceTable,scene,state,toolTipText,toolsActions,visible

CLSS public abstract interface static org.netbeans.api.visual.widget.Widget$Dependency
 outer org.netbeans.api.visual.widget.Widget
meth public abstract void revalidateDependency()

CLSS public org.netbeans.api.visual.widget.general.IconNodeWidget
cons public init(org.netbeans.api.visual.widget.Scene)
cons public init(org.netbeans.api.visual.widget.Scene,org.netbeans.api.visual.widget.general.IconNodeWidget$TextOrientation)
innr public final static !enum TextOrientation
meth public final org.netbeans.api.visual.widget.ImageWidget getImageWidget()
meth public final org.netbeans.api.visual.widget.LabelWidget getLabelWidget()
meth public final void setImage(java.awt.Image)
meth public final void setLabel(java.lang.String)
meth public void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
supr org.netbeans.api.visual.widget.Widget
hfds imageWidget,labelWidget

CLSS public final static !enum org.netbeans.api.visual.widget.general.IconNodeWidget$TextOrientation
 outer org.netbeans.api.visual.widget.general.IconNodeWidget
fld public final static org.netbeans.api.visual.widget.general.IconNodeWidget$TextOrientation BOTTOM_CENTER
fld public final static org.netbeans.api.visual.widget.general.IconNodeWidget$TextOrientation RIGHT_CENTER
meth public static org.netbeans.api.visual.widget.general.IconNodeWidget$TextOrientation valueOf(java.lang.String)
meth public static org.netbeans.api.visual.widget.general.IconNodeWidget$TextOrientation[] values()
supr java.lang.Enum<org.netbeans.api.visual.widget.general.IconNodeWidget$TextOrientation>

CLSS public org.netbeans.api.visual.widget.general.ListItemWidget
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.api.visual.widget.Scene)
meth public void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
supr org.netbeans.api.visual.widget.LabelWidget

CLSS public org.netbeans.api.visual.widget.general.ListWidget
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.api.visual.widget.Scene)
meth public final org.netbeans.api.visual.widget.ImageWidget getImageWidget()
meth public final org.netbeans.api.visual.widget.LabelWidget getLabelWidget()
meth public final org.netbeans.api.visual.widget.Widget getHeader()
meth public final void setImage(java.awt.Image)
meth public final void setLabel(java.lang.String)
meth public void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
supr org.netbeans.api.visual.widget.Widget
hfds header,imageWidget,labelWidget

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

