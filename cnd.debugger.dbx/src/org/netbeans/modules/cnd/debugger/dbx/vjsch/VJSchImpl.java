/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.debugger.dbx.vjsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.sun.tools.swdev.glue.vjsch.VirtJSch;
import com.sun.tools.swdev.glue.vjsch.VirtSession;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.spi.support.JSchAccess;
import org.netbeans.modules.nativeexecution.spi.support.JSchAccessor;
import org.openide.util.Exceptions;

/**
 *
 *
 */
public final class VJSchImpl implements VirtJSch {

    public VirtSession getSession(String username, String hostname, int port) throws JSchException {
        return new VirtSessionImpl(username, hostname, port);
    }

    private static class VirtSessionImpl implements VirtSession {

        private final JSchAccess jschAccess;

        public VirtSessionImpl(String username, String hostname, int port) {
            jschAccess = JSchAccessor.get(ExecutionEnvironmentFactory.createNew(username, hostname, port));
        }

        public String getConfig(String key) {
            return jschAccess.getConfig(key);
        }

        public String getServerVersion() {
            try {
                return jschAccess.getServerVersion();
            } catch (JSchException ex) {
                Exceptions.printStackTrace(ex);
            }
            throw new InternalError();
        }

        public void connect() throws JSchException {
        }

        public void disconnect() {
        }

        public Channel openChannel(String type) throws JSchException {
            try {
                return jschAccess.openChannel(type);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        public void setPortForwardingR(String bind_address, int rport, String host, int lport) throws JSchException {
            jschAccess.setPortForwardingR(bind_address, rport, host, lport);
        }

        public int setPortForwardingL(int lport, String host, int rport) throws JSchException {
            return jschAccess.setPortForwardingL(lport, host, rport);
        }

        public void delPortForwardingR(int rport) throws JSchException {
            jschAccess.delPortForwardingR(rport);
        }

        public void delPortForwardingL(int lport) throws JSchException {
            jschAccess.delPortForwardingL(lport);
        }

        public void disposeChannel(Channel channel) {
            try {
                jschAccess.releaseChannel(channel);
            } catch (JSchException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
