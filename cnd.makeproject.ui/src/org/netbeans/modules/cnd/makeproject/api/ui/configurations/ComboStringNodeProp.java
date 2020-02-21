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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.cnd.makeproject.api.ui.configurations;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ComboStringConfiguration;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

public class ComboStringNodeProp extends Node.Property {

    private final ComboStringConfiguration comboStringConfiguration;
    private final String name;
    private final String description;
    private final boolean canWrite;
    StringEditor intEditor = null;

    @SuppressWarnings("unchecked")
    public ComboStringNodeProp(ComboStringConfiguration comboStringConfiguration, boolean canWrite, String name, String description) {
        super(String.class);
        this.comboStringConfiguration = comboStringConfiguration;
        this.canWrite = canWrite;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortDescription() {
        return description;
    }

    @Override
    public String getHtmlDisplayName() {
        if (comboStringConfiguration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public Object getValue() {
        return comboStringConfiguration.getValue();
    }

    @Override
    public void setValue(Object v) {
        comboStringConfiguration.setValue((String) v);
        comboStringConfiguration.getPicklist().addElement((String) v);
    }

    @Override
    public void restoreDefaultValue() {
        comboStringConfiguration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return !comboStringConfiguration.getModified();
    }

    @Override
    public boolean canWrite() {
        return true;
    }

//    public void setCanWrite(boolean canWrite) {
//        this.canWrite = canWrite;
//    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (intEditor == null) {
            intEditor = new StringEditor();
        }
        return intEditor;
    }

    protected class StringEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv env;

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE); // NOI18N
        }

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            return comboStringConfiguration.getValue();
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public String[] getTags() {
            return comboStringConfiguration.getPicklist().getElementsDisplayName();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            return new StringPanel(this, env);
        }
    }
}
