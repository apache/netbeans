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

/**
 * Exception thrown when no handler is available for the Intent, or when
 * invocation of all handlers fails. In the latter case, exception thrown by the
 * last handler is set as the init cause of this exception.
 *
 * @author jhavlin
 */
public class NoAvailableHandlerException extends Exception {

    public NoAvailableHandlerException(Intent intent) {
        super(messageForIntent(intent));
    }

    public NoAvailableHandlerException(Intent intent, Throwable cause) {
        super(messageForIntent(intent), cause);
    }

    private static String messageForIntent(Intent intent) {
       return "No available handler for intent " + intent;
    }
}
