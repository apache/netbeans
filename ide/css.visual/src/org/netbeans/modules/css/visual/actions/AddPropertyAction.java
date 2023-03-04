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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.text.View;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelUtils;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.netbeans.modules.css.visual.api.ViewMode;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "label.add.property=Add Property"
})
public class AddPropertyAction extends AbstractAction {

    private static final String HELP_ID = "css_visual_AddPropertyPanel"; //NOI18N
    
    private RuleEditorPanel panel;

    public AddPropertyAction(RuleEditorPanel panel) {
        super(Bundle.label_add_property());
        this.panel = panel;
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            //use the default rule editor panel with some modifications
            final RuleEditorPanel addPropertyPanel = new RuleEditorPanel(true);

            //create a new model instance and do all the modifications in the dialog
            //in this model. If the user confirms changes the model will be persisted
            //in the corresponding file and RuleEditor will be refreshed based on
            //an event from the parsing task.
            final AtomicReference<Model> model_ref = new AtomicReference<Model>();
            Snapshot snapshot = panel.getModel().getLookup().lookup(Snapshot.class);
            ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/css");
                    if (ri != null) {
                        CssParserResult result = (CssParserResult) ri.getParserResult();
                        model_ref.set(Model.createModel(result));
                    }
                }
            });
            final Model model = model_ref.get();
            
            final AtomicReference<Rule> rule_ref = new AtomicReference<Rule>();
            //resolve the rule to the new model
            model.runReadTask(new Model.ModelTask() {

                @Override
                public void run(StyleSheet styleSheet) {
                    ModelUtils utils = new ModelUtils(model);
                    rule_ref.set(utils.findMatchingRule(panel.getModel(), panel.getRule()));
                }
            });
            
            Rule rule = rule_ref.get();
            
            addPropertyPanel.setModel(model);
            addPropertyPanel.setRule(rule);
            
            DialogDescriptor descriptor = new DialogDescriptor(addPropertyPanel, Bundle.label_add_property(), true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("OK".equals(e.getActionCommand())) {
                        addPropertyPanel.node.applyModelChanges();
                        //Refresh the RE content:
                        //As here we use a new css source model but not the one held in RE panel,
                        //we need to explicly let the RE know that its model changed.
                        //Normally this is done by the CssCaretAwareSourceTask, but in some
                        //cases the task is not called as the source is not even opened in editor.
                        //(RE content is set by "document" section of the CSS Styles Window)
                        panel.refreshModel();
                    }
                }
            });
            descriptor.setHelpCtx(new HelpCtx(HELP_ID));
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
            
            //clear out the panel's model reference so it can be GCed
            addPropertyPanel.releaseModel();
            
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
