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

package org.netbeans.modules.j2ee.ddloaders.web.event;

import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;

/** DDChangeEvent describes the change that affects deployment of web application.
 *  Deployment descriptor object can listen to these changes
 *  and update its configuration according to change.
 *
 * @author  Radim Kubacki
 */
public class DDChangeEvent extends java.util.EventObject {

    /** Event fired when new servlet is added or copied from another location */
    public static final int SERVLET_ADDED = 1;

    /** Event fired when servlet is renamed or moved within one web module */
    public static final int SERVLET_CHANGED = 2;
    
    /** Event fired when servlet is deleted */
    public static final int SERVLET_DELETED = 3; // delete
    
    /** Event fired when servlet is moved from one web module to another one */
    public static final int SERVLET_MOVED = 4;
    
    /** Event fired when new filter is added or copied from another location */
    //public static final int FILTER_ADDED = 5;
    
    /** Event fired when filter is renamed or moved within one web module */
    public static final int FILTER_CHANGED = 6;
    
    /** Event fired when filter is deleted */
    public static final int FILTER_DELETED = 7;
    
    /** Event fired when listener is moved from one web module to another one */
    //public static final int FILTER_MOVED = 8;
    
    /** Event fired when new listener is added or copied from another location */
    //public static final int LISTENER_ADDED = 9;
    
    /** Event fired when listener is renamed or moved within one web module */
    public static final int LISTENER_CHANGED = 10;
    
    /** Event fired when listener is deleted */
    public static final int LISTENER_DELETED = 11;
    
    /** Event fired when listener is moved from one web module to another one */
    //public static final int LISTENER_MOVED = 12;
    
    /** Event fired when new JSP is added or copied from another location */
    //public static final int JSP_ADDED = 13;
    
    /** Event fired when JSP is renamed or moved within one web module */
    public static final int JSP_CHANGED = 14;
    
    /** Event fired when JSP is deleted */
    public static final int JSP_DELETED = 15;
    
    /** Event fired when JSP is moved from one web module to another one */
    //public static final int JSP_MOVED = 16;
    
    /** Newly set value. Usually current classname of servlet if it makes sense. */
    private String newValue;
    
    /** Old value. Usually old classname of servlet if it makes sense. */
    private String oldValue;
    
    /** Event type */
    private int type;
    
    /** placeholder for old depl. descriptor (only for servlet moves) */
    private DDDataObject oldDD;
    
    /** Creates new event.
     *
     * @param src class name of servlet
     * @param type type of change
     */    
    public DDChangeEvent (Object src, DDDataObject oldDD, String oldVal, String newVal, int type) {
        super (src);
        newValue = newVal;
        oldValue = oldVal;
        this.type = type;
        this.oldDD = oldDD;
    }
    
    /** Creates new event.
     *
     * @param src class name of servlet
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
    
    public DDDataObject getOldDD () {
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
