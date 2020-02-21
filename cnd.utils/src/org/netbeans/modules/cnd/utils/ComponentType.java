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
package org.netbeans.modules.cnd.utils;

import org.netbeans.modules.cnd.spi.utils.ComponentVersionProvider;
import org.openide.util.Lookup;

/**
 *
 */
public enum ComponentType {

    CND("cnd"), //NOI18N
    OSS_IDE("sside"), //NOI18N
    DBXTOOL("dbxtool"), //NOI18N
    DLIGHTTOOL("dlighttool"), //NOI18N
    CODE_ANALYZER("analytics"), //NOI18N
    PROJECT_CREATOR("ide_project"); //NOI18N
    
    private static ComponentType component;
    private String version = ""; //NOI18N
    private final String tag;

    private ComponentType(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static ComponentType getComponent() {
        if (component == null) {
            component = CND;
            String ide = System.getProperty("spro.ide.name"); // NOI18N
            for (ComponentType c : ComponentType.values()) {
                if (c.getTag().equals(ide)) {
                    component = c;
                    break;
                }
            }
        }
        return component;
    }

    public static String getVersion() {
        ComponentType current = getComponent();
        if (current.version == null || current.version.isEmpty()) {
            for (ComponentVersionProvider provider : Lookup.getDefault().lookupAll(ComponentVersionProvider.class)) {
                String version = provider.getVersion(current.getTag());
                if (version != null) {
                    current.version = version;
                    break;
                }
            }
        }
        return current.version;
    }
    
    public static String getFullName() {
        return getComponent().toString() + " " + getVersion(); //NOI18N
    }
    
}
