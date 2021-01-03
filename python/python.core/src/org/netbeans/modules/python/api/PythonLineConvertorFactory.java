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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;

public final class PythonLineConvertorFactory implements LineConvertorFactory {

    //private static final String WINDOWS_DRIVE = "(?:\\S{1}:[\\\\/])"; // NOI18N
    //private static final String FILE_CHAR = "[^\\s\\[\\]\\:\\\"]"; // NOI18N
    //private static final String FILE = "((?:" + FILE_CHAR + "*))"; // NOI18N
    //private static final String FILE_WIN = "(" + WINDOWS_DRIVE + "(?:" + FILE_CHAR + ".*))"; // NOI18N
    //private static final String LINE = "([1-9][0-9]*)"; // NOI18N
    //private static final String ROL = ".*\\s?"; // NOI18N
    //private static final String SEP = "\\:"; // NOI18N
    //private static final String STD_SUFFIX = FILE + SEP + LINE + ROL;
    //private static final Pattern RUBY_COMPILER = Pattern.compile(".*?" + STD_SUFFIX); // NOI18N
    //private static final Pattern RUBY_COMPILER_WIN_MY = Pattern.compile(".*?" + FILE_WIN + SEP + LINE + ROL); // NOI18N

    // Typical line from Python:
    // "/Users/user/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py", line 7,
    // Also check test failure output

    /* Keeping old one. Get rid of this with more specific recongizers? */
    //private static final Pattern RUBY_COMPILER_WIN =
    //        Pattern.compile("^(?:(?:\\[|\\]|\\-|\\:|[0-9]|\\s|\\,)*)(?:\\s*from )?" + FILE_WIN + SEP + LINE + ROL); // NOI18N
    //public static final Pattern RAILS_RECOGNIZER =
    //        Pattern.compile(".*#\\{RAILS_ROOT\\}/" + STD_SUFFIX); // NOI18N
    //public static final Pattern RUBY_TEST_OUTPUT = Pattern.compile("\\s*test.*\\[" + STD_SUFFIX); // NOI18N

    // See the traceback module for details
    static final Pattern PYTHON_STACKTRACE_PATTERN = Pattern.compile("^  File \"(.+\\.py)\", line (\\d+).*");
    /** Regexp. for extensions. */
    public static final Pattern EXT_RE = Pattern.compile(".*\\.(py|pyw)"); // NOI18N

    private final FileLocator locator;
    private final LineConvertor[] convertors;
    private final boolean stdConvertors;


    /**
     * Creates a new convertor factory.
     * 
     * @param locator the locator to use.
     * @param convertors the convertors to use (if more than one is passed, they will
     *  be chained in the given order, i.e. the first given convertor will get to handle
     *  lines first).
     * @return
     */
    public static PythonLineConvertorFactory create(FileLocator locator, LineConvertor... convertors) {
        return new PythonLineConvertorFactory(locator, false, convertors);
    }

    /**
     * Creates a new convertor factory with the standard Ruby line convertors. The
     * standard convertors will be chained after the given (if any) convertors.
     *
     * @param locator the locator to use.
     * @param convertors the convertors to use (if more than one is passed, they will
     *  be chained in the given order, i.e. the first given convertor will get to handle
     *  lines first).
     * @return
     */
    public static PythonLineConvertorFactory withStandardConvertors(FileLocator locator, LineConvertor... convertors) {
        return new PythonLineConvertorFactory(locator, true, convertors);
    }

    private PythonLineConvertorFactory(FileLocator locator, boolean stdConvertors, LineConvertor... convertors) {
        this.locator = locator;
        this.convertors = convertors;
        this.stdConvertors = stdConvertors;
    }

    /**
     * Gets the standard convertors.
     *
     * @param locator the locator for the convertors to use.
     * @return
     */
    public static List<LineConvertor> getStandardConvertors(FileLocator locator) {
        List<LineConvertor> result = new ArrayList<>(4);
        result.add(LineConvertors.filePattern(locator, PYTHON_STACKTRACE_PATTERN, EXT_RE, 1, 2));
//        result.add(LineConvertors.filePattern(locator, RAILS_RECOGNIZER, EXT_RE, 1, 2));
//        result.add(LineConvertors.filePattern(locator, RUBY_COMPILER_WIN_MY, EXT_RE, 1, 2));
//        result.add(LineConvertors.filePattern(locator, RUBY_COMPILER, EXT_RE, 1, 2));
//        result.add(LineConvertors.filePattern(locator, RUBY_COMPILER_WIN, EXT_RE, 1, 2));
        return result;
    }

    @Override
    public LineConvertor newLineConvertor() {
        final List<LineConvertor> convertorList = new ArrayList<>();

        if (convertors != null) {
            for (LineConvertor each : convertors) {
                if (each != null) {
                    convertorList.add(each);
                }
            }
        }

        if (stdConvertors) {
            convertorList.addAll(getStandardConvertors(locator));
        }
        return LineConvertors.proxy(convertorList.toArray(new LineConvertor[convertorList.size()]));
    }
}
