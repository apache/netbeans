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

package org.netbeans.modules.websvc.wsitconf.ui.service;

import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    
    private ToolBarDesignEditor editor;
    private Node node;
    private UndoManager undoManager;
    private Project project;
    private JaxWsModel jaxwsmodel;
    
    /**
     * Creates a new instance of PanelFactory
     */
    PanelFactory(ToolBarDesignEditor editor, Node node, UndoManager undoManager, Project p, JaxWsModel jxwsmodel) {
        this.editor=editor;
        this.node = node;
        this.project = p;
        this.jaxwsmodel = jxwsmodel;
        this.undoManager = undoManager;
    }

    public SectionInnerPanel createInnerPanel(Object key) {
        if (key instanceof Binding) {
            Binding b = (Binding)key;
            return new BindingPanel((SectionView) editor.getContentView(), node, project, b, undoManager, jaxwsmodel);
        }
        if (key instanceof BindingOperation) {
            BindingOperation o = (BindingOperation)key;
            return new OperationPanel((SectionView) editor.getContentView(), node, project, o);
        }
        if (key instanceof BindingInput) {
            BindingInput i = (BindingInput)key;
            return new InputPanel((SectionView) editor.getContentView(), i, undoManager);
        }
        if ((key instanceof BindingOutput) || (key instanceof BindingFault)) {
            return new GenericElementPanel((SectionView) editor.getContentView(),(WSDLComponent) key, undoManager);
        }
        return null;
    }
}
