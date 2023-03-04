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
package org.netbeans.modules.php.dbgp.packets;

import org.netbeans.modules.php.dbgp.packets.Stack.Type;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class Input extends BaseMessageChildElement {
    private static final String LINENO = "lineno"; // NOI18N
    private static final String FILENAME = "filename"; // NOI18N
    private static final String TYPE = "type"; // NOI18N
    private static final String LEVEL = "level"; // NOI18N

    Input(Node node) {
        super(node);
    }

    public int getLevel() {
        return getInt(LEVEL);
    }

    public Type getType() {
        return Type.forString(getAttribute(TYPE));
    }

    public String getFileName() {
        return getAttribute(FILENAME);
    }

    public int getLine() {
        return getInt(LINENO);
    }

}
