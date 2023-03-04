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

package org.netbeans.modules.javaee.wildfly.config;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 * @author Libor Kotouc
 */
public class WildflyMessageDestination implements MessageDestination {

    public static final String QUEUE_PREFIX = "queue/";
    public static final String TOPIC_PREFIX = "topic/";
    private final String name;
    private final Set<String> jndiNames = new HashSet<String>(1);
    private final Type type;

    public WildflyMessageDestination(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void addEntry(String jndiName) {
        jndiNames.add(jndiName);
    }

    public Set<String> getJndiNames() {
        return jndiNames;
    }

}
