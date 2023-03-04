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
package org.netbeans.modules.javascript2.extdoc.completion;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTag;
import org.netbeans.modules.javascript2.doc.spi.ParameterFormat;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TypeNamedTag extends AnnotationCompletionTag {

    public static final String TEMPLATE = " {${type}} ${name} ${description}";

    public TypeNamedTag(String name) {
        super(name, name + TEMPLATE);
    }

    @Override
    public List<ParameterFormat> getParameters() {
        List<ParameterFormat> ret = new ArrayList<ParameterFormat>(3);
        ret.add(new ParameterFormat(" {", "type", "}")); //NOI18N
        ret.add(new ParameterFormat(" ", "name", null)); //NOI18N
        ret.add(new ParameterFormat(" ", "description", null)); //NOI18N
        return ret;
    }

}
