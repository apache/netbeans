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
package org.netbeans.modules.cnd.analysis.api;

import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;

/**
 *
 */
public class CodeAuditProviderImpl {

    private static final Default DEFAULT = new Default();

    public static Collection<CodeAuditProvider> getDefault() {
        return DEFAULT.getAuditProviders();
    }

    private static final class Default {

        private final Lookup.Result<CodeAuditProvider> res;

        Default() {
            res = Lookup.getDefault().lookupResult(CodeAuditProvider.class);
        }

        public Collection<CodeAuditProvider> getAuditProviders() {
            List<CodeAuditProvider> audits = new ArrayList<CodeAuditProvider>();
            for (CodeAuditProvider selector : res.allInstances()) {
                audits.add(selector);
            }
            return audits;
        }
    }
}
