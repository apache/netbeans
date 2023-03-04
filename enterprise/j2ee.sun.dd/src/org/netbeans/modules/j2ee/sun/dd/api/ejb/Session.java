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
 * Session.java
 *
 * Created on November 17, 2004, 5:21 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Session extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String CHECKPOINT_LOCATION = "CheckpointLocation";	// NOI18N
    public static final String QUICK_CHECKPOINT = "QuickCheckpoint";	// NOI18N
    public static final String CHECKPOINTED_METHODS = "CheckpointedMethods";	// NOI18N

    /** Setter for checkpoint-location property
     * @param value property value
     */
    public void setCheckpointLocation(java.lang.String value);
    /** Getter for checkpoint-location property.
     * @return property value
     */
    public java.lang.String getCheckpointLocation();
    /** Setter for quick-checkpoint property
     * @param value property value
     */
    public void setQuickCheckpoint(java.lang.String value);
    /** Getter for quick-checkpoint property.
     * @return property value
     */
    public java.lang.String getQuickCheckpoint();
    /** Setter for checkpointed-methods property
     * @param value property value
     */
    public void setCheckpointedMethods(CheckpointedMethods value);
    /** Getter for checkpointed-methods property.
     * @return property value
     */
    public CheckpointedMethods getCheckpointedMethods(); 
    
    public CheckpointedMethods newCheckpointedMethods();
    
}
