/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.i18n;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;


/**
 * Registry which gets i18n support factories for specified data objects.
 * It gets the factories which are registered in SFS/Services/i18n directory
 * via lookup.
 *
 * @author  Peter Zavadsky
 * @see I18nSupport.Factory
 * @see org.netbeans.modules.i18n.form.FormI18nSupport.Factory
 * @see org.netbeans.modules.i18n.jsp.JspI18nSupport.Factory
 */
public final class FactoryRegistry extends Object {

    private FactoryRegistry() {};
    
    /** All i18n supports kept as <code>Lookup.Result</code>. */
    private static Lookup.Result<I18nSupport.Factory> result;
    private static final Set<Class> cache = Collections.synchronizedSet(new HashSet<Class>(5));    
    private static final Set<Class> ncache = Collections.synchronizedSet(new HashSet<Class>(50));    
    
    /** Gets lookup result holding script type instances. */
    private static Lookup.Result<I18nSupport.Factory> getSupports() {
        if (result == null) {
            result = Lookup.getDefault().lookupResult(I18nSupport.Factory.class);
            result.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent e) {
                    cache.clear();
                    ncache.clear();
                }
            });
        }
        
        return result;
    }
    
    /** Gets <code>I18nSupportFactory</code> for specified data object class.
     * @return factory for specified data object class or <code>null</code> */
    public static I18nSupport.Factory getFactory(Class dataObjectClass) {
        
        List<I18nSupport.Factory> candidates
                = new ArrayList<I18nSupport.Factory>(3);
        
        for (I18nSupport.Factory factory : getSupports().allInstances()) {

            // XXX it has to be checked for null, for cases Jsp support and java support
            // don't have their modules available, see JspI18nSupportFactory.getDataObjectClass.
            Class<?> clazz = factory.getDataObjectClass();
            
            if ((clazz != null) && clazz.isAssignableFrom(dataObjectClass)) {
                candidates.add(factory);
            }
        }
        
        if (candidates.size() == 0) {
            return null;
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        } else {
            I18nSupport.Factory chosen = null;
            
            // Find factory which supported class data object 
            // is the lowest one in the class hierarchy.
            for (I18nSupport.Factory fct : candidates) {
                
                if (chosen == null) {
                    chosen = fct;
                    continue;
                }

                if (chosen.getDataObjectClass().isAssignableFrom(fct.getDataObjectClass()) ) {
                    chosen = fct;
                }
            }
            
            return chosen;
        }
    }

    /** 
     * Indicates if there is a factory for that data object class. 
     * It queried very often from interactive mode.
     */
    public static boolean hasFactory(Class dataObjectClass) {
        
        if (cache.contains(dataObjectClass)) return true;
        if (ncache.contains(dataObjectClass)) return false;
        
        for (I18nSupport.Factory factory : getSupports().allInstances()) {

            // XXX it has to be checked for null, for cases Jsp support and java support
            // don't have their modules available, see JspI18nSupportFactory.getDataObjectClass.
            Class<?> clazz = factory.getDataObjectClass();
            
            if ((clazz != null) && clazz.isAssignableFrom(dataObjectClass)) {
                cache.add(dataObjectClass);
                return true;
            }
        }

        ncache.add(dataObjectClass);
        return false;
    }
        
}
