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

import java.util.Map;
import org.netbeans.modules.payara.common.nodes.actions.OpenURLActionCookie;
import org.netbeans.modules.payara.spi.AppDesc;
import org.netbeans.modules.payara.spi.Decorator;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.Utils;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Peter Williams
 */
public class Hk2ApplicationNode extends Hk2ItemNode {

    public Hk2ApplicationNode(final Lookup lookup, final AppDesc app, final Decorator decorator) {
        super(Children.LEAF, lookup, app.getName(), decorator);
        setDisplayName(app.getName());
        setShortDescription("<html>name: " + app.getName() + "<br>path: " + app.getPath() + "<br>enabled: " + app.getEnabled() + "</html>");
        
        // !PW FIXME should method of retrieving context root be controlled by decorator?
        if(decorator.canShowBrowser()) {
            getCookieSet().add(new OpenURLActionCookie() {
                public String getWebURL() {
                    String result = null;
                    PayaraModule commonModule = lookup.lookup(PayaraModule.class);
                    if(commonModule != null) {
                        Map<String, String> ip = commonModule.getInstanceProperties();
                        String host = ip.get(PayaraModule.HTTPHOST_ATTR);
                        if (null == host) {
                            host = ip.get(PayaraModule.HOSTNAME_ATTR);
                        }
                        String httpPort = ip.get(PayaraModule.HTTPPORT_ATTR);
                        String url = ip.get(PayaraModule.URL_ATTR);
                        if (url == null || !url.contains("ee6wc")) {
                            result = Utils.getHttpListenerProtocol(host, httpPort) +
                                "://" + host + ":" + httpPort + "/" + app.getContextRoot();
                        } else {
                            result = "http" +
                                "://" + host + ":" + httpPort + "/" + app.getContextRoot();
                        }
                        if(result.endsWith("//")) {
                            result = result.substring(0, result.length()-1);
                        }
                    }
                    return result;
                }
            });
        }
    }

}
