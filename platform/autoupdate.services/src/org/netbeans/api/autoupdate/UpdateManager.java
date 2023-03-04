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

package org.netbeans.api.autoupdate;

import java.util.List;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;

/** The central manager of content available to users in Autoupdate UI.
 * It providers list of units which can be browsed in UI and handles operations 
 * which can be performed on units (i.e. install, update or unistall etc.).
 *
 * @author Jiri Rechtacek(jrechtacek@netbeans.org), Radek Matous 
 */
public final class UpdateManager {
    
    public static enum TYPE {
        MODULE,
        FEATURE,
        STANDALONE_MODULE,
        KIT_MODULE,
        CUSTOM_HANDLED_COMPONENT,
        LOCALIZATION
    }
        
    /**
     * Creates a new instance of UpdateManager
     */
    private UpdateManager () {}
    
    private static UpdateManager mgr = null;
    
    /** Returns singleton instance of <code>UpdateManager</code>
     * 
     * @return UpdateManager
     */
    public static final UpdateManager getDefault () {
        if (mgr == null) {
            mgr = new UpdateManager ();
        }
        return mgr;
    }
    
        
    /** Returns <code>java.util.List</code> of <code>UpdateUnit</code> build on the top of 
     * <code>UpdateUnitProvider</code>. Only enabled providers are taken in the result.
     * 
     * @return list of UpdateUnit
     */
    public List<UpdateUnit> getUpdateUnits () {
        return getImpl().getUpdateUnits ();
    }
                
    /** Returns <code>java.util.List</code> of <code>UpdateUnit</code> build on the top of 
     * <code>UpdateUnitProvider</code>. Only enabled providers are taken in the result.
     * 
     * @param types returns <code>UpdateUnit</code>s contain only given types, e.g. modules for <code>MODULE</code> type.
     * If types is <code>null</code> or null then returns default types
     * @return list of UpdateUnit
     */
    public List<UpdateUnit> getUpdateUnits (TYPE... types) {
        return getImpl().getUpdateUnits (types);
    }
                
    private static UpdateManagerImpl getImpl() {
        return UpdateManagerImpl.getInstance();
    }    
}
