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
package org.netbeans.modules.web.common.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.openide.util.Parameters;

/**
 * Support for working with lines on immutable CharSequence.
 * 
 * <b>The input text must contain only \n as line terminators!</b>
 * <b>The given CharSequence must be immutable!</b> 
 * Typical usage is to create instance of the Lines object for Snapshot's content.
 * 
 * This is compatible with the netbeans document which never contains \r\n
 * line separators.
 *
 * @since 1.28
 * @author marekfukala
 */
public class Lines {
    
    private CharSequence text;
    
    //contains beginning offsets for each line
    private Integer[] lines;

    /**
     * Creates new instance of Lines object.
     * 
     * @param text IMMUTABLE CharSequence, not null
     */
    public Lines(CharSequence text) {
        Parameters.notNull("text", text);
        this.text = text;
        initLines();
    }
    
    private void initLines() {
        List<Integer> ls = new ArrayList<Integer>();
        int lineStart = 0;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == '\r') {
                throw new IllegalArgumentException("The input text cannot contain carriage return char \\r"); //NOI18N
            }
            if(c == '\n') {
                ls.add(lineStart);
                lineStart = i + 1;
            }
        }
        //last line
        if(text.length() == 0 || ( text.length() - lineStart > 0)) {
            ls.add(lineStart);
        }
        
        lines = ls.toArray(new Integer[0]);
    }
    
    /**
     * Gets number of lines.
     * @return number of lines in the source text. Always >= 0.
     */
    public int getLinesCount() {
        return lines.length;
    }
    
    /**
     * Gets start offset of the specified line.
     * 
     * @param text
     * @param line line number
     * @return offset of the beginning of the line
     */ 
    public int getLineOffset(int lineIndex) {
        if(lineIndex < 0) {
            throw new IllegalArgumentException("Negative line index");
        }
        if(lineIndex > lines.length) {
            throw new IllegalArgumentException(String.format("Line index %s > number of lines %s", lineIndex, lines.length));
        }
        return lines[lineIndex];
    }
    
    /**
     * Gets line index(number) for the give offset.
     * 
     * @param offset
     * @return line offset, starting with zero.
     */
    public int getLineIndex(int offset) throws BadLocationException {
        if(offset < 0 || offset > text.length()) {
            throw new BadLocationException("The given offset " + offset + " is out of bounds <0, " + text.length() + ">" , offset); //NOI18N
        }
        
//        //linear
//        for(int i = 0; i < lines.length; i++) {
//            int loffset = lines[i];
//            if(loffset > offset) {
//                return i - 1;
//            }
//        }
//        return lines.length - 1; //last line
//        
        //logarithmical
        int index = Arrays.binarySearch(lines, offset);
        if(index >= 0) {
            //hit
            return index;
        } else {
            //missed (between)
            return -(index + 2);
        }
       
    }
    
    
    
}
