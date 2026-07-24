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
package org.netbeans.modules.php.blade.editor.parser.listeners;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.blade.editor.parser.BladeReferenceIdsCollection;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParserBaseListener;

/**
 *
 * @author bogdan
 */
public class ReferenceIdListener extends BladeAntlrParserBaseListener {

    private final BladeReferenceIdsCollection referenceIdsCollection;

    public ReferenceIdListener(BladeReferenceIdsCollection referenceIdsCollection) {
        this.referenceIdsCollection = referenceIdsCollection;
    }

    @Override
    public void exitIdentifiableArgDirective(BladeAntlrParser.IdentifiableArgDirectiveContext ctx) {
        int tokenType = ctx.getStart().getType();

        if (ctx.IDENTIFIABLE_STRING() == null || ctx.IDENTIFIABLE_STRING().getSymbol() == null) {
            return;
        }

        Token token = ctx.IDENTIFIABLE_STRING().getSymbol();
        String identifier = referenceIdsCollection.sanitizeIdentifier(token);
        OffsetRange range = referenceIdsCollection.extractOffset(token);

        referenceIdsCollection.addReferenceId(tokenType, identifier, range);

        switch (tokenType) {
            case D_EXTENDS:
            case D_INCLUDE:
            case D_INCLUDE_IF:
            case D_INCLUDE_WHEN:
            case D_INCLUDE_UNLESS: {
                referenceIdsCollection.markIncludeBladeOccurrence(identifier, range);
                break;
            }
            case D_YIELD: {
                referenceIdsCollection.addYieldOccurence(identifier, range);
                break;
            }
            case D_STACK: {
                referenceIdsCollection.addStackOccurence(identifier, range);
                break;
            }
        }
    }

    @Override
    public void exitBlockIdentifiableArgDirective(BladeAntlrParser.BlockIdentifiableArgDirectiveContext ctx) {
        if (ctx.IDENTIFIABLE_STRING() == null || ctx.IDENTIFIABLE_STRING().getSymbol() == null) {
            return;
        }

        Token identifierToken = ctx.IDENTIFIABLE_STRING().getSymbol();
        addIdentifierReference(ctx.getStart(), identifierToken);
    }

    @Override
    public void exitMultipleArgDirective(BladeAntlrParser.MultipleArgDirectiveContext ctx) {
        int tokenType = ctx.getStart().getType();

        for (TerminalNode identifierNode : ctx.IDENTIFIABLE_STRING()) {
            Token identifierToken = identifierNode.getSymbol();
            if (identifierToken != null) {
                String identifier = referenceIdsCollection.sanitizeIdentifier(identifierToken);
                OffsetRange range = referenceIdsCollection.extractOffset(identifierToken);

                referenceIdsCollection.addReferenceId(tokenType, identifier, range);

                switch (tokenType) {
                    case D_EACH: {
                        referenceIdsCollection.markIncludeBladeOccurrence(identifier, range);
                        break;
                    }
                }
            }
        }
    }

    private void addIdentifierReference(Token directive, Token token) {
        String identifier = referenceIdsCollection.sanitizeIdentifier(token);
        OffsetRange range = referenceIdsCollection.extractOffset(token);

        referenceIdsCollection.addReferenceId(directive.getType(), identifier, range);
    }
}
