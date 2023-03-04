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

package org.netbeans.modules.editor.mimelookup.impl;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author vita
 */
public class DummyMimeDataProvider implements MimeDataProvider {

    /** Creates a new instance of DummyMimeDataProvider */
    public DummyMimeDataProvider() {
//        System.out.println("Creating DummyMimeDataProvider");
    }

    public Lookup getLookup(MimePath mimePath) {
//        System.out.println("MimeDataProvider creating Marker for " + mimePath.getPath());
        return Lookups.fixed(new Object [] { new Marker(), mimePath });
    }
    
    public static final class Marker {
        
    } // End of Marker class
}
