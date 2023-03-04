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
package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.*;

/**
 * @author girix
 */
public class FileResourceRetriever implements ResourceRetriever{
    
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException {
        
        URI currURI = new URI(currentAddr);
        if( (currURI.isAbsolute()) && (currURI.getScheme().equalsIgnoreCase("file"))) //NOI18N
            return true;
        if(!currURI.isAbsolute() && (baseAddr == null))
            return true;
        if(baseAddr != null){
            if(!currURI.isAbsolute()){
                URI baseURI = new URI(baseAddr);
                if(baseURI.getScheme().equalsIgnoreCase("file")) //NOI18N
                    return true;
            }
        }
        return false;
    }
    
    long streamLength = 0;

    public HashMap<String, InputStream> retrieveDocument(String baseAddress, String documentAddress) throws IOException,URISyntaxException{
        String address = getEffectiveAddress(baseAddress, documentAddress);

        if (address == null) {
            return new HashMap<String, InputStream>();
        }
        URI currURI = new URI(address);
        HashMap<String, InputStream> result = null;
        File curFile = new File(currURI);
    
        if(curFile.isFile()){
            InputStream is = new FileInputStream(curFile);
            result = new HashMap<String, InputStream>();
            result.put(curFile.toURI().toString(), is);
            streamLength = curFile.length();
            return result;
        }else{
            //file not found in the system
            throw new IOException("File not found: "+curFile.toString()); //NOI18N
        }
    }
    
    public long getStreamLength() {
        return streamLength;
    }
    
    public String getEffectiveAddress(String baseAddress, String documentAddress) throws IOException, URISyntaxException {
//System.out.println();
//System.out.println("baseAddress: " + baseAddress);
//System.out.println("documentAddress: " + documentAddress);
        URI currURI = new URI(documentAddress);
        if(currURI.isAbsolute()){
            //abs file URI
            return currURI.toString();
        }else{
            //relative URI
            if(baseAddress != null){
                URI baseURI = new URI(baseAddress);
                return (baseURI.resolve(currURI)).toString();
            }else{
                //neither the current URI nor the base URI are absoulte. So, can not resolve this
                //path
//System.out.println("    return null");
                return null;
            }
        }
    }
}
