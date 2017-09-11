/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.editor;

import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.jshell.model.ConsoleEvent;
import org.netbeans.modules.jshell.model.ConsoleListener;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleSection;
import static org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE;
import static org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EOL;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;

/**
 * A special layer, which repains GuardedBlock highlighting on non-input console sections
 * back to editor's default attributes.
 * 
 * @author sdedic
 */
public class GuardedBlockSuppressLayer extends AbstractHighlightsContainer implements ConsoleListener {
    private static final String LAYER_ID = GuardedBlockSuppressLayer.class.getName();

    private final Document        document;
    private final PositionsBag    bag;
    private final String          mimeType;
    
    private ConsoleModel    model;
    private volatile boolean       invalid = true;
    
    public GuardedBlockSuppressLayer(JTextComponent component) {
        this.document = component.getDocument();
        this.bag = new PositionsBag(document);
        this.mimeType = "text/x-repl"; // NOI18N
    }
    
    private synchronized boolean updateConsoleModel() {
        ConsoleModel nm = ConsoleModel.get(document);
        if (model != nm) {
            if (model != null) {
                model.removeConsoleListener(this);
            }
            if (nm != null) {
                nm.addConsoleListener(this);
            }
        }
        this.model = nm;
        return nm != null;
    }

    private AttributeSet getAttribs(String coloringName, boolean extendsEol, boolean extendsEmptyLine) {
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
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
    
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (!updateConsoleModel()) {
            return HighlightsSequence.EMPTY;
        }
        if (invalid) {
            bag.clear();
            AttributeSet def = getAttribs("default", true, true);
            for (ConsoleSection s : model.getSections()) {
                if (s.getType().input) {
                    continue;
                }
                try {
                    bag.addHighlight(
                            document.createPosition(s.getStart()),
                            document.createPosition(s.getEnd()),
                            def
                    );
                } catch (BadLocationException ex) {
                }
            }
            invalid = false;
        }
        return bag.getHighlights(startOffset, endOffset);
    }
    
    private void fireHighlightsChanged(ConsoleEvent ev) {
        int min = document.getLength();
        int max = 0;
        
        List<ConsoleSection> secs = ev.getAffectedSections();
        for (ConsoleSection s : secs) {
            if (s.getType().input) {
                continue;
            }
            min = Math.min(min, s.getStart());
            max = Math.max(max, s.getEnd());
        }
        if (max < min) {
            return;
        }
        synchronized (this) {
            invalid = true;
        }
        fireHighlightsChange(min, max);
    }

    @Override
    public void sectionCreated(ConsoleEvent e) {
        fireHighlightsChanged(e);
    }

    @Override
    public void sectionUpdated(ConsoleEvent e) {
        fireHighlightsChanged(e);
    }

    @Override
    public void executing(ConsoleEvent e) {
    }

    @Override
    public void closed(ConsoleEvent e) {
    }
    
    @MimeRegistration(service = HighlightsLayerFactory.class, mimeType = "text/x-repl")
    public static class HLF implements HighlightsLayerFactory {

        @Override
        public HighlightsLayer[] createLayers(Context context) {
            final Document d = context.getDocument();
            final JTextComponent c = context.getComponent();
            
            return new HighlightsLayer[] {
                HighlightsLayer.create(
                    GuardedBlockSuppressLayer.LAYER_ID, 
                    ZOrder.BOTTOM_RACK.forPosition(10), 
                    true,  // fixedSize
                    new GuardedBlockSuppressLayer(c)
                )
            };
        }
        
    }
}
