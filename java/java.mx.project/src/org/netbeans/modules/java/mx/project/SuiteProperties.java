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
package org.netbeans.modules.java.mx.project;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.modules.InstalledFileLocator;

final class SuiteProperties implements AuxiliaryProperties {

    private static final Map<String, String> PROPS = new HashMap<>();

    static {
        File file = InstalledFileLocator.getDefault().locate("org.eclipse.jdt.core.prefs", "org.netbeans.modules.java.mx.project", false);
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter.eclipseFormatterEnabled", "true");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter.eclipseFormatterLocation", file.getPath());
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter.enableFormatAsSaveAction", "true");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter.preserveBreakPoints", "true");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter.SaveActionModifiedLinesOnly", "false");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter.showNotifications", "false");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter.useProjectPref", "false");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter.useProjectSettings", "true");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter46.eclipseFormatterEnabled", "true");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter46.eclipseFormatterLocation", file.getPath());
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter46.enableFormatAsSaveAction", "true");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter46.preserveBreakPoints", "true");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter46.SaveActionModifiedLinesOnly", "false");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter46.showNotifications", "false");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter46.useProjectPref", "false");
        PROPS.put("de-markiewb-netbeans-plugins-eclipse-formatter46.useProjectSettings", "true");
    }

    @Override
    public String get(String key, boolean shared) {
        return PROPS.get(key);
    }

    @Override
    public void put(String key, String value, boolean shared) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterable<String> listKeys(boolean shared) {
        return Collections.unmodifiableSet(PROPS.keySet());
    }
}
