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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import java.util.ResourceBundle;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


/**
 *
 * @author Peter Williams
 */
public abstract class TableEntry {

    protected final ResourceBundle bundle;
    protected final String resourceBase;
    protected final String parentPropertyName;
    protected final String propertyName;
    protected final String columnName;
    protected final int columnWidth;
    protected final boolean requiredFieldFlag;
    protected final boolean nameFieldFlag;

    public TableEntry(String pn, String c, int w) {
        this(pn, c, w, false);
    }

    public TableEntry(String pn, String c, int w, boolean required) {
        this(null, pn, c, w, required);
    }

    public TableEntry(String ppn, String pn, String c, int w, boolean required) {
        this(ppn, pn, c, w, required, false);
    }

    public TableEntry(String ppn, String pn, String c, int w, boolean required, boolean isName) {
        parentPropertyName = ppn;
        bundle = null;
        resourceBase = null;
        propertyName = pn;
        columnName = c;
        columnWidth = w;
        requiredFieldFlag = required;
        nameFieldFlag = isName;
    }

    public TableEntry(String ppn, String pn, ResourceBundle resBundle,
            String base, int w, boolean required, boolean isName) {
        parentPropertyName = ppn;
        propertyName = pn;
        bundle = resBundle;
        resourceBase = base;
        columnName = bundle.getString("LBL_" + resourceBase);	// NOI18N
        columnWidth = w;
        requiredFieldFlag = required;
        nameFieldFlag = isName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public boolean isRequiredField() {
        return requiredFieldFlag;
    }

    public boolean isNameField() {
        return nameFieldFlag;
    }

    public String getLabelName() {
        return columnName + " :";	// NOI18N
    }

    public char getLabelMnemonic() {
        assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
        return bundle.getString("MNE_" + resourceBase).charAt(0);	// NOI18N
    }

    public String getAccessibleName() {
        assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
        return bundle.getString("ACSN_" + resourceBase);	// NOI18N
    }

    public String getAccessibleDescription() {
        assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
        return bundle.getString("ACSD_" + resourceBase);	// NOI18N
    }

    public abstract Object getEntry(CommonDDBean parent);
    public abstract void setEntry(CommonDDBean parent, Object value);

    public abstract Object getEntry(CommonDDBean parent, int row);
    public abstract void setEntry(CommonDDBean parent, int row, Object value);

}
