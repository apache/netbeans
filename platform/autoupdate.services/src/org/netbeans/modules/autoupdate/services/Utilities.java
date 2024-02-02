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

package org.netbeans.modules.autoupdate.services;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.CodeSigner;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.PKIXRevocationChecker.Option;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.core.startup.AutomaticDependencies;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.modules.autoupdate.updateprovider.DummyModuleInfo;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.netbeans.spi.autoupdate.KeyStoreProvider.TrustLevel;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.updater.ModuleDeactivator;
import org.netbeans.updater.ModuleUpdater;
import org.netbeans.updater.UpdateTracking;
import org.netbeans.updater.UpdaterDispatcher;
import org.openide.filesystems.FileUtil;
import org.openide.modules.*;
import org.openide.util.*;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class Utilities {
    public static final String N_A = "N/A";
    public static final String UNSIGNED = "UNSIGNED";
    public static final String SIGNATURE_UNVERIFIED = "SIGNATURE_UNVERIFIED";
    public static final String SIGNATURE_VERIFIED = "SIGNATURE_VERIFIED";
    public static final String TRUSTED = "TRUSTED";
    public static final String MODIFIED = "MODIFIED";

    private Utilities() {}

    public static final String UPDATE_DIR = "update"; // NOI18N
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String DOWNLOAD_DIR = UPDATE_DIR + FILE_SEPARATOR + "download"; // NOI18N
    public static final String RUNNING_DOWNLOAD_DIR = UPDATE_DIR + FILE_SEPARATOR + "download-in-progress"; // NOI18N
    public static final String NBM_EXTENTSION = ".nbm";
    public static final String JAR_EXTENSION = ".jar"; //OSGi bundle
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("yyyy/MM/dd"); // NOI18N
    public static final String ATTR_ESSENTIAL = "AutoUpdate-Essential-Module";

    private static final String PLUGIN_MANAGER_FIRST_CLASS_MODULES = "plugin.manager.first.class.modules"; // NOI18N

    private static final String USER_KS_KEY = "userKS";
    private static final String USER_KS_FILE_NAME = "user.ks";
    private static final String KS_USER_PASSWORD = "open4user";
    private static Lookup.Result<KeyStoreProvider> keyStoreLookupResult;
    private static Map<TrustLevel,List<KeyStore>> keystoreCache = Collections.synchronizedMap(new HashMap<>());
    private static final Logger err = Logger.getLogger(Utilities.class.getName ());

    public static Collection<KeyStore> getKeyStore (TrustLevel trustLevel) {
        if (keyStoreLookupResult == null) {
            keyStoreLookupResult = Lookup.getDefault().lookupResult(KeyStoreProvider.class);
            keyStoreLookupResult.addLookupListener(new KeyStoreProviderListener());
        }

        List<KeyStore> result = keystoreCache.get(trustLevel);

        if (result == null) {
            Collection<? extends KeyStoreProvider> c = keyStoreLookupResult.allInstances();
            if (c == null || c.isEmpty()) {
                return Collections.emptyList();
            }
            List<KeyStore> kss = new ArrayList<>();

            for (KeyStoreProvider provider : c) {
                if (provider.getTrustLevel() == trustLevel) {
                    KeyStore ks = provider.getKeyStore();
                    if (ks != null) {
                        kss.add(ks);
                    }
                }
            }

            result = Collections.unmodifiableList(kss);

            keystoreCache.put(trustLevel, result);
        }

        return result;
    }

    /**
     *
     * @param archiveCertPath
     * @param trustedCertificates
     * @return
     */
    public static String verifyCertificates(CodeSigner archiveCertPath,
        Collection<Certificate> trustedCertificates,
        Set<TrustAnchor> trustedCACertificates,
        Collection<Certificate> validateCertificates,
        Set<TrustAnchor> validationCACertificates) {
        assert archiveCertPath != null;

        List<? extends Certificate> archiveCertificates = archiveCertPath.getSignerCertPath().getCertificates();

        if(archiveCertificates.isEmpty()) {
            return UNSIGNED;
        }

        // Case 1: We have direct trust into one of the certificates of the
        //         certificate chain
        if(isChainTrusted(archiveCertificates, trustedCertificates)) {
            return TRUSTED;
        }

        // Case 2: We trust the CA, that issued a certificate - do the
        //         normal CertPathValidation
        if(verifyCACertificatePath(trustedCACertificates, archiveCertPath)) {
            return TRUSTED;
        }

        // Case 3: We have a list of certificates, that we directly mark as
        //         valid
        if(isChainTrusted(archiveCertificates, validateCertificates)) {
            return SIGNATURE_VERIFIED;
        }

        // Case 4: We trust the CA to do validation
        if (verifyCACertificatePath(validationCACertificates, archiveCertPath)) {
            return SIGNATURE_VERIFIED;
        }

        // Case 5: File is not signed
        return SIGNATURE_UNVERIFIED;
    }

    private static boolean verifyCACertificatePath(Set<TrustAnchor> trustedCACertificates, CodeSigner archiveCertPath) {
        if(trustedCACertificates.isEmpty()) {
            return false;
        }
        try {
            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
            PKIXParameters verificationParameters = new PKIXParameters(trustedCACertificates);
            PKIXRevocationChecker rc = (PKIXRevocationChecker) cpv.getRevocationChecker();
            rc.setOptions(EnumSet.of(Option.SOFT_FAIL));
            verificationParameters.addCertPathChecker(rc);
            if (archiveCertPath.getTimestamp() != null) {
                cpv.validate(archiveCertPath.getSignerCertPath(), verificationParameters);
                verificationParameters.setDate(archiveCertPath.getTimestamp().getTimestamp());
            }
            // validate raises a CertPathValidatorException if validation failed
            cpv.validate(archiveCertPath.getSignerCertPath(), verificationParameters);
            return true;
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException ex) {
            // InvalidAlgorithmParameterException - Should not get here - trustAnchor cannot be null -> collection cannot be empty
            // NoSuchAlgorithmException - Should not get here - "PKIX" is proper algorythm
            err.log(Level.SEVERE, "Certificate verification failed - " + ex.getMessage(), ex);
            //SIGNATURE_UNVERIFIED - result = 0;
        } catch (CertPathValidatorException ex) {
            // CertPath cannot be validated
            err.log(Level.INFO, "Cannot validate certificate path - " + ex.getMessage(), ex);
            //SIGNATURE_UNVERIFIED - result = 0;
        }
        return false;
    }

    private static boolean isChainTrusted(Collection<? extends Certificate> archiveCertificates, Collection<? extends Certificate> trustedCertificates) {
        Collection<Certificate> c = new HashSet<>(trustedCertificates);
        c.retainAll(archiveCertificates);
        return ! c.isEmpty();
    }

    /**
     * Establishes an order for verificication result strings. Only the values
     * <ul>
     * <li>{@code null}</li>
     * <li>{@link #UNSIGNED}</li>
     * <li>{@link #SIGNATURE_UNVERIFIED}</li>
     * <li>{@link #SIGNATURE_VERIFIED}</li>
     * <li>{@link #TRUSTED}</li>
     * </ul>
     */
    public static final Comparator<String> VERIFICATION_RESULT_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            int i1 = mapVerificationResultToInt(o1);
            int i2 = mapVerificationResultToInt(o2);
            return i1 - i2;
        }
    };

    private static int mapVerificationResultToInt(String input) {
        if(input == null) {
            return 0;
        }
        switch(input) {
            case UNSIGNED: return 1;
            case SIGNATURE_UNVERIFIED: return 2;
            case SIGNATURE_VERIFIED: return 3;
            case TRUSTED: return 4;
            default: throw new IllegalArgumentException("Unmappable value: " + input);
        }
    }

    public static Collection<Certificate> getCertificates (KeyStore keyStore) throws KeyStoreException {
        Set<Certificate> certs = new HashSet<> ();
        for (String alias: Collections.list (keyStore.aliases ())) {
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            if (certificateChain != null) {
                certs.addAll(Arrays.asList(certificateChain));
            }
            certs.add(keyStore.getCertificate(alias));
        }
        return certs;
    }

    public static Collection<TrustAnchor> getTrustAnchor (KeyStore keyStore) throws KeyStoreException {
        Set<TrustAnchor> certs = new HashSet<> ();
        for (String alias: Collections.list (keyStore.aliases ())) {
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            if (certificateChain != null) {
                for(Certificate cert: certificateChain) {
                    if(cert instanceof X509Certificate) {
                        certs.add(new TrustAnchor((X509Certificate) cert, null));
                    }
                }
            }
            Certificate aliasCert = keyStore.getCertificate(alias);
            if(aliasCert instanceof X509Certificate) {
                certs.add(new TrustAnchor((X509Certificate) aliasCert, null));
            }
        }
        return certs;
    }
    /**
     * Get entries packed with pack200 in given NBM file.
     *
     * @param nbmFile the file to scan
     * @return names of entries 
     * @throws IOException in case of I/O error
     */
    static List<String> getNbmPack200Entries(File nbmFile) throws IOException {
        List<String> pack200Entries = new ArrayList<>();
        try (JarFile jf = new JarFile(nbmFile)) {
            for (JarEntry entry : Collections.list(jf.entries())) {
                if (entry.getName().endsWith(".pack.gz")) {
                    pack200Entries.add(entry.getName());
                }
            }
        }
        return pack200Entries;
    }

    /**
     * Get the certpaths that were used to sign the NBM content.
     *
     * @param nbmFile file of nbm
     * @return collection of CodeSigners, that were used to sign the non-signature
     * entries of the NBM
     * @throws IOException
     * @throws SecurityException if JAR was tampered with or if the certificate
     *         chains are not consistent
     */
    public static Collection<CodeSigner> getNbmCertificates (File nbmFile) throws IOException, SecurityException {
        Set<CodeSigner> certs = null;

        // Empty means only the MANIFEST.MF is present - special cased to be in
        // line with established behaviour
        boolean empty = true;

        // The idea:
        // - iterate over all JAR entries
        // - read each entry (as required by the JarFile specificiation for verification)
        // - extract the certificate paths, that were used to sign the
        //   entry
        // - compare it with the previously read entries. If they are not
        //   identical, raise a SecurityException.
        //
        // Excluded from the above algorithm are:
        // - directory entries
        // - files that are part of the signature (each entry in META-INF, that
        //   ends with .SF, .DSA, .RSA or .EC
        try (JarFile jf = new JarFile(nbmFile)) {
            for (JarEntry entry : Collections.list(jf.entries())) {
                verifyEntry(jf, entry);
                if ((! entry.isDirectory()) && (! isSignatureEntry(entry))) {
                    if(! entry.getName().equals("META-INF/MANIFEST.MF")) {
                        empty = false;
                    }
                    Set<CodeSigner> entryCerts = new HashSet<>();
                    CodeSigner[] codeSigners = entry.getCodeSigners();
                    if (codeSigners != null) {
                        for (CodeSigner cs : entry.getCodeSigners()) {
                            entryCerts.add(cs);
                        }
                    }
                    if(certs == null) {
                        certs = entryCerts;
                    } else if (! certs.equals(entryCerts)) {
                        throw new SecurityException("Inconsistent certificate paths used for signing");
                    }
                }
            }
        }

        return empty ? null : certs;
    }

    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("META-INF/([^/]+)\\.(SF|DSA|RSA|EC)");
    private static boolean isSignatureEntry(JarEntry je) {
	return SIGNATURE_PATTERN.matcher(je.getName()).matches();
    }

    /**
     * @throws SecurityException
     */
    @SuppressWarnings("empty-statement")
    private static void verifyEntry (JarFile jf, JarEntry je) throws IOException, SecurityException {
        InputStream is = null;
        try {
            is = jf.getInputStream (je);
            byte[] buffer = new byte[8192];
            while ((is.read (buffer, 0, buffer.length)) != -1);
        } finally {
            if (is != null) is.close ();
        }
    }
    
    private static class KeyStoreProviderListener implements LookupListener {
        private KeyStoreProviderListener () {
        }
        
        @Override
        public void resultChanged (LookupEvent ev) {
            keystoreCache.clear();
        }
    }
    
    private static final String ATTR_NAME = "name"; // NOI18N
    private static final String ATTR_SPEC_VERSION = "specification_version"; // NOI18N
    private static final String ATTR_SIZE = "size"; // NOI18N
    private static final String ATTR_NBM_NAME = "nbm_name"; // NOI18N
    
    private static File getInstallLater(File root) {
        File file = new File(root.getPath() + FILE_SEPARATOR + DOWNLOAD_DIR + FILE_SEPARATOR + ModuleUpdater.LATER_FILE_NAME);
        return file;
    }

    public static void deleteAllDoLater() {
        List<File> clusters = UpdateTracking.clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            for (File doLater : findDoLater (cluster)) {
                doLater.delete ();
            }
        }                                
    }
    
    private static Collection<File> findDoLater (File cluster) {
        if (! cluster.exists ()) {
            return Collections.emptySet ();
        } else {
            Collection<File> res = new HashSet<File> ();
            if (getInstallLater (cluster).exists ()) {
                res.add (getInstallLater (cluster));
            }
            if (ModuleDeactivator.getDeactivateLater (cluster).exists ()) {
                res.add (ModuleDeactivator.getDeactivateLater (cluster));
            }
            return res;
        }
    }
    
    public static void writeInstallLater (Map<UpdateElementImpl, File> updates) {
        // loop for all clusters and write if needed
        List<File> clusters = UpdateTracking.clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            writeInstallLaterToCluster (cluster, updates);
        }
    }
    
    private static void writeInstallLaterToCluster (File cluster, Map<UpdateElementImpl, File> updates) {
        Document document = XMLUtil.createDocument(UpdateTracking.ELEMENT_MODULES, null, null, null);                
        
        Element root = document.getDocumentElement();

        if (updates.isEmpty ()) {
            return ;
        }
        
        boolean isEmpty = true;
        for (Map.Entry<UpdateElementImpl, File> entry : updates.entrySet ()) {
            UpdateElementImpl elementImpl = entry.getKey();
            File c = entry.getValue();
            // pass this module to given cluster ?
            if (cluster.equals (c)) {
                Element module = document.createElement(UpdateTracking.ELEMENT_MODULE);
                module.setAttribute(UpdateTracking.ATTR_CODENAMEBASE, elementImpl.getCodeName());
                module.setAttribute(ATTR_NAME, elementImpl.getDisplayName());
                module.setAttribute(ATTR_SPEC_VERSION, elementImpl.getSpecificationVersion().toString());
                module.setAttribute(ATTR_SIZE, Long.toString(elementImpl.getDownloadSize()));
                module.setAttribute(ATTR_NBM_NAME, InstallSupportImpl.getDestination(cluster, elementImpl.getCodeName(), elementImpl.getInstallInfo().getDistribution()).getName());

                root.appendChild( module );
                isEmpty = false;
            }
        }
        
        if (isEmpty) {
            return ;
        }
        
        writeXMLDocumentToFile (document, getInstallLater (cluster));
    }
    
    private static void writeXMLDocumentToFile (Document doc, File dest) {
        doc.getDocumentElement ().normalize ();

        dest.getParentFile ().mkdirs ();
        InputStream is = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        OutputStream fos = null;
        try {
            try {
                XMLUtil.write (doc, bos, "UTF-8"); // NOI18N
                bos.close ();
                fos = new FileOutputStream (dest);
                is = new ByteArrayInputStream (bos.toByteArray ());
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) {
                    is.close ();
                }
                if (fos != null) {
                    fos.close ();
                }
                bos.close ();
            }
        } catch (java.io.FileNotFoundException fnfe) {
            Exceptions.printStackTrace (fnfe);
        } catch (java.io.IOException ioe) {
            Exceptions.printStackTrace (ioe);
        } finally {
            try {
                bos.close ();
            } catch (IOException x) {
                Exceptions.printStackTrace (x);
            }
        }
    }

    public static void writeDeactivateLater (Collection<File> files) {
        File userdir = InstallManager.getUserDir ();
        assert userdir != null && userdir.exists (): "Userdir " + userdir + " found and exists."; // NOI18N
        writeMarkedFilesToFile (files, ModuleDeactivator.getDeactivateLater (userdir));
    }
    
    public static void writeFileMarkedForDelete (Collection<File> files) {
        writeMarkedFilesToFile (files, ModuleDeactivator.getControlFileForMarkedForDelete (InstallManager.getUserDir ()));
    }
    
    public static void writeFileMarkedForDisable (Collection<File> files) {
        writeMarkedFilesToFile (files, ModuleDeactivator.getControlFileForMarkedForEnableDisable(InstallManager.getUserDir (), false));
    }

    public static void writeFileMarkedForEnable (Collection<File> files) {
        writeMarkedFilesToFile (files, ModuleDeactivator.getControlFileForMarkedForEnableDisable(InstallManager.getUserDir (), true));
    }

    private static void writeMarkedFilesToFile (Collection<File> files, File dest) {
        // don't forget for content written before
        StringBuilder content = new StringBuilder();
        if (dest.exists ()) {
            content.append(ModuleDeactivator.readStringFromFile (dest));
        }
        
        for (File f : files) {
            content.append(f.getAbsolutePath ());
            content.append(UpdateTracking.PATH_SEPARATOR);
        }
        
        if (content.length () == 0) {
            return ;
        }
        
        dest.getParentFile ().mkdirs ();
        assert dest.getParentFile ().exists () && dest.getParentFile ().isDirectory () : "Parent of " + dest + " exists and is directory.";
        InputStream is = null;
        OutputStream fos = null;            
        
        try {
            try {
                fos = new FileOutputStream (dest);
                is = new ByteArrayInputStream (content.toString().getBytes());
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) is.close();
                if (fos != null) fos.close();
            }                
        } catch (java.io.FileNotFoundException fnfe) {
            Exceptions.printStackTrace(fnfe);
        } catch (java.io.IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

    }
    
    public static void writeAdditionalInformation (Map<UpdateElementImpl, File> updates) {
        // loop for all clusters and write if needed
        List<File> clusters = UpdateTracking.clusters (true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            writeAdditionalInformationToCluster (cluster, updates);
        }
    }
    
    public static File locateUpdateTracking (ModuleInfo m) {
        String fileNameToFind = UpdateTracking.TRACKING_FILE_NAME + '/' + m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        return InstalledFileLocator.getDefault ().locate (fileNameToFind, m.getCodeNameBase (), false);
    }
    
    public static String readSourceFromUpdateTracking (ModuleInfo m) {
        String res = null;
        File ut = locateUpdateTracking (m);
        if (ut != null) {
            Node n = getModuleConfiguration (ut);
            if (n != null) {
                Node attrOrigin = n.getAttributes ().getNamedItem (UpdateTracking.ATTR_ORIGIN);
                assert attrOrigin != null : "ELEMENT_VERSION must contain ATTR_ORIGIN attribute.";
                if (! (UpdateTracking.UPDATER_ORIGIN.equals (attrOrigin.getNodeValue ()) ||
                        UpdateTracking.INSTALLER_ORIGIN.equals (attrOrigin.getNodeValue ()))) {
                    // ignore default value
                    res = attrOrigin.getNodeValue ();
                }
            }
        }
        return res;
    }
    
    public static Date readInstallTimeFromUpdateTracking (ModuleInfo m) {
        Date res = null;
        String time = null;
        File ut = locateUpdateTracking (m);
        if (ut != null) {
            Node n = getModuleConfiguration (ut);
            if (n != null) {
                Node attrInstallTime = n.getAttributes ().getNamedItem (UpdateTracking.ATTR_INSTALL);
                assert attrInstallTime != null : "ELEMENT_VERSION must contain ATTR_INSTALL attribute.";
                time = attrInstallTime.getNodeValue ();
            }
        }
        if (time != null) {
            try {
                long lTime = Long.parseLong (time);
                res = new Date (lTime);
            } catch (NumberFormatException nfe) {
                getLogger ().log (Level.INFO, nfe.getMessage (), nfe);
            }
        }
        return res;
    }
    
    static void writeUpdateOfUpdaterJar (JarEntry updaterJarEntry, File zipFileWithUpdater, File targetCluster) throws IOException {
        JarFile jf = new JarFile(zipFileWithUpdater);
        String entryPath = updaterJarEntry.getName();
        String entryName = entryPath.contains("/") ? entryPath.substring(entryPath.lastIndexOf("/") + 1) : entryPath;
        File dest = new File (targetCluster, UpdaterDispatcher.UPDATE_DIR + // updater
                                UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.NEW_UPDATER_DIR + // new_updater
                                UpdateTracking.FILE_SEPARATOR + entryName
                                );
        
        dest.getParentFile ().mkdirs ();
        assert dest.getParentFile ().exists () && dest.getParentFile ().isDirectory () : "Parent of " + dest + " exists and is directory.";
        InputStream is = null;
        OutputStream fos = null;            
        
        try {
            try {
                fos = new FileOutputStream (dest);
                is = jf.getInputStream (updaterJarEntry);
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) is.close();
                if (fos != null) fos.close();
                jf.close();
            }                
        } catch (java.io.FileNotFoundException fnfe) {
            getLogger ().log (Level.SEVERE, fnfe.getLocalizedMessage (), fnfe);
        } catch (java.io.IOException ioe) {
            getLogger ().log (Level.SEVERE, ioe.getLocalizedMessage (), ioe);
        }
    }
    
    static void cleanUpdateOfUpdaterJar () {
        // loop for all clusters and clean if needed
        List<File> clusters = UpdateTracking.clusters (true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            File updaterDir = new File (cluster, UpdaterDispatcher.UPDATE_DIR + UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.NEW_UPDATER_DIR);
            if (updaterDir.exists () && updaterDir.isDirectory ()) {
                for (File f : updaterDir.listFiles ()) {
                    f.delete ();
                }
                updaterDir.delete ();
            }
        }
    }

    static Module toModule(UpdateUnit uUnit) {
        return getModuleInstance(uUnit.getCodeName(), null); // XXX
    }
    
    public static Module toModule(String codeNameBase) {
        ModuleInfo mi = ModuleCache.getInstance().find(codeNameBase);
        if (mi instanceof Module) {
            return (Module)mi;
        }
        return null;
    }
    
    public static Module toModule(String codeNameBase, SpecificationVersion specificationVersion) {
        return getModuleInstance(codeNameBase, specificationVersion);
    }
    
    public static Module toModule (ModuleInfo info) {
        Module m = getModuleInstance (info.getCodeNameBase(), info.getSpecificationVersion ());
        if (m == null && info instanceof Module) {
            m = (Module)info;
        }
        return m;
    }
    
    public static boolean isFixed (ModuleInfo info) {
        Module m = toModule (info);
        assert ! info.isEnabled () || m != null : "Module found for enabled " + info;
        return m == null ? false : m.isFixed ();
    }
    
    public static boolean isValid (ModuleInfo info) {
        Module m = toModule (info);
        assert ! info.isEnabled () || m != null : "Module found for enabled " + info;
        return m == null ? false : m.isValid ();
    }
    
    static UpdateUnit toUpdateUnit(Module m) {
        return UpdateManagerImpl.getInstance().getUpdateUnit(m.getCodeNameBase());
    }
    
    static UpdateUnit toUpdateUnit(String codeNameBase) {
        return UpdateManagerImpl.getInstance().getUpdateUnit(codeNameBase);
    }
    
    public static Set<UpdateElement> findRequiredUpdateElements (UpdateElement element,
            Collection<ModuleInfo> infos,
            Set<Dependency> brokenDependencies,
            boolean topAggressive,
            Collection<UpdateElement> recommended) {

        Set<UpdateElement> retval = new HashSet<UpdateElement> ();
        switch (element.getUpdateUnit().getType ()) {
        case KIT_MODULE :
        case MODULE :
            boolean avoidRecommended = recommended != null && ! recommended.isEmpty();
            ModuleUpdateElementImpl el = (ModuleUpdateElementImpl) Trampoline.API.impl(element);
            Set<Dependency> deps = new HashSet<Dependency> (el.getModuleInfo ().getDependencies ());
            Set<ModuleInfo> availableInfos = new HashSet<ModuleInfo> (infos);
            
            maybeAddImplicitHostDependency(element, deps);
            
            int max_counter = el.getType().equals(UpdateManager.TYPE.KIT_MODULE) ? 2 : 1;
            int counter = max_counter;
            boolean aggressive = topAggressive && counter > 0;

            Set<Dependency> all = new HashSet<Dependency>();
            for (;;) {
                Set<Dependency> newones = processDependencies(deps, retval, availableInfos, brokenDependencies, element, aggressive, recommended, avoidRecommended);
                newones.removeAll(all);
               
                //issue #247884 Autoupdate should force restart when a new module enables module which is a fragment of other module
                for (Dependency dep : deps) {
                    UpdateUnit uu = toUpdateUnit(dep.getName());
                    if (uu != null && uu.getInstalled() != null) {
                        ModuleUpdateElementImpl em = (ModuleUpdateElementImpl) Trampoline.API.impl(uu.getInstalled());
                        // fragments which are installed, but not enabled yet, and have live host module will cause
                        // restart
                        if (em.getInstallInfo().getUpdateItemImpl().isFragment() && !uu.getInstalled().isEnabled()) {
                            String fh = em.getInstallInfo().getUpdateItemImpl().getFragmentHost();
                            Module m = Utilities.toModule(fh);
                            if (m != null && m.isEnabled()) {
                                el.getInstallInfo().getUpdateItemImpl().setNeedsRestart(true);
                            }
                        }
                    }
                }
                
                if (newones.isEmpty()) {
                    break;
                }
                all.addAll(newones);
                deps = newones;
            }

            Set<Dependency> moreBroken = new HashSet<Dependency> ();
            Set<ModuleInfo> tmp = new HashSet<ModuleInfo> (availableInfos);

            Set<UpdateElement> more;
            
            counter = max_counter;
            aggressive = topAggressive && counter > 0;
            while (retval.addAll (more = handleBackwardCompatability (tmp, moreBroken, aggressive))) {
                if (! moreBroken.isEmpty ()) {
                    brokenDependencies.addAll (moreBroken);
                    break;
                }
                for (UpdateElement e : more) {
                    //infos.addAll (Trampoline.API.impl (el).getModuleInfos ());
                    tmp.add (((ModuleUpdateElementImpl) Trampoline.API.impl (e)).getModuleInfo ());
                }
                aggressive = aggressive && (counter--) > 0;
            }
            if (! moreBroken.isEmpty ()) {
                brokenDependencies.addAll (moreBroken);
            }

            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl feature = (FeatureUpdateElementImpl) Trampoline.API.impl(element);
            aggressive = topAggressive;
            for (ModuleUpdateElementImpl module : feature.getContainedModuleElements ()) {
                retval.addAll (findRequiredUpdateElements (module.getUpdateElement (), infos, brokenDependencies, aggressive, recommended));                
            }
            break;
        case CUSTOM_HANDLED_COMPONENT :
            getLogger ().log (Level.INFO, "CUSTOM_HANDLED_COMPONENT doesn't care about required elements."); // XXX
            break;
        default:
            assert false : "Not implement for type " + element.getUpdateUnit() + " of UpdateElement " + element;
        }
        return retval;
    }
    
    private static Reference<Map<ModuleInfo, Set<UpdateElement>>> cachedInfo2RequestedReference = null;            
    
    private static Set<UpdateElement> handleBackwardCompatability4ModuleInfo (ModuleInfo mi, Set<ModuleInfo> forInstall, Set<Dependency> brokenDependencies, boolean aggressive) {
        if (cachedInfo2RequestedReference != null && cachedInfo2RequestedReference.get() != null) {
            Set<UpdateElement> requested = cachedInfo2RequestedReference.get().get(mi);
            if (requested != null) {
                return requested;
            }
        }
        UpdateUnit u = UpdateManagerImpl.getInstance().getUpdateUnit(mi.getCodeNameBase());
        Set<UpdateElement> moreRequested = new HashSet<UpdateElement>();
        // invalid codenamebase (in unit tests)
        if (u == null) {
            return moreRequested;
        }
        // not installed, not need to handle backward compatability
        UpdateElement i = u.getInstalled();
        if (i == null) {
            return moreRequested;
        }

        // Dependency.TYPE_MODULE
        Collection<Dependency> dependencies = new HashSet<Dependency>();
        dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, mi.getCodeName()));

        SortedSet<String> newTokens = new TreeSet<String>(Arrays.asList(mi.getProvides()));
        SortedSet<String> oldTokens = new TreeSet<String>(Arrays.asList(((ModuleUpdateElementImpl) Trampoline.API.impl(i)).getModuleInfo().getProvides()));
        oldTokens.removeAll(newTokens);
        // handle diff
        for (String tok : oldTokens) {
            // don't care about provider of platform dependency here
            if (tok.startsWith("org.openide.modules.os") || tok.startsWith("org.openide.modules.jre")) { // NOI18N
                continue;
            }
            dependencies.addAll(Dependency.create(Dependency.TYPE_REQUIRES, tok));
            dependencies.addAll(Dependency.create(Dependency.TYPE_NEEDS, tok));
        }

        for (Dependency d : dependencies) {
            DependencyAggregator deco = DependencyAggregator.getAggregator(d);
            int type = d.getType();
            String name = d.getName();
            Collection<ModuleInfo> dependings = deco.getDependening();
            synchronized (dependings) {
                for (ModuleInfo depMI : dependings) {
                    Module depM = getModuleInstance(depMI.getCodeNameBase(), depMI.getSpecificationVersion());
                    if (depM == null) {
                        continue;
                    }
                    if (!depM.getProblems().isEmpty()) {
                        // skip this module because it has own problems already
                        continue;
                    }
                    for (Dependency toTry : depM.getDependencies()) {
                        // check only relevant deps
                        if (type == toTry.getType() && name.equals(toTry.getName())
                                && !DependencyChecker.checkDependencyModule(toTry, mi)) {
                            UpdateUnit tryUU = UpdateManagerImpl.getInstance().getUpdateUnit(depM.getCodeNameBase());
                            if (!tryUU.getAvailableUpdates().isEmpty()) {
                                UpdateElement tryUE = tryUU.getAvailableUpdates().get(0);

                                ModuleInfo tryUpdated = ((ModuleUpdateElementImpl) Trampoline.API.impl(tryUE)).getModuleInfo();
                                Set<Dependency> deps = new HashSet<Dependency>(tryUpdated.getDependencies());
                                Set<ModuleInfo> availableInfos = new HashSet<ModuleInfo>(forInstall);
                                Set<Dependency> newones;
                                maybeAddImplicitHostDependency(tryUE, deps);
                                while (!(newones = processDependencies(deps, moreRequested, availableInfos, brokenDependencies, tryUE, aggressive, null, false)).isEmpty()) {
                                    deps = newones;
                                }
                                moreRequested.add(tryUE);
                            }
                        }
                    }
                }
            }
        }

        if (cachedInfo2RequestedReference == null || cachedInfo2RequestedReference.get() == null) {
            cachedInfo2RequestedReference = new WeakReference<Map<ModuleInfo, Set<UpdateElement>>>
                    (new HashMap<ModuleInfo, Set<UpdateElement>>());
        }
        cachedInfo2RequestedReference.get().put(mi, moreRequested);

        return moreRequested;
    }
    
    
    private static Reference<Set<ModuleInfo>> cachedInfosReference = null;            
    private static Reference<Set<UpdateElement>> cachedResultReference = null;
    
    private static Set<UpdateElement> handleBackwardCompatability (Set<ModuleInfo> forInstall, Set<Dependency> brokenDependencies, boolean aggressive) {
        if (cachedInfosReference != null) {
            Set<ModuleInfo> cir = cachedInfosReference.get();
            if (cir != null && cir.equals(forInstall) ) {
                if (cachedResultReference != null) {
                    Set<UpdateElement> crr = cachedResultReference.get();
                    if (crr != null) {
                        return crr;
                    }
                }
            }
        }
        cachedInfosReference = new WeakReference<Set<ModuleInfo>>(forInstall);
        err.finest("calling handleBackwardCompatability(size: " + forInstall.size() + ")");
        
        Set<UpdateElement> moreRequested = new HashSet<UpdateElement> ();
        // backward compatibility
        for (ModuleInfo mi : forInstall) {
            moreRequested.addAll(handleBackwardCompatability4ModuleInfo(mi, forInstall, brokenDependencies, aggressive));
        }
        cachedResultReference = new WeakReference<Set<UpdateElement>>(moreRequested);

        return moreRequested;
    }
    
    private static boolean maybeAddImplicitHostDependency(UpdateElement el, Set<Dependency> deps) {
        
        // check fragment, add implicit dependencies
        UpdateElementImpl elImpl = Trampoline.API.impl(el);
        UpdateItemImpl uiImpl = elImpl.getInstallInfo().getUpdateItemImpl();
        if (!uiImpl.isFragment()) {
            return false;
        }
        String fhost = uiImpl.getFragmentHost();
        Collection<Dependency> hostDep = Dependency.create(Dependency.TYPE_MODULE, fhost);
        err.fine(hostDep.toString());
        return deps.addAll(hostDep);
    }

    private static Set<Dependency> processDependencies (final Set<Dependency> original,
            Set<UpdateElement> retval,
            Set<ModuleInfo> availableInfos,
            Set<Dependency> brokenDependencies,
            UpdateElement el,
            boolean agressive,
            Collection<UpdateElement> recommended,
            boolean avoidRecommended) {
        Set<Dependency> res = new HashSet<Dependency> ();
        AutomaticDependencies.Report rep = AutomaticDependencies.getDefault().refineDependenciesAndReport(el.getCodeName(), original);
        if (rep.isModified()) {
            err.fine(rep.toString());
        }
        for (Dependency dep : original) {
            if (Dependency.TYPE_RECOMMENDS == dep.getType() && avoidRecommended) {
                continue;
            }
            Collection<UpdateElement> requestedElements = handleDependency (el, dep, availableInfos, brokenDependencies, agressive);
            if (requestedElements != null) {
                if (Dependency.TYPE_RECOMMENDS == dep.getType()) {
                    if (recommended != null) {
                        recommended.addAll(requestedElements);
                    }
                }
                for (UpdateElement req : requestedElements) {
                    ModuleUpdateElementImpl reqM = (ModuleUpdateElementImpl) Trampoline.API.impl (req);
                    availableInfos.add (reqM.getModuleInfo ());
                    retval.add (req);
                    res.addAll (reqM.getModuleInfo ().getDependencies ());
                    maybeAddImplicitHostDependency(req, res);
                }
            }
        }
        res.removeAll (original);
        return res;
    }
    
    public static Collection<UpdateElement> handleDependency (UpdateElement el,
            Dependency dep,
            Collection<ModuleInfo> availableInfos,
            Set<Dependency> brokenDependencies,
            boolean beAggressive) {
        
        Collection<UpdateElement> requested = new HashSet<UpdateElement> ();
        
        switch (dep.getType ()) {
            case Dependency.TYPE_JAVA :
                if (! DependencyChecker.matchDependencyJava (dep)) {
                    brokenDependencies.add (dep);
                }
                break;
            case Dependency.TYPE_PACKAGE :
                if (! DependencyChecker.matchPackageDependency (dep)) {
                    brokenDependencies.add (dep);
                }
                break;
            case Dependency.TYPE_MODULE :
                Collection<UpdateUnit> reqUnits = DependencyAggregator.getRequested (dep);
                assert reqUnits == null || reqUnits.isEmpty() || reqUnits.size() == 1 : dep + " returns null, empty or only once module, but returns " + reqUnits;
                boolean matched = false;
                UpdateUnit u = reqUnits == null || reqUnits.isEmpty() ? null : reqUnits.iterator().next();
                if (u != null) {
                    boolean aggressive = beAggressive;
                    if (aggressive && (isFirstClassModule(el) || u.getType() ==  UpdateManager.TYPE.KIT_MODULE)) {
                        aggressive = false;
                    }
                    // follow aggressive updates strategy
                    // if new module update is available, promote it even though installed one suites all the dependendencies
                    if (u.getInstalled() != null) {
                        UpdateElementImpl reqElImpl = Trampoline.API.impl(u.getInstalled());
                        matched = DependencyChecker.checkDependencyModule(dep, ((ModuleUpdateElementImpl) reqElImpl).getModuleInfo());
                    }

                    if (!matched) {
                        for (ModuleInfo m : availableInfos) {
                            if (DependencyChecker.checkDependencyModule(dep, m)) {
                                matched = true;
                                break;
                            }
                        }
                    }
                    if (aggressive || !matched) {
                        UpdateElement reqEl = u.getAvailableUpdates().isEmpty() ? null : u.getAvailableUpdates().get(0);
                        if (reqEl != null) {
                            UpdateElementImpl reqElImpl = Trampoline.API.impl(reqEl);
                            ModuleUpdateElementImpl reqModuleImpl = (ModuleUpdateElementImpl) reqElImpl;
                            ModuleInfo info = reqModuleImpl.getModuleInfo();
                            if (DependencyChecker.checkDependencyModule(dep, info)) {
                                if (!availableInfos.contains(info)) {
                                    requested.add(reqEl);
                                    matched = true;
                                }
                            }
                        }
                    }
                } else {
                    for (ModuleInfo m : availableInfos) {
                        if (DependencyChecker.checkDependencyModule(dep, m)) {
                            matched = true;
                            break;
                        }
                    }
                }

                if (!matched) {
                    brokenDependencies.add(dep);
                }

                break;
            case Dependency.TYPE_REQUIRES :
            case Dependency.TYPE_NEEDS :
            case Dependency.TYPE_RECOMMENDS :
                if (DummyModuleInfo.TOKEN_MODULE_FORMAT1.equals (dep.getName ()) ||
                        DummyModuleInfo.TOKEN_MODULE_FORMAT2.equals (dep.getName ())) {
                    // these tokens you can ignore here
                    break;
                }
                Collection<UpdateUnit> requestedUnits = DependencyAggregator.getRequested (dep);
                boolean passed = false;
                if (requestedUnits == null || requestedUnits.isEmpty()) {
                    // look on availableInfos as well
                    for (ModuleInfo m : availableInfos) {
                        if (Arrays.asList (m.getProvides ()).contains (dep.getName ())) {
                            passed = true;
                            break;
                        }
                    }
                } else {
                    passed = true;
                    for (UpdateUnit uu : requestedUnits) {
                        if (! uu.getAvailableUpdates ().isEmpty ()) {
                            requested.add(uu.getAvailableUpdates ().get (0));
                        }
                    }
                }
                if (! passed && Dependency.TYPE_RECOMMENDS != dep.getType ()) {
                    brokenDependencies.add (dep);
                }
                break;
        }
        return requested;
    }
    
    static Set<String> getBrokenDependencies (UpdateElement element, List<ModuleInfo> infos) {
        assert element != null : "UpdateElement cannot be null";
        Set<Dependency> brokenDependencies = new HashSet<Dependency> ();
        // create init collection of brokenDependencies
        Utilities.findRequiredUpdateElements (element, infos, brokenDependencies, false, new HashSet<UpdateElement>());
        // backward compatibility
        for (ModuleInfo mi : infos) {
            UpdateUnit u = UpdateManagerImpl.getInstance ().getUpdateUnit (mi.getCodeNameBase ());
            // invalid codenamebase (in unit tests)
            if (u == null) {
                continue;
            }
            // not installed, not need to handle backward compatability
            UpdateElement i = u.getInstalled ();
            if (i == null) {
                continue;
            }
            // maybe newer version is processed as a update
            if (! u.getAvailableUpdates ().isEmpty ()) {
                UpdateElement ue = u.getAvailableUpdates ().get (0);
                ModuleInfo newerMI = ((ModuleUpdateElementImpl) Trampoline.API.impl (ue)).getModuleInfo ();
                if (infos.contains (newerMI)) {
                    continue;
                }
            }
            // Dependency.TYPE_MODULE
            for (Dependency d : Dependency.create (Dependency.TYPE_MODULE, mi.getCodeName ())) {
                DependencyAggregator deco = DependencyAggregator.getAggregator (d);
                Collection<ModuleInfo> dependings = deco.getDependening();
                synchronized (dependings) {
                    for (ModuleInfo depMI : dependings) {
                        //Module depM = Utilities.toModule (depMI);
                        Module depM = getModuleInstance(depMI.getCodeNameBase(), depMI.getSpecificationVersion());
                        if (depM == null) {
                            continue;
                        }
                        if (! depM.getProblems ().isEmpty ()) {
                            // skip this module because it has own problems already
                            continue;
                        }
                        for (Dependency toTry : depM.getDependencies ()) {
                            // check only relevant deps
                            if (deco.equals (DependencyAggregator.getAggregator (toTry)) &&
                                    ! DependencyChecker.checkDependencyModule (toTry, mi)) {
                                brokenDependencies.add (toTry);
                            }
                        }
                    }
                }
            }
            // Dependency.TYPE_REQUIRES
            // Dependency.TYPE_NEEDS
            SortedSet<String> newTokens = new TreeSet<String> (Arrays.asList (mi.getProvides ()));
            SortedSet<String> oldTokens = new TreeSet<String> (Arrays.asList (((ModuleUpdateElementImpl) Trampoline.API.impl (i)).getModuleInfo ().getProvides ()));
            oldTokens.removeAll (newTokens);
            // handle diff
            for (String tok : oldTokens) {
                Collection<Dependency> deps = new HashSet<Dependency> (Dependency.create (Dependency.TYPE_REQUIRES, tok));
                deps.addAll (Dependency.create (Dependency.TYPE_NEEDS, tok));
                for (Dependency d : deps) {
                    DependencyAggregator deco = DependencyAggregator.getAggregator (d);
                    Collection<ModuleInfo> dependings = deco.getDependening();
                    synchronized (dependings) {
                        for (ModuleInfo depMI : dependings) {
                            //Module depM = Utilities.toModule (depMI);
                            Module depM = getModuleInstance(depMI.getCodeNameBase(), depMI.getSpecificationVersion());
                            if (depM == null) {
                                continue;
                            }
                            if (! depM.getProblems ().isEmpty ()) {
                                // skip this module because it has own problems already
                                continue;
                            }
                            for (Dependency toTry : depM.getDependencies ()) {
                                // check only relevant deps
                                if (deco.equals (DependencyAggregator.getAggregator (toTry))) {
                                    brokenDependencies.add (toTry);
                                }
                            }
                        }
                    }
                }
            }
        }
        Set<String> retval = new HashSet<String> (brokenDependencies.size ());
        for (Dependency dep : brokenDependencies) {
            retval.add (dep.toString ());
        }
        return retval;
    }
    
    static Set<String> getBrokenDependenciesInInstalledModules (UpdateElement element) {
        assert element != null : "UpdateElement cannot be null";
        Set<Dependency> deps = new HashSet<Dependency> ();
        for (ModuleInfo m : getModuleInfos (Collections.singleton (element))) {
            deps.addAll (DependencyChecker.findBrokenDependenciesTransitive (m,
                    InstalledModuleProvider.getInstalledModules ().values (),
                    new HashSet<ModuleInfo> ()));
        }
        Set<String> retval = new HashSet<String> ();
        for (Dependency dep : deps) {
            retval.add (dep.toString ());
        }
        return retval;
    }
    
    private static List<ModuleInfo> getModuleInfos (Collection<UpdateElement> elements) {
        List<ModuleInfo> infos = new ArrayList<ModuleInfo> (elements.size ());
        for (UpdateElement el : elements) {
            if (el.getUpdateUnit () != null && el.getUpdateUnit ().isPending ()) {
                // cannot depend of UpdateElement in pending state
                continue;
            }
            UpdateElementImpl impl = Trampoline.API.impl (el);
            infos.addAll (impl.getModuleInfos ());
        }
        return infos;
    }
    
    private static Module getModuleInstance(String codeNameBase, SpecificationVersion specificationVersion) {
        ModuleInfo mi = ModuleCache.getInstance().find(codeNameBase);
        if (mi instanceof Module) {
            Module m = (Module)mi;
            if (specificationVersion == null) {
                err.log(Level.FINE, "no module {0} for null version", m);
                return m;
            } else {
                SpecificationVersion version = m.getSpecificationVersion();
                if (version == null) {
                    err.log(Level.FINER, "No version for {0}", m);
                    return null;
                }
                final int res = version.compareTo(specificationVersion);
                err.log(Level.FINER, "Comparing versions: {0}.compareTo({1}) = {2}", new Object[]{version, specificationVersion, res});
                return res >= 0 ? m : null;
            }
        }
        return null;
    }
    
    public static boolean isAutomaticallyEnabled(String codeNameBase) {
        Module m = getModuleInstance(codeNameBase, null);
        return m != null ? (m.isAutoload() || m.isEager() || m.isFixed()) : false;
    }
    
    public static ModuleInfo takeModuleInfo (UpdateElement el) {
        UpdateElementImpl impl = Trampoline.API.impl (el);
        assert impl instanceof ModuleUpdateElementImpl;
        return ((ModuleUpdateElementImpl) impl).getModuleInfo ();
    }
    
    private static String productVersion = null;
    
    public static String getProductVersion () {
        if (productVersion == null) {
            String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
            productVersion = NbBundle.getMessage (TopLogging.class, "currentVersion", buildNumber); // NOI18N
        }
        return productVersion;
    }
    
    private static Node getModuleConfiguration (File moduleUpdateTracking) {
        Document document;
        InputStream is;
        try {
            is = new BufferedInputStream (new FileInputStream (moduleUpdateTracking));
            InputSource xmlInputSource = new InputSource (is);
            document = XMLUtil.parse (xmlInputSource, false, false, null, org.openide.xml.EntityCatalog.getDefault ());
            is.close ();
        } catch (SAXException saxe) {
            getLogger ().log (Level.INFO, "SAXException when reading " + moduleUpdateTracking, saxe);
            return null;
        } catch (IOException ioe) {
            getLogger ().log (Level.INFO, "IOException when reading " + moduleUpdateTracking, ioe);
            return null;
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
        if (document.getDocumentElement () == null) {
            return null;
        }
        return getModuleElement (document.getDocumentElement ());
    }
    
    private static Node getModuleElement (Element element) {
        Node lastElement = null;
        assert UpdateTracking.ELEMENT_MODULE.equals (element.getTagName ()) : "The root element is: " + UpdateTracking.ELEMENT_MODULE + " but was: " + element.getTagName ();
        NodeList listModuleVersions = element.getElementsByTagName (UpdateTracking.ELEMENT_VERSION);
        for (int i = 0; i < listModuleVersions.getLength (); i++) {
            lastElement = getModuleLastVersion (listModuleVersions.item (i));
            if (lastElement != null) {
                break;
            }
        }
        return lastElement;
    }
    
    private static Node getModuleLastVersion (Node version) {
        Node attrLast = version.getAttributes ().getNamedItem (UpdateTracking.ATTR_LAST);
        assert attrLast != null : "ELEMENT_VERSION must contain ATTR_LAST attribute.";
        if (Boolean.valueOf (attrLast.getNodeValue ())) {
            return version;
        } else {
            return null;
        }
    }
    
    private static File getAdditionalInformation (File root) {
        File file = new File (root.getPath () + FILE_SEPARATOR + DOWNLOAD_DIR + 
                FILE_SEPARATOR + UpdateTracking.ADDITIONAL_INFO_FILE_NAME);
        return file;
    }

    private static void writeAdditionalInformationToCluster (File cluster, Map<UpdateElementImpl, File> updates) {
        if (updates.isEmpty ()) {
            return ;
        }
        
        Document document = XMLUtil.createDocument (UpdateTracking.ELEMENT_ADDITIONAL, null, null, null);                
        Element root = document.getDocumentElement ();
        boolean isEmpty = true;
        
        for (Map.Entry<UpdateElementImpl, File> entry : updates.entrySet ()) {
            UpdateElementImpl impl = entry.getKey ();
            File c = entry.getValue();
            // pass this module to given cluster ?
            if (cluster.equals (c)) {
                Element module = document.createElement (UpdateTracking.ELEMENT_ADDITIONAL_MODULE);
                module.setAttribute(ATTR_NBM_NAME,
                        InstallSupportImpl.getDestination (cluster, impl.getCodeName(), impl.getInstallInfo().getDistribution()).getName ());
                module.setAttribute (UpdateTracking.ATTR_ADDITIONAL_SOURCE, impl.getSource ());
                root.appendChild( module );
                isEmpty = false;
            }
        }
        
        if (isEmpty) {
            return ;
        }
        
        writeXMLDocumentToFile (document, getAdditionalInformation (cluster));
    }
    
    public static UpdateItem createUpdateItem (UpdateItemImpl impl) {
        assert Trampoline.SPI != null;
        return Trampoline.SPI.createUpdateItem (impl);
    }
    
    public static UpdateItemImpl getUpdateItemImpl (UpdateItem item) {
        assert Trampoline.SPI != null;
        return Trampoline.SPI.impl (item);
    }
    
    public static boolean canDisable (Module m) {
        return m != null &&  m.isEnabled () && ! isEssentialModule (m) && ! m.isAutoload () && ! m.isEager ();
    }
    
    public static boolean canEnable (Module m) {
        return m != null && !m.isEnabled () && ! m.isAutoload () && ! m.isEager ();
    }
    
    @SuppressWarnings("null")
    public static boolean isElementInstalled (UpdateElement el) {
        assert el != null : "Invalid call isElementInstalled with null parameter.";
        if (el == null) {
            return false;
        }
        return el.equals (el.getUpdateUnit ().getInstalled ());
    }
    
    public static boolean isKitModule (ModuleInfo mi) {
        return Main.getModuleSystem().isShowInAutoUpdateClient(mi);
    }
    
    public static boolean isEssentialModule (ModuleInfo mi) {
        Object o = mi.getAttribute (ATTR_ESSENTIAL);
        return isFixed (mi) || (o != null && Boolean.parseBoolean (o.toString ()));
    }

    public static boolean isFirstClassModule (UpdateElement ue) {
        String codeName = ue.getCodeName();
        String names = System.getProperty (PLUGIN_MANAGER_FIRST_CLASS_MODULES);
        if (names == null || names.length () == 0) {
            UpdateElementImpl ueImpl = Trampoline.API.impl(ue);
            return ueImpl.isPreferredUpdate();
        } else {
            StringTokenizer en = new StringTokenizer(names, ","); // NOI18N
            while (en.hasMoreTokens()) {
                if (en.nextToken().trim().equals(codeName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static Logger getLogger () {
        return err;
    }
    
    /** Finds modules depending on given module.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @return a set (possibly empty) of modules managed by this manager, never including m
     */
    public static Set<Module> findRequiredModules (Module m, ModuleManager mm, Map<Module, Set<Module>> m2reqs) {
        Set<Module> res;
        if (m2reqs != null) {
            res = m2reqs.get (m);
            if (res == null) {
                res = mm.getModuleInterdependencies(m, false, false, true);
                m2reqs.put (m, res);
            }
        } else {
            res = mm.getModuleInterdependencies(m, false, false, true);
        }
        return res;
    }
    
    /** Finds for modules given module depends upon.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @return a set (possibly empty) of modules managed by this manager, never including m
     */
    public static Set<Module> findDependingModules (Module m, ModuleManager mm, Map<Module, Set<Module>> m2deps) {
        Set<Module> res;
        if (m2deps != null) {
            res = m2deps.get (m);
            if (res == null) {
                res = filterDependingOnOtherProvider(m, mm.getModuleInterdependencies(m, true, false, true));
                m2deps.put (m, res);
            }
        } else {
            res = filterDependingOnOtherProvider(m, mm.getModuleInterdependencies(m, true, false, true));
        }
        return res;
    }
    
    private static Set<Module> filterDependingOnOtherProvider(Module m, Set<Module> modules) {
        Set<Module> alive = new HashSet<Module> ();
        for (String token : m.getProvides()) {
            for (Module depM : modules) {
                for (Dependency dep : depM.getDependencies()) {
                    if (dep.getType() == Dependency.TYPE_REQUIRES || dep.getType() == Dependency.TYPE_NEEDS) {
                        if (token.equals(dep.getName())) {
                            // check other installed providers
                            assert UpdateManagerImpl.getInstance().getInstalledProviders(token).contains(m) :
                                    "Provides of token " + token + " " + UpdateManagerImpl.getInstance().getInstalledProviders(token) +
                                    " contains " + m;
                            if (UpdateManagerImpl.getInstance().getInstalledProviders(token).size() > 1) {
                                alive.add(depM);
                            }
                        }
                    }
                }
            }
        }
        modules.removeAll(alive);
        return modules;
    }
    
    public static String formatDate(Date date) {
        synchronized(DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }

    public static Date parseDate(String date) throws ParseException {
        synchronized(DATE_FORMAT) {
            return DATE_FORMAT.parse(date);
        }
    }    
    
    @SuppressWarnings("null")
    public static boolean canWriteInCluster (File cluster) {
        assert cluster != null : "dir cannot be null";
        if (cluster == null) {
            return false;
        }
        if (cluster.exists () && cluster.isDirectory ()) {
            File dir4test;
            File update = new File (cluster, UPDATE_DIR);
            File download = new File (cluster, DOWNLOAD_DIR);
            if (download.exists ()) {
                dir4test = download;
            } else if (update.exists ()) {
                dir4test = update;
            } else {
                dir4test = cluster;
            }
            // workaround the bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
            if (dir4test.canWrite () && dir4test.canRead ()) {
                boolean canWrite = canWrite (dir4test);
                getLogger ().log (Level.FINE, "Can write into {0}? {1}", new Object[]{dir4test, canWrite});
                return canWrite;
            } else {
                getLogger ().log (Level.FINE, "Can write into {0}? {1}", new Object[]{dir4test, dir4test.canWrite ()});
                return dir4test.canWrite ();
            }
        }
        
        cluster.mkdirs ();
        getLogger ().log (Level.FINE, "Can write into new cluster {0}? {1}", new Object[]{cluster, cluster.canWrite ()});
        return cluster.canWrite ();
    }
    
    public static boolean canWrite (File f) {
        // workaround the bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
        if (org.openide.util.Utilities.isWindows ()) {
            if (f.isFile ()) {
                FileWriter fw = null;
                try {
                    fw = new FileWriter (f, true);
                    getLogger ().log (Level.FINE, "{0} has write permission", f);
                } catch (IOException ioe) {
                    // just check of write permission
                    getLogger ().log (Level.FINE, f + " has no write permission", ioe);
                    return false;
                } finally {
                    try {
                        if (fw != null) {
                            fw.close ();
                        }
                    } catch (IOException ex) {
                        getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                    }
                }
                return true;
            } else {
                try {
                    File dummy = File.createTempFile ("dummy", null, f);
                    dummy.delete ();
                    getLogger ().log (Level.FINE, "{0} has write permission", f);
                } catch (IOException ioe) {
                    getLogger ().log (Level.FINE, f + " has no write permission", ioe);
                    return false;
                }
                return true;
            }
        } else {
            return f.canWrite ();
        }
    }
    
    public static KeyStore loadKeyStore () {
        String fileName = getPreferences ().get (USER_KS_KEY, null);
        if (fileName == null) {
            return null;
        } else {
            InputStream is = null;
            KeyStore ks = null;
            try {
                File f = new File (getCacheDirectory (), fileName);
                if (! f.exists ()) {
                    return null;
                }
                is = new BufferedInputStream (new FileInputStream (f));
                ks = KeyStore.getInstance (KeyStore.getDefaultType ());
                ks.load (is, KS_USER_PASSWORD.toCharArray ());
            } catch (IOException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } catch (NoSuchAlgorithmException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } catch (CertificateException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } catch (KeyStoreException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } finally {
                try {
                    if (is != null) {
                        is.close ();
                    }
                } catch (IOException ex) {
                    getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                }
            }
            return ks;
        }
    }
    
    private static void storeKeyStore (KeyStore ks) {
        OutputStream os = null;
        try {
            File f = new File (getCacheDirectory (), USER_KS_FILE_NAME);
            os = new BufferedOutputStream (new FileOutputStream (f));
            ks.store (os, KS_USER_PASSWORD.toCharArray ());
            getPreferences ().put (USER_KS_KEY, USER_KS_FILE_NAME);
        } catch (KeyStoreException ex) {
            getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
        } catch (IOException ex) {
            getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
        } catch (NoSuchAlgorithmException ex) {
            getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
        } catch (CertificateException ex) {
            getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } finally {
                try {
                    if (os != null) {
                        os.close ();
                    }
                } catch (IOException ex) {
                    getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                }
            }
    }
    
    public static void addCertificates (Collection<Certificate> certs) {
        KeyStore ks = loadKeyStore ();
        if (ks == null) {
            try {
                ks = KeyStore.getInstance (KeyStore.getDefaultType ());
                ks.load (null, KS_USER_PASSWORD.toCharArray ());
            } catch (IOException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                return ;
            } catch (NoSuchAlgorithmException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                return ;
            } catch (CertificateException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                return ;
            } catch (KeyStoreException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                return ;
            }
        }

        for (Certificate c : certs) {
            
            try {
                // don't add certificate twice
                if (ks.getCertificateAlias (c) != null) {
                    continue;
                }

                // Find free alias name
                String alias = null;
                for (int i = 0; i < 9999; i++) {
                    alias = "genAlias" + i; // NOI18N
                    if (! ks.containsAlias (alias)) {
                        break;
                    }
                }
                if (alias == null) {
                    getLogger ().log (Level.INFO, "Too many certificates with {0}", c);
                }

                ks.setCertificateEntry (alias, c);
            } catch (KeyStoreException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            }
            
        }
        
        storeKeyStore (ks);
    }

    public static void writeFirstClassModule(String moduleCodeName) {
        if (moduleCodeName == null) {
            getPreferences().put(PLUGIN_MANAGER_FIRST_CLASS_MODULES, "");
            return ;
        }
        String names = getPreferences().get(PLUGIN_MANAGER_FIRST_CLASS_MODULES, "");
        names = names.isEmpty() ? moduleCodeName : names + "," + moduleCodeName;
        getPreferences().put(PLUGIN_MANAGER_FIRST_CLASS_MODULES, names);
    }

    private static File getCacheDirectory () {
        return Places.getCacheSubdirectory("catalogcache");
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate"); // NOI18N
    }    

    public static String hexEncode(byte[] input) {
        StringBuilder sb = new StringBuilder(input.length * 2);
        for(byte b: input) {
            sb.append(Character.forDigit((b & 0xF0) >> 4, 16));
            sb.append(Character.forDigit((b & 0x0F), 16));
        }
        return sb.toString();
    }

    public static byte[] hexDecode(String input) {
        int length = input.length() / 2;
        byte[] result = new byte[length];
        for(int i = 0; i < length; i++) {
            int b = Character.digit(input.charAt(i * 2), 16) << 4;
            b |= Character.digit(input.charAt(i * 2 + 1), 16);
            result[i] = (byte) (b & 0xFF);
        }
        return result;
    }
}
