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

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.util.swing.PositionRegion;

/**
 * Parser of the parametrized text.
 *
 * @author Miloslav Metelka
 */
public final class ParametrizedTextParser {

    private final CodeTemplateInsertHandler handler;

    private final String parametrizedText;

    private List<CodeTemplateParameterImpl> paramImpls; // filled only when (handler == null)

    /**
     * Fragments of the parametrized text between the parameters.
     */
    private List<String> parametrizedTextFragments;

    public ParametrizedTextParser(CodeTemplateInsertHandler handler, String parametrizedText) {
        this.handler = handler; // may be null for parsing for completion doc item
        this.parametrizedText = parametrizedText;
        if (handler == null) { // will build doc for completion item
            paramImpls = new ArrayList<CodeTemplateParameterImpl>();
        }
    }
    
    public void parse() {
        parametrizedTextFragments = new ArrayList<String>();

        StringBuffer textFrag = new StringBuffer();
        int copyStartIndex = 0;
        int index = 0; // actual index in parametrizedText
        boolean atEOT = false;
        while (!atEOT) {
            // Search for '${...}'
            // '$$' interpreted as '$'
            int dollarIndex = parametrizedText.indexOf('$', index);
            if (dollarIndex != -1 && dollarIndex < parametrizedText.length() - 1) { // found
                switch (parametrizedText.charAt(dollarIndex + 1)) { // test char after '$'
                    case '{': // parameter parsing
                        // Store preceding part into fragments
                        textFrag.append(parametrizedText.substring(copyStartIndex, dollarIndex));
                        copyStartIndex = dollarIndex;
                        parametrizedTextFragments.add(textFrag.toString());
                        textFrag.setLength(0);

                        // Create parameter found at the dollarIndex
                        CodeTemplateParameterImpl paramImpl = new CodeTemplateParameterImpl(
                                handler, parametrizedText, dollarIndex);

                        int afterClosingBraceIndex = paramImpl.getParametrizedTextEndOffset();
                        if (afterClosingBraceIndex <= parametrizedText.length()) { // successfully recognized
                            if (handler != null) {
                                handler.notifyParameterParsed(paramImpl);
                            } else { // store params locally
                                for (CodeTemplateParameterImpl impl : paramImpls) {
                                    if (impl.getName().equals(paramImpl.getName())) {
                                        paramImpl.markSlave(impl.getParameter());
                                        break;
                                    }
                                }
                                paramImpls.add(paramImpl);
                            }
                            index = afterClosingBraceIndex;
                            copyStartIndex = index;

                        } else { // parameter's parsing hit EOT
                            atEOT = true;
                            break;
                        }
                        break;
                        
                    case '$': // shrink to single '$'
                        textFrag.append(parametrizedText.substring(copyStartIndex, dollarIndex + 1));
                        index = dollarIndex + 2;
                        copyStartIndex = index;
                        break;
                        
                    default: // something else => '$'
                        index = dollarIndex + 1;
                        break;
                }

            } else { // '$' not found till the end of parametrizedText
                textFrag.append(parametrizedText.substring(copyStartIndex));
                parametrizedTextFragments.add(textFrag.toString());
                atEOT = true;
            }
        }
    }
    
    public String buildInsertText(List/*<CodeTemplateParameter>*/ allParameters) {
        StringBuffer insertTextBuffer = new StringBuffer(parametrizedText.length());
        insertTextBuffer.append(parametrizedTextFragments.get(0));
        int fragIndex = 1;
        for (Iterator<CodeTemplateParameter> it = allParameters.iterator(); it.hasNext();) {
            CodeTemplateParameterImpl param = CodeTemplateParameterImpl.get(it.next());
            int startOffset = insertTextBuffer.length();
            insertTextBuffer.append(param.getValue());
            param.resetPositions(
                    PositionRegion.createFixedPosition(startOffset),
                    PositionRegion.createFixedPosition(insertTextBuffer.length())
            );
            insertTextBuffer.append(parametrizedTextFragments.get(fragIndex));
            fragIndex++;
        }
        return insertTextBuffer.toString();
    }
    
    public static StringBuffer parseToHtml(StringBuffer sb, String parametrizedText) {
        // Parametrized text - parsed; parameters in bold
        ParametrizedTextParser parser = new ParametrizedTextParser(null, parametrizedText);
        parser.parse();
        parser.appendHtmlText(sb);
        return sb;
    }

    public static String toHtmlText(String text) {
        StringBuffer htmlText = null;
        for (int i = 0; i < text.length(); i++) {
            String rep; // replacement string
            char ch = text.charAt(i);
            switch (ch) {
                case '<':
                    rep = "&lt;"; // NOI18N
                    break;
                case '>':
                    rep = "&gt;"; // NOI18N
                    break;
                case '\n':
                    rep = "<br>"; // NOI18N
                    break;
                default:
                    rep = null;
                    break;
            }

            if (rep != null) {
                if (htmlText == null) {
                    // Expect 20% of text to be html tags text
                    htmlText = new StringBuffer(120 * text.length() / 100);
                    if (i > 0) {
                        htmlText.append(text.substring(0, i));
                    }
                }
                htmlText.append(rep);

            } else { // no replacement
                if (htmlText != null) {
                    htmlText.append(ch);
                }
            }
        }
        return (htmlText != null) ? htmlText.toString() : text;
    }
    
    private void appendHtmlText(StringBuffer htmlTextBuffer) {
        htmlTextBuffer.append(toHtmlText(parametrizedTextFragments.get(0)));
        
        int fragIndex = 1;
        for (CodeTemplateParameterImpl paramImpl : paramImpls) {
            htmlTextBuffer.append("<b>"); // NOI18N
            if (CodeTemplateParameter.CURSOR_PARAMETER_NAME.equals(paramImpl.getName())) {
                htmlTextBuffer.append("|"); // NOI18N
            } else {
                htmlTextBuffer.append(toHtmlText(paramImpl.getValue()));
            }
            htmlTextBuffer.append("</b>"); // NOI18N
            htmlTextBuffer.append(toHtmlText(parametrizedTextFragments.get(fragIndex)));
            fragIndex++;
        }
    }
    
    /**
     * Returns a map in which the keys are the ordinals and the values are the corresponding parametrized text 
     * fragments and template parameters.
     * <p>
     * This method should only be called after calling the {@link #parse} method.
     * 
     * @return unmodifiable map in which the keys are the ordinals and the values are the corresponding parametrized 
     * text fragments and template parameters.
     * 
     * @since 1.54
     */
    public Map<Integer, Object> getParametrizedFragmentsByOrdinals() {
        Map<Integer, Object> parametrizedFragmentsByOrdinals = new HashMap<>();
        int numberOfFragments = parametrizedTextFragments.size() + paramImpls.size();
        int j = 0;
        int k = 0;
        for (int i = 0; i < numberOfFragments; i++) { 
            if (i % 2 == 0) {
                if (j < parametrizedTextFragments.size()) {
                    parametrizedFragmentsByOrdinals.put(i, parametrizedTextFragments.get(j));
                    j++;
                }
            } else {
                if (k < paramImpls.size()) {
                    parametrizedFragmentsByOrdinals.put(i, paramImpls.get(k));
                    k++;
                }
            }
        }
        return Collections.unmodifiableMap(parametrizedFragmentsByOrdinals);
    }

}
