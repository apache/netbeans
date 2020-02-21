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

package org.netbeans.modules.cnd.api.model.syntaxerr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmErrorInfoHintProvider {
    private static final Default DEFAULT = new Default();
    protected CsmErrorInfoHintProvider() {
    }

    public static List<Fix> getFixes(CsmErrorInfo info) {      
        List<Fix> out = DEFAULT.getFixesImpl(info);
        if (out.isEmpty()) {
            out = Collections.<Fix>emptyList();
        }
        return out;
    }
    
    // add extra fixes to already found bag and return that bag (or create new bag and return)
    protected abstract List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound);
    
    //
    // Implementation part
    //
    private static final class Default {

        protected final Lookup.Result<CsmErrorInfoHintProvider> res;        

        public Default() {
            res = Lookup.getDefault().lookupResult(CsmErrorInfoHintProvider.class);
        }

        protected List<Fix> getFixesImpl(CsmErrorInfo info) {
            List<Fix> bag = new ArrayList<Fix>(0);
            for( CsmErrorInfoHintProvider provider : res.allInstances() ) {
                bag = provider.doGetFixes(info, bag);
            }
            return bag;
        }
   }
}
