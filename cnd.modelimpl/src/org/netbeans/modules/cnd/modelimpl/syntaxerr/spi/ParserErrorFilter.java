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

package org.netbeans.modules.cnd.modelimpl.syntaxerr.spi;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.openide.util.Lookup;

/**
 * An abstrasct class (a usual NB parttern - 
 * hybrid of an interface and a factory)
 * for filtering ANTLR recognition exceptions.
 * 
 * Implementation can
 * - filter out some particular sort of errors
 * - convert messages / info into human understandable format
 * 
 */
public abstract class ParserErrorFilter {

    /** A class that just joins all available filters to a single one */
    private static class JointFilter extends ParserErrorFilter {
        
        private final Lookup.Result<ParserErrorFilter> res;

        public JointFilter() {
            res = Lookup.getDefault().lookupResult(ParserErrorFilter.class);
        }
        
        
        @Override
        public void filter(Collection<CsmParserProvider.ParserError> parserErrors, Collection<CsmErrorInfo> result, 
                ReadOnlyTokenBuffer tokenBuffer, CsmFile file) {
            for( ParserErrorFilter filter : res.allInstances() ) {
                filter.filter(parserErrors, result, tokenBuffer, file);
            }
        }
    }
    
    private static final ParserErrorFilter DEFAULT = new JointFilter();  
    
    public static ParserErrorFilter getDefault() {
        return DEFAULT;
    }

    /**
     * Filters the collection of exceptions returned by ANTLR parser,
     * converts some (or all) of them to CsmErrorInfo
     * 
     * 
     * @param parserErrors the collection of exceptions returned by ANTLR parser. 
     * Feel free to remove some elements if the filter knows they are induced errors
     * and you don't want anyone to process them
     * 
     * @param result a collection to add resulting CsmErrorInfos to
     */
    abstract public void filter(
            Collection<CsmParserProvider.ParserError> parserErrors, 
            Collection<CsmErrorInfo> result,
            ReadOnlyTokenBuffer tokenBuffer,
            CsmFile file);
}
