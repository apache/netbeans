/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.view.layout.TableLayout;

/**
 *
 * @author Ajit Bhate
 */
public class TableWidget extends Widget{
    
    private static final Color HEADER_COLOR =  new Color(217,235,255);
    private static final Color CELL_COLOR =  Color.WHITE;
    private static final Color BORDER_COLOR =  new Color(169, 197, 235);
    private static final Color SELECTED_BORDER_COLOR = new Color(255,153,0);
    private TableModel model;
    private static final int COLUMN_WIDTH = 100;
    
    /**
     * Creates a table widget for a tablemodel.
     * @param scene
     * @param model
     */
    public TableWidget(Scene scene, TableModel model) {
        super(scene);
        this.model = model;
        setLayout(LayoutFactory.createVerticalFlowLayout());
        createTableHeader();
        createTable();
    }
    
    private void createTableHeader() {
        Scene scene = getScene();
        int noCols = model.getColumnCount();
        Widget headerWidget = new RowWidget(scene,noCols,null);
        addChild(headerWidget);
        
        for (int i = 0; i<noCols;i++) {
            LabelWidget columnHeader = new LabelWidget(scene, model.getColumnName(i));
            if(i!=0) {
                columnHeader.setBorder(new LineBorder(0, 1, 0, 0, BORDER_COLOR));
            }
            columnHeader.setAlignment(LabelWidget.Alignment.CENTER);
            columnHeader.setBackground(HEADER_COLOR);
            columnHeader.setOpaque(true);
            headerWidget.addChild(columnHeader);
        }
    }
    
    private void createTable() {
        Scene scene = getScene();
        int noCols = model.getColumnCount();
        for(int j=0; j<model.getRowCount();j++) {
            Widget rowWidget = new RowWidget(scene,noCols,model.getUserObject(j));
            addChild(rowWidget);
            for (int i = 0; i<noCols;i++) {
                final int ii = i;
                final LabelWidget cellWidget = new LabelWidget(scene, model.getValueAt(j, i)) {
                    private Object key = new Object();
                    protected void notifyAdded() {
                        super.notifyAdded();
                        ObjectScene scene =(ObjectScene) getScene();
                        scene.addObject(key,this);
                    }
                    protected void notifyRemoved() {
                        super.notifyRemoved();
                        ObjectScene scene =(ObjectScene) getScene();
                        scene.removeObject(key);
                    }
                    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
                        if (previousState.isSelected() != state.isSelected() ||
                                previousState.isFocused() != state.isFocused()) {
                            setBorder(state.isSelected() ? state.isFocused()?
                                BorderFactory.createDashedBorder(SELECTED_BORDER_COLOR, 2, 2, true):
                                BorderFactory.createLineBorder(1,SELECTED_BORDER_COLOR) : 
                                state.isFocused() ? BorderFactory.createDashedBorder
                                (BORDER_COLOR, 2, 2, true):new LineBorder(0,ii!=0?1:0,0,0,BORDER_COLOR));
                            revalidate(true);
                        }
                    }
                };
                if(i!=0) {
                    cellWidget.setBorder(new LineBorder(0, 1, 0, 0, BORDER_COLOR));
                }
                cellWidget.setFont(getScene().getFont().deriveFont(Font.BOLD));
                cellWidget.setBackground(CELL_COLOR);
                cellWidget.setOpaque(true);
                cellWidget.setAlignment(LabelWidget.Alignment.CENTER);
                if(model.isCellEditable(j, i)) {
                    final int row = j;
                    final int column = i;
                    cellWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(
                            new TextFieldInplaceEditor(){
                        public boolean isEnabled(Widget widget) {
                            return true;
                        }
                        
                        public String getText(Widget widget) {
                            return model.getValueAt(row, column);
                        }
                        
                        public void setText(Widget widget, String text) {
                            model.setValueAt(text, row, column);
                            cellWidget.setLabel(text);
                        }
                    }));
                }
                rowWidget.addChild(cellWidget);
            }
        }
    }
    
    private static class RowWidget extends Widget {
        private Object userObject;
        RowWidget(Scene scene, int columns, Object userObject) {
            super(scene);
            setLayout(new TableLayout(columns, 0, 0,COLUMN_WIDTH));
            this.userObject = userObject;
            if(getScene() instanceof ObjectScene && userObject!=null) {
                getActions().addAction(((ObjectScene) getScene()).createSelectAction());
                setBorder(new LineBorder(1,0,0,0,BORDER_COLOR));
            }
        }
        
        protected void notifyAdded() {
            super.notifyAdded();
            if(getScene() instanceof ObjectScene && userObject!=null) {
                ObjectScene scene =(ObjectScene) getScene();
                List<Widget> widgets = scene.findWidgets(userObject);
                if(widgets==null|| widgets.isEmpty())
                    scene.addObject(userObject, this);
                else {
                    scene.removeObject(userObject);
                    widgets = new ArrayList<Widget>(widgets);
                    widgets.add(this);
                    scene.addObject(userObject, widgets.toArray(new Widget[0]));
                }
            }
        }
        protected void notifyRemoved() {
            super.notifyRemoved();
            if(getScene() instanceof ObjectScene && userObject!=null) {
                ObjectScene scene =(ObjectScene) getScene();
                List<Widget> widgets = scene.findWidgets(userObject);
                if(widgets!=null && widgets.contains(this)) {
                    if(widgets.size()==1) 
                        scene.removeObject(userObject);
                    else {
                        widgets = new ArrayList<Widget>(widgets);
                        widgets.remove(this);
                        scene.removeObject(userObject);
                        scene.addObject(userObject, widgets.toArray(new Widget[0]));
                    }
                }
            }
        }
        protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
            if (previousState.isSelected() != state.isSelected() ||
                    previousState.isFocused() != state.isFocused()) {
                setBorder(state.isSelected() ? state.isFocused()?
                    BorderFactory.createDashedBorder(SELECTED_BORDER_COLOR, 2, 2, true):
                    BorderFactory.createLineBorder(1,SELECTED_BORDER_COLOR) : 
                    state.isFocused() ? BorderFactory.createDashedBorder
                    (BORDER_COLOR, 2, 2, true):new LineBorder(1,0,0,0,BORDER_COLOR));
                revalidate(true);
            }
        }
        
    }
    private static class LineBorder implements Border {
        private Insets insets;
        private Color drawColor;
        public LineBorder(int top, int left, int bottom, int right, Color drawColor) {
            insets = new Insets(top,left,bottom,right);
            this.drawColor = drawColor;
        }

        public Insets getInsets() {
            return insets;
        }
        
        public void paint(Graphics2D gr, Rectangle bounds) {
            Paint oldPaint = gr.getPaint();
            gr.setPaint(drawColor);
            if(insets.top>0)
                gr.drawLine(bounds.x,bounds.y,bounds.x+bounds.width,bounds.y);
            if(insets.left>0)
                gr.drawLine(bounds.x,bounds.y,bounds.x,bounds.y+bounds.height);
            if(insets.bottom>0)
                gr.drawLine(bounds.x,bounds.y+bounds.height,bounds.x+bounds.width,bounds.y+bounds.height);
            if(insets.right>0)
                gr.drawLine(bounds.x+bounds.width,bounds.y,bounds.x+bounds.width,bounds.y+bounds.height);
            gr.setPaint(oldPaint);
        }
        
        public boolean isOpaque() {
            return true;
        }
        
    }
}
