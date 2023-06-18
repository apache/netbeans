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
package org.netbeans.modules.web.jsfapi.api;

import java.util.Map;

/**
 * Enum which represents the JSF version and with the corresponding mapping of
 * default xhtml namespace prefix and urn.
 *
 * @author Benjamin Asbach
 */
public enum JsfVersion {

    JSF_1_0("1.0", JsfNamespaces.JAVA_SUN_COM),
    JSF_1_1("1.1", JsfNamespaces.JAVA_SUN_COM),
    JSF_1_2("1.2", JsfNamespaces.JAVA_SUN_COM),
    JSF_2_0("2.0", JsfNamespaces.JAVA_SUN_COM),
    JSF_2_1("2.1", JsfNamespaces.JAVA_SUN_COM),
    JSF_2_2("2.2", JsfNamespaces.XMLNS_JCP_ORG),
    JSF_2_3("2.3", JsfNamespaces.XMLNS_JCP_ORG),
    JSF_3_0("3.0", JsfNamespaces.XMLNS_JCP_ORG),
    JSF_4_0("4.0", JsfNamespaces.JAKARTA);

    private final String version;

    private final Map<String, String> prefixUriMapping;

    private JsfVersion(String version, Map<String, String> prefixUriMapping) {
        this.version = version;
        this.prefixUriMapping = prefixUriMapping;
    }

    public static JsfVersion latest() {
        return JsfVersion.values()[JsfVersion.values().length - 1];
    }

    public String getVersion() {
        return version;
    }

    public String getNamespaceUri(String prefix) {
        return prefixUriMapping.get(prefix);
    }

    public boolean isAtLeast(JsfVersion jsfVersion) {
        return this.ordinal() >= jsfVersion.ordinal();
    }
}
