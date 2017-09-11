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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hibernate.catalog;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class registers Hibernate specific DTDs in the NetBeans DTD and Schema 
 * catalog.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateCatalog implements CatalogReader, CatalogDescriptor2, 
        EntityResolver{

    private String CONFIG_PUBLIC_ID = "-//Hibernate/Hibernate Configuration DTD 3.0//EN"; //NOI18N
    private String CONFIG_DTD = "hibernate-configuration-3.0.dtd"; //NOI18N
    private String CONFIG = "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd"; //NOI18N
    private String CONFIG_DTD_URL = "nbres:/org/netbeans/modules/hibernate/resources/" + CONFIG_DTD; //NOI18N
    
    private String MAPPING_PUBLIC_ID = "-//Hibernate/Hibernate Mapping DTD 3.0//EN"; //NOI18N
    private String MAPPING_DTD = "hibernate-mapping-3.0.dtd"; //NOI18N
    private String MAPPING = "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd"; //NOI18N
    private String MAPPING_DTD_URL = "nbres:/org/netbeans/modules/hibernate/resources/" + MAPPING_DTD; //NOI18N

    private String REVERSE_ENG_PUBLIC_ID = "-//Hibernate/Hibernate Reverse Engineering DTD 3.0//EN"; //NOI18N
    private String REVERSE_ENG_DTD = "hibernate-reverse-engineering-3.0.dtd"; //NOI18N
    private String REVERSE_ENG = "http://hibernate.sourceforge.net/hibernate-reverse-engineering-3.0.dtd"; //NOI18N
    private String REVERSE_ENG_DTD_URL = "nbres:/org/netbeans/modules/hibernate/resources/" + REVERSE_ENG_DTD; //NOI18N

    public Iterator getPublicIDs() {
        List<String> publicIdList = new ArrayList<String>();
        publicIdList.add(CONFIG_PUBLIC_ID);
        publicIdList.add(MAPPING_PUBLIC_ID);
        publicIdList.add(REVERSE_ENG_PUBLIC_ID);

        return publicIdList.listIterator();
    }

    public void refresh() {
        //TODO Investigate the necessity of this implementation.
    }

    public String getSystemID(String publicId) {
        
        if(CONFIG_PUBLIC_ID.equals(publicId)) {
            return CONFIG_DTD_URL;
        } else if(MAPPING_PUBLIC_ID.equals(publicId)) {
            return MAPPING_DTD_URL;
        } else if(REVERSE_ENG_PUBLIC_ID.equals(publicId)) {
            return REVERSE_ENG_DTD_URL;
        }
        
        return null;
    }

    public String resolveURI(String name) {
        return null;
    }

    public String resolvePublic(String publicId) {
        return null;
    }

    public void addCatalogListener(CatalogListener l) {
        
    }

    public void removeCatalogListener(CatalogListener l) {
        
    }

    public String getIconResource(int type) {
        return "org/netbeans/modules/hibernate/resources/hibernate-configuration.png"; //NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(HibernateCatalog.class, "LBL_HibernateCatalog"); //NOI18N
    }

    public String getShortDescription() {
        return NbBundle.getMessage(HibernateCatalog.class, "LBL_HibernateCatalogDescription"); //NOI18N
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (CONFIG_PUBLIC_ID.equals(publicId)) {
            return new org.xml.sax.InputSource(CONFIG_DTD_URL);
        } else if(MAPPING_PUBLIC_ID.equals(publicId)) {
            return new org.xml.sax.InputSource(MAPPING_DTD_URL);
        } else if(REVERSE_ENG_PUBLIC_ID.equals(publicId)) {
            return new org.xml.sax.InputSource(REVERSE_ENG_DTD_URL);
        }
        
        return null;
    }
}
