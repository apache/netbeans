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
