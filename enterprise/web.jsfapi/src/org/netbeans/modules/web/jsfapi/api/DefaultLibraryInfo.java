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
package org.netbeans.modules.web.jsfapi.api;

public enum DefaultLibraryInfo implements LibraryInfo {

    // JSF 2.0, JSF 2.1
    HTML("http://xmlns.jcp.org/jsf/html", "Html Basic", "h"), //NOI18N
    JSF_CORE("http://xmlns.jcp.org/jsf/core", "Jsf Core", "f"), //NOI18N
    JSTL_CORE("http://xmlns.jcp.org/jsp/jstl/core", "Jstl Core", "c"), //NOI18N
    JSTL_CORE_FUNCTIONS("http://xmlns.jcp.org/jsp/jstl/functions", "Jstl Core Functions", "fn"), //NOI18N
    FACELETS("http://xmlns.jcp.org/jsf/facelets", "Facelets", "ui"), //NOI18N
    COMPOSITE("http://xmlns.jcp.org/jsf/composite", "Composite Components", "cc"), //NOI18N

    // PrimeFaces
    PRIMEFACES("http://primefaces.org/ui", "PrimeFaces", "p"), //NOI18N
    PRIMEFACES_MOBILE("http://primefaces.org/mobile", "PrimeFaces Mobile", "pm"), //NOI18N

    // JSF 2.2+
    JSF("http://xmlns.jcp.org/jsf", "Jsf", "jsf"), //NOI18N
    PASSTHROUGH("http://xmlns.jcp.org/jsf/passthrough", "Passthrough", "p"); //NOI18N

    private static final DefaultLibraryInfo[] ALL_INFOS = values();

    private String namespace;
    private String displayName;
    private String defaultPrefix;


    private DefaultLibraryInfo(String namespace, String displayName, String defaultPrefix) {
        this.namespace = namespace;
        this.displayName = displayName;
        this.defaultPrefix = defaultPrefix;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * Second supported namespace by the library.
     * @return legacy namespace if any or {@code null}
     */
    @Override
    public String getLegacyNamespace() {
        return NamespaceUtils.NS_MAPPING.get(namespace);
    }

    @Override
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public static LibraryInfo forNamespace(String namespace) {
        for (int i = 0; i < ALL_INFOS.length; i++) {
            LibraryInfo li = ALL_INFOS[i];
            if (li.getNamespace().equals(namespace)
                    || (li.getLegacyNamespace() != null && li.getLegacyNamespace().equals(namespace))) {
                return li;
            }
        }
        return null;
    }


}
