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

import java.util.ArrayList;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 * The factory for editor default highlighting layers.
 * 
 * @author Vita Stejskal
 */
public class Factory implements HighlightsLayerFactory {

    /** A unique identifier of the block search layer type. */
    public static final String BLOCK_SEARCH_LAYER = "org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting/BLOCK_SEARCH"; //NOI18N
    
    /** A unique identifier of the incremental search layer type. */
    public static final String INC_SEARCH_LAYER = "org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting/INC_SEARCH"; //NOI18N
    
    /** Creates a new instance of Factory */
    public Factory() {
    }

    public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
        ArrayList<HighlightsLayer> layers = new ArrayList<HighlightsLayer>();
        
        layers.add(HighlightsLayer.create(
            ReadOnlyFilesHighlighting.LAYER_TYPE_ID,
            ZOrder.BOTTOM_RACK.forPosition(-1000),
            true,
            new ReadOnlyFilesHighlighting(context.getDocument()))
        );

        layers.add(HighlightsLayer.create(
            CaretBasedBlockHighlighting.CaretRowHighlighting.LAYER_TYPE_ID,
            ZOrder.CARET_RACK,
            true,
            new CaretBasedBlockHighlighting.CaretRowHighlighting(context.getComponent()))
        );

        layers.add(HighlightsLayer.create(
            INC_SEARCH_LAYER, 
            ZOrder.SHOW_OFF_RACK.forPosition(300),
            true,
            new BlockHighlighting(INC_SEARCH_LAYER, context.getComponent()))
        );

        layers.add(HighlightsLayer.create(
            CaretBasedBlockHighlighting.TextSelectionHighlighting.LAYER_TYPE_ID,
            ZOrder.SHOW_OFF_RACK.forPosition(500), 
            true, 
            new CaretBasedBlockHighlighting.TextSelectionHighlighting(context.getComponent()))
        );

        // If there is a lexer for the document create lexer-based syntax highlighting
        if (TokenHierarchy.get(context.getDocument()) != null) {
            layers.add(HighlightsLayer.create(
                SyntaxHighlighting.LAYER_TYPE_ID,
                ZOrder.SYNTAX_RACK,
                true,
                new SyntaxHighlighting(context.getDocument()))
            );
        }
        
        layers.add(HighlightsLayer.create(
            WhitespaceHighlighting.LAYER_TYPE_ID,
            ZOrder.CARET_RACK.forPosition(-100), // Below CaretRowHighlighting
            true, // fixed size
            new WhitespaceHighlighting(context.getComponent()))
        );
        
        layers.add(HighlightsLayer.create(
            CaretOverwriteModeHighlighting.LAYER_TYPE_ID,
            ZOrder.TOP_RACK.forPosition(100),
            true, // fixed size
            new CaretOverwriteModeHighlighting(context.getComponent()))
        );

        return layers.toArray(new HighlightsLayer [layers.size()]);
    }
    
}
