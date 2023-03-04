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
package org.netbeans.core;

import java.awt.EventQueue;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProxySelector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.core.startup.CoreBridge;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.ManifestSection;
import org.netbeans.swing.plaf.Startup;
import org.openide.nodes.NodeOp;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/** Implements necessary callbacks from module system.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=CoreBridge.class)
public final class CoreBridgeImpl extends CoreBridge {
    protected void attachToCategory (Object category) {
        ModuleActions.attachTo(category);
    }
    
    protected void loadDefaultSection (
        org.netbeans.core.startup.ManifestSection s, 
        org.openide.util.lookup.InstanceContent.Convertor<ManifestSection,Object> convertor, 
        boolean load
    ) {
        if (load) {
            if (convertor != null) {
                MainLookup.register(s, convertor);
            } else {
                MainLookup.register(s);
            }
        } else {
            if (convertor != null) {
                MainLookup.unregister(s, convertor);
            } else {
                MainLookup.unregister(s);
            }
        }
    }
    
    protected void loadActionSection(ManifestSection.ActionSection s, boolean load) throws Exception {
        if (load) {
            ModuleActions.add(s);
        } else {
            ModuleActions.remove(s);
        }
    }
    
    @Override
    protected void loadLoaderSection(ManifestSection.LoaderSection s, boolean load) throws Exception {
        if (load) {
            NbLoaderPool.add(s);
        } else {
            NbLoaderPool.remove((org.openide.loaders.DataLoader)s.getInstance(), NbLoaderPool.getNbLoaderPool());
        }
    }
    
    protected void loaderPoolTransaction (boolean begin) {
        if (begin) {
            NbLoaderPool.beginUpdates();
        } else {
            NbLoaderPool.endUpdates();
        }
    }
    
    public void setStatusText (String status) {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(status);
    }

    @Override
    public void initializePlaf (final Class uiClass, final int uiFontSize, final java.net.URL themeURL) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                final Class uiClassToUse = null == uiClass ? getPreferredUIClass() : uiClass;
                if( null != uiClass ) {
                    System.setProperty( "nb.laf.forced", uiClass.getName() ); //NOI18N
                }
                EventQueue.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        Startup.run(uiClassToUse, uiFontSize, themeURL, NbBundle.getBundle(Startup.class));
                    }
                });
            }
        });
    }

    @SuppressWarnings("deprecation")
    public org.openide.util.Lookup lookupCacheLoad () {
        return NbLoaderPool.getNbLoaderPool().findServicesLookup();
    }

    public int cli(
        String[] args,
        InputStream inputStream, 
        OutputStream outputStream, 
        OutputStream errorStream, 
        File file
    ) {
        /*
        try {
            org.netbeans.api.sendopts.CommandLine.getDefault().parse(
                string, inputStream, outputStream, file
            );
            for (int i = 0; i < string.length; i++) {
                string[i] = null;
            }
        } catch (CommandException ex) {
            ex.printStackTrace();
            return ex.getExitCode();
        }
         */
        return CLIOptions2.INSTANCE.cli(args);
    }
    
    public void registerPropertyEditors() {
        doRegisterPropertyEditors();
    }

    /**Flag to avoid multiple adds of the same path to the
     * of PropertyEditorManager if multiple tests call 
     * registerPropertyEditors() */
    private static boolean editorsRegistered=false;
    /** Register NB specific property editors.
     *  Allows property editor unit tests to work correctly without 
     *  initializing full NetBeans environment.
     *  @since 1.98 */
    private static final void doRegisterPropertyEditors() {
        //issue 31879
//        if (editorsRegistered) return;
//        String[] syspesp = PropertyEditorManager.getEditorSearchPath();
//        String[] nbpesp = new String[] {
//            "org.netbeans.beaninfo.editors", // NOI18N
//            "org.openide.explorer.propertysheet.editors", // NOI18N
//        };
//        String[] allpesp = new String[syspesp.length + nbpesp.length];
//        System.arraycopy(nbpesp, 0, allpesp, 0, nbpesp.length);
//        System.arraycopy(syspesp, 0, allpesp, nbpesp.length, syspesp.length);
//        PropertyEditorManager.setEditorSearchPath(allpesp);
//        PropertyEditorManager.registerEditor (java.lang.Character.TYPE, org.netbeans.beaninfo.editors.CharEditor.class);
//        PropertyEditorManager.registerEditor(String[].class, org.netbeans.beaninfo.editors.StringArrayEditor.class); 
//        // use replacement hintable/internationalizable primitive editors - issues 20376, 5278
//        PropertyEditorManager.registerEditor (Integer.TYPE, org.netbeans.beaninfo.editors.IntEditor.class);
//        PropertyEditorManager.registerEditor (Boolean.TYPE, org.netbeans.beaninfo.editors.BoolEditor.class);
        
        NodeOp.registerPropertyEditors();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NodeOp.registerPropertyEditors();
            }
        });

        ProxySelector selector = Lookup.getDefault().lookup(ProxySelector.class);
        if (selector != null) {
            // install java.net.ProxySelector
            ProxySelector.setDefault(selector);
        }

        editorsRegistered = true;
    }

    /**
     * Checks Preferences for look and feel class name that user selected in Options window.
     * @return Look and feel class selected in Options window or null.
     */
    private static Class getPreferredUIClass() {
        Preferences prefs = NbPreferences.root().node( "laf" ); //NOI18N
        String uiClassName = prefs.get( "laf", null ); //NOI18N
        if( null == uiClassName )
            return null;
        ClassLoader loader = Lookup.getDefault().lookup( ClassLoader.class );
        if( null == loader )
            loader = ClassLoader.getSystemClassLoader();
        try {
            Class uiClass = loader.loadClass( uiClassName );
            return uiClass;
        } catch( ClassNotFoundException ex ) {
            //HACK ModuleInstall.uninstalled() is never called so let's check if Dark Themes module has been uninstalled
            if( prefs.getBoolean( "dark.themes.installed", false) ) {
                prefs.remove( "laf" ); //NOI18N
                prefs.remove( "dark.themes.installed" ); //NOI18N
            } else {
                Logger.getLogger( CoreBridgeImpl.class.getName() ).log( Level.INFO, "Cannot use look and feel class: " + uiClassName, ex ); //NOI18N
            }
        }
        return null;
    }
}
