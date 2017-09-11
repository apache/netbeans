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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.queries;

import java.net.URL;
import org.netbeans.api.java.queries.SourceForBinaryQuery;

/**
 * Information about where Java sources corresponding to binaries
 * (classfiles) can be found. 
 * <p>
 * In addition to the original SourceForBinaryQueryImplementation this interface
 * also provides information used by the java infrastructure if sources should be
 * preferred over the binaries. When sources are preferred the java infrastructure
 * will use sources as a primary source of the metadata otherwise the binaries
 * (classfiles) are used as a primary source of information and sources are used
 * as a source of formal parameter names and javadoc only.
 * In general sources should be preferred for projects which are user editable
 * but not for libraries or platforms where the sources may not be complete or 
 * up to date.
 * </p>
 * @see org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation
 * @since org.netbeans.api.java/1 1.15
 */
public interface SourceForBinaryQueryImplementation2 extends SourceForBinaryQueryImplementation {

    /**
     * Returns the source root(s) for a given binary root.
     * @see SourceForBinaryQueryImplementation#findSourceRoots(java.net.URL) 
     * @param binaryRoot the class path root of Java class files
     * @return a result object encapsulating the answer or null if the binaryRoot is not recognized
     */
    public Result findSourceRoots2 (final URL binaryRoot);
    
    public static interface Result extends SourceForBinaryQuery.Result {
        
        /**
         * When true the java model prefers sources otherwise binaries are used.
         * Project's {@link SourceForBinaryQueryImplementation} should return
         * true. The platform and libraries {@link SourceForBinaryQueryImplementation}
         * should return false - the attached sources may not be complete.
         * @see SourceForBinaryQueryImplementation2
         * @return true if sources should be used by the java infrastructure
         */
        public boolean preferSources();
    }
}
