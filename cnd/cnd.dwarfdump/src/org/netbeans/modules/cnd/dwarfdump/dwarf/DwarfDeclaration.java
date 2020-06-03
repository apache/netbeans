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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

/**
 *
 */
public class DwarfDeclaration {
    public final String kind;
    public final String declarationString;
    public final String declarationFile;    
    public final String declarationPosition;
            
    public DwarfDeclaration(String kind, String declarationString, String declarationFile, String declarationPosition) {
        this.kind = kind;
        this.declarationString = declarationString;
        this.declarationFile = declarationFile;
        this.declarationPosition = declarationPosition;
    }
    
    @Override
    public String toString() {
        return kind + " " + declarationString + " " + declarationFile + ":" + declarationPosition; // NOI18N
    }
}
