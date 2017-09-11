/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import org.openide.util.lookup.NamedServiceDefinition;

/**
 * Replacement for {@link URLStreamHandlerFactory} within the NetBeans platform.
 * (The JVM only permits one global factory to be set at a time,
 * whereas various independent modules may wish to register handlers.)
 * May be placed on a {@link URLStreamHandler} implementation to register it.
 * Your handler will be loaded and used if and when a URL of a matching protocol is created.
 * <p>A {@link URLStreamHandlerFactory} which uses these registrations may be found in {@link Lookup#getDefault}.
 * This factory is active whenever the module system is loaded.
 * You may also wish to call {@link URL#setURLStreamHandlerFactory}
 * from a unit test or otherwise without the module system active.
 * @since org.openide.util 7.31
 */
@NamedServiceDefinition(path="URLStreamHandler/@protocol()", serviceType=URLStreamHandler.class)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface URLStreamHandlerRegistration {

    /**
     * URL protocol(s) which are handled.
     * {@link URLStreamHandler#openConnection} will be called with a matching {@link URL#getProtocol}.
     */
    String[] protocol();

    /**
     * An optional position in which to register this handler relative to others.
     * The lowest-numbered handler is used in favor of any others, including unnumbered handlers.
     */
    int position() default Integer.MAX_VALUE;

}
