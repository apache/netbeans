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
