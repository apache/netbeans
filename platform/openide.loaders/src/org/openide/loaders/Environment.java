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

package org.openide.loaders;

import javax.naming.Context;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** Utilities that define the "settings hierarchy".
 *
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public final class Environment extends Object {
    /** Result of query for all instances of Environment.Provider */
    private static Lookup.Result<Provider> result;

    /** Constructor
     */
    private Environment() {
    }

    /** Finds a lookup for given data object.
     * @param obj the object
     * @return the lookup for this data object
     */
    public static Lookup find (DataObject obj) {
        while (obj != null) {
            Lookup l = findForOne (obj);
            if (l != null) {
                return l;
            }
            
            FileObject fo = obj.getPrimaryFile ().getParent ();
            if (fo == null) {
                break;
            }
            
            try {
                obj = DataObject.find (fo);
            } catch (DataObjectNotFoundException ex) {
                break;
            }
        }
        
        // ok, use empty lookup
        return Lookup.EMPTY;
    }
    
    /** Finds a JNDI context for a given data object.
     * @param obj the data object
     * @return the JNDI context for this data object
     * @since 3.13
     * @deprecated This method is probably unused and useless.
     */
    @Deprecated
    public static Context findSettingsContext(DataObject obj) {
        for (Provider ep : getProviders().allInstances()) {
            Lookup lookup = ep.getEnvironment(obj);
            if (lookup != null) {
                Context ctx = lookup.lookup(Context.class);
                if (ctx != null) return ctx;
            }
        }
        
        // default impl
        return new DefaultSettingsContext(obj);
    }
    
    /** Find method that tries to scan for lookup of one data object.
     * @param obj the object
     * @return the lookup or null
     */
    static Lookup findForOne (DataObject obj) {
        /*
        DataLoader loader = obj.getLoader ();
        Provider provider = loader instanceof Provider ? (Provider)loader : null;
        if (provider != null) {
            Lookup lookup = provider.getEnvironment (obj);
            if (lookup != null) {
                return lookup;
            } else {
                return Lookup.EMPTY;
            }
            }
        */

        for (Provider ep : getProviders().allInstances()) {
            Lookup lookup = ep.getEnvironment (obj);
            if (lookup != null) {
                return lookup;
            }
        }
        
        // not found for this data object
        return null;
    }
    
    static Lookup.Result<Provider> getProviders() {
        if (result == null) {
            result = Lookup.getDefault().lookupResult(Environment.Provider.class);
        }
        return result;
    }
    
    /** Cookie for objects that plan to provide environment.
     * @since 1.9
     */
    public static interface Provider {
        /** Returns a lookup that represents environment.
         * @return the lookup
         */
        public Lookup getEnvironment (DataObject obj);
    } // end of Provider
}
