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
