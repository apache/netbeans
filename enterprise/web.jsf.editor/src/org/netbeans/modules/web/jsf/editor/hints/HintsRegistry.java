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

package org.netbeans.modules.web.jsf.editor.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;

/**
 * TODO declarative providers
 *
 * @author marekfukala
 */
public class HintsRegistry {
    
    private static HintsRegistry INSTANCE;

    public static synchronized HintsRegistry getDefault() {
        if(INSTANCE == null) {
            INSTANCE = new HintsRegistry();
        }
        return INSTANCE;
    }

    private final Collection<HintsProvider> PROVIDERS;

    private HintsRegistry() {
        PROVIDERS = new ArrayList<>();
        //init providers
        PROVIDERS.add(new ComponentUsagesChecker());
        PROVIDERS.add(new LibraryDeclarationChecker());
    }

    public List<Hint> gatherHints(RuleContext context) {
        List<Hint> hints = new ArrayList<>();
        for(HintsProvider provider : PROVIDERS) {
            hints.addAll(provider.compute(context));
        }
        return hints;
    }

}
