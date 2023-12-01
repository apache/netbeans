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
package org.netbeans.api.j2ee.core;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.netbeans.api.j2ee.core.DeploymentDescriptors.Type.BEANS;
import static org.netbeans.api.j2ee.core.DeploymentDescriptors.Type.CONSTRAINT;
import static org.netbeans.api.j2ee.core.DeploymentDescriptors.Type.EAR;
import static org.netbeans.api.j2ee.core.DeploymentDescriptors.Type.VALIDATION;
import static org.netbeans.api.j2ee.core.DeploymentDescriptors.Type.WEB;
import static org.netbeans.api.j2ee.core.DeploymentDescriptors.Type.WEB_FRAGMENT;

/**
 * This class contains information about which schema version is used for a
 * specific Java EE or Jakarta EE version.
 *
 * @author Benjamin Asbach
 */
public enum DeploymentDescriptors {

    J2EE_13(
            entry(WEB, "2.3"),
            entry(EAR, "1.4")
    ),
    J2EE_14(
            entry(WEB, "2.4"),
            entry(EAR, "1.4")
    ),
    JAVA_EE_5(
            entry(WEB, "2.5"),
            entry(EAR, "5")
    ),
    JAVA_EE_6_WEB(
            entry(WEB, "3.0"),
            entry(WEB_FRAGMENT, "3.0"),
            entry(BEANS, "1.0"),
            entry(VALIDATION, "1.0"),
            entry(CONSTRAINT, "1.0")
    ),
    JAVA_EE_6(JAVA_EE_6_WEB,
            entry(EAR, "6")
    ),
    JAVA_EE_7_WEB(
            entry(WEB, "3.1"),
            entry(WEB_FRAGMENT, "3.1"),
            entry(BEANS, "1.1"),
            entry(VALIDATION, "1.1"),
            entry(CONSTRAINT, "1.1")
    ),
    JAVA_EE_7(JAVA_EE_7_WEB,
            entry(EAR, "7")
    ),
    JAVA_EE_8_WEB_AND_JAKARTA_EE_8_WEB(
            entry(WEB, "4.0"),
            entry(WEB_FRAGMENT, "4.0"),
            entry(BEANS, "2.0"),
            entry(VALIDATION, "2.0"),
            entry(CONSTRAINT, "2.0")
    ),
    JAVA_EE_8_AND_JAKARTA_EE_8(JAVA_EE_8_WEB_AND_JAKARTA_EE_8_WEB,
            entry(EAR, "8")
    ),
    JAKARTA_EE_9_WEB_AND_JAKARTA_EE_91_WEB(
            entry(WEB, "5.0"),
            entry(WEB_FRAGMENT, "5.0"),
            entry(BEANS, "3.0"),
            entry(VALIDATION, "3.0"),
            entry(CONSTRAINT, "3.0")
    ),
    JAKARTA_EE_9_AND_JAKARTA_EE_91(JAKARTA_EE_9_WEB_AND_JAKARTA_EE_91_WEB,
            entry(EAR, "9")
    ),
    JAKARTA_EE_10_WEB(
            entry(WEB, "6.0"),
            entry(WEB_FRAGMENT, "6.0"),
            entry(BEANS, "4.0"),
            entry(VALIDATION, "3.0"),
            entry(CONSTRAINT, "3.0")
    ),
    JAKARTA_EE_10(JAKARTA_EE_10_WEB,
            entry(EAR, "10")
    );

    public enum Type {
        WEB,
        WEB_FRAGMENT,
        BEANS,
        VALIDATION,
        CONSTRAINT,
        EAR;
    }

    private final Map<Type, String> mapping;

    DeploymentDescriptors(Entry<Type, String>... entries) {
        this.mapping = Collections.unmodifiableMap(new EnumMap<>(ofEntries(entries)));
    }

    DeploymentDescriptors(DeploymentDescriptors base, Entry<Type, String>... additional) {
        Map<Type, String> map = new EnumMap<>(base.mapping);
        map.putAll(ofEntries(additional));

        this.mapping = Collections.unmodifiableMap(map);
    }

    /* This method can be replaced with a static import to Map.entry() when enterprise module release target is Java 11+ */
    private static Entry<Type, String> entry(Type type, String value) {
        return new AbstractMap.SimpleEntry<>(type, value);
    }

    /* This method can be replaced with a static import to Map.entry() when enterprise module release target is Java 11+ */
    private static Map<Type, String> ofEntries(Entry<Type, String>... additional) {
        return Stream.of(additional).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public Map<Type, String> getMapping() {
        return mapping;
    }
}
