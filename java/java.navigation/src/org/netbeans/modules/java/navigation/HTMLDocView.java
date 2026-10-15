/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.java.navigation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;

/**
 *  HTML documentation view.
 *  Javadoc content is displayed in JEditorPane pane using HTMLEditorKit.
 *
 *  @author  Martin Roskanin
 *  @since   03/2002
 */
class HTMLDocView extends JEditorPane {
    
    private HTMLEditorKit htmlKit;
    
    /** Creates a new instance of HTMLJavaDocView */
    public HTMLDocView(Color bgColor) {
        setEditable(false);
        setFocusable(true);
        setBackground(bgColor);
        setMargin(new Insets(0,3,3,3));
        putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );
    }

    /**
     * Get font or null if attributes do not provide enough info for font composition.
     * Shamelessly copied from Editor implementation -- make an API/utility in Editor settings ?
     * 
     * @param attrs non-null attrs.
     * @return font or null.
     */
    public static Font getFont(AttributeSet attrs) {
        Font font = null;
        if (attrs != null) {
            String fontName = (String) attrs.getAttribute(StyleConstants.FontFamily);
            Integer fontSizeInteger = (Integer) attrs.getAttribute(StyleConstants.FontSize);
            if (fontName != null && fontSizeInteger != null) {
                Boolean bold = (Boolean) attrs.getAttribute(StyleConstants.Bold);
                Boolean italic = (Boolean) attrs.getAttribute(StyleConstants.Italic);
                int fontStyle = Font.PLAIN;
                if (bold != null && bold) {
                    fontStyle |= Font.BOLD;
                }
                if (italic != null && italic) {
                    fontStyle |= Font.ITALIC;
                }
                font = new Font(fontName, fontStyle, fontSizeInteger);
            }
        }
        return font;
    }

    /** Sets the javadoc content as HTML document */
    public void setContent(final String content, final String reference) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                Reader in = new StringReader("<HTML><BODY>"+content+"</BODY></HTML>");//NOI18N                
                try{
                    setContentType("text/html"); //NOI18N
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
    
    @Override
    protected EditorKit createDefaultEditorKit() {
        // it is extremelly slow to init it
        if (htmlKit == null){
            htmlKit= new HTMLEditorKit ();
            setEditorKit(htmlKit);

            // #250761: set the same font as the base in the editor.
            FontColorSettings font = MimeLookup.getLookup("text/x-java").lookup(FontColorSettings.class);
            Font f = null;
            if (font != null) {
                AttributeSet set = font.getFontColors(FontColorNames.DEFAULT_COLORING);
                f = getFont(set);
                if (f != null) {
                    setFont(f);
                }
            }
            // override the Swing default CSS to make the HTMLEditorKit use the
            // same font as the rest of the UI.
            
            // XXX the style sheet is shared by all HTMLEditorKits.  We must
            // detect if it has been tweaked by ourselves or someone else
            // (template description for example) and avoid doing the same
            // thing again
            
            if (htmlKit.getStyleSheet().getStyleSheets() != null || f == null)
                return htmlKit;
            
            javax.swing.text.html.StyleSheet css = new javax.swing.text.html.StyleSheet();
            setFont(f);
            css.addRule(new StringBuffer("body { font-size: ").append(f.getSize()) // NOI18N
                        /*.append("; font-family: ").append(f.getName())*/ .append("; }").toString()); // NOI18N
            css.addStyleSheet(htmlKit.getStyleSheet());
            htmlKit.setStyleSheet(css);
        }
        return htmlKit;
    }
}
