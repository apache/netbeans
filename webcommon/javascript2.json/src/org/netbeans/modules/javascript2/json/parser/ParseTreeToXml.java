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
package org.netbeans.modules.javascript2.json.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
public class ParseTreeToXml extends JsonParserBaseVisitor<Document> {
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
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
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
