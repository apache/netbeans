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

package org.netbeans.modules.editor.mimelookup.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class SwitchLookup extends Lookup {

    private static final Logger LOG = Logger.getLogger(SwitchLookup.class.getName());
    
    /* package */ static final String ROOT_FOLDER = "Editors"; //NOI18N

    private MimePath mimePath;

    private final String LOCK = new String("SwitchLookup.LOCK"); //NOI18N
    
    private Map<Class<?>,Lookup> classLookups = new HashMap<Class<?>, Lookup>();
    private Map<List<String>,Lookup> pathsLookups = new HashMap<List<String>,Lookup>();

    public SwitchLookup(MimePath mimePath) {
        super();
        
        this.mimePath = mimePath;
    }

    public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
        return findLookup(template.getType()).lookup(template);
    }

    public <T> T lookup(Class<T> clazz) {
        return findLookup(clazz).lookup(clazz);
    }

    private Lookup findLookup(Class<?> clazz) {
        synchronized (LOCK) {
            Lookup lookup = classLookups.get(clazz);
            if (lookup == null) {
                // Create lookup
                lookup = createLookup(clazz);
                classLookups.put(clazz, lookup);
            }
            return lookup;
        }
    }

    private Lookup createLookup(Class<?> forClass) {
        MimeLocation loc = forClass.getAnnotation(MimeLocation.class);

        if (loc == null) {
            loc = new MimeLocation() {
                @Override
                public String subfolderName() {
                    return null;
                }
                @Override
                public Class<? extends InstanceProvider> instanceProviderClass() {
                    return null;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return MimeLocation.class;
                }
            };
        }
        List<String> paths = computePaths(mimePath, ROOT_FOLDER, loc.subfolderName());
        Lookup lookup;
        
        if (loc.instanceProviderClass() != null && loc.instanceProviderClass() != InstanceProvider.class) {
            try {
                // Get a lookup for the new instance provider
                lookup = getLookupForProvider(paths, loc.instanceProviderClass().getDeclaredConstructor().newInstance());
            } catch (ReflectiveOperationException ex) {
                Exceptions.printStackTrace(ex);
                lookup = Lookup.EMPTY;
            }
        } else {
            // Get a lookup for the new paths
            lookup = getLookupForPaths(paths);
        }
        
        return lookup;
    }
    
    private Lookup getLookupForPaths(List<String> paths) {
        Lookup lookup = pathsLookups.get(paths);
        if (lookup == null) {
            lookup = new FolderPathLookup(paths.toArray(new String[0]));
            pathsLookups.put(paths, lookup);
        }
        
        return lookup;
    }

    private Lookup getLookupForProvider(List<String> paths, InstanceProvider instanceProvider) {
        return new InstanceProviderLookup(paths.toArray(new String[0]), instanceProvider);
    }
    
    private static List<String> computePaths(MimePath mimePath, String prefixPath, String suffixPath) {
        try {
            Method m = MimePath.class.getDeclaredMethod("getInheritedPaths", String.class, String.class); //NOI18N
            m.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> paths = (List<String>) m.invoke(mimePath, prefixPath, suffixPath);
            return paths;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Can't call org.netbeans.api.editor.mimelookup.MimePath.getInheritedPaths method.", e); //NOI18N
        }
        
        // No inherited mimepaths, provide at least something
        StringBuilder sb = new StringBuilder();
        if (prefixPath != null && prefixPath.length() > 0) {
            sb.append(prefixPath);
        }
        if (mimePath.size() > 0) {
            if (sb.length() > 0) {
                sb.append('/'); //NOI18N
            }
            sb.append(mimePath.getPath());
        }
        if (suffixPath != null && suffixPath.length() > 0) {
            if (sb.length() > 0) {
                sb.append('/'); //NOI18N
            }
            sb.append(suffixPath);
        }
        return Collections.singletonList(sb.toString());
    }
    
}
