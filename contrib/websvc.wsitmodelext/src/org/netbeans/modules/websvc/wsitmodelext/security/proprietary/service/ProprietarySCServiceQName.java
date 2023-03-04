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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum ProprietarySCServiceQName {
    SCCONFIGURATION(createSCServiceQName("SCConfiguration")), //NOI18N
    LIFETIME(createSCServiceQName("LifeTime")); //NOI18N

    public static final String PROPRIETARY_SC = 
            "http://schemas.sun.com/ws/2006/05/sc/server"; //NOI18N
    public static final String PROPRIETARY_SC_NS_PREFIX = "scc"; //NOI18N
            
    public static QName createSCServiceQName(String localName){
        return new QName(PROPRIETARY_SC, localName, PROPRIETARY_SC_NS_PREFIX);
    }
    
    ProprietarySCServiceQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (ProprietarySCServiceQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
