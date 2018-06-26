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
