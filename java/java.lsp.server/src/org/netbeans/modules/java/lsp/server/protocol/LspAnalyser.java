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
package org.netbeans.modules.java.lsp.server.protocol;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.lsp.server.ui.AnalyserConfig;
import org.netbeans.modules.java.source.usages.BinaryAnalyser;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = BinaryAnalyser.Config.class, position = 1000)
public final class LspAnalyser extends BinaryAnalyser.Config {
    private static final String PROP_USG_LVL = "org.netbeans.modules.java.source.usages.BinaryAnalyser.usages"; //NOI18N
    private static final String PROP_ID_LVL = "org.netbeans.modules.java.source.usages.BinaryAnalyser.idents"; //NOI18N

    private static final UsagesLevel DEFAULT_USAGES_LEVEL = UsagesLevel.EXEC_VAR_REFS;
    private static final IdentLevel DEFAULT_IDENT_LEVEL = IdentLevel.VISIBLE;

    private final UsagesLevel usgLvl;
    private final IdentLevel idLvl;

    public LspAnalyser() {
        usgLvl = resolveUsagesLevel();
        idLvl = resolveIdentLevel();
    }

    @Override
    @NonNull
    public UsagesLevel getUsagesLevel() {
        return usgLvl;
    }

    @Override
    @NonNull
    public IdentLevel getIdentLevel() {
        return idLvl;
    }

    @NonNull
    private static UsagesLevel resolveUsagesLevel() {
        UsagesLevel lvl = AnalyserConfig.fullIndex() ?
                UsagesLevel.ALL:
                null;
        if (lvl == null) {
            lvl = UsagesLevel.forName(System.getProperty(PROP_USG_LVL));
            if (lvl == null) {
                lvl = DEFAULT_USAGES_LEVEL;
            }
        }
        return lvl;
    }

    @NonNull
    private static IdentLevel resolveIdentLevel() {
        IdentLevel lvl = IdentLevel.forName(System.getProperty(PROP_ID_LVL));
        if (lvl == null) {
            lvl = DEFAULT_IDENT_LEVEL;
        }
        return lvl;
    }
}
