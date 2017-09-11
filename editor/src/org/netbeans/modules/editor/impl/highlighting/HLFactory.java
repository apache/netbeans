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

package org.netbeans.modules.editor.impl.highlighting;

import java.util.ArrayList;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Vita Stejskal
 */
public final class HLFactory implements HighlightsLayerFactory {
    
    /** Creates a new instance of HLFactory */
    public HLFactory() {
    }

    public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
        ArrayList<HighlightsLayer> layers = new ArrayList<HighlightsLayer>();
        
        final Document d = context.getDocument();
        final JTextComponent c = context.getComponent();
        final String mimeType = getMimeType(c, d);
        
        layers.add(HighlightsLayer.create(
            GuardedBlocksHighlighting.LAYER_TYPE_ID, 
            ZOrder.BOTTOM_RACK, 
            true,  // fixedSize
            new GuardedBlocksHighlighting(d, mimeType)
        ));
        
        layers.add(HighlightsLayer.create(
            ComposedTextHighlighting.LAYER_TYPE_ID, 
            ZOrder.TOP_RACK, 
            true,  // fixedSize
            new ComposedTextHighlighting(c, d, mimeType)
        ));

        layers.add(HighlightsLayer.create(
            AnnotationsHighlighting.LAYER_TYPE_ID,
            ZOrder.DEFAULT_RACK,
            true,  // fixedSize
            new AnnotationsHighlighting(d)
        ));


        if (!new TokenHierarchyActiveRunnable(context.getDocument()).isActive()) {
            // There is no lexer yet, we will use this layer for backwards compatibility
            layers.add(HighlightsLayer.create(
                NonLexerSyntaxHighlighting.LAYER_TYPE_ID, 
                ZOrder.SYNTAX_RACK, 
                true,  // fixedSize
                new NonLexerSyntaxHighlighting(d, mimeType)
            ));
        }
        
        return layers.toArray(new HighlightsLayer[layers.size()]);
    }
    
    private static String getMimeType(JTextComponent c, Document d) {
        String mimeType = (String) d.getProperty("mimeType"); //NOI18N
        
        if (mimeType == null) {
            mimeType = c.getUI().getEditorKit(c).getContentType();
        }
        
        return mimeType == null ? "" : mimeType; //NOI18N
    }

    private static final class TokenHierarchyActiveRunnable implements Runnable {

        private Document doc;

        private boolean tokenHierarchyActive;

        TokenHierarchyActiveRunnable(Document doc) {
            this.doc = doc;
        }

        boolean isActive() {
            doc.render(this);
            return tokenHierarchyActive;
        }

        public void run() {
            tokenHierarchyActive = TokenHierarchy.get(doc).isActive();
        }

    }

}
