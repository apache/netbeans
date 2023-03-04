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

/**
 * API for working with {@link org.netbeans.api.intent.Intent}s, abstract descriptions of intended
 * operations.
 * <p>
 *  Intents can be used when we want to perform some standard operation and we
 *  believe that the environment (the IDE, some application) is capable of
 *  finding and choosing correct action for it.
 * </p>
 * <p>
 *  The operations are specified as pair of action type and a URI. See example:
 * </p>
 * <code>
 *  {@link org.netbeans.api.intent.Intent} i = new {@link org.netbeans.api.intent.Intent}(Intent.ACTION_VIEW, new URI("file://path/file.txt"));
 * </code>
 * <p>
 *  We can execute an Intent to let the system choose to most appropriate
 *  action for the intent and invoke it:
 * </p>
 * <code>
 *  i.{@link org.netbeans.api.intent.Intent#execute() execute()};
 * </code>
 * <p>
 *  Or we can get list of all available actions, display them somehow, and let
 *  the user select one of them:
 * </p>
 * <code>
 *  Set&lt;IntentAction&gt; available = i.{@link org.netbeans.api.intent.Intent#getIntentActions() getIntentActions()};
 * </code>
 */
package org.netbeans.api.intent;
