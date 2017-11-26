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
package org.netbeans.modules.hibernate.loaders.mapping;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Represents the Hibernate mapping file
 * 
 * @author Dongmei Cao
 */
@MIMEResolver.NamespaceRegistration(
    mimeType=HibernateMappingDataLoader.REQUIRED_MIME,
    displayName="org.netbeans.modules.hibernate.resources.Bundle#HibernateMappingResolver",
    doctypePublicId="-//Hibernate/Hibernate Mapping DTD 3.0//EN",
    position=1550
)
public class HibernateMappingDataObject extends MultiDataObject {

    private HibernateMapping mapping;
    public static final String VIEW_ID = "hibernate_mapping_multiview_id"; // NOI18N
    public static final String ICON = "org/netbeans/modules/hibernate/resources/hibernate-mapping.png"; //NOI18N

    public HibernateMappingDataObject(FileObject pf, HibernateMappingDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);

        CookieSet cookies = getCookieSet();
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        cookies.add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        cookies.add(validateCookie);
        registerEditor(HibernateMappingDataLoader.REQUIRED_MIME, true);
    }

    @MultiViewElement.Registration(
        mimeType=HibernateMappingDataLoader.REQUIRED_MIME,
        iconBase=ICON,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID=VIEW_ID,
        displayName="#CTL_SourceTabCaption",
        position=1
    )
    @NbBundle.Messages("CTL_SourceTabCaption=Source")
    public static MultiViewEditorElement createXmlMultiViewElement(Lookup lookup) {
        return new MultiViewEditorElement(lookup);
    }  
    
    /**
     * Adds MyClass object to Mapping 
     * 
     */
    public void addMyClass(MyClass myClass) {
        OutputStream out = null;
        try {
            getHibernateMapping().addMyClass(myClass);
            out = getPrimaryFile().getOutputStream();
            getHibernateMapping().write(out);
        } catch (FileAlreadyLockedException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    /**
     * Gets the object graph representing the contents of the 
     * Hibernate mapping file with which this data object 
     * is associated.
     *
     * @return the persistence graph.
     */
    public HibernateMapping getHibernateMapping() {
        if (mapping == null) {
            try {
                mapping = HibernateMappingMetadata.getDefault().getRoot(getPrimaryFile());
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
        assert mapping != null;
        return mapping;
    }

    /**
     * Saves the document.
     * @see EditorCookie#saveDocument
     */
    public void save() {
        EditorCookie edit = (EditorCookie) getCookie(EditorCookie.class);
        if (edit != null) {
            try {
                edit.saveDocument();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    @Override
    protected Node createNodeDelegate() {
        return new HibernateMappingDataNode(this);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
    
    
}
