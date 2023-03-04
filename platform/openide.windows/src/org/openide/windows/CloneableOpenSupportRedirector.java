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
package org.openide.windows;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Allows to find another {@link CloneableOpenSupport} that all the
 * requests passed to given one should be redirected to. This is useful
 * for redirecting operation on <a href="@org-openide-filesystems@/org/openide/filesystems/FileObject.html">
 * FileObject</a> to another one in cases when two <code>FileObject</code>s
 * represent the same physical file.
 * <p>
 * Instances should be registered to default lookup.
 * @author akrasny
 * @since 6.65
 */
public abstract class CloneableOpenSupportRedirector {

    private static final Lookup.Result<CloneableOpenSupportRedirector> lkp = Lookup.getDefault().lookup(new Lookup.Template<>(CloneableOpenSupportRedirector.class));
    private static final AtomicReference<Collection<? extends CloneableOpenSupportRedirector>> redirectors = new AtomicReference<>();
    private static final LookupListener listener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            redirectors.set(lkp.allInstances());
        }
    };

    static {
        lkp.addLookupListener(listener);
        listener.resultChanged(null);
    }

    /** Find a delegate for given {@link CloneableOpenSupport}'s {@link CloneableOpenSupport.Env}.
     *
     * @param env the environment associated with current CloneableOpenSupport
     * @return null or another CloneableOpenSupport to use as a replacement
     */
    protected abstract CloneableOpenSupport redirect(CloneableOpenSupport.Env env);

    protected abstract void opened(CloneableOpenSupport.Env env);

    protected abstract void closed(CloneableOpenSupport.Env env);

    static CloneableOpenSupport findRedirect(CloneableOpenSupport one) {
        final CloneableOpenSupport.Env env = one.env;
        Collection<? extends CloneableOpenSupportRedirector> rlist = redirectors.get();
        if (rlist != null) {
            for (CloneableOpenSupportRedirector r : rlist) {
                CloneableOpenSupport ces = r.redirect(env);
                if (ces != null && ces != one) {
                    return ces;
                }
            }
        }
        return null;
    }

    static void notifyOpened(CloneableOpenSupport one) {
        final CloneableOpenSupport.Env env = one.env;
        Collection<? extends CloneableOpenSupportRedirector> rlist = redirectors.get();
        if (rlist != null) {
            for (CloneableOpenSupportRedirector r : rlist) {
                r.opened(env);
            }
        }
    }

    static void notifyClosed(CloneableOpenSupport one) {
        final CloneableOpenSupport.Env env = one.env;
        Collection<? extends CloneableOpenSupportRedirector> rlist = redirectors.get();
        if (rlist != null) {
            for (CloneableOpenSupportRedirector r : rlist) {
                r.closed(env);
            }
        }
    }
}
