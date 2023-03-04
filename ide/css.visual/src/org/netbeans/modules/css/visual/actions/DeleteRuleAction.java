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
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "label.delete.rule=Delete Rule",
    "delete.rule.confirmation=Do you really want to delete the rule {0} from file {1}?"
})
public class DeleteRuleAction extends AbstractAction {

    private RuleEditorPanel panel;

    public DeleteRuleAction(RuleEditorPanel panel) {
        super(Bundle.label_delete_rule());
        this.panel = panel;
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Model model = panel.getModel();
        final Rule rule = panel.getRule();
        assert model != null;
        assert rule != null;
        
        final AtomicReference<String> ruleName_ref = new AtomicReference<String>();
        model.runReadTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                ruleName_ref.set(model.getElementSource(rule.getSelectorsGroup()).toString());
            }
        });
        String ruleName = ruleName_ref.get();
        assert ruleName != null;
        
        FileObject file = model.getLookup().lookup(FileObject.class);
        String fileName = file != null ? file.getNameExt() : "???"; //NOI18N
        
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.delete_rule_confirmation(ruleName, fileName), NotifyDescriptor.YES_NO_OPTION);
        
        Object option = DialogDisplayer.getDefault().notify(nd);
        if(option == NotifyDescriptor.YES_OPTION) {
            model.runWriteTask(new Model.ModelTask() {
                @Override
                public void run(StyleSheet styleSheet) {
                    styleSheet.getBody().removeRule(rule);
                    try {
                        model.applyChanges();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }

    
}
