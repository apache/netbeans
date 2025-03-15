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
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.blade.editor.parser.BladePhpExpressionOccurences;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParserBaseListener;

/**
 *
 * @author bogdan
 */
public class PhpExpressionOccurenceListener extends BladeAntlrParserBaseListener {

    private final BladePhpExpressionOccurences phpExprOccurences;

    public PhpExpressionOccurenceListener(BladePhpExpressionOccurences phpExprOccurences) {
        this.phpExprOccurences = phpExprOccurences;
    }

    @Override
    public void exitIdentifiableArgDirective(BladeAntlrParser.IdentifiableArgDirectiveContext ctx) {
        if (ctx.LPAREN() == null || ctx.RPAREN() == null) {
            return;
        }

        Token leftParen = ctx.LPAREN().getSymbol();
        Token rightParen = ctx.RPAREN().getSymbol();

        if (leftParen.getStartIndex() + 1 >= rightParen.getStopIndex()) {
            return;
        }

        markPhpOutputExprOccurence(leftParen, rightParen, 1);
    }

    @Override
    public void exitForeachLoopArguments(BladeAntlrParser.ForeachLoopArgumentsContext ctx) {
        if (ctx.LPAREN() == null || ctx.RPAREN() == null) {
            return;
        }

        Token leftParen = ctx.LPAREN().getSymbol();
        Token rightParen = ctx.RPAREN().getSymbol();

        if (leftParen.getStartIndex() + 1 >= rightParen.getStopIndex()) {
            return;
        }

        markPhpForeachExprOccurence(leftParen, rightParen, 1);
    }

    @Override
    public void exitMultipleArgDirective(BladeAntlrParser.MultipleArgDirectiveContext ctx) {
        if (ctx.LPAREN() == null || ctx.RPAREN() == null) {
            return;
        }

        Token leftParen = ctx.LPAREN().getSymbol();
        Token rightParen = ctx.RPAREN().getSymbol();

        if (leftParen.getStartIndex() + 1 >= rightParen.getStopIndex()) {
            return;
        }

        markPhpOutputExprOccurence(leftParen, rightParen, 1);
    }

    @Override
    public void exitDirectiveArguments(BladeAntlrParser.DirectiveArgumentsContext ctx) {
        if (ctx.LPAREN() == null || ctx.RPAREN() == null) {
            return;
        }
        Token leftParen = ctx.LPAREN().getSymbol();
        Token rightParen = ctx.RPAREN().getSymbol();
        markPhpOutputExprOccurence(leftParen, rightParen, 1);
    }

    @Override
    public void exitBlockIdentifiableArgDirective(BladeAntlrParser.BlockIdentifiableArgDirectiveContext ctx) {
        if (ctx.LPAREN() == null || ctx.RPAREN() == null) {
            return;
        }
        Token leftParen = ctx.LPAREN().getSymbol();
        Token rightParen = ctx.RPAREN().getSymbol();
        markPhpOutputExprOccurence(leftParen, rightParen, 1);
    }


    @Override
    public void exitBladeContentTags(BladeAntlrParser.BladeContentTagsContext ctx) {
        if (ctx.BLADE_CONTENT_OPEN_TAG() == null || ctx.BLADE_CONTENT_CLOSE_TAG() == null) {
            return;
        }
        Token openTag = ctx.BLADE_CONTENT_OPEN_TAG().getSymbol();
        Token closeTag = ctx.BLADE_CONTENT_CLOSE_TAG().getSymbol();
        markPhpOutputExprOccurence(openTag, closeTag, 1);
    }

    @Override
    public void exitBladeRawTags(BladeAntlrParser.BladeRawTagsContext ctx) {
        if (ctx.start == null || ctx.stop == null) {
            return;
        }
        Token openTag = ctx.start;
        Token closeTag = ctx.stop;
        markPhpOutputExprOccurence(openTag, closeTag, 1);
    }

    @Override
    public void exitBladePhpBlock(BladeAntlrParser.BladePhpBlockContext ctx) {
        if (ctx.D_PHP() == null || ctx.D_ENDPHP() == null) {
            return;
        }
        Token openTag = ctx.D_PHP().getSymbol();
        Token closeTag = ctx.D_ENDPHP().getSymbol();
        markPhpExprOccurence(openTag, closeTag, 1);
    }

    @Override
    public void exitPhpInline(BladeAntlrParser.PhpInlineContext ctx) {
        if (ctx.start == null || ctx.stop == null) {
            return;
        }
        Token openTag = ctx.start;
        Token closeTag = ctx.stop;
        markPhpExprOccurence(openTag, closeTag, 1);
    }

    private void markPhpExprOccurence(Token start, Token end, int offset) {
        int startOffset = start.getStopIndex() + offset;
        int endOffset = end.getStartIndex();
        
        if (startOffset > endOffset){
            return;
        }
        
        OffsetRange range = new OffsetRange(startOffset, endOffset);
        phpExprOccurences.markPhpInlineExpressionOccurence(range);
    }
    
    private void markPhpOutputExprOccurence(Token start, Token end, int offset) {
        int startOffset = start.getStopIndex() + offset;
        int endOffset = end.getStartIndex();
        
        if (startOffset > endOffset){
            return;
        }
        
        OffsetRange range = new OffsetRange(startOffset, endOffset);
        phpExprOccurences.markPhpOutputExpressionOccurence(range);
    }
    
    private void markPhpForeachExprOccurence(Token start, Token end, int offset) {
        int startOffset = start.getStopIndex() + offset;
        int endOffset = end.getStartIndex();
        
        if (startOffset > endOffset){
            return;
        }
        
        OffsetRange range = new OffsetRange(startOffset, endOffset);
        phpExprOccurences.markPhpForeachExpressionOccurence(range);
    }
}
