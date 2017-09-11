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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.csl.api;

import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;

/**
 * A CancellableTask which should analyze the given CompilationInfo
 * and produce a set of highlights.
 * 
 * @author Tor Norbye
 */
public abstract class SemanticAnalyzer<T extends Parser.Result> extends ParserResultTask<T> {
    
    /**
     * Return a set of highlights computed by the last call to
     * Note - there are a number of EnumSet constants in the ColoringAttributes
     * class you should use for many of the common combinations of coloring
     * attributes you want.
     * {@link #run}.
     * <p>
     * <b>NOTE</b>: The OffsetRanges should NOT be overlapping! (The unit test
     * infrastructure in GsfTestBase will check for this condition and fail
     * semantic highlighting tests if they violate this constraint. The test
     * is not performed at runtime.)
     */
    public abstract @NonNull Map<OffsetRange, Set<ColoringAttributes>> getHighlights();

    // Not yet implemented:
    /**
     * Provide a custom description of a set of attributes. This may be shown
     * by the IDE as a tooltip when the user hovers over a region with the given
     * attribute set.  Just return null if you want to get the default
     * descriptions (a comma separated list of the attribute names, such as
     * "Unused", or "Unused, Field".
     * (With the offset range information as well as the CompilationInfo you
     * can provide more accurate descriptions for the user if applicable).
     * 
     * @param attributes
     * @return A localized String for the user describing the given attributes,
     *   or null to get the default supplied descriptions.
     */
    //String describe(CompilationInfo info, OffsetRange range, Set<ColoringAttributes> attributes);
}
