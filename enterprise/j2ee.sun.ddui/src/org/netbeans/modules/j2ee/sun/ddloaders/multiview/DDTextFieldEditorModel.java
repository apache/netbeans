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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.ErrorManager;

/**
 *
 * @author Peter Williams
 */
public abstract class DDTextFieldEditorModel extends TextItemEditorModel {

    private final String nameProperty;
    private final String attrProperty;

    public DDTextFieldEditorModel(final XmlMultiViewDataSynchronizer synchronizer, final String np) {
        super(synchronizer, true, true);
        this.nameProperty = np;
        this.attrProperty = null;
    }
    
    public DDTextFieldEditorModel(final XmlMultiViewDataSynchronizer synchronizer, final String np, String ap) {
        super(synchronizer, true, true);
        this.nameProperty = np;
        this.attrProperty = ap;
    }

    /** Override this to return the parent bean that this object manipulates.
     */
    protected abstract CommonDDBean getBean();
    
    /** Override this if the parent really provides a wrapper for a child bean,
     *  ala JavaWebStartAccess in SunApplicationClient or EnterpriseBeans in SunEjbJar.
     * 
     *  Used by setValue() method to ensure such child beans are created only when
     *  needed.
     */
    protected CommonDDBean getBean(boolean create) {
        return getBean();
    }

    protected String getValue() {
        String result = null;
        CommonDDBean bean = getBean();
        if(bean != null) {
            if(attrProperty == null) {
                result = (String) bean.getValue(nameProperty);
            } else if(nameProperty == null) {
                result = bean.getAttributeValue(attrProperty);
            } else {
                result = bean.getAttributeValue(nameProperty, attrProperty);
            }
        }
        return result;
    }

    protected void setValue(String value) {
        CommonDDBean bean = getBean(true);
        if(bean != null) {
            if(attrProperty == null) {
                getBean().setValue(nameProperty, value);
            } else if(nameProperty == null) {
                getBean().setAttributeValue(attrProperty, value);
            } else {
                getBean().setAttributeValue(nameProperty, attrProperty, value);
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Unable to set property ("
                    + nameProperty + ", " + attrProperty + ") -- bean is null");
        }
    }

}
