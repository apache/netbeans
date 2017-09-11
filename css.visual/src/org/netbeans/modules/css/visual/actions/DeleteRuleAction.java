/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
