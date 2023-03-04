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

package org.netbeans.modules.form.codestructure;

/**
 * @author Tomas Pavek
 */

public interface CodeExpressionOrigin {

    // type of the expression
    public Class getType();

    // parent expression from which this is created (can be null)
    public CodeExpression getParentExpression();

    // meta object representing the expression
    // (e.g. Constructor, Method, Field)
    public Object getMetaObject();

    // the value of the expression (if available)
    public Object getValue();

    // parameters for creating the expression
    public CodeExpression[] getCreationParameters();

    public String getJavaCodeString(String parentStr, String[] paramsStr);
}
