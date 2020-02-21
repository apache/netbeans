/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.api.xml;

/**
 * Means of passing XML attribute/value pairs to {@link XMLEncoderStream}.
 * <p>
 * There is no need to escape attribute values.
 * <p>
 * <pre>
 AttrValuePair attrs[] = {
    new AttrValuePair("firstName", person.getFirstName()),
    new AttrValuePair("lastName", person.getLastName()),
 }
 * </pre>
 */
public final class AttrValuePair {
    private final String attr;
    private final String value;

    public AttrValuePair(String attr, String value) {
	this.attr = attr;
	this.value = value;
    }

    public String getAttr() {
	return attr;
    } 

    public String getValue() {
	return value;
    }
}
