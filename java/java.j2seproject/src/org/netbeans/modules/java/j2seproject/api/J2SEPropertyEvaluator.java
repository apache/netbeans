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

package org.netbeans.modules.java.j2seproject.api;

import org.netbeans.modules.java.api.common.project.PropertyEvaluatorProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Readonly access to project properties through PropertyEvaluator,
 * an instance will be in lookup of the j2seproject.
 * 
 * @author Milan Kubec
 * @since 1.10
 */
public interface J2SEPropertyEvaluator extends PropertyEvaluatorProvider {
    /**
     * Gives PropertyEvaluator for resolving project properties
     *
     * @return PropertyEvaluator for given project
     */
    PropertyEvaluator evaluator();

    @Override
    public default PropertyEvaluator getPropertyEvaluator() {
        return evaluator();
    }
}
