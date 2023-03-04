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
package org.netbeans.modules.netserver.api;


/**
 * @author ads
 *
 */
public final class ProtocolDraft {
    
    public enum Draft {
        Draft75,
        Draft76,
    }

    private ProtocolDraft( Draft draft ){
        this.draft = draft;
        version = 0;
    }
    
    private ProtocolDraft( int version){
        draft = null;
        this.version = version;
    }
    
    private ProtocolDraft( ){
        draft = null;
        version = 0;
    }
    
    public static ProtocolDraft getProtocol( int number ){
        if ( number == 75 ){
            return new ProtocolDraft( Draft.Draft75 );
        }
        else if ( number == 76 ){
            return new ProtocolDraft( Draft.Draft76 );
        }
        else if ( number >=7 && number < 13){
            return new ProtocolDraft(number);
        }
        else if (number >=13 && number <=17){
            return new ProtocolDraft();
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    
    public static ProtocolDraft getRFC(){
        return new ProtocolDraft();
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof ProtocolDraft){
            ProtocolDraft protocol = (ProtocolDraft)obj;
            return draft == protocol.draft && version == protocol.version;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        if ( draft == Draft.Draft75){
            return 75;
        }
        else if ( draft == Draft.Draft76){
            return 76; 
        }
        else {
            return version;
        }
    }
    
    public Draft getDraft(){
        return draft;
    }
    
    public int getVersion(){
        return version;
    }
    
    public boolean isRFC(){
        return draft == null && version ==0;
    }
    
    private final Draft draft;
    private final int version;
}
