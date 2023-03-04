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

import java.awt.EventQueue;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ProjectService;
import org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.design.view.actions.AddOperationAction;
import org.netbeans.modules.websvc.design.view.actions.RemoveOperationAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class OperationsWidget extends FlushableWidget {
    
    private transient ServiceModel serviceModel;
    private transient Action addAction;
    private transient RemoveOperationAction removeAction;
    
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private ObjectSceneListener operationSelectionListener;
    
    
    
    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param service
     * @param serviceModel
     */
    public OperationsWidget(ObjectScene scene, final ProjectService service, 
            final ServiceModel serviceModel) 
    {
        super(scene,RADIUS,BORDER_COLOR);
        this.serviceModel = serviceModel;
        serviceModel.addServiceChangeListener(new ServiceChangeListener() {
            
            @Override
            public void propertyChanged(String propertyName, String oldValue,
                    String newValue) {
            }
            
            @Override
            public void operationAdded(final MethodModel method) {
                if(!EventQueue.isDispatchThread()) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            OperationWidget operationWidget = 
                                new OperationWidget(getObjectScene(), 
                                        serviceModel,service, method);
                            getContentWidget().addChild(operationWidget);
                            updateHeaderLabel();
                            getScene().validate();
                        }
                    });
                } else {
                    OperationWidget operationWidget = 
                        new OperationWidget(getObjectScene(), serviceModel,
                                service, method);
                    getContentWidget().addChild(operationWidget);
                    updateHeaderLabel();
                    getScene().validate();
                }
            }
            
            @Override
            public void operationRemoved(MethodModel method) {
                final Widget operationWidget = getObjectScene().findWidget(method);
                if(operationWidget!=null) {
                    if(!EventQueue.isDispatchThread()) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                getContentWidget().removeChild(operationWidget);
                                updateHeaderLabel();
                                getScene().validate();
                            }
                        });
                    } else {
                        getContentWidget().removeChild(operationWidget);
                        updateHeaderLabel();
                        getScene().validate();
                    }
                }
            }
            
            @Override
            public void operationChanged(final MethodModel oldMethod, 
                    final MethodModel newMethod) 
            {
                if(!EventQueue.isDispatchThread()) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            operationRemoved(oldMethod);
                            operationAdded(newMethod);
                        }
                    });
                } else {
                    operationRemoved(oldMethod);
                    operationAdded(newMethod);
                }
            }
            
        });
        addAction = new AddOperationAction(service, serviceModel.getImplementationClass());
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
            addAction,
        })));
        createContent(service);
    }
    
    private void createContent(ProjectService service) {
        if (serviceModel==null) {
            return;
        }
        
        headerLabelWidget = new ImageLabelWidget(getScene(), null,
                NbBundle.getMessage(OperationWidget.class, "LBL_Operations"));
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),1);
        updateHeaderLabel();
        
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));
        
        ButtonWidget addButton = new ButtonWidget(getScene(), addAction);
        addButton.setOpaque(true);
        addButton.setRoundedBorder(addButton.BORDER_RADIUS, 4, 0, null);
        
        removeAction = new RemoveOperationAction(service);
        ButtonWidget removeButton = new ButtonWidget(getScene(), removeAction);
        removeButton.setOpaque(true);
        removeButton.setRoundedBorder(removeButton.BORDER_RADIUS, 4, 0, null);
        
        buttons.addChild(addButton);
        buttons.addChild(removeButton);
        buttons.addChild(getExpanderWidget());
        
        getHeaderWidget().addChild(buttons);
        
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(RADIUS));
        if(serviceModel.getOperations()!=null) {
            for(MethodModel operation:serviceModel.getOperations()) {
                OperationWidget operationWidget = new OperationWidget(getObjectScene(), 
                        serviceModel, service, operation);
                getContentWidget().addChild(operationWidget);
            }
        }
    }
    
    private void updateHeaderLabel() {
        int noOfOperations = serviceModel.getOperations()==null?0:serviceModel.getOperations().size();
        headerLabelWidget.setComment("("+noOfOperations+")");
    }
    
    public Object hashKey() {
        return serviceModel;
    }
    
    protected void notifyAdded() {
        super.notifyAdded();
        operationSelectionListener = new ObjectSceneAdapter() {
            public void selectionChanged(ObjectSceneEvent event,
                    Set<Object> previousSelection, Set<Object> newSelection) {
                Set<MethodModel> methods = new HashSet<MethodModel>();
                if(newSelection!=null) {
                    for(Object obj:newSelection) {
                        if(obj instanceof MethodModel) {
                            methods.add((MethodModel)obj);
                        }
                    }
                }
                removeAction.setWorkingSet(methods);
            }
        };
        getObjectScene().addObjectSceneListener(operationSelectionListener,
                ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
    }
    
    protected void notifyRemoved() {
        super.notifyRemoved();
        if(operationSelectionListener!=null) {
            getObjectScene().removeObjectSceneListener(operationSelectionListener,
                    ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
            operationSelectionListener=null;
        }
    }
    
    /**
     * Adds the widget actions to the given toolbar (no separators are
     * added to either the beginning or end).
     *
     * @param  toolbar  to which the actions are added.
     */
    public void addToolbarActions(JToolBar toolbar) {
        toolbar.add(addAction);
    }
}
