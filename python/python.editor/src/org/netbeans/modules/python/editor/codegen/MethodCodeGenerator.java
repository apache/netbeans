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
