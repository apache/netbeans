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

package org.netbeans.modules.editor.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting;
import static org.netbeans.modules.editor.lib2.highlighting.Factory.BLOCK_SEARCH_LAYER;
import static org.netbeans.modules.editor.lib2.highlighting.Factory.INC_SEARCH_LAYER;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public class TextSearchHighlighting extends AbstractHighlightsContainer implements PropertyChangeListener, HighlightsChangeListener, DocumentListener {

    private static final Logger LOG = Logger.getLogger(TextSearchHighlighting.class.getName());
    
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.TextSearchHighlighting"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(LAYER_TYPE_ID, 1, false, false);
    
    private final MimePath mimePath;
    private final JTextComponent component;
    private final Document document;
    private final OffsetsBag bag;
    
    /** Creates a new instance of TextSearchHighlighting */
    public TextSearchHighlighting(JTextComponent component) {
        // Determine the mime type
        String mimeType = getMimeType(component);
        this.mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);
        
        this.component = component;
        this.document = component.getDocument();
        
        // Let the internal listener update first...
        this.document.addDocumentListener(WeakListeners.document(this, this.document));

        // ...and the bag second
        this.bag = new OffsetsBag(document);
        this.bag.addHighlightsChangeListener(this);
        
        EditorFindSupport.getInstance().addPropertyChangeListener(
            WeakListeners.propertyChange(this, EditorFindSupport.getInstance())
        );
        
        fillInTheBag();
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }
    
    @Override
    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null ||
            EditorFindSupport.FIND_WHAT.equals(evt.getPropertyName()) ||
            EditorFindSupport.FIND_HIGHLIGHT_SEARCH.equals(evt.getPropertyName()))
        {
            fillInTheBag();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.bag.removeHighlights(Math.max(e.getOffset() - 1, 0), Math.min(e.getOffset() + 1, document.getLength()), false);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.bag.removeHighlights(e.getOffset(), e.getOffset() + e.getLength(), false);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // not interested
    }

    private void fillInTheBag() {
        final Document d = document;
        final OffsetsBag b = bag;
        RP.post(new Runnable() {
            private boolean documentLocked = false;

            @Override
            public void run() {
                if (!documentLocked) {
                    documentLocked = true;
                    d.render(this);
                    return;
                }

                OffsetsBag newBag = new OffsetsBag(d);

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "TSH: filling the bag; enabled = {0}", isEnabled());
                }

                if (isEnabled() && component.equals(EditorFindSupport.getInstance().getFocusedTextComponent())) {
                    try {
                        int [] blocks = EditorFindSupport.getInstance().getBlocks(
                            new int [] {-1, -1}, d, 0, d.getLength());

                        assert blocks.length % 2 == 0 : "Wrong number of block offsets";

                        AttributeSet attribs = getAttribs();
                        for (int i = 0; i < blocks.length / 2; i++) {
                            newBag.addHighlight(blocks[2 * i], blocks[2 * i + 1], attribs);
                        }
                    } catch (BadLocationException e) {
                        LOG.log(Level.WARNING, e.getMessage(), e);
                    }
                }

                b.setHighlights(newBag);
            }
        });
    }
    
    private boolean isEnabled() {
        Object prop = EditorFindSupport.getInstance().getFindProperty(
            EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
        return (prop instanceof Boolean) && ((Boolean) prop).booleanValue();
    }
    
    private AttributeSet getAttribs() {
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs.getFontColors(FontColorNames.HIGHLIGHT_SEARCH_COLORING);
        return attribs == null ? SimpleAttributeSet.EMPTY : attribs;
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

    @MimeRegistration(mimeType = "", service = HighlightsLayerFactory.class)
    public static final class FactoryImpl implements HighlightsLayerFactory {

        public final static class SearchBlockHighlighting extends BlockHighlighting {
            public SearchBlockHighlighting(String layerId, JTextComponent component) {
                super(layerId,component);
                EditorFindSupport.getInstance().hookLayer(this, component);
            }
        }

        @Override
        public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
            ArrayList<HighlightsLayer> layers = new ArrayList<>();
            layers.add(HighlightsLayer.create(
                    TextSearchHighlighting.LAYER_TYPE_ID,
                    ZOrder.SHOW_OFF_RACK.forPosition(200),
                    true,
                    new TextSearchHighlighting(context.getComponent())));

            layers.add(HighlightsLayer.create(
                    BLOCK_SEARCH_LAYER,
                    ZOrder.SHOW_OFF_RACK.forPosition(100),
                    true,
                    new SearchBlockHighlighting(BLOCK_SEARCH_LAYER, context.getComponent())));

            layers.add(HighlightsLayer.create(
                    INC_SEARCH_LAYER,
                    ZOrder.SHOW_OFF_RACK.forPosition(300),
                    true,
                    new SearchBlockHighlighting(INC_SEARCH_LAYER, context.getComponent())));

            return layers.toArray(new HighlightsLayer[layers.size()]);
        }

    } // End of FactoryImpl class
}
