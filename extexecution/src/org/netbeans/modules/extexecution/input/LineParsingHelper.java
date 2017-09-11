/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.extexecution.input;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class is <i>NotThreadSafe</i>.
 * @author Petr Hejl
 */
public final class LineParsingHelper {

    private String trailingLine;

    public LineParsingHelper() {
        super();
    }
    
    public String[] parse(char[] buffer) {
        return parse(buffer, 0, buffer.length);
    }

    public String[] parse(char[] buffer, int offset, int limit) {
        return parse(CharBuffer.wrap(buffer, offset, limit));
    }

    public String[] parse(CharSequence input) {
        //prepend the text from the last reading to the text actually read
        String lines = (trailingLine != null ? trailingLine : "");
        lines += input.toString();
        int tlLength = (trailingLine != null ? trailingLine.length() : 0);
        int start = 0;
        List<String> ret = new ArrayList<String>();
        int length = input.length();
        for (int i = 0; i < length; i++) { // going through the text read and searching for the new line
            //we see '\n' or '\r', *not* '\r\n'
            char c = input.charAt(i);
            if (c == '\r'
                    && (i + 1 == length || input.charAt(i + 1) != '\n')
                    || c == '\n') {
                String line = lines.substring(start, tlLength + i);
                //move start to the character right after the new line
                start = tlLength + (i + 1);
                ret.add(line);
            } else if (c == '\r'
                    && (i + 1 < length) && input.charAt(i + 1) == '\n') {//we see '\r\n'
                String line = lines.substring(start, tlLength + i);
                //skip the '\n' character
                i += 1;
                //move start to the character right after the new line
                start = tlLength + (i + 1);
                ret.add(line);
            }
        }
        if (start < lines.length()) {
            //new line was not found at the end of the input, the remaing text is stored for the next reading
            trailingLine = lines.substring(start);
        } else {
            //null and not empty string to indicate that there is no valid input to write out;
            //an empty string means that a new line character may be written out according
            //to the LineProcessor implementation
            trailingLine = null;
        }
        return ret.toArray(new String[ret.size()]);
    }

    public String getTrailingLine(boolean flush) {
        String line = trailingLine;
        if (flush) {
            trailingLine = null;
        }
        return "".equals(line) ? null : line;
    }
}
