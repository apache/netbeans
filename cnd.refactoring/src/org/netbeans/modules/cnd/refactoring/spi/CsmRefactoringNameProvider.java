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
package org.netbeans.modules.cnd.refactoring.spi;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;

/**
 *
 */
public interface CsmRefactoringNameProvider {
    /**
     * provide name to be used in refactorings
     * @param refactoring refactoring instance
     * @param object object to be refactored
     * @param current current calculated name to adjust if needed
     * @return adjusted name or null if no name adjusting done by provider
     */
    public String getRefactoredName(CsmObject object, String current);

    /**
     * adjust new text for reference to be used as replacement text.
     * @param ref reference to be refactored
     * @param newText current calculated text to adjust if needed
     * @param refactoring current refactoring
     * @return adjusted new text or null if no name adjusting done by provider
     */
    public String getReplaceText(CsmReference ref, String newText, AbstractRefactoring refactoring);

    /**
     * provides rename description.
     * @param ref reference to be refactored
     * @param refactoring current refactoring
     * @return description or null if not known by this ref
     */
    public String getReplaceDescription(CsmReference ref, AbstractRefactoring refactoring);
}
