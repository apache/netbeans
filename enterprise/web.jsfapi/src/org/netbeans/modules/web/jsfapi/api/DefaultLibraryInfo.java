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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public enum DefaultLibraryInfo implements LibraryInfo {

    // JSF 2.0, JSF 2.1
    HTML(
            sortedSet(
                    "jakarta.faces.html",
                    "http://xmlns.jcp.org/jsf/html",
                    "http://java.sun.com/jsf/html"
            ),
            "Html Basic",
            "h"
    ), //NOI18N
    JSF_CORE(
            sortedSet(
                    "jakarta.faces.core",
                    "http://xmlns.jcp.org/jsf/core",
                    "http://java.sun.com/jsf/core"
            ),
            "Jsf Core",
            "f"
    ), //NOI18N
    JSTL_CORE(
            sortedSet(
                    "jakarta.tags.core",
                    "http://xmlns.jcp.org/jsp/jstl/core",
                    "http://java.sun.com/jsp/jstl/core"
            ),
            "Jstl Core",
            "c"
    ), //NOI18N
    JSTL_CORE_FUNCTIONS(
            sortedSet(
                    "jakarta.tags.functions",
                    "http://xmlns.jcp.org/jsp/jstl/functions",
                    "http://java.sun.com/jsp/jstl/functions"
            ),
            "Jstl Core Functions",
            "fn"
    ), //NOI18N
    FACELETS(
            sortedSet(
                    "jakarta.faces.facelets",
                    "http://xmlns.jcp.org/jsf/facelets",
                    "http://java.sun.com/jsf/facelets"
            ),
            "Facelets",
            "ui"
    ), //NOI18N
    COMPOSITE(
            sortedSet(
                    "jakarta.faces.composite",
                    "http://xmlns.jcp.org/jsf/composite",
                    "http://java.sun.com/jsf/composite"
            ),
            "Composite Components",
            "cc"
    ), //NOI18N

    // PrimeFaces
    PRIMEFACES(
            sortedSet(
                    "http://primefaces.org/ui"
            ),
            "PrimeFaces",
            "p"
    ), //NOI18N
    PRIMEFACES_MOBILE(
            sortedSet(
                    "http://primefaces.org/mobile"
            ),
            "PrimeFaces Mobile",
            "pm"
    ), //NOI18N

    // JSF 2.2+
    JSF(
            sortedSet(
                    "jakarta.faces",
                    "http://xmlns.jcp.org/jsf"
            ),
            "Jsf",
            "jsf"
    ), //NOI18N
    PASSTHROUGH(
            sortedSet(
                    "jakarta.faces.passthrough",
                    "http://xmlns.jcp.org/jsf/passthrough"
            ),
            "Passthrough",
            "p"
    ); //NOI18N

    private final Set<String> allValidNamespaces;
    private final String displayName;
    private final String defaultPrefix;

    private DefaultLibraryInfo(Set<String> allValidNamespaces, String displayName, String defaultPrefix) {
        this.allValidNamespaces = allValidNamespaces;
        this.displayName = displayName;
        this.defaultPrefix = defaultPrefix;
    }

    @Override
    public String getNamespace() {
        return allValidNamespaces.iterator().next();
    }

    @Override
    public Set<String> getValidNamespaces() {
        return allValidNamespaces;
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
        return Stream.of(values())
                .filter(lib -> lib.getValidNamespaces().contains(namespace))
                .findFirst()
                .orElse(null);
    }

    private static Set<String> sortedSet(String... entries) {
        Set<String> sortedSet = new LinkedHashSet<>();
        Stream.of(entries).forEach(sortedSet::add);

        return Collections.unmodifiableSet(sortedSet);
    }
}
