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
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;

/**
 *
 * @author bogdan
 */
public class UnknownDirective implements Rule.AstRule {

    public void computeHints(RuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) throws BadLocationException {
        
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
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
        return HintSeverity.WARNING;
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton("blade.option.directive.hints");
    }

    @Override
    public String getId() {
        return "blade.hint.unknown_directive";
    }

    @Override
    public String getDescription() {
       return "Unknown Directive. The directive my not be in the base laravel blade compiler and was not found in the custom list.";
    }

    @Override
    public String getDisplayName() {
        return "Unknown Directive";
    }
}
