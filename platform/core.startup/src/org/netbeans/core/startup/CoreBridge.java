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
package org.netbeans.core.startup;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.BaseUtilities;
import org.openide.util.lookup.Lookups;

/** Interface to environment that the Module system needs around itself.
 *
 * @author Jaroslav Tulach
 */
public abstract class CoreBridge {

    public static CoreBridge getDefault () {
        CoreBridge b = Lookup.getDefault().lookup(CoreBridge.class);
        if (b == null) {
            b = new FakeBridge();
        }
        return b;
    }
    
    /** Attaches or detaches to current category of actions.
     * @param category name or null
     */
    protected abstract void attachToCategory(Object category);
    
    protected abstract void loadDefaultSection (
        ManifestSection ms, 
        org.openide.util.lookup.InstanceContent.Convertor<ManifestSection,Object> convertor, 
        boolean add
    );                                         
    
    protected abstract void loadActionSection(ManifestSection.ActionSection s, boolean load) throws Exception;
    
    protected abstract void loadLoaderSection(ManifestSection.LoaderSection s, boolean load) throws Exception;
    
    protected abstract void loaderPoolTransaction (boolean begin);

    /** Abstracts away from definition of property editors. 
     * @since 1.7 */
    public abstract void registerPropertyEditors();

    public abstract Lookup lookupCacheLoad ();
    
    /** Delegates to status displayer.
     */
    public abstract void setStatusText (String status);
    
    public abstract void initializePlaf (Class uiClass, int uiFontSize, java.net.URL themeURL);
    
    public abstract int cli(
        String[] string, 
        InputStream inputStream, 
        OutputStream outputStream, 
        OutputStream errorStream, 
        File file
    );
    
    
    /** Default implementation of the bridge, so certain
     * applications can run without any bridge being present.
     */
    private static final class FakeBridge extends CoreBridge {
        /** Attaches or detaches to current category of actions.
         * @param category name or null
         */
        protected void attachToCategory (Object category) {

        }

        protected void loadDefaultSection (
            ManifestSection ms, 
            org.openide.util.lookup.InstanceContent.Convertor convertor, 
            boolean add
        ) {
        }

        protected void loadActionSection(ManifestSection.ActionSection s, boolean load) throws Exception {
            s.getInstance();
        }

        protected void loadLoaderSection(ManifestSection.LoaderSection s, boolean load) throws Exception {
        }

        protected void loaderPoolTransaction (boolean begin) {
            // just ignore
        }

        protected void addToSplashMaxSteps (int cnt) {
        }
        protected void incrementSplashProgressBar () {
        }

        @Override
        public Lookup lookupCacheLoad () {
            return Lookups.forPath("Services");
        }
        public void lookupCacheStore (Lookup l) throws java.io.IOException {
        }

        public void setStatusText (String status) {
            System.err.println(status);
        }

        public void initializePlaf (Class uiClass, int uiFontSize, java.net.URL themeURL) {
        }

        public void registerPropertyEditors() {
        }

        public int cli(String[] string, InputStream inputStream, OutputStream outputStream, OutputStream errorStream, File file) {
            return 0;
        }
    }

    /**
     * Define {@code org.openide.modules.os.*} tokens according to the current platform.
     * @param provides a collection that may be added to
     */
    public static void defineOsTokens(Collection<? super String> provides) {
        if (BaseUtilities.isUnix()) {
            provides.add("org.openide.modules.os.Unix"); // NOI18N
            if (!BaseUtilities.isMac()) {
                provides.add("org.openide.modules.os.PlainUnix"); // NOI18N
            }
        }
        if (BaseUtilities.isWindows()) {
            provides.add("org.openide.modules.os.Windows"); // NOI18N
        }
        if (BaseUtilities.isMac()) {
            provides.add("org.openide.modules.os.MacOSX"); // NOI18N
        }
        if ((BaseUtilities.getOperatingSystem() & BaseUtilities.OS_OS2) != 0) {
            provides.add("org.openide.modules.os.OS2"); // NOI18N
        }
        if ((BaseUtilities.getOperatingSystem() & BaseUtilities.OS_LINUX) != 0) {
            provides.add("org.openide.modules.os.Linux"); // NOI18N
        }
        if ((BaseUtilities.getOperatingSystem() & BaseUtilities.OS_SOLARIS) != 0) {
            provides.add("org.openide.modules.os.Solaris"); // NOI18N
        }
        
        if (isJavaFX(new File(System.getProperty("java.home")))) {
            provides.add("org.openide.modules.jre.JavaFX"); // NOI18N
        }

        provides.add("org.openide.modules.arch." + System.getProperty("os.arch"));
    }

    static boolean isJavaFX(File javaHome) {
        try {
            Class.forName("javafx.application.Platform"); // NOI18N
            return true;
        } catch (ClassNotFoundException ex) {
            return
                new File(new File(javaHome, "lib"), "jfxrt.jar").exists() ||
                new File(new File(new File(javaHome, "lib"), "ext"), "jfxrt.jar").exists();

        }
    }
}
