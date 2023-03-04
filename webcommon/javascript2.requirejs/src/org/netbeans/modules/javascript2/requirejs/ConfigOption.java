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

package org.netbeans.modules.javascript2.requirejs;

/**
 *
 * @author ppisl
 */
public enum ConfigOption {
    BASEURL("baseUrl", OptionType.STRING), 
    PATHS("paths", OptionType.OBJECT), 
    BUNDLES("bundles", OptionType.OBJECT), 
    SHIM("shim", OptionType.OBJECT), 
    MAP("map", OptionType.OBJECT), 
    CONFIG("config", OptionType.OBJECT), 
    PACKAGES("packages", OptionType.ARRAY), 
    NODEIDCOMPAT("nodeIdCompat", OptionType.BOOLEAN), 
    WAITSECONDS("waitSeconds", OptionType.NUMBER), 
    CONTEXT("context", OptionType.STRING), 
    DEPS("deps", OptionType.ARRAY), 
    CALLBACK("callback", OptionType.UNKNOWN), 
    ENFORCEDEFINE("enforceDefine", OptionType.BOOLEAN), 
    XHTML("xhtml", OptionType.BOOLEAN), 
    URLARGS("urlArgs", OptionType.STRING), 
    SCRIPTTYPE("scriptType", OptionType.STRING), 
    SKIPDATAMAIN("skipDataMain", OptionType.BOOLEAN);
    
    public static enum OptionType {
        STRING, OBJECT, NUMBER, ARRAY, BOOLEAN, UNKNOWN
    }
    
    private final String name;
    private final OptionType type;

    private ConfigOption(final String name, final OptionType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public OptionType getType() {
        return type;
    }

    public static ConfigOption getEnum(String byName) {
        for (ConfigOption option : ConfigOption.values()) {
            if (option.getName().equals(byName)) {
                return option;
            }
        }
        throw new IllegalArgumentException("No enum defined for this name: " + byName); //NOI18N
    }
    
}
