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
package org.netbeans.modules.websvc.customization.multiview;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author rico
 */
public abstract class SaveableSectionInnerPanel extends SectionInnerPanel {

    private EnterKeyListener listener;

    /** Creates a new instance of SaveableSectionInnerPanel */
    public SaveableSectionInnerPanel(SectionView view) {
        super(view);
    }

    protected boolean isClient(Node node) {
        Client client = node.getLookup().lookup(Client.class);
        if (client != null) {
            return true;
        }
        JaxWsService service = node.getLookup().lookup(JaxWsService.class);
        if (service != null && !service.isServiceProvider()) {
            return true;
        }
        return false;
    }

    protected void setModelDirty(WSDLModel model) {
        try {
            ModelSource ms = model.getModelSource();
            FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
            DataObject wsdlDO = DataObject.find(fo);
            if (!wsdlDO.isModified()) {
                wsdlDO.setModified(true);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    protected void disableEnterKey() {
        Component[] components = this.getComponents();
        listener = new EnterKeyListener();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component.isFocusable() && !(component instanceof JLabel)) {
                KeyListener kl = (KeyListener) WeakListeners.create(KeyListener.class, listener,
                        component);
                component.addKeyListener(kl);
            }
        }
    }

    private static class EnterKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
            }
        }
    }

    /**
     * Perform anything here other than saving the wsdl
     */
    public abstract void save();

    /**
     * Does the jaxws model need to be saved?
     */
    public boolean jaxwsIsDirty() {
        return false;
    }

    /**
     * Has the wsdl been changed?
     */
    public abstract boolean wsdlIsDirty();
}
