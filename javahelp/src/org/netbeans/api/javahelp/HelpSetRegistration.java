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

package org.netbeans.api.javahelp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a help set.
 * A help set reference according to {@code -//NetBeans//DTD JavaHelp Help Set Reference 1.0//EN} is created.
 * If the help set specifies a search view, the search indexer will also be run;
 * all {@code *.html} and {@code *.htm} in the package containing the help set, and its subpackages, will be indexed.
 * @since org.netbeans.modules.javahelp/1 2.20
 * @see <a href="@TOP@/apichanges.html#HelpSetRegistration">how to convert to this annotation</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface HelpSetRegistration {

    /**
     * Relative location of a help set file.
     * Typically matches: {@code -//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN}
     */
    String helpSet();

    /**
     * Whether the help set should be merged into the master help set.
     */
    boolean merge() default true;

    /**
     * Position of help set reference.
     */
    int position() default Integer.MAX_VALUE;

    /**
     * Helpset-relative HTML filenames to exclude from indexing.
     */
    String[] excludes() default {"credits.html"};

}
