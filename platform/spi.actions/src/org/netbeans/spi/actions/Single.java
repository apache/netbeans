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

/**
 * Subclass of ContextAction which does not support multi-selection -
 * like ContextAction, it is sensitive to a particular type.  However,
 * it only is enabled if there is exactly one object of type <code>type</code>
 * in the selection.
 * @param <T> The type this action is sensitive to
 * @author Tim Boudreau
 */
public abstract class Single<T> extends ContextAction<T> {
    protected Single(Class<T> type) {
        super(type);
    }

    protected Single(Class<T> type, String displayName, Image icon) {
        super(type, displayName, icon);
    }

    /**
     * Delegates to actionePerformed(T)</code> with the first and
     * only element of the collection.
     * @param targets The objects this action may operate on
     */
    @Override
    protected final void actionPerformed(Collection<? extends T> targets) {
        actionPerformed(targets.iterator().next());
    }

    /**
     * Actually perform the action.
     * @param target The only instance of <code>T</code> in the action
     * context.
     */
    protected abstract void actionPerformed(T target);

    @Override
    protected final boolean checkQuantity(int count) {
        return count == 1;
    }

    /**
     * Determine if this action should be enabled.  This method will only be
     * called if the size of the collection == 1.  The default implementation
     * returns <code>true</code>.  If you need to do some further
     * test on the collection of objects to determine if the action should
     * really be enabled or not, override this method do that here.
     *
     * @param targets A collection of objects of type <code>type</code>
     * @return Whether or not the action should be enabled.
     */
    @Override
    protected final boolean isEnabled(Collection<? extends T> targets) {
        //Overridden only in order to have different javadoc
        assert !targets.isEmpty();
        return isEnabled (targets.iterator().next());
    }

    /**
     * Determine if the action should be enabled for this object.
     * @param target The target object.
     * @return true if the action should be enabled
     */
    protected boolean isEnabled (T target) {
        return true;
    }
}
