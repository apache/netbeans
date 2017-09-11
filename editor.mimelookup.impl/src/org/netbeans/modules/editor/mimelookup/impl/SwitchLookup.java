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
                lookup = getLookupForProvider(paths, loc.instanceProviderClass().newInstance());
            } catch (InstantiationException ex) {
                Exceptions.printStackTrace(ex);
                lookup = Lookup.EMPTY;
            } catch (IllegalAccessException ex) {
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
            lookup = new FolderPathLookup(paths.toArray(new String[paths.size()]));
            pathsLookups.put(paths, lookup);
        }
        
        return lookup;
    }

    private Lookup getLookupForProvider(List<String> paths, InstanceProvider instanceProvider) {
        return new InstanceProviderLookup(paths.toArray(new String[paths.size()]), instanceProvider);
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
