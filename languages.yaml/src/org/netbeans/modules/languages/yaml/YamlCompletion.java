/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.yaml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * YAML code completion.
 *
 * @author Tor Norbye
 */
public class YamlCompletion implements CodeCompletionHandler {

    private String refcard;
    // Based http://www.yaml.org/refcard.html
    private static final String[] YAML_KEYS =
            new String[]{
        "? ", "Key indicator.",
        ": ", "Value indicator.",
        "- ", "Nested series entry indicator.",
        ", ", "Separate in-line branch entries.",
        "[]", "Surround in-line series branch.",
        "{}", "Surround in-line keyed branch.",
        "'", "Surround in-line unescaped scalar",
        "\"", "Surround in-line escaped scalar",
        "|", "Block scalar indicator.",
        ">", "Folded scalar indicator.",
        "-", "Strip chomp modifier ('|-' or '>-').",
        "+", "Keep chomp modifier ('|+' or '>+').",
        //1-9  : Explicit indentation modifier ("|1", or '>2').
        //       # Modifiers can be combined ('|2-', '>+1').
        "&", "Anchor property.",
        "*", "Alias indicator.",
        //"none",  "Unspecified tag (automatically resolved by application).",
        "!", "Non-specific tag",
        "!foo", "Primary",
        "!!foo", "Secondary",
        "!h!foo", "Requires \"%TAG !h! <prefix>\"",
        "!<foo>", "Verbatim tag (always means \"foo\").",
        "%", "Directive indicator.",
        "---", "Document header.",
        "...", "Document terminator.",
        "#", "Throwaway comment indicator.",
        "`@", "Both reserved for future use.",
        "=", "Default \"value\" mapping key.",
        "<<", "Merge keys from another mapping.",
        "!!map", "{ Hash table, dictionary, mapping }",
        "!!seq", "{ List, array, tuple, vector, sequence }",
        "!!str", "Unicode string",
        "!!set", "{ cherries, plums, apples }",
        "!!omap", "[ one: 1, two: 2 ]", //{ ~, null }              : Null (no value).
    //[ 1234, 0x4D2, 02333 ]   : [ Decimal int, Hexadecimal int, Octal int ]
    //[ 1_230.15, 12.3015e+02 ]: [ Fixed float, Exponential float ]
    //[ .inf, -.Inf, .NAN ]    : [ Infinity (float), Negative, Not a number ]
    };

    private boolean startsWith(String theString, String prefix, boolean caseSensitive) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        boolean caseSensitive = context.isCaseSensitive();
        String prefix = context.getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        int anchor = context.getCaretOffset();

        // Regular expression matching.  {
        for (int i = 0, n = YAML_KEYS.length; i < n; i += 2) {
            String word = YAML_KEYS[i];
            String desc = YAML_KEYS[i + 1];

            if (startsWith(word, prefix, caseSensitive)) {
                KeywordItem item = new KeywordItem(word, desc, anchor, Integer.toString(10000 + i));
                proposals.add(item);
            }
        }

        if (proposals.isEmpty()) {
            // Prefix isn't any of the chars -- so just add all
            for (int i = 0, n = YAML_KEYS.length; i < n; i += 2) {
                String word = YAML_KEYS[i];
                String desc = YAML_KEYS[i + 1];

                KeywordItem item = new KeywordItem(word, desc, anchor, Integer.toString(10000 + i));
                proposals.add(item);
            }
        }
        DefaultCompletionResult result = new DefaultCompletionResult(proposals, false);
        result.setFilterable(false);
        return result;
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        if (refcard == null) {
            refcard = ""; // NOI18N
            // TODO: I18N
            InputStream is = null;
            StringBuilder sb = new StringBuilder();

            try {
                is = new BufferedInputStream(YamlCompletion.class.getResourceAsStream("refcard.html"));
                while (true) {
                    int c = is.read();

                    if (c == -1) {
                        break;
                    }

                    sb.append((char) c);
                }

                if (sb.length() > 0) {
                    refcard = sb.toString();
                }
            } catch (IOException ie) {
                Exceptions.printStackTrace(ie);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ie) {
                    Exceptions.printStackTrace(ie);
                }
            }
        }

        return refcard.length() > 0 ? refcard : null;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        if (caretOffset > 0) {
            try {
                Document doc = ((YamlParserResult) info).getSnapshot().getSource().getDocument(false);
                if (doc != null) {
                    return doc.getText(caretOffset - 1, 1);
                } else {
                    return null;
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return null;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }
    private static ImageIcon keywordIcon;

    private static class KeywordItem implements CompletionProposal, ElementHandle {

        private int anchor;
        private static final String YAML_KEYWORD = "org/netbeans/modules/languages/yaml/yaml_files_16.png"; //NOI18N
        private final String keyword;
        private final String description;
        private final String sort;

        KeywordItem(String keyword, String description, int anchor, String sort) {
            this.keyword = keyword;
            this.description = description;
            this.anchor = anchor;
            this.sort = sort;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (description != null) {
                //formatter.appendText(description);
                formatter.appendHtml(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = ImageUtilities.loadImageIcon(YAML_KEYWORD, false);
            }

            return keywordIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return this;
        }

        @Override
        public boolean isSmart() {
            return false;
        }

        @Override
        public int getAnchorOffset() {
            return anchor;
        }

        @Override
        public String getInsertPrefix() {
            return keyword;
        }

        @Override
        public String getSortText() {
            return sort;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(ElementKind.KEYWORD, true);
            formatter.appendText(getName());
            formatter.name(ElementKind.KEYWORD, false);

            return formatter.getText();
        }

        @Override
        public String getCustomInsertTemplate() {
            return null;
        }

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return YamlTokenId.YAML_MIME_TYPE;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public int getSortPrioOverride() {
            return 0;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            // FIXME parsing API
            return OffsetRange.NONE;
        }
    }
}
