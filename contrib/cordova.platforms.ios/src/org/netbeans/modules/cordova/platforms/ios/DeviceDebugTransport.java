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
package org.netbeans.modules.cordova.platforms.ios;

import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;

/**
 *
 * @author Jan Becicka
 */
public class DeviceDebugTransport extends IOSDebugTransport implements TransportImplementation {
    
    private WebInspectorJNIBinding nativeCall;

    public DeviceDebugTransport() {
        super();
        nativeCall = WebInspectorJNIBinding.getDefault();
    }
    
    @Override
    protected void init() throws Exception {
        nativeCall.start();
    }

    @Override
    public void sendCommand(JSONObject command) throws Exception {
        sendMessage(createJSONCommand(command));
    }

    @Override
    public void sendCommand(String command) throws Exception {
        //System.out.println("sending " + command);
        sendMessage(command);
    }
    
    private void sendMessage(String message) {
        if (keepGoing) {
            nativeCall.sendMessage(message);
        }
    }

    @Override
    protected NSObject readData() throws Exception {
        String content = nativeCall.receiveMessage();
        if (content==null) {
            Thread.sleep(100);
            return null;
        }

        NSObject object = XMLPropertyListParser.parse(fromString(content));
        return object;
    }
    
    @Override
    protected void stop() {
        super.stop();
        nativeCall.stop();
   }

    @Override
    public String getConnectionName() {
        return "iOS Device"; // NOI18N
    }

    @Override
    public String getVersion() {
        return "1.0"; // NOI18N
    }
}
         
