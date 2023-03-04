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

package org.netbeans.test.stub.spi;

import org.netbeans.test.stub.api.StubDelegate;

/**
 *
 * @author Andrei Badea
 */
public interface StubImplementation {
    
    public Object create(Class[] intfs);
    
    public Object create(Class[] intfs, StubDelegate delegate);
    
    public Object getDelegate(Object stub);
    
    public void setProperty(Object stub, Object key, Object value);
    
    // also need
    // public abstract Object create(Class[] intfs, Object delegate, String setPropertyMethodName);
    // for cases when StubDelegate.setProperty() collides with a method that the delegate must implement
}
