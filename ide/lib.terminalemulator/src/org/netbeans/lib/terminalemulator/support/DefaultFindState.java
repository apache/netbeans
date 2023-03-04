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
package org.netbeans.lib.terminalemulator.support;

import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Extent;
import org.netbeans.lib.terminalemulator.LogicalLineVisitor;
import org.netbeans.lib.terminalemulator.Term;

public class DefaultFindState implements FindState {

    private final Term term;
    private boolean visible = false;
    private String pattern;
    private boolean tentative;
    private Direction direction;
    private Extent extent = null;
    private Status status = Status.EMPTYPATTERN;
    private boolean found = false;

    private enum Direction {
        BACKWARD, FORWARD
    }

    public DefaultFindState(Term term) {
        super();
        this.term = term;
        this.direction = Direction.FORWARD;
    }

    private LogicalLineVisitor forwardVisitor = new LogicalLineVisitor() {

        public boolean visit(int line, Coord begin, Coord end, String text) {
            int i = text.indexOf(pattern);
            if (i == -1) {
                return true;    // keep going
            }
            if (!tentative) {
                extent = term.extentInLogicalLine(begin, i, pattern.length());
                term.setSelectionExtent(extent);
                extent.end = term.advance(extent.end);
            }
            found = true;
            return false;       // stop
        }
    };

    private LogicalLineVisitor backwardVisitor = new LogicalLineVisitor() {

        public boolean visit(int line, Coord begin, Coord end, String text) {
            int i = text.lastIndexOf(pattern);
            if (i == -1) {
                return true;        // keep going
            }
            if (!tentative) {
                extent = term.extentInLogicalLine(begin, i, pattern.length());
                term.setSelectionExtent(extent);
                Coord bckp = term.backup(extent.begin);
                if (bckp != null) {
                    extent.begin = bckp;
                }

            }
            found = true;
            return false;          // stop
        }
    };

    public void setPattern(String pattern) {
        this.pattern = pattern;
        this.found = false;
        if (pattern == null || pattern.equals("")) {
            status = Status.EMPTYPATTERN;
        } else {
            status = Status.OK;
        }
    }

    public String getPattern() {
        return pattern;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void next() {
        if (status == Status.EMPTYPATTERN) {
            return;
        }

        found = false;      // dot and found set by forwardVisitor

        tentative = false;
        term.visitLogicalLines(end(), null, forwardVisitor);

        if (found) {
            term.possiblyNormalize(end());
            status = Status.OK;
        } else {
            // see if it will be found if wrapped
            tentative = true;
            term.visitLogicalLines(null, null, forwardVisitor);
            if (found) {
                extent.end = null;
                status = Status.WILLWRAP;
            } else {
                status = Status.NOTFOUND;
            }
        }
    }

    public void prev() {
        if (status == Status.EMPTYPATTERN) {
            return;
        }

        found = false;      // dot and found set by forwardVisitor

        tentative = false;
        term.reverseVisitLogicalLines(null, begin(), backwardVisitor);

        if (found) {
            term.possiblyNormalize(begin());
            status = Status.OK;
        } else {
            // see if it will be found if wrapped
            tentative = true;
            term.reverseVisitLogicalLines(null, null, backwardVisitor);
            if (found) {
                extent.begin = null;
                status = Status.WILLWRAP;
            } else {
                status = Status.NOTFOUND;
            }
        }
    }

    public Status getStatus() {
        return status;
    }

    private Coord begin() {
        return (extent == null) ? null : extent.begin;
    }

    private Coord end() {
        return (extent == null) ? null : extent.end;
    }
}
