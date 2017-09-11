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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.mimelookup.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Mock implementation of MimeLookup. Initially the mime lookup is empty. An instance
 * is automatically installed in <code>META-INF/services</code>.
 * 
 * <p>Example:
 * 
 * <pre>
 * MimePath mimePath = MimePath.parse("text/x-abc");
 * MockMimeLookup.setInstances(mimePath, new TestFoldManagerFactory());
 * 
 * FoldManagerFactory f = MimeLookup.getLookup(mimePath).lookup(FoldManagerFactory.class);
 * </pre>
 */
public final class MockMimeLookup implements MimeDataProvider {

    private static final Map<MimePath, Lkp> MAP = new HashMap<MimePath, Lkp>();
    
    /**
     * Sets the lookup for <code>mimePath</code> with zero or more delegate lookups.
     * 
     * @param mimePath The mime path to set the lookup for.
     * @param lookups The delegate lookups.
     */
    public static void setLookup(MimePath mimePath, Lookup... lookups) {
        Lkp toUpdate = null;
        
        synchronized (MAP) {
            Lkp lkp = MAP.get(mimePath);
            if (lkp == null) {
                lkp = new Lkp(lookups);
                MAP.put(mimePath, lkp);
            } else {
                toUpdate = lkp;
            }
        }
        
        if (toUpdate != null) {
            toUpdate.set(lookups);
        }
    }
    
    /**
     * Sets the lookup for <code>mimePath</code> with some fixed instances.
     * 
     * @param mimePath The mime path to set the lookup for.
     * @param instances The instances to set.
     */
    public static void setInstances(MimePath mimePath, Object... instances) {
        setLookup(mimePath, Lookups.fixed(instances));
    }

    /** Don't use this directly. */
    public MockMimeLookup() {
    }
    
    /** You can call it, but it's probably not what you want. */
    public @Override Lookup getLookup(MimePath mimePath) {
        synchronized (MAP) {
            List<String> paths = Collections.singletonList(mimePath.getPath());
            
            try {
                Method m = MimePath.class.getDeclaredMethod("getInheritedPaths", String.class, String.class); //NOI18N
                m.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<String> ret = (List<String>) m.invoke(mimePath, null, null);
                paths = ret;
            } catch (Exception e) {
                throw new IllegalStateException("Can't call org.netbeans.api.editor.mimelookup.MimePath.getInheritedPaths method.", e); //NOI18N
            }

            List<Lookup> lookups = new ArrayList<Lookup>(paths.size());
            for(String path : paths) {
                MimePath mp = MimePath.parse(path);
                Lkp lookup = MAP.get(mp);
                if (lookup == null) {
                    lookup = new Lkp();
                    MAP.put(mp, lookup);
                }
                lookups.add(lookup);
            }
            
            return new ProxyLookup(lookups.toArray(new Lookup [lookups.size()]));
        }
    }
    
    private static final class Lkp extends ProxyLookup {
        
        public Lkp(Lookup... lookups) {
            super(lookups);
        }
        
        public void set(Lookup... lookups) {
            super.setLookups(lookups);
        }
    }
}
