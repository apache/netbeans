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
package org.netbeans.modules.maven.model.pom;

import javax.xml.namespace.QName;

/**
 *
 * @author mkleint
 */
public final class POMQName {

    public static final String NS_URI = "http://maven.apache.org/POM/4.0.0";  // NOI18N
    public static final String NS_PREFIX = "pom";   // NOI18N        
    
    public static QName createQName(String localName, boolean ns) {
        if (ns) {
            return new QName(NS_URI, localName, NS_PREFIX);
        } else {
            return new QName("", localName);
        }
    }

    public static QName createQName(String localName) {
        return createQName(localName, true);
    }

    private final QName qName;

    POMQName(String localName, boolean ns) {
        qName = createQName(localName, ns);
    }
    
    public QName getQName() {
        return qName;
    }

    public String getName() {
        return qName.getLocalPart();
    }
    
}
