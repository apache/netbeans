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

package org.netbeans.modules.j2ee.dd.api.common;

import org.openide.util.NbBundle;
/**
 * Exception for cases when adding a new element should be prevented for avoiding duplicity.<br>
 * See : {@link CreateCapability#addBean} method.
 *
 * @author  Milan Kuchtiak
 */
public class NameAlreadyUsedException extends java.lang.Exception {
    private java.lang.String keyProperty, keyValue, beanName;

    /**
     * Constructor NameAlreadyUsedException
     *
     * @param beanName name of the DD element (CommonDDBean object)
     * @param keyProperty name of the property that should be unique
     * @param keyValue value of the keyProperty that causes the duplicity
     */
    public NameAlreadyUsedException (String beanName, String keyProperty, String keyValue) {
        super();
        this.beanName=beanName;
        this.keyProperty=keyProperty;
        this.keyValue=keyValue;
    }
    
    /**
     * Returns the localized message
     * 
     * @return localized message describing the problem.
     */
    public String getMessage() {
        return NbBundle.getMessage(NameAlreadyUsedException.class,"MSG_nameAlreadyUsed",beanName,keyProperty,keyValue);
    }
}
