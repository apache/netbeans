/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2002-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.api.settings;

import java.beans.PropertyChangeListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Specifies the kind of persistence to use of the annotated class.
 * Uses {@link XMLDecoder} and {@link XMLEncoder} to store and read
 * values of the class (and by default also its subclasses).
 * <p>
 * The format uses getters and setters of the bean and usually needs
 * default constructor:
 * <pre>
 * <code>@</code>ConvertAsJavaBean
 * <font class="type">public class</font> YourObject {
 *   <font class="type">public</font> YourObject() {}
 *   <font class="type">public</font> <font class="type">String</font> <font class="function-name">getName</font>();
 *   <font class="type">public void</font> <font class="function-name">setName</font>(<font class="type">String</font> <font class="variable-name">name</font>);
 * }
 * </pre>
 * If the bean supports {@link PropertyChangeListener} notifications and
 * contains <code>addPropertyChangeListener</code> method, the system
 * starts to listen on existing objects and in case a property change
 * is delivered, the new state of the object is persisted again.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 1.20
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface ConvertAsJavaBean {
    /** Shall subclasses of this class be also converted as JavaBeans? */
    boolean subclasses() default true;
}
