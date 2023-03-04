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

package org.netbeans.modules.pdf;

import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Data object representing a PDF file.
 * Only interesting feature is the {@link OpenCookie}
 * which lets you view it in e.g. Acrobat Reader or similar.
 * @author Jesse Glick
 */
@MIMEResolver.ExtensionRegistration(
    extension="pdf",
    mimeType="application/pdf",
    position=370,
    displayName="#PDFResolver"
)
public class PDFDataObject extends MultiDataObject {

    private static final long serialVersionUID = -1073885636989804140L;
    
    public PDFDataObject(FileObject pf, MultiFileLoader loader)
                                            throws DataObjectExistsException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        // [PENDING] try also Java-implemented reader
        File f = FileUtil.toFile(pf);
        if (f != null) {
            cookies.add(new PDFOpenSupport(this));
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PDFDataObject.class);
    }

    @Override
    protected Node createNodeDelegate() {
        return new PDFDataNode(this);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

}
