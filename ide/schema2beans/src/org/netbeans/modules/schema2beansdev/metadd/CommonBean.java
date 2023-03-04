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
/**
 * This interface is the intersection of all generated methods.
 * 
 * @Generated
 */

package org.netbeans.modules.schema2beansdev.metadd;

public interface CommonBean {
	public void changePropertyByName(String name, Object value);

	public org.netbeans.modules.schema2beansdev.metadd.CommonBean[] childBeans(boolean recursive);

	public void childBeans(boolean recursive, java.util.List beans);

	public boolean equals(Object o);

	public Object fetchPropertyByName(String name);

	public int hashCode();

	public boolean isVetoable();

	public String nameChild(Object childObj);

	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName);

	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName);

	public String nameSelf();

	public void readNode(org.w3c.dom.Node node);

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes);

	public void setVetoable(boolean value);

	public String toString();

	public void validate() throws org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException;

	public void writeNode(java.io.Writer out) throws java.io.IOException;

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException;

}
