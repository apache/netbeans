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
package  org.netbeans.modules.db.sql.visualeditor.querybuilder;

import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDFactory;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;

import javax.swing.*;
import java.awt.*;

import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.layout.LayoutFactory;

import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.router.Router;

/**
 * @author Jim Davidson
 */
public class QBGraphScene extends GraphScene {
    
    private LayerWidget 	mainLayer;
    private LayerWidget 	connectionLayer;
    
    private WidgetAction 	moveAction = ActionFactory.createMoveAction();
    private WidgetAction 	mouseHoverAction = ActionFactory.createHoverAction(new MyHoverProvider());
    // private WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction (new MyPopupProvider()) ;
    
    private int 		pos = 0;
    
    private Router 		router;
    
    public QBGraphScene(QueryBuilderGraphFrame qbGF) {
        qbGF._firstTableInserted = false;
        mainLayer = new LayerWidget(this);
        connectionLayer = new LayerWidget(this);
        addChild(mainLayer);
        addChild(connectionLayer);
        
        router = RouterFactory.createOrthogonalSearchRouter(mainLayer, connectionLayer);
        getActions().addAction(mouseHoverAction);
        // getActions().addAction(ActionFactory.createZoomAction());
        // ToDo: qbGF has been passed in only to support the menu; eventually, merge QBGF into this
        getActions().addAction(ActionFactory.createPopupMenuAction(qbGF));
    }
    
    protected Widget attachNodeWidget(Object node) {
        
        VMDNodeWidget widget = new VMDNodeWidget(this);
        // Widget widget = new Widget(this);
        
        widget.setLayout(LayoutFactory.createVerticalFlowLayout());
        widget.setBorder(VMDFactory.createVMDNodeBorder());
        widget.getActions().addAction(moveAction);
        
        ComponentWidget componentWidget = null;
        // LabelWidget label = null;
        
        if (node instanceof QBNodeComponent) // we have a qbNodeComponent
        {
            // label = new LabelWidget(this, ((QBNodeComponent)node).getNodeName());
            widget.setNodeName(((QBNodeComponent)node).getNodeName());
            widget.setNodeType("Table");
            componentWidget = new ComponentWidget(this, (QBNodeComponent)node);
            componentWidget.setBorder(VMDFactory.createVMDNodeBorder());
        }
        
        // 	label.setOpaque(true);
        // 	label.setBackground(Color.LIGHT_GRAY);
        // 	widget.addChild(label);
        
        widget.addChild(componentWidget);
        
        mainLayer.addChild(widget);
        return widget;
    }
    
    protected Widget attachEdgeWidget(Object edge) {
        
        VMDConnectionWidget connectionWidget = new VMDConnectionWidget(this, router);
        // ConnectionWidget connectionWidget = new ConnectionWidget (this);
        
        // connectionWidget.setRouter(router);
        connectionLayer.addChild(connectionWidget);
        return connectionWidget;
    }
    
    protected void attachEdgeSourceAnchor(Object edge, Object oldSourceNode, Object sourceNode) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(findWidget(sourceNode)));
    }
    
    protected void attachEdgeTargetAnchor(Object edge, Object oldTargetNode, Object targetNode) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(findWidget(targetNode)));
    }
    
    public LayerWidget getMainLayer() {
        return mainLayer;
    }
    
    public LayerWidget getConnectionLayer() {
        return connectionLayer;
    }
    
    // Create a node using the contents of the QueryBuilderTable
    public Widget addNode(String nodeName, QueryBuilderTableModel qbTableModel) {
        QBNodeComponent qbNC = new QBNodeComponent(nodeName, qbTableModel);
        return this.addNode(qbNC);
    }
    
    private static class MyHoverProvider implements TwoStateHoverProvider {
        
        public void unsetHovering(Widget widget) {
            widget.setBackground(Color.WHITE);
        }
        
        public void setHovering(Widget widget) {
            widget.setBackground(Color.CYAN);
        }
    }
    
    //     private static class MyPopupMenuProvider implements PopupMenuProvider {
    
    // 	public JPopupMenu getPopupMenu (Widget widget, Point localLocation) {
    // 	    JPopupMenu popupMenu = new JPopupMenu ();
    // 	    popupMenu.add (new JMenuItem ("Open " + ((UMLClassWidget) widget).getClassName ()));
    // 	    return popupMenu;
    // 	}
    //     }
    
}
