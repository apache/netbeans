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
 * MessageDestinationRef.java
 *
 * Created on August 02, 2005, 5:19 PM
 */
package org.netbeans.modules.j2ee.sun.dd.api.common;

public interface MessageDestinationRef extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String MESSAGE_DESTINATION_REF_NAME = "MessageDestinationRefName";	// NOI18N
	public static final String JNDI_NAME = "JndiName";	// NOI18N

	public void setMessageDestinationRefName(String value);

	public String getMessageDestinationRefName();

	public void setJndiName(String value);

	public String getJndiName();

}
