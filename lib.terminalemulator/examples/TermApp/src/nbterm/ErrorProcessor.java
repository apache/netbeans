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
package nbterm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.lib.terminalemulator.support.LineFilter;

class ErrorProcessor extends LineFilter {

    @Override
    public void processLine(String line, LineSink sink) {
        // handle stuff of the form
        // <filename>:<line>: warning: <error>
        // <filename>:<line>: error: <error>

        final Pattern errPattern =
            Pattern.compile("((.*):(\\d+):)( (.*): .*$)");
            // group         12    3       4 5

        Matcher errMatcher = errPattern.matcher(line);
        if (errMatcher.find()) {
            String location = errMatcher.group(1);
            String file = errMatcher.group(2);
            String lineno = errMatcher.group(3);
            String msg = errMatcher.group(4);
            String kind = errMatcher.group(5);

            StringBuilder buf = new StringBuilder();

            buf.append((char) 27);  // ESC
            if (kind.equals("error")) {
                buf.append("[01;31m");  // red
            } else if (kind.equals("warning")) {
                buf.append("[01;30m");  // grey
            }
            buf.append(hyperlink(file + ":" + lineno, location));
            buf.append(msg);
            buf.append((char) 27);  // ESC
            buf.append("[0m");
            sink.forwardLine(buf.toString());
            return;
        }

        sink.forwardLine(line);
    }
}
