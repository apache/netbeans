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
package org.netbeans.modules.css.visual.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.netbeans.modules.css.visual.RuleEditorNode;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "label.remove.property=Remove Property"
})
public class RemovePropertyAction extends AbstractAction {

    private RuleEditorPanel panel;
    private RuleEditorNode.DeclarationProperty propertyDescriptor;

    public RemovePropertyAction(RuleEditorPanel panel, RuleEditorNode.DeclarationProperty propertyDescriptor) {
        super(Bundle.label_remove_property());
        this.panel = panel;
        this.propertyDescriptor = propertyDescriptor;
        setEnabled(propertyDescriptor.getDeclaration().getModel().canApplyChanges());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Model model = panel.getModel();
        model.runWriteTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                PropertyDeclaration toremove = propertyDescriptor.getDeclaration();
                Declaration declaration = (Declaration)toremove.getParent();
                Declarations declarations = (Declarations)declaration.getParent();
                declaration.removeElement(toremove);
                
                declarations.removeDeclaration(declaration);
                try {
                    model.applyChanges();
                } catch (IOException | BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    
}
