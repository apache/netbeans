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
package org.netbeans.modules.python.api;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;

public final class FileLocation {
    private static final Pattern[] LOCATION_RECOGNIZER_PATTERNS = new Pattern[]{
        PythonLineConvertorFactory.PYTHON_STACKTRACE_PATTERN
        //        PythonLineConvertorFactory.RAILS_RECOGNIZER,
//        PythonLineConvertorFactory.RUBY_COMPILER_WIN_MY,
//        PythonLineConvertorFactory.RUBY_COMPILER,
//        PythonLineConvertorFactory.RUBY_COMPILER_WIN,
    };

    public final String file;
    public final int line;

    public FileLocation(String file, int line) {
        this.file = file;
        this.line = line;
    }

    // TODO: find a better place for this method (doesn't have anything to
    // do with external execution)
    public static FileLocation getLocation(String line) {

        final int fileGroup = 1;
        final int lineGroup = 2;

        if (line.length() > 400) {
            return null;
        }

        for (Pattern pattern : LOCATION_RECOGNIZER_PATTERNS) {
            Matcher match = pattern.matcher(line);

            if (match.matches()) {
                String file = null;
                int lineno = -1;

                if (fileGroup != -1) {
                    file = match.group(fileGroup);
                    // Make some adjustments - easier to do here than in the regular expression
                    // (See 109721 and 109724 for example)
                    if (file.startsWith("\"")) { // NOI18N
                        file = file.substring(1);
                    }
                    if (file.startsWith("./")) { // NOI18N
                        file = file.substring(2);
                    }
                    if (!(PythonLineConvertorFactory.EXT_RE.matcher(file).matches() || new File(file).isFile())) {
                        return null;
                    }
                }

                if (lineGroup != -1) {
                    String linenoStr = match.group(lineGroup);

                    try {
                        lineno = Integer.parseInt(linenoStr);
                    } catch (NumberFormatException nfe) {
                        Exceptions.printStackTrace(nfe);
                        lineno = 0;
                    }
                }

                return new FileLocation(file, lineno);
            }
        }

        return null;
    }

}
