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
package org.netbeans.modules.languages.hcl.ast;

import java.util.function.Consumer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;

/**
 *
 * @author lkishalmi
 */
public sealed abstract class HCLElementFactory permits HCLBlockFactory, HCLExpressionFactory {
    public record CreateContext(HCLElement element, Token start, Token stop, int group) {}

    private final Consumer<CreateContext> createAction;

    public HCLElementFactory(Consumer<CreateContext> createAction) {
        this.createAction = createAction;
    }

    protected final HCLIdentifier id(TerminalNode tn) {
        return tn != null ? id(tn.getSymbol()) : null;
    }

    protected final HCLIdentifier id(Token t) {
        return (t != null) && (t.getType() == HCLLexer.IDENTIFIER) ? created(new HCLIdentifier.SimpleId(t.getText()), t) : null;
    }

    protected final <E extends HCLElement> E created(E element, Token token) {
        return created(element, token, token);
    }

    protected final <E extends HCLElement> E created(E element, ParserRuleContext ctx) {
        return created(element, ctx.start, ctx.stop);
    }

    protected final <E extends HCLElement> E created(E element, ParserRuleContext ctx, int group) {
        return created(element, ctx.start, ctx.stop, group);
    }

    protected final <E extends HCLElement> E created(E element, Token start, Token stop) {
        elementCreated(element, start, stop, -1);
        return element;
    }

    protected final <E extends HCLElement> E created(E element, Token start, Token stop, int group) {
        elementCreated(element, start, stop, group);
        return element;
    }

    protected final void elementCreated(HCLElement element, Token start, Token stop, int group) {
        if (createAction != null) {
            createAction.accept(new CreateContext(element, start, stop, group));
        }
    }
}
