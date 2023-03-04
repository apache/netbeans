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

package org.netbeans.modules.websvc.design.view;

import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.support.EditWSAttributesCookie;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProviderRegistry;
import org.netbeans.modules.websvc.design.view.widget.AbstractTitledWidget;
import org.netbeans.modules.websvc.design.view.widget.ButtonWidget;
import org.netbeans.modules.websvc.design.view.widget.ImageLabelWidget;
import org.netbeans.modules.websvc.design.view.widget.CheckBoxWidget;
import org.netbeans.modules.websvc.jaxws.api.JAXWSView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ajit Bhate
 */
@org.netbeans.api.annotations.common.SuppressWarnings("SE_BAD_FIELD")
public class WsitWidget extends AbstractTitledWidget {
    
    private transient FileObject implementationClass;
    private transient Widget buttons;
    private transient Widget configButtons;
    private transient ButtonWidget advancedButton;
    private transient ImageLabelWidget headerLabelWidget;
    private Service service;
    private PropertyChangeListener configListener;
    private Object key = new Object();
    private Collection<WSConfiguration> configurations;
    
    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param service
     * @param serviceModel
     */
    public WsitWidget(ObjectScene scene, final Service service, 
            FileObject implementationClass, 
            Collection<WSConfiguration> wsConfigurations ) 
    {
        super(scene,RADIUS,BORDER_COLOR);
        this.implementationClass = implementationClass;
        this.service=service;
        configurations = wsConfigurations;
        setOpaque(true);
        setBackground(TITLE_COLOR_PARAMETER);
        configListener = new WSConfigurationListener(this);
        createContent();
    }
    
    private void createContent() {
        headerLabelWidget = new ImageLabelWidget(getScene(), null,
                NbBundle.getMessage(WsitWidget.class, "LBL_Wsit"));
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),1);
        
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        
        buttons.addChild(getExpanderWidget());
        
        getHeaderWidget().addChild(buttons);

        getContentWidget().setBorder(BorderFactory.createEmptyBorder(RADIUS));

        configButtons = new Widget(getScene());
        configButtons.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.LEFT_TOP, RADIUS));
        getContentWidget().addChild(configButtons);
        populateConfigWidget();

        Widget advancedButtonContainer = new Widget(getScene());
        advancedButtonContainer.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.RIGHT_BOTTOM, RADIUS));
        advancedButton = new ButtonWidget(getScene(),
                new AdvancedAction(service,implementationClass));
        advancedButton.setOpaque(true);
        advancedButton.setRoundedBorder(ButtonWidget.BORDER_RADIUS, 4, 0, null);
        advancedButtonContainer.addChild(advancedButton);
        getContentWidget().addChild(advancedButtonContainer);
    }
    
    private void populateConfigWidget() {
        for( final WSConfiguration config : getWSConfigurations() ){
            if(config != null){
                CheckBoxWidget button = new CheckBoxWidget(getScene(), config.getDisplayName()) {
                    @Override
                    protected Object hashKey() {
                        return config;
                    }

                    @Override
                    protected void notifyAdded() {
                        super.notifyAdded();
                        config.registerListener(configListener);
                        setVisible(config.isEnabled());
                        determineVisibility();
                    }
                    @Override
                    protected void notifyRemoved() {
                        super.notifyRemoved();
                        config.unregisterListener(configListener);
                    }
                };
                button.setSelected(config.isSet());
                button.setAction(new ConfigWidgetAction(config));
                button.setToolTipText(config.getDescription());
                button.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
                configButtons.addChild(button);
            }
        }
    }
    
    private Collection<WSConfiguration> getWSConfigurations(){
        return configurations;
    }
    
    private void determineVisibility() {
        for(Widget button:configButtons.getChildren()) {
            if(button.isVisible()) {
                setVisible(true);
                return;
            }
        }
        setVisible(false);
    }

    protected Paint getBodyPaint(Rectangle bounds) {
        return TITLE_COLOR_PARAMETER;
    }
    
    @Override
    public Object hashKey() {
        return key;
    }
    
    static class ConfigWidgetAction extends AbstractAction{
        WSConfiguration config;
        public ConfigWidgetAction(WSConfiguration config){
            this.config = config;
        }
        public void actionPerformed(ActionEvent event) {
            if(event.getActionCommand().equals(CheckBoxWidget.ACTION_COMMAND_SELECTED)){
                config.set();
            }else{
                config.unset();
            }
        }
    }
    
    class AdvancedAction extends AbstractAction{
        private transient FileObject implementationClass;
        private Service service;
        private EditWSAttributesCookie cookie;
        public AdvancedAction(Service service, FileObject implementationClass){
            super(NbBundle.getMessage(WsitWidget.class, "LBL_Wsit_Advanced"));
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AdvancedAction.class, "Hint_Wsit_Advanced"));
            putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AdvancedAction.class, "LBL_Wsit_Advanced_mnem_pos")));
            this.implementationClass = implementationClass;
            this.service = service;
            setEnabled(false);
            initializeCookie();
        }
        public void actionPerformed(ActionEvent event) {
            if(cookie==null) return;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    cookie.openWSAttributesEditor();
                }
            }, 10);
        }
        private void initializeCookie() {
            Project project = FileOwnerQuery.getOwner(implementationClass);
            if (project==null) return;
            JAXWSView view = JAXWSView.getJAXWSView();
            if (view==null) return;
            final Node node = view.createJAXWSView(project);
            if(node==null) return;
            final FileObject currentImplBean = implementationClass;
            final Service currentService = service;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for(Node n :node.getChildren().getNodes()) {
                        Service sv = n.getLookup().lookup(Service.class);
                        if (sv==null ) continue;
                        if(n.getLookup().lookup(FileObject.class) == currentImplBean &&
                                 sv.getName().equals(currentService.getName())) {
                            cookie = n.getLookup().lookup(EditWSAttributesCookie.class);
                            setEnabled(cookie!=null);
                            return;
                        }
                    }
                }
            });
        }
    }
    
    static final class WSConfigurationListener implements PropertyChangeListener {

        private WsitWidget widget;
        private WSConfigurationListener(WsitWidget widget) {
            this.widget= widget;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            ObjectScene scene = (ObjectScene) widget.getScene();
            String property = evt.getPropertyName();
            Object newValue = evt.getNewValue();
            if(source instanceof WSConfiguration) {
                Widget configWidget = scene.findWidget(source);
                if(WSConfiguration.PROPERTY.equals(property)) {
                    if(configWidget instanceof CheckBoxWidget) {
                        ((CheckBoxWidget)configWidget).setSelected((Boolean)newValue);
                        scene.validate();
                        widget.revalidate(true);
                    }
                } else if(WSConfiguration.PROPERTY_ENABLE.equals(property)) {
                    if(configWidget instanceof CheckBoxWidget) {
                        ((CheckBoxWidget)configWidget).setVisible((Boolean)newValue);
                        widget.determineVisibility();
                        widget.revalidate();
                    }
                }

            }
        }
        
    }
}
