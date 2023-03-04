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
package org.netbeans.modules.web.inspect;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.sourcemap.Mapping;
import org.netbeans.modules.web.common.sourcemap.SourceMap;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/**
 * CSS-related utility methods.
 *
 * @author Jan Stola
 */
public class CSSUtils {
    /** Prefixes of CSS properties used by various vendors. */
    private static final String[] vendorPropertyPrefixes = new String[] {
        "-moz-", // NOI18N
        "-webkit-", // NOI18N
        "-ms-", // NOI18N
        "-o-" // NOI18N
    };

    private static final List<String> inheritedProperties = Arrays.asList(new String[] {
        "azimuth", // NOI18N
        "border-collapse", // NOI18N
        "border-spacing", // NOI18N
        "caption-side", // NOI18N
        "color", // NOI18N
        "cursor", // NOI18N
        "direction", // NOI18N
        "elevation", // NOI18N
        "empty-cells", // NOI18N
        "font-family", // NOI18N
        "font-size", // NOI18N
        "font-style", // NOI18N
        "font-variant", // NOI18N
        "font-weight", // NOI18N
        "font", // NOI18N
        "letter-spacing", // NOI18N
        "line-height", // NOI18N
        "list-style-image", // NOI18N
        "list-style-position", // NOI18N
        "list-style-type", // NOI18N
        "list-style", // NOI18N
        "orphans", // NOI18N
        "pitch-range", // NOI18N
        "pitch", // NOI18N
        "quotes", // NOI18N
        "richness", // NOI18N
        "speak-header", // NOI18N
        "speak-numeral", // NOI18N
        "speak-punctuation", // NOI18N
        "speak", // NOI18N
        "speech-rate", // NOI18N
        "stress", // NOI18N
        "text-align", // NOI18N
        "text-indent", // NOI18N
        "text-transform", // NOI18N
        "text-shadow", // NOI18N
        "visibility", // NOI18N
        "voice-family", // NOI18N
        "volume", // NOI18N
        "white-space", // NOI18N
        "widows", // NOI18N
        "word-spacing" // NOI18N
    });
    /** Name of the class that is used to simulate hovering. */
    public static final String HOVER_CLASS = "-netbeans-hover"; // NOI18N

    /**
     * Determines whether the CSS property with the specified name is inherited.
     * 
     * @param name name of the property.
     * @return {@code true} when the property is inherited,
     * returns {@code false} otherwise.
     */
    public static boolean isInheritedProperty(String name) {
        for (String propertyName : possiblePropertyNames(name)) {
            if (inheritedProperties.contains(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns possible known (base) names of the given property
     * (the returned list contains all candidates with
     * possible prefixes/suffixes removed).
     * 
     * @param name name of the property.
     * @return list of possible (base) names of the given property.
     */
    private static List<String> possiblePropertyNames(String name) {
        List<String> names = new ArrayList<String>();
        names.add(name);
        
        // -moz-color => color
        for (String prefix : vendorPropertyPrefixes) {
            if (name.startsWith(prefix)) {
                String withoutPrefix = name.substring(prefix.length());
                names.add(withoutPrefix);
            }
        }

        return names;
    }

    /**
     * Determines whether the specified CSS value means that the actual
     * value should be inherited from the parent.
     * 
     * @param value value to check.
     * @return {@code true} if the actual value should be inherited,
     * returns {@code false} otherwise.
     */
    public static boolean isInheritValue(String value) {
        return value.trim().startsWith("inherit"); // NOI18N
    }

    /**
     * Opens the specified file at the given offset.
     * 
     * @param fob file that should be opened.
     * @param offset offset where the caret should be placed.
     * @return {@code true} when the file was opened successfully,
     * returns {@code false} otherwise.
     */
    public static boolean openAtOffset(FileObject fob, int offset) {
        return openAt(fob, -1, offset);
    }

    /**
     * Opens the specified file at the given line.
     * 
     * @param fob file that should be opened.
     * @param lineNo line where the caret should be placed.
     * @return {@code true} when the file was opened successfully,
     * returns {@code false} otherwise.
     */
    public static boolean openAtLine(FileObject fob, int lineNo) {
        return openAt(fob, lineNo, 0);
    }

    /**
     * Opens the specified file at the given line and column.
     * 
     * @param fob file that should be opened.
     * @param lineNo line where the caret should be placed.
     * @param columnNo column where the caret should be placed.
     * @return {@code true} when the file was opened successfully,
     * returns {@code false} otherwise.
     */
    public static boolean openAtLineAndColumn(FileObject fob, int lineNo, int columnNo) {
        return openAt(fob, lineNo, columnNo);
    }

    /**
     * Opens the specified file at the given position. The position is either
     * offset (when {@code lineNo} is -1) or line/column otherwise.
     * This method has been copied (with minor modifications) from UiUtils
     * class in csl.api module. This method is not CSS-specific. It was placed
     * into this file just because there was no better place.
     * 
     * @param fob file that should be opened.
     * @param lineNo line where the caret should be placed
     * (or -1 when the {@code columnNo} represents an offset).
     * @param columnNo column (or offset when {@code lineNo} is -1)
     * where the caret should be placed.
     * @return {@code true} when the file was opened successfully,
     * returns {@code false} otherwise.
     */
    private static boolean openAt(FileObject fob, int lineNo, int columnNo) {
        try {
            DataObject dob = DataObject.find(fob);
            Lookup dobLookup = dob.getLookup();
            EditorCookie ec = dobLookup.lookup(EditorCookie.class);
            LineCookie lc = dobLookup.lookup(LineCookie.class);
            OpenCookie oc = dobLookup.lookup(OpenCookie.class);

            if ((ec != null) && (lc != null) && (columnNo != -1)) {
                StyledDocument doc;
                try {
                    doc = ec.openDocument();
                } catch (UserQuestionException uqe) {
                    String title = NbBundle.getMessage(
                            CSSUtils.class,
                            "CSSUtils.openQuestion"); // NOI18N
                    Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                            uqe.getLocalizedMessage(),
                            title,
                            NotifyDescriptor.YES_NO_OPTION));
                    if (value != NotifyDescriptor.YES_OPTION) {
                        return false;
                    }
                    uqe.confirmed();
                    doc = ec.openDocument();
                }

                if (doc != null) {
                    boolean offset = (lineNo == -1);
                    int line = offset ? NbDocument.findLineNumber(doc, columnNo) : lineNo;
                    int column = offset ? columnNo - NbDocument.findLineOffset(doc, line) : columnNo;
                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);
                        if (l != null) {
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, column);
                            return true;
                        }
                    }
                }
            }

            if (oc != null) {
                oc.open();
                return true;
            }
        } catch (IOException ioe) {
            Logger.getLogger(CSSUtils.class.getName()).log(Level.INFO, null, ioe);
        }

        return false;
    }

    /**
     * Returns an unspecified "normalized" version of the selector suitable
     * for {@code String} comparison with other normalized selectors.
     *
     * @param selector selector to normalize.
     * @return "normalized" version of the selector.
     */
    public static String normalizeSelector(String selector) {
        // Hack that simplifies the following cycle: adding a dummy
        // character that ensures that the last group is ended.
        // This character is removed at the end of this method.
        selector += 'A';
        String whitespaceChars = " \t\n\r\f\""; // NOI18N
        String specialChars = ".>+~#:*()[]|,="; // NOI18N
        StringBuilder main = new StringBuilder();
        StringBuilder group = null;
        for (int i=0; i<selector.length(); i++) {
            char c = selector.charAt(i);
            boolean whitespace = (whitespaceChars.indexOf(c) != -1);
            boolean special = (specialChars.indexOf(c) != -1);
            if (whitespace || special) {
                if (group == null) {
                    group = new StringBuilder();
                }
                if (special) {
                    group.append(c);
                }
            } else {
                if (group != null) {
                    if (group.length() == 0) {
                        // whitespace only group => insert single space instead
                        main.append(' ');
                    } else {
                        // group with special chars
                        main.append(group);
                    }
                    group = null;
                }
                main.append(c);
            }
        }
        // Removing the dummy character added at the beginning of the method
        return main.substring(0, main.length()-1).trim();
    }

    /**
     * Returns an unspecified "normalized" version of the media query suitable
     * for {@code String} comparison with other normalized media queries.
     *
     * @param mediaQueryList media query list to normalize.
     * @return "normalized" version of the media query.
     */
    public static String normalizeMediaQuery(String mediaQueryList) {
        mediaQueryList = mediaQueryList.trim().toLowerCase(Locale.ENGLISH);
        StringBuilder result = new StringBuilder();
        StringTokenizer st = new StringTokenizer(mediaQueryList, ","); // NOI18N
        while (st.hasMoreTokens()) {
            String mediaQuery = st.nextToken();
            int index;
            List<String> parts = new ArrayList<String>();
            while ((index = mediaQuery.indexOf("and")) != -1) { // NOI18N
                String part = mediaQuery.substring(0,index);
                mediaQuery = mediaQuery.substring(index+3);
                // 'part' is not a selector, but the same normalization
                // works well here as well.
                part = normalizeSelector(part);
                parts.add(part);
            }
            mediaQuery = normalizeSelector(mediaQuery);
            parts.add(mediaQuery);
            Collections.sort(parts);
            Collections.reverse(parts); // Make sure that media type is before expressions
            for (int i=0; i<parts.size(); i++) {
                if (i != 0) {
                    result.append(" and "); // NOI18N
                }
                String part = parts.get(i);
                result.append(part);
            }
            if (st.hasMoreTokens()) {
                result.append(", "); // NOI18N
            }
        }
        return result.toString();
    }

    /**
     * Determines whether the CSS property with the specified name
     * determines some color.
     * 
     * @param propertyName name of the property to check.
     * @return {@code true} when the given property determines some color,
     * returns {@code false} otherwise.
     */
    public static boolean isColorProperty(String propertyName) {
        // Simple heuristics
        return propertyName.contains("color"); // NOI18N
    }

    /**
     * Jumps into the location given by a source map.
     * 
     * @param cssFile compiled CSS file.
     * @param sourceModel source model corresponding to the CSS file.
     * @param styleSheetText text of the style-sheet that corresponds to the CSS file.
     * @param offset offset in the compiled CSS file.
     * @return {@code true} if the file contains source map information
     * and this information was used successfully to open the source file,
     * returns {@code false} otherwise.
     */
    public static boolean goToSourceBySourceMap(FileObject cssFile, Model sourceModel, String styleSheetText, int offset) {
        if (styleSheetText != null) {
            String sourceMapPath = sourceMapPath(styleSheetText);
            if (sourceMapPath != null) {
                FileObject folder = cssFile.getParent();
                FileObject sourceMapFob = folder.getFileObject(sourceMapPath);
                if (sourceMapFob != null) {
                    try {
                        CharSequence modelSource = sourceModel.getModelSource();
                        int line = LexerUtils.getLineOffset(modelSource, offset);
                        int lineStartOffset = LexerUtils.getLineBeginningOffset(modelSource, line);
                        int column = offset-lineStartOffset;
                        String sourceMapText = sourceMapFob.asText();
                        SourceMap sourceMap = SourceMap.parse(sourceMapText);
                        final Mapping mapping = sourceMap.findMapping(line, column);
                        if (mapping == null) {
                            Logger.getLogger(CSSUtils.class.getName()).log(Level.INFO,
                                "No mapping for line {0} and column {1}!", new Object[] {line, column}); // NOI18N
                        } else {
                            int sourceIndex = mapping.getSourceIndex();
                            String sourcePath = sourceMap.getSourcePath(sourceIndex);
                            folder = sourceMapFob.getParent();
                            final FileObject source = folder.getFileObject(sourcePath);
                            boolean validSourceFile = (source != null);
                            if (validSourceFile) {
                                EventQueue.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        int line = mapping.getOriginalLine();
                                        int column = mapping.getOriginalColumn();
                                        CSSUtils.openAtLineAndColumn(source, line, column);
                                    }
                                });
                            } else {
                                // Invalid path in the source map.
                                Logger.getLogger(CSSUtils.class.getName()).log(Level.INFO,
                                        "Unable to find the file {0} relative to the source map {1}!",
                                        new Object[] {sourcePath, sourceMapFob.getPath()});
                            }
                            return validSourceFile;
                        }
                    } catch (IOException ioex) {
                        Exceptions.printStackTrace(ioex);
                    } catch (BadLocationException blex) {
                        Logger.getLogger(CSSUtils.class.getName()).log(Level.INFO, null, blex);
                    }
                }
            }
        }
        return false;
    }

    /** Pattern for the source mapping URL in CSS files. */
    private static final Pattern SOURCE_MAPPING_PATTERN = Pattern.compile("/\\*[#@]\\s+sourceMappingURL=(.*)\\s+\\*/"); // NOI18N

    /**
     * Returns the path to the source map.
     * 
     * @param cssText text of some file.
     * @return path to the source map or {@code null} if there is no source
     * map information in the given text.
     */
    public static String sourceMapPath(String cssText) {
        String result = null;
        if (cssText != null) {
            Matcher matcher = SOURCE_MAPPING_PATTERN.matcher(cssText);
            if (matcher.find()) {
                result = matcher.group(1);
            }
        }
        return result;
    }

}
