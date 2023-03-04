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

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.spi.PasswordAuthorizer;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implements authentication based on Hudson's standard login form.
 * {@code main/core/src/main/resources/hudson/model/Hudson/login.jelly} shows the style.
 * Uses {@link PasswordAuthorizer} to find the username and password.
 * Can handle both ACEGI-based security, and "legacy" container auth using
 * form-based logins according to the Servlet spec.
 */
@ServiceProvider(service=ConnectionAuthenticator.class, position=100)
public class ServletConnectionAuthenticator implements ConnectionAuthenticator {

    private static final Logger LOGGER = Logger.getLogger(ServletConnectionAuthenticator.class.getName());

    public @Override void prepareRequest(URLConnection conn, URL home) {}

    public @Override URLConnection forbidden(URLConnection conn, URL home) {
        for (PasswordAuthorizer aa : Lookup.getDefault().lookupAll(PasswordAuthorizer.class)) {
            String[] auth = aa.authorize(home);
            if (auth != null) {
                LOGGER.log(Level.FINE, "Got authorization for {0} on {1} from {2}", new Object[] {auth[0], home, aa});
                for (String realmURI : new String[] {"j_spring_security_check", "j_acegi_security_check", "j_security_check"}) { // NOI18N
                    try {
                        LOGGER.log(Level.FINER, "Posting authentication to {0}", realmURI);
                        if (realmURI.equals("j_security_check")) { // #193066: indulge org.apache.catalina.authenticator.FormAuthenticator
                            new ConnectionBuilder().url(new URL(home, "loginEntry")).homeURL(home).authentication(false).connection();
                        }
                        new ConnectionBuilder().url(new URL(home, realmURI)).
                                postData(("j_username=" + URLEncoder.encode(auth[0], "UTF-8") + "&j_password=" + // NOI18N
                                URLEncoder.encode(auth[1], "UTF-8")).getBytes(StandardCharsets.UTF_8)). // NOI18N
                                homeURL(home).authentication(false).connection();
                        LOGGER.log(Level.FINER, "Posted authentication to {0} worked", realmURI);
                        return conn.getURL().openConnection();
                    } catch (IOException x) {
                        LOGGER.log(Level.FINE, null, x);
                    }
                }
            }
        }
        return null;
    }

}
