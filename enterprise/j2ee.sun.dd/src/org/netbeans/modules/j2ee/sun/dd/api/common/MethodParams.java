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
/*
 * MethodParams.java
 *
 * Created on November 18, 2004, 11:54 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface MethodParams extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String METHOD_PARAM = "MethodParam";	// NOI18N

    public String[] getMethodParam();
    public String getMethodParam(int index);
    public void setMethodParam(String[] value);
    public void setMethodParam(int index, String value);
    public int addMethodParam(String value);
    public int removeMethodParam(String value);
    public int sizeMethodParam();
    
}
