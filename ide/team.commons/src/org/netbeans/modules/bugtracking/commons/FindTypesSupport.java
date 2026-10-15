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
package org.netbeans.modules.bugtracking.commons;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
final class FindTypesSupport implements MouseMotionListener, MouseListener {
    
    private static final Pattern JAVA_CLASS_NAME_PATTERN = Pattern.compile("\\.?([a-z0-9\\.]*)([A-Z]\\w+)+");  // NOI18N
    private static final String HIGHLIGHTS_PROPERTY = "highlights.property";                                             // NOI18N
    private static final String PREV_HIGHLIGHT_PROPERTY = "prev.highlights.property";                                    // NOI18N
    private static final String PREV_HIGHLIGHT_ATTRIBUTES = "prev.highlights.attributes";                                    // NOI18N
            
    private static FindTypesSupport instance;
    private final Style defStyle;
    private final PopupMenu popupMenu;

    private FindTypesSupport() {
        defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        popupMenu = new PopupMenu();
    }
    
    public static FindTypesSupport getInstance() {
        if (instance == null) {
            instance = new FindTypesSupport();            
        }
        return instance;
    }

    private Highlight getHighlight(JTextPane pane, int offset) {
        List<Highlight> highlights = getHighlights(pane);
        Highlight h = null;
        for (int i = 0; i < highlights.size(); i++) {
            h = highlights.get(i);
            if(h.startOffset <= offset && h.endOffset >= offset) {
                break;
            } 
            h = null;
        }
        return h;
    }

    private List<Highlight> getHighlights(JTextPane pane) {
        List<Highlight> highlights = (List<Highlight>) pane.getClientProperty(HIGHLIGHTS_PROPERTY);
        if(highlights == null) {
            highlights = new LinkedList<Highlight>();
            pane.putClientProperty(HIGHLIGHTS_PROPERTY, highlights);
        }
        return highlights;
    }

    private class Highlight {
        int startOffset;
        int endOffset;
        public Highlight(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Highlight other = (Highlight) obj;
            if (this.startOffset != other.startOffset) {
                return false;
            }
            if (this.endOffset != other.endOffset) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + this.startOffset;
            hash = 71 * hash + this.endOffset;
            return hash;
        }
    }
    
    public void register(final JTextPane pane) {
        IDEServices ideServices = Support.getInstance().getIDEServices();
        if(ideServices == null || !ideServices.providesJumpTo()) {
            return;
        }
        long t = System.currentTimeMillis();
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StyledDocument doc = pane.getStyledDocument();
                    Style hlStyle = doc.addStyle("regularBlue-findtype", defStyle);     // NOI18N
                    hlStyle.addAttribute(HyperlinkSupport.TYPE_ATTRIBUTE, new TypeLink());
                    StyleConstants.setForeground(hlStyle, UIUtils.getLinkColor());
                    StyleConstants.setUnderline(hlStyle, true);            

                    List<Integer> l = Collections.emptyList();
                    try {
                        l = getHighlightOffsets(doc.getText(0, doc.getLength()));
                    } catch (BadLocationException ex) {
                        Support.LOG.log(Level.SEVERE, null, ex);
                    }
                    List<Highlight> highlights = new ArrayList<Highlight>(l.size());
                    for (int i = 0; i < l.size(); i++) {
                        highlights.add(new Highlight(l.get(i), l.get(++i)));
                    }
                    pane.putClientProperty(HIGHLIGHTS_PROPERTY, highlights);
                    pane.removeMouseMotionListener(FindTypesSupport.this);
                    pane.addMouseMotionListener(FindTypesSupport.this);
                    pane.removeMouseListener(FindTypesSupport.this);
                    pane.addMouseListener(FindTypesSupport.this);
                }
            });
            
        } finally {
            Support.LOG.log(Level.FINE, "{0}.register took  {1}", new Object[]{this.getClass().getName(), System.currentTimeMillis() - t}); // NOI18N
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) { }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        JTextPane pane = (JTextPane)e.getSource();
        StyledDocument doc = pane.getStyledDocument();

        int offset = pane.viewToModel(e.getPoint());
        Element elem = doc.getCharacterElement(offset);

        Highlight h = getHighlight(pane, offset);
        Highlight prevHighlight = (Highlight) pane.getClientProperty(PREV_HIGHLIGHT_PROPERTY);
        AttributeSet prevAs = (AttributeSet) pane.getClientProperty(PREV_HIGHLIGHT_ATTRIBUTES);
//        if(h != null && h.equals(prevHighlight)) {
//            return; // nothing to do
//        } else 
        if(prevHighlight != null && prevAs != null) {
            doc.setCharacterAttributes(prevHighlight.startOffset, prevHighlight.endOffset - prevHighlight.startOffset, prevAs, true);
            pane.putClientProperty(PREV_HIGHLIGHT_PROPERTY, null);
            pane.putClientProperty(PREV_HIGHLIGHT_ATTRIBUTES, null);
        }

        int modifiers = e.getModifiers() | e.getModifiersEx();
        if ( (modifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK ||
             (modifiers & InputEvent.META_DOWN_MASK) == InputEvent.META_DOWN_MASK) 
        {            
            AttributeSet as = elem.getAttributes();
            if (StyleConstants.isUnderline(as)) {
                // do not underline whats already underlined
                return;
            }

            Font font = doc.getFont(as);
            FontMetrics fontMetrics = pane.getFontMetrics(font);
            try {
                Rectangle rectangle = new Rectangle(
                        pane.modelToView(elem.getStartOffset()).x,
                        pane.modelToView(elem.getStartOffset()).y,
                        fontMetrics.stringWidth(doc.getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset())),
                        fontMetrics.getHeight());

                if (h != null && offset < elem.getEndOffset() - 1 && rectangle.contains(e.getPoint())) {
                    Style hlStyle = doc.getStyle("regularBlue-findtype");               // NOI18N

                    pane.putClientProperty(PREV_HIGHLIGHT_ATTRIBUTES, as.copyAttributes());
                    doc.setCharacterAttributes(h.startOffset, h.endOffset - h.startOffset, hlStyle, true);
    //                doc.setCharacterAttributes(h.startOffset, h.endOffset - h.startOffset, as.copyAttributes(), true);
                    pane.putClientProperty(PREV_HIGHLIGHT_PROPERTY, h);
                } 
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    static List<Integer> getHighlightOffsets(String txt) {
        LinkedList<Integer> result = new LinkedList<Integer>();
        if ( txt == null) {
            return Collections.emptyList();
        }

        Matcher m  = JAVA_CLASS_NAME_PATTERN.matcher(txt);
        while( m.find() ) {
           int last = m.end(); 
           int start = last - (m.group(1) != null ? m.group(1).length() : 0) - m.group(2).length();
           result.add(start);
           result.add(last);
        }
        return result;
    }

    private Element element(MouseEvent e) {
        JTextPane pane = (JTextPane)e.getSource();
        StyledDocument doc = pane.getStyledDocument();
        return doc.getCharacterElement(pane.viewToModel(e.getPoint()));
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            if (SwingUtilities.isLeftMouseButton(e)) {
                Element elem = element(e);
                AttributeSet as = elem.getAttributes();
                TypeLink action = (TypeLink) as.getAttribute(HyperlinkSupport.TYPE_ATTRIBUTE);
                if (action != null) {
                    try {
                        String name = elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
                        int idx = name.lastIndexOf(".");
                        if(idx > -1 && name.length() > idx) {
                            name = name.substring(idx + 1);
                        }
                        action.jumpTo(name);
                    } catch(BadLocationException ex) {
                        Support.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            } else if (SwingUtilities.isRightMouseButton(e)) {
                popupMenu.clickPoint.setLocation(e.getPoint());
                popupMenu.pane = (JTextPane)e.getSource();
                popupMenu.show((JTextPane)e.getSource(), e.getPoint().x, e.getPoint().y);
            }
        } catch(Exception ex) {
            Support.LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
    
    private class TypeLink {
        public void jumpTo(String resource) {
            IDEServices ideServices = Support.getInstance().getIDEServices();
            if(ideServices != null) {
                ideServices.jumpTo(resource, NbBundle.getMessage(FindTypesSupport.class, "LBL_FindType"));  // NOI18N
            }
        }
    }
    
    private class PopupMenu extends JPopupMenu {

        /*
         * Holds the location of where the user invoked the pop-up menu.
         * It must be remembered before calling super.show(...) because
         * the method show() may change the location of the pop-up menu,
         * so the original location might not be available.
         */
        private final Point clickPoint = new Point();
        private JTextPane pane;
        

        @Override
        public void show(Component invoker, int x, int y) {
            clickPoint.setLocation(x, y);
            super.show(invoker, x, y);
        }        
        
        @Override
        public void setVisible(boolean b) {
            if (b) {
                StyledDocument doc = pane.getStyledDocument();
                int offset = pane.viewToModel(clickPoint);
                Element elem = doc.getCharacterElement(offset);
                AttributeSet as = elem.getAttributes();
                final TypeLink link = (TypeLink) as.getAttribute(HyperlinkSupport.TYPE_ATTRIBUTE);
                if (link != null) {
                    try {
                        String name = elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
                        int idx = name.lastIndexOf(".");
                        final String shortname = idx > -1 && name.length() > idx ? name.substring(idx + 1) : name;
                        add(new JMenuItem(new AbstractAction(NbBundle.getMessage(FindTypesSupport.class, "MSG_GotoType", name)) { // NOI18N
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                link.jumpTo(shortname);
                            }
                        }));
                        if(name.length() > shortname.length()) {
                            final String path = name.replace(".", "/") + ".java"; // NOI18N
                            add(new JMenuItem(new AbstractAction(NbBundle.getMessage(FindTypesSupport.class, "MSG_OpenType", name)) { // XXX + ".java" ??? // NOI18N
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    StackTraceSupport.open(path, -1);
                                }
                            }));
                        }
//                        add(new JMenuItem(new AbstractAction("Find in projects") {
//                            @Override
//                            public void actionPerformed(ActionEvent e) {
//                                Action a = Actions.forID("Edit", "org.netbeans.modules.search.FindInFilesAction");
//                                if(a instanceof ContextAwareAction) {
// //                                    a = ((ContextAwareAction)a).createContextAwareInstance(Lookups.singleton(ctx));
//                                }            
//                                a.actionPerformed(null);
//                            }
//                        }));
                    } catch(Exception ex) {
                        Support.LOG.log(Level.SEVERE, null, ex);
                    }
                    super.setVisible(true);
                } else {
                    super.setVisible(false);
                }
            } else {
                super.setVisible(false);
                removeAll();
            }
        }        
    }    

    
}
