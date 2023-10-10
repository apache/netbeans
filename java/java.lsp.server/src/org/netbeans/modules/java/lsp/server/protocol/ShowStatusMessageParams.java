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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.Objects;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 *
 * @author sdedic
 */
public class ShowStatusMessageParams extends MessageParams {
    private Integer timeout;

    public ShowStatusMessageParams() {
    }

    public ShowStatusMessageParams(MessageType type, String message) {
        super(type, message);
    }

    public ShowStatusMessageParams(MessageType type, String message, int timeout) {
        super(type, message);
        this.timeout = timeout;
    }

    @Pure
    public Integer getTimeout() {
        return timeout;
    }

    public ShowStatusMessageParams setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (timeout == null ? 7 : this.timeout.hashCode());
        return hash;
    }

    @Pure
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ShowStatusMessageParams other = (ShowStatusMessageParams) obj;
        if (Objects.equals(this.timeout, other.timeout)) {
            return false;
        }
        return true;
    }
}
