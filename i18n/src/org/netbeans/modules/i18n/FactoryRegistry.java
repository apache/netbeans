/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            Class clazz = factory.getDataObjectClass();
            
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
            Class clazz = factory.getDataObjectClass();
            
            if ((clazz != null) && clazz.isAssignableFrom(dataObjectClass)) {
                cache.add(dataObjectClass);
                return true;
            }
        }

        ncache.add(dataObjectClass);
        return false;
    }
        
}
