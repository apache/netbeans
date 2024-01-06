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

import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Benjamin Asbach
 */
public enum JsfVersion {
    
    JSF_1_0("1.0"),
    JSF_1_1("1.1"),
    JSF_1_2("1.2"),
    JSF_2_0("2.0"),
    JSF_2_1("2.1"),
    JSF_2_2("2.2"),
    JSF_2_3("2.3"),
    JSF_3_0("3.0"),
    JSF_4_0("4.0"),
    JSF_4_1("4.1");

    private final String version;

    private JsfVersion(String version) {
        this.version = version;
    }

    public String getShortName() {
        if (isAtLeast(JSF_4_0)) {
            return "Faces " + version;
        }
        return "JSF " + version;
    }

    /**
     * Find out if the version of the JsfVersion is equal or higher to given 
     * JsfVersion.
     * @param jsfVersion
     * @return true if this JsfVersion is equal or higher to given one,
     *         false otherwise
     */
    public boolean isAtLeast(@NonNull JsfVersion jsfVersion) {
        return this.ordinal() >= jsfVersion.ordinal();
    }
    
    /**
     * Find out if the version of the JsfVersion is equal or lower to given 
     * JsfVersion.
     * @param jsfVersion
     * @return true if this JsfVersion is equal or lower to given one,
     *         false otherwise
     */
    public boolean isAtMost(@NonNull JsfVersion jsfVersion) {
        return this.ordinal() <= jsfVersion.ordinal();
    }

    public static JsfVersion latest() {
        return values()[values().length - 1];
    }
}
