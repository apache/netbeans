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
package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import org.netbeans.modules.j2ee.persistence.action.*;
import com.sun.source.tree.ClassTree;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;

/**
 * This interface represents a generation strategy for generating
 * the code needed to access an EntityManager.
 * 
 * @author Erno Mononen
 */
public interface EntityManagerGenerationStrategy {

    void setTreeMaker(TreeMaker treeMaker);

    void setClassTree(ClassTree classTree);

    void setWorkingCopy(WorkingCopy workingCopy);

    void setGenUtils(GenerationUtils genUtils);

    void setPersistenceUnit(PersistenceUnit persistenceUnit);

    void setGenerationOptions(GenerationOptions generationOptions);

    /**
     * Generate the code needed to access an EntityManager. 
     * @return the modified ClassTree. 
     */ 
    ClassTree generate();
}
