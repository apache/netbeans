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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Basic property editor for dates.
 * Could be expanded; see tasklist/usertasks/src/org/netbeans/modules/tasklist/usertasks/DateEditor.java.
 * @author Jesse Glick
 */
public class DateEditor extends PropertyEditorSupport {

    private static DateFormat fmt = DateFormat.getDateTimeInstance();

    @Override
    public String getAsText() {
        Date d = (Date)getValue();
        if (d != null) {
            return fmt.format(d);
        } else {
            return ""; // NOI18N
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if ("".equals(text)) { // NOI18N
            setValue(null);
        } else {
            try {
                setValue(fmt.parse(text));
            } catch (ParseException e) {
                throw (IllegalArgumentException)new IllegalArgumentException(e.toString()).initCause(e);
            }
        }
    }
    
    // #67524: Properties Editor doesn't support Date type. Replaces them with '???'
    @Override
    public String getJavaInitializationString () {
        return "new java.util.Date(" + ((Date) getValue()).getTime() + "L)"; // NOI18N
    }

}
