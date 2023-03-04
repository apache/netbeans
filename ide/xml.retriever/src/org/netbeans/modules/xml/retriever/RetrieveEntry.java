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
package org.netbeans.modules.xml.retriever;

import java.io.File;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;


public class RetrieveEntry{
    //address from where this file was refered. Null for root file.
    private String baseAddress;
    //relative/abs address of the external entry
    private String currentAddress;
    //location of the base file
    private File localBaseFile = null;
    //location where this file has to be stroed or was stored
    private File saveFile = null;
    //retrieve this entry recursively
    private boolean recursive = false;
    //what is the type of this document.
    private DocumentTypesEnum docType = DocumentTypesEnum.schema;
    //final abs address from where the file was retrieved
    private String effectiveAddress = null;
    
    public RetrieveEntry(String baseAddress, String currentAddress, File localBaseFile, File saveFile, DocumentTypesEnum docType, boolean recursive){
        this.baseAddress = baseAddress;
        this.currentAddress = currentAddress;
        this.localBaseFile = localBaseFile;
        this.saveFile = saveFile;
        this.setDocType(docType);
        this.setRecursive(recursive);
    }
    
    public String getBaseAddress() {
        return baseAddress;
    }
    
    public void setBaseAddress(String baseAddress) {
        this.baseAddress = baseAddress;
    }
    
    public String getCurrentAddress() {
        return currentAddress;
    }
    
    public File getLocalBaseFile() {
        return localBaseFile;
    }
    
    public void setLocalBaseFile(File localBaseFile) {
        this.localBaseFile = localBaseFile;
    }
    
    public File getSaveFile() {
        return saveFile;
    }
    
    public void setSaveFile(File saveFile) {
        this.saveFile = saveFile;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public DocumentTypesEnum getDocType() {
        return docType;
    }

    public void setDocType(DocumentTypesEnum docType) {
        this.docType = docType;
    }
    //NOI18N
    public String toString(){
        return "base:" +this.baseAddress+
                "\n\tcur:" +this.currentAddress+
                "\n\tbFile:" +this.localBaseFile+
                "\n\tcFile:" +this.saveFile+
                "\n\tdType:" +this.docType+
                "\n\trec:"+this.recursive;
    }

    public String getEffectiveAddress() {
        return effectiveAddress;
    }

    public void setEffectiveAddress(String effectiveAddress) {
        this.effectiveAddress = effectiveAddress;
    }
}
