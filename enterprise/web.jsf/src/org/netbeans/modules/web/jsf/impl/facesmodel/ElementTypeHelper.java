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
package org.netbeans.modules.web.jsf.impl.facesmodel;

/**
 * This is helper class for getting correct, validated element types.
 * Element types corresponds with javaee_x.xsd and web-facesconfig_x_x.xsd element types.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class ElementTypeHelper {

    private ElementTypeHelper() {
    }

    /**
     * Picks valid string value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to string element type
     */
    public static String pickString(String string) {
        return (string != null) ? string.trim() : null;
    }

    /**
     * Picks valid fully-qualified-classType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to fully-qualified-classType element type
     */
    public static String pickFullyQualifiedClassType(String string) {
        return pickString(string);
    }

    /**
     * Picks valid java-identifierType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to java-identifierType element type
     */
    public static String pickJavaIdentifierType(String string) {
        return pickString(string);
    }

    /**
     * Picks valid java-typeType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to java-typeType element type
     */
    public static String pickJavaTypeType(String string) {
        return pickString(string);
    }

    /**
     * Picks valid faces-config-value-classType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to faces-config-value-classType element type
     */
    public static String pickFacesConfigValueClassType(String string) {
        return pickFullyQualifiedClassType(string);
    }

    /**
     * Picks valid faces-config-from-view-idType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to faces-config-from-view-idType element type
     */
    public static String pickFacesConfigFromViewIdType(String string) {
        return pickString(string);
    }

}
