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

package org.netbeans.api.extexecution.print;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.extexecution.open.DefaultFileOpenHandler;
import org.netbeans.modules.extexecution.open.DefaultHttpOpenHandler;
import org.netbeans.modules.extexecution.print.FileListener;
import org.netbeans.modules.extexecution.print.UrlListener;
import org.netbeans.spi.extexecution.open.FileOpenHandler;
import org.netbeans.spi.extexecution.open.HttpOpenHandler;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.windows.OutputListener;

/**
 * Factory methods for {@link LineConvertor} classes.
 *
 * @author Petr Hejl
 */
public final class LineConvertors {

    private static final Logger LOGGER = Logger.getLogger(LineConvertors.class.getName());

    private static final DefaultFileOpenHandler DEFAULT_FILE_HANDLER = new DefaultFileOpenHandler();
    
    private static final DefaultHttpOpenHandler DEFAULT_HTTP_HANDLER = new DefaultHttpOpenHandler();

    private LineConvertors() {
        super();
    }

    /**
     * Returns the convertor that will delegate to passed converters.
     * <p>
     * On call to {@link LineConvertor#convert(java.lang.String)} converters
     * are asked one by one for conversion in the same order they were
     * passed to constructor. The first non <code>null</code> value
     * is returned. If all coverters return <code>null</code> for particular
     * line <code>null</code> is returned.
     * <p>
     * Returned convertor is <i>not thread safe</i>.
     *
     * @param convertors convertors that will be proxied; the same order
     *            is used on {@link LineConvertor#convert(java.lang.String)}
     *            invocation
     * @return the convertor proxying passed convertors
     */
    @NonNull
    public static LineConvertor proxy(@NonNull LineConvertor... convertors) {
        return new ProxyLineConvertor(convertors);
    }

    /**
     * Returns the convertor searching for lines matching the patterns,
     * considering matched lines as being files.
     * <p>
     * Convertor is trying to mach each line against the given
     * <code>linePattern</code>. If the line matches the regexp group number
     * <code>fileGroup</code> is supposed to be filename. This filename is then
     * checked whether it matches <code>filePattern</code> (if any).
     * <p>
     * In next step converter tries to determine the line in file. The line
     * is parsed as <code>lineGroup</code> regexp group. Line number begins
     * with <code>1</code> (first line of the file). If resulting value
     * representing line number can't be parsed or is less then or equal
     * to zero the {@link OutputListener} associated with converted line
     * will use value <code>1</code> as a line number.
     * <p>
     * When the line does not match the <code>linePattern</code> or
     * received filename does not match <code>filePattern</code>
     * <code>null</code> is returned.
     * <p>
     * Resulting converted line contains the original text and a listener
     * that consults the <code>fileLocator</code> (if any) when clicked
     * and displays the received file in the editor. If <code>fileLocator</code>
     * is <code>null</code> output listener will try to find file simply by
     * <code>new File(filename)</code> checking its existence by
     * <code>isFile()</code>.
     * <p>
     * Returned convertor is <i>not thread safe</i>.
     *
     * @param fileLocator locator that is consulted for real file; used in
     *             listener for the converted line; may be <code>null</code>
     * @param linePattern pattern for matching the line
     * @param filePattern pattern for matching once received filenames;
     *             may be <code>null</code>
     * @param fileGroup regexp group supposed to be the filename;
     *             only nonnegative numbers allowed
     * @param lineGroup regexp group supposed to be the line number;
     *             if negative line number is not parsed
     * @return the convertor searching for lines matching the patterns,
     *             considering matched lines as being files (names or paths)
     * @see FileOpenHandler
     */
    @NonNull
    public static LineConvertor filePattern(@NullAllowed FileLocator fileLocator, @NonNull Pattern linePattern,
            @NullAllowed Pattern filePattern, int fileGroup, int lineGroup) {

        Parameters.notNull("linePattern", linePattern);
        if (fileGroup < 0) {
            throw new IllegalArgumentException("File goup must be non negative: " + fileGroup); // NOI18N
        }

        return new FilePatternConvertor(fileLocator, linePattern, filePattern, fileGroup, lineGroup);
    }

    /**
     * Returns the convertor parsing the line and searching for
     * <code>http</code> or <code>https</code> URL.
     * <p>
     * Converted line returned from the processor consist from the original
     * line and listener opening browser with recognized url on click.
     * <p>
     * If line is not recognized as <code>http</code> or <code>https</code>
     * URL <code>null</code> is returned.
     * <p>
     * Returned convertor is <i>not thread safe</i>.
     *
     * @return the convertor parsing the line and searching for
     *             <code>http</code> or <code>https</code> URL
     * @see HttpOpenHandler
     */
    @NonNull
    public static LineConvertor httpUrl() {
        return new HttpUrlConvertor();
    }

    /**
     * Locates the file for the given path, file or part of the path.
     *
     * @see LineConvertors#filePattern(org.netbeans.api.extexecution.print.LineConvertors.FileLocator, java.util.regex.Pattern, java.util.regex.Pattern, int, int)
     */
    public interface FileLocator {

        /**
         * Returns the file corresponding to the filename (or path) or
         * <code>null</code> if locator can't find the file.
         *
         * @param filename name of the file
         * @return the file corresponding to the filename (or path) or
         *             <code>null</code> if locator can't find the file
         */
        @CheckForNull
        FileObject find(@NonNull String filename);

    }

    private static class ProxyLineConvertor implements LineConvertor {

        private final List<LineConvertor> convertors = new ArrayList<LineConvertor>();

        public ProxyLineConvertor(LineConvertor... convertors) {
            for (LineConvertor convertor : convertors) {
                if (convertor != null) {
                    this.convertors.add(convertor);
                }
            }
        }
        public List<ConvertedLine> convert(String line) {
            for (LineConvertor convertor : convertors) {
                List<ConvertedLine> converted = convertor.convert(line);
                if (converted != null) {
                    return converted;
                }
            }

            return null;
        }

    }

    private static class FilePatternConvertor implements LineConvertor {

        private final FileOpenHandler handler;

        private final FileLocator locator;

        private final Pattern linePattern;

        private final Pattern filePattern;

        private final int fileGroup;

        private final int lineGroup;

        public FilePatternConvertor(FileLocator locator, Pattern linePattern,
                Pattern filePattern) {
            this(locator, linePattern, filePattern, 1, 2);
        }

        public FilePatternConvertor(FileLocator locator, Pattern linePattern,
                Pattern filePattern, int fileGroup, int lineGroup) {

            FileOpenHandler candidate = Lookup.getDefault().lookup(FileOpenHandler.class);
            if (candidate != null) {
                handler = candidate;
            } else {
                handler = DEFAULT_FILE_HANDLER;
            }

            this.locator = locator;
            this.linePattern = linePattern;
            this.fileGroup = fileGroup;
            this.lineGroup = lineGroup;
            this.filePattern = filePattern;
        }

        public List<ConvertedLine> convert(final String line) {
            // Don't try to match lines that are too long - the java.util.regex library
            // throws stack exceptions (101234)
            if (line.length() > 400) {
                return null;
            }

            Matcher match = linePattern.matcher(line);

            if (match.matches()) {
                String file = null;
                int lineno = -1;

                if (fileGroup >= 0) {
                    file = match.group(fileGroup);
                    // Make some adjustments - easier to do here than in the regular expression
                    // (See 109721 and 109724 for example)
                    if (file.startsWith("\"")) { // NOI18N
                        file = file.substring(1);
                    }
                    if (file.startsWith("./")) { // NOI18N
                        file = file.substring(2);
                    }
                    if (filePattern != null && !filePattern.matcher(file).matches()) {
                        return null;
                    }
                }

                if (lineGroup >= 0) {
                    String linenoStr = match.group(lineGroup);

                    try {
                        lineno = Integer.parseInt(linenoStr);
                    } catch (NumberFormatException nfe) {
                        LOGGER.log(Level.INFO, null, nfe);
                        lineno = 0;
                    }
                }

                return Collections.<ConvertedLine>singletonList(
                        ConvertedLine.forText(line,
                        new FileListener(file, lineno, locator, handler)));
            }

            return null;
        }
    }

    private static class HttpUrlConvertor implements LineConvertor {

        private final Pattern pattern = Pattern.compile(".*(((http)|(https))://\\S+)(\\s.*|$)"); // NOI18N

        private final HttpOpenHandler handler;

        public HttpUrlConvertor() {
            HttpOpenHandler candidate = Lookup.getDefault().lookup(HttpOpenHandler.class);
            if (candidate != null) {
                handler = candidate;
            } else {
                handler = DEFAULT_HTTP_HANDLER;
            }
        }

        @Override
        public List<ConvertedLine> convert(String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String stringUrl = matcher.group(1);
                try {
                    URL url = new URL(stringUrl);
                    return Collections.<ConvertedLine>singletonList(
                            ConvertedLine.forText(line, new UrlListener(url, handler)));
                } catch (MalformedURLException ex) {
                    // return null
                }
            }

            return null;
        }

    }
}
