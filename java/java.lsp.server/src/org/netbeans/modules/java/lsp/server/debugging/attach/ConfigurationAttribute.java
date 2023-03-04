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
package org.netbeans.modules.java.lsp.server.debugging.attach;

/**
 * Representation of a single attribute of attach configuration.
 *
 * @author Martin Entlicher
 */
final class ConfigurationAttribute {

    private final String defaultValue;
    private final String description;
    private final boolean mustSpecify;

    public ConfigurationAttribute(String defaultValue, String description, boolean mustSpecify) {
        this.defaultValue = defaultValue;
        this.description = description;
        this.mustSpecify = mustSpecify;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMustSpecify() {
        return mustSpecify;
    }

}
