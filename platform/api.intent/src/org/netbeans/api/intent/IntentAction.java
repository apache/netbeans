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
package org.netbeans.api.intent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.intent.CallbackResult;
import org.netbeans.modules.intent.IntentHandler;
import org.netbeans.modules.intent.SettableResult;
import org.netbeans.spi.intent.Result;

/**
 * Actual action for an Intent. Pair of an Intent and one of its handlers.
 *
 * @see Intent#getIntentActions()
 * @author jhavlin
 */
public final class IntentAction {

    private final Intent intent;
    private final IntentHandler delegate;

    IntentAction(Intent intent, IntentHandler delegate) {
        this.intent = intent;
        this.delegate = delegate;
    }

    int getPosition() {
        return delegate.getPosition();
    }

    /**
     * Execute the intent action. The operation will be run asynchronously.
     *
     * @param callback Callback object that will be notified when the execution
     * completes. If callback is null, the result will be ignored.
     */
    public void execute(@NullAllowed final Callback callback) {
        IntentHandler.RP.post(new Runnable() {
            @Override
            public void run() {
                Result result = callback == null
                        ? null
                        : new CallbackResult(callback);
                delegate.handle(intent, result);
            }
        });
    }

    /**
     * Execute the intent action. The operation will be run asynchronously.
     * <p>
     * If the result is ignored, it's recommended to use
     * {@code intentAction.execute(null);}
     * </p>
     *
     * @return Future for result of the action. The type of result depends on
     * implementation of chosen intent handler, it can be null.
     */
    public @NonNull Future<Object> execute() {

        return IntentHandler.RP.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                SettableResult result = new SettableResult();
                delegate.handle(intent, result);
                if (result.getException() != null) {
                    throw result.getException();
                }
                return result.getResult();
            }
        });
    }

    /**
     * Get display name of this action.
     *
     * @return The localized display name.
     */
    public @NonNull String getDisplayName() {
        return delegate.getDisplayName();
    }

    /**
     * Get icon of this action.
     *
     * @return Some resource identifier, e.g. icon id, path or URI.
     * Depends on the platform. If not available, empty string is returned.
     */
    public @NonNull String getIcon() {
        return delegate.getIcon();
    }
}
