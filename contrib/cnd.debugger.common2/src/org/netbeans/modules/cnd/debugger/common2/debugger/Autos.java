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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.util.Collections;
import java.util.Set;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.spi.model.services.AutosProvider;
import org.openide.util.Lookup;

public final class Autos {
    private static AutosProvider DEFAULT = null;
    private static final AutosProvider EMPTY = new AutosProvider() {
        @Override
        public Set<String> getAutos(StyledDocument document, int line) {
            return Collections.<String>emptySet();
        }
    };

    private Autos() {
    }

    private static AutosProvider getDefault() {
        if (DEFAULT == null) {
            // Take the first one
            DEFAULT = Lookup.getDefault().lookup(AutosProvider.class);
        }
        return DEFAULT == null ? EMPTY : DEFAULT;
    }

    public static Set<String> get(final StyledDocument document, int line) {
        return getDefault().getAutos(document, line);
    }
}
