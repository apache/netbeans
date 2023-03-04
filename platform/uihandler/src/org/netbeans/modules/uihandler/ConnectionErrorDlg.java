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
package org.netbeans.modules.uihandler;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.HtmlBrowser;

/**
 * To be used as an argument to {@link Message}
 * 
 * @author Martin Entlicher
 */
final class ConnectionErrorDlg extends Box {
    
    private static final int MaxCharactersPerLineCount = 100;
    
    private static final String LINK_START = "[URL]";                           // NOI18N
    private static final String LINK_END = "[/URL]";                            // NOI18N
    private static final String FIELD_START = "[TTC]";                          // NOI18N
    private static final String FIELD_END = "[/TTC]";                           // NOI18N
    
    private ConnectionErrorDlg(Segment[] segments) {
        super(BoxLayout.PAGE_AXIS);
        initComponents(segments);
    }
    
    static Object get(String msg) {
        Segment[] segments = parseMessage(msg);
        if (segments != null) {
            return new ConnectionErrorDlg(segments);
        } else {
            return msg;
        }
    }
    
    static Segment[] parseMessage(String msg) {
        int t = 0; // Text index
        int l = 0; // link index
        int f = 0; // field index
        List<Segment> segments = null;
        do {
            if (l >= 0) {
                l = msg.indexOf(LINK_START, l);
            }
            if (f >= 0) {
                f = msg.indexOf(FIELD_START, f);
            }
            if (l < 0 && f < 0 && segments == null) {
                return null;
            }
            if (segments == null) {
                segments = new ArrayList<Segment>();
            }
            int t2 = t;
            if (l > t) {
                t2 = l;
            }
            if (f > t && (l < 0 || f < l)) {
                t2 = f;
            }
            if (l < 0 && f < 0) {
                t2 = msg.length();
            }
            if (t2 > t) {
                String text = msg.substring(t, t2);
                int nl = 0;
                int nl2;
                while ((nl2 = text.indexOf('\n', nl)) >= 0) {
                    segments.add(new Segment(SegmentKind.LABEL, text.substring(nl, nl2), true));
                    nl = nl2 + 1;
                }
                if (nl < text.length()) {
                    segments.add(new Segment(SegmentKind.LABEL, text.substring(nl)));
                }
            }
            t = t2;
            boolean linkFirst = l >= 0 && (f < 0 || l < f);
            if (l >= 0 && linkFirst) {
                l += LINK_START.length();
                int l2 = msg.indexOf(LINK_END, l);
                if (l2 > 0) {
                    segments.add(new Segment(SegmentKind.LINK, msg.substring(l, l2)));
                    t = l = l2 + LINK_END.length();
                }
            }
            if (f >= 0 && !linkFirst) {
                f += FIELD_START.length();
                int f2 = msg.indexOf(FIELD_END, f);
                if (f2 > 0) {
                    segments.add(new Segment(SegmentKind.FIELD, msg.substring(f, f2)));
                    t = f = f2 + FIELD_END.length();
                }
            }
        } while (l > 0 || f > 0 || t < msg.length());
        return segments.toArray(new Segment[] {});
    }
    
    private void initComponents(Segment[] segments) {
        List<Segment> line = new ArrayList<Segment>();
        int i = 0;
        while (i < segments.length) {
            int l = segments[i].text.length();
            line.add(segments[i++]);
            while (i < segments.length) {
                l += segments[i].text.length();
                if (l <= MaxCharactersPerLineCount) {
                    boolean eol = segments[i].eol;
                    line.add(segments[i++]);
                    if (eol) {
                        break;
                    }
                } else {
                    break;
                }
            }
            addLine(line);
            line.clear();
        }
    }

    private void addLine(List<Segment> line) {
        if (line.size() == 1) {
            addSegment(this, line.get(0));
        } else {
            Box lineBox = new Box(BoxLayout.LINE_AXIS);
            if (lineBox.getComponentOrientation().isLeftToRight()) {
                lineBox.setAlignmentX(LEFT_ALIGNMENT);
            } else {
                lineBox.setAlignmentX(RIGHT_ALIGNMENT);
            }
            for (Segment s : line) {
                addSegment(lineBox, s);
            }
            add(lineBox);
        }
    }
    
    private void addSegment(Box box, Segment segment) {
        JComponent c;
        switch (segment.kind) {
            case FIELD:
                JTextField f = new JTextField(segment.text);
                f.setEditable(false);
                Dimension preferredSize = f.getPreferredSize();
                f.setMinimumSize(preferredSize);
                f.setMaximumSize(preferredSize);
                f.setBorder(new EmptyBorder(f.getBorder().getBorderInsets(f)));
                c = f;
                break;
            case LABEL:
                c = new JLabel(segment.text);
                break;
            case LINK:
                // use lighter blue for dark themes
                String linkColor  = UIManager.getBoolean("nb.dark.theme")  ? "#A4A4FF" : "#0000FF";
                c = new JLabel("<html><u><font color=\""+ linkColor + "\">"+segment.text+"</font></u></html>"); // NOI18N
                preferredSize = c.getPreferredSize();
                c.setMinimumSize(preferredSize);
                c.setMaximumSize(preferredSize);
                c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                try {
                    final URL url = new URL(segment.text);
                    c.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                        }
                    });
                } catch (MalformedURLException muex) {}
                break;
            default:
                throw new IllegalStateException("Unknown segment kind: "+segment.kind);             // NOI18N
        }
        if (c.getComponentOrientation().isLeftToRight()) {
            c.setAlignmentX(LEFT_ALIGNMENT);
        } else {
            c.setAlignmentX(RIGHT_ALIGNMENT);
        }
        box.add(c);
    }
    
    static enum SegmentKind {
        LABEL,  // A label with a text, which is not possible to copy
        LINK,   // A link which opens in a web browser
        FIELD,  // A non-editable field with a text, which is possible to copy
    }
    
    static final class Segment {
        
        final SegmentKind kind;
        final String text;
        final boolean eol;
        
        public Segment(SegmentKind kind, String text) {
            this(kind, text, false);
        }
        
        public Segment(SegmentKind kind, String text, boolean eol) {
            this.kind = kind;
            this.text = text;
            this.eol = eol;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Segment)) {
                return false;
            }
            Segment s = (Segment) obj;
            return kind == s.kind &&
                   text.equals(s.text) &&
                   eol == s.eol;
        }

        @Override
        public int hashCode() {
            return kind.ordinal() + text.hashCode() + ((eol) ? 1048576 : 0);
        }

        @Override
        public String toString() {
            return "Segment("+kind.name()+")["+text+"]" + ((eol) ? "\\n" : "");
        }
        
    }
    
}
