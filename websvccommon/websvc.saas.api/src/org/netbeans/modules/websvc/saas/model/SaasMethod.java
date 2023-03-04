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

package org.netbeans.modules.websvc.saas.model;

import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Input;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Output;

/**
 *
 * @author nam
 */
public class SaasMethod implements Comparable<SaasMethod> {
    private final Method method;
    private final Saas saas;
    
    public SaasMethod(Saas saas, Method method) {
        this.saas = saas;
        this.method = method;
    }

    public Saas getSaas() {
        return saas;
    }
    
    public Method getMethod() {
        return method;
    }

    protected Output getOutput() {
        return method.getOutput();
    }

    public String getName() {
        return method.getName();
    }

    public String getDisplayName() {
        return method.getName();
    }
    
    protected Input getInput() {
        return method.getInput();
    }

    protected String getHref() {
        return method.getHref();
    }

    public String getDocumentation() {
        return method.getDocumentation();
    }
    
    public int compareTo(SaasMethod method) {
        return getDisplayName().compareTo(method.getDisplayName());
    }
}
