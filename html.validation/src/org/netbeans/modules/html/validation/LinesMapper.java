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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.validation;

import java.util.ArrayList;
import java.util.List;
import nu.validator.htmlparser.common.CharacterHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class LinesMapper implements CharacterHandler {

    private final List<Line> lines = new ArrayList<Line>();
    private Line currentLine = null;
    private boolean prevWasCr = false;
    final StringBuilder content = new StringBuilder();

    public CharSequence getSourceText() {
        return content;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        int from = content.length();
        content.append(ch, start, length);
        int to = content.length();

        //now scan for EOLs
        for (int i = from; i < to; i++) {
            char c = content.charAt(i);
            switch (c) {
                case '\r':
                    currentLine.setCR();
                    currentLine.setEnd(i + 1);
                    prevWasCr = true;
                    break;
                case '\n':
                    currentLine.setLF();
                    currentLine.setEnd(i + 1);
                    prevWasCr = false;
                    newLine(i + 1);
                    break;
                default:
                    currentLine.setEnd(i + 1);
                    if (prevWasCr) {
                        //only \r (on old Mac-s)
                        prevWasCr = false;
                        newLine(i);
                    }
                    break;
            }
        }
    }

    private void newLine(int from) {
        currentLine = new Line(from);
        lines.add(currentLine);
    }

    @Override
    public void end() throws SAXException {
        //no-op
//        System.out.println("Lines:");
//        for (int i = 0; i < lines.size(); i++) {
//            Line l = lines.get(i);
//            System.out.println(l);
//        }
//        System.out.println("------------");
    }

    @Override
    public void start() throws SAXException {
        lines.clear();
        newLine(0);
        prevWasCr = false;
    }

    public int getLinesCount() {
        return lines.size();
    }

    public Line getLine(int linenum) {
        return lines.get(linenum);
    }

    public int getSourceOffsetForLocation(int line, int column) {
        if (line == -1 || column == -1) {
            throw new IllegalArgumentException();
        }
        Line lline = lines.get(line);
        int sourceOffset = lline.getOffset() + column;

        if(sourceOffset > content.length()) {
            //the location overlaps the source length, recover by returning the source len
            //XXX report it somehow?
            //XXX note: may *possibly* be a bug in the LinesMapper itself!
            return content.length();
        } else {
            return sourceOffset;
        }

    }

    public class Line {

        private int start, end; //len includes the EOL chars!
        private boolean cr, lf;

        public Line(int offset) {
            this.start = offset;
            this.end = offset;
        }

        public void setCR() {
            this.cr = true;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public void setLF() {
            this.lf = true;
        }

        public CharSequence getText() {
            return LinesMapper.this.content.subSequence(start, end - getNewLineDelimitersLen());
        }

        public CharSequence getTextWithEndLineChars() {
            return LinesMapper.this.content.subSequence(start, end);
        }

        public int getNewLineDelimitersLen() {
            return (cr ? 1 : 0) + (lf ? 1 : 0);
        }

        public int getOffset() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "Line{" + start + "-" + end + " "+ (cr ? "\\r" : "") + (lf ? "\\n" : "") + " '" + getText() + "'";
        }
        
    }
}
