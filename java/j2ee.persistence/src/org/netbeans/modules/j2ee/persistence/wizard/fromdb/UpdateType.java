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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import org.openide.util.NbBundle;

/**
 *
 * @author answer
 */
public enum UpdateType{
    NEW(NbBundle.getMessage(UpdateType.class, "LBL_Type_New")),             //NOI18N
    RECREATE(NbBundle.getMessage(UpdateType.class, "LBL_Type_Recreate")),   //NOI18N
    UPDATE(NbBundle.getMessage(UpdateType.class, "LBL_Type_Update"));       //NOI18N

    private String name;

    private UpdateType(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
