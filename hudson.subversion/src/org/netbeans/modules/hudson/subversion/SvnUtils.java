/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
