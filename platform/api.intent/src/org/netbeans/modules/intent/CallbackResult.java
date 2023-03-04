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
package org.netbeans.modules.intent;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.intent.Callback;
import org.netbeans.spi.intent.Result;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jhavlin
 */
public class CallbackResult implements Result {

    private static final RequestProcessor RP
            = new RequestProcessor("Intent Callbacks");

    private final Callback callback;

    public CallbackResult(@NonNull Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setResult(final Object result) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                callback.success(result);
            }
        });
    }

    @Override
    public void setException(final Exception exception) {
        Parameters.notNull("exception", exception);                     //NOI18N
        RP.post(new Runnable() {

            @Override
            public void run() {
                callback.failure(exception);
            }
        });
    }
}
