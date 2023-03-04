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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class BlockHighlighting extends AbstractHighlightsContainer implements HighlightsChangeListener {

    private static final Logger LOG = Logger.getLogger(BlockHighlighting.class.getName());

    private String layerId;
    private JTextComponent component;
    private Document document;
    private PositionsBag bag;
    
    public BlockHighlighting(String layerId, JTextComponent component) {
        this.layerId = layerId;
        this.component = component;
        this.document = component.getDocument();
        
        this.bag = new PositionsBag(document);
        this.bag.addHighlightsChangeListener(this);
        
    }

    public String getLayerTypeId() {
        return layerId;
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }

    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
    }
    
    public void highlightBlock(
        final int startOffset, final int endOffset, final String coloringName, 
        final boolean extendsEol, final boolean extendsEmptyLine
    ) {
        document.render(new Runnable() {
            public void run() {
                if (startOffset < endOffset) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Highlighting block: [" + startOffset + ", " + endOffset + "]; " + getLayerTypeId());
                    }

                    try {
                        PositionsBag newBag = new PositionsBag(document);
                        newBag.addHighlight(
                            document.createPosition(startOffset), 
                            document.createPosition(endOffset),
                            getAttribs(coloringName, extendsEol, extendsEmptyLine)
                        );
                        bag.setHighlights(newBag);
                    } catch (BadLocationException e) {
                        LOG.log(Level.FINE, "Can't add highlight <" + startOffset + 
                            ", " + endOffset + ", " + coloringName + ">", e);
                    }
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Reseting block highlighs; " + getLayerTypeId());
                    }

                    bag.clear();
                }
            }
        });
    }

    public int [] gethighlightedBlock() {
        HighlightsSequence sequence = bag.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (sequence.moveNext()) {
            return new int [] { sequence.getStartOffset(), sequence.getEndOffset() };
        } else {
            return null;
        }
    }
    
    private AttributeSet getAttribs(String coloringName, boolean extendsEol, boolean extendsEmptyLine) {
        FontColorSettings fcs = MimeLookup.getLookup(getMimeType(component)).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs.getFontColors(coloringName);
        
        if (attribs == null) {
            attribs = SimpleAttributeSet.EMPTY;
        } else if (extendsEol || extendsEmptyLine) {
            attribs = AttributesUtilities.createImmutable(
                attribs, 
                AttributesUtilities.createImmutable(
                    ATTR_EXTENDS_EOL, Boolean.valueOf(extendsEol),
                    ATTR_EXTENDS_EMPTY_LINE, Boolean.valueOf(extendsEmptyLine))
            );
        }
        
        return attribs;
    }
    
    /* package */ static String getMimeType(JTextComponent component) {
        Document doc = component.getDocument();
        String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
        if (mimeType == null) {
            EditorKit kit = component.getUI().getEditorKit(component);
            if (kit != null) {
                mimeType = kit.getContentType();
            }
        }
        return mimeType;
    }
}
