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
package org.netbeans.core.network.proxy.pac.impl;

import org.netbeans.core.network.proxy.pac.PacScriptEvaluatorNoProxy;
import org.netbeans.core.network.proxy.pac.PacParsingException;
import org.netbeans.core.network.proxy.pac.PacScriptEvaluator;
import org.netbeans.core.network.proxy.pac.PacScriptEvaluatorFactory;
import org.openide.util.lookup.ServiceProvider;



/**
 * NetBeans default implementation of {@code PacScriptEvaluatorFactory}.
 * 
 * <p>Returns an instance of {@link NbPacScriptEvaluator}.
 * 
 * @author lbruun
 */
@ServiceProvider(service = PacScriptEvaluatorFactory.class)
public class NbPacScriptEvaluatorFactory implements PacScriptEvaluatorFactory {

    @Override
    public PacScriptEvaluator createPacScriptEvaluator(String pacSource) throws PacParsingException {
       
        if (pacSource == null || pacSource.isEmpty()) {
            return new PacScriptEvaluatorNoProxy();
        }
        return new NbPacScriptEvaluator(pacSource);
    }

    @Override
    public PacScriptEvaluator getNoOpEvaluator() {
           return new PacScriptEvaluatorNoProxy();
    }
}
