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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.core.startup;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
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
        if (Utilities.isUnix()) {
            provides.add("org.openide.modules.os.Unix"); // NOI18N
            if (!Utilities.isMac()) {
                provides.add("org.openide.modules.os.PlainUnix"); // NOI18N
            }
        }
        if (Utilities.isWindows()) {
            provides.add("org.openide.modules.os.Windows"); // NOI18N
        }
        if (Utilities.isMac()) {
            provides.add("org.openide.modules.os.MacOSX"); // NOI18N
        }
        if ((Utilities.getOperatingSystem() & Utilities.OS_OS2) != 0) {
            provides.add("org.openide.modules.os.OS2"); // NOI18N
        }
        if ((Utilities.getOperatingSystem() & Utilities.OS_LINUX) != 0) {
            provides.add("org.openide.modules.os.Linux"); // NOI18N
        }
        if ((Utilities.getOperatingSystem() & Utilities.OS_SOLARIS) != 0) {
            provides.add("org.openide.modules.os.Solaris"); // NOI18N
        }
        
        if (isJavaFX(new File(System.getProperty("java.home")))) {
            provides.add("org.openide.modules.jre.JavaFX"); // NOI18N
        }
    }

    static boolean isJavaFX(File javaHome) {
        return 
            new File(new File(javaHome, "lib"), "jfxrt.jar").exists() || 
            new File(new File(new File(javaHome, "lib"), "ext"), "jfxrt.jar").exists();
    }
}
