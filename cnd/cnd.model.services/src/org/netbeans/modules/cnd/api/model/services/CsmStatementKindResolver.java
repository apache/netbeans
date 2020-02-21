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

package org.netbeans.modules.cnd.api.model.services;

import javax.swing.text.Document;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmStatementKindResolver {
    /**
     * default instance
     */
    private static CsmStatementKindResolver DEFAULT = new Default();
    
    protected CsmStatementKindResolver() {
    }
    
    /**
     * Static method to obtain the CsmStatementKindResolver implementation.
     * @return the resolver
     */
    public static CsmStatementKindResolver getDefault() {
        return DEFAULT;
    }
    
    public enum StatementKind {
        NAMESPACE,
        CLASS,
        STRIUCT,
        ENUM,
        UNION,
        FUNCTION,
        DECLARATION,
        EXPRESSION,
        COMPOUND,
        OTHER,
        UNKNOWN;
    }
    
    /**
     * Detect statement kind
     */
    public abstract StatementKind getKind(Document doc, int offset);

    
    /**
     * Implementation of the default resolver
     */  
    private static final class Default extends CsmStatementKindResolver {
        private final Lookup.Result<CsmStatementKindResolver> res;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmStatementKindResolver.class);
        }

        @Override
        public StatementKind getKind(Document doc, int offset) {
            for (CsmStatementKindResolver resolver : res.allInstances()) {
                StatementKind out = resolver.getKind(doc, offset);
                if (out != StatementKind.UNKNOWN) {
                    return out;
                }
            }
            return StatementKind.UNKNOWN;
        }
    }
}
