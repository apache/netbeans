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
 * SPI for Intent handlers.
 * <p>
 * Handling some type of Intents is as simple as registering a method using
 * annotation {@link org.netbeans.spi.intent.IntentHandlerRegistration}.
 * </p>
 * <p>
 * Currently two types of handling methods are supported:
 * </p>
 * <ul>
 * <li>
 * Public static method taking {@link org.netbeans.api.intent.Intent} and
 * returning {@link java.lang.Object}. This method will be invoked in a
 * background thread. It is suitable if no waiting for asynchronous operations
 * is needed and when the method will finish reasonably quickly (so that it
 * will not block execution of other intents).
 * </li>
 * <li>
 * Public static method taking {@link org.netbeans.api.intent.Intent} and
 * {@link org.netbeans.spi.intent.Result} with no return type (void). It will be
 * invoked in a background thread, but it can simply pass the result object to
 * other threads. When the computation is finished, either
 * {@link org.netbeans.spi.intent.Result#setException(java.lang.Exception)} or
 * {@link org.netbeans.spi.intent.Result#setResult(java.lang.Object)}
 * <b>MUST</b> be called on the result object.
 * </li>
 * </ul>
 * <p>See examples:</p>
 * <p>Basic handler:</p>
 * <pre>
 *  &nbsp;&#64;{@link org.netbeans.spi.intent.IntentHandlerRegistration}(
 *               displayName = "Show my item in MyEditor",
 *               position = 800,
 *               uriPattern = "myscheme://.*",
 *               actions = {Intent.ACTION_VIEW, Intent.ACTION_EDIT}
 *   )
 *   public static Object handleIntent({@link org.netbeans.api.intent.Intent} intent) {
 *       SomeType result = parseAndPerformIntentSomehow(intent);
 *       return result;
 *   }
 * </pre>
 * <p>Handler that uses {@link org.netbeans.spi.intent.Result}:</p>
 * <pre>
 *  &nbsp;&#64;{@link org.netbeans.spi.intent.IntentHandlerRegistration}(
 *               displayName = "Show my item in MyEditor",
 *               position = 800,
 *               uriPattern = "myscheme://.*",
 *               actions = "*"
 *   )
 *   public static void handleIntent(final {@link org.netbeans.api.intent.Intent} intent, final {@link org.netbeans.spi.intent.Result} result) {
 *       EventQueue.invokeLater(new Runnable() {
 *           public void run() {
 *               try {
 *                   Object value = doSomethingInEDT(intent);
 *                   result.setResult(value);
 *               } catch (Exception e) {
 *                   result.setException(e);
 *               }
 *           }
 *       });
 *   }
 * </pre>
 */
package org.netbeans.spi.intent;
