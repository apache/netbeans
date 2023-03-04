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
package org.netbeans.modules.refactoring.java;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Module installation class for Refactoring module.
 *
 * @author Jan Becicka
 * @author Pavel Flaska
 */
public final class RefactoringModule {

    /** Holds the file objects whose attributes represents options */
    private static Preferences preferences = NbPreferences.forModule(RefactoringModule.class);

    /**
     * Gets the attribute of options fileobject. Attribute name is represented
     * by key parameter. If attribute value is not found, defaultValue parameter
     * is used in method return.
     * 
     * @param  key           key whose associated value is to be returned.
     * @param  defaultValue  value used when attribute is not found
     *
     * @return attribute value or defaultValue if attribute is not found
     */
    public static boolean getOption(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * Sets the attribute to options fileobject. This attribute is persitent
     * and allows to re-read it when IDE is restarted. Key and value pair
     * is used in the same way as Map works.
     *
     * @param key    key with which the specified value is to be associated.
     * @param value  value to be associated with the specified key.
     */
    public static void setOption(String key, boolean value) {
        preferences.putBoolean(key, value);
    }
    
    public static void setOption(String key, int value) {
        preferences.putInt(key, value);
    }

    public static int getOption(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    private RefactoringModule() {
    }
}
