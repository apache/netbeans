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
