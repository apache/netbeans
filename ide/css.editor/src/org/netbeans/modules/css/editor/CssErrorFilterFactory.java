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
package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ErrorFilter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service = ErrorFilter.Factory.class)
public class CssErrorFilterFactory implements ErrorFilter.Factory {

    private static final ErrorFilter INSTANCE = new ErrorFilter() {
        @Override
        public List<? extends Error> filter(ParserResult parserResult) {
            if(parserResult instanceof CssParserResult) {
                List<? extends FilterableError> extendedDiagnostics = ((CssParserResult)parserResult).getDiagnostics();
                List<Error> kept = new ArrayList<>();
                for(FilterableError fe : extendedDiagnostics) {
                    if(!fe.isFiltered()) {
                        kept.add(fe);
                    }
                }
                return kept;
            }
            return parserResult.getDiagnostics();
        }
    };
    
    @Override
    public ErrorFilter createErrorFilter(String featureName) {
        return ErrorFilter.FEATURE_TASKLIST.equals(featureName) ? INSTANCE : null;
    }
    
}
