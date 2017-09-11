/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.semantic;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase.ErrorDescriptionSetter;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class SemanticHighlighter extends SemanticHighlighterBase {

    protected boolean process(final CompilationInfo info, final Document doc) {
        long start = System.currentTimeMillis();
        boolean ret = process(info, doc, ERROR_DESCRIPTION_SETTER);
        Logger.getLogger("TIMER").log(Level.FINE, "Semantic",
            new Object[] {NbEditorUtilities.getFileObject(doc), System.currentTimeMillis() - start});
        return ret;
    }
    
    static ErrorDescriptionSetter ERROR_DESCRIPTION_SETTER = new ErrorDescriptionSetter() {
        
        public void setErrors(Document doc, List<ErrorDescription> errors, List<TreePathHandle> allUnusedImports) {}
        
        public void setHighlights(final Document doc, final Collection<int[]> highlights) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    OffsetsBag bag = new OffsetsBag(doc);
                    Coloring unused = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.UNUSED);
                    for (int[] highlight : highlights) {
                        bag.addHighlight(highlight[0], highlight[1], ColoringManager.getColoringImpl(unused));
                    }
                    getImportHighlightsBag(doc).setHighlights(bag);
                }
            });
        }
    
        public void setColorings(final Document doc, final Map<Token, Coloring> colorings) {
            SwingUtilities.invokeLater(new Runnable () {
                public void run() {
                    Map<Token, Coloring> oldColors = LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, doc).getColorings();
                    Map<Token, Coloring> removedTokens = new IdentityHashMap<>(oldColors);
                    Set<Token> addedTokens = new HashSet<>();
                    for (Map.Entry<Token, Coloring> entrySet : colorings.entrySet()) {
                        Token t = entrySet.getKey();
                        Coloring oldColoring = removedTokens.remove(t);
                        if (oldColoring == null || !oldColoring.equals(entrySet.getValue())) {
                            addedTokens.add(t);
                        }
                    }
                    LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, doc).setColorings(colorings, addedTokens, removedTokens.keySet());
                }                
            });            
        }
    };

    private static final Object KEY_UNUSED_IMPORTS = new Object();
    static OffsetsBag getImportHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(KEY_UNUSED_IMPORTS);
        
        if (bag == null) {
            doc.putProperty(KEY_UNUSED_IMPORTS, bag = new OffsetsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
//                TimesCollector.getDefault().reportReference(((DataObject) stream).getPrimaryFile(), "ImportsHighlightsBag", "[M] Imports Highlights Bag", bag);
            }
        }
        
        return bag;
    }

}
