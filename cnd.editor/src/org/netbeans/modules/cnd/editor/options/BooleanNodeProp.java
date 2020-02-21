/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.options;

import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

public class BooleanNodeProp extends PropertySupport<Boolean> {

    private final CodeStyle.Language language;
    private final String optionID;
    private PreviewPreferences preferences;
    private boolean state;

    public BooleanNodeProp(CodeStyle.Language language, PreviewPreferences preferences, String optionID) {
        super(optionID, Boolean.class, getString("LBL_" + optionID), getString("HINT_" + optionID), true, true); // NOI18N
        this.language = language;
        this.optionID = optionID;
        this.preferences = preferences;
        init();
    }

    // create read only property
    public BooleanNodeProp(CodeStyle.Language language, PreviewPreferences preferences, String optionID, boolean state) {
        super(optionID, Boolean.class, getString("LBL_" + optionID), getString("HINT_" + optionID), true, false); // NOI18N
        this.language = language;
        this.optionID = optionID;
        this.preferences = preferences;
        this.state = state;
    }

    private static String getString(String key) {
        return NbBundle.getMessage(BooleanNodeProp.class, key);
    }

    private void init() {
        state = getPreferences().getBoolean(optionID, getDefault());
    }

    private boolean getDefault(){
        return (Boolean) EditorOptions.getDefault(
                getPreferences().getLanguage(), getPreferences().getStyleId(), optionID);
    }
    
    private PreviewPreferences getPreferences() {
        return preferences;
    }

    @Override
    public String getHtmlDisplayName() {
        if (!isDefaultValue()) {
            return "<b>" + getDisplayName(); // NOI18N
        }
        return null;
    }

    @Override
    public Boolean getValue() {
        return Boolean.valueOf(state);
    }

    @Override
    public void setValue(Boolean v) {
        state = v;
        getPreferences().putBoolean(optionID, state);
    }

    @Override
    public void restoreDefaultValue() {
        setValue(getDefault());
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return getDefault() == getValue().booleanValue();
    }
}
