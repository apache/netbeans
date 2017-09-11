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
package org.netbeans.modules.languages.features;

import javax.swing.text.Document;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Jan Jancura
 */
public class GLFHighlightsLayerFactory implements HighlightsLayerFactory {

    public HighlightsLayer[] createLayers (Context context) {
        Document document = context.getDocument ();
//        try {
//            if (LanguagesManager.getDefault ().getLanguage (mimeType).getParser () == null)
//                return null;
            return new HighlightsLayer[] {
                HighlightsLayer.create (
                    "GLF Semantic Coloring", 
                    ZOrder.SYNTAX_RACK.forPosition (10), 
                    false, 
                    new SemanticHighlightsLayer (document)
                ),
                HighlightsLayer.create (
                    "GLF Languages Coloring", 
                    ZOrder.SYNTAX_RACK.forPosition (11), 
                    false, 
                    new LanguagesHighlightsLayer (document)
                ),
                HighlightsLayer.create (
                    "GLF Token Highlighting", 
                    ZOrder.SHOW_OFF_RACK.forPosition (0), 
                    false, 
                    new TokenHighlightsLayer (document)
                )
            };
//        } catch (LanguageDefinitionNotFoundException ex) {
//            return null;
//        }
    }
}
