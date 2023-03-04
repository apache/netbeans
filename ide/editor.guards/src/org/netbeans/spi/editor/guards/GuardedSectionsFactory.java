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

package org.netbeans.spi.editor.guards;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;

/**
 * The factory allows to look up {@link GuardedSectionsProvider} factories for given
 * mime type. Factories have to be registered under <code>Editors/&lt;mime path&gt;</code>
 * in the module's layer.
 * 
 * 
 * 
 * @author Jan Pokorsky
 */
public abstract class GuardedSectionsFactory {

    /**
     * Use this to find a proper factory instance for the passed mime path.
     * @param mimePath a mime path
     * @return the factory instance or <code>null</code>
     */
    public static GuardedSectionsFactory find(String mimePath) {
        MimePath mp = MimePath.get(mimePath);
        GuardedSectionsFactory factory = null;
        if (mp != null) {
            factory = MimeLookup.getLookup(mp).lookup(GuardedSectionsFactory.class);
        }
        return factory;
    }
    
    /**
     * Creates a guarded sections provider.
     * @param editor an editor support
     * @return the provider impl
     */
    public abstract GuardedSectionsProvider create(GuardedEditorSupport editor);
    
}
