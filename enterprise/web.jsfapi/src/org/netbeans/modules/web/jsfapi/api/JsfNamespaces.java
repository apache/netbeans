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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.netbeans.modules.web.jsfapi.api.JsfNamespaces.Type.TAGLIB;

/**
 *
 * @author Benjamin Asbach
 */
public enum JsfNamespaces {

    JAVA_SUN_COM_NS(
            entry(TAGLIB, "http://java.sun.com/xml/ns/javaee")
    ),
    XMLNS_JCP_ORG_NS(
            entry(TAGLIB, "http://xmlns.jcp.org/xml/ns/javaee")
    ),
    JAKARTA_EE_NS(
            entry(TAGLIB, "https://jakarta.ee/xml/ns/jakartaee")
    );

    public enum Type {
        TAGLIB;
    }

    private final Map<Type, String> namespaces;

    private JsfNamespaces(Entry<Type, String>... entries) {
        this.namespaces = Collections.unmodifiableMap(new EnumMap<>(ofEntries(entries)));
    }

    public String getNamespace(Type type) {
        return namespaces.get(type);
    }

    /* This method can be replaced with a static import to Map.entry() when enterprise module release target is Java 11+ */
    private static Entry<Type, String> entry(Type type, String value) {
        return new AbstractMap.SimpleEntry<>(type, value);
    }

    /* This method can be replaced with a static import to Map.entry() when enterprise module release target is Java 11+ */
    private static Map<Type, String> ofEntries(Entry<Type, String>... additional) {
        return Stream.of(additional).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
