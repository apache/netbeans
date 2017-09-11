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
package org.netbeans.spi.java.hints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.prefs.Preferences;

/**Specify an option that affects the way the hint works.
 *
 * Only {@code static final String} compile-time constant can be marked with this
 * annotation. The value of the constant will be used as the key to the hint's {@link Preferences}.
 *
 * For hints that consist of a class, all options that are directly enclosed in the class
 * will be used in their source order.
 *
 * For hints that consist of a single method, use {@link UseOptions} to specify which options
 * from the enclosing class should be used. The order of the options will be the order in which
 * they appear in the source code of the enclosing class.
 *
 * The customizer will be generated automatically when {@link BooleanOption} is used.
 *
 * Two keys need to be defined in the corresponding {@code Bundle.properties}:
 * {@code LBL_<class-fqn>.<field_name>}, which will be used as the display name of
 * the corresponding checkbox in the customizer, and {@code TP_<class-fqn>.<field_name>}
 * which will be used as the tooltip of the checkbox.
 *
 * @author lahvac
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface BooleanOption {

    /**The options' display name. Will be used as the display name of the
     * checkbox in the customizer.
     */
    public String displayName();
    /**The tooltip of the checkbox in the customizer.
     */
    public String tooltip();
    
    /**The default value of the option.
     */
    public boolean defaultValue();
    
}
