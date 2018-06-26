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
package org.netbeans.modules.cordova.platforms.ios;

import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;

/**
 *
 * @author Jan Becicka
 */
public class SimulatorDebugTransport extends IOSDebugTransport {
    private static final String LOCALHOST_IPV6 = "::1"; // NOI18N
    private static final int port = 27753;
    private static final Logger LOG
            = Logger.getLogger(SimulatorDebugTransport.class.getName());

    private Socket socket;

    @Override
    protected void sendCommand(JSONObject command) throws Exception {
        String cmd = createJSONCommand(command);
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "\n\nSending:\n{0}\n{1}",             //NOI18N
                    new Object[]{command.toJSONString(), cmd});
        }
        sendBinaryMessage(plistXmlToBinary(cmd));
    }

    @Override
    protected void sendCommand(String xml) throws Exception {
        LOG.log(Level.FINEST, "\n\nSending:\n{0}", xml);
        sendBinaryMessage(plistXmlToBinary(xml));
    }

    private void sendBinaryMessage(byte[] bytes) throws IOException {
        if (socket == null || socket.isClosed()) {
            return;
        }
        OutputStream os = socket.getOutputStream();
        byte[] lenght = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(bytes.length).array();
        os.write(lenght);
        os.write(bytes);
    }

    @Override
    protected NSObject readData() throws Exception {
        InputStream is = socket.getInputStream();
        byte[] sizeBuffer = new byte[4];
        int count = is.read(sizeBuffer);
        while (count < 4) {
            count += is.read(sizeBuffer, count, 4 - count);
        }
        int size = ByteBuffer.wrap(sizeBuffer, 0, 4).getInt();
        byte[] content = new byte[size];
        count = is.read(content);
        while (count < size) {
            final int read = is.read(content, count, size - count);
            if (read == -1) {
                boolean s = keepGoing;
                stop();
                if (s) {
                    WebKitDebuggingSupport.getDefault().stopDebugging(false);
                }
                return null;
            }
            count += read;
        }
        assert count == size;
        NSObject object = BinaryPropertyListParser.parse(content);
        return object;
    }

    @Override
    protected void stop() {
        super.stop();
        try {
            socket.close();
        } catch (Exception e) {
            LOG.log(Level.FINE , e.getMessage(), e);
        }
    }

    @Override
    public String getConnectionName() {
        return "iOS Simulator"; // NOI18N
    }

    @Override
    public String getVersion() {
        return "1.0"; // NOI18N
    }

    @Override
    protected void init() throws Exception {
        if (socket != null && (socket.isConnected() || !socket.isClosed())) {
            socket.close();
        }
        for (long stop = System.nanoTime() + TimeUnit.MINUTES.toNanos(2); stop > System.nanoTime();) {
            try {
                socket = new Socket(LOCALHOST_IPV6, port);
                break;
            } catch (java.net.ConnectException ex) {
                Thread.sleep(5000);
                continue;
            }
        }
    }
    
}
