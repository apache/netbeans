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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.web.beans.navigation;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

/**
 *  HTML documentation view.
 *  Javadoc content is displayed in JEditorPane pane using HTMLEditorKit.
 *  
 *  Copy of HTMLDocView in java.naviation.
 *
 *  @author  ads
 */
class HTMLDocView extends JEditorPane {
    
    private static final long serialVersionUID = 6743108080998604204L;
    
    private HTMLEditorKit htmlKit;
    
    /** Creates a new instance of HTMLJavaDocView */
    public HTMLDocView(Color bgColor) {
        setEditable(false);
        setFocusable(true);
        setBackground(bgColor);
        setMargin(new Insets(0,3,3,3));
    }

    /** Sets the javadoc content as HTML document */
    public void setContent(final String content, final String reference) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                Reader in = new StringReader("<HTML><BODY>"+content+"</BODY></HTML>");//NOI18N                
                try{
                    Document doc = getDocument();
                    doc.remove(0, doc.getLength());
                    getEditorKit().read(in, getDocument(), 0);  //!!! still too expensive to be called from AWT
                    setCaretPosition(0);
                    if (reference != null) {
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run(){
                                scrollToReference(reference);
                            }
                        });
                    } else {
                        scrollRectToVisible(new Rectangle(0,0,0,0));
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(BadLocationException ble){
                    ble.printStackTrace();
                }
            }
        });
    }
    
    protected EditorKit createDefaultEditorKit() {
        // it is extremelly slow to init it
        if (htmlKit == null){
            htmlKit= new HTMLEditorKit ();
            setEditorKit(htmlKit);

            // override the Swing default CSS to make the HTMLEditorKit use the
            // same font as the rest of the UI.
            
            // XXX the style sheet is shared by all HTMLEditorKits.  We must
            // detect if it has been tweaked by ourselves or someone else
            // (template description for example) and avoid doing the same
            // thing again
            
            if (htmlKit.getStyleSheet().getStyleSheets() != null)
                return htmlKit;
            
            javax.swing.text.html.StyleSheet css = new javax.swing.text.html.StyleSheet();
            java.awt.Font f = getFont();
            css.addRule(new StringBuffer("body { font-size: ").append(f.getSize()) // NOI18N
                        .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css.addStyleSheet(htmlKit.getStyleSheet());
            htmlKit.setStyleSheet(css);
        }
        return htmlKit;
    }
}
