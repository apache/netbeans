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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.MimeDataProvider.class)
public final class MemoryMimeDataProvider implements MimeDataProvider {
    
    private static final HashMap<String, Lkp> CACHE = new HashMap<String, Lkp>();
    
    /** Creates a new instance of MemoryMimeDataProvider */
    public MemoryMimeDataProvider() {
    }

    public Lookup getLookup(MimePath mimePath) {
        return getLookup(mimePath.getPath(), true);
    }
    
    public static void addInstances(String mimePath, Object... instances) {
        assert mimePath != null : "Mime path can't be null";
        getLookup(mimePath, true).addInstances(instances);
    }
    
    public static void removeInstances(String mimePath, Object... instances) {
        assert mimePath != null : "Mime path can't be null";
        getLookup(mimePath, true).removeInstances(instances);
    }
    
    public static void reset(String mimePath) {
        if (mimePath == null) {
            synchronized (CACHE) {
                for(Lkp lookup : CACHE.values()) {
                    lookup.reset();
                }
            }
        } else {
            Lkp lookup = getLookup(mimePath, false);
            if (lookup != null) {
                lookup.reset();
            }
        }
    }
    
    private static Lkp getLookup(String mimePath, boolean create) {
        synchronized (CACHE) {
            Lkp lookup = CACHE.get(mimePath);
            if (lookup == null && create) {
                lookup = new Lkp();
                CACHE.put(mimePath, lookup);
            }
            return lookup;
        }
    }
    
    private static final class Lkp extends AbstractLookup {
        
        private ArrayList<Object> all = new ArrayList<Object>();
        private InstanceContent contents;
            
        public Lkp() {
            this(new InstanceContent());
        }
        
        private Lkp(InstanceContent ic) {
            super(ic);
            this.contents = ic;
        }
        
        public void addInstances(Object... instances) {
            all.addAll(Arrays.asList(instances));
            contents.set(all, null);
        }

        public void removeInstances(Object... instances) {
            ArrayList<Object> newAll = new ArrayList<Object>();
            
            loop:
            for(Object oo : all) {
                for(Object o : instances) {
                    if (o == oo) {
                        continue loop;
                    }
                }
                
                newAll.add(oo);
            }
            
            all = newAll;
            contents.set(all, null);
        }
        
        public void reset() {
            all.clear();
            contents.set(all, null);
        }
    } // End of Lkp class
}
