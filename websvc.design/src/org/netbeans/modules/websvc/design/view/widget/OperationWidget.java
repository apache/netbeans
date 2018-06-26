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

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ProjectService;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.design.view.DesignView;
import org.netbeans.modules.websvc.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.design.view.Flushable;
import org.netbeans.modules.websvc.design.view.actions.GotoSourceAction;
import org.netbeans.modules.websvc.design.view.actions.RemoveOperationAction;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class OperationWidget extends AbstractTitledWidget implements Flushable {
    
    private static final String IMAGE_ONE_WAY  = 
            "org/netbeans/modules/websvc/design/view/resources/oneway_operation.png"; // NOI18N   
    private static final String IMAGE_REQUEST_RESPONSE  = 
            "org/netbeans/modules/websvc/design/view/resources/requestresponse_operation.png"; // NOI18N   
    private static final String IMAGE_NOTIFICATION  = 
            "org/netbeans/modules/websvc/design/view/resources/notification_operation.png"; // NOI18N   

    private ProjectService service;
    private MethodModel operation;
    private ServiceModel serviceModel;
    
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;

    private transient TabbedPaneWidget tabbedWidget;
    private transient Widget listWidget;
    private transient ButtonWidget viewButton;
    
    private transient RemoveOperationAction removeAction;

    private ParametersWidget inputWidget;
    private OutputWidget outputWidget;
    private FaultsWidget faultWidget;
    private DescriptionWidget descriptionWidget;
    
    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param operation
     */
    public OperationWidget(ObjectScene scene, ServiceModel serviceModel, 
            ProjectService service, MethodModel operation) 
    {
        super(scene,RADIUS,RADIUS,RADIUS/2,BORDER_COLOR);
        this.service = service;
        this.operation=operation;
        this.serviceModel = serviceModel;
        
        removeAction = new RemoveOperationAction(service);
        removeAction.setWorkingSet(Collections.singleton(operation));
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
            new GotoSourceAction(operation, serviceModel.getImplementationClass()),
            removeAction
        })));
        createContent();
    }
    
    /**
     * Obtain the underlying MethodModel
     */
     public MethodModel getMethodModel(){
         return operation;
     }
     
     /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.design.view.Flushable#flushContent()
     */
    @Override
    public void flushContent() {
        if ( descriptionWidget != null ){
            descriptionWidget.flushContent();
        }
    }
    
    private void createContent() {
        String typeOfOperation ="";
        Image image = null;
        if(operation.isOneWay()) {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_OneWay");  //NOI18N
            image = ImageUtilities.loadImage(IMAGE_ONE_WAY);
        } else if (!operation.getParams().isEmpty()) {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_RequestResponse"); //NOI18N
            image = ImageUtilities.loadImage(IMAGE_REQUEST_RESPONSE);
        } else {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_Notification"); //NOI18N
            image = ImageUtilities.loadImage(IMAGE_NOTIFICATION);
        }
        headerLabelWidget = new ImageLabelWidget(getScene(), image, operation.getOperationName()) {
            private Object key = new Object();
            protected void notifyAdded() {
                super.notifyAdded();
                getObjectScene().addObject(key,headerLabelWidget);
            }
            protected void notifyRemoved() {
                super.notifyRemoved();
                getObjectScene().removeObject(key);
            }
        };
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        headerLabelWidget.setLabelEditor(new TextFieldInplaceEditor(){
            public boolean isEnabled(Widget widget) {
                return isNameEditable();
            }
            
            public String getText(Widget widget) {
                return headerLabelWidget.getLabel();
            }
            
            public void setText(Widget widget, String text) {
                operation.setOperationName(text);
                headerLabelWidget.setLabel(text);
            }
        });
        headerLabelWidget.setToolTipText(typeOfOperation);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),1);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));
        viewButton = new ButtonWidget(getScene(),null,null);
        viewButton.setImage(new TabImageWidget(getScene(),16));
        viewButton.setSelectedImage(new ListImageWidget(getScene(),16));
        viewButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                setTabbedView(!viewButton.isSelected());
            }
        });
        buttons.addChild(viewButton);

        for(final SampleMessageWidget.Type type:SampleMessageWidget.Type.values()) {
            final ButtonWidget sampleButton = new ButtonWidget(getScene(), null, null);
            sampleButton.setImage(type.getIcon(getScene()));
            sampleButton.setAction(new AbstractAction() {
                SampleMessageWidget messageWidget;
                public void actionPerformed(ActionEvent arg0) {
                    Widget messageLayer = getObjectScene().findWidget(DesignView.messageLayerKey);
                    if(messageWidget != null && messageLayer.getChildren().contains(messageWidget)) {
                        messageLayer.removeChild(messageWidget);
                        messageWidget = null;
                    } else {
                        messageLayer.removeChildren();
                        messageWidget = new SampleMessageWidget(
                                getObjectScene(), operation, type) {
                            protected void notifyAdded() {
                                super.notifyAdded();
                                sampleButton.setOpaque(true);
                                sampleButton.setSelected(true);
                            }
                            protected void notifyRemoved() {
                                super.notifyRemoved();
                                sampleButton.setOpaque(false);
                                sampleButton.setSelected(false);
                            }
                        };
                        messageLayer.addChild(messageWidget);
                    }
                }
            });
            sampleButton.setToolTipText(type.getDescription());
            buttons.addChild(sampleButton);
        }
        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);

        listWidget = new Widget(getScene());
        listWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, RADIUS/2));
        inputWidget = new ParametersWidget(getObjectScene(), operation, isNameEditable());
        outputWidget = new OutputWidget(getObjectScene(),operation);
        faultWidget = new FaultsWidget(getObjectScene(),operation);
        descriptionWidget = new DescriptionWidget(getObjectScene(),operation);
        listWidget.addChild(inputWidget);
        listWidget.addChild(outputWidget);
        listWidget.addChild(faultWidget);
        listWidget.addChild(descriptionWidget);

        tabbedWidget = new TabbedPaneWidget(getScene());
        tabbedWidget.addTab(inputWidget);
        tabbedWidget.addTab(outputWidget);
        tabbedWidget.addTab(faultWidget);
        tabbedWidget.addTab(descriptionWidget);
        
        setTabbedView(!viewButton.isSelected());
    }

    protected void collapseWidget() {
        if(buttons!=null && buttons.getParentWidget()!=null) {
            getHeaderWidget().revalidate(true);
            buttons.removeChild(getExpanderWidget());
            getHeaderWidget().removeChild(buttons);
            getHeaderWidget().addChild(getExpanderWidget());
        }
        super.collapseWidget();
        // set this operation as selected and focused
        if(hashKey()!=null) {
            getObjectScene().setSelectedObjects(Collections.singleton(hashKey()));
            getObjectScene().setFocusedObject(hashKey());
        }
    }

    protected void expandWidget() {
        if(buttons!=null && buttons.getParentWidget()==null) {
            getHeaderWidget().revalidate(true);
            getHeaderWidget().removeChild(getExpanderWidget());
            buttons.addChild(getExpanderWidget());
            getHeaderWidget().addChild(buttons);
        }
        super.expandWidget();
        // set this operation as selected and focused
        if(hashKey()!=null) {
            getObjectScene().setSelectedObjects(Collections.singleton(hashKey()));
            getObjectScene().setFocusedObject(hashKey());
        }
    }

    public Object hashKey() {
        return operation;
    }

    private void setTabbedView(boolean tabbedView) {
        if(viewButton.isSelected()!=tabbedView) {
            viewButton.setSelected(tabbedView);
            if(tabbedView) {
                if(listWidget.getParentWidget()==getContentWidget())
                    getContentWidget().removeChild(listWidget);
                getContentWidget().addChild(tabbedWidget);
            } else {
                if(tabbedWidget.getParentWidget()==getContentWidget())
                    getContentWidget().removeChild(tabbedWidget);
                getContentWidget().addChild(listWidget);
            }
        }
    }

    private static class ListImageWidget extends ImageLabelWidget.PaintableImageWidget {

        public ListImageWidget(Scene scene, int size) {
            super(scene, Color.LIGHT_GRAY, size, size);
            setBorder(BorderFactory.createLineBorder(0, Color.LIGHT_GRAY));
            setBackground(Color.WHITE);
            setOpaque(true);
            setToolTipText(NbBundle.getMessage(OperationWidget.class, "Hint_ListView"));
        }

        protected Shape createImage(int width, int height) {
            GeneralPath path = new GeneralPath();
            float gap = width/5f;
            path.moveTo(gap, height/4);
            path.lineTo(width-gap, height/4);
            path.moveTo(gap, height/2);
            path.lineTo(width-gap, height/2);
            path.moveTo(gap, 3*height/4);
            path.lineTo(width-2*gap, 3*height/4);
            return path;
        }
    }

    private static class TabImageWidget extends ImageLabelWidget.PaintableImageWidget {

        public TabImageWidget(Scene scene, int size) {
            super(scene, Color.LIGHT_GRAY, size, size);
            setBorder(BorderFactory.createLineBorder(0, Color.LIGHT_GRAY));
            setBackground(Color.WHITE);
            setOpaque(true);
            setToolTipText(NbBundle.getMessage(OperationWidget.class, "Hint_TabbedView"));
        }

        protected Shape createImage(int width, int height) {
            GeneralPath path = new GeneralPath();
            path.moveTo(1, height/6f);
            path.lineTo(2*width/3f, height/6f);
            path.moveTo(1, height/3f+1);
            path.lineTo(width-1, height/3f+1);
            path.moveTo(width/3f, height/6f+1);
            path.lineTo(width/3f, height/3f);
            path.moveTo(2*width/3f, height/6f+1);
            path.lineTo(2*width/3f, height/3f);
            return path;
        }
    }

    private boolean isNameEditable() {
        return service != null && service.getWsdlUrl() == null;
    }

}
