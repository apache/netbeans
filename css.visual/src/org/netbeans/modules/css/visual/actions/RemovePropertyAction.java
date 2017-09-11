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
