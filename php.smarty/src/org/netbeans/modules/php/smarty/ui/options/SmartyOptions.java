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

package org.netbeans.modules.php.smarty.ui.options;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * @author Martin Fousek
 */
public final class SmartyOptions {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    protected static final String PREFERENCES_PATH = "smarty"; // NOI18N

    private static final SmartyOptions INSTANCE = new SmartyOptions();

    // default values for Smarty properties
    public static final int DEFAULT_TPL_SCANNING_DEPTH = 1;

    // preferences properties names
    private static final String OPEN_DELIMITER = "{"; // NOI18N
    private static final String CLOSE_DELIMITER = "}"; // NOI18N
    protected static final String PROP_TPL_VERSION = "tpl-version";
    protected static final String PROP_TPL_TOGGLE_COMMENT = "tpl-toggle-comment";

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private SmartyOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static SmartyOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getDefaultOpenDelimiter() {
        return getPreferences().get(OPEN_DELIMITER, SmartyFramework.OPEN_DELIMITER);
    }

    public void setDefaultOpenDelimiter(String delimiter) {
        getPreferences().put(OPEN_DELIMITER, delimiter);
        SmartyFramework.setDelimiterDefaultOpen(delimiter);
    }

    public String getDefaultCloseDelimiter() {
        return getPreferences().get(CLOSE_DELIMITER, SmartyFramework.CLOSE_DELIMITER);
    }

    public void setDefaultCloseDelimiter(String delimiter) {
        getPreferences().put(CLOSE_DELIMITER, delimiter);
        SmartyFramework.setDelimiterDefaultClose(delimiter);
    }

    public SmartyFramework.Version getSmartyVersion() {
        String version = getPreferences().get(PROP_TPL_VERSION, "SMARTY3"); // NOI18N
        return SmartyFramework.Version.valueOf(version);
    }

    public void setSmartyVersion(SmartyFramework.Version version) {
        getPreferences().put(PROP_TPL_VERSION, version.name());
        SmartyFramework.setSmartyVersion(version);
    }

    public SmartyFramework.ToggleCommentOption getToggleCommentOption() {
        String commentOption = getPreferences().get(PROP_TPL_TOGGLE_COMMENT,
                SmartyFramework.ToggleCommentOption.SMARTY.name());
        return SmartyFramework.ToggleCommentOption.valueOf(commentOption);
    }

    public void setToggleCommentOption(SmartyFramework.ToggleCommentOption commentOption) {
        getPreferences().put(PROP_TPL_TOGGLE_COMMENT, commentOption.name());
        SmartyFramework.setToggleCommentOption(commentOption);
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(SmartyOptions.class).node(PREFERENCES_PATH);
    }

}
