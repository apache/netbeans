/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.whitelist.project;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation.UserSelectable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *  The {@link LookupProvider} providing {@link WhiteListQueryImplementation}s
 *  enabled in the project.
 *  @author David Konecny
 *  @author Tomas Zezula
 */
public class WhiteListLookupProvider implements LookupProvider {

    private static final String WHITELISTS_PATH = "org-netbeans-api-java/whitelists/";  //NOI18N
    private static final String PROP_WHITELIST_ENABLED = "whitelist-enabled";
    private static final String PROP_WHITELIST = "whitelist-";

    //@GuardedBy("lookupCache")
    private final static Map<Project,Reference<WhiteListLookup>> lookupCache =
            Collections.synchronizedMap(new WeakHashMap<Project, Reference<WhiteListLookup>>());


    @Override
    @NonNull
    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project p = baseContext.lookup(Project.class);
        assert p != null;
        return getEnabledUserSelectableWhiteLists(p);
    }

    @NonNull
    static Lookup getEnabledUserSelectableWhiteLists(@NonNull Project p) {
        synchronized (lookupCache) {
            Reference<WhiteListLookup> lkpRef = lookupCache.get(p);
            WhiteListLookup lkp;
            if (lkpRef == null || (lkp=lkpRef.get())==null) {
                lkp = new WhiteListLookup(p);
                lookupCache.put(p,new WeakReference<WhiteListLookup>(lkp));
            }
            return lkp;
        }
    }

    static Collection<? extends WhiteListQueryImplementation.UserSelectable> getUserSelectableWhiteLists() {
        return Lookups.forPath(WHITELISTS_PATH).lookupResult(WhiteListQueryImplementation.UserSelectable.class).allInstances();
    }

    static boolean isWhiteListPanelEnabled(@NonNull Project p) {
        Preferences prefs = ProjectUtils.getPreferences(p, WhiteListQuery.class, true);
        return prefs.getBoolean(PROP_WHITELIST_ENABLED, false);
    }

    public static void enableWhiteListInProject(@NonNull Project p, final @NonNull String whiteListId, final boolean enable) {
        final Preferences prefs = ProjectUtils.getPreferences(p, WhiteListQuery.class, true);
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                prefs.putBoolean(PROP_WHITELIST+whiteListId, enable);
                 if (enable) {
                    prefs.putBoolean(PROP_WHITELIST_ENABLED, true);
                }
            }
        });
        final Reference<WhiteListLookup> lkpRef = lookupCache.get(p);
        final WhiteListLookup lkp;
        if (lkpRef != null && (lkp=lkpRef.get())!=null) {
            lkp.updateLookup();
        }
    }

    public static boolean isWhiteListEnabledInProject(@NonNull Project p, @NonNull String whiteListId) {
        Preferences prefs = ProjectUtils.getPreferences(p, WhiteListQuery.class, true);
        return prefs.getBoolean(PROP_WHITELIST+whiteListId, false);
    }

    private static class WhiteListLookup extends ProxyLookup {

        private Project p;
        private final AtomicBoolean initialized = new AtomicBoolean();

        public WhiteListLookup(Project p) {
            this.p = p;
        }

        @Override
        protected void beforeLookup(Template<?> template) {
            if (WhiteListQueryImplementation.class.isAssignableFrom(template.getType())) {
                if (!initialized.get()) {
                    //Threading: Weak consistency - may be performed several times
                    //by more threads in parallel but should be idempotent only several
                    //events will be fired
                    final UserSelectable[] queries = createQueries();
                    if (!initialized.get()) {
                        setLookups(Lookups.fixed((Object[])queries));
                        initialized.set(true);
                    }
                }
            }
            super.beforeLookup(template);
        }

        private void updateLookup() {
            setLookups(Lookups.fixed((Object[])createQueries()));
        }

        @NonNull
        private UserSelectable[] createQueries() {
            final List<WhiteListQueryImplementation.UserSelectable> impls = new ArrayList<WhiteListQueryImplementation.UserSelectable>();
            for (WhiteListQueryImplementation.UserSelectable w :
                    Lookups.forPath(WHITELISTS_PATH).lookupAll(WhiteListQueryImplementation.UserSelectable.class)) {
                if (isWhiteListEnabledInProject(p, w.getId())) {
                    impls.add(w);
                }
            }
            return impls.toArray(new UserSelectable[impls.size()]);
        }
    }
}
