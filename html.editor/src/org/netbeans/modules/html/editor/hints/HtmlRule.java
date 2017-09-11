/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
