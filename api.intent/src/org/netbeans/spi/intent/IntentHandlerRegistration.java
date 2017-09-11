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
