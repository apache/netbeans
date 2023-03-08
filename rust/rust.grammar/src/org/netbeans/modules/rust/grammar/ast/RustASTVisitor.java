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
package org.netbeans.modules.rust.grammar.ast;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.netbeans.modules.rust.grammar.antlr4.RustParser;
import org.netbeans.modules.rust.grammar.antlr4.RustParserBaseVisitor;
import org.openide.util.Cancellable;

/**
 * This is a RustParserBaseVisitor that extract structure from a Rust create. It
 * collects structs, functions and errors, so they can be shown in the editor or
 * in the * navigator window.
 */
final class RustASTVisitor extends RustParserBaseVisitor<RustASTNode> implements Cancellable {

    private static final Logger LOG = Logger.getLogger(RustASTVisitor.class.getName());
    private static final Level LOGLEVEL = Level.FINE;

    private final ArrayList<RustASTNode> nodeStack;

    private final AtomicBoolean cancelled;

    RustASTVisitor() {
        this.nodeStack = new ArrayList<>();
        this.cancelled = new AtomicBoolean(false);
    }

    private void push(RustASTNode node) {
        nodeStack.add(node);
    }

    private RustASTNode pop() {
        return nodeStack.remove(this.nodeStack.size() - 1);
    }

    private RustASTNode peek() {
        return nodeStack.isEmpty() ? null : nodeStack.get(nodeStack.size() - 1);
    }

    @Override
    public RustASTNode visitErrorNode(ErrorNode node) {
        // TODO: Register errors in create
        return null;
    }

    @Override
    public RustASTNode visitCrate(RustParser.CrateContext ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode crate = new RustASTNode(RustASTNodeKind.CRATE);
        crate.setRange(ctx.start, ctx.stop);
        push(crate);
        for (ParseTree child : ctx.children) {
            if (!cancelled.get()) {
                child.accept(this);
            }
        }
        pop();
        return crate;
    }

    @Override
    public RustASTNode visitEnumeration(RustParser.EnumerationContext ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode current = peek();

        RustASTNode e = new RustASTNode(RustASTNodeKind.ENUM);
        e.setName(ctx.identifier().getText());
        e.setRange(ctx.start, ctx.stop);
        e.setFold(ctx.LCURLYBRACE(), ctx.RCURLYBRACE());

        current.addEnum(e);

        LOG.log(LOGLEVEL, String.format("Visiting enum %s%n", e.getName()));

        push(e);
        // Visit children to seek for functions
        for (ParseTree tree : ctx.children) {
            tree.accept(this);
        }
        pop();

        return e;
    }

    @Override
    public RustASTNode visitModule(RustParser.ModuleContext ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode current = peek();

        RustASTNode module = new RustASTNode(RustASTNodeKind.MODULE);
        module.setName(ctx.identifier().getText());
        module.setRange(ctx.start, ctx.stop);
        module.setFold(ctx.LCURLYBRACE(), ctx.RCURLYBRACE());
        current.addModule(module);

        LOG.log(LOGLEVEL, String.format("Visiting module %s%n", module.getName()));

        push(module);
        // Visit children to seek for other things (modules, functions, etc.)
        for (ParseTree tree : ctx.children) {
            tree.accept(this);
        }
        pop();

        return module;
    }

    @Override
    public RustASTNode visitMacroRulesDefinition(RustParser.MacroRulesDefinitionContext ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode current = peek();

        RustASTNode macro = new RustASTNode(RustASTNodeKind.MACRO);
        macro.setName(ctx.identifier().getText());
        macro.setRange(ctx.start, ctx.stop);
        RustParser.MacroRulesDefContext def = ctx.macroRulesDef();
        if (def != null) {
            macro.setFold(def.LCURLYBRACE(), def.RCURLYBRACE());
        }

        current.addMacro(macro);

        LOG.log(LOGLEVEL, String.format("Visiting macro %s%n", macro.getName()));

        push(macro);
        // Visit children to seek for functions
        for (ParseTree tree : ctx.children) {
            tree.accept(this);
        }
        pop();

        return macro;
    }

    @Override
    public RustASTNode visitTrait_(RustParser.Trait_Context ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode current = peek();

        RustASTNode trait = new RustASTNode(RustASTNodeKind.TRAIT);
        trait.setName(ctx.identifier().getText());
        trait.setRange(ctx.start, ctx.stop);
        trait.setFold(ctx.LCURLYBRACE(), ctx.RCURLYBRACE());

        current.addTrait(trait);

        LOG.log(LOGLEVEL, String.format("Visiting trait %s%n", trait.getName()));

        push(trait);
        // Visit children to seek for functions
        for (ParseTree tree : ctx.children) {
            tree.accept(this);
        }
        pop();

        return trait;
    }

    @Override
    public RustASTNode visitInherentImpl(RustParser.InherentImplContext ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode current = peek();

        RustASTNode impl = new RustASTNode(RustASTNodeKind.IMPL);
        impl.setName(ctx.type_().getText());
        impl.setRange(ctx.start, ctx.stop);
        impl.setFold(ctx.LCURLYBRACE(), ctx.RCURLYBRACE());

        current.addImpl(impl);

        LOG.log(LOGLEVEL, String.format("Visiting (inherent) impl %s%n", impl.getName()));

        push(impl);
        // Visit children to seek for functions
        for (ParseTree child : ctx.children) {
            if (!cancelled.get()) {
                child.accept(this);
            }
        }
        pop();

        return impl;
    }

    @Override
    public RustASTNode visitTraitImpl(RustParser.TraitImplContext ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode current = peek();

        RustASTNode impl = new RustASTNode(RustASTNodeKind.IMPL);
        impl.setName(ctx.typePath().getText());
        impl.setRange(ctx.start, ctx.stop);
        impl.setFold(ctx.LCURLYBRACE(), ctx.RCURLYBRACE());

        current.addImpl(impl);

        LOG.log(LOGLEVEL, String.format("Visiting (trait) impl %s for %s%n", impl.getName(), ctx.typePath()));

        push(impl);
        // Visit children to seek for functions
        for (ParseTree tree : ctx.children) {
            tree.accept(this);
        }
        pop();

        return impl;
    }

    @Override
    public RustASTNode visitStructStruct(RustParser.StructStructContext ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode current = peek();

        RustASTNode struct = new RustASTNode(RustASTNodeKind.STRUCT);
        struct.setName(ctx.identifier().getText());
        struct.setRange(ctx.start, ctx.stop);
        struct.setFold(ctx.LCURLYBRACE(), ctx.RCURLYBRACE());

        current.addStruct(struct);

        LOG.log(LOGLEVEL, String.format("Visiting struct %s%n", struct.getName()));

        push(struct);
        // Visit children to seek for functions
        for (ParseTree tree : ctx.children) {
            tree.accept(this);
        }
        pop();

        return struct;
    }

    @Override
    public RustASTNode visitFunction_(RustParser.Function_Context ctx) {
        if (cancelled.get()) {
            return null;
        }
        RustASTNode current = peek();

        RustASTNode function = new RustASTNode(RustASTNodeKind.FUNCTION);
        function.setName(ctx.identifier().getText());
        function.setRange(ctx.start, ctx.stop);
        if (ctx.blockExpression() != null) {
            function.setFold(ctx.blockExpression().LCURLYBRACE(), ctx.blockExpression().RCURLYBRACE());
        }
        LOG.log(LOGLEVEL, String.format("Visiting function %s%n", function.getName()));

        push(function);
        for (ParseTree child : ctx.children) {
            if (!cancelled.get()) {
                child.accept(this);
            }
        }
        pop();

        current.addFunction(function);
        return function;
    }

    @Override
    public boolean cancel() {
        LOG.log(Level.INFO, "RustASTVisitor cancelled.");
        this.cancelled.set(true);
        return true;
    }

}
