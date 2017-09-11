/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
