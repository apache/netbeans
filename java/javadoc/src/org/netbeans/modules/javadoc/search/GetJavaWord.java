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

package org.netbeans.modules.javadoc.search;

import org.openide.windows.TopComponent;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;


/**
 * Tries to find actual focused java word.
 *
 * @author Petr Hrebejk
 */
final class GetJavaWord extends Object {


    static String getCurrentJavaWord() {
        Node[] n = TopComponent.getRegistry ().getActivatedNodes ();

        if (n.length == 1) {
            EditorCookie ec = n[0].getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                JEditorPane pane = NbDocument.findRecentEditorPane(ec);
                return pane == null ? null : forPane(pane);
            }
        }

        return null;
    }
    
    static String forPane(JEditorPane p) {
        if (p == null) return null;
 
        String selection = p.getSelectedText ();
 
        if ( selection != null && selection.length() > 0 ) {
            return selection;
        } else {
 
            // try to guess which word is underneath the caret's dot.
 
            Document doc = p.getDocument();
            Element lineRoot;
 
            if (doc instanceof StyledDocument) {
                lineRoot = NbDocument.findLineRootElement((StyledDocument)doc);
            } else {
                lineRoot = doc.getDefaultRootElement();
            }
            int dot = p.getCaret().getDot();
            Element line = lineRoot.getElement(lineRoot.getElementIndex(dot));
 
            if (line == null) return null;
 
            String text = null;
            try {
                text = doc.getText(line.getStartOffset(),
                    line.getEndOffset() - line.getStartOffset());
            } catch (BadLocationException e) {
                return null;
            }
            
            if ( text == null )
                return null;
            int pos = dot - line.getStartOffset();

            if ( pos < 0 || pos >= text.length() )
                return null;

            int bix, eix;

            for( bix = Character.isJavaIdentifierPart( text.charAt( pos ) ) ? pos : pos - 1;
                    bix >= 0 && Character.isJavaIdentifierPart( text.charAt( bix ) ); bix-- );
            for( eix = pos; eix < text.length() && Character.isJavaIdentifierPart( text.charAt( eix )); eix++ );

            return bix == eix ? null : text.substring( bix + 1, eix  );
        }
    }
}
