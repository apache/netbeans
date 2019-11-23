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
package org.netbeans.modules.payara.common.nodes;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Map;
import org.netbeans.modules.payara.common.nodes.actions.OpenURLActionCookie;
import org.netbeans.modules.payara.spi.Decorator;
import org.netbeans.modules.payara.spi.Utils;
import org.netbeans.modules.payara.spi.WSDesc;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 *
 * @author Peter Williams
 */
public class Hk2WSNode extends Hk2ItemNode {

    private final WSDesc ws;
    private final Lookup lu;

    public Hk2WSNode(final Lookup lookup, final WSDesc ws, final Decorator decorator) {
        super(Children.LEAF, lookup, ws.getName(), decorator);
        setDisplayName(ws.getName());
        setShortDescription(NbBundle.getMessage(Hk2WSNode.class, "WS_NODE_DESC", ws.getName(), ws.getWsdlUrl())); // "<html>name: " + ws.getName() + "<br>path: " + ws.getWsdlUrl() + "</html>"); // NOI18N
        this.ws = ws;
        this.lu = lookup;

        getCookieSet().add(new OpenURLActionCookie() {

            @Override
            public String getWebURL() {
                String result = null;
                PayaraModule commonModule = lookup.lookup(PayaraModule.class);
                if (commonModule != null) {
                    Map<String, String> ip = commonModule.getInstanceProperties();
                    String host = ip.get(PayaraModule.HTTPHOST_ATTR);
                    if (null == host) {
                        host = ip.get(PayaraModule.HOSTNAME_ATTR);
                    }
                    String httpPort = ip.get(PayaraModule.HTTPPORT_ATTR);
                    String url = ip.get(PayaraModule.URL_ATTR);
                    if (url == null || !url.contains("ee6wc")) {
                        result = Utils.getHttpListenerProtocol(host, httpPort)
                                + "://" + host + ":" + httpPort + "/" + ws.getTestURL();
                    } else {
                        result = "http"
                                + "://" + host + ":" + httpPort + "/" + ws.getTestURL();
                    }
                    if (result.endsWith("//")) {
                        result = result.substring(0, result.length() - 1);
                    }
                }
                return result;
            }
        });
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() {
        String result = "";
        PayaraModule commonModule = lu.lookup(PayaraModule.class);
        if (commonModule != null) {
            Map<String, String> ip = commonModule.getInstanceProperties();
            String host = ip.get(PayaraModule.HTTPHOST_ATTR);
            if (null == host) {
                host = ip.get(PayaraModule.HOSTNAME_ATTR);
            }
            String httpPort = ip.get(PayaraModule.HTTPPORT_ATTR);
            String url = ip.get(PayaraModule.URL_ATTR);
            if (url == null || !url.contains("ee6wc")) {
                result = Utils.getHttpListenerProtocol(host, httpPort)
                        + "://" + host + ":" + httpPort + "/" + ws.getTestURL();
            } else {
                result = "http"
                        + "://" + host + ":" + httpPort + "/" + ws.getTestURL();
            }
            if (result.endsWith("//")) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return new StringSelection(result);
    }
}
