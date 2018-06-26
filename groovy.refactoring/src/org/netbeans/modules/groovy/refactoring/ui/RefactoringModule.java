/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring.ui;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Module installation class for Refactoring module.
 * Copied from Java Refactoring module because it isn't a part of public API.
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