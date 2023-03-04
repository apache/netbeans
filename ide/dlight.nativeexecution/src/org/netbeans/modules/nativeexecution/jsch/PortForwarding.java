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
package org.netbeans.modules.nativeexecution.jsch;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.support.Logger;

/**
 *
 * @author akrasny
 */
final class PortForwarding {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private final List<PortForwardingInfoR> rinfo = new ArrayList<>();
    private final List<PortForwardingInfoL> linfo = new ArrayList<>();

    public synchronized void initSession(Session session) {
        for (PortForwarding.PortForwardingInfoR r : rinfo) {
            try {
                session.setPortForwardingR(r.bind_address, r.rport, r.host, r.lport);
            } catch (JSchException ex) {
                log.log(Level.FINE, "setPortForwardingR({0}, {1}, {2}, {3} failed", new Object[]{r.bind_address, r.rport, r.host, r.lport}); // NOI18N
                log.log(Level.FINE, "", ex); // NOI18N
            }
        }
        for (PortForwarding.PortForwardingInfoL l : linfo) {
            try {
                session.setPortForwardingL(l.lport, l.host, l.rport);
            } catch (JSchException ex) {
                log.log(Level.FINE, "setPortForwardingL({0}, {1}, {2} failed", new Object[]{l.lport, l.host, l.rport}); // NOI18N
                log.log(Level.FINE, "", ex); // NOI18N
            }
        }
    }

    public synchronized void addPortForwardingInfoR(String bind_address, int rport, String host, int lport) {
        rinfo.add(new PortForwardingInfoR(bind_address, rport, host, lport));
    }

    public synchronized void addPortForwardingInfoL(int lport, String host, int rport) {
        linfo.add(new PortForwardingInfoL(lport, host, rport));
    }

    public synchronized void removePortForwardingInfoR(int rport) {
        Iterator<PortForwardingInfoR> it = rinfo.iterator();
        while (it.hasNext()) {
            if (it.next().rport == rport) {
                it.remove();
            }
        }
    }

    public synchronized void removePortForwardingInfoL(int lport) {
        Iterator<PortForwardingInfoL> it = linfo.iterator();
        while (it.hasNext()) {
            if (it.next().lport == lport) {
                it.remove();
            }
        }
    }

    static final class PortForwardingInfoR {

        final String bind_address;
        final int rport;
        final String host;
        final int lport;

        private PortForwardingInfoR(String bind_address, int rport, String host, int lport) {
            this.bind_address = bind_address;
            this.rport = rport;
            this.host = host;
            this.lport = lport;
        }
    }

    static final class PortForwardingInfoL {

        final int lport;
        final String host;
        final int rport;

        private PortForwardingInfoL(int lport, String host, int rport) {
            this.lport = lport;
            this.host = host;
            this.rport = rport;
        }
    }
}
