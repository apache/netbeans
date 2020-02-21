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

package org.netbeans.modules.cnd.api.model.xref;

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmLabelResolver {
    private static CsmLabelResolver DEFAULT = new Default();

    /**
     * Search for usage of referenced label.
     * Return collection of labels in the function.
     * Label name can be null. Service finds all labels in the function.
     * If label name not null then service searches exact label references.
     */
    public abstract Collection<CsmReference> getLabels(CsmFunctionDefinition referencedFunction,
            CharSequence label, Set<LabelKind> kinds);
    
    protected CsmLabelResolver() {
    }
    
    /**
     * Static method to obtain the CsmLabelResolver implementation.
     * @return the selector
     */
    public static CsmLabelResolver getDefault() {
        return DEFAULT;
    }
    
    public static enum LabelKind {
        Definiton,
        Reference,
    }
    /**
     * Implementation of the default selector
     */  
    private static final class Default extends CsmLabelResolver {
        private final Lookup.Result<CsmLabelResolver> res;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmLabelResolver.class);
        }

        @Override
        public Collection<CsmReference> getLabels(CsmFunctionDefinition referencedFunction, CharSequence label, Set<LabelKind> kinds) {
            for (CsmLabelResolver selector : res.allInstances()) {
                return selector.getLabels(referencedFunction, label, kinds);
            }
            return null;
        }
    }
}
