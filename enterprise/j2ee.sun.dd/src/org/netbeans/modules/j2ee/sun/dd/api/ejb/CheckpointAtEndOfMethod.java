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
 * CheckpointAtEndOfMethod.java
 *
 * Created on November 18, 2004, 3:35 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author Nitya Doraisamy
 */
public interface CheckpointAtEndOfMethod extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String METHOD = "Method";	// NOI18N

    public Method [] getMethod ();
    public Method  getMethod (int index);
    public void setMethod (Method [] value);
    public void setMethod (int index, Method  value);
    public int addMethod (Method  value);
    public int removeMethod (Method  value);
    public int sizeMethod ();
    public Method  newMethod ();
}
