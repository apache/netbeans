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
package org.netbeans.modules.openide.awt;

import org.netbeans.spi.actions.AbstractSavable;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class SavableRegistry {
    private static final RequestProcessor RP = new RequestProcessor("Savable Registry");
    private static final InstanceContent IC = new InstanceContent(RP);
    private static final Lookup LOOKUP = new AbstractLookup(IC);
    
    public static Lookup getRegistry() {
        return LOOKUP;
    }
    
    public static void register(AbstractSavable as) {
        IC.add(as);
    }
    
    public static void unregister(AbstractSavable as) {
        IC.remove(as);
    }
}
