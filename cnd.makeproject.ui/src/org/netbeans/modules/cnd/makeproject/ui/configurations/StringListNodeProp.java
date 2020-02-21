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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.utils.StringListPanel;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;

abstract public class StringListNodeProp extends PropertySupport<List> {

    private final VectorConfiguration<String> configuration;
    private final BooleanConfiguration inheritValues;
    private final String[] texts;
    private final boolean addPathPanel;
    private final HelpCtx helpCtx;

    public StringListNodeProp(VectorConfiguration<String> configuration, BooleanConfiguration inheritValues, String[] texts, boolean addPathPanel, HelpCtx helpCtx) {
        super(texts[0], List.class, texts[1], texts[2], true, true);
        this.configuration = configuration;
        this.inheritValues = inheritValues;
        this.texts = texts;
        this.addPathPanel = addPathPanel;
        this.helpCtx = helpCtx;
    }

    @Override
    public String getHtmlDisplayName() {
        if (configuration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public List getValue() {
        return configuration.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(List v) {
        configuration.setValue(v);
    }

    @Override
    public void restoreDefaultValue() {
        configuration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return configuration.getValue().isEmpty();
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        ArrayList<String> clone = new ArrayList<>();
        clone.addAll(configuration.getValue());
        return new StringEditor(clone);
    }

    abstract protected List<String> convertToList(String text);
    abstract protected String convertToString(List<String> list);

    private class StringEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private final List<String> value;
        private PropertyEnv env;

        public StringEditor(List<String> value) {
            this.value = value;
        }

        @Override
        public void setAsText(String text) {
            super.setValue(StringListNodeProp.this.convertToList(text.trim()));
        }

        @Override
        public String getAsText() {
            return StringListNodeProp.this.convertToString(value);
        }

        @Override
        public java.awt.Component getCustomEditor() {
            String text = null;
            if (inheritValues != null) {
                text = texts[4];
            }
            return new StringListPanel(texts[3], value, addPathPanel, inheritValues, text, this, env, helpCtx);
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
