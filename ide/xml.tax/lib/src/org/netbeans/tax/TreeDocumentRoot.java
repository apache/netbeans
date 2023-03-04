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
package org.netbeans.tax;

import org.netbeans.tax.event.TreeEventManager;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface TreeDocumentRoot {

    /**
     */
    public TreeEventManager getRootEventManager ();


    /**
     */
    public String getVersion ();

    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public void setVersion (String version) throws ReadOnlyException, InvalidArgumentException;

    /**
     */
    public String getEncoding ();

    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public void setEncoding (String encoding) throws ReadOnlyException, InvalidArgumentException;
    
    
    /**
     */
    public TreeObjectList getChildNodes ();
    
}
