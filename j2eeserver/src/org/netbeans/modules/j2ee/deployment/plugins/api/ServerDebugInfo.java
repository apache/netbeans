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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * ServerDebugInfo.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

/**
 * Class to communicate the debugging information between plugin, server api and IDE.
 * @author Martin Grebac
 * @version 0.1
 */

public class ServerDebugInfo {

    public static final String TRANSPORT_SOCKET = "dt_socket";  //NOI18N
    public static final String TRANSPORT_SHMEM = "dt_shmem";    //NOI18N

    /**
     * Holds value of property transport - socket or shared memory.
     */
    private String transport = TRANSPORT_SOCKET;

    /**
     * Holds value of property host - where the target vm is running.
     */
    private String host = "localhost";                          //NOI18N
    
    /**
     * Holds value of property shmemName - shared memory name of the target vm.
     */
    private String shmemName = "";
    
    /**
     * Holds value of property port - port number of the target vm..
     */
    private int port;
    
    public ServerDebugInfo(String host, String shmemName) {
        setTransport(TRANSPORT_SHMEM);
        setHost(host);
        setShmemName(shmemName);
    }
    
    public ServerDebugInfo(String host, int port) {
        setTransport(TRANSPORT_SOCKET);
        setHost(host);
        setPort(port);
    }
    
    /**
     * Getter for property transport.
     * @return Value of property transport.
     */
    public String getTransport() {
        return this.transport;
    }
    
    /**
     * Setter for property transport.
     * @param transport New value of property transport.
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    /**
     * Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return this.host;
    }
    
    /**
     * Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Getter for property shmemName.
     * @return Value of property shmemName.
     */
    public String getShmemName() {
        return this.shmemName;
    }
    
    /**
     * Setter for property shmemName.
     * @param shmemName New value of property shmemName.
     */
    public void setShmemName(String shmemName) {
        this.shmemName = shmemName;
    }
    
    /**
     * Getter for property port.
     * @return Value of property port.
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * Setter for property port.
     * @param port New value of property port.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
}
