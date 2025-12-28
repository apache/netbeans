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
            if (WebFragment.VERSION_6_1.equals(version)) {
                try (InputStream inputStream = fo.getInputStream()) {
                    return org.netbeans.modules.j2ee.dd.impl.web.model_6_1_frag.WebFragment.createGraph(inputStream);
                }
            } else 
                if (WebFragment.VERSION_6_0.equals(version)) {
                try (InputStream inputStream = fo.getInputStream()) {
                    return org.netbeans.modules.j2ee.dd.impl.web.model_6_0_frag.WebFragment.createGraph(inputStream);
                }
            } else if (WebFragment.VERSION_5_0.equals(version)) {
                try (InputStream inputStream = fo.getInputStream()) {
                    return org.netbeans.modules.j2ee.dd.impl.web.model_5_0_frag.WebFragment.createGraph(inputStream);
                }
            } else if (WebFragment.VERSION_4_0.equals(version)) {
                try (InputStream inputStream = fo.getInputStream()) {
                    return org.netbeans.modules.j2ee.dd.impl.web.model_4_0_frag.WebFragment.createGraph(inputStream);
                }
            } else if (WebFragment.VERSION_3_1.equals(version)) {
                try (InputStream inputStream = fo.getInputStream()) {
                    return org.netbeans.modules.j2ee.dd.impl.web.model_3_1_frag.WebFragment.createGraph(inputStream);
                }
            } else if (WebFragment.VERSION_3_0.equals(version)) {
                try (InputStream inputStream = fo.getInputStream()) {
                    return org.netbeans.modules.j2ee.dd.impl.web.model_3_0_frag.WebFragment.createGraph(inputStream);
                }
            } else {
                throw new IOException("Unsupported version of web-fragment.xml found! Version: "+version);
            }
        } catch (RuntimeException ex) {
            throw new SAXException(ex);
        }
    }

}
