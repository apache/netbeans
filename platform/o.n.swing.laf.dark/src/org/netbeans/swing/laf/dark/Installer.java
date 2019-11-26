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
