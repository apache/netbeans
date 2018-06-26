/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.json.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Tomas Zezula
 */
public class ParseTreeToXml extends JsonBaseVisitor<Document> {
    private final JsonLexer lexer;
    private final JsonParser parser;
    private Document doc;
    private final Deque<Element> currentNode;

    public ParseTreeToXml (
            @NonNull final JsonLexer lexer,
            @NonNull final JsonParser parser) {
        Parameters.notNull("lexer", lexer); //NOI18N
        Parameters.notNull("parser", parser);    //NOI18N
        this.lexer = lexer;
        this.parser = parser;
        currentNode = new ArrayDeque<>();
    }


    @Override
    public Document visitJson(JsonParser.JsonContext ctx) {
        final Element e = copyAttrs(
                getRuleContextElement(ctx.getRuleIndex()),
                ctx);
        currentNode.addLast(e);
        super.visitJson(ctx);
        currentNode.removeLast();
        return doc;
    }

    @Override
    public Document visitObject(JsonParser.ObjectContext ctx) {
        final Element e = copyAttrs(
                getRuleContextElement(ctx.getRuleIndex()),
                ctx);
        currentNode.addLast(e);
        super.visitObject(ctx);
        currentNode.removeLast();
        return doc;
    }

    @Override
    public Document visitArray(JsonParser.ArrayContext ctx) {
        final Element e = copyAttrs(
                getRuleContextElement(ctx.getRuleIndex()),
                ctx);
        currentNode.addLast(e);
        super.visitArray(ctx);
        currentNode.removeLast();
        return doc;
    }

    @Override
    public Document visitKey(JsonParser.KeyContext ctx) {
        final Element e = copyAttrs(
                getRuleContextElement(ctx.getRuleIndex()),
                ctx);
        currentNode.addLast(e);
        super.visitKey(ctx);
        currentNode.removeLast();
        return doc;
    }

    @Override
    public Document visitValue(JsonParser.ValueContext ctx) {
        final Element e = copyAttrs(
                getRuleContextElement(ctx.getRuleIndex()),
                ctx);
        currentNode.addLast(e);
        super.visitValue(ctx);
        currentNode.removeLast();
        return doc;
    }

    @Override
    public Document visitPair(JsonParser.PairContext ctx) {
        final Element e = copyAttrs(
                getRuleContextElement(ctx.getRuleIndex()),
                ctx);
        currentNode.addLast(e);
        super.visitPair(ctx);
        currentNode.removeLast();
        return doc;
    }

    @Override
    public Document visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() != Token.EOF) {
            copyAttrs(
                    getTerminalNodeElement(node),
                    node);
        }
        super.visitTerminal(node);
        return doc;
    }

    @NonNull
    public static String stringify(@NonNull final Document doc) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLUtil.write(doc, out, "UTF-8");   //NOI18N
        return new String(out.toByteArray(),"UTF-8");   //NOI18N
    }


    private static Element copyAttrs(
            @NonNull final Element e,
            @NonNull final ParserRuleContext ctx) {
        e.setAttribute("start", Integer.toString(ctx.start.getStartIndex()));   //NOI18N
        e.setAttribute("stop",  Integer.toString(ctx.stop.getStopIndex()));     //NOI18N
        return e;
    }

    private static Element copyAttrs(
            @NonNull final Element e,
            @NonNull final TerminalNode ctx) {
        e.setAttribute("start", Integer.toString(ctx.getSymbol().getStartIndex())); //NOI18N
        e.setAttribute("stop",  Integer.toString(ctx.getSymbol().getStopIndex()));  //NOI18N
        e.setAttribute("text",  ctx.getSymbol().getText());  //NOI18N
        return e;
    }

    @NonNull
    private Element getRuleContextElement(final int ruleIndex) {
        return getParseTreeElement(parser.getRuleNames()[ruleIndex]);
    }

    @NonNull
    private Element getTerminalNodeElement(@NonNull final TerminalNode node) {
        return getParseTreeElement(lexer.getVocabulary().getSymbolicName(
                node.getSymbol().getType()));
    }

    @NonNull
    private Element getParseTreeElement(@NonNull final String name) {
        if (!currentNode.isEmpty()) {
            Element parent = currentNode.peekLast();
            Element me = doc.createElement(name);
            parent.appendChild(me);
            return me;
        } else if (doc != null) {
            Element parent = doc.getDocumentElement();
            Element me = doc.createElement(name);
            parent.appendChild(me);
            return me;
        } else {
            doc = XMLUtil.createDocument(name, null, null, null);
            return doc.getDocumentElement();
        }
    }

}
