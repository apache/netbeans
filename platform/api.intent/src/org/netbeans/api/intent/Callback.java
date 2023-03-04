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

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Callback invoked when an intent action has finished. It is run in a dedicated
 * thread in background.
 *
 * @see Intent#execute(org.netbeans.api.intent.Callback)
 *
 * @author jhavlin
 */
public interface Callback {

    /**
     * Invoked when the intent action has completed successfully. The type of
     * result depends on implementation of chosen intent handler, it can be
     * null.
     *
     * @param result Result value.
     */
    public void success(@NullAllowed Object result);

    /**
     * Invoked when the intent action has failed.
     *
     * @param exception Encountered exception.
     */
    public void failure(@NonNull Exception exception);
}
