/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.options.indentation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * XXX: Ideally this should be in java.source and ruby modules, but they would need
 * to become friends with editor.settings.storage and there would be two copies of
 * the same code. In the future we may need to add something similar for C/C++ language.
 *
 * @author Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageFilter.class)
public final class FormattingSettingsFromNbPreferences extends StorageFilter<String, TypedValue> {

    public FormattingSettingsFromNbPreferences() {
        super("Preferences"); //NOI18N
    }

    @Override
    public void afterLoad(Map<String, TypedValue> map, MimePath mimePath, String profile, boolean defaults) throws IOException {
        if (defaults || mimePath.size() != 1 || !affectedMimeTypes.containsKey(mimePath.getPath())) {
            return;
        }

        try {
            Preferences nbprefs = getNbPreferences(mimePath.getPath());
            if (nbprefs != null && nbprefs.nodeExists("CodeStyle/default")) { //NOI18N
                Preferences codestyle = nbprefs.node("CodeStyle/default"); //NOI18N
                for(String key : codestyle.keys()) {
                    if (!map.containsKey(key)) {
                        TypedValue typedValue = guessTypedValue(codestyle.get(key, null));
                        if (typedValue != null) {
                            map.put(key, typedValue);
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Injecting '" + key + "' = '" + typedValue.getValue() //NOI18N
                                    + "' (" + typedValue.getJavaType() + ") for '" + mimePath.getPath() + "'"); //NOI18N
                            }
                        }
                    }
                }
            }
        } catch (BackingStoreException bse) {
            // ignore
            LOG.log(Level.FINE, null, bse);
        }
    }

    @Override
    public void beforeSave(Map<String, TypedValue> map, MimePath mimePath, String profile, boolean defaults) throws IOException {
        if (defaults || mimePath.size() != 1 || !affectedMimeTypes.containsKey(mimePath.getPath())) {
            return;
        }

        try {
            Preferences nbprefs = getNbPreferences(mimePath.getPath());
            if (nbprefs != null && nbprefs.nodeExists("CodeStyle/default")) { //NOI18N
                // We loaded the settings from NbPreferences in beforeLoad,
                // they are in the map (maybe modified somehow) and they are
                // going to be saved to MimeLookup. So we can safely clean them up from NbPreferences.
                nbprefs.node("CodeStyle").removeNode(); //NOI18N
                nbprefs.flush();

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Cleaning up NbPreferences/CodeStyle node for '" + mimePath.getPath() + "'"); //NOI18N
                }
            }
        } catch (BackingStoreException bse) {
            // ignore
            LOG.log(Level.FINE, null, bse);
        }
    }

    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------

    //config/Preferences/org/netbeans/modules/java/source/CodeStyle/default.properties
    //config/Preferences/org/netbeans/modules/ruby/CodeStyle/default.properties

    private static final Logger LOG = Logger.getLogger(FormattingSettingsFromNbPreferences.class.getName());
    
    private static final Map<String, String> affectedMimeTypes = new HashMap<String, String>();
    static {
        affectedMimeTypes.put("text/x-java", "org.netbeans.api.java.source.CodeStyle"); //NOI18N
        affectedMimeTypes.put("text/x-ruby", "org.netbeans.modules.ruby.options.CodeStyle"); //NOI18N
    }

    private Preferences getNbPreferences(String mimeType) {
        Preferences prefs = null;

        String className = affectedMimeTypes.get(mimeType);
        if (className != null) {
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            if (loader != null) {
                try {
                    Class clazz = loader.loadClass(className);
                    prefs = NbPreferences.forModule(clazz);
                } catch (ClassNotFoundException ex) {
                    // ignore
                    LOG.log(Level.FINE, null, ex);
                }
            }
        }

        return prefs;
    }

    private TypedValue guessTypedValue(String value) {
        if (value == null) {
            return null;
        }

        if (value.equalsIgnoreCase("true")) { //NOI18N
            return new TypedValue(Boolean.TRUE.toString(), Boolean.class.getName());
        }
        if (value.equalsIgnoreCase("false")) { //NOI18N
            return new TypedValue(Boolean.FALSE.toString(), Boolean.class.getName());
        }

        try {
            Integer i = Integer.parseInt(value);
            return new TypedValue(value, Integer.class.getName());
        } catch (NumberFormatException nfe) {
            // ignore
        }

        try {
            Long l = Long.parseLong(value);
            return new TypedValue(value, Long.class.getName());
        } catch (NumberFormatException nfe) {
            // ignore
        }

        try {
            Float f = Float.parseFloat(value);
            return new TypedValue(value, Float.class.getName());
        } catch (NumberFormatException nfe) {
            // ignore
        }

        try {
            Double d = Double.parseDouble(value);
            return new TypedValue(value, Double.class.getName());
        } catch (NumberFormatException nfe) {
            // ignore
        }

        return new TypedValue(value, String.class.getName());
    }
}
