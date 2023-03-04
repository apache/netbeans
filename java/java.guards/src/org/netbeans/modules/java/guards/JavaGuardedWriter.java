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

package org.netbeans.modules.java.guards;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;

/**
 *
 * @author Jan Pokorsky
 */
final class JavaGuardedWriter {

    private Iterator<SectionDescriptor> descs;
    
    private CharArrayWriter writer;
    
    /** Current section from the previous iterator. For filling this
    * field is used method nextSection.
    */
    private SectionDescriptor current;

    /** Current offset in the original document (NOT in the encapsulated
    * output stream.
    */
    private int offsetCounter;

    /** This flag is used during writing. It is complicated to explain. */
    boolean wasNewLine;

    private StringBuilder currentLine;

    /** number of consecutive spaces */
    int spaces;
    
    /** Creates a new instance of JavaGuardedWriter */
    public JavaGuardedWriter() {
    }

    public void setGuardedSection(List<GuardedSection> sections) {
        assert this.descs == null; // should be invoked just once
        this.descs = prepareSections(sections).iterator();
    }

    public char[] translate(char[] writeBuff) {
        if (this.descs == null || !this.descs.hasNext()) {
            return writeBuff;
        }
        this.writer = new CharArrayWriter(writeBuff.length);
        this.offsetCounter = 0;
        this.wasNewLine = false;
        this.currentLine = new StringBuilder(100);

        nextSection();
        
        try {
            for (char c : writeBuff) {
                writeOneChar(c);
            }
            writer.append(currentLine);
            return this.writer.toCharArray();
        } catch (IOException ex) {
            // it hardly occurs since we write to CharArrayWriter, but for sure
            throw new IllegalStateException(ex);
        } finally {
            this.writer = null;
            this.current = null;
        }
        
    }

    /** Write one character. If there is a suitable place,
    * some special comments are written to the underlaying stream.
    * @param b char to write.
    */
    void writeOneChar(int b) throws IOException {
        if (b == '\r')
            return;

        if (current != null) {
            if (offsetCounter == current.getBegin()) {
                wasNewLine = false;
            }
            if (current.getBegin() <= offsetCounter && b == '\n') {
                switch(current.getType()) {
                    case LINE:

                        if (!wasNewLine) {
                            if (offsetCounter + 1 >= current.getEnd()) {
                                writeMagic(GuardTag.LINE, current.getName());
                                nextSection();
                            }
                            else {
                                writeMagic(GuardTag.BEGIN, current.getName());
                                wasNewLine = true;
                            }
                        }
                        else {
                            if (offsetCounter + 1 >= current.getEnd()) {
                                writeMagic(GuardTag.END, current.getName());
                                nextSection();
                            }
                        }

                        break;
                    case FIRST:
                    case HEADER:

                        if (!wasNewLine) {
                            if (offsetCounter + 1 >= current.getEnd()) {
                                writeMagic(GuardTag.FIRST, current.getName());
                                nextSection();
                            }
                            else {
                                writeMagic(GuardTag.FIRST, current.getName());
                                wasNewLine = true;
                            }
                        }
                        else {
                            if (offsetCounter + 1 >= current.getEnd()) {
                                writeMagic(GuardTag.HEADEREND, current.getName());
                                nextSection();
                            }
                        }

                        break;
                    case LAST:
                    case END:

                        writeMagic(GuardTag.LAST, current.getName());

                        nextSection();

                        break;
                }
            }
        }
        if (b == ' ') {
            spaces++;
        } else {
            while (spaces > 0) {
                currentLine.append(' ');
                spaces--;
            }
            currentLine.append((char)b);
            if (b == '\n') {
                writer.append(currentLine);
                currentLine.delete(0, currentLine.length());
            }
        }
        offsetCounter++;
    }

    /** Try to get next sectionDesc from the 'sections'
    * If there is no more section the 'current' will be set to null.
    */
    private void nextSection() {
        current = descs.hasNext() ? descs.next() : null;
    }

    /** Writes the magic to the underlaying stream.
    * @param type The type of the magic section - T_XXX constant.
    * @param name name of the section.
    */
    private void writeMagic(GuardTag type, String name) throws IOException {
        // XXX see #73805 to resolve this hack
//        if (!shouldReload) {
//            shouldReload = spaces != SECTION_MAGICS[type].length()  + name.length();
//        }
        spaces = 0;
        String magic = JavaGuardedReader.MAGIC_PREFIX + type.name() + ':';
        if (JavaGuardedReader.getKeepGuardedComments()) {
            int i = currentLine.lastIndexOf(magic);
            if (i >= 0) { // after section rename there could still be a comment with the previous name
                currentLine.delete(i, currentLine.length());
            }
        }
        currentLine.append(magic);
        currentLine.append(name);
    }

    /** This method prepares the iterator of the SectionDesc classes
    * @param list The list of the GuardedSection classes.
    * @return iterator of the SectionDesc
    */
    private List<SectionDescriptor> prepareSections(List<? extends GuardedSection> list) {
        List<SectionDescriptor> dest = new ArrayList<SectionDescriptor>(list.size());

        for (GuardedSection o: list) {
            if (o instanceof SimpleSection) {
                SectionDescriptor desc = new SectionDescriptor(
                        GuardTag.LINE,
                        o.getName(),
                        o.getStartPosition().getOffset(),
                        o.getEndPosition().getOffset()
                        );
                dest.add(desc);
            } else {
                SectionDescriptor desc = new SectionDescriptor(
                        GuardTag.HEADER,
                        o.getName(),
                        o.getStartPosition().getOffset(),
                        ((InteriorSection) o).getBodyStartPosition().getOffset() - 1
                        );
                dest.add(desc);

                desc = new SectionDescriptor(
                        GuardTag.END,
                        o.getName(),
                        ((InteriorSection) o).getBodyEndPosition().getOffset() + 1,
                        o.getEndPosition().getOffset()
                        );
                dest.add(desc);
            }
        }
        return dest;
    }
    
}
