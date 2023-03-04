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

import java.awt.Image;
import java.util.Collection;
import org.openide.util.Lookup;

/**
 * A context action which, once enabled, remains enabled.
 * <p/>
 * The canonical example of this sort of action in the NetBeans IDE is
 * NextErrorAction:  It becomes enabled when the output window gains
 * focus.  But it should remain enabled when focus goes back to the
 * editor, and still work against whatever context the output window
 * gave it to work on.  Such cases are rare but legitimate.
 * <p/>
 * Use judiciously - such actions are temporary memory
 * leaks - the action will retain the last usable collection of
 * objects it had to work on as long as there are any property
 * change listeners attached to it.
 *
 * @param <T> The type this object is sensitive to
 * @author Tim Boudreau
 */
public abstract class SurviveSelectionChange<T> extends ContextAction<T> {

    protected SurviveSelectionChange(Class<T> type) {
        super(type);
    }

    protected SurviveSelectionChange(Class<T> type, String displayName, Image icon) {
        super(type, displayName, icon);
    }

    @Override
    ActionStub<T> createStub(Lookup actionContext) {
        return new RetainingStub<T>(actionContext, this);
    }

    @Override
    boolean checkQuantity(Collection<? extends T> targets) {
        return super.checkQuantity(targets) || stub != null &&
                super.checkQuantity(((RetainingStub<T>) stub).retained);
    }
}
