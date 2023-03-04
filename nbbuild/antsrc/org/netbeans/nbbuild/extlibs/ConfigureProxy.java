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
package org.netbeans.nbbuild.extlibs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public final class ConfigureProxy extends Task {
    private URL connectTo;
    private String hostProperty = "http.proxyHost";
    private String portProperty = "http.proxyPort";

    public void setConnectTo(String uri) throws MalformedURLException {
        connectTo = new URL(uri);
    }

    public void setHostProperty(String host) {
        hostProperty = host;
    }

    public void setPortProperty(String port) {
        portProperty = port;
    }

    @Override
    public void execute() throws BuildException {
        try {
            URI[] connectedVia = { null };
            URLConnection connect = openConnection(this, connectTo, connectedVia);
            if (connect == null) {
                throw new BuildException("Cannot connect to " + connectTo);
            }

            if (connectedVia[0] != null) {
                final String host = connectedVia[0].getHost();
                log(String.format("Setting %s to %s", hostProperty, host), Project.MSG_INFO);
                getProject().setUserProperty(hostProperty, host);
                final int port = connectedVia[0].getPort();
                log(String.format("Setting %s to %d", portProperty, port), Project.MSG_INFO);
                getProject().setUserProperty(portProperty, "" + port);
            } else {
                log(String.format("Resetting %s to empty string", hostProperty), Project.MSG_INFO);
                getProject().setUserProperty(hostProperty, "");
                getProject().setUserProperty(portProperty, "80");
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    static URLConnection openConnection(Task task, final URL url, URI[] connectedVia) throws IOException {
        long connectTimeoutMs = getProjectPropertyInt(task, "downloadBinaries.connectTimeoutMs", 15000);
        task.log("Connect timeout set to " + connectTimeoutMs + " milliseconds prior to connecting to " + url, Project.MSG_DEBUG);
        final URLConnection[] conn = { null };
        final List<Exception> errs = new CopyOnWriteArrayList<>();
        final StringBuffer msgs = new StringBuffer();
        final CountDownLatch connected = new CountDownLatch(1);
        ExecutorService connectors = Executors.newFixedThreadPool(3);
        connectors.submit(() -> {
            checkProxyProperty("http_proxy", url, conn, connectedVia, connected, errs, msgs);
        });
        connectors.submit(() -> {
            checkProxyProperty("https_proxy", url, conn, connectedVia, connected, errs, msgs);
        });
        connectors.submit(() -> {
            try {
                URLConnection test = url.openConnection(Proxy.NO_PROXY);
                test.connect();
                conn[0] = test;
                msgs.append("\nNo proxy connected");
                connected.countDown();
            } catch (IOException ex) {
                errs.add(ex);
            }
        });
        try {
            if (!connected.await(connectTimeoutMs, TimeUnit.MILLISECONDS)) {
                throw new IOException("Could not connect to " + url + " within " + connectTimeoutMs + " milliseconds");
            }
        } catch (InterruptedException ex) {
        }
        if (conn[0] == null) {
            for (Exception ex : errs) {
                task.log(ex, Project.MSG_ERR);
            }
            if (msgs.length() > 0) {
                task.log(msgs.toString(), Project.MSG_ERR);
            }
            throw new IOException("Cannot connect to " + url);
        } else {
            task.log(msgs.toString(), Project.MSG_DEBUG);
        }
        return conn[0];
    }

    private static void checkProxyProperty(
        String propertyName, final URL url,
        final URLConnection[] conn, URI[] connectedVia,
        final CountDownLatch connected, 
        final List<Exception> errs,
        StringBuffer msgs
    ) {
        String httpProxy = System.getenv(propertyName);
        msgs.append("\n[" + propertyName + "] set to " + httpProxy);
        if (httpProxy != null) {
            try {
                URI uri = new URI(httpProxy);
                InetSocketAddress address = InetSocketAddress.createUnresolved(uri.getHost(), uri.getPort());
                Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
                URLConnection test = url.openConnection(proxy);
                test.connect();
                msgs.append("\n[" + propertyName + "] connected");
                conn[0] = test;
                if (connectedVia != null) {
                    connectedVia[0] = uri;
                }
                connected.countDown();
                msgs.append("\n[" + propertyName + "] countDown");
            } catch (IOException | URISyntaxException ex) {
                errs.add(ex);
                msgs.append("\n[" + propertyName + "] exception " + ex.getMessage());
            }
        }
    }

    private static long getProjectPropertyInt(Task task, String propertyName, int defaultValue) {
        if (task == null) {
            return defaultValue;
        }
        Project project = task.getProject();
        if (project != null) {
            String p = project.getProperty(propertyName);
            if (p == null) {
                return defaultValue;
            }
            try {
                return Long.parseLong(p);
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
