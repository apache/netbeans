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
