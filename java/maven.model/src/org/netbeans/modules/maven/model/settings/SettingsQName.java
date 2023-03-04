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
package org.netbeans.modules.maven.model.settings;

import javax.xml.namespace.QName;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author mkleint
 */
public final class SettingsQName {

    public static final String NS_URI = "http://maven.apache.org/POM/4.0.0";  // NOI18N
       public static final String NS_PREFIX = "profile";   // NOI18N
    
    /**
     * version of the namespace used in the file.
     *
     * @since 1.34
     */
    public enum Version {

        /**
         * no namespace
         */
        NONE(null),
        /**
         * old (wrong) namespace - http://maven.apache.org/POM/4.0.0
         */
        OLD("http://maven.apache.org/POM/4.0.0"),
        /**
         * settings own namespace in version 1.0.0
         */
        NEW_100("http://maven.apache.org/SETTINGS/1.0.0"),
        /**
         * settings own namespace in version 1.1.0
         */
        NEW_110("http://maven.apache.org/SETTINGS/1.1.0");
        private final String namespace;

        private Version(String namespace) {
            this.namespace = namespace;
        }

        @CheckForNull
        public String getNamespace() {
            return namespace;
        }
    }
    /**
     * 
     * @param localName
     * @param version
     * @return 
     * @since 1.34
     */
    public static QName createQName(String localName, @NonNull Version version) {
        if (version.getNamespace() != null) {
            return new QName(version.getNamespace(), localName, NS_PREFIX);
        } else {
            return new QName("", localName);
        }
    }
    
    @Deprecated
    public static QName createQName(String localName, boolean ns, boolean old) {
        Version v = resolveVersion(ns, old);
        return createQName(localName, v);
    }

    static Version resolveVersion(boolean ns, boolean old) {
        Version v;
        if (ns) {
            if (old) {
                v = Version.OLD;
            } else {
                v = Version.NEW_100;
            }
        } else {
            v = Version.NONE;
        }
        return v;
    }

    private final QName qName;

    SettingsQName(String localName, @NonNull Version version) {
        qName = SettingsQName.createQName(localName, version);
    }
    
    public QName getQName() {
        return qName;
    }

    public String getName() {
        return qName.getLocalPart();
    }
    
}
