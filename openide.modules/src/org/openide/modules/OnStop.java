/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;
import org.openide.util.lookup.NamedServiceDefinition;

/** Annotation that can be applied to {@link Runnable} or 
 * {@link Callable}{@code <Boolean>} subclasses with default constructor
 * which will be invoked during shutdown sequence or when the module
 * is being shutdown.
 * <p>
 * First of all call callables are consulted to allow or deny proceeding
 * with the shutdown:
 * <pre>
 * {@code @OnStop}
 * <b>public class</b> AskTheUser <b>implements</b> Callable<Boolean> {
 *   <b>public</b> Boolean call() {
 *     <b>return</b> isItOKToShutdown() ? Boolean.TRUE : Boolean.FALSE;
 *   }
 * }
 * </pre>
 * If the shutdown is approved, all runnables registered are acknowledged and
 * can perform the shutdown cleanup. The runnables are invoked in parallel. 
 * It is guaranteed their execution is finished before the shutdown sequence
 * is over:
 * <pre>
 * {@code @OnStop}
 * <b>public class</b> Cleanup <b>implements</b> Runnable {
 *   <b>public void</b> run() {
 *     <em>// do some cleanup</em>
 *   }
 * }
 * </pre>
 *
 * @since 7.29
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@NamedServiceDefinition(
    path="Modules/Stop", serviceType={ Runnable.class, Callable.class }
)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface OnStop {
}
