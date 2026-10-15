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

package org.netbeans.modules.languages.features;

import java.util.Iterator;
import java.util.List;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.languages.CompletionItem;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.ErrorManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/*
 * CompletionSupport.
 * 
 * @author Jan Jancura
 */
public class CompletionSupport implements org.netbeans.spi.editor.completion.CompletionItem {
    
    private static Map<String,ImageIcon> icons = new HashMap<String,ImageIcon> ();
    
    private static ImageIcon getCIcon (String resourceName) {
        if (resourceName == null)
            resourceName = "org/netbeans/modules/languages/resources/node.gif";
        if (!icons.containsKey (resourceName)) {
            ImageIcon icon = ImageUtilities.loadImageIcon (resourceName, false);
            if (icon == null)
                icon = ImageUtilities.loadImageIcon (
                    "org/netbeans/modules/languages/resources/node.gif", false);
            icons.put (
                resourceName,
                icon
            );
        }
        return icons.get (resourceName);
    }

    private String        text;
    private String        filledPrefix;
    private String        description;
    private String        rightText;
    private String        icon;
    private int           priority;
    private int           processKeyEventOffset;
    private String        confirmChars;
    private CompletionItem.Type type;

    CompletionSupport (CompletionItem item, String prefix) {
        text = item.getText ();
        filledPrefix = prefix;
        rightText = item.getLibrary ();
        priority = item.getPriority ();
        
        String color = "000000";
        type = item.getType ();
        if (type == null) type = CompletionItem.Type.FIELD;
        boolean bold = false;
        String key = item.getText ();
        switch (type) {
        case KEYWORD:
            color = "000099";
            icon = "/org/netbeans/modules/languages/resources/keyword.jpg";
            bold = true;
            break;
        case INTERFACE:
            color = "560000";
            icon = "/org/netbeans/modules/editor/resources/completion/interface.png";
            break;
        case CLASS:
            color = "560000";
            icon = "/org/netbeans/modules/editor/resources/completion/class_16.png ";
            break;
        case FIELD:
            icon = "/org/netbeans/modules/editor/resources/completion/field_16.png";
            break;
        case METHOD:
            icon = "/org/netbeans/modules/editor/resources/completion/method_16.png";
            bold = true;
            key = key + "()";
            break;
        case CONSTRUCTOR:
            icon = "/org/netbeans/modules/editor/resources/completion/constructor_16.png";
            bold = true;
            key = key + "()";
            break;
        case CONSTANT:
            icon = "/org/netbeans/modules/editor/resources/completion/field_static_16.png";
            break;
        case LOCAL:
            icon = "/org/netbeans/modules/editor/resources/completion/localVariable.gif";
            break;
        case PARAMETER:
            icon = "/org/netbeans/modules/editor/resources/completion/localVariable.gif";
            break;
        }

        if (item.getDescription () == null)
            description = 
                "<html>" + (bold ? "<b>" : "") + 
                "<font color=#" + color + ">" + key + 
                "</font>" + (bold ? "</b>" : "") + 
                "</html>";
        else
            description = 
                "<html>" + (bold ? "<b>" : "") + 
                "<font color=#" + color + ">" + key + 
                ": </font>" + (bold ? "</b>" : "") + 
                "<font color=#000000> " + 
                item.getDescription () + "</font></html>";
    }

    CompletionSupport (
        String text,
        String prefix,
        String description,
        String rightText,
        String icon,
        int    priority
    ) {
        this.text = text;
        this.filledPrefix = prefix;
        this.description = description;
        this.rightText = rightText;
        this.icon = icon;
        this.priority = priority;
    }

    public void defaultAction (JTextComponent component) {
        NbEditorDocument doc = (NbEditorDocument) component.getDocument ();
        int offset = component.getCaret ().getDot ();
        boolean isMethod = type == CompletionItem.Type.METHOD || type == CompletionItem.Type.CONSTRUCTOR;
        try {
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            if (doc instanceof NbEditorDocument)
                ((NbEditorDocument) doc).readLock ();
            String t = null;
            try {
                TokenSequence sequence = tokenHierarchy.tokenSequence ();

                //find most embedded token sequence on the specified offset
                while(true) {
                    sequence.move (offset - 1);
                    if (!sequence.moveNext ()) break;
                    TokenSequence embedded = sequence.embedded ();
                    if (embedded == null) break;
                    sequence = embedded;
                }
                Token token = sequence.token ();
                String tokenType = token != null ? token.id ().name () : ""; // NOI18N
                String mimeType = sequence.language ().mimeType ();
                Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
                List<Feature> features = l.getFeatureList ().getFeatures (CompletionProviderImpl.COMPLETION, tokenType);
                Iterator<Feature> it = features.iterator ();
                t = isMethod ? text + "()" : text;
                boolean found = false;
                while (it.hasNext ()) {
                    Feature feature =  it.next ();
                    String completionType = getCompletionType (feature, tokenType);
                    if (completionType == CompletionProviderImpl.COMPLETION_COMPLETE) {
                        t = t.substring (offset - sequence.offset ());
                        found = true;
                        break;
                    }
                }
                if (!found && filledPrefix != null) {
                    t = t.substring(filledPrefix.length());
                }
            } finally {
                if (doc instanceof NbEditorDocument)
                    ((NbEditorDocument) doc).readUnlock ();
            }
            doc.insertString (offset, t, null);
            if (isMethod) {
                component.setCaretPosition(component.getCaret ().getDot () - 1);
            }
            processKeyEventOffset = offset + t.length();
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        } catch (LanguageDefinitionNotFoundException ex) {
            // ignore the exception
        }
        Completion.get ().hideAll ();
    }

    public void processKeyEvent (KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            char c = evt.getKeyChar();
            JTextComponent component = (JTextComponent)evt.getSource();
            if (confirmChars == null) {
                confirmChars = getConfirmChars(component);
            }
            if (confirmChars.indexOf(c) != -1) {
                if (c != '.') {
                    Completion.get().hideDocumentation();
                    Completion.get().hideCompletion();
                }
                NbEditorDocument doc = (NbEditorDocument) component.getDocument ();
                try {
                    defaultAction(component);
                    doc.insertString(processKeyEventOffset, Character.toString(c), null);
                } catch (BadLocationException e) {
                }
                if (c == '.')
                    Completion.get().showCompletion();
                evt.consume();
            } // if
        } // if
    }

    public int getPreferredWidth (Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth (
            description, rightText, g, defaultFont
        );
    }

    public void render (
        Graphics g, 
        Font defaultFont, 
        Color defaultColor, 
        Color backgroundColor, 
        int width, int height, boolean selected
    ) {
        CompletionUtilities.renderHtml (
            getCIcon (icon), 
            description, 
            rightText, g, defaultFont, defaultColor, width, height, selected
        );
//            label.setText (selected ? highlightedDesc : description);
//            label.setForeground (defaultColor);
//            label.setBackground (backgroundColor);
//            label.setFont (defaultFont);
//            label.setIcon (getCIcon (icon));
//            label.setBounds (g.getClipBounds ());
//            label.paint (g);
    }

    public CompletionTask createDocumentationTask () {
        return null;
    }

    public CompletionTask createToolTipTask () {
        return null;
    }

    public boolean instantSubstitution (JTextComponent component) {
        defaultAction(component);
        return true;
    }

    public int getSortPriority () {
        return priority;
    }

    public CharSequence getSortText () {
        return text;
    }

    public CharSequence getInsertPrefix () {
        return text;
    }
    
    private String getConfirmChars (JTextComponent component) {
        NbEditorDocument doc = (NbEditorDocument) component.getDocument ();
        StringBuffer buf = new StringBuffer();
        int offset = component.getCaret ().getDot ();
        try {
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            if (doc instanceof NbEditorDocument) {
                ((NbEditorDocument) doc).readLock ();
            }
            try {
                TokenSequence sequence = tokenHierarchy.tokenSequence ();
                if (sequence.isEmpty()) {
                    return ""; // NOI18N
                }
                
                //find most embedded token sequence on the specified offset
                while(true) {
                    sequence.move (offset - 1);
                    if (!sequence.moveNext()) {
                        return ""; // NOI18N
                    }
                    TokenSequence embedded = sequence.embedded ();
                    if (embedded == null) break;
                    sequence = embedded;
                }
                Token token = sequence.token ();
                String tokenType = token.id ().name ();
                String mimeType = sequence.language ().mimeType ();
                Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
                List<Feature> features = l.getFeatureList ().getFeatures (CompletionProviderImpl.COMPLETION, tokenType);
                Iterator<Feature> it = features.iterator ();
                while (it.hasNext ()) {
                    Feature feature =  it.next ();
                    String confChars = (String) feature.getValue("confirmChars"); // NOI18N
                    if (confChars != null) {
                        buf.append(confChars);
                    }
                }
            } finally {
                if (doc instanceof NbEditorDocument)
                    ((NbEditorDocument) doc).readUnlock ();
            }
        } catch (LanguageDefinitionNotFoundException ex) {
        }
        return buf.toString();
    }
    
    private static String getCompletionType (Feature feature, String tokenType) {
        String completionType = (String) feature.getValue ("type");
        if (completionType != null) return completionType;
        if (tokenType.indexOf ("whitespace") >= 0 ||
            tokenType.indexOf ("operator") >= 0 || 
            tokenType.indexOf ("separator") >= 0
        )
            return CompletionProviderImpl.COMPLETION_INSERT;
        else
        if (tokenType.indexOf ("comment") >= 0)
            return CompletionProviderImpl.COMPLETION_APPEND;
        return CompletionProviderImpl.COMPLETION_COMPLETE;
    }
}


