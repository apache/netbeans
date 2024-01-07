/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    
    static PositionEstimator casePatterns(List<? extends Tree> oldL, 
                                          List<? extends Tree> newL,
                                          DiffContext diffContext)
    {
        return new PositionEstimator.CasePatternEstimator(oldL, newL, diffContext);
    }
    
    static PositionEstimator stringTemplate(List<? extends Tree> oldL,
                                          List<? extends Tree> newL,
                                          DiffContext diffContext)
    {
        return new PositionEstimator.StringTemaplateEstimator(oldL, newL, diffContext);
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
