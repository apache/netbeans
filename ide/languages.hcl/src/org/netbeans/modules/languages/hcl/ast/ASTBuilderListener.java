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

import java.util.ArrayList;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.modules.languages.hcl.grammar.HCLParser;
import org.netbeans.modules.languages.hcl.grammar.HCLParserBaseListener;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Laszlo Kishalmi
 */
public class ASTBuilderListener extends HCLParserBaseListener {

    final HCLDocument document = new HCLDocument();
    final SourceRef references;

    private HCLContainer current = document;
    
    public ASTBuilderListener(Snapshot source) {
        this.references = new SourceRef(source);
    }

    public HCLDocument getDocument() {
        return document;
    }

    public SourceRef getReferences() {
        return references;
    }

    
    private void addReference(HCLElement e, Token token) {
        references.add(e, token.getStartIndex(), token.getStopIndex() + 1);
    }

    private void addReference(HCLElement e, ParserRuleContext ctx) {
        
        references.add(e, ctx.start.getStartIndex(), ctx.stop.getStopIndex() + 1);
    }


    @Override
    public void exitBlock(HCLParser.BlockContext ctx) {
        HCLBlock block = (HCLBlock) current;

        ArrayList<HCLIdentifier> decl = new ArrayList<>(4);

        for (TerminalNode idn : ctx.IDENTIFIER()) {
            Token token = idn.getSymbol();
            HCLIdentifier id = new HCLIdentifier.SimpleId(block, token.getText());
            addReference(id, token);
            decl.add(id);
        }
        for (HCLParser.StringLitContext idn : ctx.stringLit()) {
            String sid = idn.getText();
            if (sid.length() > 1) { // Do not process the '"' string literal
                sid = sid.substring(1, sid.length() - (sid.endsWith("\"") ? 1 : 0));
                HCLIdentifier id = new HCLIdentifier.StringId(block, sid);
                addReference(id, idn);
                decl.add(id);
            }
        }
        block.setDeclaration(references.sortBySource(decl));

        current = current.getContainer();
        current.add(block);
        addReference(block, ctx);
    }

    @Override
    public void exitBody(HCLParser.BodyContext ctx) {
        for (HCLParser.AttributeContext actx : ctx.attribute()) {
            HCLAttribute attr = new HCLAttribute(current);
            attr.name = new HCLIdentifier.SimpleId(attr, actx.IDENTIFIER().getText());
            addReference(attr.name, actx.IDENTIFIER().getSymbol());
            addReference(attr, actx);
            current.add(attr);
        }
    }

    @Override
    public void enterBlock(HCLParser.BlockContext ctx) {
        current = new HCLBlock(current);
    }

    
}
