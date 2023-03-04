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

package org.netbeans.modules.j2ee.ddloaders.client;

//import org.netbeans.modules.j2ee.ddloaders.ejb.EjbJarDataObject;

/** DDChangeEvent describes the change that affects deployment of web application.
 *  Deployment descriptor object can listen to these changes 
 *  and update its configuration according to change.
 *
 * @author  Ludovic Champenois
 */
public class DDChangeEvent extends java.util.EventObject {
    
    /** Event fired when new ejb is added or copied from another location */
    public static final int EJB_ADDED = 1;
    
    /** Event fired when ejb is renamed or moved within one web module */
    public static final int EJB_CHANGED = 2;
    
    /** Event fired when ejb is deleted */
    public static final int EJB_DELETED = 3; // delete

    /** Event fired when ejb is moved */
    public static final int EJB_MOVED = 4;
    
    /** Event fired when ejb is moved from one web module to another one */
    public static final int EJB_HOME_CHANGED = 5;
    
    public static final int EJB_REMOTE_CHANGED = 6;
    
    public static final int EJB_LOCAL_HOME_CHANGED = 7;
    
    public static final int EJB_LOCAL_CHANGED = 8;
    
    public static final int EJB_HOME_DELETED = 9;
    
    public static final int EJB_REMOTE_DELETED = 10;
    
    public static final int EJB_LOCAL_HOME_DELETED = 11;
    
    public static final int EJB_LOCAL_DELETED = 12;
    
    public static final int EJB_CLASS_CHANGED = 13;

    public static final int EJB_CLASS_DELETED = 14;

    /** Newly set value. Usually current classname of ejb if it makes sense. */
    private String newValue;
    
    /** Old value. Usually old classname of ejb if it makes sense. */
    private String oldValue;
    
    /** Event type */
    private int type;
    
    /** placeholder for old depl. descriptor (only for ejb moves) */
    private ClientDataObject oldDD;
    
    /** Creates new event.
     *
     * @param src class name of ejb
     * @param type type of change
     */    
    public DDChangeEvent (Object src, ClientDataObject oldDD, String oldVal, String newVal, int type) {
        super (src);
        newValue = newVal;
        oldValue = oldVal;
        this.type = type;
        this.oldDD = oldDD;
    }
    
    /** Creates new event.
     *
     * @param src class name of ejb
     * @param type type of change
     */    
    public DDChangeEvent (Object src, String oldVal, String newVal, int type) {
        this (src, null, oldVal, newVal, type);
    }
    
    public String getNewValue () {
        return newValue;
    }
    
    public String getOldValue () {
        return oldValue;
    }
    
    public ClientDataObject getOldDD () {
        return oldDD;
    }
    
    /** Getter for change type
     *
     * @return change type
     */    
    public int getType () {
        return type;
    }
    
    public String toString () {
        return "DDChangeEvent "+getSource ()+" of type "+type; // NOI18N
    }
    
}
