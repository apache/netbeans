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
package org.netbeans.modules.hibernate.loaders.reveng;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.hibernate.reveng.model.HibernateReverseEngineering;
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
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Represents the Hibernate Reverse Engineering file
 * 
 * @author gowri
 */
@MIMEResolver.NamespaceRegistration(
    mimeType=HibernateRevengDataLoader.REQUIRED_MIME,
    displayName="org.netbeans.modules.hibernate.resources.Bundle#HibernateRevengResolver",
    doctypePublicId="-//Hibernate/Hibernate Reverse Engineering DTD 3.0//EN",
    position=1600
)
public class HibernateRevengDataObject extends MultiDataObject {

    private HibernateReverseEngineering revEngineering;
    private static final String VIEW_ID = "hibernate_reveng_multiview_id"; // NOI18N
    private static final String ICON = "org/netbeans/modules/hibernate/resources/hibernate-reveng.png"; //NOI18N
   
    public HibernateRevengDataObject(FileObject pf, HibernateRevengDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        cookies.add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        cookies.add(validateCookie);
        registerEditor(HibernateRevengDataLoader.REQUIRED_MIME, true);
    }
    
    @MultiViewElement.Registration(
        mimeType=HibernateRevengDataLoader.REQUIRED_MIME,
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
     * Gets the object graph representing the contents of the 
     * Hibernate reverse engineering file with which this data object 
     * is associated.
     *
     * @return the persistence graph.
     */
    public HibernateReverseEngineering getHibernateReverseEngineering() {
        if (revEngineering == null) {
            try {
                revEngineering = HibernateRevengMetadata.getDefault().getRoot(getPrimaryFile());
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
        assert revEngineering != null;
        return revEngineering;
    }
    
    /**
     * Adds MyClass object to Mapping 
     * 
     */
    public void addReveng() {
        OutputStream out = null;
        try {            
            out = getPrimaryFile().getOutputStream();
            getHibernateReverseEngineering().write(out);            
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
        return new HibernateRevengDataNode(this);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

}
