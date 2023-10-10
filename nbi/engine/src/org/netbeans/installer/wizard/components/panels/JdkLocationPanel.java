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

package org.netbeans.installer.wizard.components.panels;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.dependencies.InstallAfter;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.Version.VersionDistance;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;

/**
 *
 * @author Kirill Sorokin
 */
public class JdkLocationPanel extends ApplicationLocationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Version minimumVersion;
    private Version maximumVersion;
    private Version preferredVersion;
    private String  vendorAllowed;
    private List<File>   jdkLocations;
    private List<String> jdkLabels;
    private static File lastSelectedJava = null;
    
    public JdkLocationPanel() {
        setProperty(MINIMUM_JDK_VERSION_PROPERTY,
                DEFAULT_MINIMUM_JDK_VERSION);
        setProperty(MAXIMUM_JDK_VERSION_PROPERTY,
                DEFAULT_MAXIMUM_JDK_VERSION);
        setProperty(VENDOR_JDK_ALLOWED_PROPERTY,
                DEFAULT_VENDOR_JDK_ALLOWED);
        setProperty(JRE_ALLOWED_PROPERTY, 
                DEFAULT_JRE_ALLOWED);
        setProperty(LOCATION_LABEL_TEXT_PROPERTY,
                DEFAULT_LOCATION_LABEL_TEXT);
        setProperty(LOCATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_LOCATION_BUTTON_TEXT);
        setProperty(LIST_LABEL_TEXT_PROPERTY,
                DEFAULT_LIST_LABEL_TEXT);
        
        setProperty(ERROR_NULL_PROPERTY,
                DEFAULT_ERROR_NULL);
        setProperty(ERROR_NOT_VALID_PATH_PROPERTY,
                DEFAULT_ERROR_NOT_VALID_PATH);
        setProperty(ERROR_PATH_NOT_EXISTS_PROPERTY,
                DEFAULT_ERROR_PATH_NOT_EXISTS);
        setProperty(ERROR_NOT_JAVAHOME_PROPERTY,
                DEFAULT_ERROR_NOT_JAVAHOME);
        setProperty(ERROR_NOT_JDK_PROPERTY,
                DEFAULT_ERROR_NOT_JDK);
        setProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY,
                DEFAULT_ERROR_WRONG_VERSION_OLDER);
        setProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY,
                DEFAULT_ERROR_WRONG_VERSION_NEWER);
        setProperty(ERROR_WRONG_VENDOR_PROPERTY,
                DEFAULT_ERROR_WRONG_VENDOR);
        setProperty(ERROR_UNKNOWN_PROPERTY,
                DEFAULT_ERROR_UNKNOWN);
        setProperty(ERROR_NOTHING_FOUND_PROPERTY,
                DEFAULT_ERROR_NOTHING_FOUND);
        
        setProperty(USEDBY_LABEL_PROPERTY,
                DEFAULT_USEDBY_LABEL);

        setProperty(JAVA_DOWNLOAD_PAGE_PROPERTY,
                !SystemUtils.isMacOS() ?
                    DEFAULT_JAVA_DOWNLOAD_PAGE :
                    DEFAULT_JAVA_DOWNLOAD_PAGE_MAC);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        minimumVersion = Version.getVersion(
                getProperty(MINIMUM_JDK_VERSION_PROPERTY));
        maximumVersion = Version.getVersion(
                getProperty(MAXIMUM_JDK_VERSION_PROPERTY));
        vendorAllowed = getProperty(VENDOR_JDK_ALLOWED_PROPERTY);
        
        if (getProperty(PREFERRED_JDK_VERSION_PROPERTY) != null) {
            preferredVersion = Version.getVersion(
                    getProperty(PREFERRED_JDK_VERSION_PROPERTY));
        }
        
        addJavaLocationsFromProductDependencies();
        
        jdkLocations = new LinkedList<File>();
        jdkLabels = new LinkedList<String>();
        
        final Registry registry = Registry.getInstance();
        for (int i = 0; i < SearchForJavaAction.getJavaLocations().size(); i++) {
            final File location = SearchForJavaAction.getJavaLocations().get(i);
            
            String label = SearchForJavaAction.getJavaLabels().get(i);
            Version version = null;
            
            // initialize the version; if the location exists, it must be an
            // already installed jdk and we should fetch the version in a
            // "traditional" way; otherwise the jdk is only planned for
            // installation and we should try to get its version from the
            // registry
            if (location.exists()) {
                version = JavaUtils.getVersion(location);
            }
            if (version == null) {
                version = getVersion(location);
            }
            
            // if we could not fetch the version, we should skip this jdk
            // installation
            if (version == null) {
                continue;
            }
            
            // run through the installed and to-be-installed products and check
            // whether this location is already used somewhere
            final RegistryFilter filter = new OrFilter(
                    new ProductFilter(Status.INSTALLED),
                    new ProductFilter(Status.TO_BE_INSTALLED));
            final List<Product> products = new LinkedList<Product>();
            for (Product product: registry.queryProducts(filter)) {
                final String jdk = product.getProperty(JDK_LOCATION_PROPERTY);
                
                if ((jdk != null) && jdk.equals(location.getAbsolutePath())) {
                    products.add(product);
                }
            }
            
            final Product product = (Product) getWizard().
                    getContext().
                    get(Product.class);
            
            if (products.contains(product)) {
                products.remove(product);
            }
            if (products.size() > 0) {
                label = StringUtils.format(
                        getProperty(USEDBY_LABEL_PROPERTY),
                        label,
                        StringUtils.asString(products));
            }
            
            // if the location exists and is a jdk installation (or if the
            // location does not exist - in this case we're positive that it
            // WILL be a jdk) and if version satisfies the requirements - add
            // the location to the list
            if ((!location.exists() || isJreAllowed() || JavaUtils.isJdk(location))) {
                String vendor = JavaUtils.getInfo(location).getVendor();
                if (JavaUtils.getInfo(location) == null) {
                    LogManager.log("JdkLocationPanel - JavaUtils.getInfo(" + location + ") returns null!");
                    continue;
                }
                
                if(!version.olderThan(minimumVersion) &&
                        !version.newerThan(maximumVersion) &&
                        vendor.matches(vendorAllowed)) {
                    jdkLocations.add(location);
                    jdkLabels.add(label);
                }
                
            }
        }
                
        //reinitialize properties that are different in case of JRE or only-JDK allowance
        final boolean jreAllowed = isJreAllowed();
        setProperty(LOCATION_LABEL_TEXT_PROPERTY,
                jreAllowed ? DEFAULT_LOCATION_LABEL_TEXT_JAVA : DEFAULT_LOCATION_LABEL_TEXT);
        setProperty(LIST_LABEL_TEXT_PROPERTY,
                jreAllowed ? DEFAULT_LIST_LABEL_TEXT_JAVA : DEFAULT_LIST_LABEL_TEXT);
        setProperty(ERROR_NULL_PROPERTY,
                jreAllowed ? DEFAULT_ERROR_NULL_JAVA : DEFAULT_ERROR_NULL);
        setProperty(ERROR_NOT_VALID_PATH_PROPERTY,
                jreAllowed ? DEFAULT_ERROR_NOT_VALID_PATH_JAVA : DEFAULT_ERROR_NOT_VALID_PATH);
        setProperty(ERROR_PATH_NOT_EXISTS_PROPERTY,
                jreAllowed ? DEFAULT_ERROR_PATH_NOT_EXISTS_JAVA : DEFAULT_ERROR_PATH_NOT_EXISTS);
        setProperty(ERROR_NOT_JAVAHOME_PROPERTY,
                jreAllowed ? DEFAULT_ERROR_NOT_JAVAHOME_JAVA : DEFAULT_ERROR_NOT_JAVAHOME);
        setProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY,
                jreAllowed ? DEFAULT_ERROR_WRONG_VERSION_OLDER_JAVA : DEFAULT_ERROR_WRONG_VERSION_OLDER);
        setProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY,
                jreAllowed ? DEFAULT_ERROR_WRONG_VERSION_NEWER_JAVA : DEFAULT_ERROR_WRONG_VERSION_NEWER);
        setProperty(ERROR_WRONG_VENDOR_PROPERTY,
                jreAllowed ? DEFAULT_ERROR_WRONG_VENDOR_JAVA : DEFAULT_ERROR_WRONG_VENDOR);
        setProperty(ERROR_NOTHING_FOUND_PROPERTY,
                jreAllowed ? DEFAULT_ERROR_NOTHING_FOUND_JAVA : DEFAULT_ERROR_NOTHING_FOUND);
    }
    
    @Override
    public List<File> getLocations() {
        return jdkLocations;
    }
    
    @Override
    public List<String> getLabels() {
        return jdkLabels;
    }
    
    private boolean isJreAllowed() {        
        return "true".equals(getProperty(JRE_ALLOWED_PROPERTY));
    }
    
    @Override
    public File getSelectedLocation() {
        // the first obvious choice is the jdk that has already been selected for
        // this product; if it has not yet been set, there are still lots of
        // choices:
        // - reuse the location which is set using <uid>.jdk.location system property
        // - reuse the location which was selected on another jdk location panel if
        //   it fits the requirements
        // - use the location of the jdk if it is bundled and already installed
        // - reuse the location which has been used for an installed product if
        //   it fits the requirements
        // - choose the closest one to the preferred version if it is defined and
        //   a valid closest version exists
        // - use the first item in the list
        // - use an empty path
        
        File jdkLocation = null;
        
        jdkLocation = getJavaAlreadySelectedForProduct();
        if (jdkLocation != null) {
            LogManager.log("... use Java that is already selected for the product: "  + jdkLocation);
            return jdkLocation;
        }

        jdkLocation = getJavaFromSystemProperty();
        if (jdkLocation != null) {
            LogManager.log("... use Java that is passed via a system property: "  + jdkLocation);
            return jdkLocation;
        }

        jdkLocation = getJavaAlreadySelectedGlobal();
        if (jdkLocation != null) {
            LogManager.log("... use Java that is already selected for another product which be installed: " + jdkLocation);
            return jdkLocation;
        }

        jdkLocation = getJavaBundledAndInstalled();
        if (jdkLocation != null) {
            LogManager.log("... use Java that is bundled and installed: "  + jdkLocation);
            return jdkLocation;
        }
        /*
        jdkLocation = getJavaFromInstalledProductProperties();
        if (jdkLocation != null) {
            LogManager.log("... use Java from properties of installed products: "  + jdkLocation);
            return jdkLocation;
        }
        */
        jdkLocation = getJavaPreferredVersionLocation();
        if (jdkLocation != null) {
            LogManager.log("... use Java based on the preferred version: "  + jdkLocation);
            return jdkLocation;
        }
        
        jdkLocation = getJavaFirstItemInTheList();
        if (jdkLocation != null) {
            LogManager.log("... use Java from the first item in the overall list: "  + jdkLocation);
            return jdkLocation;
        } else {
            LogManager.log("... no Java found");
            return new File(StringUtils.EMPTY_STRING);
        }
    }
    
    private File getJavaFirstItemInTheList() {
        if(jdkLocations.isEmpty()) {
            return null;
        }        
        if(isJreAllowed()) {
            for(File f : jdkLocations) {
                if(!FileUtils.exists(f) || JavaUtils.isJdk(f)) {
                    return f;                     
                }
            }            
        }        
        return jdkLocations.get(0);        
    }
         

    private File getJavaFromSystemProperty() {
        final Object obj = getWizard().
                getContext().
                get(Product.class);
        if (obj instanceof Product) {
            final Product product = (Product) obj;
            final String jdkSysPropName = product.getUid() + StringUtils.DOT +
                    JdkLocationPanel.JDK_LOCATION_PROPERTY;
            final String jdkSysProp = System.getProperty(jdkSysPropName);
            if (jdkSysProp != null) {
                final File f = new File(jdkSysProp);
                if (jdkLocations.contains(f)) {
                    LogManager.log("... using JDK from system property " + jdkSysPropName + " : " + jdkSysProp);
                    return f;
                }
            }
        }
        return null;
    }

    private File getJavaAlreadySelectedForProduct() {
        final String jdkLocation =
                getWizard().getProperty(JDK_LOCATION_PROPERTY);
        if (jdkLocation != null && jdkLocations.contains(new File(jdkLocation))) {
            return new File(jdkLocation);
        }
        return null;
    }

    private File getJavaAlreadySelectedGlobal() {
        return ((lastSelectedJava != null) &&
                jdkLocations.contains(lastSelectedJava)) ? lastSelectedJava : null;
    }

    private File getJavaBundledAndInstalled() {
        try {
            Registry bundledRegistry = new Registry();
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);

            bundledRegistry.loadProductRegistry(
                    (bundledRegistryUri != null) ? bundledRegistryUri : Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);

            // iterate over bundled JDKs to check whether they are already installed
            for (Product bundledJdk : bundledRegistry.getProducts(JDK_PRODUCT_UID)) {
                Product globalJdk = Registry.getInstance().getProduct(
                        JDK_PRODUCT_UID,
                        bundledJdk.getVersion());

                if (globalJdk != null) {
                    final File jdkLoc = globalJdk.getStatus().equals(Status.INSTALLED) ? globalJdk.getInstallationLocation() : JavaUtils.findJDKHome(globalJdk.getVersion());

                    if (jdkLoc != null && jdkLocations.contains(jdkLoc)) {
                        return jdkLoc;
                    }

                }
            }

        } catch (InitializationException e) {
            LogManager.log("Cannot load bundled registry", e);
        }

        return null;
    }

    private File getJavaFromInstalledProductProperties() {
        for (Product product : Registry.getInstance().queryProducts(new OrFilter(
                new ProductFilter(Status.INSTALLED),
                new ProductFilter(Status.TO_BE_INSTALLED)))) {
            final String jdk = product.getProperty(JDK_LOCATION_PROPERTY);

            if (jdk != null) {
                final File jdkFile = new File(jdk);

                if (jdkLocations.contains(jdkFile)) {
                    return jdkFile;
                }
            }
        }
        return null;
    }

    private File getJavaPreferredVersionLocation() {
        if (preferredVersion == null) {
            return null;
        }
        //if JRE is allowed then first search among full JDKs and for closest one
        //if JDK is not found then choose the closest between JREs
        File closestLocation = null;
        VersionDistance closestDistance = null;
        if (isJreAllowed()) {

            for (File location : jdkLocations) {
                final Version currentVersion =
                        JavaUtils.getVersion(location);
                final VersionDistance currentDistance =
                        currentVersion.getDistance(preferredVersion);
                if (!FileUtils.exists(location) || JavaUtils.isJdk(location)) {
                    if ((closestDistance == null) ||
                            currentDistance.lessThan(closestDistance)) {
                        closestLocation = location;
                        closestDistance = currentDistance;
                    }
                }
            }
        }
        if (closestLocation == null) {
            for (File location : jdkLocations) {
                final Version currentVersion =
                        JavaUtils.getVersion(location);
                final VersionDistance currentDistance =
                        currentVersion.getDistance(preferredVersion);

                if ((closestDistance == null) ||
                        currentDistance.lessThan(closestDistance)) {
                    closestLocation = location;
                    closestDistance = currentDistance;
                }
            }
        }
        return closestLocation;
    }
    
    @Override
    public String validateLocation(final String path) {
        final File file = new File(path);
        
        if (path.equals(StringUtils.EMPTY_STRING)) {
            return StringUtils.format(
                    getProperty(ERROR_NULL_PROPERTY));
        }
        
        if (!SystemUtils.isPathValid(path)) {
            return StringUtils.format(
                    getProperty(ERROR_NOT_VALID_PATH_PROPERTY), path);
        }
                
        if (!file.exists()) {
            if(JavaUtils.getInfo(file)==null) { 
                // JDK location does not exist and is not in the list of installable JDKs
                return StringUtils.format(
                        getProperty(ERROR_PATH_NOT_EXISTS_PROPERTY), path);
            }
        } else {            
            if (!JavaUtils.isJavaHome(file)) {
                return StringUtils.format(
                        getProperty(ERROR_NOT_JAVAHOME_PROPERTY), path);
            }
            
            if (!isJreAllowed() && !JavaUtils.isJdk(file)) {
                return StringUtils.format(
                        getProperty(ERROR_NOT_JDK_PROPERTY), path);                
            }          
        }                
        
        Version version = getVersion(file);
        
        if (version == null) {
            return StringUtils.format(getProperty(ERROR_UNKNOWN_PROPERTY), path);
        }                
        
        if (version.olderThan(minimumVersion)) {
            return StringUtils.format(
                    getProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY),
                    path,
                    version,
                    minimumVersion);
        }
        
        if (version.newerThan(maximumVersion)) {
            return StringUtils.format(
                    getProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY),
                    path,
                    version,
                    maximumVersion);
        }       
        
        String vendor = JavaUtils.getInfo(file).getVendor();
        if(!vendor.matches(vendorAllowed)) {
            return StringUtils.format(
                    getProperty(ERROR_WRONG_VENDOR_PROPERTY),
                    path,
                    vendor,
                    vendorAllowed);
        }
        
        return null;
    }        
    
    @Override
    public void setLocation(final File location) {
        lastSelectedJava = location;
        SearchForJavaAction.addJavaLocation(location);
        getWizard().setProperty(JDK_LOCATION_PROPERTY, location.getAbsolutePath());
    }
    
    /**
     * Returns if JDK version is recommended - #218822
     * @param jdkPath
     * @return false if not recommended. If JDK does not exist returns true.
     */
    public boolean isJdkVersionRecommended(String jdkPath) {
        File jdkFile = new File(jdkPath);        
        Version version = getVersion(jdkFile);
        
        return version != null ? JavaUtils.isRecommended(version) : true;
    }
    
    public boolean isArchitectureMatching(String jdkPath) {
        if (jdkPath == null || jdkPath.isEmpty()) {
            return true; // Better to suppose the arch is matching then to confuse user.
        }
        
        File jdkFile = new File(jdkPath);
        JavaUtils.JavaInfo info = JavaUtils.getInfo(jdkFile);
        
        if (info == null) {
            return true; // Better to suppose the arch is matching then to confuse user.
        }
        
        return info.getArch().endsWith("64") || !SystemUtils.getNativeUtils().isSystem64Bit();
    }
    
    private Version getVersion(File file) {
        Version version = JavaUtils.getVersion(file);
        
        if (version == null) {
            for (Product jdk : Registry.getInstance().getProducts(JDK_PRODUCT_UID)) {
                if ((jdk.getStatus() == Status.TO_BE_INSTALLED) && jdk.getInstallationLocation().equals(file)) {
                    version = jdk.getVersion();
                }
            }
            for (Product jreNested : Registry.getInstance().getProducts(JRE_NESTED_PRODUCT_UID)) {
                if ((jreNested.getStatus() == Status.TO_BE_INSTALLED) && jreNested.getInstallationLocation().equals(file)) {
                    version = jreNested.getVersion();
                }
            }
        }
        
        return version;
    }
    
    private void addJavaLocationsFromProductDependencies() {
        // finally we should scan the registry for jdks planned for installation, if
        // the current product is scheduled to be installed after 'jdk', i.e. has
        // an install-after dependency on 'jdk' uid
        
        final Object objectContext = getWizard().getContext().get(Product.class);
        boolean sort = false;
        if(objectContext instanceof Product) {
            final Product product = (Product) objectContext;
            for (Dependency dependency : product.getDependencies(InstallAfter.class)) {
                
                if (dependency.getUid().equals(JDK_PRODUCT_UID)) {
                    for (Product jdk : Registry.getInstance().getProducts(JDK_PRODUCT_UID)) {
                        if (jdk.getStatus() == Status.TO_BE_INSTALLED &&
                                !SearchForJavaAction.getJavaLocations().
                                contains(jdk.getInstallationLocation())) {
                            SearchForJavaAction.addJavaLocation(
                                    jdk.getInstallationLocation(),
                                    jdk.getVersion(),
                                    SUN_MICROSYSTEMS_VENDOR);
                            sort = true;
                        }
                    }
                }
                if (dependency.getUid().equals(JRE_NESTED_PRODUCT_UID)) {
                    for (Product jreNested : Registry.getInstance().getProducts(JRE_NESTED_PRODUCT_UID)) {
                        if (jreNested.getStatus() == Status.TO_BE_INSTALLED) {
                            SearchForJavaAction.addJavaLocation(
                                    jreNested.getInstallationLocation(),
                                    jreNested.getVersion(),
                                    SUN_MICROSYSTEMS_VENDOR);
                            sort = true;
                        }
                    }
                    
                    break;
                }
            }
        }
        if(sort) {
            SearchForJavaAction.sortJavaLocations();
        }
    }
/////////////////////////////////////////////////////////////////////////////////
// Constants
    public static final String JDK_LOCATION_PROPERTY =
            "jdk.location"; // NOI18N
    
    public static final String JRE_NESTED =
            "jre.nested"; // NOI18N
    
    public static final String MINIMUM_JDK_VERSION_PROPERTY =
            "minimum.jdk.version"; // NOI18N
    public static final String MAXIMUM_JDK_VERSION_PROPERTY =
            "maximum.jdk.version"; // NOI18N
    public static final String PREFERRED_JDK_VERSION_PROPERTY =
            "preferred.jdk.version"; // NOI18N
    public static final String VENDOR_JDK_ALLOWED_PROPERTY =
            "vendor.jdk.allowed.pattern"; // NOI18N
    public static final String JRE_ALLOWED_PROPERTY =
            "jre.allowed"; // NOI18N
    public static final String DEFAULT_JRE_ALLOWED =
            "false";//NOI18N
    
    public static final String DEFAULT_LOCATION_LABEL_TEXT =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.location.label.text"); // NOI18N
    public static final String DEFAULT_LOCATION_LABEL_TEXT_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.location.label.text.java"); // NOI18N
    public static final String DEFAULT_LOCATION_BUTTON_TEXT =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.location.button.text"); // NOI18N
    public static final String DEFAULT_LIST_LABEL_TEXT =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.list.label.text"); // NOI18N
    public static final String DEFAULT_LIST_LABEL_TEXT_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.list.label.text.java"); // NOI18N
    
    public static final String ERROR_NULL_PROPERTY =
            "error.null"; // NOI18N
    public static final String ERROR_NOT_VALID_PATH_PROPERTY =
            "error.not.valid.path"; // NOI18N
    public static final String ERROR_PATH_NOT_EXISTS_PROPERTY =
            "error.path.not.exists"; // NOI18N
    public static final String ERROR_NOT_JAVAHOME_PROPERTY =
            "error.not.javahome"; // NOI18N
    public static final String ERROR_NOT_JDK_PROPERTY =
            "error.not.jdk"; // NOI18N
    public static final String ERROR_WRONG_VERSION_OLDER_PROPERTY =
            "error.wrong.version.older"; // NOI18N
    public static final String ERROR_WRONG_VERSION_NEWER_PROPERTY =
            "error.wrong.version.newer"; // NOI18N
    public static final String ERROR_WRONG_VENDOR_PROPERTY =
            "error.wrong.vendor"; // NOI18N
    public static final String ERROR_UNKNOWN_PROPERTY =
            "error.unknown"; // NOI18N
    
    public static final String DEFAULT_ERROR_NULL =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.null"); // NOI18N
    public static final String DEFAULT_ERROR_NULL_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.null.java"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_VALID_PATH =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.not.valid.path"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_VALID_PATH_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.not.valid.path.java"); // NOI18N
    public static final String DEFAULT_ERROR_PATH_NOT_EXISTS =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.path.not.exists"); // NOI18N
    public static final String DEFAULT_ERROR_PATH_NOT_EXISTS_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.path.not.exists.java"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_JAVAHOME =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.not.javahome"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_JAVAHOME_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.not.javahome.java"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_JDK =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.not.jdk"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VERSION_OLDER =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.version.older"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VERSION_OLDER_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.version.older.java"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VERSION_NEWER =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.version.newer"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VERSION_NEWER_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.version.newer.java"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VENDOR =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.vendor"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VENDOR_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.vendor.java"); // NOI18N
    
    public static final String DEFAULT_ERROR_UNKNOWN =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.unknown"); // NOI18N
    public static final String DEFAULT_ERROR_NOTHING_FOUND =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.nothing.found"); // NOI18N
    public static final String DEFAULT_ERROR_NOTHING_FOUND_JAVA =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.nothing.found.java"); // NOI18N
    
    public static final String DEFAULT_MINIMUM_JDK_VERSION =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.minimum.jdk.version"); // NOI18N
    public static final String DEFAULT_MAXIMUM_JDK_VERSION =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.maximum.jdk.version"); // NOI18N
    public static final String DEFAULT_VENDOR_JDK_ALLOWED =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.vendor.jdk.allowed");

    public static final String JAVA_DOWNLOAD_PAGE_PROPERTY =
            "java.download.page";

    public static final String DEFAULT_JAVA_DOWNLOAD_PAGE =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.java.download.page");
    public static final String DEFAULT_JAVA_DOWNLOAD_PAGE_MAC =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.java.download.page.mac");
    
    public static final String DEFAULT_USEDBY_LABEL =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.usedby.label"); //NOI18N
    public static final String USEDBY_LABEL_PROPERTY =
            "usedby.label"; //NOI18N
    
    private static final String SUN_MICROSYSTEMS_VENDOR =
            "Sun Microsystems Inc." ; //NOI18N
    
    private static final String JDK_PRODUCT_UID =
            "jdk"; //NOI18N
    
    private static final String JRE_NESTED_PRODUCT_UID =
            "jre-nested"; //NOI18N
}
