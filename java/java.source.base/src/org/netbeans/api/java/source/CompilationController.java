/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.java.source;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParserResult;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/** Class for explicit invocation of compilation phases on a java source.
 *  The implementation delegates to the {@link CompilationInfo} to get the data,
 *  the access to {@link CompilationInfo} is not synchronized, so the class isn't
 *  reentrant.
 *  
 *  XXX: make toPhase automatic in getTrees(), Trees.getElement, etc....
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CompilationController extends CompilationInfo {
    
    private final List<FileObject> forcedSources = new ArrayList<>();

    CompilationController(final CompilationInfoImpl impl) {        
        super(impl);

    }

    /**
     * Returns an instance of the {@link CompilationController} for
     * given {@link Parser.Result} if it is a result
     * of a java parser.
     * @param result for which the {@link CompilationController} should be
     * returned.
     * @return a {@link CompilationController} or null when the given result
     * is not a result of java parsing.
     * @since 0.42
     */
    public static @NullUnknown CompilationController get (final @NonNull Parser.Result result) {
        Parameters.notNull("result", result);   //NOI18N
        CompilationController info = null;
        if (result instanceof JavacParserResult) {
            final JavacParserResult javacResult = (JavacParserResult)result;            
            info = javacResult.get(CompilationController.class);            
        }
        return info;
    }
        
    // API of the class --------------------------------------------------------
    
    /** Moves the state to required phase. If given state was already reached 
     * the state is not changed. The method will throw exception if a state is 
     * illegal required. Acceptable parameters for thid method are <BR>
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.PARSED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.UP_TO_DATE}   
     * @param phase The required phase
     * @return the reached state
     * @throws IllegalArgumentException in case that given state can not be 
     *         reached using this method
     * @throws IOException when the file cannot be red
     */    
    public @NonNull JavaSource.Phase toPhase(@NonNull JavaSource.Phase phase ) throws IOException {
        return impl.toPhase (phase, forcedSources);
    }

    /**
     * Marks this {@link CompilationInfo} as invalid, may be used to
     * verify confinement.
     */
    @Override
    protected void doInvalidate () {
        final JavacParser parser = this.impl.getParser();    //Parser may be null in case when JS was
                                                                         //created with no sources - java corner case
                                                                         //not covered by parsing API.
        if (parser != null) {
            parser.resultFinished (false);
        }
    }

    void addForceSource(FileObject file) {
        forcedSources.add(file);
    }

}
