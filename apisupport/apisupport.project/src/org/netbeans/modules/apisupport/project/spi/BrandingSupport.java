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

package org.netbeans.modules.apisupport.project.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Locates brandable modules.
 * @author Radek Matous
 */
public abstract class BrandingSupport {

    protected interface BrandableModule {
        String getCodeNameBase();
        File getJarLocation();
        String getRelativePath();
    }
    
    private final Project project;
    private Set<BrandableModule> brandedModules;
    private Set<BundleKey> brandedBundleKeys;
    private Set<BundleKey> localizedBrandedBundleKeys;
    private Set<BrandedFile> brandedFiles;

    private final String brandingPath;
    private final File brandingDir;
    
    private static final String BUNDLE_NAME_PREFIX = "Bundle"; //NOI18N
    private static final String BUNDLE_NAME_SUFFIX = ".properties"; //NOI18N

    protected Locale locale;
    private final Object LOCK = new Object();
    private SoftReference<Set<BrandableModule>> cacheLoaded;
    private boolean isCached;

    /**
     * @param p Project to be branded.
     * @param brandingPath Path relative to project's dir where branded resources are stored in.
     * @throws IOException
     */
    protected BrandingSupport(Project p, String brandingPath) {
        this.project = p;
        this.brandingPath = brandingPath;
        File suiteDir = FileUtil.toFile(project.getProjectDirectory());
        assert suiteDir != null && suiteDir.exists();
        brandingDir = new File(suiteDir, brandingPath);//NOI18N
    }        
    
    /**
     * @return the project directory beneath which everything in the project lies
     */
    private File getProjectDirectory() {
        return FileUtil.toFile(project.getProjectDirectory());
    }
    
    /**
     * @return the top-level branding directory
     */
    File getBrandingRoot() {
        return new File(getProjectDirectory(), brandingPath);
    }
    
    /**
     * @return the branding directory for NetBeans module represented as
     * <code>ModuleEntry</code>
     */
    File getModuleEntryDirectory(BrandableModule mEntry) {
        String relativePath;
        relativePath = mEntry.getRelativePath();
        return new File(getBrandingRoot(),relativePath);
    }
    
    /**
     * @return the file representing localizing bundle for NetBeans module
     */
    private File getLocalizingBundle(final BrandableModule mEntry) {
        ManifestManager mfm = ManifestManager.getInstanceFromJAR(mEntry.getJarLocation());
        File bundle = null;
        if (mfm != null) {
            String bundlePath = mfm.getLocalizingBundle();
            if (bundlePath != null) {
                bundle = new File(getModuleEntryDirectory(mEntry),bundlePath);
            }
        }
        return bundle;
    }
    
    boolean isBranded(final BundleKey key) {
        boolean retval = getBrandedBundleKeys().contains(key);
        return retval;
        
    }
    
    boolean isLocalizedBranded(final BundleKey key) {
        boolean retval = getLocalizedBrandedBundleKeys().contains(key);
        return retval;
        
    }
    
    boolean isBranded(final BrandedFile bFile) {
        return getBrandedFiles().contains(bFile);
    }
    
    /**
     * @return true if NetBeans module is already branded
     */
    boolean isBranded(final BrandableModule entry) {
        boolean retval = getBrandedModules().contains(entry);
        assert (retval == getModuleEntryDirectory(entry).exists());
        return retval;
    }
    
    private Set<BrandableModule> getBrandedModules() {
        return brandedModules;
    }
    
    Set<BundleKey> getBrandedBundleKeys() {
        return brandedBundleKeys;
    }
    
    Set<BundleKey> getLocalizedBrandedBundleKeys() {
        return localizedBrandedBundleKeys;
    }
    
    Set<BrandedFile> getBrandedFiles() {
        return brandedFiles;
    }
    
    Set<BundleKey> getLocalizingBundleKeys(final String moduleCodeNameBase, final Set<String> keys) {
        BrandableModule foundEntry = findBrandableModule(moduleCodeNameBase);
        return (foundEntry != null) ? getLocalizingBundleKeys(foundEntry, keys) : null;
    }
    
    Set<BundleKey> getLocalizedLocalizingBundleKeys(final String moduleCodeNameBase, final Set<String> keys) {
        BrandableModule foundEntry = findBrandableModule(moduleCodeNameBase);
        return (foundEntry != null) ? getLocalizingBundleKeys(foundEntry, keys) : null;
    }
    
    private Set<BundleKey> getLocalizingBundleKeys(final BrandableModule moduleEntry, final Set<String> keys) {
        Set<BundleKey> retval = new HashSet<BundleKey>();
        for (Iterator<BundleKey> it = getLocalizedBrandedBundleKeys().iterator();
        it.hasNext() && retval.size() != keys.size();) {
            BundleKey bKey = it.next();
            if (keys.contains(bKey.getKey())) {
                retval.add(bKey);
            }
        }
        
        if (retval.size() != keys.size()) {
            loadLocalizedBundlesFromPlatform(moduleEntry, keys, retval);
        }
        return (retval.size() != keys.size()) ? null : retval;
    }
    
    BrandedFile getBrandedFile(final String moduleCodeNameBase, final String entryPath) {
        BrandableModule foundEntry = findBrandableModule(moduleCodeNameBase);
        return (foundEntry != null) ? getBrandedFile(foundEntry,entryPath) : null;
    }
    
    private BrandedFile getBrandedFile(final BrandableModule moduleEntry, final String entryPath) {
        BrandedFile retval = null;
        try {
            retval = new BrandedFile(moduleEntry, entryPath);
            for (Iterator<BrandedFile> it = getBrandedFiles().iterator();it.hasNext() ;) {
                BrandedFile bFile = it.next();
                
                if (retval.equals(bFile)) {
                    retval = bFile;
                    
                }
            }
        } catch (MalformedURLException ex) {
            retval = null;
        }
        return retval;
    }
    
    BundleKey getBundleKey(final String moduleCodeNameBase,
            final String bundleEntry,final String key) {
        Set<BundleKey> keys = getBundleKeys(moduleCodeNameBase, bundleEntry, Collections.singleton(key), getBrandedBundleKeys());
        return (keys == null) ? null : (BrandingSupport.BundleKey) keys.toArray()[0];
    }
    
    BundleKey getLocalizedBundleKey(final String moduleCodeNameBase,
            final String bundleEntry,final String key) {
        Set<BundleKey> keys = getBundleKeys(moduleCodeNameBase, bundleEntry, Collections.singleton(key), getLocalizedBrandedBundleKeys());
        return (keys == null) ? null : (BrandingSupport.BundleKey) keys.toArray()[0];
    }
    
    Set<BundleKey> getBundleKeys(final String moduleCodeNameBase, final String bundleEntry, final Set<String> keys, Set<BundleKey> bundleKeys) {
        BrandableModule foundEntry = findBrandableModule(moduleCodeNameBase);
        return (foundEntry != null) ? getBundleKeys(foundEntry, bundleEntry, keys, bundleKeys) : null;
    }
    
    private Set<BundleKey> getBundleKeys(final BrandableModule moduleEntry, final String bundleEntry, final Set<String> keys, Set<BundleKey> bundleKeys) {
        Set<BundleKey> retval = new HashSet<BundleKey>();
        for (Iterator<BundleKey> it = bundleKeys.iterator();
        it.hasNext() && retval.size() != keys.size();) {
            BundleKey bKey = it.next();
            if (keys.contains(bKey.getKey())) {
                retval.add(bKey);
            } 
        }
        
        if (retval.size() != keys.size()) {
            try {
                loadLocalizedBundlesFromPlatform(moduleEntry, bundleEntry, keys, retval);
            } catch (IOException ex) {
                Logger.getLogger(BrandingSupport.class.getName()).log(Level.WARNING, "#211911", ex);
                return null;
            }
        }
                    
        return (retval.size() != keys.size()) ? null : retval;
    }
    
    protected abstract BrandableModule findBrandableModule(String moduleCodeNameBase);

    protected abstract Set<File> getBrandableJars();
    
    void brandFile(final BrandedFile bFile) throws IOException {
        if (!bFile.isModified()) return;

        File target = bFile.getFileLocation();
        if (!target.exists()) {
            target.getParentFile().mkdirs();
            target.createNewFile();
        }
        
        assert target.exists();
        FileObject fo = FileUtil.toFileObject(target);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = bFile.getBrandingSource().openStream();
            os = fo.getOutputStream();
            FileUtil.copy(is, os);
        } finally {
            if (is != null) {
                is.close();
            }
            
            if (os != null) {
                os.close();
            }
            
            brandedFiles.add(bFile);
            bFile.modified = false;
        }
    }
    
    void brandFile(final BrandedFile bFile, final Runnable saveTask) throws IOException {
        if (!bFile.isModified()) return;
        
        saveTask.run();
        brandedFiles.add(bFile);
        bFile.modified = false;
    }
    
    void brandBundleKey(final BundleKey bundleKey) throws IOException {
        if (bundleKey == null) {
            return;
        }
        brandBundleKeys(Collections.singleton(bundleKey));
    }
    
    void brandBundleKeys(final Set<BundleKey> bundleKeys) throws IOException {
        init();
        Map<File,EditableProperties> mentryToEditProp = new HashMap<File,EditableProperties>();
        for (BundleKey bKey : bundleKeys) {
            if (bKey.isModified()) {
                EditableProperties ep = mentryToEditProp.get(bKey.getBrandingBundle());
                if (ep == null) {
                    File bundle = bKey.getBrandingBundle();
                    if (!bundle.exists()) {
                        bundle.getParentFile().mkdirs();
                        bundle.createNewFile();
                    }
                    ep = getEditableProperties(bundle);
                    mentryToEditProp.put(bKey.getBrandingBundle(), ep);
                }
                ep.setProperty(bKey.getKey(), bKey.getValue());
            }
        }
        
        for (Map.Entry<File,EditableProperties> entry : mentryToEditProp.entrySet()) {
            File bundle = entry.getKey();
            assert bundle.exists();
            storeEditableProperties(entry.getValue(), bundle);
            for (BundleKey bKey: bundleKeys) {
                File bundle2 = bKey.getBrandingBundle();
                if (bundle2.equals(bundle)) {
                    brandedBundleKeys.add(bKey);
                    bKey.modified = false;
                    brandedModules.add(bKey.getModuleEntry());
                }
            }
        }
    }

    /** return null in case nothing has changed since last call */
    protected abstract @CheckForNull Set<BrandableModule> loadModules() throws IOException;
    
    void init() throws IOException {
        Set<BrandableModule> loaded = null;
        synchronized(LOCK) {
            isCached = false;
            loaded = cacheOrLoadModules();
        }
        if (brandedModules == null || loaded != null) {
            brandedModules = new HashSet<BrandableModule>();
            brandedBundleKeys = new HashSet<BundleKey>();
            localizedBrandedBundleKeys = new HashSet<BundleKey>();
            brandedFiles = new HashSet<BrandedFile>();
            
            if (brandingDir.exists()) {
                if(this.locale == null)
                    this.locale = Locale.getDefault();
                assert brandingDir.isDirectory();
                scanModulesInBrandingDir(brandingDir, loaded);
            }
        }
    }
    
    void refreshLocalizedBundles(Locale locale) throws IOException {
        this.locale = locale;
        Set<BrandableModule> loaded = null;
        synchronized(LOCK) {
            loaded = cacheOrLoadModules();
        }
        if (brandedModules == null || loaded != null) {
            brandedModules = new HashSet<BrandableModule>();
            brandedBundleKeys = new HashSet<BundleKey>();
            localizedBrandedBundleKeys = new HashSet<BundleKey>();
            brandedFiles = new HashSet<BrandedFile>();
            
            if (brandingDir.exists()) {
                assert brandingDir.isDirectory();
                scanModulesInBrandingDir(brandingDir, loaded);
            }
        }
        
    }
    
    private Set<BrandableModule> cacheOrLoadModules() throws IOException {
        Set<BrandableModule> loaded = null;
        if(isCached) {
           loaded = cacheLoaded != null? cacheLoaded.get():null;
        }
        if(loaded == null) {
            cacheLoaded = new SoftReference<Set<BrandableModule>>(loaded = loadModules());
            isCached = true;
        }
        return loaded;
    }
    
    private  void scanModulesInBrandingDir(final File srcDir, final Set<BrandableModule> platformModules) throws IOException  {
        if (srcDir.getName().endsWith(".jar")) {//NOI18N
            BrandableModule foundEntry = null;
            for (BrandableModule platformModule : platformModules) {
                if (isBrandingForModuleEntry(srcDir, platformModule)) {
                    scanBrandedFiles(srcDir, platformModule);
                    
                    foundEntry = platformModule;
                    break;
                }
            }
            if (foundEntry != null) {
                brandedModules.add(foundEntry);
            }
        } else {
            String[] kids = srcDir.list();
            assert (kids != null);
            
            for (String kidName : kids) {
                File kid = new File(srcDir, kidName);
                if (!kid.isDirectory()) {
                    continue;
                }
                scanModulesInBrandingDir(kid, platformModules);
            }
        }
    }
    
    private void scanBrandedFiles(final File srcDir, final BrandableModule mEntry) throws IOException {
        String[] kids = srcDir.list();
        assert (kids != null);
        boolean foundLocale = false;
        File kid = null;
        for (String kidName : kids) {
            kid = new File(srcDir, kidName);
            if (!kid.isDirectory()) {
                if (kid.getName().endsWith(BUNDLE_NAME_PREFIX + BUNDLE_NAME_SUFFIX)) {
                    loadBundleKeys(mEntry, kid);
                } else if (kid.getName().endsWith(BUNDLE_NAME_PREFIX + "_" + this.locale.toString() + BUNDLE_NAME_SUFFIX)) {
                    loadLocalizedBundleKeys(mEntry, kid);
                    foundLocale = true;
                } else {
                    loadBrandedFiles(mEntry, kid);
                }
            } else {
                scanBrandedFiles(kid, mEntry);
            }
        }
        if(!foundLocale) {
            File defaultBundle = new File(srcDir, BUNDLE_NAME_PREFIX + BUNDLE_NAME_SUFFIX);
            if(defaultBundle.exists()) {
                loadLocalizedBundleKeys(mEntry, defaultBundle);
            }
        }
    }
    
    private void loadBundleKeys(final BrandableModule mEntry,
            final File bundle) throws IOException {
        EditableProperties p = getEditableProperties(bundle);
        for (Map.Entry<String,String> entry : p.entrySet()) {
            brandedBundleKeys.add(new BundleKey(mEntry, bundle, entry.getKey(), entry.getValue()));
        }
    }
    
    private void loadLocalizedBundleKeys(final BrandableModule mEntry,
            final File bundle) throws IOException {
        EditableProperties p = getEditableProperties(bundle);
        for (Map.Entry<String,String> entry : p.entrySet()) {
            localizedBrandedBundleKeys.add(new BundleKey(mEntry, bundle, entry.getKey(), entry.getValue()));
        }
    }
    
    private void loadBrandedFiles(final BrandableModule mEntry,
            final File file) throws IOException {
        String entryPath = PropertyUtils.relativizeFile(getModuleEntryDirectory(mEntry),file);
        BrandedFile bf = new BrandedFile(mEntry, Utilities.toURI(file).toURL(), entryPath);
        brandedFiles.add(bf);
    }
    
    private static EditableProperties getEditableProperties(final File bundle) throws IOException {
        EditableProperties p = new EditableProperties(true);
        InputStream is = new FileInputStream(bundle);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return p;
    }
    
    private static void storeEditableProperties(final EditableProperties p, final File bundle) throws IOException {
        FileObject fo = FileUtil.toFileObject(bundle);
        OutputStream os = null == fo ? new FileOutputStream(bundle) : fo.getOutputStream();
        try {
            p.store(os);
        } finally {
            os.close();
        }
    }

    protected abstract Map<String,String> localizingBundle(BrandableModule moduleEntry);
    
    private void loadLocalizedBundlesFromPlatform(final BrandableModule moduleEntry, final Set<String> keys, final Set<BundleKey> bundleKeys) {
        Map<String,String> p = localizingBundle(moduleEntry);
        for (Map.Entry<String,String> entry : p.entrySet()) {
            if (keys.contains(entry.getKey())) {
                bundleKeys.add(new BundleKey(moduleEntry, entry.getKey(), entry.getValue()));
            }
        }
    }
    
    private void loadLocalizedBundlesFromPlatform(final BrandableModule moduleEntry,
            final String bundleEntry, final Set<String> keys, final Set<BundleKey> bundleKeys) throws IOException {
        Properties p = new Properties();
        JarFile module = new JarFile(moduleEntry.getJarLocation());
        JarEntry je = module.getJarEntry(bundleEntry);
        InputStream is = module.getInputStream(je);
        File bundle = new File(getModuleEntryDirectory(moduleEntry),bundleEntry);
        try {
            
            p.load(is);
        } finally {
            is.close();
        }
        for (String key : NbCollections.checkedMapByFilter(p, String.class, String.class, true).keySet()) {
            if (keys.contains(key)) {
                String value = p.getProperty(key);
                bundleKeys.add(new BundleKey(moduleEntry, bundle, key, value));
            } 
        }
    }
    
    BundleKey createModifiedBundleKey(final BrandableModule moduleEntry, final File brandingBundle, final String key, final String value) {
        BundleKey bundleKey = new BundleKey(moduleEntry, brandingBundle, key, "");
        bundleKey.setValue(value);
        return bundleKey;
    }
    
    private boolean isBrandingForModuleEntry(final File srcDir, final BrandableModule mEntry) {
        return mEntry.getRelativePath().equals(PropertyUtils.relativizeFile(brandingDir, srcDir));
    }
    
    public final class BundleKey {
        private final File brandingBundle;
        private final BrandableModule moduleEntry;
        private final @NonNull String key;
        private @NonNull String value;
        private boolean modified = false;
        
        private BundleKey(final BrandableModule moduleEntry, final File brandingBundle, final String key, final String value) {
            this.moduleEntry = moduleEntry;
            assert key != null && value != null;
            this.key = key;
            this.value = value;
            this.brandingBundle = brandingBundle;
        }
        
        private BundleKey(final BrandableModule mEntry, final String key, final String value) {
            this(mEntry, getLocalizingBundle(mEntry), key,value);
        }
        
        BrandableModule getModuleEntry() {
            return moduleEntry;
        }
        
        @NonNull String getKey() {
            return key;
        }
        
        public @NonNull String getValue() {
            return value;
        }
        
        public void setValue(@NonNull String value) {
            assert value != null;
            if (!this.value.equals(value)) {
                modified = true;
            }
            this.value = value;
        }
        
        @Override
        public boolean equals(Object obj) {
            boolean retval = false;
            
            if (obj instanceof BundleKey) {
                BundleKey bKey = (BundleKey)obj;
                retval = getKey().equals(bKey.getKey())
                && getModuleEntry().equals(bKey.getModuleEntry())
                && getBrandingBundle().equals(bKey.getBrandingBundle());
            }
            
            return  retval;
        }
        
        @Override
        public int hashCode() {
            return 0;
        }
        
        boolean isModified() {
            return modified;
        }
        
        private File getBrandingBundle() {
            return brandingBundle;
        }

        String getBundleFilePath() {
            return  brandingBundle.getPath();
        }
    }
    
    public final class BrandedFile {
        private final BrandableModule moduleEntry;
        private final String entryPath;
        private @NonNull URL brandingSource;
        private boolean modified = false;
        
        private BrandedFile(final BrandableModule moduleEntry, final String entry) throws MalformedURLException {
            this(moduleEntry, null, entry);
        }
        
        private BrandedFile(final BrandableModule moduleEntry, final URL source, final String entry) throws MalformedURLException {
            this.moduleEntry = moduleEntry;
            this.entryPath = entry;
            if (source == null) {
                brandingSource = Utilities.toURI(moduleEntry.getJarLocation()).toURL();
                brandingSource =  new URL("jar:" + brandingSource + "!/" + entryPath); // NOI18N
            } else {
                brandingSource = source;
            }
            
        }
        
        File getFileLocation() {
            return new File(getModuleEntryDirectory(moduleEntry), entryPath);
        }
        
        public @NonNull URL getBrandingSource()  {
            return brandingSource;
        }
        
        public void setBrandingSource(@NonNull URL brandingSource) {
            Parameters.notNull("brandingSource", brandingSource);
            if (!Utilities.compareObjects(brandingSource, this.brandingSource)) {
                modified = true;
            }
            this.brandingSource = brandingSource;
        }
        
        boolean isModified() {
            return modified;
        }
        
        @Override
        public boolean equals(Object obj) {
            boolean retval = false;
            
            if (obj instanceof BrandedFile) {
                BrandedFile bFile = (BrandedFile)obj;
                retval = moduleEntry.equals(bFile.moduleEntry)
                && entryPath.equals(bFile.entryPath);
            }
            
            //if ()
            return  retval;
        }

        @Override
        public int hashCode() {
            return 0;
        }
        
    }
}
