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

package org.netbeans.modules.form;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

/**
 * A {@link RADProperty} which has no real target bean.
 *
 * @author Tomas Stupka
 */
public class FakeRADProperty extends RADProperty {

    /** Creates a new instance of FakeRADProperty */
    FakeRADProperty(RADComponent comp, FakePropertyDescriptor desc) throws IntrospectionException {
        super(comp, desc);
        setAccessType(NORMAL_RW);
    }

    @Override
    public Object getTargetValue() throws IllegalAccessException,
                                          InvocationTargetException {
        return null; // there is no real target
    }
    
    @Override
    public void setTargetValue(Object value) throws IllegalAccessException,
                                                 IllegalArgumentException,
                                                 InvocationTargetException {
    
        // there is no real target
    }    

}
