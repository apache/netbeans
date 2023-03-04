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
package org.netbeans.modules.javaee.specs.support.spi;

import org.netbeans.api.project.Project;


/**
 * @author ads
 *
 */
public interface JaxRsStackSupportImplementation {

    /**
     * Adds JSR311 API into project's classpath if it supported.
     * Returns <code>false</code> if jsr311 is  not added ( not supported).
     * @param project project which classpath should be extended
     * @return <code>true</code> if project's classpath is extended with jsr311
     */
    boolean addJsr311Api( Project project );

    /**
     * Extends project's classpath  with Jersey libraries.
     * @param project project which classpath should be extended
     * @return  <code>true</code> if project's classpath is extended
     */
    boolean extendsJerseyProjectClasspath( Project project );

    /**
     * Clear project classapth .
     * @param project project which classpath should be cleared
     */
    void removeJaxRsLibraries( Project project );
    
    /**
     * If custom Jersey library is chosen ( f.e. NB bundled ) then 
     * some JEE servers require additional project configuration.
     * Otherwise collision could happen between bundled server Jersey and custom library.
     */
    public void configureCustomJersey( Project project );

    /**
     * Checks whether class with <code>classFqn</code> FQN is bundled with
     * JEE server distribution. 
     * @param classFqn class FQN 
     * @return true if JEE server bundled with <code>classFqn</code> 
     */
    boolean isBundled( String classFqn );

}
