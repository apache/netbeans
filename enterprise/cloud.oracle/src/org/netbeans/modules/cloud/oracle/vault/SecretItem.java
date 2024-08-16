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
package org.netbeans.modules.cloud.oracle.vault;

import java.util.Date;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;

/**
 *
 * @author Jan Horvath
 */
public class SecretItem extends OCIItem {
    
    private String lifecycleState;
    private Date deletionTime;

    public SecretItem(OCID id, String compartmentId, String name, String lifecycleState, Date deletionTime) {
        super(id, compartmentId, name);
        this.lifecycleState = lifecycleState;
        this.deletionTime = deletionTime;
    }

    public SecretItem() { 
        super();
        this.lifecycleState = null;
        this.deletionTime = null;
    }
    
    @Override
    public int maxInProject() {
        return Integer.MAX_VALUE;
    }
    
    public Date getDeletionTime() {
        return this.deletionTime;
    }
    
    void setDeletionTime(Date deletionTime) {
        this.deletionTime = deletionTime;
    }
    
    public String getLifecycleState() {
        return this.lifecycleState;
    }
    
    void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }
}
