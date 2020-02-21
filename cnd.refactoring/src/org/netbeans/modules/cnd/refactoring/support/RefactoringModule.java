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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.refactoring.support;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Module installation class for Refactoring module.
 * based on org.netbeans.modules.refactoring.java.RefactoringModule
 * 
 */
public class RefactoringModule {

    private RefactoringModule() {
    }

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
}
