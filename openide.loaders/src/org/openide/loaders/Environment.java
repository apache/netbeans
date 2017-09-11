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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
