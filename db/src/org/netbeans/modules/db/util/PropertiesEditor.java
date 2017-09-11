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
package org.netbeans.modules.db.util;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.Properties;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Custom editor for properties - mainly exists to call custom editor
 *
 * @author Matthias BlÃ¤sing
 */
public class PropertiesEditor extends PropertyEditorSupport implements ExPropertyEditor {

    private boolean canWrite = true;

    @Override
    public String getAsText() {
        Properties value = (Properties) getValue();
        if (value == null || value.size() == 0) {
            return NbBundle.getMessage(PropertiesEditor.class,
                    "NoPropertiesSet");                                 //NOI18N
        } else {
            return value.toString();
        }
    }

    /**
     * Can't be called and throws IllegalArgumentException
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException("Can't be set by setAsText");//NOI18N
    }

    @Override
    public String getJavaInitializationString() {
        return null; // does not generate any code
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public java.awt.Component getCustomEditor() {
        PropertyEditorPanel pep = new PropertyEditorPanel(
                (Properties) this.getValue(), canWrite);
        pep.addPropertyChangeListener(PropertyEditorPanel.PROP_VALUE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                setValue(((PropertyEditorPanel) pce.getSource()).getValue());
            }
        });
        return pep;
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        FeatureDescriptor d = env.getFeatureDescriptor();
        if (d instanceof Node.Property) {
            canWrite = ((Node.Property) d).canWrite();
        }
    }

    public boolean isEditable() {
        return canWrite;
    }
}