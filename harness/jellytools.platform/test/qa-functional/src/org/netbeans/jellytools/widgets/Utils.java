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
package org.netbeans.jellytools.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.ReconnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jiri Skrivanek
 */
public class Utils {

    /** Shows given Scene wrapped in TopComponent and JFrame.
     * @param scene Scene to be shown
     * @return TopComponent instance where scene resides
     */
    public static TopComponent showScene(Scene scene) {
        JComponent sceneView = scene.getView();
        if (sceneView == null) {
            sceneView = scene.createView();
        }
        int width = 450, height = 250;
        JFrame frame = new JFrame("Test Scene");
        TopComponent tc = new TopComponent();
        tc.setLayout(new BorderLayout());
        tc.add(sceneView);
        frame.add(tc);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);
        frame.setVisible(true);
        return tc;
    }

    /** Creates test scene with 2 LabelWidgets and one movable widget. */
    public static class TestScene extends GraphScene.StringGraph {

        private LayerWidget mainLayer = new LayerWidget(this);
        private LayerWidget connectionLayer = new LayerWidget(this);
        private LayerWidget interractionLayer = new LayerWidget(this);
        private WidgetAction createAction = new SceneCreateAction();
        private WidgetAction moveAction = ActionFactory.createMoveAction();
        private WidgetAction connectAction = ActionFactory.createConnectAction(interractionLayer, new SceneConnectProvider());
        private WidgetAction reconnectAction = ActionFactory.createReconnectAction(new SceneReconnectProvider());
        private long nodeCounter = 0;
        private long edgeCounter = 0;

        public TestScene() {
            addChild(mainLayer);
            addChild(connectionLayer);
            addChild(interractionLayer);
            getActions().addAction(createAction);
            //mainLayer.addChild(new LabelWidget(this, "Click on background to create a node. Drag a node to create a connection."));
            // do not change label nor location because it is hard coded in test cases
            //addNode("Label 0").setPreferredLocation(new Point(100, 100));
            Widget label0Widget = addNode("Label 0");
            label0Widget.setPreferredLocation(new Point(100, 100));
            label0Widget.getActions().addAction(ActionFactory.createPopupMenuAction(new MyPopupProvider()));
            label0Widget.getActions().addAction(new LabelAction());
            Widget label1Widget = addNode("Label 1");
            label1Widget.setPreferredLocation(new Point(300, 100));
            LabelWidget movableWidget = new LabelWidget(this, "Movable Widget");
            movableWidget.setPreferredLocation(new Point(100, 150));
            movableWidget.getActions().addAction(moveAction);
            addChild(movableWidget);
        }

        protected Widget attachNodeWidget(String node) {
            LabelWidget label = new LabelWidget(this, node);
            label.setBorder(BorderFactory.createLineBorder(4));
            label.getActions().addAction(createObjectHoverAction());
//        label.getActions ().addAction (createSelectAction ());
            label.getActions().addAction(connectAction);
            mainLayer.addChild(label);
            return label;
        }

        protected Widget attachEdgeWidget(String edge) {
            ConnectionWidget connection = new ConnectionWidget(this);
            connection.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
            connection.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
            connection.getActions().addAction(createObjectHoverAction());
            connection.getActions().addAction(createSelectAction());
            connection.getActions().addAction(reconnectAction);
            connectionLayer.addChild(connection);
            return connection;
        }

        protected void attachEdgeSourceAnchor(String edge, String oldSourceNode, String sourceNode) {
            Widget w = sourceNode != null ? findWidget(sourceNode) : null;
            ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(w));
        }

        protected void attachEdgeTargetAnchor(String edge, String oldTargetNode, String targetNode) {
            Widget w = targetNode != null ? findWidget(targetNode) : null;
            ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(w));
        }

        private class SceneCreateAction extends WidgetAction.Adapter {

            @Override
            public State mousePressed(Widget widget, WidgetMouseEvent event) {
                if (event.getClickCount() == 1) {
                    if (event.getButton() == MouseEvent.BUTTON1 || event.getButton() == MouseEvent.BUTTON2) {
                        addNode("node" + nodeCounter++).setPreferredLocation(widget.convertLocalToScene(event.getPoint()));
                        return State.CONSUMED;
                    }
                }
                return State.REJECTED;
            }
        }

        private class SceneConnectProvider implements ConnectProvider {

            private String source = null;
            private String target = null;

            public boolean isSourceWidget(Widget sourceWidget) {
                Object object = findObject(sourceWidget);
                source = isNode(object) ? (String) object : null;
                return source != null;
            }

            public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
                Object object = findObject(targetWidget);
                target = isNode(object) ? (String) object : null;
                if (target != null) {
                    return !source.equals(target) ? ConnectorState.ACCEPT : ConnectorState.REJECT_AND_STOP;
                }
                return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
            }

            public boolean hasCustomTargetWidgetResolver(Scene scene) {
                return false;
            }

            public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
                return null;
            }

            public void createConnection(Widget sourceWidget, Widget targetWidget) {
                String edge = "edge" + edgeCounter++;
                addEdge(edge);
                setEdgeSource(edge, source);
                setEdgeTarget(edge, target);
            }
        }

        private class SceneReconnectProvider implements ReconnectProvider {

            String edge;
            String originalNode;
            String replacementNode;

            public void reconnectingStarted(ConnectionWidget connectionWidget, boolean reconnectingSource) {
            }

            public void reconnectingFinished(ConnectionWidget connectionWidget, boolean reconnectingSource) {
            }

            public boolean isSourceReconnectable(ConnectionWidget connectionWidget) {
                Object object = findObject(connectionWidget);
                edge = isEdge(object) ? (String) object : null;
                originalNode = edge != null ? getEdgeSource(edge) : null;
                return originalNode != null;
            }

            public boolean isTargetReconnectable(ConnectionWidget connectionWidget) {
                Object object = findObject(connectionWidget);
                edge = isEdge(object) ? (String) object : null;
                originalNode = edge != null ? getEdgeTarget(edge) : null;
                return originalNode != null;
            }

            public ConnectorState isReplacementWidget(ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource) {
                Object object = findObject(replacementWidget);
                replacementNode = isNode(object) ? (String) object : null;
                if (replacementNode != null) {
                    return ConnectorState.ACCEPT;
                }
                return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
            }

            public boolean hasCustomReplacementWidgetResolver(Scene scene) {
                return false;
            }

            public Widget resolveReplacementWidget(Scene scene, Point sceneLocation) {
                return null;
            }

            public void reconnect(ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource) {
                if (replacementWidget == null) {
                    removeEdge(edge);
                } else if (reconnectingSource) {
                    setEdgeSource(edge, replacementNode);
                } else {
                    setEdgeTarget(edge, replacementNode);
                }
            }
        }

        private static class MyPopupProvider implements PopupMenuProvider, ActionListener {

            private static final String OPEN_ACTION = "openAction";
            private static final String MODAL_ACTION = "modalAction";
            private static final int WIDTH = 100;
            private static final int HEIGHT = 100;
            private JPopupMenu menu;

            public MyPopupProvider() {
                menu = new JPopupMenu("Popup menu");
                JMenuItem item;

                item = new JMenuItem("Open");
                item.setActionCommand(OPEN_ACTION);
                item.addActionListener(this);
                menu.add(item);

                item = new JMenuItem("Modal");
                item.setActionCommand(MODAL_ACTION);
                item.addActionListener(this);
                menu.add(item);

            }

            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                return menu;
            }

            public void actionPerformed(ActionEvent e) {
                if (OPEN_ACTION.equals(e.getActionCommand())) {
                    JDialog dialog = new JDialog((Frame) null, "Open");
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    dialog.setBounds((screenSize.width - WIDTH) / 2, (screenSize.height - HEIGHT) / 2, WIDTH, HEIGHT);
                    dialog.setVisible(true);
                } else if (MODAL_ACTION.equals(e.getActionCommand())) {
                    JDialog dialog = new JDialog((Frame) null, "Modal", true);
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    dialog.setBounds((screenSize.width - WIDTH) / 2, (screenSize.height - HEIGHT) / 2, WIDTH, HEIGHT);
                    dialog.setVisible(true);
                }
            }
        }

        private class LabelAction extends WidgetAction.Adapter {

            private static final int WIDTH = 100;
            private static final int HEIGHT = 100;

            @Override
            public State mouseClicked(Widget widget, WidgetMouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    JDialog dialog = new JDialog((Frame) null, "Mouse Clicked " + event.getClickCount());
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    dialog.setBounds((screenSize.width - WIDTH) / 2, (screenSize.height - HEIGHT) / 2, WIDTH, HEIGHT);
                    dialog.setVisible(true);
                    return State.CONSUMED;
                }
                return State.REJECTED;
            }
        }
    }
}
