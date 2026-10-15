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
package org.netbeans.modules.css.lib.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lib.AbstractParseTreeNode;
import org.netbeans.modules.css.lib.ErrorsProviderQuery;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marekfukala
 */
public class CssParserResult extends ParserResult {

    public static boolean IN_UNIT_TESTS = false;
    
    private AbstractParseTreeNode parseTree;
    private List<ProblemDescription> diagnostics;
    
    private Map<Class<?>,Object> properties;
    
    public CssParserResult(Snapshot snapshot, AbstractParseTreeNode parseTree, List<ProblemDescription> diagnostics) {
        super(snapshot);
        assert parseTree != null;
        this.parseTree = parseTree;
        this.diagnostics = diagnostics;
    }

    @Override
    protected void invalidate() {
        if(IN_UNIT_TESTS) {
            return ; //some simplification - do not invalidate the result in unit tests
        }
        parseTree = null;
        diagnostics = null;
        properties = null;
    }

    public Node getParseTree() {
        if(parseTree == null) {
            throw new IllegalStateException("Already invalidated parser result, "
                    + "you are likely trying to use it outside of the parsing task runnable!"); //NOI18N
        }
        return parseTree;
    }
    
    /**
     * Gets lexer / parser diagnostics w/o additional issues 
     * possibly added by {@link ExtendedDiagnosticsProvider}.
     * @return list of parsing issues
     */
    public List<ProblemDescription> getParserDiagnostics() {
        return diagnostics;
    }
    
    public List<? extends FilterableError> getDiagnostics(boolean includeFiltered) {
        List<? extends FilterableError> extendedDiagnostics = ErrorsProviderQuery.getExtendedDiagnostics(this);
        if(includeFiltered) {
            return extendedDiagnostics;
        } else {
            //remove filtered issues
            List<FilterableError> result = new ArrayList<>();
            for(FilterableError e : extendedDiagnostics) {
                if(!e.isFiltered()) {
                    result.add(e);
                }
            }
            return result;
        }
    }
    

    @Override
    public List<? extends FilterableError> getDiagnostics() {
        return getDiagnostics(false);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getProperty(Class<T> type) {
        if(properties == null) {
            return null;
        } else {
            return (T)properties.get(type);
        }
    }
    
    public <T> void setProperty(Class<T> type, T value) {
        if(properties == null) {
            properties = new HashMap<>();
        }
        properties.put(type, value);
    }
    
}
