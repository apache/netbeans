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

package org.netbeans.modules.debugger.ui.registry;

import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator.EvaluatorService;

/**
 *
 * @author Martin Entlicher
 */
public class EvaluatorServiceContextAware extends EvaluatorService implements ContextAwareService<EvaluatorService> {
    
    private final String serviceName;
    private ContextProvider context;
    private EvaluatorService delegate;
    
    public EvaluatorServiceContextAware(String serviceName) {
        this.serviceName = serviceName;
    }
    
    @Override
    public EvaluatorService forContext(ContextProvider context) {
        if (context == this.context) {
            return this;
        } else {
            return (EvaluatorService) ContextAwareSupport.createInstance(serviceName, context);
        }
    }

    @Override
    public void setupContext(JEditorPane editorPane, Runnable contextSetUp) {
    }

    @Override
    public boolean canEvaluate() {
        return false;
    }

    @Override
    public void evaluate(String expression) {
    }

    @Override
    public List<String> getExpressionsHistory() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Creates instance of <code>ContextAwareService</code> based on layer.xml
     * attribute values
     *
     * @param attrs attributes loaded from layer.xml
     * @return new <code>ContextAwareService</code> instance
     */
    static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
        String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
        return new EvaluatorServiceContextAware(serviceName);
    }

}
