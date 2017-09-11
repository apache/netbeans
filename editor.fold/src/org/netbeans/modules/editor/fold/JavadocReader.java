/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.fold;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.fold.ContentReader;

    
/**
 * The class will read the contents of the document starting after the fold's guarded start.
 * It will produce text found on the first non-blank line fully contained within the fold. During
 * the search, the Readed ignores leading whitespaces on a line followed by 'lineStartMarker' string.
 * <p/>
 * For example, if lineStartMarker is set to '*', it will ignore stars at the beginning of the Javadoc, 
 * so it will produce the overwiew sentence, although it typically starts at the 2nd javadoc line. If
 * the 'lineStartMarker' is set to "#", it will produce 1st line of a consecutive line comment block.
 * <p/>
 * The Reader will stop reading the content on line starting with a 'stopPattern'. For javadoc, lines 
 * that start with @tag will stop the reading.
 * <p/>
 * So, for Javadocs, the configuration will look like:
 * <code>
 * <pre>
 * ContentReader rd = new JavadocReader("*", "\.", "@");
 * </pre>
 * </code>
 * 
 * 
 */
public final class JavadocReader implements ContentReader {
    /**
     * The marker, which may (should!) be present at the start of the line
     */
    private final String lineStartMarker; 

    /**
     * Ignore contents after this pattern
     */
    private final Pattern stopPattern;
    
    private final Pattern termPattern;
    
    private final String prefix;
    
    public JavadocReader(String lineStartMarker, String terminator, String stopPattern, String prefix) {
        this.lineStartMarker = lineStartMarker;
        this.termPattern = terminator != null ? Pattern.compile(terminator) : null;
        this.stopPattern = stopPattern != null ? Pattern.compile(stopPattern, Pattern.CASE_INSENSITIVE) : null;
        this.prefix = prefix == null ? " " : prefix; // NOI18N
    }
    
    private int nonWhiteBwd(CharSequence seq, int pos, int limit) {
        while (pos > limit) {
            char c = seq.charAt(pos);
            if (!Character.isWhitespace(c)) {
                return pos;
            }
            pos--;
        }
        return limit;
    }
    
    private int nonWhiteFwdOnRow(CharSequence seq, int pos) {
        int l = seq.length();
        while (pos < l) {
            char c = seq.charAt(pos);
            if (!Character.isWhitespace(c)) {
                return pos;
            }
            if (c == '\n') { // NOI18N
                return -1;
            }
            pos++;
        }
        return -1;
    }

    @Override
    public CharSequence read(Document d, Fold f, FoldTemplate ft) throws BadLocationException {
        int contentStart = f.getGuardedStart();
        int contentEnd = f.getGuardedEnd();
        CharSequence seq = DocumentUtilities.getText(d);
        
        int rowStart = contentStart;
        
        while (rowStart < contentEnd) {
            Element e = DocumentUtilities.getParagraphElement(d, rowStart);
            int nextRow = Math.min(contentEnd, e.getEndOffset());
            int nonWhite = nonWhiteFwdOnRow(seq, rowStart);
            // check if the nonwhite content matches the lineStartMarker
            if (nonWhite != -1 && 
                lineStartMarker != null &&
                CharSequenceUtilities.textEquals(DocumentUtilities.getText(d, nonWhite, lineStartMarker.length()), lineStartMarker)) {
                nonWhite = nonWhiteFwdOnRow(seq, nonWhite + lineStartMarker.length());
            }
            if (nonWhite != -1) {
                // found a non-whitespace
                int endIndex = nonWhiteBwd(seq, nextRow, nonWhite);
                if (endIndex < nextRow) {
                    endIndex++;
                }
                CharSequence ret = DocumentUtilities.getText(d, nonWhite, endIndex - nonWhite);
                if (stopPattern != null && stopPattern.matcher(ret).lookingAt()) {
                    return null;
                }
                int idx = -1;
                if (termPattern != null) {
                    Matcher m = termPattern.matcher(ret);
                    if (m.find()) {
                        idx = m.start();
                    }
                }
                if (idx > -1) {
                    return prefix + DocumentUtilities.getText(d, nonWhite, idx);
                } else {
                    return prefix + ret;
                }
            }
            rowStart = nextRow;
        }
        return null;
    }
}
