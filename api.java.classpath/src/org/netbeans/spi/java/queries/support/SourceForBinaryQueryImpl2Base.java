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

package org.netbeans.spi.java.queries.support;

import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.queries.SFBQImpl2Result;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.util.Parameters;

/**
 * Base class for {@link SourceForBinaryQueryImplementation2} which need to delegate
 * to other {@link SourceForBinaryQueryImplementation}.
 * @since 1.16
 * @author Tomas Zezula
 */
public abstract class SourceForBinaryQueryImpl2Base implements SourceForBinaryQueryImplementation2 {

    /**
     * Creates a wrapper for {@link SourceForBinaryQuery.Result}. This method
     * should be used by delegating {@link SourceForBinaryQueryImplementation2}
     * which need to delegate to {@link SourceForBinaryQueryImplementation}. 
     * @param result returned by {@link SourceForBinaryQueryImplementation},
     * When result is already instanceof {@link SourceForBinaryQueryImplementation2.Result}
     * it's returned without wrapping.
     * @return a {@link SourceForBinaryQueryImplementation2.Result}.
     */
    protected final Result asResult (SourceForBinaryQuery.Result result) {
        Parameters.notNull("result", result);   //NOI18N
        if (result instanceof Result) {
            return (Result) result;
        }
        else {
            return new SFBQImpl2Result(result);
        }
    }
}
