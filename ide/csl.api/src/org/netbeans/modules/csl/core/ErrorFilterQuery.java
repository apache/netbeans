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

package org.netbeans.modules.csl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ErrorFilter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.util.Lookup;

/**
 * Clients can use this class to filter out some of the parser errors returned by
 * {@link ParserResult.getDiagnostics()}. See the {@link ErrorFilter}
 * documentation. 
 *
 * @author marekfukala
 */
public class ErrorFilterQuery {
    private static final Logger UPDATER_BACKDOOR = Logger.getLogger("org.netbeans.modules.parsing.impl.indexing.LogContext.backdoor");

    public static List<? extends Error> getFilteredErrors(ParserResult parserResult, String featureName) {
        Collection<? extends ErrorFilter.Factory> factories = Lookup.getDefault().lookupAll(ErrorFilter.Factory.class);
        List<Error> filtered = null;
        for(ErrorFilter.Factory factory : factories) {
            ErrorFilter filter = factory.createErrorFilter(featureName);
            String fn = "TLIndexer:" + filterName(factory, filter);
            if (filter == null) {
                continue;
            }
            try {
                LogRecord lr = new LogRecord(Level.INFO, "INDEXER_START");
                lr.setParameters(new Object[] { fn });
                UPDATER_BACKDOOR.log(lr);
                List<? extends Error> result = filter.filter(parserResult);
                if(result != null) {
                    if (filtered == null) {
                        filtered = new ArrayList<Error>(result);
                    } else {
                        filtered.addAll(result); 
                    }
                }
            } finally {
                LogRecord lr = new LogRecord(Level.INFO, "INDEXER_END");
                lr.setParameters(new Object[] { fn });
                UPDATER_BACKDOOR.log(lr);
            }
        }
        return filtered;
    }

    static String filterName(ErrorFilter.Factory fact, ErrorFilter f) {
        Class c;
        
        if (f == null) {
            c = fact.getClass();
        } else {
            c = f.getClass();
        }
        
        String n = c.getName();
        int idx = n.indexOf(".modules.");
        int last = n.lastIndexOf('.');
        if (idx > -1) {
            int n2 = n.indexOf('.', idx + 9);
            return n.substring(idx + 9, n2 + 1) + n.substring(last + 1);
        } else {
           return n.substring(last + 1);
        }
    }
}
