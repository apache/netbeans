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
