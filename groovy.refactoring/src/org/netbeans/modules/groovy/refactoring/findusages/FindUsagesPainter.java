/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            Token<GroovyTokenId> token = (Token) tok.token();
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
