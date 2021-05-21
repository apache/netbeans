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

package org.netbeans.modules.groovy.refactoring.findusages;

import java.awt.Color;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.api.ASTUtils.FakeASTNode;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.text.Line;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Janicek
 */
public class FindUsagesPainter {

    /**
     * For the given {@link Line} and {@link ASTNode} returns colored text. In the
     * returned value there is the same text, but for example keywords are colored,
     * usage itself is bold, etc.
     *
     * @param node concrete usage node
     * @param line line where we have found the usage
     * @return colored text
     */
    public static String colorASTNode(final ASTNode node, final Line line) {
        final int columnStart = node.getColumnNumber();
        final int columnEnd = node.getLastColumnNumber();

        if (node instanceof ClassNode) {
            return colorLine(line, ((ClassNode) node).getNameWithoutPackage());
        } else if (node instanceof ConstructorNode) {
            return colorLine(line, ((ConstructorNode) node).getDeclaringClass().getNameWithoutPackage());
        } else if (node instanceof ConstructorCallExpression) {
            return colorLine(line, ((ConstructorCallExpression) node).getType().getNameWithoutPackage());
        } else if (node instanceof MethodNode) {
            return colorLine(line, ((MethodNode) node).getName());
        } else if (node instanceof FieldNode) {
            return colorLine(line, ((FieldNode) node).getName());
        } else if (node instanceof FakeASTNode) {
            // I know this isn't the nicest way, but I can't pass ImportNode directly and
            // don't see easier way how to find out if the FakeASTNode is based on ImportNode
            ASTNode originalNode = ((FakeASTNode) node).getOriginalNode();
            if (originalNode instanceof ImportNode) {
                return colorLine(line, ((ImportNode) originalNode).getAlias());
            }
        }

        final String beforePart = line.createPart(0, columnStart - 1).getText();
        final String usagePart = line.createPart(columnStart - 1, columnEnd - columnStart).getText();
        final String afterPart = line.createPart(columnEnd - 1, line.getText().length()).getText();

        return buildHTML(beforePart, usagePart, afterPart);
    }

    private static String colorLine(final Line line, final String name) {
        final int start = line.getText().lastIndexOf(name);
        final String beforePart = line.createPart(0, start).getText();
        final String usagePart = line.createPart(start, name.length()).getText();
        final String afterPart = line.createPart(start + name.length(), line.getText().length()).getText();
        
        return buildHTML(beforePart, usagePart, afterPart);
    }

    private static String buildHTML(final String beforePart, final String usagePart, final String afterPart) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHtml(trimStart(beforePart)));
        sb.append("<b>"); //NOI18N
        sb.append(usagePart);
        sb.append("</b>");//NOI18N
        sb.append(getHtml(trimEnd(afterPart)));

        return sb.toString().trim();
    }

    private static String trimStart(String s) {
        for (int x = 0; x < s.length(); x++) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(x, s.length());
            }
        }
        return "";
    }

    private static String trimEnd(String s) {
        for (int x = s.length()-1; x >=0; x--) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(0, x + 1);
            }
        }
        return "";
    }

    private static String getHtml(String text) {
        StringBuilder buf = new StringBuilder();
        TokenHierarchy tokenH = TokenHierarchy.create(text, GroovyTokenId.language());
        Lookup lookup = MimeLookup.getLookup(MimePath.get(GroovyLanguage.GROOVY_MIME_TYPE));
        FontColorSettings settings = lookup.lookup(FontColorSettings.class);
        TokenSequence tok = tokenH.tokenSequence();
        while (tok.moveNext()) {
            Token<GroovyTokenId> token = tok.token();
            String category = token.id().primaryCategory();
            if (category == null) {
                category = "whitespace"; //NOI18N
            }
            AttributeSet set = settings.getTokenFontColors(category);
            buf.append(color(htmlize(token.text().toString()), set));
        }
        return buf.toString();
    }

    private static String color(String string, AttributeSet set) {
        if (set == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
        }
        StringBuffer buf = new StringBuffer(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0, "<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0, "<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0, "<s>"); // NOI18N
            buf.append("</s>"); // NOI18N
        }
        buf.insert(0, "<font color=" + getHTMLColor(LFCustoms.getForeground(set)) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf.toString();
    }

    private static String getHTMLColor(Color c) {
        String colorR = "0" + Integer.toHexString(c.getRed()); //NOI18N
        colorR = colorR.substring(colorR.length() - 2);
        String colorG = "0" + Integer.toHexString(c.getGreen()); //NOI18N
        colorG = colorG.substring(colorG.length() - 2);
        String colorB = "0" + Integer.toHexString(c.getBlue()); //NOI18N
        colorB = colorB.substring(colorB.length() - 2);
        String html_color = "#" + colorR + colorG + colorB; //NOI18N
        return html_color;
    }

    private static String htmlize(String input) {
        String temp = input.replace("<", "&lt;"); // NOI18N
        temp = temp.replace(">", "&gt;"); // NOI18N
        return temp;
    }
}
