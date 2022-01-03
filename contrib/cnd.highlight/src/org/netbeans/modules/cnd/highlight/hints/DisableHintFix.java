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

package org.netbeans.modules.cnd.highlight.hints;

import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.openide.util.NbBundle;

/**
 *
 */
public class DisableHintFix implements EnhancedFix {
    private final CodeAuditInfo info;

    DisableHintFix(CodeAuditInfo error) {
        this.info = error;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(DisableHintFix.class, "DisableHint") // NOI18N
                .concat(" - ") // NOI18N
                .concat(info.getAuditID());
    }

    @Override
    public ChangeInfo implement() throws Exception {
        OptionsDisplayer.getDefault().open("Editor/Hints/text/x-cnd+sourcefile/" + info.getProviderID() + "/" + info.getAuditID()); // NOI18N
        return null;
    }

    @Override
    public CharSequence getSortText() {
        //Hint opening options dialog should always be the lastest in offered list
        return "\uFFFF"; //NOI18N
    }
    
    public static interface CodeAuditInfo {
        String getProviderID();
        String getAuditID();
    }
}
