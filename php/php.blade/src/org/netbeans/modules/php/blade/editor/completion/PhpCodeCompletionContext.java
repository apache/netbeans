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
package org.netbeans.modules.php.blade.editor.completion;

import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * used to trigger php.editor package completion flow
 * 
 * @author bhaidu
 */
public class PhpCodeCompletionContext {

    public static CodeCompletionContext completionContext(int carretOffset,
            ParserResult phpParserResult,
            String phpPrefix) {
        return new CodeCompletionContext() {
            @Override
            public int getCaretOffset() {
                //the offset should be taken from compiler
                return carretOffset;
            }

            @Override
            public ParserResult getParserResult() {
                return phpParserResult;
            }

            @Override
            public String getPrefix() {
                return phpPrefix;
            }

            @Override
            public boolean isPrefixMatch() {
                return true;
            }

            @Override
            public CodeCompletionHandler.QueryType getQueryType() {
                return CodeCompletionHandler.QueryType.COMPLETION;
            }

            @Override
            public boolean isCaseSensitive() {
                return true;
            }
        };
    }
}
