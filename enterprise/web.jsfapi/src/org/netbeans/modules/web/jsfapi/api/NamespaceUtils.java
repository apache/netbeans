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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Contains constants and helper methods for work with new ang legacy namespaces.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class NamespaceUtils {

    /** Location of namespaces since JSF 4.0. */
    public static final String JAKARTA_ORG_LOCATION = "jakarta.faces"; //NOI18N
    
    /** Location of namespaces since JSF 2.2. */
    public static final String JCP_ORG_LOCATION = "http://xmlns.jcp.org"; //NOI18N
    
    /** Location of namespaces up to JSF 2.1. */
    public static final String SUN_COM_LOCATION = "http://java.sun.com";  //NOI18N

    /** Mapping of the new namespace to the legacy one. */
    public static final Map<String, String> NS_MAPPING = new HashMap<>(16);

    static {
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/html", "http://java.sun.com/jsf/html");                     //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/core", "http://java.sun.com/jsf/core");                     //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsp/jstl/core", "http://java.sun.com/jsp/jstl/core");           //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsp/jstl/functions", "http://java.sun.com/jsp/jstl/functions"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/facelets", "http://java.sun.com/jsf/facelets");             //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/composite", "http://java.sun.com/jsf/composite");           //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf", "http://java.sun.com/jsf");                               //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/passthrough", "http://java.sun.com/jsf/passthrough");       //NOI18N
    }
    
    /** Mapping of the new Jakarta EE namespace to the JCP. */
    public static final Map<String, String> JAKARTA_NS_MAPPING = new HashMap<>(16);

    static {
        JAKARTA_NS_MAPPING.put("jakarta.faces.html", "http://xmlns.jcp.org/jsf/html");                     //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.faces.core", "http://xmlns.jcp.org/jsf/core");                     //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.tags.core", "http://xmlns.jcp.org/jsp/jstl/core");           //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.tags.fmt", "http://xmlns.jcp.org/jsp/jstl/fmt"); //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.tags.functions", "http://xmlns.jcp.org/jsp/jstl/functions"); //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.faces.facelets", "http://xmlns.jcp.org/jsf/facelets");             //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.faces.composite", "http://xmlns.jcp.org/jsf/composite");           //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.faces", "http://xmlns.jcp.org/jsf");                               //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.faces.passthrough", "http://xmlns.jcp.org/jsf/passthrough");       //NOI18N
        JAKARTA_NS_MAPPING.put("jakarta.faces.component", "http://xmlns.jcp.org/jsf/component");       //NOI18N
    }

    /**
     * Takes map of libraries and namespace and return library for the namespace or its legacy version.
     * @param map map of libraries
     * @param ns namespace to examine
     * @return library for the given or its legacy namespace, {@code null} if no such library was found
     */
    @CheckForNull
    public static <T> T getForNs(Map<String, T> map, String ns) {
        if (map.containsKey(ns)) {
            return map.get(ns);
        }

        LibraryInfo libraryInfo = DefaultLibraryInfo.forNamespace(ns);
        if (libraryInfo == null) {
            ns = DefaultLibraryInfo.COMPOSITE.getValidNamespaces().stream()
                    .filter(ns::startsWith)
                    .findFirst()
                    .orElse(null);
            if (ns == null) {
                return null;
            }
            libraryInfo = DefaultLibraryInfo.forNamespace(ns);
        }

        return libraryInfo.getValidNamespaces().stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Says whether given namespaces collection contains namespace of the library.
     * @param collection collection of namespaces
     * @param library library to check
     * @return {@code true} if the collection contains new or legacy library namespace, {@code false} otherwise
     */
    public static boolean containsNsOf(Collection<String> collection, DefaultLibraryInfo library) {
        return library.getValidNamespaces().stream().anyMatch(collection::contains);
    }
}
