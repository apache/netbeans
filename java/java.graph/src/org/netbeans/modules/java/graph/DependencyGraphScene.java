/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.export.SceneExporter;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutFactory;
import org.netbeans.api.visual.graph.layout.GraphLayoutSupport;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milos Kleint 
 * @param <I> 
 */
public class DependencyGraphScene<I extends GraphNodeImplementation> extends GraphScene<GraphNode<I>, GraphEdge<I>> {
    
    
    public interface PaintingProvider<I extends GraphNodeImplementation> {        
        /**
         * Determines an icon for the given node.
         * 
         * @param node
         * @return the icon or <code>null</code> if none should be used
         */
        Icon getIcon(I node);
        
        /**
         * Determines if the given node should be visible.
         * 
         * @param node
         * @return <code>true</code> if the node should be visible, otherwise <code>false</code>
         */
        boolean isVisible(I node);
        
        /**
         * Determines if the given edge should be visible.
         * 
         * @param source
         * @param target
         * @return <code>true</code> if the edge should be visible, otherwise <code>false</code>
         */
        boolean isVisible(I source, I target);
        
        /**
         * Determines a color for the given node.
         * 
         * @param node
         * @return the color or <code>null</code> if default should be used
         */
        Color getColor(I node);
        
        /**
         * Determines a stroke for the given edge.
         * 
         * @param source
         * @param target
         * @return the stroke or <code>null</code> if default should be used
         */
        Stroke getStroke(I source, I target);
    }
        
    public interface HighlightDepthProvider {
        int getDepth();
    }
    
    public interface VersionProvider<I extends GraphNodeImplementation> {        
        // conflict type
        public static final int VERSION_NO_CONFLICT = 0;
        public static final int VERSION_POTENTIAL_CONFLICT = 1;
        public static final int VERSION_CONFLICT = 2;
    
        String getVersion(I impl);    
        int compareVersions(I impl1, I impl2);
        boolean isIncluded(I impl);
        boolean isOmmitedForConflict(I impl);
    }
    
    public interface ActionsProvider<I extends GraphNodeImplementation> {
        Action createFixVersionConflictAction(DependencyGraphScene scene, GraphNode<I> rootNode, GraphNode<I> node);
        Action createExcludeDepAction(DependencyGraphScene scene, GraphNode<I> rootNode, GraphNode<I> node);
        Action createShowGraphAction(GraphNode<I> node);
    }
    
    private final VersionProvider versionProvider;
    private final ActionsProvider nodeActionProvider;
    private final HighlightDepthProvider highlightProvider;
    private final PaintingProvider<I> paintingProvider;
    
    private LayerWidget mainLayer;
    private final LayerWidget connectionLayer;
    private GraphNode rootNode;
    private final AllActionsProvider allActionsP = new AllActionsProvider();
    
//    private GraphLayout layout;
    private final WidgetAction moveAction = ActionFactory.createMoveAction(null, allActionsP);
    private final WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction(allActionsP);
    private final WidgetAction zoomAction = ActionFactory.createMouseCenteredZoomAction(1.1);
    private final WidgetAction panAction = ActionFactory.createPanAction();
    private final WidgetAction editAction = ActionFactory.createEditAction(allActionsP);
    private final WidgetAction hoverAction = ActionFactory.createHoverAction(new HoverController());

    private final Action sceneZoomToFitAction = new SceneZoomToFitAction();
    private final Action highlitedZoomToFitAction = new HighlightedZoomToFitAction();

    private FruchtermanReingoldLayout layout;
    private int maxDepth = 0;
    private FitToViewLayout fitViewL;

    private static final Set<GraphNode> EMPTY_SELECTION = new HashSet<>();
    private JScrollPane pane;
    private final AtomicBoolean animated = new AtomicBoolean(false);
    
    private HighlightVisitor highlightV;
    
    public DependencyGraphScene(JScrollPane pane) {
        this(null, null, null, null);
    }
    
    public DependencyGraphScene(ActionsProvider<I> nodeActionProvider, HighlightDepthProvider highlightProvider, VersionProvider<I> versionProvider, PaintingProvider<I> paintingProvider) {                
        this.nodeActionProvider = nodeActionProvider;
        this.highlightProvider = highlightProvider;
        this.versionProvider = versionProvider;
        this.paintingProvider = paintingProvider;
        
        mainLayer = new LayerWidget(this);
        addChild(mainLayer);
        connectionLayer = new LayerWidget(this);
        addChild(connectionLayer);
        
        //this glasspane thing is only here to get rid of connection lines across widgets
        Widget glasspane = new LayerWidget(this) {

            @Override
            protected void paintChildren() {
                if (isCheckClipping()) {
                    Rectangle clipBounds = DependencyGraphScene.this.getGraphics().getClipBounds();
                    for (Widget child : mainLayer.getChildren()) {
                        Point location = child.getLocation();
                        Rectangle bounds = child.getBounds();
                        bounds.translate(location.x, location.y);
                        if (clipBounds == null || bounds.intersects(clipBounds)) {
                            child.paint();
                        }
                    }
                } else {
                    for (Widget child : mainLayer.getChildren()) {
                        child.paint();
                    }
                }
            }

        };
        addChild(glasspane);
        //getActions().addAction(this.createObjectHoverAction());
        getActions().addAction(hoverAction);
        getActions().addAction(ActionFactory.createSelectAction(allActionsP));
        getActions().addAction(zoomAction);
        getActions().addAction(panAction);
        getActions().addAction(editAction);
        getActions().addAction(popupMenuAction);
    }

    boolean isVisible(GraphEdge<I> e) {
        return paintingProvider == null || paintingProvider.isVisible(e.getSource(), e.getTarget());
    }   
    
    boolean isVisible(GraphNode<I> n) {
        return paintingProvider == null || paintingProvider.isVisible(n.getImpl());
    }
    
    Stroke getStroke(GraphEdge<I> e) {
        return paintingProvider != null ? paintingProvider.getStroke(e.getSource(), e.getTarget()) : null;
    }
    
    Color getColor(GraphNode<I> n) {
        return paintingProvider != null ? paintingProvider.getColor(n.getImpl()) : null;
    }
    
    Icon getIcon(GraphNode<I> n) {
        return paintingProvider != null ? paintingProvider.getIcon(n.getImpl()) : null;
    }
    
    public void addGraphNodeImpl(I d) {
        super.addNode(new GraphNode<>(d));
    }
    
    public GraphEdge addEdge(I source, I target) {
        Collection<GraphNode<I>> nodes = getNodes();
        GraphNode<I> s = getGraphNode(nodes, source);
        assert s != null;
        GraphNode<I> t = getGraphNode(nodes, target);
        assert t != null;
        GraphEdge<I> edge = new GraphEdge<>(source, target);
        addEdge(edge);
        setEdgeSource(edge, s);
        setEdgeTarget(edge, t);
        return edge;
    }
    
    private GraphNode<I> getGraphNode(Collection<GraphNode<I>> nodes, I i) {
        for(GraphNode<I> n : nodes) {
            if(n.getImpl().equals(i)) {
                return n;
            }
        }
        assert false : "no node found for " + i.getName();
        return null;
    }

    public void setSearchString(String val) {
        SearchVisitor visitor = new SearchVisitor(this);
        visitor.setSearchString(val);
        visitor.accept(getRootGraphNode().getImpl());
        validate();
        repaint();
        revalidate();
        repaint();
    }
    
    public void notifyModelChanged(GraphNode<I> graphNode) {
        NodeWidget nodeWidget = (NodeWidget) findWidget(graphNode);
        if (nodeWidget != null) {
            nodeWidget.modelChanged();
        }
    }
    
    public void notifyModelChanged(GraphEdge<I> graphEdge) {
        EdgeWidget edgeWidget = (EdgeWidget) findWidget(graphEdge);
        if (edgeWidget != null) {
            edgeWidget.modelChanged();
        }
    }
    
    boolean supportsVersions () {
        return versionProvider != null;
    }    
    
    public boolean isIncluded(I node) {
        if(!supportsVersions()) {
            return true;
        }
        assert versionProvider != null;
        return versionProvider.isIncluded(node);
    }
    
    boolean isConflict(I node) {
        if(!supportsVersions()) {
            return false;
        }
        assert versionProvider != null;
        return versionProvider.isOmmitedForConflict(node);        
    }
            
    String getVersion(I impl) {
        assert versionProvider != null;
        return versionProvider.getVersion(impl);
    } 

    int compareVersions(I n1, I n2) {
        assert versionProvider != null;
        return versionProvider.compareVersions(n1, n2);        
    }
    
    public void setMyZoomFactor(double zoom) {
        setZoomFactor(zoom);
        ArrayList<Widget> arr = new ArrayList<Widget>();
        arr.addAll(mainLayer.getChildren());
        arr.addAll(connectionLayer.getChildren());
        for (Widget wid : arr) {
            if (wid instanceof NodeWidget) {
                ((NodeWidget)wid).updateReadableZoom();
            }
            if (wid instanceof EdgeWidget) {
                ((EdgeWidget)wid).updateReadableZoom();
            }
        }
    }

    public void initialLayout() {
        //start using default layout
        layout =  new FruchtermanReingoldLayout(this, pane);
        layout.invokeLayout();
        addSceneListener(new SceneListener() {

            @Override
            public void sceneRepaint() {
               
            }

            @Override
            public void sceneValidating() {
                
            }

            @Override
            public void sceneValidated() {
                //the first layout has not be non animated, then we can fit to zoom easily
                if (animated.compareAndSet(false, true)) {
                    new SceneZoomToFitAction().actionPerformed(null);
                }
                    
            }
        });
    }
    
    public void setSurroundingScrollPane(JScrollPane pane) {
        this.pane = pane; 
    }
    
    public GraphNode<I> getRootGraphNode() {
        return rootNode;
    }

    public int getMaxNodeDepth() {
        return maxDepth;
    }

    boolean isAnimated () {
        return animated.get();
    }

    @CheckForNull public GraphNode getGraphNodeRepresentant(GraphNodeImplementation impl) {
        for (GraphNode grnode : getNodes()) {
            if (grnode.getImpl().equals(impl)) {
                return grnode;                
            }
            if(supportsVersions() && (grnode.represents(impl))) { 
                return grnode;            
            }
        }
        return null;
    }
    
    @Override protected Widget attachNodeWidget(GraphNode node) {
        if (rootNode == null) {
            rootNode = node;
        }
        if (node.getPrimaryLevel() > maxDepth) {
            maxDepth = node.getPrimaryLevel();
        }
        
        Action fixAction = nodeActionProvider != null ? nodeActionProvider.createFixVersionConflictAction(this, rootNode, node) : null;        
        NodeWidget root = new NodeWidget(this, node, fixAction, hoverAction);
        mainLayer.addChild(root);
        root.setOpaque(true);
        
        root.getActions().addAction(this.createObjectHoverAction());
        root.getActions().addAction(this.createSelectAction());
        root.getActions().addAction(moveAction);
        root.getActions().addAction(editAction);
        root.getActions().addAction(popupMenuAction);
                    
        return root;
    }
    
    @Override protected Widget attachEdgeWidget(GraphEdge edge) {
        EdgeWidget connectionWidget = new EdgeWidget(this, edge);
        connectionLayer.addChild(connectionWidget);
        return connectionWidget;
    }
    
    @Override protected void attachEdgeSourceAnchor(GraphEdge edge,
            GraphNode oldsource,
            GraphNode source) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(findWidget(source)));
        
    }
    
    @Override protected void attachEdgeTargetAnchor(GraphEdge edge,
            GraphNode oldtarget,
            GraphNode target) {
        NodeWidget wid = (NodeWidget)findWidget(target);
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(wid));
    }    
    
    public void updateVisibility() {
        getEdges().stream().forEach(edge -> ((EdgeWidget)findWidget(edge)).updateAppearance(true));
        getNodes().stream().forEach(node -> ((NodeWidget)findWidget(node)).updatePaintContent());
    }
    
    public void calculatePrimaryPathsAndLevels() {
        Collection<GraphNode<I>> ns = getNodes();        
        for (GraphNode<I> n : ns) {
            List<GraphEdge> primaryPathEdges = new ArrayList<>();
            LinkedList<GraphNode> importantNodes = new LinkedList<>(); // XXX node depth?
            addPathToRoot(n, primaryPathEdges, importantNodes);
            for (GraphEdge pe : primaryPathEdges) {
                pe.setPrimaryPath(true);
                }
            int level = primaryPathEdges.size();
            n.setPrimaryLevel(level);
            if (level > maxDepth) {
                maxDepth = level;
            }
        }
    }
    
    void highlightRelated (GraphNode<I> node) {
        List<GraphNode> importantNodes  = new ArrayList<>();
        List<GraphEdge> otherPathsEdges = new ArrayList<>();
        List<GraphEdge> primaryPathEdges = new ArrayList<>();
        List<GraphNode> childrenNodes   = new ArrayList<>();
        List<GraphEdge> childrenEdges   = new ArrayList<>();

        importantNodes.add(node);

        @SuppressWarnings("unchecked")
        List<I> children = node.getImpl().getChildren();
        if (children != null) {
            for (I n : children) {
                GraphNode child = getGraphNodeRepresentant(n);
                if (child != null) {
                    childrenNodes.add(child);
                }
            }
        }

        childrenEdges.addAll(findNodeEdges(node, true, false));

        // primary path
        addPathToRoot(node, primaryPathEdges, importantNodes);

        if(supportsVersions()) {
            // other important paths
            for (GraphNodeImplementation curRep : node.getDuplicatesOrConflicts()) {
                addPathToRoot(curRep, curRep.getParent(), otherPathsEdges, importantNodes);
            }
        }
        EdgeWidget ew;
        for (GraphEdge curE : getEdges()) {
            ew = (EdgeWidget) findWidget(curE);
            if (primaryPathEdges.contains(curE)) {
                ew.setState(EdgeWidget.HIGHLIGHTED_PRIMARY);
            } else if (otherPathsEdges.contains(curE)) {
                ew.setState(EdgeWidget.HIGHLIGHTED);
            } else if (childrenEdges.contains(curE)) {
                ew.setState(EdgeWidget.GRAYED);
            } else {
                ew.setState(EdgeWidget.DISABLED);
            }
        }

        NodeWidget aw;
        for (GraphNode curN : getNodes()) {
            aw = (NodeWidget) findWidget(curN);
            if (importantNodes.contains(curN)) {
                aw.setPaintState(EdgeWidget.REGULAR);
                aw.setReadable(true);
            } else if (childrenNodes.contains(curN)) {
                aw.setPaintState(EdgeWidget.REGULAR);
                aw.setReadable(true);
            } else {
                aw.setPaintState(EdgeWidget.DISABLED);
                aw.setReadable(false);
            }
        }

    }

    private void addPathToRoot(GraphNode node, List<GraphEdge> edges, List<GraphNode> nodes) {
        GraphNodeImplementation parentImpl = node.getParent();
        addPathToRoot(node.getImpl(), parentImpl, edges, nodes);
    }


    private void addPathToRoot(GraphNodeImplementation impl, GraphNodeImplementation parentImpl, List<GraphEdge> edges, List<GraphNode> nodes) {
        final Set<GraphNodeImplementation> seen = new HashSet<>();
        GraphNode grNode;
        while (parentImpl != null) {
            seen.add(parentImpl);
            grNode = getGraphNodeRepresentant(parentImpl);
            if (grNode == null) {
                return;
            }
            GraphNode targetNode = getGraphNodeRepresentant(impl);
            if (targetNode == null) {
                return;
            }
            edges.addAll(findEdgesBetween(grNode, targetNode));
            nodes.add(grNode);
            impl = parentImpl;
            parentImpl = grNode.getParent();
            if (seen.contains(parentImpl)) {
                parentImpl = null;
            }
        }
    }

    private class AllActionsProvider implements PopupMenuProvider, 
            MoveProvider, EditProvider, SelectProvider {

        private Point moveStart;

/*        public void select(Widget wid, Point arg1, boolean arg2) {
            System.out.println("select called...");
            Widget w = wid;
            while (w != null) {
                GraphNode node = (GraphNode)findObject(w);
                if (node != null) {
                    setSelectedObjects(Collections.singleton(node));
                    System.out.println("selected object: " + node.getArtifact().getArtifact().getArtifactId());
                    highlightRelated(node);
                    ((NodeWidget)w).setSelected(true);
                    return;
                }
                w = w.getParentWidget();
            }
        }*/
        
        @Messages({
            "ACT_Export_As_Image=Export As Image",
            "ACT_LayoutSubMenu=Layout",
            "ACT_Export_As_Image_Title=Export Dependency Graph As PNG"
        })
        @Override public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
            JPopupMenu popupMenu = new JPopupMenu();
            if (widget == DependencyGraphScene.this) {
                popupMenu.add(sceneZoomToFitAction);
                final JMenu layoutMenu = new JMenu(Bundle.ACT_LayoutSubMenu());
                popupMenu.add(layoutMenu);
                layoutMenu.add(new FruchtermanReingoldLayoutAction());
                layoutMenu.add(new JSeparator());
                layoutMenu.add(new HierarchicalGraphLayoutAction());
                layoutMenu.add(new TreeGraphLayoutVerticalAction());
                layoutMenu.add(new TreeGraphLayoutHorizontalAction());
                popupMenu.add(new AbstractAction(Bundle.ACT_Export_As_Image()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        File file = new FileChooserBuilder("DependencyGraphScene-ExportDir").setTitle(Bundle.ACT_Export_As_Image_Title())
                                .setAcceptAllFileFilterUsed(false).addFileFilter(new FileNameExtensionFilter("PNG file", "png")).showSaveDialog();
                        if (file != null) {
                            try {
                                DependencyGraphScene theScene = DependencyGraphScene.this;
                                SceneExporter.createImage(theScene, file, SceneExporter.ImageType.PNG, SceneExporter.ZoomType.CURRENT_ZOOM_LEVEL, false, false, -1, -1, -1);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                });
            } else {
                if(nodeActionProvider != null) {
                    GraphNode node = (GraphNode)findObject(widget);
                    
                    boolean addSeparator = false;
                    Action a = nodeActionProvider.createFixVersionConflictAction(DependencyGraphScene.this, rootNode, node);
                    if(a != null) {
                        popupMenu.add(a);
                        addSeparator = true;                        
                    }
                    a = nodeActionProvider.createExcludeDepAction(DependencyGraphScene.this, rootNode, node);
                    if(a != null) {
                        popupMenu.add(a);
                        addSeparator = true;                        
                    }                    
                    if (addSeparator) {
                        popupMenu.add(new JSeparator());
                    }
                    popupMenu.add(highlitedZoomToFitAction);
                    if (!node.isRoot()) {
                        a = nodeActionProvider.createShowGraphAction(node);
                        if(a != null) {
                            popupMenu.add(a);
                        }
                    }
                }
            }
            return popupMenu;
        }

        @Override public void movementStarted(Widget widget) {
            widget.bringToFront();
            moveStart = widget.getLocation();
        }
        @Override public void movementFinished(Widget widget) {
            // little hack to call highlightRelated on mouse click while leaving
            // normal move behaviour on real dragging
            Point moveEnd = widget.getLocation();
            if (moveStart.distance(moveEnd) < 5) {
                Object obj = DependencyGraphScene.this.findObject(widget);
                if (obj instanceof GraphNode) {
                    DependencyGraphScene.this.highlightRelated((GraphNode)obj);
                }
            }
        }
        @Override public Point getOriginalLocation(Widget widget) {
            return widget.getPreferredLocation ();
        }
        @Override public void setNewLocation(Widget widget, Point location) {
            widget.setPreferredLocation (location);
        }

        @Override public void edit(Widget widget) {
            if (DependencyGraphScene.this == widget) {
                sceneZoomToFitAction.actionPerformed(null);
            } else {
                highlitedZoomToFitAction.actionPerformed(null);
            }
        }

        @Override public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }

        @Override public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return true;
        }

        @Override public void select(Widget widget, Point localLocation, boolean invertSelection) {
            setSelectedObjects(EMPTY_SELECTION);
            highlightDepthIntern();
        }

    }        

    /** 
     * Highlights/diminishes graph nodes and edges based on path from root depth 
     * based on the in constructor provided HighlightDepthProvider
     */
    public void highlightDepth(int depth) {
        assert depth > -1 || highlightProvider != null : "using depth highlighting without providing HighlightProvider in c'tor no allowed";
        if(depth == -1) {
            depth = 0;
        }
        if (highlightV == null) {
            highlightV = new HighlightVisitor(this);
        }
        highlightV.setMaxDepth(depth);
        highlightV.accept(getRootGraphNode().getImpl());
        validate();
        repaint();
    }
    
    public void resetHighlight() {
        highlightV = null;
    }
    
    private void highlightDepthIntern () {        
        int depth;
        if(highlightProvider == null) {
            depth = -1; // not available
        } else {
            depth = highlightProvider.getDepth();
        }
        highlightDepth(depth);
    }
    
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);

        if (!previousState.isSelected() && state.isSelected()) {
            highlightDepthIntern();
        }
    }

    private FitToViewLayout getFitToViewLayout () {
        if (fitViewL == null) {
            fitViewL = new FitToViewLayout(this, pane);
        }
        return fitViewL;
    }
        
    private static class FitToViewLayout extends SceneLayout {

        private final DependencyGraphScene depScene;
        private List<? extends Widget> widgets;
        private final JScrollPane parentScrollPane;

        FitToViewLayout(DependencyGraphScene scene, JScrollPane parentScrollPane) {
            super(scene);
            this.depScene = scene;
            this.parentScrollPane = parentScrollPane;
        }

        
        protected void fitToView(@NullAllowed List<? extends Widget> widgets) {
            this.widgets = widgets;
            this.invokeLayout(); 
        }

        @Override
        protected void performLayout() {
            Rectangle rectangle = null;
            List<? extends Widget> toFit = widgets != null ? widgets : depScene.getChildren();
            if (toFit == null) {
                return;
            }

            for (Widget widget : toFit) {
                Rectangle bounds = widget.getBounds();
                if (bounds == null) {
                    continue;
                }
                if (rectangle == null) {
                    rectangle = widget.convertLocalToScene(bounds);
                } else {
                    rectangle = rectangle.union(widget.convertLocalToScene(bounds));
                }
            }
            // margin around
            if (widgets == null) {
                rectangle.grow(5, 5);
            } else {
                rectangle.grow(25, 25);
            }
            Dimension dim = rectangle.getSize();
            Dimension viewDim = parentScrollPane.getViewportBorderBounds().getSize ();
            double zf = Math.min ((double) viewDim.width / dim.width, (double) viewDim.height / dim.height);
            if (depScene.isAnimated()) {
                if (widgets == null) {
                    depScene.getSceneAnimator().animateZoomFactor(zf);
                } else {
                    CenteredZoomAnimator cza = new CenteredZoomAnimator(depScene.getSceneAnimator());
                    cza.setZoomFactor(zf,
                            new Point((int)rectangle.getCenterX(), (int)rectangle.getCenterY()));
                }
            } else {
                depScene.setMyZoomFactor (zf);
            }
        }

    }

    private class SceneZoomToFitAction extends AbstractAction {

        @Messages("ACT_ZoomToFit=Zoom To Fit")
        SceneZoomToFitAction() {
            putValue(NAME, Bundle.ACT_ZoomToFit());
        }

        @Override public void actionPerformed(ActionEvent e) {
            DependencyGraphScene.this.getFitToViewLayout().fitToView(null);
        }
    };

    private class HighlightedZoomToFitAction extends AbstractAction {

        HighlightedZoomToFitAction() {
            putValue(NAME, Bundle.ACT_ZoomToFit());
        }

        @Override public void actionPerformed(ActionEvent e) {
            Collection<GraphNode<I>> grNodes = DependencyGraphScene.this.getNodes();
            List<NodeWidget> aws = new ArrayList<NodeWidget>();
            NodeWidget aw = null;
            int paintState;
            for (GraphNode grNode : grNodes) {
                aw = (NodeWidget) findWidget(grNode);
                paintState = aw.getPaintState();
                if (paintState != EdgeWidget.DISABLED && paintState != EdgeWidget.GRAYED) {
                    aws.add(aw);
                }
            }
            DependencyGraphScene.this.getFitToViewLayout().fitToView(aws);
        }
    };

    private static class HoverController implements TwoStateHoverProvider {

        @Override public void unsetHovering(Widget widget) {
            NodeWidget aw = findArtifactW(widget);
            if (widget != null) {
                aw.bulbUnhovered();
            }
        }

        @Override public void setHovering(Widget widget) {
            NodeWidget aw = findArtifactW(widget);
            if (aw != null) {
                aw.bulbHovered();
            }
        }

        private NodeWidget findArtifactW (Widget w) {
            while (w != null && !(w instanceof NodeWidget)) {
                w = w.getParentWidget();
            }
            return (NodeWidget)w;
        }

    }
    
    private class HierarchicalGraphLayoutAction extends AbstractAction {

       @Messages("ACT_Layout_HierarchicalGraphLayout=Hierarchical")
       HierarchicalGraphLayoutAction() {
           putValue(NAME, Bundle.ACT_Layout_HierarchicalGraphLayout());
       }

       @Override public void actionPerformed(ActionEvent e) {
           final GraphLayout layout = GraphLayoutFactory.createHierarchicalGraphLayout(DependencyGraphScene.this, DependencyGraphScene.this.isAnimated(), false);
           layout.layoutGraph(DependencyGraphScene.this);
            fitToZoomAfterLayout();
        }
   };
   private class TreeGraphLayoutVerticalAction extends AbstractAction {

       @Messages("ACT_Layout_TreeGraphLayoutVertical=Vertical Tree")
       TreeGraphLayoutVerticalAction() {
           putValue(NAME, Bundle.ACT_Layout_TreeGraphLayoutVertical());
       }

       @Override public void actionPerformed(ActionEvent e) {
           final GraphLayout layout = GraphLayoutFactory.createTreeGraphLayout(10, 10, 50, 50, true);
           GraphLayoutSupport.setTreeGraphLayoutRootNode(layout, DependencyGraphScene.this.rootNode);
           
           layout.layoutGraph(DependencyGraphScene.this);
           fitToZoomAfterLayout();
       }
   };

   private class TreeGraphLayoutHorizontalAction extends AbstractAction {

       @Messages("ACT_Layout_TreeGraphLayoutHorizontal=Horizontal Tree")
       TreeGraphLayoutHorizontalAction() {
           putValue(NAME, Bundle.ACT_Layout_TreeGraphLayoutHorizontal());
       }

       @Override public void actionPerformed(ActionEvent e) {
           final GraphLayout layout = GraphLayoutFactory.createTreeGraphLayout(10, 10, 50, 50, false);
           GraphLayoutSupport.setTreeGraphLayoutRootNode(layout, DependencyGraphScene.this.rootNode);
           
           layout.layoutGraph(DependencyGraphScene.this);
           fitToZoomAfterLayout();
           
       }
   };
   private class FruchtermanReingoldLayoutAction extends AbstractAction {

       @Messages("ACT_Layout_FruchtermanReingoldLayout=Default Layout")
       FruchtermanReingoldLayoutAction() {
           putValue(NAME, Bundle.ACT_Layout_FruchtermanReingoldLayout());
       }

       @Override public void actionPerformed(ActionEvent e) {
           //default layout in 7.3 
           layout.invokeLayout();
           fitToZoomAfterLayout();
       }
   };

    void fitToZoomAfterLayout() {
            DependencyGraphScene.this.getSceneAnimator().getPreferredLocationAnimator().addAnimatorListener(new AnimatorListener() {
                
                @Override
                public void animatorStarted(AnimatorEvent event) {
                    
                }
                
                @Override
                public void animatorReset(AnimatorEvent event) {
                    
                }
                
                @Override
                public void animatorFinished(AnimatorEvent event) {
                    DependencyGraphScene.this.getSceneAnimator().getPreferredLocationAnimator().removeAnimatorListener(this);
                    new SceneZoomToFitAction().actionPerformed(null);
                }
                
                @Override
                public void animatorPreTick(AnimatorEvent event) {
                    
                }
                
                @Override
                public void animatorPostTick(AnimatorEvent event) {
                    
                }
            });
    }
}
