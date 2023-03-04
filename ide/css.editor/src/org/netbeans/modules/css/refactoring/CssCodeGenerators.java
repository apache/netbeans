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
package org.netbeans.modules.css.refactoring;

import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.css.editor.ui.CssRuleCreateActionDialog;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssCodeGenerators {

    public static class Factory implements CodeGenerator.Factory {

        @Override
	public List<? extends CodeGenerator> create(Lookup context) {
	    JTextComponent component = context.lookup(JTextComponent.class);
	    return Collections.singletonList(new GenerateCssRule());
	}
    }

    public static class GenerateCssRule implements CodeGenerator {

        @Override
	public String getDisplayName() {
	    return NbBundle.getMessage(CssCodeGenerators.class, "MSG_CreateCssRule"); //NOI18N
	}

        @Override
	public void invoke() {
	    final JTextComponent target = EditorRegistry.lastFocusedComponent();

	    CssRuleCreateActionDialog cssRuleCreateActionDialog = new CssRuleCreateActionDialog();
	    cssRuleCreateActionDialog.showDialog();
	    final String styleRuleName = cssRuleCreateActionDialog.getStyleRuleName();
	    if ((styleRuleName != null) && !styleRuleName.equals("")) {
		final BaseDocument doc = (BaseDocument) target.getDocument();
		doc.runAtomic(new Runnable() {

                    @Override
		    public void run() {
                        try {
			    doc.insertString(target.getCaretPosition(), "\n" + styleRuleName + " {\n\n}", null);
			} catch (BadLocationException exc) {
                            Exceptions.printStackTrace(exc);
			}
		    }
		});
	    }
	}
    }
}
