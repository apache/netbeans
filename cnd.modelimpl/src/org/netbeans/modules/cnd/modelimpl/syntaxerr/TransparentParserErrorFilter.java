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

package org.netbeans.modules.cnd.modelimpl.syntaxerr;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ReadOnlyTokenBuffer;

/**
 * A trivial implementation of the filter 
 * that just creates an error for each RecognitionException
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ParserErrorFilter.class)
public class TransparentParserErrorFilter extends BaseParserErrorFilter {

    private static final boolean ENABLE = DebugUtils.getBoolean("cnd.parser.error.transparent", true);
    private static final boolean ONLY_WARNINGS = Boolean.getBoolean("cnd.parser.error.transparent.warnings");

    @Override
    public void filter(Collection<CsmParserProvider.ParserError> parserErrors, Collection<CsmErrorInfo> result, 
            ReadOnlyTokenBuffer tokenBuffer, CsmFile file) {
        if (ENABLE) {
            result.addAll(toErrorInfo(parserErrors, file));
        }
    }
    
    @Override
    protected CsmErrorInfo.Severity getDefaultSeverity() {
        return ONLY_WARNINGS ? CsmErrorInfo.Severity.WARNING : CsmErrorInfo.Severity.ERROR;
    }
}
