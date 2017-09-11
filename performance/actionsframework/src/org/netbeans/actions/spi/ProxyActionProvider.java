/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
/*
 * ProxyActionProvider.java
 *
 * Created on January 24, 2004, 2:55 PM
 */

package org.netbeans.actions.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.netbeans.actions.spi.ActionProvider;

/** ActionProvider impl which can proxy other action providers, such as
 * a legacy wrapper and some other implementation.
 *
 * @author  Tim Boudreau
 */
public abstract class ProxyActionProvider extends ActionProvider {
    /** Stores mapping of containerContext + action name to action providers,
     * so implementation methods of ActionProvider can efficiently
     * look up which provider should be proxied for a given action and
     * container context */
    private Map cache = new HashMap();

    /** Creates a new instance of ProxyActionProvider */
    protected ProxyActionProvider() {
    }

    /** Fetch the action providers being proxied */
    protected abstract ActionProvider[] getProviders();
    
    /** Sorts the actions that will be returned by getActionNames from the
     * set of providers.  The default implementation does nothing except 
     * concatenate the arrays of actions.
     *
     * @param providers the action providers
     * @param An array of arrays of strings provided by the passed providers
     * @param length The combined number of actions passed in in the actions array
     * @param A sorted array containing all the strings passed in in the actions
     *  parameter.  The result array <strong>must</strong> contain the same number
     *  of actions as the length parameter - this is enforced.  Do not use 
     *  sort() as a convenient place to filter out actions.  */
    protected String[] sort (ActionProvider[] providers, Object[] actions, int length) {
        String[] result = new String[length];
        int pos = 0;
        for (int i=0; i < providers.length; i++) {
            String[] curr = (String[]) actions[i];
            System.arraycopy (curr, 0, result, pos, curr.length);
            pos += curr.length;
        }
        return result;
    }
    
    /** Call this if the return value of getProviders has changed to dump any
     * cached mappings between providers, contexts and actions */
    protected void providersChanged() {
        cache.clear();
    }
    
    /** Given a container context and an action, reverse-lookup which action
     * provider we are proxying is responsible for it. */
    protected final ActionProvider findProviderOf (String containerCtx, String action) {
        ActionProvider result = findInCache (containerCtx, action);
        if (result == null) {
            result = locateAndCache (containerCtx, action);
        }
        return result;
    }
    
    public final String[] getActionNames(String containerCtx) {
        ActionProvider[] providers = getProviders();
        List list = new ArrayList(providers.length);
        int count = 0;
        for (int i=0; i < providers.length; i++) {
            String[] curr = providers[i].getActionNames(containerCtx);
            count += curr.length;
            list.add(curr);
        }
        Object[] actions = new Object[list.size()];
        actions = list.toArray(actions);
        String[] result = sort (providers, actions, count);
        if (result == null) {
            throw new IllegalStateException ("Sort may not return null"); //NOI18N
        }
        if (result.length != count) {
            throw new IllegalStateException ("Passed " + count + " actions to " +
                " sort() but only got " + result.length + " back.  Do NOT use " +
                "sort() to filter actions."); //NOI18N
        }
        return result;
    }
    
    public final int getActionType(String actionName, String containerCtx) {
        ActionProvider provider = findProviderOf (actionName, containerCtx);
        return provider.getActionType (actionName, containerCtx);
    }
    
    public final String getDescription(String actionName, String containerCtx) {
        ActionProvider provider = findProviderOf (actionName, containerCtx);
        return provider.getDescription (actionName, containerCtx);
    }
    
    public final String getDisplayName(String actionName, String containerCtx) {
        ActionProvider provider = findProviderOf (actionName, containerCtx);
        return provider.getDisplayName (actionName, containerCtx);
    }
    
    public final Icon getIcon(String actionName, String containerCtx, int type) {
        ActionProvider provider = findProviderOf (actionName, containerCtx);
        return provider.getIcon (actionName, containerCtx, type);
    }
    
    public final int getMnemonic(String actionName, String containerCtx) {
        ActionProvider provider = findProviderOf (actionName, containerCtx);
        return provider.getMnemonic (actionName, containerCtx);
    }
    
    public final int getMnemonicIndex(String actionName, String containerCtx) {
        ActionProvider provider = findProviderOf (actionName, containerCtx);
        return provider.getMnemonicIndex (actionName, containerCtx);
    }
    
    public final int getState(String actionName, String containerCtx, Map context) {
        ActionProvider provider = findProviderOf (actionName, containerCtx);
        return provider.getState (actionName, containerCtx, context);
    }

//*************** Caching implementation for reverse lookups ******************
    
    private Object munge (String containerCtx, String action) {
        return new Integer (containerCtx.hashCode() ^ action.hashCode());
    }
    
    private ActionProvider findInCache (String containerCtx, String action) {
        Object key = munge (containerCtx, action);
        return (ActionProvider) cache.get(key);
    }
    
    private ActionProvider locateAndCache (String containerCtx, String action) {
        ActionProvider[] providers = getProviders();
        ActionProvider result = null;
        Object key = munge (containerCtx, action);
        for (int i=0; i < providers.length; i++) {
            String[] curr = providers[i].getActionNames(containerCtx);
            if (Arrays.asList(curr).contains (action)) {
                result = providers[i];
                //XXX may want to use result.hashCode as the value in the cache
                //instead, and then look up the provider by the hashcode in
                //findProviderOf, to avoid holding any references to the
                //provider objects themselves.  This will only be needed if
                //we're want to be able to look up/recreate providers on the
                //fly, which is only needed if we expect providers to change
                //frequently. Otherwise, caching the provider is fine, and 
                //we can just dump any references to providers in providersChanged().
                cache.put (key, result);
                break;
            }
        }
        return result;
    }
}
