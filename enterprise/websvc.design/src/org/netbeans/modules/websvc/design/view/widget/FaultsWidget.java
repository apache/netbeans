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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.openide.util.NbBundle;

/**
 * @author Ajit Bhate
 */
public class FaultsWidget extends AbstractTitledWidget implements TabWidget {

    private MethodModel method;

    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    
    private transient TableModel model;
    private transient TableWidget faultTable;
    
    private transient Widget tabComponent;

    /** 
     * Creates a new instance of OperationWidget 
     * @param scene
     * @param method  
     */
    public FaultsWidget(ObjectScene scene, MethodModel method) {
        super(scene,0,RADIUS,0,BORDER_COLOR);
        this.method = method;
        createContent();
    }
    
    protected Paint getTitlePaint(Rectangle bounds) {
        return TITLE_COLOR_FAULT;
    }
    
    private void createContent() {
        model = new FaultsTableModel(method);
        populateContentWidget(getContentWidget());
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(0,1,1,1));
        headerLabelWidget = new ImageLabelWidget(getScene(), getIcon(), getTitle(), 
                "("+method.getFaults().size()+")");
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(new Widget(getScene()),5);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),4);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));

        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);
    }

    private void populateContentWidget(Widget parentWidget) {
        if(model.getRowCount()>0) {
            faultTable = new TableWidget(getScene(),model);
            parentWidget.addChild(faultTable);
        } else {
            LabelWidget noFaults = new LabelWidget(getScene(),
                    NbBundle.getMessage(OperationWidget.class, "LBL_FaultsNone"));
            noFaults.setAlignment(LabelWidget.Alignment.CENTER);
            parentWidget.addChild(noFaults);
        }
    }

    public Object hashKey() {
        return model;
    }

    public String getTitle() {
        return NbBundle.getMessage(OperationWidget.class, "LBL_Faults");
    }

    public Image getIcon() {
        return null;
//        return Utilities.loadImage
//            ("org/netbeans/modules/websvc/design/view/resources/fault.png"); // NOI18N
    }

    public Widget getComponentWidget() {
        if(tabComponent==null) {
            tabComponent = createContentWidget();
            populateContentWidget(tabComponent);
        }
        return tabComponent;
    }
}
