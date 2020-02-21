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
package org.netbeans.modules.cnd.makeproject.ui.runprofiles;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;

public class ConsoleIntNodeProp extends IntNodeProp {

    private final IntConfiguration intConfiguration;
    IntEditor intEditor = null;
    private final PropertyChangeSupport pcs;

    @SuppressWarnings("unchecked")
    public ConsoleIntNodeProp(IntConfiguration intConfiguration, boolean canWrite, String name, String displayName, String description) {
        super( intConfiguration,  canWrite,  name,  displayName,  description);
        this.intConfiguration = intConfiguration;
        pcs = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void remotePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void setValue(Object v) {
        String s = (String)v;
        if (s != null) {
            for (int i = 0; i < intConfiguration.getNames().length; i++) {
                if (s.equals(intConfiguration.getNames()[i])) {
                    intConfiguration.setValue(i+1);
                    pcs.firePropertyChange(getName(), null, s);
                    break;
                }
            }
        }
        //intConfiguration.setValue((String) v);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (intEditor == null) {
            intEditor = new IntEditor();
        }
        return intEditor;
    }

    private class IntEditor extends PropertyEditorSupport {

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            int v = intConfiguration.getValue();
            if (v == 0) {
                v = RunProfile.getDefaultConsoleType();
            }
             return intConfiguration.getNames()[v-1];
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public String[] getTags() {
            return intConfiguration.getNames();
        }
    }
}
