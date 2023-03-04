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
