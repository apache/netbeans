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

import java.awt.Dialog;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Code generator for generating a Python property
 *
 */
public class PropertyCodeGenerator implements CodeGenerator {
    private JTextComponent target;

    private PropertyCodeGenerator(Lookup context) {
        target = context.lookup(JTextComponent.class);
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new PropertyCodeGenerator(context));
        }
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(PropertyCodeGenerator.class, "Property");
    }

    @Override
    public void invoke() {
        PropertyCodeGenPanel panel = new PropertyCodeGenPanel();
        DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(PropertyCodeGenerator.class, "PropertyTitle"));
        descriptor.setModal(true);
        DialogDisplayer displayer = DialogDisplayer.getDefault();
        Dialog dialog = displayer.createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();
        if (DialogDescriptor.OK_OPTION.equals(descriptor.getValue())) {
            try {
                String name = panel.getPropertyName();
                boolean readOnly = panel.isReadOnly();
                int insertOffset = target.getCaretPosition();
                BaseDocument doc = (BaseDocument) target.getDocument();
                int lineStartOffset = IndentUtils.lineStartOffset(doc, insertOffset);
                int initialIndent = IndentUtils.lineIndent(doc, lineStartOffset);
                int oneLevel = IndentUtils.indentLevelSize(doc);
                StringBuilder sb = new StringBuilder();

                insertOffset = lineStartOffset;


                sb.append(IndentUtils.createIndentString(doc, initialIndent));
                sb.append("def __init__(self):\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, initialIndent+oneLevel));
                sb.append("self._");
                sb.append(name);
                sb.append(" = 0 # TODO\n");

                sb.append(IndentUtils.createIndentString(doc, initialIndent));
                sb.append("def "); // NOI18N
                sb.append(name);
                sb.append("():\n"); // NOI18N

                // TODO - insert the "_name" assignment in the constructor

                sb.append(IndentUtils.createIndentString(doc, initialIndent+oneLevel));
                sb.append("doc = \"The " + name + " property.\"\n"); // NOI18N

                sb.append(IndentUtils.createIndentString(doc, initialIndent+oneLevel));
                sb.append("def fget(self):\n"); // NOI18N

                sb.append(IndentUtils.createIndentString(doc, initialIndent+2*oneLevel));
                sb.append("return self._" + name + "\n"); // NOI18N
                if (!readOnly) {
                    sb.append(IndentUtils.createIndentString(doc, initialIndent+1*oneLevel));
                    sb.append("def fset(self, value):\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, initialIndent+2*oneLevel));
                    sb.append("self._" + name + " = value\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, initialIndent+1*oneLevel));
                    sb.append("def fdel(self):\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, initialIndent+2*oneLevel));
                    sb.append("del self._" + name + "\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, initialIndent+1*oneLevel));
                    sb.append("return locals()\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, initialIndent));
                    sb.append(name + " = property(**" + name + "())\n"); // NOI18N
                }
                try {
                    doc.insertString(insertOffset, sb.toString(), null);
                    target.setCaretPosition(insertOffset+initialIndent);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
