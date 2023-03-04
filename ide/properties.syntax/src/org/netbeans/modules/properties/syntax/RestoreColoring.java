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

package org.netbeans.modules.properties.syntax;

import java.util.MissingResourceException;

import org.netbeans.editor.LocaleSupport;

import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;

/**
 * Instalation class of module properties syntax.
 *
 * @author Petr Jiricka, Libor Kramolis, Jesse Glick
 */
public class RestoreColoring extends ModuleInstall {

    /** <code>Localizer</code> passed to editor. */
    private static LocaleSupport.Localizer localizer;

    /** Registers properties editor, installs options and copies settings. Overrides superclass method.  */
    public void restored() {
        installOptions();
    }

    /** Uninstalls properties options. And cleans up editor settings copy. Overrides superclass method. */
    public void uninstalled() {
        uninstallOptions();
    }

    /** Installs properties editor and print options. */
    public void installOptions() {
        // Adds localizer.
        LocaleSupport.addLocalizer(localizer = new LocaleSupport.Localizer() {
            public String getString(String key) {
                try {
                    return NbBundle.getBundle(RestoreColoring.class).getString(key);
                } catch(MissingResourceException mre) {
                    return null;
                }
            }
        });
    }

    /** Uninstalls properties editor and print options. */
    public void uninstallOptions() {
        // remove localizer
        LocaleSupport.removeLocalizer(localizer);
    }
    
}
