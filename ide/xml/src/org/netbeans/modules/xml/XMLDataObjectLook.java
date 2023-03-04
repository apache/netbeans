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
package org.netbeans.modules.xml;

import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;
import org.openide.nodes.Node;

/**
 * Interface implemented by DTDDataObject and XMLDataObject.
 *
 * @author Libor Kramolis
 */
public interface XMLDataObjectLook {

    // property change

    public void addPropertyChangeListener (java.beans.PropertyChangeListener l);


    // node

    public org.openide.nodes.Node getNodeDelegate ();


    // cookie

    public <T extends Node.Cookie> T getCookie(Class<T> type);

    // data object

    public String getName ();

    public void setModified (boolean modif);

    public boolean isModified ();


    // sync support
    
//      public int getTreeStatus ();

    public Synchronizator getSyncInterface ();

//     public void updateTextDocument ();
    
    // it cannotbe a cookie because a cookie can not be called from <init>
    public DataObjectCookieManager getCookieManager();

}
