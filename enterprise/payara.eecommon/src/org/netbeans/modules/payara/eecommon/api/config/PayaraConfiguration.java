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

package org.netbeans.modules.payara.eecommon.api.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.payara.eecommon.api.Utils;
import org.netbeans.modules.payara.eecommon.api.XmlFileCreator;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.payara.eecommon.dd.loader.PayaraDDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * Basic Java EE server configuration API support for V2, V3 and V4 plugins.
 * <p/>
 * @author Peter Williams, Tomas Kraus
 */
public abstract class PayaraConfiguration implements
        ContextRootConfiguration,
        EjbResourceConfiguration,
        MessageDestinationConfiguration,
        DatasourceConfiguration {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara Java EE common module Logger. */
    private static final Logger LOGGER = Logger.getLogger("payara-eecommon");

    /** Payara resource file suffix is {@code .xml}. */
    private static final String RESOURCE_FILES_SUFFIX = ".xml";

   /** List of base file names containing server resources:<ul>
      * <li><i>[0]</i> points to current name used since GlassFich v3.</li>
      * <li><i>[1]</i> points to old name used before GlassFich v3.</li>
      * <ul>*/
    static final String[] RESOURCE_FILES = {
        "glassfish-resources" + RESOURCE_FILES_SUFFIX,
        "sun-resources" + RESOURCE_FILES_SUFFIX
    };

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create resource file path fragment for given Java EE module.
     * <i>Internal helper method.</i>
     * <p/>
     * @param module   Java EE module (project).
     * @param fileName Resources file name.
     * @return Resource file path fragment for given Java EE module.
     */
    private static String resourceFilePath(final J2eeModule module,final String fileName) {
        String configDir = JavaEEModule.getConfigDir(module.getType());
        if (configDir == null) {
            throw new IllegalArgumentException("Unknown Java EE module type.");
        }
        return OsUtils.joinPaths(configDir, fileName);
    }

    /**
     * Get existing {@code RESOURCE_FILES} array indexes for provided Payara
     * server version.
     * <br/>
     * <i>Internal {@link #getExistingResourceFile(J2eeModule, PayaraVersion)
     * helper method.</i>
     * <p/>
     * @param version Payara server version.
     * @return An array of {@code RESOURCE_FILES} array indexes pointing
     *         to resource files to search for.
     */
    private static int[] versionToResourceFilesIndexes(
            final PayaraVersion version) {
        // All files for unknown version.
        if (version == null) {
            return new int[]{0,1};
        }
        // glassfish-resources.xml for v4
        if (PayaraVersion.ge(version, PayaraVersion.PF_4_1_144)) {
            return new int[]{0};
        } else {
            // sun-resources.xml for older
            return new int[]{1};
        }
    }

    /**
     * Get new {@code RESOURCE_FILES} array index for provided Payara
     * server version.
     * <p/>
     * @param version Payara server version.
     * @return An {@code RESOURCE_FILES} array index pointing
     *         to resource file to be created.
     */
    private static int versionToNewResourceFilesIndex(
            final PayaraVersion version) {
        // glassfish-resources.xml is returned for versions 3.1 and higher
        // or as default for unknown version.
        if (version == null
                || PayaraVersion.ge(version, PayaraVersion.PF_4_1_144)) {
            return 0;
        } else {
        // sun-resources.xml is returned for old versions
            return 1;
        }
    }

    /**
     * Get existing Payara resources file name.
     * Payara resources file depends on server version and used storage.
     * GlassFish v3 supports old {@code sun-resources.xml} together with new
     * {@code glassfish-resources.xml}. Payara v4 supports only
     * new {@code glassfish-resources.xml} file.
     * Those files can be found in server configuration directory (to be included
     * into target project archive) or in server resources directory (won't be
     * included into target project archive).
     * Search works with following priorities:<ul>
     * <li><i>GlassFish v2:</i> Only {@code sun-resources.xml} is checked.</li>
     * <li><i>GlassFish v3:</i> {@code glassfish-resources.xml} is checked first,
     *                          {@code sun-resources.xml} as fallback.</li>
     * <li><i>Payara v4:</i> Only {@code glassfish-resources.xml} is checked.</li>
     * <li><i>Configuration directory</i> is checked first, <i>resources directory</i>
     *        as fallback.</li>
     * </ul>
     * 
     * @param module  Java EE module (project).
     * @param version Resources file names depend on Payara server version.
     * @return Existing Payara resources file together with boolean flag
     *         indicating whether this is application scoped resource or
     *         {@code null} when no resources file was found.
     */
    public static final Pair<File, Boolean> getExistingResourceFile(
            final J2eeModule module, final PayaraVersion version) {
        // RESOURCE_FILES indexes to search for.
        final int[] indexes = versionToResourceFilesIndexes(version);
        for (int index : indexes) {
            // Check configuration directory first.
            final String name = resourceFilePath(module, RESOURCE_FILES[index]);
            File file = module.getDeploymentConfigurationFile(name);
            if (file != null && file.isFile() && file.canRead()) {
                return Pair.of(file, true);
            }
            // Check resources directory as a fallback.
            file = new File(module.getResourceDirectory(), RESOURCE_FILES[index]);
            if (file.isFile() && file.canRead()) {
                return Pair.of(file, false);
            }
            
            file = new File(module.getResourceDirectory(), "glassfish-resources");
            if (file.isFile() && file.canRead()) {
                return Pair.of(file, false);
            }
            
        }
        return null;
    }

    /**
     * Get new Payara resources file name for creation.
     * <p/>
     * @param module  Java EE module (project).
     * @param version Resources file names depend on Payara server version.
     * @return Payara resources file to be created.
     */
    public static final Pair<File, Boolean> getNewResourceFile(
            final J2eeModule module, final PayaraVersion version) {
        final int index = versionToNewResourceFilesIndex(version);
        if (PayaraVersion.lt(version, PayaraVersion.PF_4_1_144)) {
            return Pair.of(new File(module.getResourceDirectory(), RESOURCE_FILES[index]), false);
        }
        final String name = resourceFilePath(module, RESOURCE_FILES[index]);
        return Pair.of(module.getDeploymentConfigurationFile(name), true);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    protected final J2eeModule module;
    protected final J2eeModuleHelper moduleHelper;
    protected final File primaryDD;
    protected final File secondaryDD;
    protected DescriptorListener descriptorListener;
    /** Payara server version. */
    protected PayaraVersion version;
    private ASDDVersion appServerVersion;
    private ASDDVersion minASVersion;
    private ASDDVersion maxASVersion;
    private boolean deferredAppServerChange;
    private final String defaultcr;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE server configuration API support.
     * {@link J2eeModuleHelper} instance is added depending on Java EE module type.
     * <p/>
     * @param module  Java EE module (project).
     * @param version Payara server version.
     * @throws ConfigurationException when there is a problem with Java EE server
     *         configuration initialization.
     */
    protected PayaraConfiguration(
            final J2eeModule module, final PayaraVersion version
    ) throws ConfigurationException {
        this(module, J2eeModuleHelper.getSunDDModuleHelper(module.getType()), version);
    }

    /**
     * Creates an instance of Java EE server configuration API support with existing
     * {@link J2eeModuleHelper} instance.
     * <p/>
     * @param module       Java EE module (project).
     * @param moduleHelper Already existing {@link J2eeModuleHelper} instance.
     * @param version      Payara server version.
     * @throws ConfigurationException when there is a problem with Java EE server
     *         configuration initialization.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    protected PayaraConfiguration(
            final J2eeModule module, final J2eeModuleHelper moduleHelper,
            final PayaraVersion version
    ) throws ConfigurationException {
        this.module = module;
        this.moduleHelper = moduleHelper;
        this.version = version;
        if(moduleHelper != null) {
            this.primaryDD = moduleHelper.getPrimaryDDFile(module);
            this.secondaryDD = moduleHelper.getSecondaryDDFile(module);
        } else {
            throw new ConfigurationException("Unsupported module type: " + module.getType()); // NOI18N
        }

        if (null == primaryDD) {
            throw new ConfigurationException("No primaryDD for module type: " + module.getType()); // NOI18N
        }

        try {

            if (null == primaryDD.getParentFile()) {
                throw new ConfigurationException("module is not initialized completely");  // NOI18N
            }
            addConfiguration(primaryDD, this);
            if (primaryDD.getName().endsWith("-web.xml")) { // NOI18N
                String path = primaryDD.getParent().
                        replaceAll("[\\\\/]web[\\\\/]WEB-INF", "").   // NOI18N
                        replaceAll("[\\\\/]src[\\\\/]main[\\\\/]webapp[\\\\/]WEB-INF", "");  // NOI18N
                int dex = path.lastIndexOf(File.separatorChar);
                if (dex < 0) {
                    defaultcr = null;
                } else {
                    defaultcr = "/"+path.substring(dex+1);
                }

            } else {
                defaultcr = null;
            }

            // Default to 8.1 in new beans.  This is set by the bean parser
            // in the appropriate root type, if reading from existing file(s).
            this.appServerVersion = ASDDVersion.SUN_APPSERVER_8_1;
            this.deferredAppServerChange = false;
            J2eeModule.Type mt = module.getType();
            String moduleVersion = module.getModuleVersion();

            minASVersion = computeMinASVersion(moduleVersion);
            maxASVersion = computeMaxASVersion();
            appServerVersion = maxASVersion;

            J2EEBaseVersion j2eeVersion = J2EEBaseVersion.getVersion(mt, moduleVersion);
            boolean isPreJavaEE6 = (j2eeVersion != null) ?
                    (J2EEVersion.JAVAEE_5_0.compareSpecification(j2eeVersion) >= 0) : false;
            if (isPreJavaEE6) {
               // not supported
            }
        } catch (RuntimeException ex) {
            removeConfiguration(primaryDD);
            ConfigurationException ce = new ConfigurationException(primaryDD.getAbsolutePath(), ex);
            throw ce;
        }
    }

    @Deprecated
    public PayaraConfiguration() {
        throw new UnsupportedOperationException("JSR-88 configuration not supported.");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    public void dispose() {
        if(descriptorListener != null) {
            descriptorListener.removeListeners();
            descriptorListener = null;
        }

        PayaraConfiguration storedCfg = getConfiguration(primaryDD);
        if (storedCfg != this) {
            LOGGER.log(Level.INFO, 
                    "Stored DeploymentConfiguration ({0}) instance not the one being disposed of ({1}).",
                    new Object[]{storedCfg, this});
        }

        if (storedCfg != null) {
            removeConfiguration(primaryDD);
        }
    }

    // ------------------------------------------------------------------------
    // Appserver version support
    // ------------------------------------------------------------------------
    private ASDDVersion computeMinASVersion(String j2eeModuleVersion) {
        return moduleHelper.getMinASVersion(
                j2eeModuleVersion, ASDDVersion.SUN_APPSERVER_7_0);
    }

    private ASDDVersion computeMaxASVersion() {
        // This is min of (current server target, 9.0) so if we can figure out the
        // target server, use that, otherwise, use 9.0.
        ASDDVersion result = getTargetAppServerVersion();
        if (result == null) {
            if (primaryDD.getName().startsWith("glassfish-"))
                result = ASDDVersion.SUN_APPSERVER_10_1;
            else
                result = ASDDVersion.SUN_APPSERVER_10_0;
            LOGGER.log(Level.WARNING,
                    NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_UnidentifiedTargetServer", result.toString())); // NOI18N
        }
        return result;
    }

    public ASDDVersion getMinASVersion() {
        return minASVersion;
    }

    public ASDDVersion getMaxASVersion() {
        return maxASVersion;
    }

    /** Get the AppServer version to be used for saving deployment descriptors.
     *  Note that this is different than the version of the connected target
     *  application server (which can be queried by the appropriate method on
     *  SunONEDeploymentConfiguration.)
     *
     * @return ASDDVersion enum for the appserver version
     */
    public ASDDVersion getAppServerVersion() {
        return appServerVersion;
    }

    /** Set the AppServer version to be used for saving deployment descriptors.
     *  This version should be greater or equal to "minASVersion" and lesser or
     *  equal to "maxASVersion", as specified by the configuration, otherwise an
     *  IllegalArgumentException will be thrown.
     *
     * @param asVersion enum for the appserver version (cannot be null)
     */
    public void setAppServerVersion(ASDDVersion asVersion) {
        if (asVersion.compareTo(getMinASVersion()) < 0) {
            throw new IllegalArgumentException(asVersion.toString() +
                    " is lower than required minimum version " + getMinASVersion().toString());
        }

        if (asVersion.compareTo(getMaxASVersion()) > 0) {
            throw new IllegalArgumentException(asVersion.toString() +
                    " is higher than required maximum version " + getMaxASVersion().toString());
        }

        if (!asVersion.equals(appServerVersion) || deferredAppServerChange) {
            appServerVersion = asVersion;
//            ConfigurationStorage localStorage = getStorage();
//            if (localStorage != null) {
                deferredAppServerChange = false;
//                localStorage.setChanged();
//            }
        }
    }

    /** Set the AppServer version to be used for saving deployment descriptors.
     *
     *  This method is only for use by the DConfigBean tree, used to set the version
     *  while the configuration is being loaded (and thus should not and cannot be
     *  saved, which the public version would do.)  Instead, this changes the version
     *  and marks the change unsaved.  The version passed in here is the version
     *  actually found in the descriptor file as specified by the DOCTYPE, hence
     *  no range validation.  What recourse to take if the version found is actually
     *  outside the "valid range" is as yet an unsupported scenario.
     *
     * @param asVersion enum for the appserver version.
     */
    void internalSetAppServerVersion(ASDDVersion asVersion) {
        if (!asVersion.equals(appServerVersion)) {
            appServerVersion = asVersion;
            deferredAppServerChange = true;
        }
    }

    // !PW FIXME replace these with more stable version of equivalent functionality
    // once Vince or j2eeserver crew can implement a good api for this.
    // this code will NOT work for remote servers.
    private static String [] serverIds = {
        "pfv3ee6",
        "pfv4ee7",
        "pfv5ee8"
    };

    protected ASDDVersion getTargetAppServerVersion() {
        ASDDVersion result = null;
        J2eeModuleProvider provider = getProvider(primaryDD.getParentFile());
        if (null == provider) {
            return result;
        }
        String serverType = Utils.getInstanceReleaseID(provider); // provider.getServerInstanceID();
// [/tools/glassfish_b35]deployer:pfv5ee8:localhost:4948, serverType: J2EE
        if (Arrays.asList(serverIds).contains(serverType)) {
            // NOI18N
            String instance = provider.getServerInstanceID();
            if (Utils.notEmpty(instance)) {
                try {
                    String asInstallPath = instance.substring(1, instance.indexOf("]deployer:"));
                    if (asInstallPath.contains(File.pathSeparator)) 
                        asInstallPath = asInstallPath.substring(0, asInstallPath.indexOf(File.pathSeparator));
                    File asInstallFolder = new File(asInstallPath);
                    if (asInstallFolder.exists()) {
                        result = getInstalledAppServerVersion(asInstallFolder);
                    }
                } catch (IndexOutOfBoundsException ex) {
                    // Can't identify server install folder.
                    LOGGER.log(Level.WARNING, NbBundle.getMessage(PayaraConfiguration.class, "ERR_NoServerInstallLocation", instance)); // NOI18N
                } catch (NullPointerException ex) {
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        } else if ("SUNWebserver7".equals(serverType)) {
            // NOI18N
            result = ASDDVersion.SUN_APPSERVER_8_1;
        }

        return result;
    }

    protected ASDDVersion getInstalledAppServerVersion(File asInstallFolder) {
        return getInstalledAppServerVersionFromDirectory(asInstallFolder);
    }
    
    static ASDDVersion getInstalledAppServerVersionFromDirectory(File asInstallFolder) {
        File dtdFolder = new File(asInstallFolder, "lib/dtds/"); // NOI18N
        if (dtdFolder.exists()) {
            if (new File(dtdFolder, "payara-web-app_4.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_10_1;
            }
            if (new File(dtdFolder, "glassfish-web-app_3_0-1.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_10_1;
            }
            if (new File(dtdFolder, "sun-web-app_3_0-0.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_10_0;
            }
            if (new File(dtdFolder, "sun-domain_1_3.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_9_0;
            }
            if (new File(dtdFolder, "sun-domain_1_2.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_9_0;
            }
            if (new File(dtdFolder, "sun-domain_1_1.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_8_1;
            }
            if (new File(dtdFolder, "sun-domain_1_0.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_7_0;
            }
        }

        return null;
    }

    // ------------------------------------------------------------------------
    // Access to V2/V3 specific information.  Allows for graceful deprecation
    // of unsupported features (e.g. CMP, etc.)
    // ------------------------------------------------------------------------

    protected void createDefaultSunDD(File sunDDFile) throws IOException {
        FileObject sunDDTemplate = Utils.getSunDDFromProjectsModuleVersion(module, sunDDFile.getName()); //FileUtil.getConfigFile(resource);
        if (sunDDTemplate != null) {
            FileObject configFolder = FileUtil.createFolder(sunDDFile.getParentFile());
            FileSystem fs = configFolder.getFileSystem();
            XmlFileCreator creator = new XmlFileCreator(sunDDTemplate, configFolder, sunDDTemplate.getName(), sunDDTemplate.getExt());
            fs.runAtomicAction(creator);
        }
    }

    public J2eeModule getJ2eeModule() {
        return module;
    }

    public J2EEBaseVersion getJ2eeVersion() {
        return J2EEBaseVersion.getVersion(module.getType(), module.getModuleVersion());
    }

    final public org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = null;
        J2eeModuleHelper j2eeModuleHelper = J2eeModuleHelper.getSunDDModuleHelper(module.getType());
        if(j2eeModuleHelper != null) {
            stdRootDD = j2eeModuleHelper.getStandardRootDD(module);
        }
        return stdRootDD;
    }

    final public Webservices getWebServicesRootDD() {
        Webservices wsRootDD = null;
        J2eeModuleHelper j2eeModuleHelper = J2eeModuleHelper.getSunDDModuleHelper(module.getType());
        if(j2eeModuleHelper != null) {
            wsRootDD = j2eeModuleHelper.getWebServicesRootDD(module);
        }
        return wsRootDD;
    }

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        return module.getMetadataModel(type);
    }

    /** !PW FIXME web free form project does not implement J2eeModulePrvoider so
     *  this method will fail for that project type.  This method is used for:
     *
     *  * Getting the server instance id => install location for determining
     *    server version.
     *  * Getting the deployment manager => accessing the ResourceConfigurator
     *    and CMP Mapper (V2 only).
     *
     */
    protected J2eeModuleProvider getProvider(File file) {
        J2eeModuleProvider provider = null;
        if (file != null) {
            file = FileUtil.normalizeFile(file);
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    org.openide.util.Lookup lookup = project.getLookup();
                    provider = lookup.lookup(J2eeModuleProvider.class);
                }
            } else {
                File parent = file.getParentFile();
                if (parent != null) {
                    provider = getProvider(parent);
                }
            }
        }
        return provider;
    }

    // ------------------------------------------------------------------------
    // J2EE 1.4 Automatic Descriptor updating support.
    //
    // Exposed as api outside this package only because CMP related change
    // listeners need to be injected from j2ee.sun.ddui module and handled
    // by SunONEDeploymentConfiguration instances only.
    // ------------------------------------------------------------------------
    public enum ChangeOperation { CREATE, DELETE };

    void updateDefaultEjbJndiName(final String ejbName, final String prefix, final ChangeOperation op) {
        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, op == ChangeOperation.CREATE);

            if(primaryDDFO != null) {
                boolean changed = false;
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                if (ddRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if(eb == null && op == ChangeOperation.CREATE) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    if(eb != null) {
                        Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                        if(ejb == null && op == ChangeOperation.CREATE) {
                            ejb = eb.newEjb();
                            ejb.setEjbName(ejbName);
                            eb.addEjb(ejb);
                        }

                        if(ejb != null) {
                            assert ejbName.equals(ejb.getEjbName());

                            String defaultJndiName = ejbName.startsWith(prefix) ? ejbName : (prefix + ejbName);
                            if(op == ChangeOperation.CREATE && Utils.strEmpty(ejb.getJndiName())) {
                                ejb.setJndiName(defaultJndiName);
                                changed = true;
                            } else if(op == ChangeOperation.DELETE && Utils.strEquals(defaultJndiName, ejb.getJndiName())) {
                                ejb.setJndiName(null);
                                if(ejb.isTrivial(Ejb.EJB_NAME)) {
                                    eb.removeEjb(ejb);
                                    if(eb.isTrivial(null)) {
                                        sunEjbJar.setEnterpriseBeans(null);
                                    }
                                }
                                changed = true;
                            }
                        }
                    }
                }

                if(changed) {
                    ddRoot.write(primaryDDFO);
                }
            }
        } catch(IOException ex) {
            handleEventRelatedIOException(ex);
        } catch(Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    /**
     * Set a default Endpoint Address URI for the specified ejb hosted endpoint.
     */
    void updateDefaultEjbEndpointUri(final String linkName, final String portName, final ChangeOperation op) {
        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, true);

            if(primaryDDFO != null) {
                boolean changed = false;
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                if (ddRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if(eb == null && op == ChangeOperation.CREATE) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    if(eb != null) {
                        Ejb ejb = findNamedBean(eb, linkName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                        if(ejb == null && op == ChangeOperation.CREATE) {
                            ejb = eb.newEjb();
                            ejb.setEjbName(linkName);
                            eb.addEjb(ejb);
                        }

                        if(ejb != null) {
                            assert linkName.equals(ejb.getEjbName());

                            WebserviceEndpoint endpoint = findNamedBean(ejb, portName, Ejb.WEBSERVICE_ENDPOINT,
                                    WebserviceEndpoint.PORT_COMPONENT_NAME);
                            if(endpoint == null && op == ChangeOperation.CREATE) {
                                endpoint = ejb.newWebserviceEndpoint();
                                endpoint.setPortComponentName(portName);
                                ejb.addWebserviceEndpoint(endpoint);
                            }

                            if(endpoint != null) {
                                assert portName.equals(endpoint.getPortComponentName());

                                if(op == ChangeOperation.CREATE && Utils.strEmpty(endpoint.getEndpointAddressUri())) {
                                    String defaultUri = portName;
                                    endpoint.setEndpointAddressUri(defaultUri);
                                    changed = true;
                                } else if(op == ChangeOperation.DELETE) {
                                    endpoint.setEndpointAddressUri(null);
                                    if(endpoint.isTrivial(WebserviceEndpoint.PORT_COMPONENT_NAME)) {
                                        ejb.removeWebserviceEndpoint(endpoint);
                                        if(ejb.isTrivial(Ejb.EJB_NAME)) {
                                            eb.removeEjb(ejb);
                                            if(eb.isTrivial(null)) {
                                                sunEjbJar.setEnterpriseBeans(null);
                                            }
                                        }
                                    }
                                    changed = true;
                                }
                            }
                        }
                    }
                }

                if(changed) {
                    ddRoot.write(primaryDDFO);
                }
            }
        } catch(IOException ex) {
            handleEventRelatedIOException(ex);
        } catch(Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    // ------------------------------------------------------------------------
    // J2EE Server API implementations
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Implementation of ContextRootConfiguration
    // ------------------------------------------------------------------------
    @Override
    public String getContextRoot() throws ConfigurationException {
        // assume the return value will be the default CR
        String contextRoot = defaultcr;
        if (J2eeModule.Type.WAR.equals(module.getType())) {
            try {
                RootInterface rootDD = getPayaraDDRoot(false);
                if (rootDD instanceof SunWebApp) {
                    // read the value of CR out of the DD file.
                    contextRoot = ((SunWebApp) rootDD).getContextRoot();
                    if((contextRoot != null) && (contextRoot.equals("/"))) { //NOI18N
                        contextRoot = ""; //NOI18N
                    } else if (null == contextRoot) {
                        // if there wasn't a value for context-root in the file... use the default
                        contextRoot = defaultcr;
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                String defaultMessage = " retrieving context-root from sun-web.xml";
                displayError(ex, defaultMessage);
            }
        } else {
            LOGGER.log(Level.WARNING,
                    "PayaraConfiguration.getContextRoot() invoked on incorrect module type: {0}",
                    module.getType());
        }
        return contextRoot;
    }
    
    private static final RequestProcessor RP = new RequestProcessor("PayaraConfiguration.setContextRoot");

    @Override
    public void setContextRoot(final String contextRoot) throws ConfigurationException {
        try {
            if (J2eeModule.Type.WAR.equals(module.getType())) {
                if (null != defaultcr && defaultcr.equals(contextRoot)) {
                    // remove the context-root entry from the DD file... if it exists
                    final FileObject sunDDFO = getPayaraDD(primaryDD, false);
                    if (null != sunDDFO) {
                        // remove the context-root element from the file
                        RP.post(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (sunDDFO != null) {
                                        RootInterface rootDD = PayaraDDProvider.getDefault().getDDRoot(sunDDFO);
                                        if (rootDD instanceof SunWebApp) {
                                            SunWebApp swa = (SunWebApp) rootDD;
                                            swa.setContextRoot(null);
                                            swa.write(sunDDFO);
                                        }
                                    }
                                } catch (IOException ex) {
                                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                                    String defaultMessage = " trying set context-root in sun-web.xml";
                                    displayError(ex, defaultMessage);
                                } catch (Exception ex) {
                                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                                    String defaultMessage = " trying set context-root in sun-web.xml";
                                    displayError(ex, defaultMessage);
                                }
                            }
                        });
                    }
                } else {
                    // create the DD file and set the value of the context-root element
                    final FileObject sunDDFO = getPayaraDD(primaryDD, true);
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                if (sunDDFO != null) {
                                    RootInterface rootDD = PayaraDDProvider.getDefault().getDDRoot(sunDDFO);
                                    if (rootDD instanceof SunWebApp) {
                                        SunWebApp swa = (SunWebApp) rootDD;
                                        if (contextRoot == null || contextRoot.trim().length() == 0) {
                                            swa.setContextRoot("/"); //NOI18N
                                        } else {
                                            swa.setContextRoot(contextRoot);
                                        }
                                        swa.write(sunDDFO);
                                    }
                                }
                            } catch (IOException ex) {
                                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                                String defaultMessage = " trying set context-root in sun-web.xml";
                                displayError(ex, defaultMessage);
                            } catch (Exception ex) {
                                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                                String defaultMessage = " trying set context-root in sun-web.xml";
                                displayError(ex, defaultMessage);
                            }
                        }
                    });
                }
            } else {
                LOGGER.log(Level.WARNING,  // NOI18N
                        "PayaraConfiguration.setContextRoot() invoked on incorrect module type: {0}",  // NOI18N
                        module.getType());
            }
        } catch (IOException ex) {
            throw new ConfigurationException("", ex);  // NOI18N
        }
    }


    // ------------------------------------------------------------------------
    // Implementation of DatasourceConfiguration
    // ------------------------------------------------------------------------
    @Override
    public abstract Set<Datasource> getDatasources() throws ConfigurationException;

    @Override
    public abstract boolean supportsCreateDatasource();

    @Override
    public abstract Datasource createDatasource(String jndiName, String url,
            String username, String password, String driver)
            throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException;

    @Override
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, true);
            if (primaryDDFO != null) {
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                ResourceRef ref = findNamedBean(ddRoot, referenceName, SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                if (ref != null) {
                    // set jndi name of existing reference.
                    assert referenceName.equals(ref.getResRefName());
                    ref.setJndiName(jndiName);
                } else {
                    // add new resource-ref
                    if (ddRoot instanceof SunWebApp) {
                        ref = ((SunWebApp) ddRoot).newResourceRef();
                    } else if (ddRoot instanceof SunApplicationClient) {
                        ref = ((SunApplicationClient) ddRoot).newResourceRef();
                    }
                    if (null != ref) {
                        ref.setResRefName(referenceName);
                        ref.setJndiName(jndiName);
                        ddRoot.addValue(SunWebApp.RESOURCE_REF, ref);
                    }
                }

                // if changes, save file.
                ddRoot.write(primaryDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    @Override
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(ejbType) ||
                Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, true);
            if (primaryDDFO != null) {
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                if (ddRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb == null) {
                        ejb = eb.newEjb();
                        ejb.setEjbName(ejbName);
                        eb.addEjb(ejb);
                    }

                    ResourceRef ref = findNamedBean(ejb, referenceName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                    if (ref != null) {
                        // set jndi name of existing reference.
                        assert referenceName.equals(ref.getResRefName());
                        ref.setJndiName(jndiName);
                    } else {
                        // add new resource-ref
                        ref = ejb.newResourceRef();
                        ref.setResRefName(referenceName);
                        ref.setJndiName(jndiName);
                        ejb.addValue(Ejb.RESOURCE_REF, ref);
                    }

                    // if changes, save file.
                    sunEjbJar.write(primaryDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    @Override
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName)) {
            return null;
        }

        String jndiName = null;
        try {
            RootInterface ddRoot = getPayaraDDRoot(false);
            ResourceRef ref = findNamedBean(ddRoot, referenceName, SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
            if (ref != null) {
                // get jndi name of existing reference.
                assert referenceName.equals(ref.getResRefName());
                jndiName = ref.getJndiName();
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }

        return jndiName;    }

    @Override
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(referenceName)) {
            return null;
        }

        String jndiName = null;
        try {
            RootInterface ddRoot = getPayaraDDRoot(false);
            if (ddRoot instanceof SunEjbJar) {
                SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if (eb != null) {
                    Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb != null) {
                        ResourceRef ref = findNamedBean(ejb, referenceName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                        if (ref != null) {
                            // get jndi name of existing reference.
                            assert referenceName.equals(ref.getResRefName());
                            jndiName = ref.getJndiName();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }

        return jndiName;
    }


    // ------------------------------------------------------------------------
    // Implementation of EjbResourceConfiguration
    // ------------------------------------------------------------------------
    @Override
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName)) {
            return null;
        }

        String jndiName = null;
        try {
            RootInterface ddRoot = getPayaraDDRoot(false);
            if (ddRoot instanceof SunEjbJar) {
                SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if (eb != null) {
                    Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb != null) {
                        // get jndi name of existing reference.
                        assert ejbName.equals(ejb.getEjbName());
                        jndiName = ejb.getJndiName();
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionReadingEjb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionReadingEjb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }

        return jndiName;
    }

    @Override
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        // Version > 2.4, then return, but we can't compare directly against 2.4
        // because FP formats are not exact.
        //
        // !PW this appears to be overloaded logic in that it's differentiating
        // servlet 2.4 from servlet 2.5 and also appclient 1.4 from appclient 5.0,
        // hence the odd usage of "2.45" in the comparison.
        //
        try {
            if (Double.parseDouble(module.getModuleVersion()) > 2.45) {
                return;
            }
        } catch(NumberFormatException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }

        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, true);
            if (primaryDDFO != null) {
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                EjbRef ref = findNamedBean(ddRoot, referenceName, SunWebApp.EJB_REF, EjbRef.EJB_REF_NAME);
                if (ref != null) {
                    // set jndi name of existing reference.
                    assert referenceName.equals(ref.getEjbRefName());
                    ref.setJndiName(jndiName);
                } else {
                    // add new ejb-ref
                    if (ddRoot instanceof SunWebApp) {
                        ref = ((SunWebApp) ddRoot).newEjbRef();
                    } else if (ddRoot instanceof SunApplicationClient) {
                        ref = ((SunApplicationClient) ddRoot).newEjbRef();
                    }
                    if (ref != null) {
                        ref.setEjbRefName(referenceName);
                        ref.setJndiName(jndiName);
                        ddRoot.addValue(SunWebApp.EJB_REF, ref);
                    }
                }

                // if changes, save file.
                ddRoot.write(primaryDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    @Override
    public void bindEjbReferenceForEjb(String ejbName, String ejbType, String referenceName,
            String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(ejbType) ||
                Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        // Version > 2.1, then return, but we can't compare directly against 2.1
        // because FP formats are not exact.
        try {
            if (Double.parseDouble(module.getModuleVersion()) > 2.15) {
                return;
            }
        } catch(NumberFormatException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }

        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, true);
            if (primaryDDFO != null) {
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                if (ddRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb == null) {
                        ejb = eb.newEjb();
                        ejb.setEjbName(ejbName);
                        eb.addEjb(ejb);
                    }

                    EjbRef ref = findNamedBean(ejb, referenceName, Ejb.EJB_REF, EjbRef.EJB_REF_NAME);
                    if (ref != null) {
                        // set jndi name of existing reference.
                        assert referenceName.equals(ref.getEjbRefName());
                        ref.setJndiName(jndiName);
                    } else {
                        // add new ejb-ref
                        ref = ejb.newEjbRef();
                        ref.setEjbRefName(referenceName);
                        ref.setJndiName(jndiName);
                        ejb.addValue(Ejb.EJB_REF, ref);
                    }

                    // if changes, save file.
                    sunEjbJar.write(primaryDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }


    // ------------------------------------------------------------------------
    // Implementation of MessageDestinationConfiguration
    // ------------------------------------------------------------------------
    @Override
    public abstract Set<MessageDestination> getMessageDestinations() throws ConfigurationException;

    @Override
    public abstract boolean supportsCreateMessageDestination();

    @Override
    public abstract MessageDestination createMessageDestination(String name, Type type)
            throws UnsupportedOperationException, ConfigurationException;

    @Override
    public void bindMdbToMessageDestination(String mdbName, String name, Type type) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(mdbName) || Utils.strEmpty(name)) {
            return;
        }

        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, true);
            if (primaryDDFO != null) {
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                if (ddRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }
                    Ejb ejb = findNamedBean(eb, mdbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb == null) {
                        ejb = eb.newEjb();
                        ejb.setEjbName(mdbName);
                        eb.addEjb(ejb);
                    }
                    ejb.setJndiName(name);
                    String factory = name + "Factory"; //NOI18N
                    MdbConnectionFactory connFactory = ejb.newMdbConnectionFactory();
                    connFactory.setJndiName(factory);
                    ejb.setMdbConnectionFactory(connFactory);
//                    /* I think the following is not needed. These entries are being created through
//                     * some other path - Peter
//                     */
//                    org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination destination =
//                            findNamedBean(eb, mdbName, EnterpriseBeans.MESSAGE_DESTINATION,
//                            org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination.JNDI_NAME);
//                    if (destination == null) {
//                        destination = eb.newMessageDestination();
//                        destination.setJndiName(name);
//                        eb.addMessageDestination(destination);
//                    }
                    // if changes, save file.
                    ddRoot.write(primaryDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    @Override
    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(mdbName)) {
            return null;
        }

        String destinationName = null;
        try {
            RootInterface ddRoot = getPayaraDDRoot(false);
            if(ddRoot instanceof SunEjbJar) {
                SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if (eb != null) {
                    Ejb ejb = findNamedBean(eb, mdbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb != null) {
                        assert mdbName.equals(ejb.getEjbName());
                        destinationName = ejb.getJndiName();
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
        return destinationName;
    }

    @Override
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName,
            String destName, Type type) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(connectionFactoryName) || Utils.strEmpty(destName)) {
            return;
        }

        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, true);
            if (primaryDDFO != null) {
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                MessageDestinationRef destRef = findNamedBean(ddRoot, referenceName,
                        SunWebApp.MESSAGE_DESTINATION_REF, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME);
                if (destRef != null) {
                    // set jndi name of existing reference.
                    assert referenceName.equals(destRef.getMessageDestinationRefName());
                    destRef.setJndiName(referenceName);
                } else {
                    // add new resource-ref
                    if (ddRoot instanceof SunWebApp) {
                        destRef = ((SunWebApp) ddRoot).newMessageDestinationRef();
                    } else if (ddRoot instanceof SunApplicationClient) {
                        destRef = ((SunApplicationClient) ddRoot).newMessageDestinationRef();
                    }
                    if (null != destRef) {
                        destRef.setJndiName(referenceName);
                        destRef.setMessageDestinationRefName(referenceName);
                        ddRoot.addValue(SunWebApp.MESSAGE_DESTINATION_REF, destRef);
                    }
                }

                ResourceRef factoryRef = findNamedBean(ddRoot, connectionFactoryName,
                        SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                if (factoryRef != null) {
                    // set jndi name of existing reference.
                    assert connectionFactoryName.equals(factoryRef.getResRefName());
                    factoryRef.setJndiName(connectionFactoryName);
                } else {
                    // add new resource-ref
                    if (ddRoot instanceof SunWebApp) {
                        factoryRef = ((SunWebApp) ddRoot).newResourceRef();
                    } else if (ddRoot instanceof SunApplicationClient) {
                        factoryRef = ((SunApplicationClient) ddRoot).newResourceRef();
                    }
                    if (null != factoryRef) {
                        factoryRef.setResRefName(connectionFactoryName);
                        factoryRef.setJndiName(connectionFactoryName);
                        ddRoot.addValue(SunWebApp.RESOURCE_REF, factoryRef);
                    }
                }

                // if changes, save file.
                ddRoot.write(primaryDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    @Override
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType, String referenceName,
            String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        try {
            FileObject primaryDDFO = getPayaraDD(primaryDD, true);
            if (primaryDDFO != null) {
                RootInterface ddRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
                SunEjbJar sunEjbJar = (SunEjbJar) ddRoot;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if (eb == null) {
                    eb = sunEjbJar.newEnterpriseBeans();
                    sunEjbJar.setEnterpriseBeans(eb);
                }
                Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                if (ejb == null) {
                    ejb = eb.newEjb();
                    ejb.setEjbName(ejbName);
                    eb.addEjb(ejb);
                }
                if ((org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION.equals(ejbType)) ||
                        (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY.equals(ejbType))) {
                    org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef ref =
                            findNamedBean(ejb, connectionFactoryName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                    if (ref != null) {
                        // set jndi name of existing reference.
                        assert referenceName.equals(ref.getResRefName());
                        ref.setJndiName(connectionFactoryName);
                    } else {
                        // add new resource-ref
                        ref = ejb.newResourceRef();
                        ref.setResRefName(connectionFactoryName);
                        ref.setJndiName(connectionFactoryName);
                        ejb.addResourceRef(ref);
                    }

                    MessageDestinationRef destRef = findNamedBean(ejb, referenceName,
                            Ejb.MESSAGE_DESTINATION_REF, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME);
                    if (destRef != null) {
                        // set jndi name of existing reference.
                        assert referenceName.equals(destRef.getMessageDestinationRefName());
                        destRef.setJndiName(referenceName);
                    } else {
                        destRef = ejb.newMessageDestinationRef();
                        destRef.setJndiName(referenceName);
                        destRef.setMessageDestinationRefName(referenceName);
                        ejb.addMessageDestinationRef(destRef);
                    }
                } else if(org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)){
                    ejb.setJndiName(referenceName);
                    MdbConnectionFactory connFactory = ejb.newMdbConnectionFactory();
                    connFactory.setJndiName(connectionFactoryName);
                    ejb.setMdbConnectionFactory(connFactory);
                    org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination destination =
                            findNamedBean(eb, referenceName, EnterpriseBeans.MESSAGE_DESTINATION,
                            org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination.JNDI_NAME);
                    if (destination == null) {
                        destination = eb.newMessageDestination();
                        destination.setJndiName(referenceName);
                        eb.addMessageDestination(destination);
                    }
                }
                // if changes, save file.
                ddRoot.write(primaryDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(PayaraConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of DeploymentPlanConfiguration.
    //
    // save(OutputStream) is renamed due to conflict with JSR-88 method
    // DeploymentConfiguration.save(OutputStream).  Differentiation between
    // these two methods is performed by ModuleConfigurationImpl class.
    // ------------------------------------------------------------------------
    public void saveConfiguration(OutputStream outputStream) throws ConfigurationException {
        try {
            if (this.module.getType().equals(J2eeModule.Type.WAR)) {
                // copy sun-web.xml to stream directly.
                FileObject configFO = FileUtil.toFileObject(primaryDD);
                if(configFO != null) {
                    RootInterface rootDD = PayaraDDProvider.getDefault().getDDRoot(configFO);
                    rootDD.write(outputStream);
                }
            } else {
                LOGGER.log(Level.WARNING,
                        "Deployment plan not supported in PayaraConfiguration.save()");
            }
        } catch(Exception ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }


    // ------------------------------------------------------------------------
    // Internal implementation methods.
    // ------------------------------------------------------------------------

    /* ------------------------------------------------------------------------
     * Default descriptor file creation, root interface retrieval
     */
    // This method is only useful for reading the model.  If the model is to
    // be modified and rewritten to disk, you'll need the FileObject it was
    // retrieved from as well.
    protected RootInterface getPayaraDDRoot(boolean create) throws IOException {
        RootInterface payaraDDRoot = null;
        FileObject primaryDDFO = getPayaraDD(primaryDD, create);
        if (primaryDDFO != null) {
            payaraDDRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
        } else {
            FileObject secondaryDDFO = getPayaraDD(secondaryDD, create);
            if (secondaryDDFO != null) {
                payaraDDRoot = DDProvider.getDefault().getDDRoot(secondaryDDFO);
            }
        }
        return payaraDDRoot;
    }

    public RootInterface getPayaraDDRoot(File payaraDD, boolean create) throws IOException {
        RootInterface payaraDDRoot = null;
        FileObject primaryDDFO = getPayaraDD(payaraDD, create);
        if (primaryDDFO != null) {
            payaraDDRoot = PayaraDDProvider.getDefault().getDDRoot(primaryDDFO);
        }
        return payaraDDRoot;
    }

    protected FileObject getPayaraDD(File payaraDDFile, boolean create) throws IOException {
        if (!payaraDDFile.exists()) {
            if (create) {
                createDefaultSunDD(payaraDDFile);
            } else {
                return null;
            }
        }
        return FileUtil.toFileObject(payaraDDFile);
    }

    protected void displayError(Exception ex, String defaultMessage) {
        String message = ex.getLocalizedMessage();
        if(message == null || message.length() == 0) {
            message = ex.getClass().getSimpleName() + defaultMessage;
        }
        final NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(msg);
    }

    // Could have beanProp & nameProp in db indexed by Class<T>
    protected <T extends CommonDDBean> T findNamedBean(
            CommonDDBean parentDD, String referenceName, /*Class<T> c,*/ String beanProp, String nameProp) {
        T result = null;
        @SuppressWarnings("unchecked")
        T[] beans = (T[]) parentDD.getValues(beanProp);
        if (beans != null) {
            for (int i = 0; i < beans.length; i++) {
                String name = (String) beans[i].getValue(nameProp);
                if (referenceName.equals(name)) {
                    result = beans[i];
                    break;
                }
            }
        }
        return result;
    }

    void addDescriptorListener(FileObject target) {
        // Note: We don't use target to locate the genuine descriptor file.  We
        // lookup the descriptor file through proper channels and add a listener
        // to that result (which 99% of the time ought to be the same as what was
        // passed in here.  But there's that pesky 1% case.... bleah.)
        addDescriptorListener("webservices.xml".equals(target.getNameExt()) ?
            getWebServicesRootDD() : getStandardRootDD());
    }

    private void addDescriptorListener(org.netbeans.modules.j2ee.dd.api.common.RootInterface rootDD) {
        if(rootDD != null) {
            descriptorListener.addListener(rootDD);
        }
    }

    protected void handleEventRelatedIOException(IOException ex) {
        // This is a legitimate exception that could occur, such as a problem
        // writing the changed descriptor to disk.
        // !PW FIXME notify user
        // RR = could do handleEventRelatedException(ex) instead
        LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
    }

    protected void handleEventRelatedException(Exception ex) {
        // This would probably be a runtime exception due to a bug, but we
        // must trap it here so it doesn't cause trouble upstream.
        // We handle it the same as above for now.
        // !PW FIXME should we notify here, or just log?
        LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
    }

    // ------------------------------------------------------------------------
    // !PW FIXME workaround for linking sun descriptor file DataObjects w/ the
    // correct Deployment Configuration object.  Key is primary File for configuration.
    // ------------------------------------------------------------------------
    private static final Object configurationMonitor = new Object();
    private static final WeakHashMap<File, WeakReference<PayaraConfiguration>> configurationMap =
            new WeakHashMap<File, WeakReference<PayaraConfiguration>>();

    public static void addConfiguration(File key, PayaraConfiguration config) {
        synchronized(configurationMonitor) {
            configurationMap.put(key, new WeakReference<PayaraConfiguration>(config));
        }
    }

    public static void removeConfiguration(File key) {
        synchronized(configurationMonitor) {
            configurationMap.remove(key);
        }
    }

    public static PayaraConfiguration getConfiguration(File key) {
        PayaraConfiguration config = null;
        WeakReference<PayaraConfiguration> ref = null;
        synchronized(configurationMonitor) {
            ref = configurationMap.get(key);
        }
        if (ref != null) {
            config = ref.get();
        }
        return config;
    }

}
