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
package org.netbeans.modules.refactoring.java.ui.scope;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.netbeans.modules.refactoring.java.api.ui.JavaScopeBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import static org.netbeans.modules.refactoring.java.ui.scope.Bundle.*;
import org.netbeans.modules.refactoring.spi.ui.ScopeReference;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@Messages({"LBL_CustomScope=Custom Scope...", "TTL_CustomScope=Custom Scope"})
@ScopeProvider.Registration(id = "custom-scope", displayName = "#LBL_CustomScope", position = 900, iconBase = "org/netbeans/modules/refactoring/java/resources/filter.png")
@ScopeReference(path="org-netbeans-modules-refactoring-java-ui-WhereUsedPanel")
public final class CustomScopeProvider extends ScopeProvider.CustomScopeProvider {

    private static final String PREF_SCOPE = "FindUsages-Scope";
    private Scope customScope;

    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        return true;
    }

    @Override
    public void setScope(Scope value) {
        customScope = value != null ? Scope.create(value.getSourceRoots(), value.getFolders(), value.getFiles()) : null;
    }

    @Override
    public boolean showCustomizer() {
        Scope nue = JavaScopeBuilder.open(TTL_CustomScope(), customScope); //NOI18N
        if (nue != null) {
            storeScope(customScope = nue);
        }

        return nue != null;
    }

    @Override
    public Scope getScope() {
        if (customScope == null) {
            customScope = readScope();
        }
        return customScope;
    }

    private void storeScope(Scope customScope) {
        try {
            storeFileList(customScope.getSourceRoots(), "sourceRoot"); //NOI18N
            storeFileList(customScope.getFolders(), "folder"); //NOI18N
            storeFileList(customScope.getFiles(), "file"); //NOI18N
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Scope readScope() {
        try {
            if (NbPreferences.forModule(JavaScopeBuilder.class).nodeExists(PREF_SCOPE)) { //NOI18N
                return Scope.create(
                        loadFileList("sourceRoot", FileObject.class), //NOI18N
                        loadFileList("folder", NonRecursiveFolder.class), //NOI18N
                        loadFileList("file", FileObject.class)); //NOI18N
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private <T> List<T> loadFileList(String basekey, Class<T> type) throws BackingStoreException {
        Preferences pref = NbPreferences.forModule(JavaScopeBuilder.class).node(PREF_SCOPE).node(basekey);
        List<T> toRet = new LinkedList<T>();
        for (String key : pref.keys()) {
            final String url = pref.get(key, null);
            if (url != null && !url.isEmpty()) {
                try {
                    final FileObject f = URLMapper.findFileObject(new URL(url));
                    if (f != null && f.isValid()) {
                        if (type.isAssignableFrom(FileObject.class)) {
                            toRet.add((T) f);
                        } else {
                            toRet.add((T) new NonRecursiveFolder() {
                                @Override
                                public FileObject getFolder() {
                                    return f;
                                }
                            });
                        }
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return toRet;
    }

    private void storeFileList(Set files, String basekey) throws BackingStoreException {
        Preferences pref = NbPreferences.forModule(CustomScopeProvider.class).node(PREF_SCOPE).node(basekey);
        assert files != null;
        pref.clear();
        int count = 0;
        for (Object next : files) {
            if (next instanceof FileObject) {
                pref.put(basekey + count++, ((FileObject) next).toURL().toExternalForm());
            } else {
                pref.put(basekey + count++, ((NonRecursiveFolder) next).getFolder().toURL().toExternalForm());
            }
        }
        pref.flush();
    }
}
