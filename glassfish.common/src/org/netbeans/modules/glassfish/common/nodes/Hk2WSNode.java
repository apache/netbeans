/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.nodes;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Map;
import org.netbeans.modules.glassfish.common.nodes.actions.OpenURLActionCookie;
import org.netbeans.modules.glassfish.spi.Decorator;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.Utils;
import org.netbeans.modules.glassfish.spi.WSDesc;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

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
                GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
                if (commonModule != null) {
                    Map<String, String> ip = commonModule.getInstanceProperties();
                    String host = ip.get(GlassfishModule.HTTPHOST_ATTR);
                    if (null == host) {
                        host = ip.get(GlassfishModule.HOSTNAME_ATTR);
                    }
                    String httpPort = ip.get(GlassfishModule.HTTPPORT_ATTR);
                    String url = ip.get(GlassfishModule.URL_ATTR);
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
        GlassfishModule commonModule = lu.lookup(GlassfishModule.class);
        if (commonModule != null) {
            Map<String, String> ip = commonModule.getInstanceProperties();
            String host = ip.get(GlassfishModule.HTTPHOST_ATTR);
            if (null == host) {
                host = ip.get(GlassfishModule.HOSTNAME_ATTR);
            }
            String httpPort = ip.get(GlassfishModule.HTTPPORT_ATTR);
            String url = ip.get(GlassfishModule.URL_ATTR);
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
