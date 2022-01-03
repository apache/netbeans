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
public abstract class CsmOperatorKindResolver {
    /**
     * default instance
     */
    private static CsmOperatorKindResolver DEFAULT = new Default();
    
    protected CsmOperatorKindResolver() {
    }
    
    /**
     * Static method to obtain the CsmOperatorKindResolver implementation.
     * @return the resolver
     */
    public static CsmOperatorKindResolver getDefault() {
        return DEFAULT;
    }
    
    public enum OperatorKind {
        BINARY,
        UNARY,
        SEPARATOR,
        TYPEMODIFIER,
        UNKNOWN;
    }
    
    /**
     * Detect operator kind
     * for example:
     * Document a*b;
     * Offset point to * (start position)
     * Result is TypeModifier or Binary
     * Possible requestes about:
     * *, &, +, -, <, >.
     */
    public abstract OperatorKind getKind(Document doc, int offset);
    
    /**
     * Implementation of the default resolver
     */  
    private static final class Default extends CsmOperatorKindResolver {
        private final Lookup.Result<CsmOperatorKindResolver> res;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmOperatorKindResolver.class);
        }

        @Override
        public OperatorKind getKind(Document doc, int offset) {
            for (CsmOperatorKindResolver resolver : res.allInstances()) {
                OperatorKind out = resolver.getKind(doc, offset);
                if (out != OperatorKind.UNKNOWN) {
                    return out;
                }
            }
            return OperatorKind.UNKNOWN;
        }
    }
}
