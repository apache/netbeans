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

package org.netbeans.modules.cnd.spi.model.services;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CodeModelProblemResolver {
    private static final CodeModelProblemResolver DEFAULT = new Default();

    protected CodeModelProblemResolver() {
    }

    /** 
     * Static method to obtain the problem resolver
     * @return the problem detector
     */
    public static ParsingProblemDetector getParsingProblemDetector(CsmProject project) {
        return DEFAULT.createResolver(project);
    }

    public interface ParsingProblemDetector {
        void start();
        void switchToDeterminate(int maxWorkUnits);
        void finish();
        String nextCsmFile(CsmFile file, int fileLineCount, int current, int allWork);
        String getRemainingTime();
    }
    
    public abstract ParsingProblemDetector createResolver(CsmProject project);
    
    /**
     * Implementation of the default selector
     */
    private static final class Default extends CodeModelProblemResolver {
        private final Lookup.Result<CodeModelProblemResolver> res;
        Default() {
            res = Lookup.getDefault().lookupResult(CodeModelProblemResolver.class);
        }

        @Override
        public ParsingProblemDetector createResolver(CsmProject project) {
            for (CodeModelProblemResolver service : res.allInstances()) {
                ParsingProblemDetector createResolver = service.createResolver(project);
                if (createResolver != null) {
                    return createResolver;
                }
            }
            return null;
        }
    }
}
