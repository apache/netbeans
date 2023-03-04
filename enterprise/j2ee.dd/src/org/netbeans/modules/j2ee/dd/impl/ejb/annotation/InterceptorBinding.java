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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

/**
 *
 * @author Martin Adamek
 */
public interface InterceptorBinding {

    int addDescription(String value);
    int addInterceptorClass(String value);
    String[] getDescription();
    String getDescription(int index);
    String getEjbName();
    String[] getInterceptorClass();
    String getInterceptorClass(int index);
    InterceptorOrderImpl getInterceptorOrder();
    NamedMethodImpl getMethod();
    boolean isExcludeClassInterceptors();
    boolean isExcludeDefaultInterceptors();
    InterceptorOrderImpl newInterceptorOrder();
    NamedMethodImpl newNamedMethod();
    int removeDescription(String value);
    int removeInterceptorClass(String value);
    void setDescription(int index, String value);
    void setDescription(String[] value);
    void setEjbName(String value);
    void setExcludeClassInterceptors(boolean value);
    void setExcludeDefaultInterceptors(boolean value);
    void setInterceptorClass(int index, String value);
    void setInterceptorClass(String[] value);
    void setInterceptorOrder(InterceptorOrderImpl value);
    void setMethod(NamedMethodImpl value);
    int sizeDescription();
    int sizeInterceptorClass();
    
}
