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

package org.netbeans.editor;

import java.util.HashMap;

/** This singleton class is an editor state encapsulation object. Every part
 * of the editor could store its state-holder here and it will be automatically
 * persistent across restarts. It is intended for any state informations
 * that are not "Settings", like the contents of the input field histories,
 * persistent, named bookmarks or so.
 * The implementation is just like a HashMap indexed by state-holders' names.
 * Typical usage is <CODE>myState = EditorState.get( MY_STATE_NAME );</CODE>
 * There is no support for state change notifications, but the inserted
 * value objects could be singletons as well and could do its own notifications.
 *
 * @author  Petr Nejedly
 * @version 1.0
 * @deprecated Use Editor Settings and Settings Storage APIs. Please note that
 *     the states stored here are not persisted and therefore don't survive
 *     JVM restarts.
 */
@Deprecated
public class EditorState {
    private static HashMap state = new HashMap();
    
    /** This is fixed singleton, don't need instances */
    private EditorState() {
    }
  
    /** Retrieve the object specified by the key. */
    public static Object get( Object key ) {
        return state.get( key );
    }

    /** Store the object under specified key */
    public static void put( Object key, Object value ) {
        state.put( key, value );
    }
    
    public static HashMap getStateObject() {
        return state;
    }
    
    public static void setStateObject( HashMap stateObject ) {
        state = stateObject;
    }
}
