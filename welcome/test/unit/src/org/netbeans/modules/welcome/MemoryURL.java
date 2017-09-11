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

package org.netbeans.modules.welcome;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;

/**
 *
 * @author Jaroslav Tulach
 */
public class MemoryURL extends URLStreamHandler {

    static {
        class F implements URLStreamHandlerFactory {
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (protocol.startsWith("memory")) {
                    return new MemoryURL();
                }
                return null;
            }
        }
        F f = new F();
        URL.setURLStreamHandlerFactory(f);
    }
    
    private static Map<String,InputStream> contents = new HashMap<String,InputStream>();
    private static Map<String,MC> outputs = new HashMap<String,MC>();
    public static void registerURL(String u, String content) {
        contents.put(u, new ByteArrayInputStream(content.getBytes()));
    }
    public static void registerURL(String u, InputStream content) {
        contents.put(u, content);
    }
    
    public static byte[] getOutputForURL(String u) {
        MC out = outputs.get(u);
        Assert.assertNotNull("No output for " + u, out);
        return out.out.toByteArray();
    }
    
    public static String getRequestParameter(String u, String param) {
        MC out = outputs.get(u);
        Assert.assertNotNull("No output for " + u, out);
        return out.params.get(param.toLowerCase());
    }

    protected URLConnection openConnection(URL u) throws IOException {
        return new MC(u);
    }
    
    private static final class MC extends URLConnection {
        private InputStream values;
        private ByteArrayOutputStream out;
        private Map<String,String> params;
        
        public MC(URL u) {
            super(u);
            outputs.put(u.toExternalForm(), this);
            params = new HashMap<String,String>();
        }

        public void connect() throws IOException {
            if (values != null) {
                return;
            }
            values = contents.remove(url.toExternalForm());
            if (values == null) {
                throw new IOException("No such content: " + url);
            }
        }

        public InputStream getInputStream() throws IOException {
            connect();
            return values;
        }

        public OutputStream getOutputStream() throws IOException {
            if (out == null) {
                out = new ByteArrayOutputStream();
            }
            return out;
        }

        public void setRequestProperty(String key, String value) {
            super.setRequestProperty(key, value);
            params.put(key.toLowerCase(), value);
        }

        @Override
        public String getContentType() {
            return "text/html";
        }
        
        
    }
}
