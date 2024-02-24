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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.JdbcChildrenFactory.JDBCDataBean;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UnregisterCookie;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class JdbcRetriever implements JdbcChildrenFactory.Retriever {

    private static final Logger LOGGER = Logger.getLogger(JdbcRetriever.class.getName());

    private static final String JDBC = "jdbc"; // NOI18N

    private static final int UNREGISTER_WAIT_TIME = 60000;

    private static final int UNREGISTER_TIMEOUT = 120000;

    private final AtomicReference<List<JDBCDataBean>> data = new AtomicReference<List<JDBCDataBean>>();

    private final Lookup lookup;

    private boolean isRetrieveStarted;

    public JdbcRetriever(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void clean() {
        data.set(null);
    }

    @Override
    public List<JDBCDataBean> get() {
        return data.get();
    }

    @Override
    public void retrieve() {
        synchronized (this) {
            if (isRetrieveStarted) {
                return;
            }
            isRetrieveStarted = true;
        }

        data.set(null);

        WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

        WLConnectionSupport support = manager.getConnectionSupport();
        List<JDBCDataBean> list = Collections.emptyList();

        try {
            list = support.executeAction(new WLConnectionSupport.
                    JMXRuntimeAction<List<JDBCDataBean>>() {

                @Override
                public List<JDBCDataBean> call(MBeanServerConnection con, ObjectName service) throws Exception {
                    List<JDBCDataBean> list = new LinkedList<JDBCDataBean>();

                    ObjectName[] adminServers = (ObjectName[]) con
                            .getAttribute(service, "ServerRuntimes"); // NOI18N
                    Set<String> adminNames = new HashSet<String>();
                    for (ObjectName adminServer : adminServers) {
                        adminNames.add(con.getAttribute(adminServer, "Name").toString()); // NOI18N
                    }

                    ObjectName config = (ObjectName) con.getAttribute(
                            service, "DomainConfiguration"); // NOI18N
                    findSystemJdbc(con, list, adminNames, config);
                    findDeployedJdbc( con, list , adminNames, config);
                    return list;
                }

                private void findDeployedJdbc( MBeanServerConnection con,
                        List<JDBCDataBean> list, Set<String> adminNames,
                        ObjectName config ) throws MBeanException,
                    AttributeNotFoundException, InstanceNotFoundException,
                    ReflectionException, IOException {

                    ObjectName applications[] = (ObjectName[]) con
                        .getAttribute(config, "AppDeployments"); // NOI18N
                    for (ObjectName application : applications) {
                        Object objType = con.getAttribute( application, "ModuleType"); // NOI18N
                        if (objType != null && JDBC.equals(objType.toString())) {
                            boolean foundAdminServer = false;
                            ObjectName[] targets = (ObjectName[]) con
                                .getAttribute(application, "Targets"); // NOI18N
                            for (ObjectName target : targets) {
                                String targetServer = con.getAttribute(
                                        target, "Name").toString(); // NOI18N
                                if (adminNames.contains(targetServer)) {
                                    foundAdminServer = true;
                                }
                            }
                            if (!foundAdminServer) {
                                continue;
                            }
                            String path = (String)con.getAttribute(application,
                                    "AbsoluteSourcePath"); // NOI18N
                            String name = (String)con.getAttribute(application,
                                    "Name"); // NOI18N
                            if (path != null) {
                                loadDeployedDataSource(path, list, name);
                            }
                        }
                    }

                }

                private void findSystemJdbc( MBeanServerConnection con,
                        List<JDBCDataBean> list, Set<String> adminNames,
                        ObjectName objectName ) throws MBeanException,
                    AttributeNotFoundException, InstanceNotFoundException,
                    ReflectionException, IOException {

                    ObjectName objectNames[] = (ObjectName[]) con
                            .getAttribute(objectName, "SystemResources"); // NOI18N

                    for (ObjectName resource : objectNames) {
                        String type = con.getAttribute(resource, "Type").toString();// NOI18N
                        if ("JDBCSystemResource".equals(type)) { // NOI18N
                            ObjectName dataSource = (ObjectName) con
                                    .getAttribute(resource, "JDBCResource"); // NOI18N
                            ObjectName[] targets = (ObjectName[]) con
                                    .getAttribute(resource, "Targets"); // NOI18N

                            String name = con.getAttribute(dataSource,
                                    "Name").toString(); // NOI18N
                            boolean foundAdminServer = false;
                            for (ObjectName target : targets) {
                                String targetServer = con.getAttribute(
                                        target, "Name").toString(); // NOI18N
                                if (adminNames.contains(targetServer)) {
                                    foundAdminServer = true;
                                }
                            }
                            if (!foundAdminServer) {
                                continue;
                            }

                            ObjectName dataSourceParams = (ObjectName) con
                                    .getAttribute(dataSource,
                                            "JDBCDataSourceParams"); // NOI18N
                            String jndiNames[] = (String[]) con
                                    .getAttribute(dataSourceParams,
                                            "JNDINames"); // NOI18N
                            JDBCDataBean bean = new JDBCDataBean(name,
                                    jndiNames);
                            list.add(bean);
                        }
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        } finally {
            data.compareAndSet(null, list);
            synchronized (this) {
                isRetrieveStarted = false;
                notifyAll();
            }
        }
    }

    @Override
    public void waitForCompletion() {
        synchronized (this) {
            while (isRetrieveStarted) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.FINE, null, e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void loadDeployedDataSource(String path, List<JDBCDataBean> list,
            String deplName) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            JdbcConfigHandler handler = new JdbcConfigHandler();
            FileObject jdbcConfig = FileUtil.toFileObject( FileUtil.
                    normalizeFile( new File(path)));
            if (jdbcConfig == null) {
                return;
            }
            parser.parse(new BufferedInputStream(
                    jdbcConfig.getInputStream()), handler);
            List<String> jndiNames = handler.getJndiNames();
            list.add( new JDBCDataBean( handler.getName(),
                    jndiNames.toArray(new String[0]), deplName));
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (SAXException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        }
    }

    public static class JdbcUnregisterFactory implements JdbcChildrenFactory.UnregisterFactory {

        @Override
        public UnregisterCookie createUnregisterForPool(String name, RefreshModulesCookie refresh, Lookup lookup) {
            return new UnregisterJdbcPool(name, refresh, lookup);
        }

        @Override
        public UnregisterCookie createUnregisterForResource(String name, RefreshModulesCookie refresh, Lookup lookup) {
            return new UnregisterJdbcJndiName(name, refresh, lookup);
        }

    }

    private static class JdbcConfigHandler extends DefaultHandler {

        private static final String DATA_SOURCE_PARAMS = "jdbc-data-source-params"; // NOI18N

        private final List<String> jndiNames = new LinkedList<String>();

        private final StringBuilder content = new StringBuilder();

        private String name;

        private boolean dataSourceParamsStarted;

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            content.setLength(0);
            if (DATA_SOURCE_PARAMS.equals(getUnprefixedName(qName))) {
                dataSourceParamsStarted = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (name == null && "name".equals(getUnprefixedName(qName))) { // NOI18N
                name = content.toString();
            } else if (DATA_SOURCE_PARAMS.equals(getUnprefixedName(qName))) { // NOI18N
                dataSourceParamsStarted = false;
            } else if ( dataSourceParamsStarted
                    && "jndi-name".equals(getUnprefixedName(qName))) { // NOI18N
                jndiNames.add(content.toString());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            content.append(ch, start, length);
        }

        String getName() {
            return name;
        }

        List<String> getJndiNames() {
            return jndiNames;
        }

        private String getUnprefixedName(String name) {
            if (name.contains(":")) { // NOI18N
                return name.substring(name.indexOf(":") + 1); // NOI18N
            } else {
                return name;
            }
        }
    }

    private static class UnregisterJdbcJndiName implements UnregisterCookie {

        private final String jndiName;

        private final RefreshModulesCookie cookie;

        private final Lookup lookup;

        UnregisterJdbcJndiName(String jndiName, RefreshModulesCookie cookie,
                Lookup lookup) {
            this.jndiName = jndiName;
            this.cookie = cookie;
            this.lookup = lookup;
        }

        @Override
        public void unregister() {
            WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

            WLConnectionSupport support = manager.getConnectionSupport();
            try {
                support.executeAction(new WLConnectionSupport.JMXEditAction<Void>() {

                    @Override
                    public Void call(MBeanServerConnection con, ObjectName service) throws Exception {
                        ObjectName config = (ObjectName) con.getAttribute(
                                service, "DomainConfiguration"); // NOI18N
                        ObjectName resources[] = (ObjectName[]) con
                                .getAttribute(config, "SystemResources"); // NOI18N

                        ObjectName manager = (ObjectName) con.getAttribute(
                                service, "ConfigurationManager"); // NOI18N
                        ObjectName domainConfigRoot = (ObjectName) con.invoke(
                                manager, "startEdit", new Object[] {
                                        UNREGISTER_WAIT_TIME, UNREGISTER_TIMEOUT }, new String[] {
                                "java.lang.Integer", "java.lang.Integer" });
                        if (domainConfigRoot == null) {
                            // Couldn't get the lock
                            throw new UnableLockException();
                        }

                        for (ObjectName resource : resources) {
                            String type = con
                                    .getAttribute(resource, "Type")
                                    .toString();// NOI18N
                            if ("JDBCSystemResource".equals(type)) { // NOI18N
                                ObjectName jdbcResource = (ObjectName) con
                                        .getAttribute(resource,
                                                "JDBCResource"); // NOI18N
                                ObjectName params = (ObjectName) con
                                        .getAttribute(jdbcResource,
                                                "JDBCDataSourceParams"); // NOI18N
                                con.invoke(params, "removeJNDIName",
                                        new Object[] { jndiName },
                                        new String[] { "java.lang.String" }); // NOI18N
                            }
                        }
                        con.invoke(manager, "save", null, null); // NOI18N
                        ObjectName activationTask = (ObjectName) con
                                .invoke(manager, "activate",
                                        new Object[] { UNREGISTER_TIMEOUT },
                                        new String[] { "java.lang.Long" }); // NOI18N
                        con.invoke(activationTask, "waitForTaskCompletion", null, null);
                        return null;
                    }


                });
            } catch (UnableLockException e) {
                failNotify();
            } catch (MBeanException e) {
                Exception targetException = e.getTargetException();
                if ( targetException.getClass().getCanonicalName().equals(
                        "weblogic.management.mbeanservers.edit.EditTimedOutException")) {
                    failNotify();
                }
            } catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
            cookie.refresh();
        }

        private void failNotify(){
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(JdbcRetriever.class, "MSG_UnableUnregister"),
                    NotifyDescriptor.ERROR_MESSAGE );
            DialogDisplayer.getDefault().notify(notDesc);
        }
    }

    private static class UnregisterJdbcPool implements UnregisterCookie {

        private final String resourceName;

        private final RefreshModulesCookie cookie;

        private final Lookup lookup;

        UnregisterJdbcPool(String name, RefreshModulesCookie cookie, Lookup lookup) {
            this.resourceName = name;
            this.cookie = cookie;
            this.lookup = lookup;
        }

        @Override
        public void unregister() {
            WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

            WLConnectionSupport support = manager.getConnectionSupport();
            try {
                support.executeAction(new WLConnectionSupport.JMXEditAction<Void>() {

                    @Override
                    public Void call(MBeanServerConnection con, ObjectName service) throws Exception {
                        StringBuilder dataSourceCanonicalName = new StringBuilder(
                                "com.bea:Name="); // NOI18N
                        dataSourceCanonicalName.append(resourceName);
                        dataSourceCanonicalName
                                .append(",Type=JDBCSystemResource");// NOI18N
                        ObjectName dataSourceBean = new ObjectName(
                                dataSourceCanonicalName.toString());
                        disable(con, service, dataSourceBean);
                        return null;
                    }

                    private void disable( MBeanServerConnection connection,
                            ObjectName service, ObjectName dataSourceBean)
                            throws AttributeNotFoundException,
                        InstanceNotFoundException, MBeanException, ReflectionException,
                        IOException, MalformedObjectNameException, UnableLockException {

                        ObjectName manager =(ObjectName) connection.getAttribute(service,
                                        "ConfigurationManager");                // NOI18N
                        ObjectName domainConfigRoot = (ObjectName)connection.invoke(manager,
                                "startEdit", new Object[]{ UNREGISTER_WAIT_TIME, UNREGISTER_TIMEOUT},
                                    new String[]{ "java.lang.Integer", "java.lang.Integer"});
                        if ( domainConfigRoot == null ){
                         // Couldn't get the lock
                            throw new UnableLockException();
                        }

                        try {
                            ObjectName targets[]  = (ObjectName[]) connection.getAttribute(
                                dataSourceBean, "Targets"); // NOI18N
                            for (ObjectName target : targets) {
                                connection
                                        .invoke(dataSourceBean,
                                                "removeTarget",
                                                new Object[] { target },
                                                new String[] { "javax.management.ObjectName" }); // NOI18N
                            }
                        } catch( InstanceNotFoundException e) {
                            /*
                             *  This is not system config JDBC resource bean. This is
                             *  deployed JDBC resource  .
                             */
                            StringBuilder deploymentCanonicalName =
                                new StringBuilder("com.bea:Name=");       // NOI18N
                            deploymentCanonicalName.append( resourceName);
                            deploymentCanonicalName
                                .append(",Type=AppDeployment");             // NOI18N
                            ObjectName application = new ObjectName(
                                    deploymentCanonicalName.toString());
                            ObjectName targets[]  = (ObjectName[]) connection.getAttribute(
                                    application, "Targets"); // NOI18N
                            for (ObjectName target : targets) {
                                connection
                                        .invoke(application,
                                                "removeTarget",
                                                new Object[] { target },
                                                new String[] { "javax.management.ObjectName" }); // NOI18N
                            }
                        }

                        connection.invoke(manager, "save", null, null);                // NOI18N
                        ObjectName  activationTask = (ObjectName)connection.invoke(manager,
                                "activate", new Object[]{UNREGISTER_TIMEOUT},
                                    new String[]{"java.lang.Long"});                // NOI18N
                        connection.invoke(activationTask, "waitForTaskCompletion", null, null);
                    }
                });
            }
            catch (UnableLockException e) {
                failNotify();
            }
            catch (MBeanException e) {
                Exception targetException = e.getTargetException();
                if ( targetException.getClass().getCanonicalName().equals(
                        "weblogic.management.mbeanservers.edit.EditTimedOutException")) {
                    failNotify();
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
            cookie.refresh();
        }

        private void failNotify(){
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(JdbcRetriever.class, "MSG_UnableUnregister"),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(notDesc);
        }
    }

    private static class UnableLockException extends Exception {

        private static final long serialVersionUID = 1491526792800773444L;

    }
}
