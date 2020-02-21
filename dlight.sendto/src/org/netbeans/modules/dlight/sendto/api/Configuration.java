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

package org.netbeans.modules.dlight.sendto.api;

import org.netbeans.modules.dlight.sendto.spi.Handler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.openide.util.NbBundle;

/**
 *
 */
public final class Configuration implements Cloneable, Comparable<Configuration> {

    private static final AtomicInteger lastID = new AtomicInteger();
    public static final String DISPLAY_NAME = "display_name"; // NOI18N
    public static final String HANDLER_ID = "script_handler_id"; // NOI18N
    // This id is not unique. (clones have the same id as an original)
    private final Integer id;
    private final Map<String, String> map = new HashMap<String, String>();
    private boolean modified = false;

    private Configuration(Integer id) {
        this.id = id;
        map.put(DISPLAY_NAME, NbBundle.getMessage(Configuration.class, "Configuration.default.DisplayName")); // NOI18N
    }

    public Configuration() {
        this(lastID.incrementAndGet());
    }

    @Override
    public Object clone() {
        Configuration clone = new Configuration(id);
        clone.map.putAll(map);
        return clone;
    }

    public Configuration copy() {
        Configuration copy = new Configuration();
        copy.map.putAll(map);
        copy.map.put(DISPLAY_NAME, map.get(DISPLAY_NAME) + "_copy"); // NOI18N
        return copy;
    }

    public boolean isModified() {
        return modified;
    }

    public String getName() {
        return map.get(DISPLAY_NAME);
    }

    public void setName(String name) {
        map.put(DISPLAY_NAME, name);
        modified = true;
    }

    public Handler getHandler() {
        return Handlers.getHandler(map.get(HANDLER_ID));
    }

    public String get(String key) {
        final String result = map.get(key);
        return result == null ? "" : result; // NOI18N
    }

    public void set(String key, String param) {
        map.put(key, param);
        modified = true;
    }

    public Integer getID() {
        return id;
    }

    public void setHandler(Handler handler) {
        map.put(HANDLER_ID, handler.getID());
        modified = true;
    }

    /*package*/ Map<String, String> getProperties() {
        return Collections.unmodifiableMap(map);
    }

    public void applyChanges() {
        getHandler().applyChanges(this);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public void setBoolean(String key, boolean param) {
        set(key, Boolean.toString(param));
    }

    @Override
    public int compareTo(Configuration that) {
        if (that == null) {
            return 1;
        }
        return this.getName().compareTo(that.getName());
    }
}
