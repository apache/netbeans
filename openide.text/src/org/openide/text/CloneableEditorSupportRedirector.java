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
 * The Original Software is NetBeans.
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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


