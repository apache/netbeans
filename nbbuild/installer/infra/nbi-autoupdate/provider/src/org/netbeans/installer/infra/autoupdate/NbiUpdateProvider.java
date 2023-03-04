/**
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

package org.netbeans.installer.infra.autoupdate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.installer.Installer;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.DateUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.util.NbBundle;

/**
 *
 */
public class NbiUpdateProvider implements UpdateProvider {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static UpdateProvider instance;
    
    public static synchronized UpdateProvider getInstance() {
        if (instance == null) {
            instance = new NbiUpdateProvider();
        }
        
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private final Logger logger = Logger.getLogger(LOGGER_NAME);
    
    private List<Product> products;
    private Map<String, UpdateItem> updateItems;
    
    // contructor ///////////////////////////////////////////////////////////////////
    public NbiUpdateProvider() {
        logger.setLevel(Level.FINEST);
        
        try {
            // initialize the local working directory
            logger.log(Level.FINE,
                    "Initializing the local working directory"); // NOI18N
            
            String localDirectoryPath = 
                    System.getProperty(LOCAL_DIRECTORY_PROPERTY);
            if (localDirectoryPath == null) {
                localDirectoryPath = DEFAULT_LOCAL_DIRECTORY;
            }
            
            final File localDirectory = new File(localDirectoryPath);
            
            logger.log(Level.FINE,
                    "    ... " + localDirectory); // NOI18N
            
            System.setProperty(
                Installer.LOCAL_DIRECTORY_PATH_PROPERTY,
                localDirectory.getAbsolutePath());
            
            // initialize the URIs for the remote registries (if any)
            logger.log(Level.FINE,
                    "Initializing the remote product registries URIs"); // NOI18N
            
            String remoteRegistriesUris =
                    System.getProperty(REMOTE_REGISTRIES_PROPERTY);
            if (remoteRegistriesUris != null) {
                remoteRegistriesUris.replace(
                        REMOTE_REGISTRIES_SEPARATOR_INLINE,
                        REMOTE_REGISTRIES_SEPARATOR_CORRECT);
                System.setProperty(
                        Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY,
                        remoteRegistriesUris);
                
                for (String uri: remoteRegistriesUris.split(
                        REMOTE_REGISTRIES_SEPARATOR_CORRECT)) {
                    logger.log(Level.FINE,
                            "    ... " + uri); // NOI18N
                }
            } else {
                logger.log(Level.FINE,
                        "    ... no remote registries"); // NOI18N
            }
            
            // initialize and load the NBI objects: download manager, registry and
            // wizard
            logger.log(Level.FINE,
                    "Initializing the NBI engine objects"); // NOI18N
            
            LogManager.setLogFile(new File(
                    localDirectory,
                    "log/" + DateUtils.getTimestamp() + ".log"));
            LogManager.start();
            
            final DownloadManager downloadManager = DownloadManager.getInstance();
            downloadManager.setLocalDirectory(localDirectory);
            downloadManager.setFinishHandler(UpdateProviderFinishHandler.INSTANCE);
            downloadManager.init();
            
            logger.log(Level.FINE,
                    "    ... download manager initialized"); // NOI18N
            
            final Registry registry = Registry.getInstance();
            
            registry.setLocalDirectory(localDirectory);
            registry.setFinishHandler(UpdateProviderFinishHandler.INSTANCE);
            registry.initializeRegistry(new Progress());
            
            logger.log(Level.FINE,
                    "    ... registry initialized"); // NOI18N
            
            final Wizard wizard = Wizard.getInstance();
            wizard.setFinishHandler(UpdateProviderFinishHandler.INSTANCE);
            wizard.getContext().put(registry);
            
            logger.log(Level.FINE,
                    "    ... wizard initialized"); // NOI18N
            
            // load the products list
            logger.log(Level.FINE,
                    "Loading the list of products"); // NOI18N
            
            products = registry.getProducts(registry.getTargetPlatform());
            
            logger.log(Level.FINE,
                    "    ... loaded " + products.size() + " products"); // NOI18N
            
            // build the list of UpdateItems
            logger.log(Level.FINE,
                    "Building the list of update items"); // NOI18N
            
            updateItems = new HashMap<String, UpdateItem>();
            for (Product product: products) {
                final String codename =
                        product.getUid() + "-" + product.getVersion();
                final UpdateLicense license = UpdateLicense.createUpdateLicense(
                        "",
                        product.getLogic().getLicense().getText());
                
                logger.log(Level.FINE,
                        "    ... adding " + product.getDisplayName() +  // NOI18N
                        "[" + product.getStatus() + "]"); // NOI18N
                
                // devise the version string which can be safely used by the 
                // autoupdate/module manager mechanism - it is based on Integers, 
                // thus will fail to work with Longs which NBI is using; we work 
                // around it by splitting big numbers in parts with a standard part 
                // length being 8 characters long; we also assume that the first 
                // four version components never get that big; e.g
                // 5.6.0.0.200706121400 becomes 5.6.0.0.20070612.1400
                String version = product.getVersion().toString();
                
                final int lastPartIndex = 
                        version.lastIndexOf(".") + 1;
                final String lastPart = 
                        version.substring(lastPartIndex);
                final int lastPartLength = 
                        lastPart.length();
                
                if (lastPartLength > 8) {
                    final StringBuilder lastPartBuilder = new StringBuilder();
                    
                    int i = 0;
                    for (; i < lastPartLength - 8; i += 8) {
                        lastPartBuilder.append(lastPart.substring(i, i + 8)).append(".");
                    }
                    lastPartBuilder.append(lastPart.substring(i, lastPartLength));
                    
                    version = version.substring(0, lastPartIndex) + 
                            lastPartBuilder.toString();
                }
                
                
                final UpdateItem updateItem;
                if (product.getStatus() == Status.INSTALLED) {
                    updateItem = UpdateItem.createInstalledNativeComponent(
                            codename, // codename
                            version, // version
                            // new HashSet<String>(), // dependencides
                            null, // dependencies
                            product.getDisplayName(), // display name
                            product.getDescription(), // description
                            new NbiCustomUninstaller(product)); // custom uninstaller
                } else {
                    updateItem = UpdateItem.createNativeComponent(
                            codename, // codename
                            version, // version
                            Long.toString(product.getDownloadSize()), // size
                            // new HashSet<String>(), // dependencides
                            null, // dependencies
                            product.getDisplayName(), // display name
                            product.getDescription(), // description
                            true, // needs restart
                            true, // is global
                            "extra", // target cluster
                            new NbiCustomInstaller(product), // custom installer
                            license); // license
                }
                
                updateItems.put(codename, updateItem);
            }
            
            logger.log(Level.FINE,
                    "    ... built successfully"); // NOI18N
            
        } catch (InitializationException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            
            updateItems = new HashMap<String, UpdateItem>();
        }
    }
    
    // updateprovider implementation
    public String getName() {
        return NAME;
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(NbiUpdateProvider.class, DISPLAY_NAME_KEY);
    }
    
    public String getDescription() {
        return NbBundle.getMessage(NbiUpdateProvider.class, DESCRIPTION_KEY);
    }
    
    public Map<String, UpdateItem> getUpdateItems() throws IOException {
        return updateItems;
    }
    
    public boolean refresh(final boolean refresh) throws IOException {
        return true;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class UpdateProviderFinishHandler implements FinishHandler {
        public static final UpdateProviderFinishHandler INSTANCE =
                new UpdateProviderFinishHandler();
        
        private UpdateProviderFinishHandler() {
            // does nothing
        }
        
        public void cancel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void finish() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void criticalExit() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LOGGER_NAME =
            "org.netbeans.installer.infra.autoupdate"; // NOI18N
    
    private static final String NAME =
            "nbi-update-provider"; // NOI18N
    
    private static final String DISPLAY_NAME_KEY =
            "NUP.display.name"; // NOI18N
    
    private static final String DESCRIPTION_KEY =
            "NUP.description"; // NOI18N
    
    private static final String LOCAL_DIRECTORY_PROPERTY =
            "nbi.local.directory"; // NOI18N
    
    private static final String DEFAULT_LOCAL_DIRECTORY =
            System.getProperty("user.home") + "/.nbi"; // NOI18N
    
    private static final String REMOTE_REGISTRIES_PROPERTY =
            "nbi.remote.product.registries";
    
    private static final String REMOTE_REGISTRIES_SEPARATOR_INLINE =
            ";";
    
    private static final String REMOTE_REGISTRIES_SEPARATOR_CORRECT =
            "\n";
}
