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
package org.netbeans.modules.web.clientproject.api.network.ui;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class NetworkErrorPanelTest {

    @Test
    public void testJoinRequestsSingle() {
        List<String> requests = Arrays.asList("www.oracle.com");
        Assert.assertEquals("www.oracle.com", NetworkErrorPanel.joinRequests(requests));
    }

    @Test
    public void testJoinRequestsMulti() {
        List<String> requests = Arrays.asList(
                "www.oracle.com",
                "MyJsLibrary-0.1");
        Assert.assertEquals("www.oracle.com<br>MyJsLibrary-0.1", NetworkErrorPanel.joinRequests(requests));
    }

    @Test
    public void testDecorateRequestPlain() {
        String request = "www.oracle.com";
        Assert.assertEquals("www.oracle.com", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestPlainLong() {
        String request = "www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com ";
        Assert.assertEquals("www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.oracle.com www.o...", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestHttp() {
        String request = "http://www.oracle.com";
        Assert.assertEquals("<a href=\"http://www.oracle.com\">http://www.oracle.com</a>", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestHttpLong() {
        String request = "http://www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/";
        Assert.assertEquals("<a href=\"http://www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/\">http://www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.com/www.oracle.co...</a>", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestHttps() {
        String request = "https://www.oracle.com";
        Assert.assertEquals("<a href=\"https://www.oracle.com\">https://www.oracle.com</a>", NetworkErrorPanel.decorateRequest(request));
    }

    @Test
    public void testDecorateRequestFtp() {
        String request = "ftp://www.oracle.com";
        Assert.assertEquals("ftp://www.oracle.com", NetworkErrorPanel.decorateRequest(request));
    }

}
