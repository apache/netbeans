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
import org.netbeans.modules.php.blade.editor.parser.BladeCustomDirectiveOccurences;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParserBaseListener;

/**
 *
 * @author bogdan
 */
public class CustomDirectivesListener extends BladeAntlrParserBaseListener {

    private final BladeCustomDirectiveOccurences phpExprOccurences;

    public CustomDirectivesListener(BladeCustomDirectiveOccurences phpExprOccurences) {
        this.phpExprOccurences = phpExprOccurences;
    }

    @Override
    public void exitCustomDirective(BladeAntlrParser.CustomDirectiveContext ctx) {
        if (ctx.D_CUSTOM() == null){
            return;
        }
        Token customDirective = ctx.D_CUSTOM().getSymbol();
        OffsetRange range = new OffsetRange(customDirective.getStartIndex() + 1, customDirective.getStopIndex());
        phpExprOccurences.markPhpExpressionOccurence(range, customDirective.getText());
    }
}
