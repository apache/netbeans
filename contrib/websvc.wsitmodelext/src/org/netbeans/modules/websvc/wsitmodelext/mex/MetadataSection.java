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

package org.netbeans.modules.websvc.wsitmodelext.mex;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Martin Grebac
 */
public interface MetadataSection extends ExtensibilityElement {
    
    public static final String METADATAREFERENCE_PROPERTY = "METADATAREFERENCE";     //NOI18N
    public static final String LOCATION_PROPERTY = "LOCATION";      //NOI18N
    public static final String DIALECT_PROPERTY = "DIALECT";        //NOI18N
    public static final String IDENTIFIER_PROPERTY = "IDENTIFIER";  //NOI18N
    
    MetadataReference getMetadataReference();
    void setMetadataReference(MetadataReference mSection);
    void removeMetadataReference(MetadataReference mSection);

    Location getLocation();
    void setLocation(Location loc);
    void removeLocation(Location loc);

    Dialect getDialect();
    void setDialect(Dialect dialect);
    void removeDialect(Dialect dialect);

    Identifier getIdentifier();
    void setIdentifier(Identifier id);
    void removeIdentifier(Identifier id);
}
