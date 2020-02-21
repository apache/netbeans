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

package org.netbeans.modules.cnd.discovery.api;

import java.util.List;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.util.Cancellable;

/**
 *
 */
public interface DiscoveryProvider extends Cancellable {

    /**
     * @return provider ID
     */
    String getID();
    
    /**
     * @return provider name
     */
    String getName();

    /**
     * @return provider description
     */
    String getDescription();
    
    /**
     * @return property keys of additional information for provider
     */
    List<String> getPropertyKeys();

    /**
     * @param key property name
     * 
     * @return property of additional information for provider
     */
    ProviderProperty getProperty(String key);
    
    /**
     * @param project proxy project
     * 
     * @return true if analyzer is applicable to project
     */
    boolean isApplicable(ProjectProxy project);

    /**
     * Can analyze project. Returns weight of assurance of results.
     * Results range is [0,100].
     * 0 provider is not sure about results at all
     * 100 provider is sure about results
     * 
     * @param project project proxy
     * @param interrupter interrupter
     * 
     * @return is analyzer applicable to project
     */
    DiscoveryExtensionInterface.Applicable canAnalyze(ProjectProxy project, Interrupter interrupter);

    /**
     * @param project project proxy
     * @param progress progress
     * @param interrupter interrupter
     * 
     * @return analyzes project and returns list of configuration
     */
    public List<Configuration> analyze(ProjectProxy project, Progress progress, Interrupter interrupter);
    
    /**
     * Stop analyzing.
     */
    @Override
    boolean cancel();
}
