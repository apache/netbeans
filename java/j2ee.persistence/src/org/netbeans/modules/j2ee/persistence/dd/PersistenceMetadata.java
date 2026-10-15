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

package org.netbeans.modules.j2ee.persistence.dd;

import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.persistence.dd.common.JPAParseUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;

/**
 * Provider of model based on persistence.xsd schema.
 * Provided model is representation of deployment descriptor
 * as defined in persistence specification.
 *
 * @author Martin Adamek
 */
public final class PersistenceMetadata {
    
    private static final PersistenceMetadata DEFAULT = new PersistenceMetadata();
    private Map<FileObject, Persistence> ddMap;
    
    private PersistenceMetadata() {
        ddMap = new WeakHashMap<>(5);
    }
    
    /**
     * Use this to get singleton instance of provider
     *
     * @return singleton instance
     */
    public static PersistenceMetadata getDefault() {
        return DEFAULT;
    }
    
    /**
     * Provides root element as defined in persistence.xsd
     * 
     * @param fo persistence.xml deployment descriptor. 
     * It can be retrieved from {@link PersistenceProvider} for any file
     * @throws java.io.IOException 
     * @return root element of schema or null if it doesn't exist for provided 
     * persistence.xml deployment descriptor
     * @see PersistenceProvider
     */
    public Persistence getRoot(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        Persistence persistence = null;
        synchronized (ddMap) {
            persistence = ddMap.get(fo);
            if (persistence == null) {
                String version=Persistence.VERSION_1_0;
                try (InputStream is=fo.getInputStream()) {
                    version=JPAParseUtils.getVersion(is);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                }

                try (InputStream is=fo.getInputStream()) {
                    if (Persistence.VERSION_3_2.equals(version)) {
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.Persistence.createGraph(is);
                    } else if(Persistence.VERSION_3_1.equals(version)) {
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.Persistence.createGraph(is);
                    } else if(Persistence.VERSION_3_0.equals(version)) {
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.Persistence.createGraph(is);
                    } else if(Persistence.VERSION_2_2.equals(version)) {
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.Persistence.createGraph(is);
                    } else if(Persistence.VERSION_2_1.equals(version)) {
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.Persistence.createGraph(is);
                    } else if(Persistence.VERSION_2_0.equals(version)) {
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.Persistence.createGraph(is);
                    } else {//1.0 - default
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence.createGraph(is);
                    }
                }
                ddMap.put(fo, persistence);
            }
        }
        return persistence;
    }

    /**
     * provide a way to refresh metadata in cashe if required
     * @param fo
     */
    public void refresh(FileObject fo){
        synchronized (ddMap) {
            if( fo!=null ) {
                ddMap.remove(fo);
            }
        }
    }

}
