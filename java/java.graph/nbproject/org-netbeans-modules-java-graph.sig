#Signature file v4.1
#Version 1.24

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

CLSS public abstract interface javax.accessibility.Accessible
meth public abstract javax.accessibility.AccessibleContext getAccessibleContext()

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

CLSS public org.netbeans.modules.java.graph.DependencyGraphScene<%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation>
cons public init(javax.swing.JScrollPane)
cons public init(org.netbeans.modules.java.graph.DependencyGraphScene$ActionsProvider<{org.netbeans.modules.java.graph.DependencyGraphScene%0}>,org.netbeans.modules.java.graph.DependencyGraphScene$HighlightDepthProvider,org.netbeans.modules.java.graph.DependencyGraphScene$VersionProvider<{org.netbeans.modules.java.graph.DependencyGraphScene%0}>,org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider<{org.netbeans.modules.java.graph.DependencyGraphScene%0}>)
innr public abstract interface static ActionsProvider
innr public abstract interface static HighlightDepthProvider
innr public abstract interface static PaintingProvider
innr public abstract interface static VersionProvider
meth protected org.netbeans.api.visual.widget.Widget attachEdgeWidget(org.netbeans.modules.java.graph.GraphEdge)
meth protected org.netbeans.api.visual.widget.Widget attachNodeWidget(org.netbeans.modules.java.graph.GraphNode)
meth protected void attachEdgeSourceAnchor(org.netbeans.modules.java.graph.GraphEdge,org.netbeans.modules.java.graph.GraphNode,org.netbeans.modules.java.graph.GraphNode)
meth protected void attachEdgeTargetAnchor(org.netbeans.modules.java.graph.GraphEdge,org.netbeans.modules.java.graph.GraphNode,org.netbeans.modules.java.graph.GraphNode)
meth protected void notifyStateChanged(org.netbeans.api.visual.model.ObjectState,org.netbeans.api.visual.model.ObjectState)
meth public boolean isIncluded({org.netbeans.modules.java.graph.DependencyGraphScene%0})
meth public int getMaxNodeDepth()
meth public org.netbeans.modules.java.graph.GraphEdge addEdge({org.netbeans.modules.java.graph.DependencyGraphScene%0},{org.netbeans.modules.java.graph.DependencyGraphScene%0})
meth public org.netbeans.modules.java.graph.GraphNode getGraphNodeRepresentant(org.netbeans.modules.java.graph.GraphNodeImplementation)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.java.graph.GraphNode<{org.netbeans.modules.java.graph.DependencyGraphScene%0}> getRootGraphNode()
meth public void addGraphNodeImpl({org.netbeans.modules.java.graph.DependencyGraphScene%0})
meth public void calculatePrimaryPathsAndLevels()
meth public void highlightDepth(int)
meth public void initialLayout()
meth public void notifyModelChanged(org.netbeans.modules.java.graph.GraphEdge<{org.netbeans.modules.java.graph.DependencyGraphScene%0}>)
meth public void notifyModelChanged(org.netbeans.modules.java.graph.GraphNode<{org.netbeans.modules.java.graph.DependencyGraphScene%0}>)
meth public void resetHighlight()
meth public void setMyZoomFactor(double)
meth public void setSearchString(java.lang.String)
meth public void setSurroundingScrollPane(javax.swing.JScrollPane)
meth public void updateVisibility()
supr org.netbeans.api.visual.graph.GraphScene<org.netbeans.modules.java.graph.GraphNode<{org.netbeans.modules.java.graph.DependencyGraphScene%0}>,org.netbeans.modules.java.graph.GraphEdge<{org.netbeans.modules.java.graph.DependencyGraphScene%0}>>
hfds EMPTY_SELECTION,allActionsP,animated,connectionLayer,editAction,fitViewL,highlightProvider,highlightV,highlitedZoomToFitAction,hoverAction,layout,mainLayer,maxDepth,moveAction,nodeActionProvider,paintingProvider,panAction,pane,popupMenuAction,rootNode,sceneZoomToFitAction,versionProvider,zoomAction
hcls AllActionsProvider,FitToViewLayout,FruchtermanReingoldLayoutAction,HierarchicalGraphLayoutAction,HighlightedZoomToFitAction,HoverController,SceneZoomToFitAction,TreeGraphLayoutHorizontalAction,TreeGraphLayoutVerticalAction

CLSS public abstract interface static org.netbeans.modules.java.graph.DependencyGraphScene$ActionsProvider<%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation>
 outer org.netbeans.modules.java.graph.DependencyGraphScene
meth public abstract javax.swing.Action createExcludeDepAction(org.netbeans.modules.java.graph.DependencyGraphScene,org.netbeans.modules.java.graph.GraphNode<{org.netbeans.modules.java.graph.DependencyGraphScene$ActionsProvider%0}>,org.netbeans.modules.java.graph.GraphNode<{org.netbeans.modules.java.graph.DependencyGraphScene$ActionsProvider%0}>)
meth public abstract javax.swing.Action createFixVersionConflictAction(org.netbeans.modules.java.graph.DependencyGraphScene,org.netbeans.modules.java.graph.GraphNode<{org.netbeans.modules.java.graph.DependencyGraphScene$ActionsProvider%0}>,org.netbeans.modules.java.graph.GraphNode<{org.netbeans.modules.java.graph.DependencyGraphScene$ActionsProvider%0}>)
meth public abstract javax.swing.Action createShowGraphAction(org.netbeans.modules.java.graph.GraphNode<{org.netbeans.modules.java.graph.DependencyGraphScene$ActionsProvider%0}>)

CLSS public abstract interface static org.netbeans.modules.java.graph.DependencyGraphScene$HighlightDepthProvider
 outer org.netbeans.modules.java.graph.DependencyGraphScene
meth public abstract int getDepth()

CLSS public abstract interface static org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider<%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation>
 outer org.netbeans.modules.java.graph.DependencyGraphScene
meth public abstract boolean isVisible({org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider%0})
meth public abstract boolean isVisible({org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider%0},{org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider%0})
meth public abstract java.awt.Color getColor({org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider%0})
meth public abstract java.awt.Stroke getStroke({org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider%0},{org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider%0})
meth public abstract javax.swing.Icon getIcon({org.netbeans.modules.java.graph.DependencyGraphScene$PaintingProvider%0})

CLSS public abstract interface static org.netbeans.modules.java.graph.DependencyGraphScene$VersionProvider<%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation>
 outer org.netbeans.modules.java.graph.DependencyGraphScene
fld public final static int VERSION_CONFLICT = 2
fld public final static int VERSION_NO_CONFLICT = 0
fld public final static int VERSION_POTENTIAL_CONFLICT = 1
meth public abstract boolean isIncluded({org.netbeans.modules.java.graph.DependencyGraphScene$VersionProvider%0})
meth public abstract boolean isOmmitedForConflict({org.netbeans.modules.java.graph.DependencyGraphScene$VersionProvider%0})
meth public abstract int compareVersions({org.netbeans.modules.java.graph.DependencyGraphScene$VersionProvider%0},{org.netbeans.modules.java.graph.DependencyGraphScene$VersionProvider%0})
meth public abstract java.lang.String getVersion({org.netbeans.modules.java.graph.DependencyGraphScene$VersionProvider%0})

CLSS public final org.netbeans.modules.java.graph.GraphEdge<%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation>
cons public init({org.netbeans.modules.java.graph.GraphEdge%0},{org.netbeans.modules.java.graph.GraphEdge%0})
meth public boolean isPrimary()
meth public java.lang.String toString()
meth public void setPrimaryPath(boolean)
meth public {org.netbeans.modules.java.graph.GraphEdge%0} getSource()
meth public {org.netbeans.modules.java.graph.GraphEdge%0} getTarget()
supr java.lang.Object
hfds primary,source,target,toString

CLSS public final org.netbeans.modules.java.graph.GraphNode<%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation>
cons public init({org.netbeans.modules.java.graph.GraphNode%0})
 anno 1 org.netbeans.api.annotations.common.NonNull()
fld public final static int MANAGED = 1
fld public final static int OVERRIDES_MANAGED = 2
fld public final static int UNMANAGED = 0
meth public boolean represents({org.netbeans.modules.java.graph.GraphNode%0})
meth public int getPrimaryLevel()
meth public java.lang.String getName()
meth public java.lang.String getTooltipText()
meth public java.util.Set<{org.netbeans.modules.java.graph.GraphNode%0}> getDuplicatesOrConflicts()
meth public void addDuplicateOrConflict({org.netbeans.modules.java.graph.GraphNode%0})
meth public void removeDuplicateOrConflict({org.netbeans.modules.java.graph.GraphNode%0})
meth public void setImpl({org.netbeans.modules.java.graph.GraphNode%0})
meth public void setManagedState(int)
meth public void setParent({org.netbeans.modules.java.graph.GraphNode%0})
meth public void setPrimaryLevel(int)
meth public {org.netbeans.modules.java.graph.GraphNode%0} getImpl()
meth public {org.netbeans.modules.java.graph.GraphNode%0} getParent()
supr java.lang.Object
hfds duplicates,impl,level,managedState,parentAfterFix

CLSS public abstract interface org.netbeans.modules.java.graph.GraphNodeImplementation
meth public abstract <%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation> java.util.List<{%%0}> getChildren()
meth public abstract <%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation> {%%0} getParent()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getQualifiedName()
meth public abstract java.lang.String getTooltipText()

CLSS public abstract interface org.netbeans.modules.java.graph.GraphNodeVisitor<%0 extends org.netbeans.modules.java.graph.GraphNodeImplementation>
meth public abstract boolean endVisit({org.netbeans.modules.java.graph.GraphNodeVisitor%0})
meth public abstract boolean visit({org.netbeans.modules.java.graph.GraphNodeVisitor%0})
meth public boolean accept({org.netbeans.modules.java.graph.GraphNodeVisitor%0})

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

