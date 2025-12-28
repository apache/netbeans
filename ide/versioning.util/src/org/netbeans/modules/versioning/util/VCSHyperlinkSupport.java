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

package org.netbeans.modules.versioning.util;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

/**
 * Implementation provides hyperlink support for VCS anotation bar and history views
 *
 * @author Tomas Stupka
 */
public class VCSHyperlinkSupport {
    private static Logger LOG = Logger.getLogger(VCSHyperlinkSupport.class.getName());
    private final Map<String, List<Hyperlink>> linkers = new HashMap<>();

    public <T extends Hyperlink> T getLinker(Class<T> t, int idx) {
        return getLinker(t, Integer.toString(idx));
    }

    public <T extends Hyperlink> T getLinker(Class<T> t, String idx) {
        List<Hyperlink> list = linkers.get(idx);
        if(list == null) return null;
        for (Hyperlink linker : list) {
            if(linker.getClass() == t) return (T) linker;
        }
        return null;
    }

    public void add(Hyperlink l, int idx) {
        add(l, Integer.toString(idx));
    }

    public void add(Hyperlink l, String idx) {
        if(l == null) return;
        List<Hyperlink> list = linkers.get(idx);
        if(list == null) {
            list = new ArrayList<>();
        }
        list.add(l);
        linkers.put(idx, list);
    }

    public <T extends Hyperlink> void remove(Class<T> c, String idx) {
        if(c == null) return;
        List<Hyperlink> list = linkers.get(idx);
        if(list == null) return;
        Iterator<Hyperlink> it = list.iterator();
        while(it.hasNext()) {
            if(it.next().getClass() == c) {
                it.remove();
                return;
            }
        }
    }

    public void remove(Hyperlink l, String idx) {
        if(l == null) return;
        List<Hyperlink> list = linkers.get(idx);
        if(list == null) return;
        list.remove(l);
    }

    public boolean mouseMoved(Point p, JComponent component, int idx) {
        return mouseMoved(p, component, Integer.toString(idx));
    }

    public boolean mouseMoved(Point p, JComponent component, String idx) {
        List<Hyperlink> list = linkers.get(idx);
        if(list == null) return false;
        for (Hyperlink linker : list) {
            if(linker.mouseMoved(p, component)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseClicked(Point p, int idx) {
        return mouseClicked(p, Integer.toString(idx));
    }

    public boolean mouseClicked(Point p, String idx) {
        List<Hyperlink> list = linkers.get(idx);
        if(list == null) return false;
        for (Hyperlink linker : list) {
            if(linker.mouseClicked(p)) {
                return true;
            }
        }
        return false;
    }

    public void computeBounds(JTextPane textPane, int idx) {
        computeBounds(textPane, Integer.toString(idx));
    }

    public void computeBounds(JTextPane textPane, String idx) {
        List<Hyperlink> list = linkers.get(idx);
        if(list == null) return ;
        for (Hyperlink linker : list) {
            linker.computeBounds(textPane);
        }
    }

    public abstract static class Hyperlink {
        public abstract boolean mouseMoved(Point p, JComponent component);
        public abstract boolean mouseClicked(Point p);
        public abstract void computeBounds(JTextPane textPane);
    }

    public abstract static class StyledDocumentHyperlink extends Hyperlink {
        public abstract void insertString(StyledDocument sd, Style style) throws BadLocationException;
    }

    public static class IssueLinker extends StyledDocumentHyperlink {

        private Rectangle bounds[];
        private final int docstart[];
        private final int docend[];
        private final int start[];
        private final int end[];
        private final String text;
        private final VCSHyperlinkProvider hp;
        private final File root;
        private final int length;
        private final Style issueHyperlinkStyle;

        private IssueLinker(VCSHyperlinkProvider hp, Style issueHyperlinkStyle, File root, StyledDocument sd, String text, int[] spans) {
            this.length = spans.length / 2;
            this.docstart = new int[length];
            this.docend = new int[length];
            this.start = new int[length];
            this.end = new int[length];
            this.hp = hp;
            this.root = root;
            this.text = text;
            this.issueHyperlinkStyle = issueHyperlinkStyle;

            for (int i = 0; i < spans.length;) {
                int linkeridx = i / 2;
                int spanstart = spans[i++];
                int spanend = spans[i++];
                if(spanend < spanstart) {
                    LOG.warning("Hyperlink provider " + hp.getClass().getName() + " returns wrong spans [" + spanstart + "," + spanend + "]");
                    continue;
                }

                int doclen = sd.getLength();
                this.start[linkeridx] = spanstart;
                this.end[linkeridx] = spanend;
                this.docstart[linkeridx] = doclen + spanstart;
                this.docend[linkeridx] = doclen + spanend;
            }
        }

        public static IssueLinker create(VCSHyperlinkProvider hp, Style issueHyperlinkStyle, File root, StyledDocument sd, String text) {
            int[] spans = hp.getSpans(text);
            if (spans == null) {
                return null;
            }
            if(spans.length % 2 != 0) {
                // XXX more info and log only _ONCE_
                LOG.warning("Hyperlink provider " + hp.getClass().getName() + " returns wrong spans");
                return null;
            }
            if(spans.length > 0) {
                IssueLinker l = new IssueLinker(hp, issueHyperlinkStyle, root, sd, text, spans);
                return l;
            }
            return null;
        }

        @Override
        public void computeBounds(JTextPane textPane) {
            computeBounds(textPane, null);
        }
        
        public void computeBounds(JTextPane textPane, BoundsTranslator translator) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            this.bounds = new Rectangle[length];
            for (int i = 0; i < length; i++) {
                try {
                    Rectangle startr = tui.modelToView(textPane, docstart[i], Position.Bias.Forward);
                    Rectangle endr = tui.modelToView(textPane, docend[i], Position.Bias.Backward);
                    if (startr == null || endr == null) {
                        continue;
                    }
                    startr = startr.getBounds();
                    endr = endr.getBounds();
                    this.bounds[i] = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                    //NOTE the textPane is positioned within a parent panel so the origin has to be modified too
                    if (null != translator) {
                        translator.correctTranslation(textPane, this.bounds[i]);
                    }
                } catch (BadLocationException ex) { }
            }
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            for (int i = 0; i < start.length; i++) {
                if (bounds != null && bounds[i] != null && bounds[i].contains(p)) {
                    component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    component.setToolTipText(hp.getTooltip(text, start[i], end[i]));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            for (int i = 0; i < start.length; i++) {
                if (bounds != null && bounds[i] != null && bounds[i].contains(p)) {
                    hp.onClick(root, text, start[i], end[i]);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void insertString(StyledDocument sd, Style style) throws BadLocationException {
            sd.insertString(sd.getLength(), text, style);
            for (int i = 0; i < length; i++) {
                sd.setCharacterAttributes(sd.getLength() - text.length() + start[i], end[i] - start[i], issueHyperlinkStyle, false);
            }
        }
    }

    public static interface BoundsTranslator {
        /**
         * Corrects the bounding rectangle of nested textpanes.
         * @param startComponent
         * @param r 
         */
        public void correctTranslation (final Container startComponent, final Rectangle r);
    }
}

