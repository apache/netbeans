/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.core.output2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.openide.windows.IOColors;
import org.openide.windows.OutputListener;

/**
 *
 * @author Tomas Holy
 */
public class LineInfo {

    List<Segment> segments = new CopyOnWriteArrayList<Segment>();
    final Lines parent;

    LineInfo(Lines parent) {
        this.parent = parent;
    }

    LineInfo(Lines parent, int end) {
        this(parent, end, OutputKind.OUT, null, null, null, false);
    }

    LineInfo(Lines parent, int end, OutputKind outKind, OutputListener l, Color c, Color b, boolean important) {
        this.parent = parent;
        addSegment(end, outKind, l, c, b, important);
    }

    int getEnd() {
        return segments.isEmpty() ? 0 : segments.get(segments.size() - 1).getEnd();
    }

    void addSegment(int end, OutputKind outKind, OutputListener l, Color c, Color b, boolean important) {
        Segment s = null;
        if (!segments.isEmpty()) {
            s = segments.get(segments.size() - 1);
            if (s.getKind() == outKind && s.getListener() == l && hasColors(s, c, b)) {
                // the same type of segment, prolong last one
                s.end = end;
                return;
            }
        }
        boolean isColor = c != null || b != null;
        if (l != null) {
            s = isColor ? new ColorListenerSegment(end, l, important, c, b) : new ListenerSegment(end, l, important);
        } else {
            s = isColor ? new ColorSegment(end, outKind, c, b) : new Segment(end, outKind);
        }
        segments.add(s);
    }

    private boolean hasColors(Segment s, Color c, Color b) {
        return hasForeground(s, c) && hasBackground(s, b);
    }

    private boolean hasForeground(Segment s, Color c) {
        return (s.getCustomColor() == c
                || (c != null && c.equals(s.getCustomColor())));
    }

    private boolean hasBackground(Segment s, Color b) {
        return (s.getCustomBackground() == b
                || (b != null && b.equals(s.getCustomBackground())));
    }

    OutputListener getListenerAfter(int pos, int[] range) {
        int start = 0;
        for (Segment s : segments) {
            if (s.getEnd() < pos) {
                continue;
            }
            if (s.getListener() != null) {
                if (range != null) {
                    range[0] = start;
                    range[1] = s.getEnd();
                }
                return s.getListener();
            }
            start = s.getEnd();
        }
        return null;
    }

    OutputListener getListenerBefore(int pos, int[] range) {
        for (int i = segments.size() - 1; i >= 0; i--) {
            int startPos = i == 0 ? 0 : segments.get(i-1).getEnd();
            if (startPos > pos) {
                continue;
            }
            if (segments.get(i).getListener() != null) {
                if (range != null) {
                    range[0] = startPos;
                    range[1] = segments.get(i).getEnd();
                }
                return segments.get(i).getListener();
            }
        }
        return null;
    }

    OutputListener getFirstListener(int[] range) {
        int pos = 0;
        for (Segment s : segments) {
            if (s.getListener() != null) {
                if (range != null) {
                    range[0] = pos;
                    range[1] = s.getEnd();
                }
                return s.getListener();
            }
            pos = s.getEnd();
        }
        return null;
    }

    OutputListener getLastListener(int[] range) {
        for (int i = segments.size() - 1; i >= 0; i--) {
            Segment s = segments.get(i);
            if (s.getListener() != null) {
                if (range != null) {
                    range[0] = i == 0 ? 0 : segments.get(i-1).getEnd();
                    range[1] = s.getEnd();
                }
                return s.getListener();
            }
        }
        return null;
    }

    Collection<Segment> getLineSegments() {
        return segments;
    }

    Collection<OutputListener> getListeners() {
        ArrayList<OutputListener> ol = new ArrayList<OutputListener>();
        for (Segment s : segments) {
            OutputListener l = s.getListener();
            if (l != null) {
                ol.add(l);
            }
        }
        return ol;
    }

    public class Segment {

        private int end;
        private OutputKind outputKind;

        Segment(int end, OutputKind outputKind) {
            this.end = end;
            this.outputKind = outputKind;
        }

        int getEnd() {
            return end;
        }

        OutputListener getListener() {
            return null;
        }

        OutputKind getKind() {
            return outputKind;
        }

        Color getColor() {
            IOColors.OutputType type;
            switch (outputKind) {
                case OUT:
                    type = IOColors.OutputType.OUTPUT;
                    break;
                case ERR:
                    type = IOColors.OutputType.ERROR;
                    break;
                case IN:
                    type = IOColors.OutputType.INPUT;
                    break;
                default:
                    type = IOColors.OutputType.OUTPUT;
            }
            return parent.getDefColor(type);
        }

        Color getCustomColor() {
            return null;
        }

        Color getCustomBackground() {
            return null;
        }
    }

    private class ColorSegment extends Segment {

        final Color color;
        final Color background;

        public ColorSegment(int end, OutputKind outputKind, Color color, Color background) {
            super(end, outputKind);
            this.color = color == null ? super.getColor() : color;
            this.background = background;
        }

        @Override
        Color getColor() {
            return color;
        }

        @Override
        Color getCustomColor() {
            return color;
        }

        @Override
        Color getCustomBackground() {
            return background;
        }
    }

    private class ListenerSegment extends Segment {

        final OutputListener listener;
        final boolean important;

        public ListenerSegment(int end, OutputListener l, boolean important) {
            super(end, OutputKind.OUT);
            this.listener = l;
            this.important = important;
        }

        @Override
        OutputListener getListener() {
            return listener;
        }

        @Override
        Color getColor() {
            return parent.getDefColor(important ? IOColors.OutputType.HYPERLINK_IMPORTANT
                    : IOColors.OutputType.HYPERLINK);
        }
    }

    private class ColorListenerSegment extends ListenerSegment {

        final Color color;
        final Color background;

        public ColorListenerSegment(int end, OutputListener l, boolean important, Color color, Color background) {
            super(end, l, important);
            this.color = color == null ? super.getColor() : color;
            this.background = background;
        }

        @Override
        Color getColor() {
            return color;
        }

        @Override
        Color getCustomColor() {
            return color;
        }

        @Override
        Color getCustomBackground() {
            return background;
        }
    }
}
