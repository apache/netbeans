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
package org.netbeans.modules.cnd.remote.ui.networkneighbour;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 */
public final class NetworkRegistry {

    private static final int SSH_PING_TIMEOUT = Integer.getInteger("ssh.ping.timeout", 3000); // NOI18N
    private final static boolean NO_SCAN = Boolean.getBoolean("networkregistry.noscan"); // NOI18N
    private final static ScannerLock lock = new ScannerLock();
    private final static NetworkRegistry instance = new NetworkRegistry();
    private final static int numOfScanThreads = 8;
    private final ExplorerManager manager = new ExplorerManager();
    private final List<NeighbourHost> hosts;
    private final ChangeSupport cs = new ChangeSupport(this);
    private Scanner scanner;
    private Task[] scanningTasks;
    private final RequestProcessor rc = new RequestProcessor("NetwrorkRegistry", numOfScanThreads);//NOI18N

    private NetworkRegistry() {
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4773521
        System.setProperty("java.net.preferIPv4Stack", "true"); // NOI18N
        hosts = new ArrayList<>();
        scanner = NO_SCAN ? null : new Scanner();
        manager.setRootContext(Node.EMPTY);
        Preferences proxyPrefs = NbPreferences.root().node("org/netbeans/core"); // NOI18N
        if (!NO_SCAN) {
            proxyPrefs.addPreferenceChangeListener(new PreferenceChangeListener() {

                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    synchronized (NetworkRegistry.this) {
                        stopScan();
                        hosts.clear();
                        cs.fireChange();
                        scanner = new Scanner();
                        startScan();
                    }
                }
            });
        }
    }

    public NeighbourHost[] getHosts() {
        return hosts.toArray(new NeighbourHost[hosts.size()]);
    }

    public void addChangeListener(ChangeListener cl) {
        cs.addChangeListener(cl);
        cl.stateChanged(new ChangeEvent(this));
    }

    public void removeChangeListener(ChangeListener cl) {
        cs.removeChangeListener(cl);
    }

    public static NetworkRegistry getInstance() {
        return instance;
    }

    public boolean isHostAccessible(String hostname, int port) {
        try {
            return doPing(InetAddress.getByName(hostname), port);
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    private void addHost(Scanner scanner, NeighbourHost host) {
        synchronized (this) {
            if (scanner != this.scanner) {
                return;
            }
            hosts.add(host);
            cs.fireChange();
        }
    }

    public void startScan() {
        synchronized (this) {
            if (scanner != null && scanningTasks == null) {
                scanningTasks = new Task[numOfScanThreads];
                for (int i = 0; i < numOfScanThreads; i++) {
                    scanningTasks[i] = rc.post(scanner);
                }
            }
        }
    }

    public void stopScan() {
        synchronized (this) {
            if (scanner != null && scanningTasks != null) {
                scanner.stop();
                scanningTasks = null;
            }
        }
    }

    private boolean doPing(InetAddress addr, int port) {
        Socket socket = null;

        try {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.setSoTimeout(SSH_PING_TIMEOUT);
            socket.setKeepAlive(false);
            InetSocketAddress address = new InetSocketAddress(addr, port);
            socket.connect(address, SSH_PING_TIMEOUT);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private class Scanner implements Runnable {

        private AdressesEnumeration addressesToScan = new AdressesEnumeration();
        private volatile boolean isInterrupted;

        public void stop() {
            isInterrupted = true;
        }

        @Override
        public void run() {
            isInterrupted = false;
            while (true) {
                if (isInterrupted()) {
                    break;
                }

                InetAddress address = null;

                synchronized (lock) {
                    if (addressesToScan.hasMoreElements()) {
                        address = addressesToScan.nextElement();
                    }
                }

                if (address == null) {
                    break;
                }

                String hostName = address.getCanonicalHostName();
                String ipAddress = address.getHostAddress();

                if (hostName == null || ipAddress == null
                        || ipAddress.equalsIgnoreCase(hostName)) {
                    // was not resolved - just skip
                    continue;
                }

                addHost(this, new NeighbourHost(hostName, doPing(address, 22)));
            }
        }

        private boolean isInterrupted() {
            try {
                Thread.sleep(0);
            } catch (InterruptedException ex) {
                isInterrupted = true;
                Thread.currentThread().interrupt();
            }

            isInterrupted |= Thread.currentThread().isInterrupted();
            return isInterrupted;
        }

        private void reset() {
            synchronized (lock) {
                addressesToScan = new AdressesEnumeration();
            }
        }
    }

    private final static class AdressesEnumeration implements Enumeration<InetAddress> {

        private final Iterator<SingleRangeEnumeration> rangesIterator;
        private SingleRangeEnumeration currentEnumerator;

        public AdressesEnumeration() {
            final Set<SingleRangeEnumeration> set = new HashSet<>();

            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface ifc = networkInterfaces.nextElement();

                    if (ifc.isLoopback()) {
                        continue;
                    }

                    List<InterfaceAddress> interfaceAddresses = null;

                    try {
                        interfaceAddresses = ifc.getInterfaceAddresses();
                    } catch (Throwable th) {
                        // http://netbeans.org/bugzilla/show_bug.cgi?id=223119
                    }

                    if (interfaceAddresses == null) {
                        continue;
                    }

                    for (InterfaceAddress address : interfaceAddresses) {
                        InetAddress broadcast = address.getBroadcast();
                        if (broadcast == null) {
                            // will not deal with IPv6
                            continue;
                        }

                        set.add(new SingleRangeEnumeration(address));
                    }
                }
            } catch (SocketException ex) {
                Exceptions.printStackTrace(ex);
            }

            rangesIterator = set.iterator();
            currentEnumerator = rangesIterator.hasNext() ? rangesIterator.next() : null;
        }

        @Override
        public boolean hasMoreElements() {
            return currentEnumerator != null && currentEnumerator.hasMoreElements();
        }

        @Override
        public InetAddress nextElement() {
            InetAddress result = currentEnumerator.nextElement();

            if (!currentEnumerator.hasMoreElements()) {
                if (rangesIterator.hasNext()) {
                    currentEnumerator = rangesIterator.next();
                } else {
                    currentEnumerator = null;
                }
            }

            return result;
        }
    }

    private final static class SingleRangeEnumeration implements Enumeration<InetAddress> {

        private final long lastAddress;
        private long currentAddress;

        public SingleRangeEnumeration(InterfaceAddress address) {
            short prefixLength = address.getNetworkPrefixLength();
            InetAddress inetAddress = address.getAddress();
            byte[] b = inetAddress.getAddress();
            currentAddress = (0xFFL & b[0]) << 24;
            currentAddress |= (0xFFL & b[1]) << 16;
            currentAddress |= (0xFFL & b[2]) << 8;
            currentAddress |= (0xFFL & b[3]);
            currentAddress &= (0xFFFFFFFFL << (32 - prefixLength));
            currentAddress += 1;

            lastAddress = currentAddress | (1 << (32 - prefixLength)) - 1;
        }

        @Override
        public boolean hasMoreElements() {
            return currentAddress < lastAddress;
        }

        @Override
        public InetAddress nextElement() {
            try {
                return Inet4Address.getByAddress(
                        new byte[]{
                            (byte) ((currentAddress >> 24) & 0xFF),
                            (byte) ((currentAddress >> 16) & 0xFF),
                            (byte) ((currentAddress >> 8) & 0xFF),
                            (byte) ((currentAddress) & 0xFF)});
            } catch (UnknownHostException ex) {
//                Exceptions.printStackTrace(ex);
            } finally {
                currentAddress++;
            }

            return null;
        }
    }

    private final static class ScannerLock {
    }
}
