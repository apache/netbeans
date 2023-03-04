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
 * PmDescriptors.java
 *
 * Created on November 17, 2004, 4:48 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PmDescriptors extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String PM_DESCRIPTOR = "PmDescriptor";	// NOI18N
    public static final String PM_INUSE = "PmInuse";	// NOI18N

    public PmDescriptor[] getPmDescriptor();
    public PmDescriptor getPmDescriptor(int index);
    public void setPmDescriptor(PmDescriptor[] value);
    public void setPmDescriptor(int index, PmDescriptor value);
    public int addPmDescriptor(PmDescriptor value);
    public int removePmDescriptor(PmDescriptor value);
    public int sizePmDescriptor(); 
    public PmDescriptor newPmDescriptor(); 
    
    /** Setter for pm-inuse property
     * @param value property value
     */
    public void setPmInuse(PmInuse value);
    /** Getter for pm-inuse property.
     * @return property value
     */
    public PmInuse getPmInuse(); 
    public PmInuse newPmInuse(); 
}
