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
package org.netbeans.modules.python.source;

import java.awt.Color;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.source.elements.Element;
import org.netbeans.modules.python.source.elements.IndexedElement;
import org.netbeans.modules.python.source.elements.IndexedMethod;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.python.antlr.PythonTree;

/**
 * Support for reStructured text. Parse .rst files and rst content in
 * Python doc strings and format it as HTML. Also provide functions
 * to locate a code element in RST files.
 * @see http://www.python.org/dev/peps/pep-0287/
 * @see http://docutils.sourceforge.net/docs/user/rst/quickstart.html
 *
 * @todo Render verbatim blocks
 * @todo Syntax highlight verbatim blocks?
 * @todo Render *bold* and `identifier` stuff
 * @todo For class definitions which nest the method documentation,
 *    try to remove all items, or perhaps just make it shorter
 * @todo Render note:: into something cleaner etc.
 *
 */
public class RstFormatter {
    private static final String BRBR = "\n<br><br>\n"; // NOI18N
    private StringBuilder sb = new StringBuilder();
    private boolean lastWasEmpty = false;
    private int beginPos;
    private boolean inVerbatim;
    private boolean inIndex;
    private boolean inTable;
    private boolean inDiv;
    private int lastIndent;
    private boolean maybeVerbatim;
    private boolean inDocTest;
    private List<String> code;

    public RstFormatter() {
    }

    private void flush() {
        if (inTable) {
            sb.append("</pre>\n"); // NOI18N
            inTable = false;
            inVerbatim = false;
        } else if (inVerbatim) {
            // Process code and format as Python
            String html = getPythonHtml(code, true);
            if (html != null) {
                // <pre> tag is added as part of the rubyhtml (since it
                // needs to pick up the background color from the syntax
                // coloring settings)
                sb.append(html);
            } else {
                sb.append("<pre style=\"margin: 5px 5px; background: #ffffdd; border-size: 1px; padding: 5px\">"); // NOI18N
                sb.append("\n"); // NOI18N
                // Some kind of error; normal append
                for (String s : code) {
                    appendEscaped(s);
                    sb.append("<br>"); // NOI18N
                }
                sb.append("</pre>\n"); // NOI18N
            }
            inVerbatim = false;
            code = null;
        } else if (inDiv) {
            sb.append("</div>\n"); // NOI18N
            inDiv = false;
        }
    }

    private void appendEscaped(char c) {
        if ('<' == c) {
            sb.append("&lt;"); // NOI18N
        } else if ('&' == c) {
            sb.append("&amp;"); // NOI18N
        } else {
            sb.append(c);
        }
    }

    private void appendEscaped(CharSequence s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if ('<' == c) {
                sb.append("&lt;"); // NOI18N
            } else if ('&' == c) {
                sb.append("&amp;"); // NOI18N
            } else {
                sb.append(c);
            }
        }
    }

    private int appendColonCmd(String line, int i, String marker, boolean url) throws CharConversionException {
        String MARKER = ":" + marker + ":`"; // NOI18N
        if (line.startsWith(MARKER, i)) {
            int end = line.indexOf("`", i + MARKER.length()); // NOI18N
            if (end != -1) {
                String token = line.substring(i + MARKER.length(), end);
                if (url) {
                    sb.append("<a href=\""); // NOI18N
                    if ("pep".equals(marker)) { // NOI18N
                        sb.append("http://www.python.org/dev/peps/pep-"); // NOI18N
                        for (int j = 0; j < 4 - token.length(); j++) {
                            sb.append("0");
                        }
                        sb.append(token);
                        sb.append("/"); // NOI18N
                        sb.append("\">PEP "); // NOI18N
                        sb.append(token);
                        sb.append("</a>");
                        return end;
                    } else {
                        sb.append(marker);
                        sb.append(":"); // NOI18N
                        appendEscaped(token);
                    }
                    sb.append("\">"); // NOI18N
                } else if (marker.equals("keyword")) { // NOI18N
                    sb.append("<code style=\""); // NOI18N

                    MimePath mimePath = MimePath.parse(PythonMIMEResolver.PYTHON_MIME_TYPE);
                    Lookup lookup = MimeLookup.getLookup(mimePath);
                    FontColorSettings fcs = lookup.lookup(FontColorSettings.class);

                    AttributeSet attribs = fcs.getTokenFontColors("keyword"); // NOI18N
                    Color fg = (Color)attribs.getAttribute(StyleConstants.Foreground);
                    if (fg != null) {
                        sb.append("color:"); // NOI18N
                        sb.append(getHtmlColor(fg));
                        sb.append(";"); // NOI18N
                    }
                    Color bg = (Color)attribs.getAttribute(StyleConstants.Background);
                    // Only set the background for dark colors
                    if (bg != null && bg.getRed() < 128) {
                        sb.append("background:"); // NOI18N
                        sb.append(getHtmlColor(bg));
                    }

                    sb.append("\">"); // NOI18N
                } else {
                    sb.append("<code>"); // NOI18N
                }
                appendEscaped(token);
                if (url) {
                    sb.append("</a>"); // NOI18N
                } else {
                    sb.append("</code>"); // NOI18N
                }
                //return end+1; // instead of end+2: get to end of ``, minus loop increment
                return end; // instead of end+2: get to end of ``, minus loop increment
            }
        }

        return -1;
    }

    private void appendRstLine(String line) throws CharConversionException {
        int n = line.length();
        char prev = 0;
        Loop:
        for (int i = 0; i < n; i++) {
            char c = line.charAt(i);
            if (c == '`') {
                if (i < n - 2 && line.charAt(i + 1) == '`') {
                    // See if it's an ``identifier``
                    int end = line.indexOf("``", i + 2);
                    if (end != -1) {
                        sb.append("<code>"); // NOI18N
                        appendEscaped(line.substring(i + 2, end));
                        sb.append("</code>"); // NOI18N
                        i = end + 1; // instead of end+2: get to end of ``, minus loop increment
                        continue;
                    }
                } else {
                    // Single identifier
                    for (int j = i + 1; j < n; j++) {
                        char d = line.charAt(j);
                        if (d == '`') {
                            sb.append("<code>"); // NOI18N
                            appendEscaped(line.substring(i + 1, j));
                            sb.append("</code>"); // NOI18N
                            i = j;
                            continue Loop;
                        } else if (!Character.isJavaIdentifierPart(d)) {
                            break;
                        }
                    }
                }
            } else if (c == ':') {
                int nextI = appendColonCmd(line, i, "class", true); // NOI18N
                if (nextI == -1) {
                    nextI = appendColonCmd(line, i, "exc", true); // NOI18N
                    if (nextI == -1) {
                        nextI = appendColonCmd(line, i, "var", false); // NOI18N
                        if (nextI == -1) {
                            nextI = appendColonCmd(line, i, "meth", true); // NOI18N
                            if (nextI == -1) {
                                nextI = appendColonCmd(line, i, "func", true); // NOI18N
                                if (nextI == -1) {
                                    nextI = appendColonCmd(line, i, "data", false); // NOI18N
                                    if (nextI == -1) {
                                        nextI = appendColonCmd(line, i, "attr", false); // NOI18N
                                        if (nextI == -1) {
                                            nextI = appendColonCmd(line, i, "envvar", false); // NOI18N
                                            if (nextI == -1) {
                                                nextI = appendColonCmd(line, i, "mod", true); // NOI18N
                                                if (nextI == -1) {
                                                    nextI = appendColonCmd(line, i, "pep", true); // NOI18N
                                                    if (nextI == -1) {
                                                        nextI = appendColonCmd(line, i, "ref", false); // NOI18N
                                                        if (nextI == -1) {
                                                            nextI = appendColonCmd(line, i, "mod", true); // NOI18N
                                                            if (nextI == -1) {
                                                                nextI = appendColonCmd(line, i, "keyword", false); // NOI18N
                                                                if (nextI == -1) {
                                                                    if (line.startsWith(":noindex:", i)) { // NOI18N
                                                                        nextI = i + 9;
                                                                    } else if (line.startsWith(":synopsis:", i)) { // NOI18N
                                                                        nextI = i + 10;
                                                                    } else if (line.startsWith(":deprecated:", i)) {
                                                                        sb.append("Deprecated. ");
                                                                        nextI = i + 12;
                                                                    } else if (line.startsWith(":platform:", i)) {
                                                                        sb.append("Platform: ");
                                                                        nextI = i + 10;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (nextI != -1) {
                    i = nextI;
                    continue;
                }
            } else if (c == '*') {
                // Bold?
                if (i < n - 1 && Character.isJavaIdentifierPart(line.charAt(i + 1)) && !Character.isJavaIdentifierPart(prev)) { // TODO Use PythonUtils
                    // Peek ahead to see if we have [not-identifier-char]*[identifierchars]*[not-identifier-chars]
                    for (int j = i + 1; j < n; j++) {
                        char d = line.charAt(j);
                        if (d == '*') {
                            if (j == n - 1 || !Character.isJavaIdentifierPart(line.charAt(j + 1))) {
                                // Yess, make bold
                                sb.append("<b>"); // NOI18N
                                appendEscaped(line.substring(i + 1, j));
                                sb.append("</b>"); // NOI18N
                                i = j;
                                continue Loop;
                            }
                        } else if (!Character.isJavaIdentifierPart(d)) {
                            break;
                        }
                    }
                }
            } // TODO: :addedin, :deprecated, etc

            appendEscaped(c);
            prev = c;
        }
    }

    public void append(String line) {
        try {
            String trim = line.trim();
            if (trim.length() == 0) {
                inDocTest = false;
                if (inIndex) {
                    // Completely swallow all indexing entries
                    return;
                } else if (inTable) {
                    sb.append("</pre>\n"); // NOI18N
                    inVerbatim = false;
                    inTable = false;
                } else if (inVerbatim) {
                    sb.append("\n"); // NOI18N
                } else if (!lastWasEmpty && sb.length() > beginPos) {
                    sb.append(BRBR); // NOI18N
                }
                lastWasEmpty = true;
            } else {
                if (!lastWasEmpty && trim.startsWith("- ")) { // NOI18N
                    // lists - make sure they're on a new line
                    sb.append("<br>"); // NOI18N
                }

                lastWasEmpty = false;

                if (maybeVerbatim) {
                    int indent = getIndentation(line, 0);
                    if (indent > lastIndent) {
                        // Truncate last whitespace separator before <pre> if any
                        if (sb.length() > BRBR.length() && sb.substring(sb.length() - BRBR.length()).equals(BRBR)) {
                            sb.setLength(sb.length() - BRBR.length());
                        }
                        inVerbatim = true;
                        code = new ArrayList<>();
                        code.add(line);
                        maybeVerbatim = false;
                        return;
                    }
                    maybeVerbatim = false;
                } else if (inVerbatim || inTable) {
                    int indent = getIndentation(line, 0);
                    if (indent <= lastIndent) {
////                        // Truncate trailing whitespace
////                        while (sb.length() > 0 && sb.charAt(sb.length()-1) == '\n') {
////                            sb.setLength(sb.length()-1);
////                        }
////                        sb.append("</pre>"); // NOI18N
//                        inVerbatim = false;
//                        inTable = false;
                        flush();
                        lastWasEmpty = true;
                    } else if (inVerbatim) {
                        // We need to buffer up the text such that we can lex it as a unit
                        // (and determine when done with the section if it's code or regular text)
                        code.add(line);
                        return;
                    } else {
                        appendEscaped(line);
                        sb.append("\n"); // NOI18N
                        return;
                    }
                } else if (inDiv) {
                    int indent = getIndentation(line, 0);
                    if (indent <= lastIndent) {
                        // Truncate last whitespace separator before <pre> if any
                        if (sb.length() > BRBR.length() && sb.substring(sb.length() - BRBR.length()).equals(BRBR)) {
                            sb.setLength(sb.length() - BRBR.length());
                        }
                        sb.append("</div>\n"); // NOI18N
                        inDiv = false;
                        lastWasEmpty = true;
                    } else {
                        appendRstLine(line);
                        sb.append("\n"); // NOI18N
                        return;
                    }
                } else if (inIndex) {
                    int indent = getIndentation(line, 0);
                    if (indent <= lastIndent) {
                        inIndex = false;
                        lastWasEmpty = true;
                    } else {
                        return;
                    }

                }

                if (trim.startsWith(".. method:: ")) { // NOI18N
                    String sig = trim.substring(12).trim();
                    sb.append("<a href=\"meth:" + sig.replace("\"", "&quot;") + "\">" + sig + "</a>\n"); // NOI18N
                    return;
                } else if (trim.startsWith(".. function:: ")) { // NOI18N
                    String sig = trim.substring(14).trim();
                    sb.append("<a href=\"func:" + sig.replace("\"", "&quot;") + "\">" + sig + "</a>\n"); // NOI18N
                    return;
                } else if (trim.startsWith(".. class:: ")) { // NOI18N
                    String sig = trim.substring(11).trim();
                    sb.append("<a href=\"class:" + sig.replace("\"", "&quot;") + "\">" + sig + "</a>\n"); // NOI18N
                    return;
                } else if (trim.startsWith(".. attribute:: ")) { // NOI18N
                    String sig = trim.substring(15).trim();
                    sb.append("<a href=\"attr:" + sig.replace("\"", "&quot;") + "\">" + sig + "</a>\n"); // NOI18N
                    return;
                } else if (trim.startsWith(".. data:: ")) { // NOI18N
                    String sig = trim.substring(10).trim();
                    sb.append("<a href=\"data:" + sig.replace("\"", "&quot;") + "\">" + sig + "</a>\n"); // NOI18N
                    return;
                } else if (trim.startsWith(".. module:: ")) { // NOI18N
                    String sig = trim.substring(12).trim();
                    sb.append("<a href=\"module:" + sig.replace("\"", "&quot;") + "\">" + sig + "</a>"); // NOI18N
                    sb.append("<br>\n");
                    return;
                } else if (trim.startsWith(".. productionlist:")) {
                    lastIndent = getIndentation(line, 0);
                    inVerbatim = true;
                    code = new ArrayList<>();
                    code.add(line);
                    maybeVerbatim = false;
                    return;
                }

                if (trim.startsWith(">>>") || inDocTest) { // NOI18N
                    if (!trim.startsWith(">>>")) { // NOI18N
                        sb.append("<code>"); // NOI18N
                        appendEscaped(line); // NOI18N
                        // Wait until there is an empty line before we mark doctest done!
                        // inDocTest = false;
                        sb.append("</code><br>"); // NOI18N
                    } else {
                        sb.append("<code>"); // NOI18N
                        appendEscaped(">>>"); // NOI18N
                        String html = getPythonHtml(Collections.singletonList(trim.substring(3)), false);
                        sb.append(html);
                        sb.append("</code>"); // NOI18N
                        inDocTest = true;
                    }
                    return;
                }
                inDocTest = false;

                if (trim.startsWith(".. note::") || trim.startsWith(".. warning::") || trim.startsWith(".. seealso:")) { // NOI18N
                    // Truncate last whitespace separator before <pre> if any
                    if (sb.length() > BRBR.length() && sb.substring(sb.length() - BRBR.length()).equals(BRBR)) {
                        sb.setLength(sb.length() - BRBR.length());
                    }
                    sb.append("<div style=\"margin: 5px 5px; "); // NOI18N
                    if (!trim.contains("seealso")) { // NOI18N
                        sb.append("background: #ffdddd; "); // NOI18N
                    } else {
                        sb.append("background: #ddffdd; "); // NOI18N
                    }
                    sb.append("border-size: 1px; padding: 5px\">"); // NOI18N
                    if (trim.contains("note:")) {
                        sb.append("<b>NOTE</b>: "); // NOI18N
                    } else if (trim.contains("warning")) {
                        sb.append("<b>WARNING</b>: "); // NOI18N
                    } else {
                        sb.append("<b>See Also</b>: "); // NOI18N
                    }
                    sb.append("\n"); // NOI18N
                    inDiv = true;
                    lastIndent = getIndentation(line, 0);
                    maybeVerbatim = false;
                    return;
                } else if (trim.startsWith(".. versionadded::") || trim.startsWith(".. versionchanged::") || trim.startsWith(".. deprecated::")) { // NOI18N
                    // Truncate last whitespace separator before <pre> if any
                    if (sb.length() > BRBR.length() && sb.substring(sb.length() - BRBR.length()).equals(BRBR)) {
                        sb.setLength(sb.length() - BRBR.length());
                    }
                    sb.append("<div style=\"margin: 5px 5px; background: #dddddd; border-size: 1px; padding: 5px\">"); // NOI18N
                    if (trim.contains("added:")) {
                        sb.append("<b>Version Added</b>: "); // NOI18N
                    } else if (trim.contains("changed")) {
                        sb.append("<b>Version Changed</b>: "); // NOI18N
                    } else {
                        assert trim.contains("deprecated"); // NOI18N
                        sb.append("<b>Deprecated</b>: "); // NOI18N
                    }
                    sb.append(trim.substring(trim.indexOf("::") + 2));
                    sb.append("\n"); // NOI18N
                    inDiv = true;
                    lastIndent = getIndentation(line, 0);
                    maybeVerbatim = false;
                    return;
                } else if (trim.startsWith(".. index:")) { // NOI18N
                    inIndex = true;
                    lastIndent = getIndentation(line, 0);
                    return;
                } else if (trim.startsWith(".. _") && trim.endsWith(":")) {
                    // skip lines like .. _pyzipfile-objects: 
                    return;
                } else if (trim.startsWith(".. moduleauthor::") || trim.startsWith(".. sectionauthor::")) { // NOI18N
                    if (trim.startsWith(".. mod")) {
                        sb.append("<br>Module Author:</b>");
                    } else {
                        sb.append("<br>Section Author:</b>");
                    }
                    appendEscaped(trim.substring(trim.indexOf("::") + 2)); //
                    sb.append("\n");
                    return;
                } else if (trim.endsWith("::")) { // NOI18N
                    maybeVerbatim = true;
                    lastIndent = getIndentation(line, 0);
                } else if (trim.startsWith("+-----")) { // NOI18N
                    // A table
                    sb.append("<pre>"); // NOI18N
                    appendEscaped(line);
                    sb.append("\n"); // NOI18N
                    inTable = true;
                    lastIndent = getIndentation(line, 0) - 1;
                    return;
                } else if (line.startsWith("======") || line.startsWith("------") || line.startsWith("******") || line.startsWith("^^^^^^^^")) { // NOI18N
                    // PREVIOUS line could be a title.
                    // Note -- we're comparing on "line" and not "trim" here because in indented contexts,
                    //  === sometimes represents parts of tables -- see the turtle.rst file for examples.

                    int n = sb.length();
                    if (n > 0 && sb.charAt(n - 1) == '\n') {
                        n--;
                    }
                    int index = n - 1;
                    for (; index >= 0; index--) {
                        char c = sb.charAt(index);
                        if (c == '\n') {
                            index++;
                            break;
                        }
                    }
                    if (index == -1) {
                        index = 0;
                    }
                    // Index now points to the beginning of the previous line
                    boolean empty = true;
//                    boolean okay = true;
                    int start = index;
                    for (; index < n; index++) {
                        char c = sb.charAt(index);
                        if (c == '\n') {
                            break;
                        }
                        empty = false;
//                        if (c == '<') {
//                            okay = false;
//                        }
                    }
                    if (!empty/* && okay*/) {
                        String tag = "h2"; // NOI18N
                        if (line.startsWith("-") || line.startsWith("^")) { // NOI18N
                            tag = "h3"; // NOI18N
                        }
                        sb.insert(start, "<" + tag + ">"); // NOI18N
                        sb.append("</" + tag + ">\n"); // NOI18N
                        lastWasEmpty = true;
                        return;
                    }
                }

                //sb.append(line);
                appendRstLine(line);

                sb.append("\n"); // NOI18N
            }
        } catch (CharConversionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void appendSignature(Element element) {
        sb.append("<pre>"); // NOI18N

        if (element instanceof IndexedMethod) {
            IndexedMethod executable = (IndexedMethod)element;
            if (element.getIn() != null && !PythonIndex.isBuiltinModule(element.getIn())) {
                String in = element.getIn();
                sb.append("<i>"); // NOI18N
                sb.append(in);
                sb.append("</i>"); // NOI18N
                sb.append("<br>"); // NOI18N
            }
            // TODO - share this between Navigator implementation and here...
            sb.append("<b>"); // NOI18N
            sb.append(element.getName());
            sb.append("</b>"); // NOI18N
            String[] parameters = executable.getParams();

            if ((parameters != null) && (parameters.length > 0)) {
                sb.append("("); // NOI18N

                sb.append("<font color=\"#808080\">"); // NOI18N

                boolean first = true;
                for (String parameter : parameters) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(parameter);
                }

                sb.append("</font>"); // NOI18N

                sb.append(")"); // NOI18N
            }
        } else if (element instanceof IndexedElement) {
            //IndexedElement clz = (IndexedElement)element;
            String name = element.getName();
//            final String fqn = clz.getFqn();
//            if (fqn != null && !name.equals(fqn)) {
//                signature.append("<i>"); // NOI18N
//                signature.append(fqn); // NOI18N
//                signature.append("</i>"); // NOI18N
//                signature.append("<br>"); // NOI18N
//            }
            sb.append("<b>"); // NOI18N
            sb.append(name);
            sb.append("</b>"); // NOI18N
        } else {
            sb.append(element.getName());
        }

        sb.append("</pre>\n"); // NOI18N
    }

    public void appendHtml(String html) {
        sb.append(html);
    }

    public void markEmpty() {
        beginPos = sb.length();
    }

    public String toHtml() {
        flush();
        return sb.toString();
    }

    public static String document(String rst) {
        RstFormatter formatter = new RstFormatter();
        String[] lines = rst.split("\n"); // NOI18N
        for (String line : lines) {
            formatter.append(line);
        }
        return formatter.toHtml();
    }

    public static String getDocumentation(IndexedElement indexedElement) {
        RstFormatter formatter = new RstFormatter();
        FileObject fileObject = indexedElement.getFileObject();
        if (fileObject == null) {
            return null;
        }
        BaseDocument document = GsfUtilities.getDocument(fileObject, true);
        if (document == null) {
            return null;
        }

        String[] signatureHolder = new String[1];
        String rst = formatter.extractRst(indexedElement, document, signatureHolder);
        if (rst != null && rst.length() > 0) {
            String signature = signatureHolder[0];
            if (signature == null) {
                formatter.appendSignature(indexedElement);
                formatter.appendHtml("\n<hr>\n"); // NOI18N
                formatter.markEmpty();
            } else {
                formatter.appendHtml("<pre>"); // NOI18N
                formatter.appendHtml("<b>"); // NOI18N
                int paren = signature.indexOf('(');
                if (paren != -1) {
                    formatter.appendHtml(signature.substring(0, paren));
                    formatter.appendHtml("</b>"); // NOI18N
                    formatter.appendHtml("<font color=\"#808080\">"); // NOI18N
                    formatter.appendHtml(signature.substring(paren));
                    formatter.appendHtml("</font>"); // NOI18N
                } else {
                    formatter.appendHtml(signature);
                    formatter.appendHtml("()"); // NOI18N
                    formatter.appendHtml("</b>"); // NOI18N
                }
                formatter.appendHtml("</pre>"); // NOI18N
                formatter.appendHtml("\n<hr>\n"); // NOI18N
                formatter.markEmpty();
            }

            String[] lines = rst.split("\n"); // NOI18N
            for (String line : lines) {
                formatter.append(line);
            }
            return formatter.toHtml();
        }

        return null;
    }

    /**
     * Find the reStructured text for the documentation for the given element
     * in the given RST document
     * @param indexedElement
     * @param document
     * @return
     */
    private String extractRst(IndexedElement element, BaseDocument doc, String[] signatureHolder) {
        return extractRst(element.getName(), element.getClz(), element.getKind(), doc, signatureHolder);
    }

    String extractRst(String name, String clz, ElementKind kind, BaseDocument doc, String[] signatureHolder) {
        try {
            String text = doc.getText(0, doc.getLength());
            // What about functions?
            if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
                int offset = findElementMatch(text, "function::", name, true); // NOI18N
                if (offset == -1) {
                    offset = findElementMatch(text, "method::", name, false); // NOI18N
                    if (offset == -1 && kind == ElementKind.CONSTRUCTOR) {
                        offset = findElementMatch(text, "class::", name, false); // NOI18N
                        if (offset == -1 && clz != null && clz.length() > 0 && "__init__".equals(name)) { // NOI18N
                            offset = findElementMatch(text, "method::", clz, false); // NOI18N
                            if (offset == -1) {
                                offset = findElementMatch(text, "class::", clz, false); // NOI18N
                            }
                        }
                    }
                }
                if (offset != -1) {
                    int end = findElementEnd(text, offset);
                    int nextLine = getNextLineOffset(text, offset);
                    if (nextLine < end) {
                        if (signatureHolder != null) {
                            String signature = text.substring(text.indexOf("::", offset) + 2, nextLine).trim(); // NOI18N
                            signatureHolder[0] = signature;
                        }
                        return text.substring(nextLine, end);
                    }
                }
            } else if (kind == ElementKind.CLASS) {
                int offset = findElementMatch(text, "class::", name, false); // NOI18N
                if (offset == -1) {
                    offset = findElementMatch(text, "exception::", name, false); // NOI18N
                }
                if (offset != -1) {
                    int end = findElementEnd(text, offset);
                    int nextLine = getNextLineOffset(text, offset);
                    if (nextLine < end) {
                        String elementText = text.substring(nextLine, end);
                        return elementText;
                    }
                }
            } else if (kind == ElementKind.MODULE) {
                int offset = findElementMatch(text, "module::", name, false); // NOI18N
                if (offset == -1) {
                    offset = findElementMatch(text, "currentmodule::", name, false); // NOI18N
                }
                if (offset != -1) {
                    int end = findElementEnd(text, offset);
                    int nextLine = getNextLineOffset(text, offset);
                    if (nextLine < end) {
                        String elementText = text.substring(nextLine, end);
                        return elementText;
                    }
                }
            } else {
//                assert kind == ElementKind.ATTRIBUTE :;
                int offset = findElementMatch(text, "data::", name, true); // NOI18N
                if (offset == -1) {
                    offset = findElementMatch(text, "attribute::", name, false); // NOI18N
                }
                if (offset != -1) {
                    int end = findElementEnd(text, offset);
                    int nextLine = getNextLineOffset(text, offset);
                    if (nextLine < end) {
                        String elementText = text.substring(nextLine, end);
                        return elementText;
                    }
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }

//        while (true) {
//            try {
//                int ret = doc.find(new FinderFactory.StringFwdFinder(".. " + key + "::", true), offset, -1);
//                if (ret == -1) {
//                    break;
//                }
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }


        return "";
    }

    public static int getIndentation(String text, int lineBegin) {
        for (int i = lineBegin; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                // Empty lines don't count
                return -1;
            }
            if (!Character.isWhitespace(c)) {
                // Doesn't quite work for tabs etc. but those aren't
                // really used in rst files... Fix when I switch to
                // direct document iteration
                return i - lineBegin;
            }
        }

        return -1;
    }

    public static int getNextLineOffset(String text, int offset) {
        int index = text.indexOf('\n', offset);
        if (index == -1) {
            return -1;
        } else {
            return index + 1;
        }
    }

    public static int findElementEnd(String text, int offset) {
        // Find beginning of line
        int lineBegin = 0;
        for (int i = offset; i > 0; i--) {
            char c = text.charAt(i);
            if (c == '\n') {
                lineBegin = i + 1;
                break;
            }
        }

        // Compute indentation of the ..
        int firstIndent = getIndentation(text, lineBegin);
        offset = getNextLineOffset(text, lineBegin);
        while (true) {
            offset = getNextLineOffset(text, offset);
            if (offset == -1) {
                return text.length();
            }
            int indent = getIndentation(text, offset);
            if (indent == -1) {
                // Empty line - doesn't count
                continue;
            } else if (indent <= firstIndent) {
                return offset;
            }
        }
    }

    public static int findElementMatch(String text, String key, String name, boolean checkAdjacentLines) {
        int nameLength = name.length();
        int offset = 0;
        int keyLength = key.length();
        while (true) {
            int next = text.indexOf(key, offset);
            if (next == -1) {
                break;
            }
            offset = next + keyLength;

            int lineEnd = text.indexOf('\n', offset);
            if (lineEnd == -1) {
                lineEnd = text.length(); // on last line with no crlf at the end
            }

            // Skip whitespace
            for (; offset < lineEnd; offset++) {
                char c = text.charAt(offset);
                if (c != ' ') {
                    break;
                }
            }

            int nameBegin = offset;
            int nameEnd = -1;

            // Pick out the bame
            for (int i = offset; i < lineEnd; i++) {
                char c = text.charAt(i);
                if (c == '(' || c == ' ') {
                    nameEnd = i;
                    break;
                } else if (c == '.') {
                    nameBegin = i + 1;
                }
            }
            if (nameEnd == -1) {
                nameEnd = lineEnd;
            }


            if (nameEnd - nameBegin == nameLength &&
                    text.regionMatches(nameBegin, name, 0, nameLength)) {
                // TODO - validate the arguments list?
                return next;
            }

            // Look on subsequent lines too - we sometimes have adjacent lines
            // with additional signatures
            if (checkAdjacentLines) {
                while (true) {
                    int lineBegin = lineEnd+1;
                    if (lineBegin >= text.length()) {
                        break;
                    }

                    lineEnd = text.indexOf('\n', lineBegin);
                    if (lineEnd == -1) {
                        lineEnd = text.length(); // on last line with no crlf at the end
                    }

                    while (lineBegin < lineEnd) {
                        char c = text.charAt(lineBegin);
                        if (!Character.isWhitespace(c)) {
                            break;
                        }
                        lineBegin++;
                    }

                    while (lineEnd > lineBegin) {
                        char c = text.charAt(lineEnd-1);
                        if (!Character.isWhitespace(c)) {
                            break;
                        }
                        lineEnd--;
                    }

                    if (lineEnd <= lineBegin) {
                        break;
                    }

                    nameBegin = lineBegin;
                    nameEnd = -1;

                    // Pick out the name
                    for (int i = lineBegin; i < lineEnd; i++) {
                        char c = text.charAt(i);
                        if (c == '(' || c == ' ') {
                            nameEnd = i;
                            break;
                        } else if (c == '.') {
                            nameBegin = i + 1;
                        }
                    }
                    if (nameEnd == -1) {
                        nameEnd = lineEnd;
                    }

                    if (nameEnd - nameBegin == nameLength &&
                            text.regionMatches(nameBegin, name, 0, nameLength)) {
                        // TODO - validate the arguments list?
                        return next;
                    }
                }
            }

        }

        return -1;
    }

    public static String document(ParserResult info, ElementHandle element) {
        if (element instanceof IndexedElement) {
            IndexedElement indexedElement = (IndexedElement)element;

            FileObject fo = indexedElement.getFileObject();

            if (fo == null) {
                return null;
            }

            if (PythonUtils.isRstFile(fo)) {
                return getDocumentation(indexedElement);
            }


            PythonTree node = indexedElement.getNode();
            if (node != null) {
                return document(info, node, indexedElement);
            }
        }
        return null;
    }

    public static String document(ParserResult info, PythonTree node, IndexedElement element) {
        if (node != null) {
            String doc = PythonAstUtils.getDocumentation(node);
            if (doc != null) {
                // Honor empty lines: paragraphs
                RstFormatter formatter = new RstFormatter();
                if (element != null) {
                    formatter.appendSignature(element);
                }
                if (doc.indexOf('\n') != -1) {
                    formatter.appendHtml("\n<hr>\n"); // NOI18N
                    formatter.markEmpty();
                    String[] lines = doc.split("\n"); // NOI18N
                    for (String line : lines) {
                        formatter.append(line);
                    }
                } else if (doc.length() > 0) {
                    formatter.appendHtml("\n<hr>\n"); // NOI18N
                    formatter.markEmpty();
                    formatter.append(doc);
                }

                return formatter.toHtml();
            }
        }

        return null;
    }

    public static String getSignature(Element element) {
        StringBuilder signature = new StringBuilder();
        // TODO:
        signature.append("<pre>"); // NOI18N

        if (element instanceof IndexedMethod) {
            IndexedMethod executable = (IndexedMethod)element;
            if (element.getIn() != null) {
                String in = element.getIn();
                signature.append("<i>"); // NOI18N
                signature.append(in);
                signature.append("</i>"); // NOI18N
                signature.append("<br>"); // NOI18N
            }
            // TODO - share this between Navigator implementation and here...
            signature.append("<b>"); // NOI18N
            signature.append(element.getName());
            signature.append("</b>"); // NOI18N
            String[] parameters = executable.getParams();

            if ((parameters != null) && (parameters.length > 0)) {
                signature.append("("); // NOI18N

                signature.append("<font color=\"#808080\">"); // NOI18N

                boolean first = true;
                for (String parameter : parameters) {
                    if (first) {
                        first = false;
                    } else {
                        signature.append(", "); // NOI18N
                    }
                    signature.append(parameter);
                }

                signature.append("</font>"); // NOI18N

                signature.append(")"); // NOI18N
            }
        } else if (element instanceof IndexedElement) {
//            IndexedElement clz = (IndexedElement)element;
            String name = element.getName();
//            final String fqn = clz.getFqn();
//            if (fqn != null && !name.equals(fqn)) {
//                signature.append("<i>"); // NOI18N
//                signature.append(fqn); // NOI18N
//                signature.append("</i>"); // NOI18N
//                signature.append("<br>"); // NOI18N
//            }
            signature.append("<b>"); // NOI18N
            signature.append(name);
            signature.append("</b>"); // NOI18N
        } else {
            signature.append(element.getName());
        }

        signature.append("</pre>\n"); // NOI18N

        return signature.toString();
    }

    @SuppressWarnings("unchecked")
    private String getPythonHtml(List<String> source, boolean addPre) {
        StringBuilder python = new StringBuilder(500);

        for (String s : source) {
            python.append(s);
            python.append("\n"); // NOI18N
        }

        Language<?> language = PythonTokenId.language();
        String mimeType = PythonMIMEResolver.PYTHON_MIME_TYPE;
        // TODO - handle YAML and other languages I can see in the documentation...
        /*if (python.indexOf(" <%") != -1) { // NOI18N
        mimeType = "application/x-httpd-eruby"; // RHTML
        Collection<LanguageProvider> providers = (Collection<LanguageProvider>) Lookup.getDefault().lookupAll(LanguageProvider.class);
        for (LanguageProvider provider : providers) {
        language = provider.findLanguage(mimeType);
        if (language != null) {
        break;
        }
        }

        if (language == null) {
        mimeType = PythonTokenId.PYTHON_MIME_TYPE;
        language = PythonTokenId.language();
        }
        } else*/ if (source.get(0).trim().startsWith("<")) {
            // Looks like markup (other than RHTML) - don't colorize it
            // since we don't know how
            return null;
        }

        StringBuilder buffer = new StringBuilder(1500);

        boolean errors = appendSequence(buffer, python.toString(), language, mimeType, addPre);
        return errors ? null : buffer.toString();
    }

    @SuppressWarnings("unchecked")
    private boolean appendSequence(StringBuilder sb, String text,
            Language<?> language, String mimeType, boolean addPre) {
        // XXX is this getting called twice?
        MimePath mimePath = MimePath.parse(mimeType);
        Lookup lookup = MimeLookup.getLookup(mimePath);
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);

        if (addPre) {
            sb.append("<pre style=\""); // NOI18N

            sb.append("border-color: #dddddd; border-style: solid; border-width: 1px; ");

            AttributeSet attribs = fcs.getTokenFontColors("default"); // NOI18N
            Color fg = (Color)attribs.getAttribute(StyleConstants.Foreground);
            if (fg != null) {
                sb.append("color:"); // NOI18N
                sb.append(getHtmlColor(fg));
                sb.append(";"); // NOI18N
            }
            Color bg = (Color)attribs.getAttribute(StyleConstants.Background);
            // Only set the background for dark colors
            if (bg != null && bg.getRed() < 128) {
                sb.append("background:"); // NOI18N
                sb.append(getHtmlColor(bg));
            }

            sb.append("\">\n"); // NOI18N
        }
        TokenHierarchy hi = TokenHierarchy.create(text, language);
        TokenSequence ts = hi.tokenSequence();

        int offset = 0;
        ts.move(offset);

        if (ts.moveNext()) {
            do {
                Token t = ts.token();
                String tokenText = t.text().toString();

                // TODO - make style classes instead of inlining everything as font!
                String category = t.id().name();
                String primaryCategory = t.id().primaryCategory();

                if ("error".equals(primaryCategory)) { // NOI18N
                    // Abort: an error token means the output probably isn't
                    // code, or it's code or markup but in a different language
                    // than we're trying to process it as
                    return true;
                }

                AttributeSet attribs = fcs.getTokenFontColors(category);
                String escapedText = tokenText;
                try {
                    escapedText = XMLUtil.toElementContent(tokenText);
                } catch (CharConversionException cce) {
                    Exceptions.printStackTrace(cce);
                }

                if (attribs == null) {
                    category = primaryCategory;
                    attribs = fcs.getTokenFontColors(category);

                }

                TokenSequence embedded = ts.embedded();
                if (embedded != null) {
                    //embedded.languagePath().mimePath();
                    String embeddedMimeType = MimePath.parse(embedded.languagePath().mimePath()).getPath();
                    Color bg = null;
                    Color fg = null;
                    if (attribs != null) {
                        bg = (Color)attribs.getAttribute(StyleConstants.Background);
                        fg = (Color)attribs.getAttribute(StyleConstants.Foreground);
                        if (fg != null || bg != null) {
                            sb.append("<span style=\"");
                            if (bg != null) {
                                sb.append("background:"); // NOI18N
                                sb.append(getHtmlColor(bg));
                                sb.append(";");
                            }
                            if (fg != null) {
                                sb.append("color:"); // NOI18N
                                sb.append(getHtmlColor(fg));
                            }
                            sb.append("\">"); // NOI18N
                        }
                    }
                    appendSequence(sb, tokenText, embedded.language(), embeddedMimeType, false);
                    if (fg != null || bg != null) {
                        sb.append("</span>"); // NOI18N
                    }
                    continue;
                }

                if (attribs == null) {
                    sb.append(escapedText);

                    continue;
                }

                if (escapedText.indexOf('\n') != -1) {
                    escapedText = escapedText.replace("\n", "<br>"); // NOI18N
                }

                if (t.id() == PythonTokenId.WHITESPACE) {
                    sb.append(escapedText);
                } else {
                    sb.append("<span style=\""); // NOI18N

                    Color fg = (Color)attribs.getAttribute(StyleConstants.Foreground);

                    if (fg != null) {
                        sb.append("color:"); // NOI18N
                        sb.append(getHtmlColor(fg));
                        sb.append(";"); // NOI18N
                    }

                    Color bg = (Color)attribs.getAttribute(StyleConstants.Background);

                    if (bg != null) {
                        sb.append("background:"); // NOI18N
                        sb.append(getHtmlColor(bg));
                        sb.append(";"); // NOI18NP
                    }

                    Boolean b = (Boolean)attribs.getAttribute(StyleConstants.Bold);

                    if ((b != null) && b) {
                        sb.append("font-weight:bold;"); // NOI18N
                    }

                    b = (Boolean)attribs.getAttribute(StyleConstants.Italic);

                    if ((b != null) && b) {
                        sb.append("font-style:italic;"); // NOI18N
                    }

                    // TODO - underline, strikethrough, ... and FONTS!
                    sb.append("\">"); // NOI18N
                    sb.append(escapedText);
                    sb.append("</span>"); // NOI18N
                }
            } while (ts.moveNext());
        }

        if (addPre) {
            sb.append("</pre>\n");
        }

        return false;
    }


    // TODO - move to GsfUtilities?
    private static String getHtmlColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        StringBuffer result = new StringBuffer();
        result.append('#');

        String rs = Integer.toHexString(r);
        String gs = Integer.toHexString(g);
        String bs = Integer.toHexString(b);

        if (r < 0x10) {
            result.append('0');
        }

        result.append(rs);

        if (g < 0x10) {
            result.append('0');
        }

        result.append(gs);

        if (b < 0x10) {
            result.append('0');
        }

        result.append(bs);

        return result.toString();
    }
}
