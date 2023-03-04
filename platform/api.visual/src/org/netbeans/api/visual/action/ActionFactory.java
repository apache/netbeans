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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.modules.visual.action.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EnumSet;

/**
 * The factory class of all built-in actions. Action creation usually requires some parameter like decorator
 * (cares about the visualization) and provider (cares about the logic of an action).
 * <p>
 * Instances of the built-in actions could be shared by multiple widgets.
 *
 * @author David Kaspar
 */
public final class ActionFactory {

    private static final BasicStroke STROKE = new BasicStroke (1.0f, BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[] { 6.0f, 3.0f }, 0.0f);

    private static final MoveStrategy MOVE_STRATEGY_FREE = new MoveStrategy () {
        public Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation) {
            return suggestedLocation;
        }
    };

    private static final MoveProvider MOVE_PROVIDER_DEFAULT = new MoveProvider () {
        public void movementStarted (Widget widget) {
        }
        public void movementFinished (Widget widget) {
        }
        public Point getOriginalLocation (Widget widget) {
            return widget.getPreferredLocation ();
        }
        public void setNewLocation (Widget widget, Point location) {
            widget.setPreferredLocation (location);
        }
    };

    private static final AlignWithMoveDecorator ALIGN_WITH_MOVE_DECORATOR_DEFAULT = new AlignWithMoveDecorator() {
        public ConnectionWidget createLineWidget (Scene scene) {
            ConnectionWidget widget = new ConnectionWidget (scene);
            widget.setStroke (STROKE);
            widget.setForeground (Color.BLUE);
            return widget;
        }
    };

    private static final MoveControlPointProvider MOVE_CONTROL_POINT_PROVIDER_FREE = new FreeMoveControlPointProvider ();

    private static final MoveControlPointProvider MOVE_CONTROL_POINT_PROVIDER_ORTHOGONAL = new OrthogonalMoveControlPointProvider ();

    private static final ConnectDecorator CONNECT_DECORATOR_DEFAULT = new ConnectDecorator() {
        public ConnectionWidget createConnectionWidget (Scene scene) {
            ConnectionWidget widget = new ConnectionWidget (scene);
            widget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
            return widget;
        }
        public Anchor createSourceAnchor (Widget sourceWidget) {
            return AnchorFactory.createCenterAnchor (sourceWidget);
        }
        public Anchor createTargetAnchor (Widget targetWidget) {
            return AnchorFactory.createCenterAnchor (targetWidget);
        }
        public Anchor createFloatAnchor (Point location) {
            return AnchorFactory.createFixedAnchor (location);
        }
    };

    private static final ReconnectDecorator RECONNECT_DECORATOR_DEFAULT = new ReconnectDecorator () {
        public Anchor createReplacementWidgetAnchor (Widget replacementWidget) {
            return AnchorFactory.createCenterAnchor (replacementWidget);
        }
        public Anchor createFloatAnchor (Point location) {
            return AnchorFactory.createFixedAnchor (location);
        }
    };

    private static final ResizeProvider RESIZE_PROVIDER_DEFAULT = new ResizeProvider() {
        public void resizingStarted (Widget widget) {
        }
        public void resizingFinished (Widget widget) {
        }
    };

    private static final ResizeStrategy RESIZE_STRATEGY_FREE = new ResizeStrategy () {
        public Rectangle boundsSuggested (Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ResizeProvider.ControlPoint controlPoint) {
            return suggestedBounds;
        }
    };

    private static final ResizeControlPointResolver RESIZE_CONTROL_POINT_RESOLVER_DEFAULT = new ResizeCornersControlPointResolver ();

    private static final ActionMapAction ACTION_MAP_ACTION = new ActionMapAction (null, null);

    private static final WidgetAction MOVE_CONTROL_POINT_ACTION_FREE = createMoveControlPointAction (createFreeMoveControlPointProvider ());

    private static final WidgetAction MOVE_CONTROL_POINT_ACTION_ORTHOGONAL = createMoveControlPointAction (createOrthogonalMoveControlPointProvider ());

    private static final WidgetAction MOVE_ACTION = createMoveAction (null, null);

    private static final WidgetAction RESIZE_ACTION = createResizeAction (null, null);

    private static final WidgetAction CYCLE_FOCUS_OBJECT_SCENE = createCycleFocusAction (new CycleObjectSceneFocusProvider ());

    private static final PanAction PAN_ACTION = new PanAction ();

    private static final WheelPanAction WHEEL_PAN_ACTION = new WheelPanAction ();

    private ActionFactory () {
    }

    /**
     * Creates a accept action with a specified accept logic provider.
     * @param provider the accept logic provider
     * @return the accept action
     */
    public static WidgetAction createAcceptAction (AcceptProvider provider) {
        assert provider != null;
        return new AcceptAction (provider);
    }

    /**
     * Creates an action which handles keys and popup menu.
     * The key-to-action binding is obtained from the InputMap of a view JComponent of a scene.
     * The actions for popup menu are obtained from the ActionMap of a view JComponent of a scene.
     * @return the action-map action
     */
    public static WidgetAction createActionMapAction () {
        return ACTION_MAP_ACTION;
    }

    /**
     * Creates an action which handles keys and popup menu.
     * The key-to-action binding and popup menu items are obtained from specified ActionMap and InputMap.
     * @param inputMap the input map
     * @param actionMap the action map
     * @return the action-map action
     */
    public static WidgetAction createActionMapAction (InputMap inputMap, ActionMap actionMap) {
        assert inputMap != null  &&  actionMap != null;
        return new ActionMapAction (inputMap, actionMap);
    }

    /**
     * Creates a add-remove control point action with a default sensitivity. The action is assigned to a FreeConnectionWidget.
     * @return the add-remove control point action
     */
    public static WidgetAction createAddRemoveControlPointAction () {
        return createAddRemoveControlPointAction (3.0, 5.0);
    }

    /**
     * Creates a add-remove control point action with a specified sensitivity.
     * @param createSensitivity the create sensitivity
     * @param deleteSensitivity the delete sensitivity
     * @return the add-remove control point action
     */
    public static WidgetAction createAddRemoveControlPointAction (double createSensitivity, double deleteSensitivity) {
        return createAddRemoveControlPointAction (createSensitivity, deleteSensitivity, null);
    }

    /**
     * Creates a add-remove control point action with a specified sensitivity.
     * @param createSensitivity the create sensitivity
     * @param deleteSensitivity the delete sensitivity
     * @param routingPolicy the routing policy that is automatically set to a connection widget with control points modified by this action;
     *     if null, then routing policy is not set
     * @return the add-remove control point action
     * @since 2.9
     */
    public static WidgetAction createAddRemoveControlPointAction (double createSensitivity, double deleteSensitivity, ConnectionWidget.RoutingPolicy routingPolicy) {
        return new AddRemoveControlPointAction (createSensitivity, deleteSensitivity, routingPolicy);
    }

    /**
     * Creates a align-with move action.
     * @param collectionLayer the layer with objects that the alignment is checked against.
     * @param interractionLayer the interraction layer where the align-with hint lines are placed
     * @param decorator the align-with move decorator
     * @return the align-with move action
     */
    public static WidgetAction createAlignWithMoveAction (LayerWidget collectionLayer, LayerWidget interractionLayer, AlignWithMoveDecorator decorator) {
        return createAlignWithMoveAction (collectionLayer, interractionLayer, decorator, true);
    }

    /**
     * Creates a align-with move action.
     * @param collectionLayer the layer with objects that the alignment is checked against.
     * @param interractionLayer the interraction layer where the align-with hint lines are placed
     * @param decorator the align-with move decorator
     * @param outerBounds if true, then the align-with is check against whole bounds of widgets in collection layer;
     *      if false, then the align-with is check against client area (widget bounds without border insets
     * @return the align-with move action
     * @since 2.7
     */
    public static WidgetAction createAlignWithMoveAction (LayerWidget collectionLayer, LayerWidget interractionLayer, AlignWithMoveDecorator decorator, boolean outerBounds) {
        assert collectionLayer != null;
        return createAlignWithMoveAction (new SingleLayerAlignWithWidgetCollector (collectionLayer, outerBounds), interractionLayer, decorator != null ? decorator : ALIGN_WITH_MOVE_DECORATOR_DEFAULT, outerBounds);
    }

    /**
     * Creates a align-with move action.
     * @param collector the collector of objects that the alignment is checked against.
     * @param interractionLayer the interraction layer where the align-with hint lines are placed
     * @param decorator the align-with move decorator
     * @return the align-with move action
     */
    public static WidgetAction createAlignWithMoveAction (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator) {
        return createAlignWithMoveAction (collector, interractionLayer, decorator, true);
    }

    /**
     * Creates a align-with move action.
     * @param collector the collector of objects that the alignment is checked against.
     * @param interractionLayer the interraction layer where the align-with hint lines are placed
     * @param decorator the align-with move decorator
     * @param outerBounds if true, then the align-with is check against whole bounds of widgets in collection layer;
     *      if false, then the align-with is check against client area (widget bounds without border insets
     * @return the align-with move action
     * @since 2.7
     */
    public static WidgetAction createAlignWithMoveAction (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator, boolean outerBounds) {
        assert collector != null  &&  interractionLayer != null  &&  decorator != null;
        AlignWithMoveStrategyProvider sp = new AlignWithMoveStrategyProvider (collector, interractionLayer, decorator, outerBounds);
        return createMoveAction (sp, sp);
    }

    /**
     * Creates a align-with resize action.
     * @param collectionLayer the layer with objects that the alignment is checked against.
     * @param interractionLayer the interraction layer where the align-with hint lines are placed
     * @param decorator the align-with move decorator
     * @return the align-with resize action
     */
    public static WidgetAction createAlignWithResizeAction (LayerWidget collectionLayer, LayerWidget interractionLayer, AlignWithMoveDecorator decorator) {
        return createAlignWithResizeAction (collectionLayer, interractionLayer, decorator, true);
    }

    /**
     * Creates a align-with resize action.
     * @param collectionLayer the layer with objects that the alignment is checked against.
     * @param interractionLayer the interraction layer where the align-with hint lines are placed
     * @param decorator the align-with move decorator
     * @param outerBounds if true, then the align-with is check against whole bounds of widgets in collection layer;
     *      if false, then the align-with is check against client area (widget bounds without border insets
     * @return the align-with resize action
     * @since 2.7
     */
    public static WidgetAction createAlignWithResizeAction (LayerWidget collectionLayer, LayerWidget interractionLayer, AlignWithMoveDecorator decorator, boolean outerBounds) {
        assert collectionLayer != null;
        return createAlignWithResizeAction (new SingleLayerAlignWithWidgetCollector (collectionLayer, outerBounds), interractionLayer, decorator != null ? decorator : ALIGN_WITH_MOVE_DECORATOR_DEFAULT, outerBounds);
    }

    /**
     * Creates a align-with resize action.
     * @param collector the collector of objects that the alignment is checked against.
     * @param interractionLayer the interraction layer where the align-with hint lines are placed
     * @param decorator the align-with move decorator
     * @return the align-with resize action
     */
    public static WidgetAction createAlignWithResizeAction (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator) {
        return createAlignWithResizeAction (collector, interractionLayer, decorator, true);
    }

    /**
     * Creates a align-with resize action.
     * @param collector the collector of objects that the alignment is checked against.
     * @param interractionLayer the interraction layer where the align-with hint lines are placed
     * @param decorator the align-with move decorator
     * @param outerBounds if true, then the align-with is check against whole bounds of widgets in collection layer;
     *      if false, then the align-with is check against client area (widget bounds without border insets
     * @return the align-with resize action
     * @since 2.7
     */
    public static WidgetAction createAlignWithResizeAction (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator, boolean outerBounds) {
        assert collector != null && interractionLayer != null && decorator != null;
        AlignWithResizeStrategyProvider sp = new AlignWithResizeStrategyProvider (collector, interractionLayer, decorator, outerBounds);
        return createResizeAction (sp, sp);
    }

    /**
     * Creates a connect action with a default decorator.
     * @param interractionLayer the interraction layer where the temporary connection is visualization placed.
     * @param provider the connect logic provider
     * @return the connect action
     */
    public static WidgetAction createConnectAction (LayerWidget interractionLayer, ConnectProvider provider) {
        return createConnectAction (null, interractionLayer, provider);
    }

    /**
     * Creates a connect action with a specific decorator.
     * @param decorator the connect decorator; if null, then the default decorator is used
     * @param interractionLayer the interraction layer where the temporary connection is visualization placed.
     * @param provider the connect logic provider
     * @return the connect action
     */
    public static WidgetAction createConnectAction (ConnectDecorator decorator, LayerWidget interractionLayer, ConnectProvider provider) {
        assert interractionLayer != null  &&  provider != null;
        return new ConnectAction (decorator != null ? decorator : createDefaultConnectDecorator (), interractionLayer, provider);
    }

    /**
     * Creates an extended connect action with a default decorator. User can invoke the action only with pressed CTRL key.
     * @param interractionLayer the interraction layer where the temporary connection is visualization placed.
     * @param provider the connect logic provider
     * @return the extended connect action
     */
    public static WidgetAction createExtendedConnectAction (LayerWidget interractionLayer, ConnectProvider provider) {
        return createExtendedConnectAction (null, interractionLayer, provider);
    }

    /**
     * Creates an extended connect action with a specific decorator. User can invoke the action only with pressed CTRL key.
     * @param decorator the connect decorator; if null, then the default decorator is used
     * @param interractionLayer the interraction layer where the temporary connection is visualization placed.
     * @param provider the connect logic provider
     * @return the extended connect action
     */
    public static WidgetAction createExtendedConnectAction (ConnectDecorator decorator, LayerWidget interractionLayer, ConnectProvider provider) {
        assert interractionLayer != null  &&  provider != null;
        return new ExtendedConnectAction (decorator != null ? decorator : createDefaultConnectDecorator (), interractionLayer, provider, MouseEvent.CTRL_MASK);
    }

    /**
     * Creates an extended connect action with a specific decorator which can be invoked only with specified modifiers (usually it is <code>MouseEvent.CTRL_MASK</code>).
     * @param decorator the connect decorator; if null, then the default decorator is used
     * @param interractionLayer the interraction layer where the temporary connection is visualization placed.
     * @param provider the connect logic provider
     * @param modifiers the invocation modifiers
     * @return the extended connect action
     * @since 2.3
     */
    public static WidgetAction createExtendedConnectAction (ConnectDecorator decorator, LayerWidget interractionLayer, ConnectProvider provider, int modifiers) {
        assert interractionLayer != null  &&  provider != null  &&  modifiers != 0;
        return new ExtendedConnectAction (decorator != null ? decorator : createDefaultConnectDecorator (), interractionLayer, provider, modifiers);
    }

    /**
     * Creates an edit action.
     * @param provider the edit logic provider.
     * @return the edit action
     */
    public static WidgetAction createEditAction (EditProvider provider) {
        assert provider != null;
        return new EditAction (provider);
    }

    /**
     * Creates a hover action using a hover provider.
     * Usually the Scene.createWidgetHoverAction and ObjectScene.createObjectHoverAction methods are used instead of this method.
     * @param provider the hover logic provider
     * @return the hover action
     */
    public static WidgetAction createHoverAction (HoverProvider provider) {
        assert provider != null;
        return new MouseHoverAction (provider);
    }

    /**
     * Creates a hover action using a two-stated hover provider.
     * Usually the Scene.createWidgetHoverAction and ObjectScene.createObjectHoverAction methods are used instead of this method.
     * @param provider the two-stated hover logic provider
     * @return the hover action.
     */
    public static WidgetAction createHoverAction  (TwoStateHoverProvider provider) {
        assert provider != null;
        return new TwoStatedMouseHoverAction (provider);
    }

    /**
     * Creates a text in-place editor action visualized using JTextField.
     * @param editor the editor logic
     * @return the in-place editor action
     */
    public static WidgetAction createInplaceEditorAction (TextFieldInplaceEditor editor) {
        return createInplaceEditorAction (editor, null);
    }

    /**
     * Creates a text in-place editor action visualized using JTextField.
     * @param editor the editor logic
     * @param expansionDirections the expansion directions
     * @return the in-place editor action
     */
    public static WidgetAction createInplaceEditorAction (TextFieldInplaceEditor editor, EnumSet<InplaceEditorProvider.ExpansionDirection> expansionDirections) {
        return createInplaceEditorAction (new TextFieldInplaceEditorProvider (editor, expansionDirections));
    }

    /**
     * Creates an in-place editor action for a specific provider.
     * @param provider the in-place editor provider
     * @return the in-place editor action
     */
    public static <C extends JComponent> WidgetAction createInplaceEditorAction (InplaceEditorProvider<C> provider) {
        return new InplaceEditorAction<C> (provider);
    }

    /**
     * Creates a move action with a default (free) strategy. The action provides movement for a widget where it is assigned.
     * @return the move action
     */
    public static WidgetAction createMoveAction () {
        return MOVE_ACTION;
    }

    /**
     * Creates a move action with a specified strategy and provider.
     * @param strategy the move strategy; if null, then default (free) move strategy is used.
     * @param provider the move logic provider; if null, then defaual move logic provider is used
     *            (provides movement for a widget where it is assigned).
     * @return the move action
     */
    public static WidgetAction createMoveAction (MoveStrategy strategy, MoveProvider provider) {
        return new MoveAction (strategy != null ? strategy : createFreeMoveStrategy (), provider != null ? provider : createDefaultMoveProvider ());
    }

    /**
     * Creates a move control point (of a connection widget) action with no movement restriction.
     * @return the move control point action
     */
    public static WidgetAction createFreeMoveControlPointAction () {
        return MOVE_CONTROL_POINT_ACTION_FREE;
    }

    /**
     * Creates a move control point (of a connection widget) action with is used at ConnectionWidget with OrthogonalSearchRouter.
     * @return the move control point action
     */
    public static WidgetAction createOrthogonalMoveControlPointAction () {
        return MOVE_CONTROL_POINT_ACTION_ORTHOGONAL;
    }

    /**
     * Creates a move control point (of a connection widget) action with a specified provider.
     * @param provider the move control point provider
     * @return the move control point action
     */
    public static WidgetAction createMoveControlPointAction (MoveControlPointProvider provider) {
        return createMoveControlPointAction (provider, null);
    }

    /**
     * Creates a move control point (of a connection widget) action with a specified provider.
     * @param provider the move control point provider
     * @param routingPolicy the routing policy that is automatically set to a connection widget with control points modified by this action;
     *     if null, then routing policy is not set
     * @return the move control point action
     * @since 2.9
     */
    public static WidgetAction createMoveControlPointAction (MoveControlPointProvider provider, ConnectionWidget.RoutingPolicy routingPolicy) {
        assert provider != null;
        return new MoveControlPointAction (provider, routingPolicy);
    }

    /**
     * Creates a scene view panning action.
     * @return the pan action
     */
    public static WidgetAction createPanAction () {
        return PAN_ACTION;
    }

    /**
     * Creates a scene view panning action using mouse-wheel.
     * @return the wheel pan action
     * @since 2.7
     */
    public static WidgetAction createWheelPanAction () {
        return WHEEL_PAN_ACTION;
    }

    /**
     * Creates a popup menu action with a speicied provider.
     * @param provider the popup menu provider
     * @return the popup menu action
     */
    public static WidgetAction createPopupMenuAction (final PopupMenuProvider provider) {
        assert provider != null;
        return new PopupMenuAction (provider);
    }

    /**
     * Creates a reconnect action with a default decorator.
     * @param provider the reconnect logic provider
     * @return the reconnect action
     */
    public static WidgetAction createReconnectAction (ReconnectProvider provider) {
        return createReconnectAction (null, provider);
    }

    /**
     * Creates a reconnect action with a specific decorator and logic provider.
     * @param decorator the reccont decorator
     * @param provider the reconnect logic provider
     * @return the reconnect action
     */
    public static WidgetAction createReconnectAction (ReconnectDecorator decorator, ReconnectProvider provider) {
        return new ReconnectAction (decorator != null ? decorator : createDefaultReconnectDecorator (), provider);
    }

    /**
     * Creates a rectangular select action for a specified object scene with a default decorator.
     * @param scene the object scene which the selection will be controlled by the action
     * @param interractionLayer the interraction layer where the selection rectangle will be visualized
     * @return the rectangular select action
     */
    public static WidgetAction createRectangularSelectAction (ObjectScene scene, LayerWidget interractionLayer) {
        assert scene != null;
        return createRectangularSelectAction (ActionFactory.createDefaultRectangularSelectDecorator (scene), interractionLayer, ActionFactory.createObjectSceneRectangularSelectProvider (scene));
    }

    /**
     * Creates a rectangular select action with a specified decorator and logic provider.
     * @param decorator the rectangular select decorator
     * @param interractionLayer the interraction layer where the selection rectangle will be visualized
     * @param provider the rectangular select logic provider
     * @return the rectangular select action
     */
    public static WidgetAction createRectangularSelectAction (RectangularSelectDecorator decorator, LayerWidget interractionLayer, RectangularSelectProvider provider) {
        assert decorator != null  &&  interractionLayer != null  &&  provider != null;
        return new RectangularSelectAction (decorator, interractionLayer, provider);
    }

    /**
     * Creates a resize action with a default (free without any restriction) strategy and default logic provider (the action affect preferredBounds of a widget where it is assigned)
     * default resize control point resolver.
     * @return the resize action
     */
    public static WidgetAction createResizeAction () {
        return RESIZE_ACTION;
    }

    /**
     * Creates a resize action with a specified resize strategy and provider and default resize control point resolver.
     * @param strategy the resize strategy; if null, then the default (free without any restriction) strategy is used
     * @param provider the resize logic provider; if null, then the default logic provider is used
     *            (the action affect preferredBounds of a widget where it is assigned)
     * @return the resize action
     */
    public static WidgetAction createResizeAction (ResizeStrategy strategy, ResizeProvider provider) {
        return createResizeAction (strategy, null, provider);
    }

    /**
     * Creates a resize action with a specified resize strategy and provider.
     * @param strategy the resize strategy; if null, then the default (free without any restriction) strategy is used
     * @param resolver the resize control point resolver; if null, then the default (points are at corners and center of edges) is used
     * @param provider the resize logic provider; if null, then the default logic provider is used
     *            (the action affect preferredBounds of a widget where it is assigned)
     * @return the resize action
     */
    public static WidgetAction createResizeAction (ResizeStrategy strategy, ResizeControlPointResolver resolver, ResizeProvider provider) {
        return new ResizeAction (strategy != null ? strategy : createFreeResizeStategy (), resolver != null ? resolver : createDefaultResizeControlPointResolver (), provider != null ? provider : createDefaultResizeProvider ());
    }

     /**
     * Creates a select action. Usually the ObjectScene.createSelectAction method is used instead of this method.
     * @param provider the select logic provider
     * @param selectOnRightClick whether or not to first select the underlying widget with a right-click.
     * @return the select action
     */
    public static WidgetAction createSelectAction (SelectProvider provider, boolean selectOnRightClick) {
        assert provider != null;
        return new SelectAction (provider, selectOnRightClick);
    }
    
    /**
     * Creates a select action. Usually the ObjectScene.createSelectAction method is used instead of this method.
     * @param provider the select logic provider
     * @return the select action
     */
    public static WidgetAction createSelectAction (SelectProvider provider) {
        assert provider != null;
        return new SelectAction (provider);
    }

    /**
     * Creates a contiguous select action. Trigger by any left, middle or right mouse-button.
     * If no key-modifier is held, then it behaves as a non-contiguous replace selection.
     * If Shift key-modifier is held, then it behaves as a contiguous replace selection.
     * If Ctrl key-modifier is held, then it behaves as a non-contiguous additive selection.
     * If Ctrl and Shift key-modifiers are held, then it behaves as a contiguous additive selection.
     * @param provider the contiguous select logic provider
     * @return the contiguous select action
     * @since 2.17
     */
    public static WidgetAction createContiguousSelectAction (ContiguousSelectProvider provider) {
        assert provider != null;
        return new ContiguousSelectAction (provider);
    }

    /**
     * Creates a switch card action with controls an active card of a widget where a card layout is used.
     * @param cardLayoutWidget the widget where a card layout is used
     * @return the switch card action
     */
    public static WidgetAction createSwitchCardAction (Widget cardLayoutWidget) {
        assert cardLayoutWidget != null;
        return new SelectAction (new SwitchCardProvider (cardLayoutWidget));
    }

    /**
     * Creates a action that controls a zoom factor of a scene where the action is assigned.
     * @return the zoom action
     */
    public static WidgetAction createZoomAction () {
        return createZoomAction (1.2, true);
    }

    /**
     * Creates a action that controls a zoom factor of a scene where the action is assigned.
     * @param zoomMultiplier the zoom multiplier of each zoom step
     * @param animated if true, then the zoom factor changed is animated
     * @return the zoom action
     */
    public static WidgetAction createZoomAction (double zoomMultiplier, boolean animated) {
        return new ZoomAction (zoomMultiplier, animated);
    }

    /**
     * Creates a free (without any restriction) move strategy
     * @return the move strategy
     */
    public static MoveStrategy createFreeMoveStrategy () {
        return MOVE_STRATEGY_FREE;
    }

    /**
     * Creates a snap-to-grid move strategy.
     * @param horizontalGridSize the horizontal grid size
     * @param verticalGridSize the vertical grid size
     * @return the move strategy
     */
    public static MoveStrategy createSnapToGridMoveStrategy (int horizontalGridSize, int verticalGridSize) {
        assert horizontalGridSize > 0  &&  verticalGridSize > 0;
        return new SnapToGridMoveStrategy (horizontalGridSize, verticalGridSize);
    }

    /**
     * Creates a default move provider where the logic controls the preferredLocation of a widget where the action is assigned to.
     * @return the move provider
     */
    public static MoveProvider createDefaultMoveProvider () {
        return MOVE_PROVIDER_DEFAULT;
    }

    /**
     * Creates a default align-with move decorator.
     * @return the move decorator
     */
    public static AlignWithMoveDecorator createDefaultAlignWithMoveDecorator () {
        return ALIGN_WITH_MOVE_DECORATOR_DEFAULT;
    }

    /**
     * Creates a free (without any restriction) move control point (of a ConnectionWidget) provider.
     * @return the move control point action
     */
    public static MoveControlPointProvider createFreeMoveControlPointProvider () {
        return MOVE_CONTROL_POINT_PROVIDER_FREE;
    }

    /**
     * Creates a orthogonal move control point provider which is usually used with ConnectionWidget with OrthogonalSearchRouter.
     * @return the move control point provider
     */
    public static MoveControlPointProvider createOrthogonalMoveControlPointProvider () {
        return MOVE_CONTROL_POINT_PROVIDER_ORTHOGONAL;
    }

    /**
     * Creates a default rectangular select decorator.
     * @param scene the scene where an action is used
     * @return the rectangular select decorator
     */
    public static RectangularSelectDecorator createDefaultRectangularSelectDecorator (Scene scene) {
        assert scene != null;
        return new DefaultRectangularSelectDecorator (scene);
    }

    /**
     * Creates a rectangular select provider which controls a selection of an object scene.
     * @param scene the object scene where an action is used
     * @return the rectangular select provider
     */
    public static RectangularSelectProvider createObjectSceneRectangularSelectProvider (ObjectScene scene) {
        assert scene != null;
        return new ObjectSceneRectangularSelectProvider (scene);
    }

    /**
     * Creates a default connect decorator
     * @return the connect decorator
     */
    public static ConnectDecorator createDefaultConnectDecorator () {
        return CONNECT_DECORATOR_DEFAULT;
    }

    /**
     * Creates a default reconnect decorator
     * @return the reconnect decorator
     */
    public static ReconnectDecorator createDefaultReconnectDecorator () {
        return RECONNECT_DECORATOR_DEFAULT;
    }

    /**
     * Creates a free (without any restriction) resize strategy
     * @return the resize strategy
     */
    public static ResizeStrategy createFreeResizeStategy () {
        return RESIZE_STRATEGY_FREE;
    }

    /**
     * Creates a default resize provider which controls preferredBounds of a widget where an action is assigned.
     * @return the resize provider
     */
    public static ResizeProvider createDefaultResizeProvider () {
        return RESIZE_PROVIDER_DEFAULT;
    }

    /**
     * Creates a default resize control point resolver which is used in resize action.
     * @return the resize control point resolver
     */
    public static ResizeControlPointResolver createDefaultResizeControlPointResolver () {
        return RESIZE_CONTROL_POINT_RESOLVER_DEFAULT;
    }

    /**
     *
     * Creates a cycle focus action which switches focused object on a object scene.
     * @return the cycle object scene focus action
     */
    public static WidgetAction createCycleObjectSceneFocusAction () {
        return CYCLE_FOCUS_OBJECT_SCENE;
    }

    /**
     * Creates a cycle focus action.
     * @param provider the cycle focus provider
     * @return the cycle focus action
     */
    public static WidgetAction createCycleFocusAction (CycleFocusProvider provider) {
        assert provider != null;
        return new CycleFocusAction (provider);
    }

    /**
     * This action is used for forwarding key events to another widget.
     * Usually it could be used to forwarding a key event from a node widget to node-label widget when
     * a scene is using process-focused-widget-and-its-parents event processing type.
     * @param forwardToWidget the widget to which events are forwarded
     * @param forwardToTool the tool to which events are forwarded; if null, then default action chain is used
     * @return the forward key events action; assign this action to widget from which the forwarding should be done
     */
    public static WidgetAction createForwardKeyEventsAction (Widget forwardToWidget, String forwardToTool) {
        assert forwardToWidget != null;
        return new ForwardKeyEventsAction (forwardToWidget, forwardToTool);
    }

    /**
     * Returns an editor controller for a specified inplace-editor-action created by <code>ActionFactory.createInplaceEditorAction</code> method.
     * @param inplaceEditorAction the inplace-editor action
     * @return the editor controller
     */
    public static InplaceEditorProvider.EditorController getInplaceEditorController (WidgetAction inplaceEditorAction) {
        return (InplaceEditorProvider.EditorController) inplaceEditorAction;
    }

    /**
     * Creates a action that controls a zoom factor of a scene where the action is assigned.
     * During zooming the view will be centered still.  
     * @param zoomMultiplier the zoom multiplier
     * @return the zoom action
     */
    public static WidgetAction createCenteredZoomAction (double zoomMultiplier) {
        return new CenteredZoomAction (zoomMultiplier);
    }

    /**
     * Creates a action that controls a zoom factor of a scene where the action is assigned.
     * During zooming the view will be centered to the mouse cursor.
     * @param zoomMultiplier the zoom multiplier
     * @return the zoom action
     * @since 2.3
     */
    public static WidgetAction createMouseCenteredZoomAction (double zoomMultiplier) {
        return new MouseCenteredZoomAction (zoomMultiplier);
    }

}
