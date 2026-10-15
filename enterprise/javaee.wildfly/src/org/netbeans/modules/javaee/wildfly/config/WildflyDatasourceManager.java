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
package org.netbeans.modules.javaee.wildfly.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.config.ds.gen.DatasourceType;
import org.netbeans.modules.javaee.wildfly.config.ds.gen.Datasources;
import org.netbeans.modules.javaee.wildfly.config.ds.gen.DsSecurityType;
import org.netbeans.modules.javaee.wildfly.config.ds.gen.PoolType;
import org.netbeans.modules.javaee.wildfly.config.xml.ConfigurationParser;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Libor Kotouc
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public final class WildflyDatasourceManager implements DatasourceManager {

    private static final Logger LOGGER = Logger.getLogger(WildflyDatasourceManager.class.getName());

    private static final String DSdotXML = "-ds.xml"; // NOI18N

    private static final String JBossDSdotXML = "jboss-ds.xml"; // NOI18N

    private final FileObject deployDir;
    private final FileObject configFile;

    private final WildflyDeploymentManager dm;

    public WildflyDatasourceManager(WildflyDeploymentManager dm) {
        this.dm = dm;
        InstanceProperties ip = InstanceProperties.getInstanceProperties(dm.getUrl());
        String deployDirPath = ip.getProperty(WildflyPluginProperties.PROPERTY_DEPLOY_DIR);
        deployDir = FileUtil.toFileObject(new File(deployDirPath));
        String configFilePath = ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE);
        if(configFilePath != null) {
            File config = new File(configFilePath);
            if(config.exists()) {
                configFile = FileUtil.toFileObject(config);
            } else {
                throw new IllegalArgumentException("No configuration file found: " + configFilePath);
            }
        } else {
            throw new IllegalArgumentException("No configuration file configured: " + dm);
        }
    }

    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return ConfigurationParser.INSTANCE.listDatasources(configFile);
    }

    @Override
    public void deployDatasources(Set<Datasource> datasources)
            throws ConfigurationException, DatasourceAlreadyExistsException {
        Set<Datasource> deployedDS = getDatasources();
        Map<String, Datasource> ddsMap = transform(deployedDS); // for faster searching

        HashMap<String, Datasource> newDS = new HashMap<String, Datasource>(); // will contain all ds which do not conflict with existing ones

        //resolve all conflicts
        LinkedList<Datasource> conflictDS = new LinkedList<Datasource>();
        for (Iterator<Datasource> it = datasources.iterator(); it.hasNext();) {
            Object o = it.next();
            if (!(o instanceof WildflyDatasource)) {
                continue;
            }
            WildflyDatasource ds = (WildflyDatasource) o;
            String jndiName = WildflyDatasource.getRawName(ds.getJndiName());
            if (ddsMap.containsKey(jndiName)) { // conflicting ds found
                if (!ddsMap.get(jndiName).equals(ds)) { // found ds is not equal
                    conflictDS.add(ddsMap.get(jndiName)); // NOI18N
                }
            } else if (jndiName != null) {
                newDS.put(jndiName, ds);
            }
        }

        if (conflictDS.size() > 0) { // conflict found -> exception
            throw new DatasourceAlreadyExistsException(conflictDS);
        }

        //write jboss-ds.xml
        FileObject dsXmlFo = deployDir.getFileObject(JBossDSdotXML);
        File dsXMLFile = (dsXmlFo != null ? FileUtil.toFile(dsXmlFo) : null);

        Datasources deployedDSGraph = null;
        try {
            deployedDSGraph = (dsXMLFile != null ? Datasources.createGraph(dsXMLFile) : new Datasources());
        } catch (IOException ioe) {
            Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(getClass(), "ERR_CannotReadDSdotXml"));
            Logger.getLogger("global").log(Level.INFO, null, ioe);
            return;
        }

        //merge ds graph with newDS - remove conflicting ds from graph
        DatasourceType ltxds[] = deployedDSGraph.getDatasource();
        for (int i = 0; i < ltxds.length; i++) {
            String jndiName = ltxds[i].getJndiName();
            if (newDS.containsKey(jndiName)) //conflict, we must remove it from graph
            {
                deployedDSGraph.removeDatasource(ltxds[i]);
            }
        }

        //add all ds from newDS
        for (Iterator it = newDS.values().iterator(); it.hasNext();) {
            WildflyDatasource ds = (WildflyDatasource) it.next();

            DatasourceType lds = new DatasourceType();
            lds.setJndiName(WildflyDatasource.getRawName(ds.getJndiName()));
            lds.setConnectionUrl(ds.getUrl());
            lds.setDriverClass(ds.getDriverClassName());
            DsSecurityType security = new DsSecurityType();
            security.setUserName(ds.getUsername());
            security.setPassword(ds.getPassword());
            lds.setSecurity(security);
            PoolType pool = new PoolType();
            pool.setMinPoolSize(Long.parseLong(ds.getMinPoolSize()));
            pool.setMaxPoolSize(Long.parseLong(ds.getMaxPoolSize()));
            lds.setPool(pool);
            String poolName = ds.getJndiName();
            int index = ds.getJndiName().lastIndexOf('/');
            if(index > 0) {
                poolName = ds.getJndiName().substring(index);
            }
            lds.setPoolName(poolName);
            deployedDSGraph.addDatasource(lds);
        }

        //write modified graph into jboss-ds.xml
        if (newDS.size() > 0) {
            if (dsXMLFile == null) {
                try {
                    dsXmlFo = deployDir.createData(JBossDSdotXML);
                } catch (IOException ioe) {
                    Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(getClass(), "ERR_CannotCreateDSdotXml"));
                    Logger.getLogger("global").log(Level.INFO, null, ioe);
                    return;
                }

                dsXMLFile = FileUtil.toFile(dsXmlFo);
            }

            writeFile(dsXMLFile, deployedDSGraph);
        }
    }

    private Map<String, Datasource> transform(Set<Datasource> datasources) {
        HashMap<String, Datasource> map = new HashMap<String, Datasource>();
        for (Iterator it = datasources.iterator(); it.hasNext();) {
            WildflyDatasource ds = (WildflyDatasource) it.next();
            if (ds.getJndiName() != null) {
                map.put(ds.getJndiName(), ds);
            }
        }
        return map;
    }

    private void writeFile(final File file, final BaseBean bean) throws ConfigurationException {
        try {

            FileSystem fs = deployDir.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = deployDir.getFileObject(name);
                        if (configFO == null) {
                            configFO = deployDir.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream(configFO.getOutputStream(lock), 4096);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException ioe) {
                            }
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                }
            });
        } catch (IOException e) {
            throw new ConfigurationException(e.getLocalizedMessage());
        }
    }

}
