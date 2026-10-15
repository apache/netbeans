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
            
            return new ProxyLookup(lookups.toArray(new Lookup [0]));
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
