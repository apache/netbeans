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

package org.netbeans.modules.javaee.injection.spi;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;

/**
 * Knowledge of ability to use resouce injection in Java class and the way the injection is generated
 * @see org.netbeans.modules.javaee.injection.api.InjectionTargetQuery
 * @author Martin Adamek
 */
public interface InjectionTargetQueryImplementation {
    
    /**
     * Decide if dependency injection can be used in given class.<br>
     * @param controller CompilationController related to JavaSource
     * @param typeElement class where annotated field or method should be inserted,
     * if null is provided, main public class from file is taken
     * @return true if any container or environment is able to inject resources in given class, false otherwise
     */
    boolean isInjectionTarget(CompilationController controller, TypeElement typeElement);
    
    /**
     * Decide if injected reference must be static in given class. 
     * For example, in application client injection can be used only in class with main method and all
     * injected fields must be static<br>
     * Implementation 
     * @param controller CompilationController related to JavaSource
     * @param typeElement class where annotated field or method should be inserted,
     * if null is provided, main public class from file is taken
     * @return true if static reference is required in given class, false otherwise
     */
    boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement);
    
}
