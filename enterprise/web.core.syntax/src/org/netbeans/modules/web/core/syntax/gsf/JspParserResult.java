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
package org.netbeans.modules.web.core.syntax.gsf;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.core.syntax.parser.JspSyntaxElement;
import org.netbeans.modules.web.core.syntax.parser.Result;

/**
 * @todo implement diagnostics - syntax parse errors support
 *
 * @author marekfukala
 */
public class JspParserResult extends ParserResult {

    private Result result;

    public JspParserResult(Snapshot s, Result syntaxParserResult) {
        super(s);
        this.result = syntaxParserResult;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return Collections.EMPTY_LIST;
    }

    @Override
    protected void invalidate() {
        //do nothing
    }

    public List<JspSyntaxElement> elements() {
        return result.elements();
    }
}
