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

package org.netbeans.modules.j2ee.ddloaders.web;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.dd.impl.web.WebParseUtils;
import org.netbeans.modules.j2ee.ddloaders.multiview.DDMultiViewDataObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Represents a DD object in the Repository.
 * Based on DDDataObject class.
 *
 * @author Petr Slechta
 */
public class DDFragmentDataObject extends DDDataObject {
    private transient WebApp webApp;
    private static final long serialVersionUID = 8857563089355069363L;
    private static final Logger LOG = Logger.getLogger(DDMultiViewDataObject.class.getName());

    public DDFragmentDataObject (FileObject pf, DDDataLoader loader, String editorMimeType) throws DataObjectExistsException {
        super (pf, loader, editorMimeType);
    }

    @Override
    public WebApp getWebApp() {
        if (webApp == null) {
            try {
                webApp = createWebFrag();
            } catch (IOException ex) {
                LOG.log(Level.INFO, "getWebApp failed", ex);
            }
        }
        return webApp;
    }

    private WebApp createWebFrag() throws java.io.IOException {
        WebApp webFrag = WebFragmentProvider.getDefault().getWebFragmentRoot(getPrimaryFile());
        if (webFrag != null) {
            setSaxError(webFrag.getError());
        }
        return webFrag;
    }

    @Override
    protected void parseDocument() throws IOException {
        if (webApp == null) {
            try {
                webApp = WebFragmentProvider.getDefault().getWebFragmentRoot(getPrimaryFile());
            } catch (IOException ex) {
                LOG.log(Level.INFO, "parseDocument failed", ex);
            }
        }
    }

    @Override
    protected String getPrefixMark() {
        return "<web-fragment";
    }

    @Override
    protected boolean isModelCreated() {
        return (webApp!=null);
    }

    @Override
    public boolean isDeleteAllowed() {
        return true;
    }

    @Override
    protected void validateDocument() throws IOException {
        InputSource is = new InputSource(createReader());
        try {
            SAXParseException error = WebParseUtils.parse(is);
            if (error != null)
                throw error;
        }
        catch (SAXException ex) {
            LOG.log(Level.SEVERE, "Parsing failed!", ex);
            throw new IOException("Parsing failed: "+ex);
        }
    }

}
