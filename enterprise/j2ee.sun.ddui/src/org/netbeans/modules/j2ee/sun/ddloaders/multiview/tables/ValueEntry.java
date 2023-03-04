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


/** Use this class for a column if the entry is stored as a value in
 *  the parent bean class.
 *
 * @author Peter Williams
 */
public class ValueEntry extends TableEntry {

    public ValueEntry(String pn, String c, int w) {
        super(pn, c, w, false);
    }

    public ValueEntry(String pn, String c, int w, boolean required) {
        super(pn, c, w, required);
    }

    public ValueEntry(String ppn, String pn, String c, int w, boolean required) {
        super(ppn, pn, c, w, required);
    }

    public ValueEntry(String ppn, String pn, String c, int w, boolean required, boolean isName) {
        super(ppn, pn, c, w, required, isName);
    }

    public ValueEntry(String ppn, String pn, ResourceBundle resBundle,
            String resourceBase, int w, boolean required, boolean isName) {
        super(ppn, pn, resBundle, resourceBase, w, required, isName);
    }

    public Object getEntry(CommonDDBean parent) {
        return parent.getValue(propertyName);
    }

    public void setEntry(CommonDDBean parent, Object value) {
        if(value instanceof String && ((String) value).length() == 0) {
            value = null;
        }
        parent.setValue(propertyName, value);
    }

    public Object getEntry(CommonDDBean parent, int row) {
        return parent.getValue(propertyName, row);
    }

    public void setEntry(CommonDDBean parent, int row, Object value) {
        parent.setValue(propertyName, row, value);
    }

}
