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
/*
 * AuthorizationEntry.java
 *
 * Created on May 19, 2006, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.openide.util.NbBundle;


/**
 *
 * @author  Peter Williams
 */
public class AuthorizationEntry extends GenericTableModel.TableEntry {

    private String childAttributeName;

    public AuthorizationEntry(String propName, String attrName, String resBase) {
        super(null, propName, NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"),
                resBase, false, false);

        childAttributeName = attrName;
    }

    public Object getEntry(CommonDDBean parent) {
        Object result = null;

        if(parent.size(propertyName) > 0) {
            result = parent.getAttributeValue(propertyName, childAttributeName);
        }

        return result;
    }

    public void setEntry(CommonDDBean parent, Object value) {
        // Set blank strings to null.  This object also handles message-security-binding
        // though, so we have to check it out.        
        if(value instanceof String && ((String) value).length() == 0) {
            value = null;
        }

        if(parent.size(propertyName) == 0) {
            parent.setValue(propertyName, Boolean.TRUE);
        }

        parent.setAttributeValue(propertyName, childAttributeName, (String) value);
    }

    public Object getEntry(CommonDDBean parent, int row) {
        throw new UnsupportedOperationException();
    }	

    public void setEntry(CommonDDBean parent, int row, Object value) {
        throw new UnsupportedOperationException();
    }
}
