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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        for (Iterator it = allParameters.iterator(); it.hasNext();) {
            CodeTemplateParameterImpl param = CodeTemplateParameterImpl.get(
                    (CodeTemplateParameter)it.next());
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

}
