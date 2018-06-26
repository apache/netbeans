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

package org.netbeans.modules.j2ee.dd.api.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.impl.web.WebParseUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Provides access to object model (OM) of web fragment file (META-INF/web-fragment.xml).
 * Root of web fragment OM is represented by object
 * {@link org.netbeans.modules.j2ee.dd.api.web.WebFragment}.
 *
 * @author Petr Slechta
 */
public final class WebFragmentProvider {

    private static final Logger LOG = Logger.getLogger(WebFragmentProvider.class.getName());
    private static WebFragmentProvider instance;

    private WebFragmentProvider() {
    }

    public static WebFragmentProvider getDefault() {
        if (instance == null) {
            instance = new WebFragmentProvider();
        }
        return instance;
    }

    /**
     * Gets the root bean graph representing the given web-fragment.xml deployment descriptor
     * file.
     *
     * @param fo the file object representing a web.xml file. Must not be null.
     * @return the <code>WebFragment</code> representing the given <code>fo</code>.
     * @throws IOException if the given <code>fo</code> could not be read
     * or if parsing it failed.
     */
    public WebFragment getWebFragmentRoot(FileObject fo) throws IOException, FileNotFoundException {
        Parameters.notNull("fo", fo); //NOI18N
        try {
            String version = WebParseUtils.getVersion(fo);
            // preparsing
            SAXParseException error = WebParseUtils.parse(fo);
            if (error != null)
                throw error;
            return createWebFragment(fo, version);
        }
        catch (SAXException ex) {
            LOG.log(Level.INFO, "Parsing failed!", ex);
            throw new IOException("Parsing failed: "+ex);
        }
    }

    private WebFragment createWebFragment(FileObject fo, String version) throws IOException, SAXException {
        try {
            if (WebFragment.VERSION_3_0.equals(version)) {
                InputStream inputStream = fo.getInputStream();
                try {
                    return org.netbeans.modules.j2ee.dd.impl.web.model_3_0_frag.WebFragment.createGraph(inputStream);
                } finally {
                    inputStream.close();
                }
            } else if (WebFragment.VERSION_3_1.equals(version)) {
                InputStream inputStream = fo.getInputStream();
                try {
                    return org.netbeans.modules.j2ee.dd.impl.web.model_3_1_frag.WebFragment.createGraph(inputStream);
                } finally {
                    inputStream.close();
                }
            } else {
                throw new IOException("Unsupported version of web-fragment.xml found! Version: "+version);
            }
        } catch (RuntimeException ex) {
            throw new SAXException(ex);
        }
    }

}
