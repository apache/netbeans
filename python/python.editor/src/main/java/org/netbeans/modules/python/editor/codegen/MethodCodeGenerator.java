/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor.codegen;

import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.python.source.PythonUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class MethodCodeGenerator implements CodeGenerator {

    private JTextComponent target;

    /**
     * 
     * @param context containing JTextComponent and possibly other items registered by {@link CodeGeneratorContextProvider}
     */
    private MethodCodeGenerator(Lookup context) { // Good practice is not to save Lookup outside ctor
        target = context.lookup(JTextComponent.class);
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new MethodCodeGenerator(context));
        }
    }

    /**
     * The name which will be inserted inside Insert Code dialog
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MethodCodeGenerator.class, "Method");
    }

    /**
     * This will be invoked when user chooses this Generator from Insert Code
     * dialog
     */
    @Override
    public void invoke() {
        /* The code generated is very simple now, so use the code templates
         * instead of hardcoded code such that they are user configurable...
        MethodCodeGenPanel panel = new MethodCodeGenPanel();
        DialogDescriptor descriptor = new DialogDescriptor(panel, 
                NbBundle.getMessage(MethodCodeGenerator.class, "GenerateMethod"));
        descriptor.setModal(true);

        DialogDisplayer displayer = DialogDisplayer.getDefault();
        Dialog dialog = displayer.createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();
        if (DialogDescriptor.OK_OPTION.equals(descriptor.getValue())) {
            StringBuilder data = new StringBuilder();
            data.append("def "); // NOI18N
            data.append(panel.getMethodName());
            data.append('('); // NOI18N
            if (panel.getMethodParameters().length() > 0) {
                data.append(panel.getMethodParameters());
            }
            data.append(')'); // NOI18N
            data.append(":\n    "); // NOI18N
            try {
                target.getDocument().insertString(target.getCaretPosition(), data.toString(), null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        */
        final BaseDocument doc = (BaseDocument)target.getDocument();
        final CodeTemplateManager ctm = CodeTemplateManager.get(doc);
        if (ctm != null) {
            String template = CodeGenUtils.getCodeTemplate(ctm, "def", "def ", "def __"); // NOI18N
            if (template == null) {
                template="def ${name}(${1 default=\"parameters\"}):\n${initialindent editable=\"false\"}${indent editable=\"false\"}\"\"\"${Documentation}\"\"\"\n${initialindent editable=\"false\"}${indent editable=\"false\"}${cursor}\n";
            }
            ctm.createTemporary(template).insert(target);
        }
    }
}
