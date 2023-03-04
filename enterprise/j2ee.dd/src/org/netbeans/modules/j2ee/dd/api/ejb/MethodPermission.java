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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface MethodPermission extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {

        public static final String ROLE_NAME = "RoleName";	// NOI18N
	public static final String UNCHECKED = "Unchecked";	// NOI18N
	public static final String UNCHECKEDID = "UncheckedId";	// NOI18N
	public static final String METHOD = "Method";	// NOI18N
        
        public void setRoleName(int index, String value);

        public String getRoleName(int index);

        public void setRoleName(String[] value);

        public String[] getRoleName();
        
        public int sizeRoleName();
        
        public int removeRoleName(String value);

	public int addRoleName(String value);
        
        public void setUnchecked(boolean value) throws VersionNotSupportedException;

        public boolean isUnchecked() throws VersionNotSupportedException;
        
        public void setMethod(int index, Method value);

        public Method getMethod(int index);

        public void setMethod(Method[] value);

        public Method[] getMethod();
        
	public int addMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method value);

	public int sizeMethod();

	public int removeMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method value);

        public Method newMethod();
        
}

