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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.spi.sendopts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a non-static field in a class as an option and assigns it a short,
 * or long name. Usually used together with {@link Description} which provides
 * human readable explanation of the option's behavior. The field should be
 * public and the class should have public default constructor. It is suggested
 * the class implements {@link ArgsProcessor} or at least {@link Runnable} - its
 * methods will be called after successful assignment of argument fields. Here
 * is an example:
 * <pre>
 * public final class YourOptions implements {@link ArgsProcessor} {
 *   // Defines an option without any arguments
 *   public {@code @}{@link Arg}(shortName='p', longName="") public boolean usedWithO;
 *   // if such option is present on the command line, the value of the 
 *   // <code>usedWithO</code> field is set to <code>true</code>. Otherwise its
 *   // value remains unchanged (e.g. <code>false</code>).
 * 
 * 
 *   // One can also annotate a {@link String} field which then becomes 
 *   // an option with a {@link org.netbeans.spi.sendopts.Option#requiredArgument(char, java.lang.String) required argument}:
 *   public {@code @}{@link Arg}(shortName='r', longName="") public String requiredArg;
 * 
 *   // If one annotates a field where an array of {@link String strings} can be 
 *   // assigned, such option will then contain all 
 *   // {@link org.netbeans.spi.sendopts.Option#additionalArguments(char, java.lang.String) additional arguments}
 *   // made available:
 *   public {@code @}{@link Arg}(longName="additional") public String[] additionalArgs;
 * 
 *   // To define an option with {@link org.netbeans.spi.sendopts.Option#optionalArgument(char, java.lang.String) optional argument}
 *   // one can annotate string field and provide its default value:
 *   {@code @}{@link Arg}(shortName='o', longName="", defaultValue="used-but-no-argument-provided") public String optionArg;
 * 
 *   public void process({@link Env} env) {
 *     // when this method is called, above defined fields are initialized
 *   }
 * }
 * </pre>
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 2.20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Arg {
    /** One character name of the option. Will be prefixed with single <em>-</em> 
     when used on command line. */
    char shortName() default org.netbeans.spi.sendopts.Option.NO_SHORT_NAME;
    /** Multi character name. Needs to be prefixed with <em>--</em> on the command
     * line. Use {@code ""} to assign no long name to the option.
     */
    String longName();
    
    /** Specifies whether this field should be implicit/{@link org.netbeans.spi.sendopts.Option#defaultArguments() default}.
     * There may be only one implicit option in the system. If there are 
     * arguments not consumed by any other option, they are passed to it.
     * The implicit options may annotate only fields of type <code>String[]</code>.
     * 
     * @return return true, if this option should be implicit
     */
    boolean implicit() default false;
    
    /** Some fields may require no argument and still be present. For those
     * one needs to specify default value which will be assigned to the field,
     * if the option is present without any argument.
     */
    String defaultValue() default "\u0000";
}
