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
import java.util.Collections;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.modules.languages.hcl.grammar.HCLParser;
import org.netbeans.modules.languages.hcl.grammar.HCLParserBaseListener;

/**
 *
 * @author Laszlo Kishalmi
 */
public class ASTBuilderListener extends HCLParserBaseListener {

    final HCLDocument document = new HCLDocument();

    public HCLDocument getDocument() {
        return document;
    }

    int blockDepth = 0;

    @Override
    public void exitBlock(HCLParser.BlockContext ctx) {
        if (blockDepth == 1) {
            ArrayList<HCLIdentifier> decl = new ArrayList<>(4);
            for (TerminalNode idn : ctx.IDENTIFIER()) {
                Token token = idn.getSymbol();
                SourceRef src = new SourceRef(null, token.getStartIndex(), token.getStopIndex());
                HCLIdentifier id = new HCLIdentifier.SimpleId(src, token.getText());
                decl.add(id);
            }
            for (HCLParser.StringLitContext idn : ctx.stringLit()) {
                SourceRef src = new SourceRef(null, idn.getStart().getStartIndex(), idn.getStop().getStopIndex());
                HCLIdentifier id = new HCLIdentifier.StringId(src, idn.getText());
                decl.add(id);
            }
            Collections.sort(decl, HCLElement.SOURCE_ORDER);
            document.add(new HCLBlock(decl));
        }
        blockDepth--;
    }

    @Override
    public void enterBlock(HCLParser.BlockContext ctx) {
        blockDepth++;
    }

    
}
