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
package org.netbeans.modules.glassfish.common.parser;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.PortCollection;
import org.netbeans.modules.glassfish.common.utils.Util;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 * Listens for changes in GlassFish configuration file <code>domain.xml</code>.
 * <p/>
 * @author Tomas Kraus
 */
public class DomainXMLChangeListener implements FileChangeListener {

    /**
     * Register this listener for GlassFish instance configuration file
     * <code>domain.xml</code> changes.
     * <p/>
     * @param instance GlassFish server instance.
     * @return GlassFish configuration file <code>domain.xml</code> changes
     *         listener created and registered.
     */
    public static void registerListener(
            GlassfishInstance instance) {
        String domainDirPath = instance.getDomainsFolder();
        String domainName = instance.getDomainName();
        String domainXMLName = org.netbeans.modules.glassfish.tooling.utils.ServerUtils
                .getDomainConfigFile(domainDirPath, domainName);
        File configPAth = FileUtil.normalizeFile(new File(domainXMLName));
        FileUtil.addFileChangeListener(
                instance.getDomainXMLChangeListener(), configPAth);
    }

    /**
     * Register this listener for GlassFish instance configuration file
     * <code>domain.xml</code> changes.
     * <p/>
     * @param instance GlassFish server instance.
     */
    public static void unregisterListener(GlassfishInstance instance) {
        String domainDirPath = instance.getDomainsFolder();
        String domainName = instance.getDomainName();
        String domainXMLName = org.netbeans.modules.glassfish.tooling.utils.ServerUtils
                .getDomainConfigFile(domainDirPath, domainName);
        File configPAth = FileUtil.normalizeFile(new File(domainXMLName));
        FileUtil.removeFileChangeListener(
                instance.getDomainXMLChangeListener(), configPAth);
    }

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(DomainXMLChangeListener.class);

    /** GlassFish server instance. */
    private final GlassfishInstance instance;

    /** GlassFish configuration file <code>domain.xml</code> full path. */
    private final String path;

    /**
     * Constructs an instance of GlassFish configuration file
     * <code>domain.xml</code> changes listener.
     * <p/>
     * This is being called from GlassFish server instance constructor.
     * Content of <code>instance</code> may not be fully initialized yet.
     * <p/>
     * @param instance GlassFish server instance.
     * @param domainXML GlassFish configuration file <code>domain.xml</code>
     *             full path.
     */
    public DomainXMLChangeListener(final GlassfishInstance instance,
            final String domainXML) {
        this.instance = instance;
        this.path = domainXML;
    }

    /**
     * Fired when a new folder is created.
     * <p/>
     * Shall not happen.
     * <p/>
     * @param fe The event describing context where action has taken place.
     */
    @Override
    public void fileFolderCreated(FileEvent fe) {
        LOGGER.log(Level.WARNING,
                "GlassFish configuration file {0} seems to be a folder!", path);
    }

    /**
     * Fired when a new file is created.
     * <p/>
     * Shall not happen.
     * <p/>
     * @param fe The event describing context where action has taken place.
     */
    @Override
    public void fileDataCreated(FileEvent fe) {
        LOGGER.log(Level.WARNING,
                "GlassFish configuration file {0} seems to be a folder!", path);
    }

    /**
     * Fired when <code>domain.xml</code> file is changed.
     * <p/>
     * Notification about GlassFish configuration change.
     * <p/>
     * @param fe The event describing context where action has taken place.
     */
    @Override
    public void fileChanged(FileEvent fe) {
        File domainDir = new File(
                instance.getDomainsFolder(), instance.getDomainName());
        PortCollection pc = new PortCollection();
        if (Util.readServerConfiguration(domainDir, pc)) {
        LOGGER.log(Level.INFO,
                "GlassFish configuration file {0} was modified, "
                + "updating server configuration.", path);
            instance.setHttpPort(pc.getHttpPort());
            instance.setAdminPort(pc.getAdminPort());
        } else {
            LOGGER.log(Level.INFO,
                    "GlassFish configuration file {0} was modified "
                    +"but configuration update failed.", path);
        }
    }

    /**
     * Fired when <code>domain.xml</code> file is deleted.
     * <p/>
     * Notification about GlassFish configuration file removal.
     * <p/>
     * @param fe The event describing context where action has taken place.
     */
    @Override
    public void fileDeleted(FileEvent fe) {
        LOGGER.log(Level.FINE,
                "GlassFish configuration file {0} was deleted.", path);
    }

    /**
     * Fired when <code>domain.xml</code> file is renamed.
     * <p/>
     * Notification about GlassFish configuration file name change.
     * <p/>
     * @param fe The event describing context where action has taken place
     *           and the original name and extension.
     */
    @Override
    public void fileRenamed(FileRenameEvent fe) {
        LOGGER.log(Level.FINE,
                "GlassFish configuration file {0} was renamed.", path);
    }

    /**
     * Fired when <code>domain.xml</code> attribute is changed.
     * <p/>
     * Notification about GlassFish configuration file attribute change.
     * <p/>
     * @param fe The event describing context where action has taken place,
    *           the name of attribute and the old and new values.
     */
    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        LOGGER.log(Level.FINE,
                "GlassFish configuration file {0} attributes were changed.",
                path);
    }
    
}
