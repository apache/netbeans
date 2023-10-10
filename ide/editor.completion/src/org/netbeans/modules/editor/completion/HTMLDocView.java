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


package org.netbeans.modules.editor.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.editor.EditorUI;
import org.openide.util.Exceptions;

/**
 *  HTML documentation view.
 *  Javadoc content is displayed in JEditorPane pane using HTMLEditorKit.
 *
 *  @author  Martin Roskanin
 *  @since   03/2002
 */
public class HTMLDocView extends JEditorPane {
    
    private HTMLEditorKit htmlKit;
    
    private int selectionAnchor = 0; // selection-begin position
    private Object highlight = null; // selection highlight
    
    /** Creates a new instance of HTMLJavaDocView */
    public HTMLDocView(Color bgColor) {
        setEditable(false);
        setFocusable(true);
        setBackground(bgColor);
        setMargin(new Insets(0, 3, 3, 3));
        
        //add listeners for selection support
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }
            public void mousePressed(MouseEvent e) {
                getHighlighter().removeAllHighlights();
                selectionAnchor = positionCaret(e);
                try {
                    highlight = getHighlighter().addHighlight(selectionAnchor, selectionAnchor, DefaultHighlighter.DefaultPainter);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if(getSelectedText() == null){
                    getHighlighter().removeAllHighlights();
                }
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                try {
                    if (highlight == null) {
                        getHighlighter().removeAllHighlights();
                        selectionAnchor = positionCaret(e);
                        highlight = getHighlighter().addHighlight(selectionAnchor, selectionAnchor, DefaultHighlighter.DefaultPainter);
                    } else if (selectionAnchor <= positionCaret(e)) {
                        getHighlighter().changeHighlight(highlight, selectionAnchor, positionCaret(e));
                    } else {
                        getHighlighter().changeHighlight(highlight, positionCaret(e), selectionAnchor);
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            public void mouseMoved(MouseEvent e) {}
        });
        putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );
    }

    private int positionCaret(MouseEvent event) {
        int positionOffset = this.viewToModel(event.getPoint());
        return positionOffset;
    }

    @Override
    public boolean isFocusable() {
        return false;
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

            if (htmlKit.getStyleSheet().getStyleSheets() != null) {
//                htmlKit.getStyleSheet().removeStyle("body");
                setBodyFontInCSS();
                return htmlKit;
            }
            setBodyFontInCSS();
        }
        return htmlKit;
    }
    
    private void setBodyFontInCSS() {
        javax.swing.text.html.StyleSheet css =
                new javax.swing.text.html.StyleSheet();
        Font editorFont = new EditorUI().getDefaultColoring().getFont();
        // do not use monospaced font, just adjust fontsize
        Font useFont =
            new Font(getFont().getFamily(), Font.PLAIN, editorFont.getSize());
        setFont(useFont);
        try {
            css.addRule(new StringBuilder("body, div { font-size: ").append(useFont.getSize()) // NOI18N
                    .append("; font-family: ").append(useFont.getFamily()).append(";}").toString()); // NOI18N
        } catch (Exception e) {
        }
        css.addStyleSheet(htmlKit.getStyleSheet());
        htmlKit.setStyleSheet(css);

    }
}
