/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
