/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.extexecution.base.input;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is <i>NotThreadSafe</i>.
 * 
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
        return ret.toArray(new String[0]);
    }

    public String getTrailingLine(boolean flush) {
        String line = trailingLine;
        if (flush) {
            trailingLine = null;
        }
        return "".equals(line) ? null : line;
    }
}
