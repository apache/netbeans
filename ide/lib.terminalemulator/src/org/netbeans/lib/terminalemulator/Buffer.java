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

/*
 * "Buffer.java"
 * Buffer.java 1.8 01/07/30
 */
package org.netbeans.lib.terminalemulator;

import java.util.Vector;

/**
 * The Buffer used by Term is _not_ related to javax.swing.text.Document.
 * <p>
 * The Swing Document is Element based while terms is Line based.
 * <br>
 * The Swing Document uses offsets for coordinates, while term uses cartesian
 * BCoords.
 * <p>
 */
class Buffer {

    /*
     * For some odd reason Vector.removeRange is protected, so 
     * we have to do this to gain access to it.
     */
    private static class OurVector<T> extends Vector<T> {

        @Override
        public void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }
    private final OurVector<Line> lines = new OurVector<>();	// buffer

    // How is this different from lines.length?
    private int nlines;		// number of lines in buffer

    private int visible_cols;	// number of columns visible in view
    private int extra_cols;	// columns needed to support lines longer
    				// than visible_cols. Only grows.

    /**
     * @return nlines
     */
    int nlines() {
	return nlines;
    }

    public int visibleCols() {
        return visible_cols;
    }

    public int totalCols() {
        return visible_cols + extra_cols;
    }

    public Buffer(int visible_cols) {
        this.visible_cols = visible_cols;
    }

    public void setVisibleCols(int visible_cols) {
        int delta = visible_cols - this.visible_cols;
        this.visible_cols = visible_cols;
        extra_cols -= delta;
        if (extra_cols < 0) {
            extra_cols = 0;
        }
    }

    /*
     * Keep track of the largest column # to help set the extent of 
     * the horizontal scrollbar.
     */
    public void noteColumn(int col) {
        int new_extra = col - visible_cols;
        if (new_extra > extra_cols) {
            extra_cols = new_extra;
        // LATER hrange_listener.adjustHRange(extra_cols);
        }
    }

    /* DEBUG
    public static volatile boolean lock = false;
    
    private void ck_lock() {
    if (lock) {
    System.out.println("Buffer ck_lock fail");	// NOI18N
    printStats();
    Thread.dumpStack();
    }
    }
     */
    Line lineAt(int brow) {
        try {
            return lines.elementAt(brow);
        } catch (ArrayIndexOutOfBoundsException x) {
            //XXX swallowing this exception caused issue 40129.
            //I've put in a null-check on the return value in sel.paint()
            //as a hotfix.  Should find out why bad values are being passed
            //here.  Ivan?

            /* DEBUG
            System.out.println("Buffer.lineAt(" +brow+ ") -> null\n");// NOI18N
            Thread.dumpStack();
             */
            return null;
        }
    }

    Line bottom() {
        return lineAt(nlines);
    }

    public Line appendLine() {
        // DEBUG ck_lock();
        Line l = new Line();
        lines.add(l);
        nlines++;
        return l;
    }

    public Line addLineAt(int row) {
        // DEBUG ck_lock();
        Line l = new Line();
        lines.add(row, l);
        nlines++;
        return l;
    }

    /**
     * Remove 'n' lines starting at 'row'.
     * Return the number of characters deleted as a result.
     */
    public int removeLinesAt(int row, int n) {
        // DEBUG ck_lock();
        int nchars = 0;
        for (int r = row; r < row + n; r++) {
            nchars += lineAt(r).length() + 1;
        }

        lines.removeRange(row, row + n);
        nlines -= n;

        return nchars;
    }

    public void removeLineAt(int row) {
        // DEBUG ck_lock();
        lines.remove(row);
        nlines--;
    }

    public Line moveLineFromTo(int from, int to) {
        // DEBUG ck_lock();
        Line l = lines.remove(from);
        lines.add(to, l);
        return l;
    }

    /**
     * Visit the physical lines from 'begin', through 'end'.
     * <p>
     * If 'newlines' is set, the passed 'ecol' is set to the actual
     * number of columns in the view to signify that the newline is included.
     * This way of doing it helps with rendering of a whole-line selection.
     * Also Line knows about this and will tack on a "\n" when Line.text()
     * is asked for.
     */
    void visitLines(BCoord begin, BCoord end, boolean newlines,
            LineVisitor visitor) {

        // In the general case a range is made up of three
        // rectangles. The partial line at top, the partial line
        // at the bottom and the middle range of fully selected lines.

        Line l;
        if (begin.row == end.row) {
            // range is on one line
            l = lineAt(begin.row);
            visitor.visit(l, begin.row, begin.col, end.col);

        } else {
            boolean cont;

            // range spans multiple lines
            l = lineAt(begin.row);
            if (newlines && !l.isWrapped()) {
                cont = visitor.visit(l, begin.row, begin.col, totalCols());
            } else {
                cont = visitor.visit(l, begin.row, begin.col, l.length() - 1);
            }
            if (!cont) {
                return;
            }

            for (int r = begin.row + 1; r < end.row; r++) {
                l = lineAt(r);
                if (newlines && !l.isWrapped()) {
                    cont = visitor.visit(l, r, 0, totalCols());
                } else {
                    cont = visitor.visit(l, r, 0, l.length() - 1);
                }
                if (!cont) {
                    return;
                }
            }

            l = lineAt(end.row);
            cont = visitor.visit(l, end.row, 0, end.col);
            if (!cont) {
                return;
            }
        }
    }

    /*
     * Like visitLines() except in reverse.
     * <p>
     * Starts at 'end' and goes to 'begin'.
     */
    void reverseVisitLines(BCoord begin, BCoord end, boolean newlines,
            LineVisitor visitor) {

        // very similar to visitLines

        Line l;
        if (begin.row == end.row) {
            // range is on one line
            l = lineAt(begin.row);
            visitor.visit(l, begin.row, begin.col, end.col);

        } else {
            boolean cont;

            // range spans multiple lines
            l = lineAt(end.row);
            cont = visitor.visit(l, end.row, 0, end.col);
            if (!cont) {
                return;
            }

            for (int r = end.row - 1; r > begin.row; r--) {
                l = lineAt(r);
                if (newlines && !l.isWrapped()) {
                    cont = visitor.visit(l, r, 0, totalCols());
                } else {
                    cont = visitor.visit(l, r, 0, l.length() - 1);
                }
                if (!cont) {
                    return;
                }
            }

            l = lineAt(begin.row);
            if (newlines && !l.isWrapped()) {
                cont = visitor.visit(l, begin.row, begin.col, totalCols());
            } else {
                cont = visitor.visit(l, begin.row, begin.col, l.length() - 1);
            }
            if (!cont) {
                return;
            }
        }
    }

    public BExtent find_line(BCoord coord) {
        WordDelineator newLine = WordDelineator.createNewlineDelineator();
        return find_word(newLine, coord);
    }

    public BExtent find_word(WordDelineator word_delineator, BCoord coord) {
        /*
         * Find the boundaries of a "word" at 'coord'.
         */

        int row = coord.row;
        Line startLine = lineAt(row);

        if (coord.col >= startLine.length()) {
            return new BExtent(coord, coord);
        }

        int lx = 0;
        int rx = 0;
        int beginRow = row;
        int endRow = row;
        StringBuffer lineBuffer;

        // Try to use lineVisitor
        while (beginRow >= 0) {
            Line line = lineAt(beginRow);
            lineBuffer = line.stringBuffer();
            int searchCol = (lx == -1) ? line.length() - 1 : coord.col;
            lx = word_delineator.findLeft(lineBuffer, searchCol, true);
            
            if (lx != -1) {
                break;
            } else {
                if (beginRow == 0) {
                    lx = 0;
                } else if (!lineAt(beginRow - 1).isWrapped()) {
                    lx = 0;
                    break;
                }
            }
            beginRow--;
        }
        while (endRow < nlines) {
            Line line = lineAt(endRow);
            lineBuffer = line.stringBuffer();
            int searchCol = (rx == -1) ? 0 : coord.col;
            rx = word_delineator.findRight(lineBuffer, searchCol, true);
            
            if (rx != -1) {
                break;
            } else {
                if (endRow == nlines - 1) {
                    rx = line.length() - 1;
                }
                if (!line.isWrapped()) {
                    rx = line.length() - 1;
                    break;
                }
            }
            endRow++;
        }

        return new BExtent(new BCoord(beginRow, lx),
                new BCoord(endRow, rx));
    }

    /**
     * Back up the coordinate by one character and return new BCoord
     * <p>
     * Travels back over line boundaries
     * <br>
     * Returns null if 'c' is the first character of the buffer.
     */
    public BCoord backup(BCoord c) {
        if (c.col > 0) {
            return new BCoord(c.row, c.col - 1);	// back one in line
        }
        // Cursor is at beginning of line.
        // Need to find the end of previous line, but it might empty,
        // so we go one line back etc
        for (int prevrow = c.row - 1; prevrow >= 0; prevrow--) {
            Line l = lineAt(prevrow);
            if (l.length() != 0) {
                return new BCoord(prevrow, l.length() - 1);
            }
        }

        // prevrow == -1, at beginning of file; nowhere to back to
        return null;
    }

    /*
     * Advance the coordinate by one character and return a new coord.
     * <p>
     * Wraps around line boundaries.
     * <br>
     * Returns null if 'c' is at the last character of the buffer.
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public BCoord advance(BCoord c) {
        int row = c.row;
        int col = c.col;

        col++;
        Line l = lineAt(row);
        if (col < l.length()) {
            return new BCoord(row, col);
        }

        // Need to wrap, but the next line might be empty ... so we
        // keep going til either we find a non-empty line or the end
        // of the buffer.
        while (++row < nlines) {
            l = lineAt(row);
            if (l.length() != 0) {
                return new BCoord(row, 0);
            }
        }
        return null;
    }

    private int utilization(int nchars, int ncapacity) {
        float u = ((float) nchars) / ((float) ncapacity);
        float p = u * 100.0f;      // convert to percentage
        return (int) p;
    }
    /*
     * Print interesting statistics and facts about this Term
     */

    public void printStats(boolean indented) {
        int chars = 0;
        int charcapacity = 0;

        int nalines = 0;     // lines with attributes
        int attrs = 0;
        int attrcapacity = 0;

        for (int lx = 0; lx < nlines; lx++) {
            Line l = lineAt(lx);
            chars += l.length();
            charcapacity += l.capacity();
            if (l.hasAttributes()) {
                nalines++;
                attrs += l.length();
                attrcapacity += l.capacity();
            }
        }

        Term.indent(indented);
        System.out.println("Buffer:" + // NOI18N
                "  nlines " + nlines);	// NOI18N

        Term.indent(indented);
        System.out.println("       " + // NOI18N
                "  chars " + chars + // NOI18N
                "  charcapacity " + charcapacity + // NOI18N
                "  utilzn %" + utilization(chars, charcapacity));	// NOI18N

        Term.indent(indented);
        System.out.println("       " + // NOI18N
                "  attrs " + attrs + // NOI18N
                "  attrcapacity " + attrcapacity + // NOI18N
                "  utilzn %" + utilization(attrs, attrcapacity));	// NOI18N

        // estimate actual byte consumption
        final int unitObjectSz = 8;
        final int unitReferenceSz = 4;

        final int unitBooleanSz = 4;
        final int unitCharSz = 2;
        final int unitIntSz = 4;

        final int unitCharArraySz = 12;
        final int unitIntArraySz = 12;

        long bytes = 0;
        long unitLineSz = unitObjectSz +
                unitBooleanSz + // about_to_wrap
                unitReferenceSz + // attr
                unitIntSz + // backgroundColor
                unitReferenceSz + // buf
                unitIntSz + // capacity
                unitIntSz + // glyphId
                unitIntSz + // length
                unitBooleanSz // wrapped
                ;
        bytes += unitLineSz * nlines;

        bytes += unitCharArraySz * nlines;
        bytes += unitCharSz * charcapacity;

        bytes += unitIntArraySz * nalines;
        bytes += unitIntSz * attrcapacity;

        Term.indent(indented);
        System.out.println("       " + // NOI18N
                "  bytes " + bytes / 1024 + "K");         	// NOI18N

    }
}
