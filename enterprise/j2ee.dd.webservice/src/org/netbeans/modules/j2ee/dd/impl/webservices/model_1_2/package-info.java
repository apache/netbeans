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

@Schema2Beans(
    schema="../../resources/javaee_web_services_1_2.xsd",
    schemaType=SchemaType.XML_SCHEMA,
    mddFile="../../resources/javaee_web_services_1_2.mdd",
    validate=true,
    java5=true,
    attrProp=true,
	generateHasChanged=true,
	outputType=OutputType.JAVABEANS,
	commonInterface="CommonBean",
	useInterfaces=true,
	extendBaseBean=true,
	finder={
        "on /webservices find webservice-description by webservice-description-name",
        "on /webservices/webservice-description/port-component find handler by handler-name",
        "on /webservices/webservice-description/port-component/handler find init-param by param-name",
        "on /webservices/webservice-description find port-component by port-component-name"
    }
)
package org.netbeans.modules.j2ee.dd.impl.webservices.model_1_2;

import org.netbeans.modules.schema2beans.Schema2Beans;
import org.netbeans.modules.schema2beans.Schema2Beans.OutputType;
import org.netbeans.modules.schema2beans.Schema2Beans.SchemaType;
