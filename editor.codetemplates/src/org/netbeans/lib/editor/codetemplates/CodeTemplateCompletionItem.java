/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.lib.editor.codetemplates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Code template completion result item.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateCompletionItem implements CompletionItem {
    
    private static ImageIcon icon;
    
    private final CodeTemplate codeTemplate;
    private final boolean abbrevBased;
    
    private String rightText;
    
    public CodeTemplateCompletionItem(CodeTemplate codeTemplate, boolean abbrevBased) {
        this.codeTemplate = codeTemplate;
        this.abbrevBased = abbrevBased;
    }
    
    private String getLeftText() {
        String description = codeTemplate.getDescription();
        if (description == null) {
            description = CodeTemplateApiPackageAccessor.get().getSingleLineText(codeTemplate);
        }
        return description.trim();
    }
    
    private String getRightText() {
        if (rightText == null) {
            rightText = ParametrizedTextParser.toHtmlText(codeTemplate.getAbbreviation());
        }
        return rightText;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftText(), getRightText(),
                g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
    Color backgroundColor, int width, int height, boolean selected) {
        
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/lib/editor/codetemplates/resources/code_template.png", false); // NOI18N
        }
        CompletionUtilities.renderHtml(icon, getLeftText(), getRightText(),
                g, defaultFont, defaultColor, width, height, selected);
    }

    public void defaultAction(JTextComponent component) {
        Completion.get().hideAll();
        // Remove the typed part
        Document doc = component.getDocument();
        int caretOffset = component.getSelectionStart();
        int prefixLength = 0;
        try {
            String ident = Utilities.getIdentifierBefore((BaseDocument)doc, caretOffset);
            if (ident != null) {
                prefixLength = ident.length();
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (prefixLength > 0) {
            try {
                // Remove the typed prefix
                doc.remove(caretOffset - prefixLength, prefixLength);
            } catch (BadLocationException ble) {
            }
        }
        codeTemplate.insert(component);
    }
    
    public void processKeyEvent(KeyEvent evt) {
    }
    
    public boolean instantSubstitution(JTextComponent component) {
        // defaultAction(component);
        return false;
    }
    
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new DocQuery(codeTemplate));
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }

    public int getSortPriority() {
        return 650;
    }        
    
    public CharSequence getSortText() {
        return ""; //NOI18N
    }

    public CharSequence getInsertPrefix() {
        if (abbrevBased) {
            return codeTemplate.getAbbreviation();
        }
        String insertPrefix = codeTemplate.getParametrizedText();
        int dollarIndex = insertPrefix.indexOf("${"); // NOI18N
        if (dollarIndex >= 0) {
            insertPrefix = insertPrefix.substring(0, dollarIndex);
        }
        return insertPrefix;
    }

    private static final class DocQuery extends AsyncCompletionQuery {
        
        private CodeTemplate codeTemplate;
        
        DocQuery(CodeTemplate codeTemplate) {
            this.codeTemplate = codeTemplate;
        }

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            StringBuffer sb = new StringBuffer(); // NOI18N

            sb.append("<html><pre>"); //NOI18N
            ParametrizedTextParser.parseToHtml(sb, codeTemplate.getParametrizedText());
            sb.append("</pre>"); // NOI18N

            String desc = codeTemplate.getDescription();
            if (desc != null && desc.length() > 0) {
                sb.append("<p>").append(desc).append("</p>"); //NOI18N
            }
            
            // Append abbreviation
            CodeTemplateManagerOperation operation = CodeTemplateApiPackageAccessor.get().getOperation(codeTemplate);
            sb.append("<p>"); //NOI18N
            sb.append(NbBundle.getMessage(CodeTemplateCompletionItem.class, 
                "DOC_ITEM_Abbreviation", //NOI18N
                ParametrizedTextParser.toHtmlText(codeTemplate.getAbbreviation()), 
                operation.getExpandKeyStrokeText()
            ));
            sb.append("<p>"); //NOI18N
            
            resultSet.setDocumentation(new DocItem(sb.toString()));
            resultSet.finish();
        }
        
    }
    
    private static final class DocItem implements CompletionDocumentation {
        
        private String text;
        
        DocItem(String text) {
            this.text = text;
        }
        
        public String getText() {
            return text;
        }
        
        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        public java.net.URL getURL() {
            return null;
        }


        public javax.swing.Action getGotoSourceAction() {
            return null;
        }
    } // End of DocItem class

}
