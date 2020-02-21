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

package org.netbeans.modules.cnd.highlight.semantic;

import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;

/**
 * Reference collector visits all references in the file and
 * creates a list of references that satisfy some criteria.
 * A good example is unused variable finder.
 *
 */
public interface ReferenceCollector {

    /**
     * Visit another reference in the file.
     *
     * @param ref  reference to visit
     * @param file  the file this reference belongs to
     */
    void visit(CsmReference ref, CsmFile file);

    /**
     * Returns a list of references satisfying some criteria.
     *
     * @return  list of references
     */
    List<CsmReference> getReferences();
    
    boolean cancelled();

}
