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
package org.netbeans.modules.j2ee.sun.dd.api;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Interface representing the root of interfaces bean tree structure.
 *
 *
 */
public interface RootInterface extends CommonDDBean {    
    
    public static final String PROPERTY_STATUS = "dd_status";
    public static final String PROPERTY_VERSION = "dd_version";
    public static final int STATE_INVALID_PARSABLE = 1;
    public static final int STATE_INVALID_UNPARSABLE = 2;
    public static final int STATE_VALID = 0;
    
 
    /** 
     * Changes current DOCTYPE to match version specified.
     * Warning: Only the upgrade from lower to higher version is supported.
     * 
     * @param version 
     */
    public void setVersion(java.math.BigDecimal version);
    
    /** 
     * Version property as defined by the DOCTYPE, if known.
     * 
     * @return current version
     */
    public java.math.BigDecimal getVersion();
    
    /** 
     * Current parsing status
     * 
     * @return status value
     */
    public int getStatus();

    /**
     * Confirms that the DD passed in is the proxied DD owned by this interface
     */
    public boolean isEventSource(RootInterface rootDD);
    
    /** 
     * Writes the deployment descriptor data from deployment descriptor bean graph to file object.<br>
     * This is more convenient method than {@link org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean#write} method.<br>
     * The locking problems are solved for the user in this method.
     *
     * @param fo FileObject for where to write the content of deployment descriptor 
     *   holding in bean tree structure
     * @throws java.io.IOException 
     */
    public void write(FileObject fo) throws IOException;
    
}
