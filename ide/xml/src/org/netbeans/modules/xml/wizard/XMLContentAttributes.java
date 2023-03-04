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

package org.netbeans.modules.xml.wizard;

import java.util.Map;

/**
 *
 * @author Sonali
 */
public class XMLContentAttributes {
    
    public XMLContentAttributes(String prefix) {
        this.prefix=prefix;
    }
    
    public int getPreferredOccurences(){
        return PREFERRED;
    }
    
    public void setPreferredOccurences(int i){
        PREFERRED = i;
    }
    
    public boolean generateOptionalAttributes(){
            return optionalAttributes;
    }
    
    public void setOptionalAttributes(boolean value){
        optionalAttributes = value;
    }
    
    public boolean generateOptionalElements(){
        return optionalElements;
    }

    public void setOptionalElements(boolean value){
        optionalElements = value;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String pre) {
        prefix = pre;
    }
    
    public void setDepthPreferrence(int value) {
       DEPTH = value;
    }
    
    public int getDepthPreferrence(){
        return DEPTH;
    }
    
    public void setNamespaceToPrefixMap(Map<String, String> nsMap){
        nsToPre = nsMap;
    }
    
    public Map<String, String> getNamespaceToPrefixMap(){
        return nsToPre;
   
    }
    private int PREFERRED = 3;
    private boolean optionalAttributes = true;
    private boolean optionalElements = true;  
    private String prefix="test_prefix";
    private int DEPTH = 2;
    private Map<String, String> nsToPre;

    

}
