/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
