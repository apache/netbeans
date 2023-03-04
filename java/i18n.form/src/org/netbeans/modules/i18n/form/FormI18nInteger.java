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


package org.netbeans.modules.i18n.form;


import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.java.JavaI18nString;

/**
 * This class extends the capability of <code>JavaI18nString</code> to be
 * <code>FormDesignValue</code> to be used in form property sheets.
 *
 * @author  Peter Zavadsky
 * @see org.netbeans.modules.i18n.java.JavaI18nString
 * @see ResourceBundleStringFormEditor
 * @see org.netbeans.modules.form.FormDesignValue
 */
public class FormI18nInteger extends FormI18nString {

    /** Creates new <code>FormI18nInteger</code>. */
    public FormI18nInteger(I18nSupport i18nSupport) {
        super(i18nSupport);
    }

    /** Cretaes new <code>FormI18nInteger</code> from <code>JavaI18nString</code>. 
     * @param source source which is created new <code>FormI18nInteger</code> from. */
    public FormI18nInteger(JavaI18nString source) {
        super(source);
    }
    
    /** Implements <code>FormDesignValue</code> interface. Gets design value.
     * @see org.netbeans.modules.form.FormDesignValue#getDesignValue(RADComponent radComponent)
     */
    @Override
    public Object getDesignValue() {
        Object designValue = super.getDesignValue();
        if (designValue == FormDesignValue.IGNORED_VALUE) {
            return FormDesignValue.IGNORED_VALUE;
        } else {
            return Integer.decode((String)designValue);
        }
    }
    
    /** The string to replace a property in source code. 
     * @return replacing string
     */
    @Override
    public String getReplaceString() {
        return "Integer.parseInt(" + super.getReplaceString() + ")";
    }
    
}
