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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import org.openide.filesystems.FileObject;

/**
 * This interface is meant to be implemented in projects where you 
 * can invoke the Use Entity Manager action. The code needed for accessing an EntityManager
 * depends on the context where it is needed, such as whether the target class
 * is a managed class or not.
 * 
 * @author Erno Mononen
 */
public interface EntityManagerGenerationStrategyResolver {
    
    /**
     * Resolves a generation strategy for the given <code>target</code>.
     * @param target the file object representing the java file that needs an
     * EntityManager. 
     * @return the class of the generation strategy or null if no appropriate strategy 
     * could be resolved.
     */ 
    Class<? extends EntityManagerGenerationStrategy> resolveStrategy(FileObject target);
    
}
