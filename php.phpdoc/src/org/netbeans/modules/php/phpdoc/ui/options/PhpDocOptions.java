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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.phpdoc.ui.options;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.phpdoc.PhpDocScript;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * @author Tomas Mysik
 */
public final class PhpDocOptions {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "phpdoc"; // NOI18N

    private static final PhpDocOptions INSTANCE = new PhpDocOptions();

    // phpdoc script
    private static final String PHPDOC = "phpdoc"; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile boolean phpDocSearched = false;

    private PhpDocOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static PhpDocOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public synchronized String getPhpDoc() {
        String phpDoc = getPreferences().get(PHPDOC, null);
        if (phpDoc == null && !phpDocSearched) {
            phpDocSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(PhpDocScript.SCRIPT_NAME, PhpDocScript.SCRIPT_NAME_LONG, PhpDocScript.SCRIPT_NAME_PHAR);
            if (!scripts.isEmpty()) {
                phpDoc = scripts.get(0);
                setPhpDoc(phpDoc);
            }
        }
        return phpDoc;
    }

    public void setPhpDoc(String phpDoc) {
        getPreferences().put(PHPDOC, phpDoc);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(PhpDocOptions.class).node(PREFERENCES_PATH);
    }
}
