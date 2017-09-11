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

package org.netbeans.modules.xml.schema.model.resolver;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.resolver.FileObjectModelAccessProvider;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A simple CatalogModel is intended for JUnit tests.
 * It is necessary to resolve references between different XAM based
 * models. Only file path references can be used with the TestCatalogModel.
 * Don't use GlobalCatalog or project references. It's good idea to put
 * all necessary files to the same folder. Files can also be located inside
 * of ZIP file. 
 *
 * @author Nikita Krjukov
 */

public class TestCatalogModel2 implements CatalogModel {

    private static TestCatalogModel2 singleton = new TestCatalogModel2(true);

    public static CatalogModel getDefault() {
        return singleton;
    }

    private Map<FileObject, Document> mFileToDocumentMap;
    private boolean mDocumentPooling = true;

    public TestCatalogModel2(boolean documentPooling) {
        mDocumentPooling = documentPooling;
    }

    public ModelSource getModelSource(URI locationURI,
            ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        //
        FileObject sourceFo = modelSourceOfSourceDocument.getLookup().lookup(FileObject.class);
        assert sourceFo != null : "Source file object has to be specified in the lookup";
        //
        FileObject fo = sourceFo.getParent().getFileObject(locationURI.toString());
        // assert fo != null : "Unknown file: " + locationURI.toString();
        if (fo == null) {
            return null;
        }
        //
        Document doc = getDocument(fo);
        assert doc != null : "Can't load the document: " + locationURI.toString();
        //
        Lookup lookup = Lookups.fixed(fo, doc, this, FileObjectModelAccessProvider.getDefault());
        ModelSource ms = new ModelSource(lookup, modelSourceOfSourceDocument.isEditable());
        //
        return ms;
    }

    private Document getDocument(FileObject fo){
        Document result = null;
        if (mDocumentPooling) {
            result = documentPool().get(fo);
        }
        //
        if (result == null) {
            try {
                result = Util.loadDocument(fo.getInputStream());
                if (mDocumentPooling) {
                    documentPool().put(fo, result);
                }
            } catch (Exception ex) {
                return null;
            }
        }
        //
        return result;
    }
    
    private Map<FileObject,Document> documentPool() {
        if (mFileToDocumentMap == null) {
            mFileToDocumentMap = new HashMap<FileObject,Document>();
        }
        return mFileToDocumentMap;
    }
    
    public void setDocumentPooling(boolean v) {
        mDocumentPooling = v;
        if (! mDocumentPooling) {
            clearDocumentPool();
        }
    }

    public void clearDocumentPool() {
        mFileToDocumentMap = null;
    }

    //------------------------------------

    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}

