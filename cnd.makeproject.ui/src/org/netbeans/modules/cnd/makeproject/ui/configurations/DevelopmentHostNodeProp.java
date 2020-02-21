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

import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.customizer.DevelopmentHostCustomizer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class DevelopmentHostNodeProp extends Node.Property {

    private final DevelopmentHostConfiguration configuration;
    private final boolean canWrite;
    private final String name;
    private final String displayName;
    private final String description;

    @SuppressWarnings("unchecked")
    public DevelopmentHostNodeProp(DevelopmentHostConfiguration configuration, boolean canWrite, String name, String displayName, String description) {
        super(Integer.class);
        this.configuration = configuration;
        this.canWrite = canWrite;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        setValue("title", NbBundle.getMessage(DevelopmentHostNodeProp.class, "DLG_TITLE_Connect")); // NOI18N
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getShortDescription() {
        return description;
    }

    @Override
    public String getHtmlDisplayName() {
        if (configuration.getModified()) {
            return "<b>" + getDisplayName() + "</b>"; // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public Object getValue() {
        return configuration.getValue();
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canAutoComplete")) { //NOI18N
            return Boolean.FALSE;
        }
        return super.getValue(attributeName);
    }        
    
    @Override
    public void setValue(Object value) {
        configuration.setValue((String) value, true);
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
        return !configuration.getModified();
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new IntEditor();
    }

    private class IntEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv env;

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            return configuration.getDisplayName(true);
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public void setValue(Object value) {
            super.setValue(value);
        }


        @Override
        public String[] getTags() {
            List<String> l = new ArrayList<>();
            ServerList.getRecords().forEach((record) -> {
                l.add(record.getDisplayName());
            });
            return l.toArray(new String[l.size()]);
        }

        @Override
        public boolean supportsCustomEditor() {
            return canWrite;
        }

        @Override
        public Component getCustomEditor() {
            return new DevelopmentHostCustomizer(configuration, this, env);
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
