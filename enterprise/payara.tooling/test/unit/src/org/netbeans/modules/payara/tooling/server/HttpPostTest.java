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
package org.netbeans.modules.payara.tooling.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.admin.CommandHttpTest;
import org.testng.annotations.Test;

/**
 * Test Payara server HTTP post request.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class HttpPostTest extends CommandHttpTest {

    @Test(enabled = false)
    public void testHttpPost() {
        try {
            String urlString = "http://localhost:4848/management/domain/stop";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Host", "127.0.0.1:4848");
            conn.addRequestProperty("X-Requested-By", "Payara REST HTML interface");
            conn.connect();
            System.out.println("Reply: " + conn.getResponseCode() + ": " + conn.getResponseMessage());
        } catch (IOException ex) {
            Logger.getLogger(HttpPostTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        
    }
}
