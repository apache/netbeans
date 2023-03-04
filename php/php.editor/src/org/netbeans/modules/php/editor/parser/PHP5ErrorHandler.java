/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.parser;

import java.util.List;
import java_cup.runtime.Symbol;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface PHP5ErrorHandler extends ParserErrorHandler {

    List<Error> displayFatalError();
    List<Error> displaySyntaxErrors(Program program);
    List<SyntaxError> getSyntaxErrors();
    void disableHandling();

    @org.netbeans.api.annotations.common.SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
    public static class SyntaxError {

        @NbBundle.Messages({
            "SE_ValidMessage=Syntax error",
            "SE_PossibleMessage=POSSIBLE Syntax Error (check preceding valid syntax error)"
        })
        public static enum Type {
            FIRST_VALID_ERROR() {

                @Override
                public String getMessageHeader() {
                    return Bundle.SE_ValidMessage();
                }

                @Override
                public Severity getSeverity() {
                    return Severity.ERROR;
                }

            },
            POSSIBLE_ERROR() {

                @Override
                public String getMessageHeader() {
                    return Bundle.SE_PossibleMessage();
                }

                @Override
                public Severity getSeverity() {
                    return Severity.WARNING;
                }

            };

            public abstract String getMessageHeader();

            public abstract Severity getSeverity();
        }

        private final short[] expectedTokens;
        private final Symbol currentToken;
        private final Symbol previousToken;
        private final Type type;

        public SyntaxError(short[] expectedTokens, Symbol currentToken, Symbol previousToken, Type type) {
            this.expectedTokens = expectedTokens;
            this.currentToken = currentToken;
            this.previousToken = previousToken;
            this.type = type;
        }

        public Symbol getCurrentToken() {
            return currentToken;
        }

        public Symbol getPreviousToken() {
            return previousToken;
        }

        public short[] getExpectedTokens() {
            return expectedTokens;
        }

        public String getMessageHeader() {
            return type.getMessageHeader();
        }

        public Severity getSeverity() {
            return type.getSeverity();
        }

        public boolean generateExtraInfo() {
            return getSeverity().equals(Severity.ERROR);
        }
    }

    public static class FatalError extends GSFPHPError {

        @NbBundle.Messages("MSG_FatalError=Unable to parse the file")
        FatalError(GSFPHPParser.Context context) {
            super(Bundle.MSG_FatalError(),
                context.getSnapshot().getSource().getFileObject(),
                0, context.getBaseSource().length(),
                Severity.ERROR, null);
        }

        @Override
        public boolean isLineError() {
            return false;
        }
    }

}
