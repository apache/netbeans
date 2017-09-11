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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.*;
import java.util.List;
import org.netbeans.modules.java.source.save.CasualDiff.LineInsertionType;

/**
 * Factory used for creating instances of position provider.
 *
 * @author Pavel Flaska
 */
final class EstimatorFactory {
    
    // prevent instance creation
    private EstimatorFactory() {
    }
    
    static PositionEstimator throwz(List<? extends ExpressionTree> oldL, 
                                    List<? extends ExpressionTree> newL,
                                    DiffContext diffContext)
    {
        return new PositionEstimator.ThrowsEstimator(oldL, newL, diffContext);
    }
    
    static PositionEstimator exportsOpensTo(List<? extends ExpressionTree> oldL, 
                                    List<? extends ExpressionTree> newL,
                                    DiffContext diffContext)
    {
        return new PositionEstimator.ExportsOpensToEstimator(oldL, newL, diffContext);
    }
    
    static PositionEstimator providesWith(List<? extends ExpressionTree> oldL, 
                                    List<? extends ExpressionTree> newL,
                                    DiffContext diffContext)
    {
        return new PositionEstimator.ProvidesWithEstimator(oldL, newL, diffContext);
    }
    
    static PositionEstimator implementz(List<? extends Tree> oldL, 
                                        List<? extends Tree> newL,
                                        DiffContext diffContext)
    {
        return new PositionEstimator.ImplementsEstimator(oldL, newL, diffContext);
    }
    
    static PositionEstimator extendz(List<? extends Tree> oldL, 
                                     List<? extends Tree> newL,
                                     DiffContext diffContext)
    {
        return new PositionEstimator.ExtendsEstimator(oldL, newL, diffContext);
    }
    
    static PositionEstimator statements(List<? extends Tree> oldL, 
                                     List<? extends Tree> newL,
                                     DiffContext diffContext)
    {
        return new PositionEstimator.MembersEstimator(oldL, newL, diffContext, false);
    }

    static PositionEstimator catches(List<? extends Tree> oldL, 
                                     List<? extends Tree> newL,
                                     boolean hasFinally,
                                     DiffContext diffContext)
    {
        return new PositionEstimator.CatchesEstimator(oldL, newL, hasFinally, diffContext);
    }
    
    static PositionEstimator cases(List<? extends Tree> oldL, 
                                     List<? extends Tree> newL,
                                     DiffContext diffContext)
    {
        return new PositionEstimator.CasesEstimator(oldL, newL, diffContext);
    }
    
    static PositionEstimator members(List<? extends Tree> oldL, 
                                     List<? extends Tree> newL,
                                     DiffContext diffContext)
    {
        return new PositionEstimator.MembersEstimator(oldL, newL, diffContext, true);
    }
    
    static PositionEstimator toplevel(List<? extends Tree> oldL, 
                                      List<? extends Tree> newL,
                                      DiffContext diffContext)
    {
        return new PositionEstimator.TopLevelEstimator(oldL, newL, diffContext);
    }

    static PositionEstimator annotations(List<? extends Tree> oldL, 
                                      List<? extends Tree> newL,
                                      DiffContext diffContext,
                                      boolean parameterPrint)
    {
        if (parameterPrint) {
            return new PositionEstimator.AnnotationsEstimator(oldL, newL, diffContext) {
                @Override
                public int prepare(int startPos, StringBuilder aHead, StringBuilder aTail) {
                    int result = super.prepare(startPos, aHead, aTail);
                    aTail.append(" ");
                    return result;
                }
                
                @Override
                public LineInsertionType lineInsertType() {
                    return LineInsertionType.NONE;
                }
            };
        } else {
            return new PositionEstimator.AnnotationsEstimator(oldL, newL, diffContext);
        }
    }
    
    /**
     * Provides offset positions for imports.
     * Consider compilation unit:
     * <pre>
     * package yerba.mate;
     *
     * import java.io.File;
     * import java.util.Collection; // utility methods
     * import java.util.Map;
     * // comment
     * import java.net.URL;
     *
     * public class Taragui {
     *    ...
     * }
     * </pre>
     *
     * Bounds for every import statement is marked by [] pair in next
     * sample:
     * <pre>
     * package yerba.mate;
     *
     * [import java.io.File;\n]
     * [import java.util.Collection; // utility methods\n]
     * [import java.util.Map;\n]
     * [// comment
     * import java.net.URL;\n]
     * \n
     * public class Taragui {
     *    ...
     * }
     * </pre>
     * These bounds are returned when user ask for offset of the specified
     * import statement.
     */
    static PositionEstimator imports(List<? extends ImportTree> oldL, 
                                     List<? extends ImportTree> newL,
                                     DiffContext diffContext)
    {
        return new PositionEstimator.ImportsEstimator(oldL, newL, diffContext);
    }
}
