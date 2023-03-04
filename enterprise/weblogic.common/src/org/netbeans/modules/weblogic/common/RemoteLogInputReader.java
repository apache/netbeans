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

package org.netbeans.modules.weblogic.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputReader;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class RemoteLogInputReader implements InputReader {

    private static final Logger LOGGER = Logger.getLogger(RemoteLogInputReader.class.getName());

    private static final int INITIAL_LOG_COUNT = 300;

    private static final int POLL_INTERVAL = 2000;

    private final WebLogicConfiguration config;

    private final Callable<String> nonProxy;

    // GuardeBy("this")
    private final StringBuilder builder = new StringBuilder();

    // GuardeBy("this")
    private RequestProcessor worker;

    private volatile boolean closed;

    public RemoteLogInputReader(WebLogicConfiguration config, Callable<String> nonProxy) {
        this.config = config;
        this.nonProxy = nonProxy;
    }

    @Override
    public int readInput(InputProcessor processor) throws IOException {
        if (closed) {
            throw new IllegalStateException("Already closed reader");
        }

        synchronized (this) {
            if (worker == null) {
                worker = new RequestProcessor(RemoteLogInputReader.class);
                worker.post(new Worker());
            } else {
                int length = builder.length();
                if (length > 0) {
                    if (processor != null) {
                        processor.processInput(builder.toString().toCharArray());
                    }
                    builder.setLength(0);
                    return length;
                }
            }
        }
        return 0;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    private class Worker implements Runnable {

        private JMXConnector connector;

        private boolean configured;

        private int recordIdIndex;

        private int timestampIndex;

        private int severityIndex;

        private int subsystemIndex;

        private int messageIdIndex;

        private int messageIndex;

        private Long recordId;

        @Override
        public void run() {
            boolean interrupted = false;
            try {
                while (true) {
                    if (Thread.currentThread().isInterrupted() || RemoteLogInputReader.this.closed) {
                        interrupted = Thread.interrupted();
                        break;
                    }

                    JMXConnector current = getConnector();
                    if (current != null) {
                        try {
                            MBeanServerConnection con = current.getMBeanServerConnection();
                            ObjectName service = new ObjectName("com.bea:Name=DomainRuntimeService," // NOI18N
                                    + "Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean"); // NOI18N
                            ObjectName[] adminServers = (ObjectName[]) con
                                    .getAttribute(service, "ServerRuntimes"); // NOI18N
                            String serverName = null;
                            for (ObjectName adminServer : adminServers) {
                                if ((Boolean) con.getAttribute(adminServer, "AdminServer")) { // NOI18N
                                    serverName = con.getAttribute(adminServer, "Name").toString(); // NOI18N
                                    break;
                                }
                            }
                            if (serverName != null) {
                                ObjectName wldf = new ObjectName("com.bea:Name=DomainLog," // NOI18N
                                        + "ServerRuntime=" + serverName + ",Location=" + serverName + ",Type=WLDFDataAccessRuntime,WLDFAccessRuntime=Accessor,WLDFRuntime=WLDFRuntime"); // NOI18N
                                setColumns(con, wldf);
                                Long lastRecordId = (Long) con.getAttribute(wldf, "LatestRecordId"); // NOI18N

                                if (recordId == null || !recordId.equals(lastRecordId)) {
                                    Long from = recordId == null ? lastRecordId - INITIAL_LOG_COUNT : recordId + 1;
                                    if (from < 0) {
                                        from = (long) 0;
                                    }
                                    Long to = lastRecordId + 1;
                                    String cursorName = (String) con.invoke(wldf, "openCursor", // NOI18N
                                            new Object[]{from, to, Long.MAX_VALUE, null},
                                            new String[]{"java.lang.Long", "java.lang.Long", "java.lang.Long", "java.lang.String"}); // NOI18N
                                    try {
                                        while ((Boolean) con.invoke(wldf, "hasMoreData", new Object[]{cursorName}, // NOI18N
                                                new String[]{"java.lang.String"})) { // NOI18N
                                            Object[] data = (Object[]) con.invoke(wldf, "fetch", // NOI18N
                                                    new Object[]{cursorName}, new String[]{"java.lang.String"}); // NOI18N
                                            synchronized (RemoteLogInputReader.this) {
                                                for (Object item : data) {
                                                    if (item instanceof Object[]) {
                                                        Object[] values = (Object[]) item;
                                                        Long id = (Long) values[recordIdIndex];
                                                        if (recordId == null || id > recordId) {
                                                            recordId = id;
                                                        }
                                                        append(builder, values[timestampIndex]);
                                                        append(builder, values[severityIndex]);
                                                        append(builder, values[subsystemIndex]);
                                                        append(builder, values[messageIdIndex]);
                                                        append(builder, values[messageIndex]);
                                                    } else {
                                                        append(builder, item);
                                                    }
                                                    builder.append("\n"); // NOI18N
                                                }
                                            }
                                        }
                                    } finally {
                                        con.invoke(wldf, "closeCursor", // NOI18N
                                                new Object[]{cursorName}, new String[]{"java.lang.String"}); // NOI18N
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            LOGGER.log(Level.FINE, null, ex);
                            closeConnector();
                        } catch (MalformedObjectNameException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        } catch (InstanceNotFoundException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        } catch (MBeanException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        } catch (ReflectionException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        } catch (AttributeNotFoundException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        }
                    }
                    try {
                        Thread.sleep(POLL_INTERVAL);
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                        interrupted = true;
                        break;
                    }
                }
            } finally {
                closeConnector();
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private JMXConnector getConnector() {
            if (connector != null) {
                return connector;
            }

            try {
                connector = config.getRemote().executeAction(new Callable<JMXConnector>() {

                    @Override
                    public JMXConnector call() throws Exception {
                        JMXServiceURL url = new JMXServiceURL(config.isSecured() ? "t3s" : "t3", // NOI18N
                                config.getHost(), config.getPort(), "/jndi/weblogic.management.mbeanservers.domainruntime"); // NOI18N

                        String username = config.getUsername();
                        String password = config.getPassword();

                        Map<String, Object> env = new HashMap<String, Object>();
                        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
                                "weblogic.management.remote"); // NOI18N
                        env.put(javax.naming.Context.SECURITY_PRINCIPAL, username);
                        env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
                        env.put("jmx.remote.credentials", // NOI18N
                                new String[]{username, password});
                        env.put("jmx.remote.protocol.provider.class.loader", //NOI18N
                                config.getLayout().getClassLoader());

                        JMXConnector jmxConnector = JMXConnectorFactory.newJMXConnector(url, env);
                        jmxConnector.connect();

                        return jmxConnector;
                    }
                }, nonProxy);
            } catch (Exception ex) {
                connector = null;
                LOGGER.log(Level.INFO, null, ex);
            }
            return connector;
        }

        private void closeConnector() {
            if (connector != null) {
                try {
                    connector.close();
                    connector = null;
                    configured = false;
                } catch (IOException ex1) {
                    LOGGER.log(Level.FINE, null, ex1);
                }
            }
        }

        private void setColumns(MBeanServerConnection connection, ObjectName wldf) throws MBeanException,
                AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {

            if (configured) {
                return;
            }

            Map<String, Integer> columnIndexMap = (Map<String, Integer>)connection.getAttribute(wldf, "ColumnIndexMap"); // NOI18N
            recordIdIndex  = columnIndexMap.get("RECORDID"); // NOI18N
            timestampIndex = columnIndexMap.get("DATE"); // NOI18N
            severityIndex  = columnIndexMap.get("SEVERITY"); // NOI18N
            subsystemIndex = columnIndexMap.get("SUBSYSTEM"); // NOI18N
            messageIdIndex = columnIndexMap.get("MSGID"); // NOI18N
            messageIndex   = columnIndexMap.get("MESSAGE"); // NOI18N
            configured = true;
        }

        private void append(StringBuilder builder, Object value) {
            builder.append("<"); // NOI18N
            builder.append(value.toString());
            builder.append("> "); // NOI18N
        }
    }
}
