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

package org.netbeans.modules.editor.mimelookup;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author vita
 */
public class DummyMimeLookupInitializer implements MimeLookupInitializer {
    
    private final MimePath mimePath;
    
    /** Creates a new instance of DummyMimeDataProvider */
    public DummyMimeLookupInitializer() {
//        System.out.println("Creating DummyMimeLookupInitializer");
        this.mimePath = MimePath.EMPTY;
    }

    private DummyMimeLookupInitializer(MimePath mimePath) {
        this.mimePath = mimePath;
    }
    
    public Lookup.Result child(String mimeType) {
        return Lookups.singleton(new DummyMimeLookupInitializer(MimePath.get(mimePath, mimeType))).lookupResult(MimeLookupInitializer.class);
    }

    public Lookup lookup() {
//        System.out.println("DummyMimeLookupInitializer creating Marker");
        if (mimePath.size() == 1) {
            return Lookups.singleton(new Marker());
        } else {
            return null;
        }
    }
    
    public static final class Marker {
        
    } // End of Marker class
}
