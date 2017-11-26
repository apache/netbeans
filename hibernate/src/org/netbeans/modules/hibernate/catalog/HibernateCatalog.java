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
