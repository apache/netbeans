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
 * @author Tor Norbye
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
