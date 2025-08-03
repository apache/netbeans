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

package org.netbeans.modules.websvc.design.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ProjectService;
import org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.design.javamodel.Utils;
import org.netbeans.modules.websvc.design.view.widget.OperationsWidget;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * WebService Designer
 *
 * @author Ajit Bhate
 */
@org.netbeans.api.annotations.common.SuppressWarnings("SE_BAD_FIELD")
public class DesignView extends JPanel  implements Flushable {
    
    public static final Object messageLayerKey = new Object();
    
    private FileObject implementationClass;
    private ProjectService service;
    private ServiceModel serviceModel;
    /** Manages the state of the widgets and corresponding objects. */
    private ObjectScene scene;
    /** Manages the zoom level. */
    private ZoomManager zoomer;
    private Widget mainLayer;
    private Widget messageWidget;
    private LabelWidget headerWidget;
    private Widget contentWidget;
    private Widget mainWidget;
    private Widget separatorWidget;
    private OperationsWidget operationsWidget;
    
    private transient JToolBar toolbar;
    
    /**
     * Creates a new instance of GraphView.
     * @param service
     * @param implementationClass
     */
    public DesignView(ProjectService service, FileObject implementationClass) {
        super(new BorderLayout());
        
        this.service = service;
        this.implementationClass = implementationClass;
        
        scene = new ObjectScene() {
            /**
             * Use our own traversal policy
             */
            @Override
            public Comparable<DesignerWidgetIdentityCode> getIdentityCode(Object object) {
                return new DesignerWidgetIdentityCode(scene,object);
            }
        };
        zoomer = new ZoomManager(scene);

        scene.getActions().addAction(ActionFactory.createCycleObjectSceneFocusAction());
        scene.setKeyEventProcessingType (EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        mainLayer = new LayerWidget(scene);
        mainLayer.setPreferredLocation(new Point(0, 0));
        mainLayer.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));
        scene.addChild(mainLayer);
        
        mainWidget = new Widget(scene);
        mainWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));
        
        headerWidget = new LabelWidget(scene);
        headerWidget.setFont(scene.getFont().deriveFont(Font.BOLD));
        headerWidget.setForeground(Color.GRAY);
        headerWidget.setBorder(BorderFactory.createEmptyBorder(6,28,0,0));
        mainWidget.addChild(headerWidget);
        
        separatorWidget = new SeparatorWidget(scene,
                SeparatorWidget.Orientation.HORIZONTAL);
        separatorWidget.setForeground(Color.ORANGE);
        mainWidget.addChild(separatorWidget);
        
        contentWidget = new Widget(scene);
        contentWidget.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 16));
        mainWidget.addChild(contentWidget);
        
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER){
            /* (non-Javadoc)
             * @see java.awt.FlowLayout#layoutContainer(java.awt.Container)
             */
            @Override
            public void layoutContainer( Container target ) {
                super.layoutContainer(target);
                Component[] components = target.getComponents();
                double height = target.getSize().getHeight()/2;
                for (Component component : components) {
                    Point location = component.getLocation();
                    component.setLocation( (int)location.getX(), (int)height );
                }
            }
        });
        panel.add(new JLabel(NbBundle.getMessage( DesignView.class, "LBL_Wait")));
        add( panel,BorderLayout.CENTER);
        
        mainLayer.addChild(mainWidget);

        messageWidget = new Widget(scene);
        messageWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 4));
        mainLayer.addChild(messageWidget);
        scene.addObject(messageLayerKey, messageWidget);
        
        initServiceModel();
    }
    
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
        }
        return toolbar;
    }
    
    /**
     * Return the view content, suitable for printing (i.e. without a
     * scroll pane, which would result in the scroll bars being printed).
     *
     * @return  the view content, sans scroll pane.
     */
    public JComponent getContent() {
        JComponent view = scene.getView();
        if ( view == null ){
            return this;
        }
        return view;
    }
    
    public void requestFocus() {
        super.requestFocus();
        // Ensure the graph widgets have the focus.
        getContent().requestFocus();
    }
    
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        // Ensure the graph widgets have the focus.
        return getContent().requestFocusInWindow();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.design.view.Flushable#flushContent()
     */
    @Override
    public void flushContent() {
        if ( contentWidget != null ){
            List<Widget> children = contentWidget.getChildren();
            for (Widget widget : children) {
                if ( widget instanceof Flushable ){
                    ((Flushable)widget).flushContent();
                }
            }
        }
    }
    
    private void initServiceModel(){
        SwingWorker<ServiceModel, Void> worker = new SwingWorker<ServiceModel, Void>(){

            @Override
            protected ServiceModel doInBackground() throws Exception {
                ServiceModel model = ServiceModel.getServiceModel(implementationClass);
                configurations = service.getConfigurations();
                return model;
            }
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#done()
             */
            @Override
            protected void done() {
                try {
                    serviceModel = get();
                    initUI( configurations );
                }
                catch(ExecutionException e ){
                    Logger.getLogger( DesignView.class.getName()).log( 
                            Level.WARNING, null, e );
                }
                catch(InterruptedException e ){
                    Logger.getLogger( DesignView.class.getName()).log( 
                            Level.WARNING, null, e );
                            
                }
            }
            
            private Collection<WSConfiguration> configurations;
        };
        worker.execute();
    }
    
    private void initUI(Collection<WSConfiguration> wsConfigurations) {
        serviceModel.addServiceChangeListener(new ServiceChangeListener() {
            public void propertyChanged(String propertyName, 
                    String oldValue, String newValue) 
            {
                if(propertyName.equals("serviceName") || 
                        propertyName.equals("portName") &&
                        DesignView.this.service.getWsdlUrl()!=null)         // NOI18N
                {
                    headerWidget.setLabel(getServiceName());
                }
            }
            public void operationAdded(MethodModel method) {
            }
            public void operationRemoved(MethodModel method) {
            }
            public void operationChanged(MethodModel oldMethod, 
                    MethodModel newMethod) 
            {
            }
        });
        //add operations widget
        operationsWidget = new OperationsWidget(scene, service, serviceModel);
        contentWidget.addChild(operationsWidget);
        //add wsit widget
        if ( !wsConfigurations.isEmpty() && Utils.getService(service)!= null ) {
            WsitWidget wsitWidget = new WsitWidget(scene, Utils.getService(service), 
                    implementationClass, wsConfigurations );
            contentWidget.addChild(wsitWidget);
        }
        
        headerWidget.setLabel( getServiceName());
        
        JComponent sceneView = scene.createView();
        final JScrollPane panel = new JScrollPane(sceneView);
        panel.getVerticalScrollBar().setUnitIncrement(16);
        panel.getHorizontalScrollBar().setUnitIncrement(16);
        panel.setBorder(null);
        
        Component[] components = getComponents();
        for (Component component : components) {
            remove(component);
        }
        add(panel);
        
        sceneView.removeMouseWheelListener((MouseWheelListener)sceneView);
        scene.addSceneListener(new ObjectScene.SceneListener() {
            public void sceneRepaint() {
            }
            public void sceneValidating() {

            }
            public void sceneValidated() {
                int width = panel.getViewport().getWidth();
                if (width <= scene.getBounds().width) {
                    mainWidget.setMinimumSize(new Dimension(width, 0));
                }
            }
        });
        addToolbarActions();
        // vlv: print
        getContent().putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        invalidate();
        repaint();
    }
    
    private void addToolbarActions() {
        getToolbarRepresentation();
        toolbar.addSeparator();
        zoomer.addToolbarActions(toolbar);
        toolbar.addSeparator();
        operationsWidget.addToolbarActions(toolbar);
    }
    
    private String getServiceName() {
        String serviceName = serviceModel.getServiceName();
        if (service.getWsdlUrl()!=null)
            serviceName += " ["+serviceModel.getPortName()+"]";
        return serviceName;
    }
}
