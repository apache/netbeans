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
package org.netbeans.modules.php.blade.editor.hints;

import java.util.Collections;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.openide.util.NbBundle;

/**
 *
 * @author bogdan
 */
public class PhpDirectiveSyntaxErrorRule implements Rule.AstRule {
    public static final String PHP_SYNTAX_ERROR_HINT_ID = "blade.hint.php_syntax_errors"; //NOI18N
            
    @Override
    public boolean getDefaultEnabled() {
        return false;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return context instanceof BladeHintsProvider.BladeRuleContext;
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(PHP_SYNTAX_ERROR_HINT_ID);
    }

    @Override
    public String getId() {
        return PHP_SYNTAX_ERROR_HINT_ID;
    }

    @Override
    public String getDescription() {
       return NbBundle.getMessage(PhpDirectiveSyntaxErrorRule.class, "AST_Rule_PhpSyntaxError"); //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(PhpDirectiveSyntaxErrorRule.class, "AST_Rule_PhpSyntaxErrorDescription"); //NOI18N
    }
}
