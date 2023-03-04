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
package org.netbeans.modules.csl.hints.infrastructure;

import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.openide.util.NbBundle;

/**
 * Hint which appears always on end of offered hint list, allowing user to
 * access options dialog
 * 
 * @author Tor Norbye
 * @author Max Sauer
 */
final class DisableHintFix implements EnhancedFix {
    private final GsfHintsManager manager;
    private final RuleContext context;
    
    DisableHintFix(GsfHintsManager manager, RuleContext context) {
        this.manager = manager;
        this.context = context;
    }

    public String getText() {
        return NbBundle.getMessage(DisableHintFix.class, "DisableHint"); // NOI18N
    }
    
    public ChangeInfo implement() throws Exception {
        OptionsDisplayer.getDefault().open("Editor/Hints"); // NOI18N
        manager.refreshHints(context);
        return null;
    }

    public CharSequence getSortText() {
        //Hint opening options dialog should always be the lastest in offered list
        return Integer.toString(Integer.MAX_VALUE);
    }
}
