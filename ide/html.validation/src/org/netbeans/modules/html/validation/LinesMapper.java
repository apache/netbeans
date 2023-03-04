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
