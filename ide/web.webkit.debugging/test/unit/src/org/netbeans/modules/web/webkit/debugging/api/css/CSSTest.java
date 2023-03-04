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
package org.netbeans.modules.web.webkit.debugging.api.css;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.Assert;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;


public class CSSTest {
    
    @Test
    public void testGetSupportedCSSProperties () {
        TransportHelper th = new TransportHelper(new TransportImplementation() {
            private ResponseCallback rc;
            
            @Override
            public boolean attach() {
                return true;
            }

            @Override
            public boolean detach() {
                return true;
            }

            @Override
            public void sendCommand(Command command) throws TransportStateException {
                JSONObject result = new JSONObject();
                result.put(Command.COMMAND_ID, command.getID());
                result.put(Command.COMMAND_METHOD, command.getCommand().get(Command.COMMAND_METHOD));
                result.put(Command.COMMAND_PARAMS, command.getCommand().get(Command.COMMAND_PARAMS));
                result.put(Command.COMMAND_RESULT, null);
                Response resp = new Response(result);
                rc.handleResponse(resp);
            }

            @Override
            public void registerResponseCallback(ResponseCallback callback) {
                this.rc = callback;
            }

            @Override
            public String getConnectionName() {
                return "Dummy";
            }

            @Override
            public URL getConnectionURL() {
                try {
                    return new URL("socket://demo");
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public String getVersion() {
                return "1.0";
            }
        });
        CSS css = new CSS(th);
        Map<String,PropertyInfo> result = css.getSupportedCSSProperties();
        // At least one property is expected
        Assert.assertFalse(result.isEmpty());
        // That at least one is border-radius
        Assert.assertTrue(result.containsKey("border-radius"));
    }
    
}
