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
package org.netbeans.modules.form.fakepeer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.openide.ErrorManager;

/**
 *
 * An {@link InvocationHandler} used for dynamic interface implementation
 * in {@link FakePeerSupport}.
 *
 * @author Tomas Stupka
 */
public class FakePeerInvocationHandler implements InvocationHandler {        
    
    private final FakeComponentPeer comp;
    
    /**
     *
     */
    public FakePeerInvocationHandler (FakeComponentPeer comp) {
        this.comp = comp;
    }
    
    /**
     * @see InvocationHandler#invoke(Object proxy, Method method, Object[] args)
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) 
       throws Throwable {        
       
        try {
            
            Class[] parameters = method.getParameterTypes();        
            Method thisMethod = comp.getClass().getMethod(method.getName(), parameters);        
            return thisMethod.invoke(comp, args);                     
            
            /* 
             * jdk 1.6 redefines the requestFocus() method in PeerComponent with a new parameter  
             * which is from a new type, unknown in previous jdk releases (<1.6), so we cannot 
             * just reimplement the method in FakePeerComponent.
             *
             * In case we should in future get a NoSuchMethodException, because of 
             * invoking the requestFocus() method with the jdk1.6 signature, we can implement 
             * here a special routine which return a proper value to the caller... .
             *
             */
            
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            throw e;
        }        
        
    }
    
}
