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

/**
 * Provider of model based on persistence_ORM.xsd schema.
 * Provided model is merged representation of metadata defined in both
 * annotations and deployment descriptor with filled default values
 * as defined in persistence specification.
 *
 * @author  Martin Adamek
 */
public final class ORMMetadata {
    private static final ORMMetadata instance = new ORMMetadata();
    
    private ORMMetadata() {
        // TODO: RETOUCHE
        //        annotationDDMap = new HashMap<MetadataUnit, EntityMappings>(5);
    }
    
    /**
     * Use this to get singleton instance of provider
     *
     * @return singleton instance
     */
    public static ORMMetadata getDefault() {
        return instance;
    }
    
    public boolean isScanInProgress() {
        // TODO: RETOUCHE
        //        return NNMDRListener.getDefault().isScanInProgress();
        return false;
    }
    
    public void waitScanFinished() {
        // TODO: RETOUCHE
        //NNMDRListener.getDefault().waitScanFinished();

    }
}
