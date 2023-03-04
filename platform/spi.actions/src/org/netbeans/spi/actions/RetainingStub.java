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
package org.netbeans.spi.actions;

import java.util.Collection;
import org.openide.util.Lookup;

/**
 * ActionStub subclass which remembers the last usable contents it had.
 * @author Tim Boudreau
 * @param <T> The type of object the stub is sensitive to
 */
final class RetainingStub<T> extends ActionStub<T> {

    Collection<? extends T> retained;

    RetainingStub(Lookup context, SurviveSelectionChange<T> parent) {
        super(context, parent);
        assert parent != null;
        assert context != null;
        retained = super.collection();
        assert retained != null;
        enabled = isEnabled();
    }

    @Override
    Collection<? extends T> collection() {
        boolean wasEnabled = enabled;
        if (wasEnabled) {
            Collection<? extends T> nue = super.collection();
            assert nue != null;
            //If we were enabled, and now we have too many objects,
            //become disabled, don't keep the old single object
            if (!parent.checkQuantity(nue)) {
                retained = nue;
            }
            if (!nue.isEmpty()) {
                retained = nue;
            }
        }
        return retained;
    }
}
