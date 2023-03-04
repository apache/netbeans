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

package org.netbeans.modules.hudson.subversion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;

/**
 * Utilities for working with Subversion.
 */
public class SvnUtils {

    private SvnUtils() {}

    /**
     * Parse a {@code .svn/entries} file for some essential information.
     * Replacement for {@link org.netbeans.modules.subversion.client.parser.SvnWcParser}
     * (though it also handles remote checkouts, not just local directories).
     * @param dir a directory URL within a Subversion checkout
     * @return information about it, or null if this is not a Subversion checkout
     * @throws IOException if it is a checkout but cannot be parsed
     * @throws UnsupportedSubversionVersionException if it is a checkout but the format is unsupported
     */
    public static Info parseCheckout(URL dir) throws IOException, UnsupportedSubversionVersionException {
        return parseCheckout(dir, null);
    }
    static Info parseCheckout(URL dir, HudsonJob job) throws IOException, UnsupportedSubversionVersionException {
        URL svnEntries = new URL(dir, ".svn/entries"); // NOI18N
        ConnectionBuilder cb = new ConnectionBuilder();
        if (job != null) {
            cb = cb.job(job);
        }
        InputStream is;
        try {
            is = cb.url(svnEntries).connection().getInputStream();
        } catch (FileNotFoundException x) {
            return null;
        }
        String module, repository;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            r.readLine(); // "8" or similar
            r.readLine(); // blank
            r.readLine(); // "dir"
            r.readLine(); // rev #
            module = r.readLine();
            repository = r.readLine();
        } finally {
            is.close();
        }
        if (module == null || repository == null) {
            throw new UnsupportedSubversionVersionException();
        }
        try {
            return new Info(new URI(module), new URI(repository));
        } catch (URISyntaxException x) {
            throw new IOException(x);
        }
    }
    /**
     * There is a Subversion checkout here, but the format is known to not be supported.
     */
    public static class UnsupportedSubversionVersionException extends Exception {}

    /**
     * Represents the checkout and repository portions of a {@code .svn/entries} file.
     * Replacement for {@link org.tigris.subversion.svnclientadapter.ISVNInfo}.
     */
    public static class Info {
        public final URI module;
        public final URI repository;
        private Info(URI module, URI repository) {
            this.module = module;
            this.repository = repository;
        }
    }

}
