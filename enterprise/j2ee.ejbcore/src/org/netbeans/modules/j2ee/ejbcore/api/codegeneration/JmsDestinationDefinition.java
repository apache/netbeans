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
package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 * Represents simple JmsDestinationDefinition which can be generated into class file.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class JmsDestinationDefinition implements MessageDestination {

    private final String name;
    private final Type type;
    private final boolean toGenerate;

    public JmsDestinationDefinition(String name, Type type, boolean toGenerate) {
        this.name = name;
        this.type = type;
        this.toGenerate = toGenerate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    public boolean isToGenerate() {
        return toGenerate;
    }

}
