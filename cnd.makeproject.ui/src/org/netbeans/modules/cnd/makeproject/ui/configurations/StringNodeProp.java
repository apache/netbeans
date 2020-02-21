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

import org.openide.nodes.PropertySupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;

public class StringNodeProp extends PropertySupport<String> {

    private final StringConfiguration stringConfiguration;
    private String def = null;
    private boolean canWrite = true;

    public StringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
        super(txt1, String.class, txt2, txt3, true, true);
        this.stringConfiguration = stringConfiguration;
    }

    public StringNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
        super(txt1, String.class, txt2, txt3, true, true);
        this.stringConfiguration = stringConfiguration;
        this.def = def;
    }

    public StringNodeProp(StringConfiguration stringConfiguration, String def, boolean canWrite, String txt1, String txt2, String txt3) {
        super(txt1, String.class, txt2, txt3, true, canWrite);
        this.stringConfiguration = stringConfiguration;
        this.def = def;
    }

    @Override
    public String getHtmlDisplayName() {
        if (stringConfiguration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        }
        else {
            return null;
        }
    }

    @Override
    public String getValue() {
        return stringConfiguration.getValueDef(def);
    }

    @Override
    public void setValue(String v) {
        stringConfiguration.setValue(v);
    }

    @Override
    public void restoreDefaultValue() {
        stringConfiguration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return !stringConfiguration.getModified();
    }
    
    public void setDefaultValue(String def) {
        this.def = def;
        stringConfiguration.setDefaultValue(def);
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean v) {
        canWrite = v;
    }
}
