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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.settings;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Settings for the C/C++/Fortran. The compile/build options stored
 * in this class are <B>default</B> options which will be applied to new files.
 * Once a file has been created its options may diverge from the defaults. A
 * files options do not change if these default options are changed.
 */
public final class CppSettings extends SharedClassObject {

    /** serial uid */
    static final long serialVersionUID = -2942467713237077336L;

    private static final String PROP_REPLACEABLE_STRINGS_TABLE = "replaceableStringsTable"; //NOI18N

    /** The resource bundle for the form editor */
    private static ResourceBundle bundle;

//    private String path = null;

    private static CppSettings cppSettings = null;

    /** Return the singleton cppSettings */
    public static CppSettings getDefault() {
        // See IZ 120502
        if (cppSettings == null) {
            cppSettings = findObject(CppSettings.class, true);
        }
        return cppSettings;
    }

    /**
     * Sets the replaceable strings table - used during instantiating
     * from template.
     */
    public void setReplaceableStringsTable(String table) {
        String t = getReplaceableStringsTable();
        if (t.equals(table)) {
            return;
        }
        getPreferences().put(PROP_REPLACEABLE_STRINGS_TABLE, table);
        firePropertyChange(PROP_REPLACEABLE_STRINGS_TABLE, t, table);
    }

    /**
     * Gets the replaceable strings table - used during instantiating
     * from template.
     */
    public String getReplaceableStringsTable() {
        String table = getPreferences().get(PROP_REPLACEABLE_STRINGS_TABLE, null);
        if (table == null) {
            return "";
        } else {
            return table;
        }
    }


    /**
     * Gets the replaceable table as the Properties class.
     * @return the properties
     */
    public Properties getReplaceableStringsProps() {
        Properties props = new Properties();

        try {
            props.load(new StringReader(getReplaceableStringsTable()));
        }
        catch (IOException e) {
        }
        return props;
    }

    /**
     * Get the display name.
     *
     *  @return value of OPTION_CPP_SETTINGS_NAME
     */
    public String displayName () {
        return getString("OPTION_CPP_SETTINGS_NAME"); //NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx("Welcome_opt_editing_sources"); //NOI18N
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(CppSettings.class);
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    /** @return localized string */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(CppSettings.class);
        }
        return bundle.getString(s);
    }

    /**
     * CndAbstractDataLoader used to call {@link #getReplaceableStringProps()}
     * directly. Here is a better solution.
     */
    @ServiceProvider(service = CreateFromTemplateAttributesProvider.class)
    public static final class AttributesProvider implements CreateFromTemplateAttributesProvider {
        @Override
        public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
            if (MIMENames.CND_TEXT_MIME_TYPES.contains(template.getPrimaryFile().getMIMEType())) {
                Map<String, Object> map = new HashMap<String, Object>();
                // convert Properties (Map<?, ?>) into Map<String, ?>
                for (Map.Entry<?, ?> entry : CppSettings.getDefault().getReplaceableStringsProps().entrySet()) {
                    if (entry.getKey() instanceof String) {
                        map.put((String) entry.getKey(), entry.getValue());
                    }
                }
                return map;
            } else {
                return null;
            }
        }
    }
}
