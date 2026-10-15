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

@Schema2Beans(
    schema="../../resources/orm_3_2.xsd",
    docRoot="entity-mappings",
    mddFile="../../resources/orm_3_2.mdd",
    schemaType=SchemaType.XML_SCHEMA,
    outputType=OutputType.TRADITIONAL_BASEBEAN,
    useInterfaces=true,
    validate=true,
    attrProp=true,
    removeUnreferencedNodes=true,
    java5=true
)
@org.netbeans.api.annotations.common.SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_INTERFACE", justification="Generated implementation classes")
package org.netbeans.modules.j2ee.persistence.dd.orm.model_3_2;

import org.netbeans.modules.schema2beans.Schema2Beans;
import org.netbeans.modules.schema2beans.Schema2Beans.OutputType;
import org.netbeans.modules.schema2beans.Schema2Beans.SchemaType;
