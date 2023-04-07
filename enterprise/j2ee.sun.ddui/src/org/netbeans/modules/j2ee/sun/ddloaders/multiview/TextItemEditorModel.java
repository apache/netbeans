/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

import java.util.Objects;

/**
 * @author pfiala,pcw
 */
public abstract class TextItemEditorModel extends ItemEditorHelper.ItemEditorModel {

    protected XmlMultiViewDataSynchronizer synchronizer;
    private boolean emptyAllowed;
    private boolean emptyIsNull;

    protected TextItemEditorModel(XmlMultiViewDataSynchronizer synchronizer, boolean emptyAllowed) {
        this(synchronizer, emptyAllowed, false);
    }
    
    protected TextItemEditorModel(XmlMultiViewDataSynchronizer synchronizer, boolean emptyAllowed, boolean emptyIsNull) {
        this.synchronizer = synchronizer;
        this.emptyAllowed = emptyAllowed;
        this.emptyIsNull = emptyIsNull;
    }

    protected boolean validate(String value) {
        return emptyAllowed ? true : value != null && value.length() > 0;
    }

    protected abstract void setValue(String value);

    protected abstract String getValue();

    public final boolean setItemValue(String value) {
        if (emptyAllowed && emptyIsNull && value != null) {
            while (value.length() > 0 && value.charAt(0) == ' ') {
                value = value.substring(1);
            }
            if (value.length() == 0) {
                value = null;
            }
        }
        if (validate(value)) {
            String currentValue = getValue();
            if (!Objects.equals(value, currentValue)) {
                setValue(value);
                synchronizer.requestUpdateData();
            }
            return true;
        } else {
            return false;
        }
    }

    public final String getItemValue() {
        String value = getValue();
        return value == null ? "" : value;
    }

    public void documentUpdated() {
        setItemValue(getEditorText());
    }
}
