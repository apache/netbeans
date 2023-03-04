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
package org.netbeans.modules.html.editor.hints;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public abstract class HtmlRule implements AstRule {

    protected boolean isEnabled;
    
    public enum Kinds {
        DEFAULT;
    }
    
    /**
     * Gets the rule priority.
     * Used instead of layer files oredering - see issue:
     * https://netbeans.org/bugzilla/show_bug.cgi?id=223793
     * 
     * Lower numbers means higher priority. Default is 100.
     */
    public int getPriority() {
        return 100; //magic constant
    }
    
    @Override
    public Set<?> getKinds() {
        return Collections.singleton(Kinds.DEFAULT);
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(HtmlRule.class, String.format("%s_Desc", getId()));
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
        return true; //always???
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(HtmlRule.class, getId());
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
    
    protected abstract void run(HtmlRuleContext context, List<Hint> result);
    
    /** return true if the rule is a special html validator rule.
     * 
     * In this case the rule is run even if it is disabled in the hints customizer.
     * 
     * This workaround is used due to the unspecified list of all possible 
     * errors from the html validator. There are several hints representing sets
     * of the validator error reports. There's also the Other rule which contains all
     * the unspecified errors. If some of the concrete rules are disabled they must not
     * be processed by the Other rule so they must run, remove the error from the errors list, 
     * but do not create a hint.
     * 
     */
    protected boolean isSpecialHtmlValidatorRule() {
        return false;
    }
    
    /** 
     * Called by the HtmlHintsProvider.
     * 
     * The value is used only by the special html validstor rules.
     * 
     * See the isSpecialHtmlValidatorRule() method javadoc for more info. 
     */
    final void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
    
}
