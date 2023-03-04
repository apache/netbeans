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
package org.openide.text;

import java.util.IdentityHashMap;
import java.util.Map;
import org.openide.util.Lookup;

/**
 * Allows to find another {@link CloneableEditorSupport} that all the
 * requests passed to given one should be redirected to. This is useful
 * for redirecting operation on <a href="@org-openide-filesystems@/org/openide/filesystems/FileObject.html">
 * FileObject</a> to another one in cases when two <code>FileObject</code>s
 * represent the same physical file.
 * <p>
 * Instances should be registered to default lookup.
 * @author Jaroslav Tulach
 * @since 6.13
 */
public abstract class CloneableEditorSupportRedirector {
    private static final ThreadLocal<Map<Lookup,CloneableEditorSupport>> CHECKED = new ThreadLocal<Map<Lookup,CloneableEditorSupport>>();
    
    /** Find a delegate for given {@link CloneableEditorSupport}'s {@link Lookup}.
     * The common code can be to extract for example a 
     * <a href="@org-openide-filesystems@/org/openide/filesystems/FileObject.html">
     * FileObject</a> from the lookup and use its location to find another
     * <code>CloneableEditorSupport</code> to delegate to.
     * 
     * @param env the environment associated with current CloneableEditorSupport
     * @return null or another CloneableEditorSupport to use as a replacement
     */
    protected abstract CloneableEditorSupport redirect(Lookup env);
    
    static CloneableEditorSupport findRedirect(CloneableEditorSupport one) {
        return findRedirect(one, false);
    }
    static CloneableEditorSupport findRedirect(CloneableEditorSupport one, boolean check) {
        Map<Lookup,CloneableEditorSupport> all = CHECKED.get();
        if (all == null) {
            all = new IdentityHashMap<Lookup, CloneableEditorSupport>();
            CHECKED.set(all);
        }
        final Lookup lkp = one.getLookup();
        try {
            if (check && all.containsKey(lkp)) {
                return null;
            }
            all.put(lkp, one);
            for (CloneableEditorSupportRedirector r : Lookup.getDefault().lookupAll(CloneableEditorSupportRedirector.class)) {
                CloneableEditorSupport ces = r.redirect(lkp);
                if (ces != null && ces != one) {
                    return ces;
                }
            }
            return null;
        } finally {
            all.remove(lkp);
        }
    }
}


