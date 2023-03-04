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

/*
 * ResourceRetriever.java
 *
 * Created on January 9, 2006, 2:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 *
 * @author girix
 */
public interface ResourceRetriever {
    /**
     * This method will be called by the factory class. Impl should decide if it supports the protocol
     * @param baseAddr originating documents address
     * @param currentAddr address of the document that needs to be fetched - as mentioned in the base document.
     * @return if impl supports then true else false
     */
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException;
    
    /**
     * Given the base doc address and the current address (that could be either relative or absolute), determine the final address to fetch doc and get the stream of that doc.
     * The method may throw {@link ResourceRedirectException} if the Retriever resolves the resource
     * to a different URL incompatible with the Retriever instance.
     * 
     * @param baseAddress address of the base document where the link was found
     * @param documentAddress current document address as mentioned in the base doc
     * @return Map has the "key" as the final address from where the file was fetched and "value" has the input stream of the file.
     */
    public Map<String,InputStream> retrieveDocument(
            String baseAddress, String documentAddress) throws IOException,URISyntaxException;
    
    
     /**
     * Given the base doc address and the current address (that could be either relative or absoulte), determine the final address to fetch doc and get the stream of that doc.
     * @param baseAddress address of the base document where the link was found
     * @param documentAddress current document address as mentioned in the base doc
     * @return Hash map has the "key" as the final address from where the file was fetched and "value" has the input stream of the file.
     */
    public String getEffectiveAddress(
            String baseAddress, String documentAddress) throws IOException,URISyntaxException;
    
    
    /**
     * Must be called after retrieveDocument() method. 
     * This returns the number of chars in the stream. 
     * Useful for URL retriever where its better to know the length upfront.
     */
    public long getStreamLength();
}
