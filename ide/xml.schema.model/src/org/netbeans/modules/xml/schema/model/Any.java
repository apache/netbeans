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

package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents common features of any.
 * @author Chris Webster
 */
public interface Any {
	
	public static final String NAMESPACE_PROPERTY = "namespace";
	public static final String PROCESS_CONTENTS_PROPERTY = "processContents";
	
	// ##any, ##other (xs:anyURI | ##targetNamespace, ##local
	String getNamespace();
	void setNamespace(String namespace);
	String getNamespaceDefault();
        String getNameSpaceEffective();

	ProcessContents getProcessContents();
	void setProcessContents(ProcessContents pc);
	ProcessContents getProcessContentsDefault();
	ProcessContents getProcessContentsEffective();
        
	enum ProcessContents {
		SKIP("skip"), LAX("lax"), STRICT("strict");
                private String value;
                ProcessContents(String s) {
                    value = s;
                }
                public String toString() {
                    return value;
                }
	}
}
