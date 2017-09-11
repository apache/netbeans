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

package org.netbeans.api.java.classpath;

/**
 * Java related classpath constants.
 * 
 * @author Jan Lahoda
 * @since 1.22
 */
public class JavaClassPathConstants {

    /**
     * ClassPath for annotation processors. If undefined, {@link ClassPath#COMPILE}
     * should be used.
     * <p class="nonnormative">
     * It corresponds to the <code>-processorpath</code> option of <code>javac</code>.
     * </p>
     *
     * @since 1.22
     */
    public static final String PROCESSOR_PATH = "classpath/processor";  //NOI18N
    
    /**
     * A part of the compilation classpath which is not included into runtime classpath.
     * @since 1.39
     */
    public static final String COMPILE_ONLY = "classpath/compile_only"; //NOI18N

    /**
     * Module path for bootstrap modules.
     * @since 1.64
     */
    public static final String MODULE_BOOT_PATH = "modules/boot";   //NOI18N
    /**
     * Module path for user modules.
     * @since 1.64
     */
    public static final String MODULE_COMPILE_PATH = "modules/compile"; //NOI18N
    /**
     * Additional classpath for modular compilation.
     * @since 1.64
     */
    public static final String MODULE_CLASS_PATH = "modules/classpath"; //NOI18N
    
    /**
     * Runtime module path for user modules.
     * @since 1.64
     */
    public static final String MODULE_EXECUTE_PATH="modules/execute";   //NOI18N
    
    /**
     * Runtime additional classpath for modular compilation.
     * @since 1.64
     */
    public static final String MODULE_EXECUTE_CLASS_PATH = "modules/execute-classpath"; //NOI18N

    /**
     * Module source path.
     * @since 1.65
     */
    public static final String MODULE_SOURCE_PATH ="modules/source";    //NOI18N

    /**
     * Module processor path.
     * @since 1.66
     */
    public static final String MODULE_PROCESSOR_PATH ="modules/processor";    //NOI18N
}
