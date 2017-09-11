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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.semantic;

import org.netbeans.modules.java.editor.rename.InstantRenamePerformer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory.Context;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Jan Lahoda
 */
public class HighlightsLayerFactoryImpl implements HighlightsLayerFactory {
    
    public HighlightsLayer[] createLayers(Context context) {
        LexerBasedHighlightLayer semantic = LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, context.getDocument());
        
        semantic.clearColoringCache();
        
        return new HighlightsLayer[] {
            HighlightsLayer.create(SemanticHighlighter.class.getName() + "-1", ZOrder.SYNTAX_RACK.forPosition(1000), false,semantic),
            HighlightsLayer.create(SemanticHighlighter.class.getName() + "-2", ZOrder.SYNTAX_RACK.forPosition(1500), false, SemanticHighlighter.getImportHighlightsBag(context.getDocument())),
            //the mark occurrences layer should be "above" current row and "below" the search layers:
            HighlightsLayer.create(MarkOccurrencesHighlighter.class.getName(), ZOrder.SHOW_OFF_RACK.forPosition(20), true, MarkOccurrencesHighlighter.getHighlightsBag(context.getDocument())),
            //"above" mark occurrences, "below" search layers:
            HighlightsLayer.create(InstantRenamePerformer.class.getName(), ZOrder.SHOW_OFF_RACK.forPosition(25), true, InstantRenamePerformer.getHighlightsBag(context.getDocument())),
        };
    }

}
