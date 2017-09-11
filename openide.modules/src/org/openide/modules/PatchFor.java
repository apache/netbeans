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

package org.openide.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class, which provides binary-compatible implementation for a changed API class.
 * When removing <code>@deprecated</code> code from API, it is still desirable to retain
 * backward binary compatibility. Because of JVM method/field/class resolution
 * mechanism, it is permitted to move the implementation upwards the inheritance
 * hierarchy. However, the superclass that receives the methods must not be
 * visible in the sources (otherwise the change would not serve any good) and
 * specifically, must not be referenced from <i>extends</i> clause of the changed
 * class. 
 * <p/>
 * The compatibility superclass ought to reside in a <b>different module</b>
 * to allow reduce type dependencies and should be only injected into
 * inheritance chain if and only if some of enabled modules depend on the
 * obsolete version of the API module.
 * <p/>
 * <hr/>
 * This annotation is designed to mark code which should be injected to provide 
 * backward compatibility and will provide implementation for removed API methods.
 * The marked class will be used as a <b>superclass</b> 
 * of the API class identified by value of the annotation, so old clients can 
 * still use removed members at run time.
 * <p/>
 * The injected superclass <b>must</b> extend the same type as the original
 * API class, so that API-visible inheritance chain is preserved. It must declare
 * all non-private constructors as the original superclass.
 * <p/>
 * The module that contains {@code PatchFor} classes <b>must be</b> defined as
 * <b>module fragment</b>, since it has to share classloader with the patched
 * module: put
 * <code><pre>
 * OpenIDE-Module-Fragment-Host: codenamebase
 * </pre></code>
 * into the Module's manifest. The <i><code>codenamebase</code></i> must identify the host module
 * which contains the class(es) to be patched.
 * <p/>
 * Note that it is not possible to support <b>removed constructors</b> this way; the
 * constructor has to be physically present on the API class. To provide backward-compatibile
 * code injection for constructors, please use {@link ConstructorDelegate} annotation.
 * <p/>
 * For examples, plese see the <a href="http://wiki.netbeans.org/BackwardCompatibilityPatches">NetBeans Wiki</a>
 * @since 7.44
 * @author sdedic
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface PatchFor {
    /**
     * The manifest header name.
     */
    public static final String MANIFEST_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host";
    
    /**
     * @return Class that should be changed to extend the annotated class
     */
    public Class<?> value();
}
