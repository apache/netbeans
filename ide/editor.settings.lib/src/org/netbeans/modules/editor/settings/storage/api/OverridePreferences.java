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
package org.netbeans.modules.editor.settings.storage.api;

/**
 * Mixin interface to detect if a value is inherited (defaulted) or not.
 * The interface is to be implemented on Preferences objects (e.g. Mime Preferences),
 * which support some sort of fallback, inheritance or default. It allows
 * clients to determine whether a preference key is defined at the level represented
 * by the Preferences object, or whether the value produced by {@link java.util.prefs.Preferences#get}
 * originates in some form of default or inherited values.
 * <p/>
 * This interface is implemented on Editor settings Preferences objects 
 * stored in MimeLookup (can be obtained by <code>MimeLookup.getLookup(mime).lookup(Preferences.class)</code>).
 *
 * @since 1.38
 * @author sdedic
 */
public interface OverridePreferences {
    /**
     * Determines whether the value is defined locally.
     * If the value comes from an inherited or default set of values,
     * the method returns {@code false}.
     * 
     * @param key key to check
     * @return true, if the value is defined locally, false if inherited.
     */
    public boolean      isOverriden(String key);
}
