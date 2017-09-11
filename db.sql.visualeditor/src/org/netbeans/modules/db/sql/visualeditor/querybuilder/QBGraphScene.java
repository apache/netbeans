/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
