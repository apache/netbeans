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
package org.netbeans.modules.websvc.wsitmodelext.catalog;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlQName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing13WsdlQName;
import org.netbeans.modules.websvc.wsitmodelext.mex.MexQName;
import org.netbeans.modules.websvc.wsitmodelext.mtom.MtomQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMMS13QName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMMSQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunClientQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityQName;
import org.netbeans.modules.websvc.wsitmodelext.trust.TrustQName;
import org.netbeans.modules.websvc.wsitmodelext.tx.TxQName;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;

/** Catalog for WSIT related schemas that enables completion support in editor.
 *
 * @author Martin Grebac
 *
 */
public class WSITSchemaCatalog implements CatalogReader, CatalogDescriptor2, EntityResolver {

    private Map<String, String> localSchemaDefs = null;

    public WSITSchemaCatalog() {}

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public Iterator<String> getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.addAll(getLocalSchemaDefs().keySet());
        return list.listIterator();
    }

    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (getLocalSchemaDefs().containsKey(publicId)) {
            return getLocalSchemaDefs().get(publicId);
        }
        return null;
    }

    private synchronized Map<String, String> getLocalSchemaDefs() {
        if (localSchemaDefs == null) {
            localSchemaDefs = new HashMap<>();
            localSchemaDefs.putAll(Addressing10QName.ADDRESS.getSchemaLocations(true));
            localSchemaDefs.putAll(Addressing10WsdlQName.USINGADDRESSING.getSchemaLocations(true));
            localSchemaDefs.putAll(Addressing13WsdlQName.ADDRESSING.getSchemaLocations(true));
            localSchemaDefs.putAll(MexQName.METADATA.getSchemaLocations(true));
            localSchemaDefs.putAll(MtomQName.OPTIMIZEDMIMESERIALIZATION.getSchemaLocations(true));
            localSchemaDefs.putAll(PolicyQName.ALL.getSchemaLocations(true));
            localSchemaDefs.putAll(RMMSQName.MAXRECEIVEBUFFERSIZE.getSchemaLocations(true));
            localSchemaDefs.putAll(RMMS13QName.INACTIVITYTIMEOUT.getSchemaLocations(true));
            localSchemaDefs.putAll(RMQName.RMASSERTION.getSchemaLocations(true));
            localSchemaDefs.putAll(RMSunQName.ALLOWDUPLICATES.getSchemaLocations(true));
            localSchemaDefs.putAll(RMSunClientQName.ACKREQUESTINTERVAL.getSchemaLocations(true));
            localSchemaDefs.putAll(SecurityPolicyQName.BODY.getSchemaLocations(true));
            localSchemaDefs.putAll(SecurityQName.SECPOLID.getSchemaLocations(true));
            localSchemaDefs.putAll(TrustQName.TOKENTYPE.getSchemaLocations(true));
            localSchemaDefs.putAll(TxQName.ATASSERTION.getSchemaLocations(true));
        }
        return localSchemaDefs;
    }

    /**
     * Refresh content according to content of mounted catalog.
     */
    public void refresh() {
    }

    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
    }

    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void removeCatalogListener(CatalogListener l) {
    }

    /** Registers new listener.  */
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return "";
    }

    /**
     * Return visualized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public String getIconResource(int type) {
        return null;
    }

    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return "";
    }

    /** Unregister the listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * Resolves schema definition file for deployment descriptor (spec.2_4)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (getLocalSchemaDefs().containsKey(publicId)) {
            return new InputSource(getLocalSchemaDefs().get(publicId));
        }
        return null;
    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
        if (getLocalSchemaDefs().containsKey(name)) {
            return getLocalSchemaDefs().get(name);
        }
        return null;
    }

    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */
    public String resolvePublic(String publicId) {
        return null;
    }
}
