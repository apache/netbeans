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

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/** Loader for PDF files (Portable Document Format).
 * Permits simple viewing of them.
 * @author Jesse Glick
 */
public class PDFDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    private static final long serialVersionUID = -4354042385752587850L;
    /** MIME-type of PDF files */
    private static final String PDF_MIME_TYPE = "application/pdf";      //NOI18N

    
    /** Creates loader. */
    public PDFDataLoader() {
        super("org.netbeans.modules.pdf.PDFDataObject"); // NOI18N
    }

    
    /** Initizalized loader, i.e. its extension list. Overrides superclass method. */
    @Override
    protected void initialize () {
        super.initialize();

        ExtensionList extensions = new ExtensionList ();
        extensions.addMimeType(PDF_MIME_TYPE);
        extensions.addMimeType("application/x-pdf");                    //NOI18N
        extensions.addMimeType("application/vnd.pdf");                  //NOI18N
        extensions.addMimeType("application/acrobat");                  //NOI18N
        extensions.addMimeType("text/pdf");                             //NOI18N
        extensions.addMimeType("text/x-pdf");                           //NOI18N
        setExtensions (extensions);
    }
    
    /** Gets default display name. Overrides superclass method. */
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage (PDFDataLoader.class, "LBL_loaderName");
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    @Override
    protected String actionsContext () {
        return "Loaders/application/pdf/Actions/";                      //NOI18N
    }
    
    /** Creates multi data objcte for specified primary file.
     * Implements superclass abstract method. */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new PDFDataObject (primaryFile, this);
    }

}
