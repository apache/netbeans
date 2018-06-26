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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
public class ParametersWidget extends AbstractTitledWidget implements TabWidget {
    
    private transient MethodModel method;
    private transient boolean nameEditable;

    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;

    private transient TableModel model;
    private transient TableWidget parameterTable;
    
    private transient Widget tabComponent;
    
    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param method 
     */
    public ParametersWidget(ObjectScene scene, MethodModel method, boolean nameEditable) {
        super(scene,0,RADIUS,0,BORDER_COLOR);
        this.method = method;
        this.nameEditable = nameEditable;
        createContent();
    }
    
    protected Paint getTitlePaint(Rectangle bounds) {
        return TITLE_COLOR_PARAMETER;
    }
    
    private void createContent() {
        model = new ParametersTableModel(method, nameEditable);
        populateContentWidget(getContentWidget());
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(0,1,1,1));
        headerLabelWidget = new ImageLabelWidget(getScene(), getIcon(), getTitle(), 
                "("+method.getParams().size()+")");
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(new Widget(getScene()),5);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),4);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));

        buttons.addChild(getExpanderWidget());
        buttons.setOpaque(true);
        buttons.setBackground(TITLE_COLOR_BRIGHT);

        getHeaderWidget().addChild(buttons);

    }

    private void populateContentWidget(Widget parentWidget) {
        if(model.getRowCount()>0) {
            parameterTable = new TableWidget(getScene(),model);
            parentWidget.addChild(parameterTable);
        } else {
            LabelWidget noParamsWidget = new LabelWidget(getScene(),
                    NbBundle.getMessage(OperationWidget.class, "LBL_InputNone"));
            noParamsWidget.setAlignment(LabelWidget.Alignment.CENTER);
            parentWidget.addChild(noParamsWidget);
        }
    }

    public Object hashKey() {
        return model;
    }
    
    public String getTitle() {
        return NbBundle.getMessage(OperationWidget.class, "LBL_Input");
    }

    public Image getIcon() {
        return null;
//        return Utilities.loadImage
//            ("org/netbeans/modules/websvc/design/view/resources/input.png");
    }

    public Widget getComponentWidget() {
        if(tabComponent==null) {
            tabComponent = createContentWidget();
            populateContentWidget(tabComponent);
        }
        return tabComponent;
    }
}
