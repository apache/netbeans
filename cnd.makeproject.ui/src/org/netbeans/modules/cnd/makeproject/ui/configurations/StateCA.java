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

package org.netbeans.modules.cnd.makeproject.ui.configurations;

import org.netbeans.modules.cnd.api.project.CodeAssistance;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public enum StateCA {
    ParsedSource, ExtraParsedSource, ParsedOrphanHeader, IncludedHeader, ExcludedSource, ExcludedHeader, NotYetParsed;

    public static StateCA getState(Configuration configuration, Item item, ItemConfiguration itemConfiguration) {
        boolean source = itemConfiguration.isCompilerToolConfiguration();
        CodeAssistance.State caState = getCodeAssistanceState(item);
        switch (caState) {
            case ParsedSource:
                return itemConfiguration.getExcluded().getValue() ? ExtraParsedSource : ParsedSource;                    
            case ParsedOrphanHeader:
                return ParsedOrphanHeader;
            case ParsedIncludedHeader:
                return IncludedHeader;
            case NotParsed:
                // check if NativeFileItem would be parsed
                if (!item.isExcluded()) {
                    return NotYetParsed;
                } 
                return source ? ExcludedSource : ExcludedHeader;
            default:
                throw new IllegalStateException("unexpected CodeAssistance.State " + caState); // NOI18N
        }
    }
    
    private static CodeAssistance.State getCodeAssistanceState(Item item) {
        CodeAssistance CAProvider = Lookup.getDefault().lookup(CodeAssistance.class);
        if (CAProvider != null) {
            return CAProvider.getCodeAssistanceState(item);
        }
        return CodeAssistance.State.NotParsed;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(StateCA.class, "CodeAssistanceItem_" + name()); //NOI18N
    }
}
