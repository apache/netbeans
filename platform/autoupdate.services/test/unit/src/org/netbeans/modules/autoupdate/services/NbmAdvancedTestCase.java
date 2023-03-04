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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.util.Utilities;

/**
 *
 * @author Jiri Rechtacek
 */
public class NbmAdvancedTestCase extends NbTestCase {
    
    protected NbmAdvancedTestCase (String name) {
        super (name);
    }
    
    protected File userDir;
    protected File platformDir;
    protected File nextDir;
    protected File installDir;
    
    @Override
    protected void setUp () throws IOException, Exception {
        super.setUp ();
        
        clearWorkDir ();
        
        userDir = new File (getWorkDir (), "userdir");
        assertTrue (userDir.mkdirs ());
        System.setProperty ("netbeans.user", userDir.toString ());
        assertEquals (userDir.toString (), System.getProperty ("netbeans.user"));
        
        installDir = new File (new File (getWorkDir (), "install"), "testnetbeans");
        new File (installDir, "config").mkdirs ();
        
        platformDir = new File (installDir, "platform");
        assertTrue (platformDir.mkdirs ());
        new File (platformDir, "config").mkdirs ();
        System.setProperty ("netbeans.home", platformDir.toString ());
        assertEquals (platformDir.toString (), System.getProperty ("netbeans.home"));
        
        nextDir = new File (installDir, "next");
        assertTrue (nextDir.mkdirs ());
        
        System.setProperty (
            "netbeans.dirs", 
            platformDir.toString () + File.pathSeparatorChar + nextDir.toString ());
    }
    
    public static String generateModuleElementWithRequires (String codeName, String version, String requires, String... deps) {
        return generateModuleElement (codeName, version, "OpenIDE-Module-Requires", requires, false, false, deps);
    }
    
    public static String generateModuleElementWithProviders (String codeName, String version, String provides, String... deps) {
        return generateModuleElement (codeName, version, "OpenIDE-Module-Provides", provides, false, false, deps);
    }
    
    public static String generateModuleElementWithJavaDependency (String codeName, String version, String java, String... deps) {
        return generateModuleElement (codeName, version, "OpenIDE-Module-Java-Dependencies", java, false, false, deps);
    }
    
    public static String generateModuleElement (String codeName, String version, Boolean global, String targetCluster) {
        String res = "<module codenamebase=\"" + codeName + "\" " +
                "homepage=\"http://au.netbeans.org/\" distribution=\"nbresloc:/org/netbeans/api/autoupdate/data/org-yourorghere-independent.nbm\" " +
                "license=\"standard-nbm-license.txt\" downloadsize=\"98765\" " +
                "needsrestart=\"false\" moduleauthor=\"\" " +
                (global == null ? "" : "global=\"" + global + "\" ") + 
                (targetCluster == null || targetCluster.length () == 0 ? "" : "targetcluster=\"" + targetCluster + "\" ") + 
                "releasedate=\"2006/02/23\">";
        res +=  "<manifest OpenIDE-Module=\"" + codeName + "\" " +
                "OpenIDE-Module-Name=\"" + codeName + "\" " +
                "OpenIDE-Module-Specification-Version=\"" + version + "\"/>";
        res += "</module>";
        return res;
    }
    
    public static String generateModuleElement (String codeName, String version,
            String manifestAttribute, String value,
            boolean kit, boolean eager, String... deps) {
        String res = "<module codenamebase=\"" + codeName + "\" " +
                "homepage=\"http://au.netbeans.org/\" distribution=\"nbresloc:/org/netbeans/api/autoupdate/data/org-yourorghere-independent.nbm\" " +
                // makes problem when installing this element, missing file at nbresloc!
                //"homepage=\"http://au.netbeans.org/\" distribution=\"nbresloc:/org/netbeans/api/autoupdate/data/" + dot2dash (codeName) + ".nbm\" " +
                "license=\"standard-nbm-license.txt\" downloadsize=\"98765\" " +
                "needsrestart=\"false\" moduleauthor=\"\" " +
                "eager=\"" + eager + "\" " + 
                "releasedate=\"2006/02/23\">";
        res +=  "<manifest OpenIDE-Module=\"" + codeName + "\" " +
                (deps == null || deps.length == 0 ? "" : "OpenIDE-Module-Module-Dependencies=\"" + deps2ModuleModuleDependencies (deps) + "\" ") +
                "OpenIDE-Module-Name=\"" + codeName + "\" " +
                "AutoUpdate-Show-In-Client=\"" + kit + "\" " +
                (value == null || value.length () == 0 ? "" : "" + manifestAttribute + "=\"" + value + "\" ") +
                "OpenIDE-Module-Specification-Version=\"" + version + "\"/>";
        res += "</module>";
        return res;
    }
    
    public AutoupdateCatalogProvider createUpdateProvider (String catalog) {
        AutoupdateCatalogProvider provider = null;
        try {
            provider = new MyProvider (catalog);
        } catch (IOException x) {
            fail (x.toString ());
        }
        return provider;
    }
    
    private static String dot2dash (String codeName) {
        return codeName.replace ('.', '-');
    }
    
    private static String deps2ModuleModuleDependencies (String... deps) {
        String res = "";
        for (String dep : Arrays.asList (deps)) {
            if (dep.indexOf (">") != -1) {
                dep = dep.replace (">", "&gt;");
            }
            res += res.length() == 0 ? dep : ", " + dep;
        }
        return res;
    }
    
    public static String generateCatalog (String... elements) {
        String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_5.dtd\">" +
                "<module_updates timestamp=\"00/00/19/08/03/2006\">";
        for (String element : Arrays.asList (elements)) {
            res += element;
        }
        res += "</module_updates>";
        return res;
    }
    
    public static String generateInfo (String body) {
        String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Module Info 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-info-2_5.dtd\">";
        if (body.indexOf ("</module>") != -1) {
            res += body.substring (0, body.length () - "</module>".length ());
        } else {
            res += body;
        }
        res += "<license name=\"8B813426\">License for NetBeans module:</license></module>";
        return res;
    }
    
    public File generateNBM (String name, String info) throws IOException {
        File f = new File (getWorkDir (), name);
        
        ZipOutputStream os = new ZipOutputStream (new FileOutputStream (f));
        os.putNextEntry (new ZipEntry ("Info/info.xml"));
        os.write (info.getBytes ());
        os.closeEntry ();
        os.close();
        
        return f;
    }
    
    protected URL generateFile (String s) throws IOException {
        File res = new File (getWorkDir (), "test-catalog.xml");
        OutputStream os = new FileOutputStream (res);
        os.write (s.getBytes ());
        os.close ();
        return Utilities.toURI(res).toURL ();
    }
    
    @SuppressWarnings("unchecked")
    protected UpdateElement installUpdateUnit (UpdateUnit unit) {
        OperationContainer ic = OperationContainer.createForInstall ();
        assertNotNull (unit + " has available update.", unit.getAvailableUpdates ());
        ic.add (unit.getAvailableUpdates ().get (0));
        OperationInfo requiresInfo = (OperationInfo) ic.listAll ().iterator ().next ();
        assertNotNull (requiresInfo);
        ic.add (requiresInfo.getRequiredElements ());
        
        assertTrue ("Install operation on " + unit + " is valid.", ic.listInvalid ().isEmpty ());
        assertFalse ("Something will be installed for " + unit, ic.listAll ().isEmpty ());
        InstallSupport is = (InstallSupport) ic.getSupport ();
        try {
            
            InstallSupport.Validator v = is.doDownload (null, false);
            InstallSupport.Installer i = is.doValidate (v, null);
            is.doInstall (i, null);
            
        } catch (OperationException ex) {
            if (OperationException.ERROR_TYPE.INSTALL == ex.getErrorType ()) {
                // can ingore
                // module system cannot load the module either
            } else {
                fail (ex.toString ());
            }
        }
        
        // check if unit was installed
        assertNotNull (unit + " is installed.", unit.getInstalled ());
        
        return unit.getInstalled ();
    }
    
    public class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider (String s) throws IOException {
            super ("test-updates-with-os-provider", "test-updates-with-os-provider", generateFile (s));
        }
    }
    
}
