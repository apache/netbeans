/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.swing.laf.dark;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * 
 */
public class Installer extends ModuleInstall {

    private static boolean switchEditorColors = false;

    @Override
    public void restored() {
        if( switchEditorColors ) {
            WindowManager.getDefault().invokeWhenUIReady( new Runnable() {

                @Override
                public void run() {
                    switchEditorColorsProfile();
                }
            });
        };
    }

    @Override
    public void validate() throws IllegalStateException {
        Preferences prefs = getPreferences();
        if( !prefs.getBoolean("dark.themes.installed", false) ) { //NOI18N
            prefs.put( "laf", DarkMetalLookAndFeel.class.getName() ); //NOI18N
            switchEditorColors = true;
        }
        prefs.putBoolean( "dark.themes.installed", true ); //NOI18N
        UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo( NbBundle.getMessage(Installer.class, "LBL_DARK_METAL"), DarkMetalLookAndFeel.class.getName()) );
        UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo( NbBundle.getMessage(Installer.class, "LBL_DARK_NIMBUS"), DarkNimbusLookAndFeel.class.getName()) );
    }

    //Use reflection to instantiate ColorModel class and get/set the current profile
    private static final String COLOR_MODEL_CLASS_NAME = "org.netbeans.modules.options.colors.ColorModel"; //NOI18N
    private static final String DARK_COLOR_THEME_NAME = "Norway Today"; //NOI18N

    private boolean isChangeEditorColorsPossible() {
        ClassLoader cl = Lookup.getDefault().lookup( ClassLoader.class );
        if( null == cl )
            cl = Installer.class.getClassLoader();
        try {
            Class klz = cl.loadClass( COLOR_MODEL_CLASS_NAME );
            Object colorModel = klz.newInstance();
            Method m = klz.getDeclaredMethod( "getCurrentProfile", new Class[0] ); //NOI18N
            Object res = m.invoke( colorModel, new Object[0] );
            return res != null && !DARK_COLOR_THEME_NAME.equals(res);
        } catch( Exception ex ) {
            //ignore
        }
        return false;
    }

    private void switchEditorColorsProfile() {
        if( !isChangeEditorColorsPossible() )
            return;

        ClassLoader cl = Lookup.getDefault().lookup( ClassLoader.class );
        if( null == cl )
            cl = Installer.class.getClassLoader();
        try {
            Class klz = cl.loadClass( COLOR_MODEL_CLASS_NAME );
            Object colorModel = klz.newInstance();
            Method m = klz.getDeclaredMethod( "setCurrentProfile", String.class ); //NOI18N
            m.invoke( colorModel, DARK_COLOR_THEME_NAME );
        } catch( Exception ex ) {
            //ignore
            Logger.getLogger( Installer.class.getName() ).log( Level.INFO, "Cannot change editor colors profile.", ex ); //NOI18N
        }
    }

    private Preferences getPreferences() {
        return NbPreferences.root().node( "laf" ); //NOI18N
    }
}
