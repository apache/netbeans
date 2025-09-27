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
import org.netbeans.modules.php.blade.editor.parser.BladeDirectiveScope;
import org.netbeans.modules.php.blade.editor.parser.BladeScope;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParserBaseListener;
/**
 *
 * @author bogdan
 */
public class ScopeListener extends BladeAntlrParserBaseListener {

    private final BladeScope bladeScope;
    private BladeDirectiveScope bufferScope;
    int bladeScopeBalance = 0;

    public ScopeListener(BladeScope bladeScope) {
        this.bladeScope = bladeScope;
    }

    @Override
    public void enterForeachStatement(BladeAntlrParser.ForeachStatementContext ctx) {
        if (bufferScope == null) {
            bufferScope = new BladeDirectiveScope(ctx.start.getType());
        } else {
            bufferScope.setChild(new BladeDirectiveScope(ctx.start.getType()));
        }
        bladeScopeBalance++;
    }

    @Override
    public void exitForeachLoopArguments(BladeAntlrParser.ForeachLoopArgumentsContext ctx) {
        if (bufferScope == null) {
            return;
        }
        if (ctx.LPAREN() == null || ctx.RPAREN() == null) {
            return;
        }

        Token leftParen = ctx.LPAREN().getSymbol();
        Token rightParen = ctx.RPAREN().getSymbol();

        if (leftParen.getStartIndex() + 1 >= rightParen.getStopIndex()) {
            return;
        }

        if (ctx.main_array != null) {
            bufferScope.addVariable(ctx.main_array.getText());
        }

        if (ctx.array_item != null) {
            bufferScope.addVariable(ctx.array_item.getText());
        }

        if (ctx.array_value != null) {
            bufferScope.addVariable(ctx.array_value.getText());
        }
    }

    @Override
    public void exitForeachStatement(BladeAntlrParser.ForeachStatementContext ctx) {
        Token start = ctx.start;
        Token stop = ctx.stop;

        OffsetRange range = new OffsetRange(start.getStartIndex(),
                stop.getStopIndex() + 1);

        bladeScopeBalance--;
        if (bufferScope != null && bladeScopeBalance == 0) {
            bladeScope.markScope(range, bufferScope);
            bufferScope = null;
        }
    }
}
