/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
