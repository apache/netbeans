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

package org.netbeans.spi.editor.hints;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.editor.hints.ContextAccessor;

/**
 * Context for {@link PositionRefresher}
 * Provides position of current alt-enter invocation and its cancel status
 * 
 * @author Max Sauer
 * @since 1.8.1
 */
public final class Context {

    final AtomicBoolean cancel;
    final int position;

    Context(int position, AtomicBoolean cancel) {
        this.position = position;
        this.cancel = cancel;
    }

    /**
     * @return true if invocation has been canceled
     */
    public boolean isCanceled() {
        return cancel.get();
    }

    /**
     * @return Caret offset inside current document
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return cancel status
     */
    public AtomicBoolean getCancel() {
        return cancel;
    }

    static {
        ContextAccessor.DEFAULT = new ContextAccessorImpl();
    }

    private static final class ContextAccessorImpl extends ContextAccessor {

        @Override
        public Context newContext(int position, AtomicBoolean cancel) {
            return new Context(position, cancel);
        }

    }
}

