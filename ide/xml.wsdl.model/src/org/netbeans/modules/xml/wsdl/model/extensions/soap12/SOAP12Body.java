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

package org.netbeans.modules.xml.wsdl.model.extensions.soap12;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author Sujit Biswas
 * Represents the body element under the wsdl:input or wsdl:output element for SOAP binding
 */
public interface SOAP12Body extends SOAP12MessageBase {
    public static final String PARTS_PROPERTY = "parts";
    
    /**
     * @return list of parts correspond to the RPC call parameter list.
     */
    List<Reference<Part>> getPartRefs();
    List<String> getParts();

    /**
     * Set list of references to message parts.
     */
    void setParts(List<String> parts);
    void setPartRefs(List<Reference<Part>> parts);
    
    
    /**
     * Append message part to part list.
     */
    void addPart(String part);
    void addPartRef(Reference<Part> ref);
    
    /**
     * Add message part to part list at specified index.
     */
    void addPart(int index, String part);
    void addPartRef(int index, Reference<Part> ref);
    
    /**
     * Remove given part.
     */
    void removePart(String part);
    void removePartRef(Reference<Part> partRef);
}
