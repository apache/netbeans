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

package org.netbeans.modules.cnd.editor.options;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.openide.util.Exceptions;

public class PreviewPreferences extends AbstractPreferences {

    private Map<String, Object> map = new HashMap<String, Object>();
    private final CodeStyle.Language language;
    private final String styleId;

    public PreviewPreferences(Preferences master, CodeStyle.Language language, String styleId) {
        super(null, ""); // NOI18N
        this.language = language;
        this.styleId = styleId;
        try {
            for (String key : master.keys()) {
                Object o = EditorOptions.getDefault(language, styleId, key);
                if (o instanceof Boolean) {
                    putBoolean(key, master.getBoolean(key, (Boolean)o));
                } else if (o instanceof Integer) {
                    putInt(key, master.getInt(key, (Integer)o));
                } else if (o instanceof String) {
                    map.put(key, master.get(key, o.toString()));
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void makeAllKeys(PreviewPreferences master){
        for(String key : EditorOptions.keys()){
            if (!map.containsKey(key)) {
                Object o = EditorOptions.getDefault(language, styleId, key);
                if (o instanceof Boolean) {
                    Boolean v = master.getBoolean(key,
                                (Boolean)EditorOptions.getDefault(language, master.getStyleId(), key));
                    putBoolean(key, v);
                } else if (o instanceof Integer) {
                    Integer v = master.getInt(key, 
                                (Integer)EditorOptions.getDefault(language, master.getStyleId(), key));
                    putInt(key, v);
                } else if (o instanceof String) {
                    String v = master.get(key, 
                               (String)EditorOptions.getDefault(language, master.getStyleId(), key));
                    map.put(key, v);
                }
            }
        }
    }

    public CodeStyle.Language getLanguage() {
        return language;
    }

    public String getStyleId() {
        return styleId;
    }

    @Override
    protected void putSpi(String key, String value) {
        map.put(key, value);
    }

    @Override
    protected String getSpi(String key) {
        return (String) map.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        map.remove(key);
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        String[] array = new String[map.keySet().size()];
        return map.keySet().toArray(array);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}
