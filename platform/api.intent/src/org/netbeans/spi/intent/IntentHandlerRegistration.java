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
package org.netbeans.spi.intent;

import java.util.regex.Pattern;

/**
 * Annotation for registering Intent handlers into the application.
 * See {@link org.netbeans.spi.intent} for more info and examples.
 *
 * @see org.netbeans.spi.intent
 */
public @interface IntentHandlerRegistration {

    /**
     * Position of the handler. The lesser value, the bigger priority.
     *
     * @return The position.
     */
    int position();

    /**
     * List of supported action types. To support all actions, use "*" wildcard.
     *
     * @return List of supported action types. If some of contained values
     * equals "*", all actions are supported.
     */
    String[] actions();

    /**
     * Pattern of supported URIs. See {@link Pattern}. Examples: To handle
     * all URIs, use ".*", to handle http adresses, use "http://.*".
     *
     * @return The URI pattern.
     */
    String uriPattern();

    /**
     * Display name of this handler. Bundle keys can be used here.
     *
     * @return The display name.
     */
    String displayName();

    /**
     * Identifier for an icon, e.g. path or URI.
     *
     * @return Icon identifier.
     */
    String icon() default "";
}
