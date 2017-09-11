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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * RuntimeCatalogModel.java
 *
 * Created on January 18, 2007, 2:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.completion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.swing.text.Document;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author girix
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.locator.CatalogModel.class)
public class RuntimeCatalogModel implements CatalogModel{
    
    /** Creates a new instance of RuntimeCatalogModel */
    public RuntimeCatalogModel() {
    }
    
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
    public ModelSource getModelSource(URI locationURI,
            ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        InputStream inputStream = null;
        try {
            UserCatalog cat = UserCatalog.getDefault();
            // mainly for unit tests
            if (cat == null) {
                return null;
            }
            EntityResolver resolver = cat.getEntityResolver();
            InputSource src = resolver.resolveEntity(null, locationURI.toString());
            if(src != null) {
                inputStream = new URL(src.getSystemId()).openStream();
            } else {
                javax.xml.transform.Source isrc = ((javax.xml.transform.URIResolver)resolver).
                        resolve(locationURI.toString(), null);
                if(isrc != null)
                    inputStream = new URL(isrc.getSystemId()).openStream();
            }
            if(inputStream != null)
                return createModelSource(inputStream);
        } catch (Exception ex) {
            throw new CatalogModelException(ex);
        }
        
        return null;
    }
    
    private ModelSource createModelSource(InputStream is) throws CatalogModelException{
        try {
            Document d = AbstractDocumentModel.getAccessProvider().loadSwingDocument(is);
            if(d != null)
                return new ModelSource(Lookups.fixed(new Object[]{this,d}), false);
        } catch (Exception ex) {
            throw new CatalogModelException(ex);
        }
                
        return null;
    }
    
    public InputSource resolveEntity(String publicId,
            String systemId) throws SAXException, IOException {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
    public LSInput resolveResource(String type, String namespaceURI,
            String publicId, String systemId, String baseURI) {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
}
