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

package org.netbeans.modules.editor.impl.highlighting;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class ComposedTextHighlighting extends AbstractHighlightsContainer implements DocumentListener, HighlightsChangeListener {

    private static final Logger LOG = Logger.getLogger(ComposedTextHighlighting.class.getName());
    
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.impl.highlighting.ComposedTextHighlighting"; //NOI18N
    private static final String PROP_COMPLETION_ACTIVE = "completion-active"; //NOI18N
    
    private final JTextComponent component;
    private final Document document;
    private final OffsetsBag bag;
    private final AttributeSet highlightInverse;
    private final AttributeSet highlightUnderlined;
    
    private boolean isComposingText = false;
    
    public ComposedTextHighlighting(JTextComponent component, Document document, String mimeType) {
        // Prepare the highlight
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(FontColorSettings.class);
        AttributeSet dc = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        Color background = (Color) dc.getAttribute(StyleConstants.Background);
        Color foreground = (Color) dc.getAttribute(StyleConstants.Foreground);
        highlightInverse = AttributesUtilities.createImmutable(StyleConstants.Background, foreground, StyleConstants.Foreground, background);
        highlightUnderlined = AttributesUtilities.createImmutable(StyleConstants.Underline, foreground);
        
        // Create the highlights container
        this.bag = new OffsetsBag(document);
        this.bag.addHighlightsChangeListener(this);

        // Start listening on the document
        this.document = document;
        this.document.addDocumentListener(WeakListeners.document(this, this.document));
        
        this.component = component;
    }

    // ----------------------------------------------------------------------
    //  AbstractHighlightsContainer implementation
    // ----------------------------------------------------------------------
    
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }

    // ----------------------------------------------------------------------
    //  HighlightsChangeListener implementation
    // ----------------------------------------------------------------------
    
    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
    }

    // ----------------------------------------------------------------------
    //  DocumentListener implementation
    // ----------------------------------------------------------------------
    
    public void insertUpdate(DocumentEvent e) {
        AttributedString composedText = getComposedTextAttribute(e);
        
        if (composedText != null) {
            if (!isComposingText) {
                // we just started composing text, disable parsing
                enableParsing(component, false);
            }
            
            // set the flag
            isComposingText = true;
            
            if (LOG.isLoggable(Level.FINE)) {
                StringBuilder sb = new StringBuilder();
                AttributedCharacterIterator aci = composedText.getIterator();

                sb.append("\nInsertUpdate: \n"); //NOI18N
                for(char c = aci.first(); c != AttributedCharacterIterator.DONE; c = aci.next()) {
                    sb.append("'").append(c).append("' = {"); //NOI18N
                    Map<AttributedCharacterIterator.Attribute, ?> attributes = aci.getAttributes();
                    for(Map.Entry<AttributedCharacterIterator.Attribute, ?> attributeEntry : attributes.entrySet()) {
                        AttributedCharacterIterator.Attribute key = attributeEntry.getKey();
                        Object value = attributeEntry.getValue();
                        if (value instanceof InputMethodHighlight) {
                            sb.append("'").append(key).append("' = {"); //NOI18N
                            Map<TextAttribute, ?> style = ((InputMethodHighlight) value).getStyle();
                            if (style == null) {
                                style = Toolkit.getDefaultToolkit().mapInputMethodHighlight((InputMethodHighlight) value);
                            }
                            if (style != null) {
                                for(Map.Entry<TextAttribute, ?> styleEntry : style.entrySet()) {
                                    TextAttribute ta = styleEntry.getKey();
                                    Object tav = styleEntry.getValue();
                                    sb.append("'").append(ta).append("' = '").append(tav).append("', "); //NOI18N
                                }
                            } else {
                                sb.append("null"); //NOI18N
                            }
                            sb.append("}, "); //NOI18N
                        } else {
                            sb.append("'").append(key).append("' = '").append(value).append("', "); //NOI18N
                        }
                    }
                    sb.append("}\n"); //NOI18N
                }
                sb.append("-------------------------------------\n"); //NOI18N
                LOG.fine(sb.toString());
            }
            
            AttributedCharacterIterator aci = composedText.getIterator();
            bag.clear();
            
            int offset = e.getOffset();
            for(char c = aci.first(); c != AttributedCharacterIterator.DONE; c = aci.next()) {
                Map<AttributedCharacterIterator.Attribute, ?> attributes = aci.getAttributes();
                AttributeSet attrs = translateAttributes(attributes);
                bag.addHighlight(offset, offset + 1, attrs);
                offset++;
            }
        } else {
            if (isComposingText) {
                // we stopped composing text, turn the parser on again
                enableParsing(component, true);
            }
            
            isComposingText = false;
            bag.clear();
        }
    }

    public void removeUpdate(DocumentEvent e) {
        // ignore
    }

    public void changedUpdate(DocumentEvent e) {
        // ignore
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------
    
    private static AttributedString getComposedTextAttribute(DocumentEvent e) {
        if (e instanceof BaseDocumentEvent) {
            AttributeSet attribs = ((BaseDocumentEvent) e).getChangeAttributes();
            if (attribs != null) {
                Object value = attribs.getAttribute(StyleConstants.ComposedTextAttribute);
                if (value instanceof AttributedString) {
                    return (AttributedString) value;
                }
            }
        }
        return null;
    }

    private AttributeSet translateAttributes(Map<AttributedCharacterIterator.Attribute, ?> source) {
        for(Object sourceValue : source.values()) {
            
            // Ignore any non-input method related highlights
            if (!(sourceValue instanceof InputMethodHighlight)) {
                continue;
            }
            
            InputMethodHighlight imh = (InputMethodHighlight) sourceValue;
            
            if (imh.isSelected()) {
                return highlightInverse;
            } else {
                return highlightUnderlined;
            }
        }
        
        LOG.fine("No translation for " + source);
        return SimpleAttributeSet.EMPTY;
    }
    
    private static void enableParsing(JTextComponent component, boolean enable) {
        boolean newCompletionActive = !enable;
        Boolean oldCompletionActive = (Boolean) component.getClientProperty(PROP_COMPLETION_ACTIVE);
        
        if (oldCompletionActive == null || oldCompletionActive != newCompletionActive) {
            component.putClientProperty(PROP_COMPLETION_ACTIVE, newCompletionActive);
        }
    }
}
