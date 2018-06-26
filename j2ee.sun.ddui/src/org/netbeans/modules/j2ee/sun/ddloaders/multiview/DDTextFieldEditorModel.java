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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
