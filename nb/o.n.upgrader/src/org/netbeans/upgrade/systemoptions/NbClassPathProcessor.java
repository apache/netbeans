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

package org.netbeans.upgrade.systemoptions;

import java.util.Iterator;
import java.util.List;

/**
 * @author Radek Matous
 */
class NbClassPathProcessor extends PropertyProcessor {
    NbClassPathProcessor() {
        super("org.openide.execution.NbClassPath");//NOI18N
    }

    void processPropertyImpl(String propertyName, Object value) {
        List l = ((SerParser.ObjectWrapper)value).data;
        for (Iterator it = l.iterator(); it.hasNext();) {
            Object elem = (Object) it.next();
            if (elem instanceof SerParser.NameValue) {
                SerParser.NameValue nv = (SerParser.NameValue)elem;
                if (nv.value != null && nv.name != null) {
                    if (nv.name.name.equals("classpath")) {//NOI18N
                        addProperty(propertyName, nv.value.toString());
                    } else  if (nv.name.name.equals("items")) {//NOI18N
                        //skip it - won't be imported
                    }
                }
            }
        }
    }
}
