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

package org.apache.tools.ant.module.spi;

import java.util.Set;
import org.apache.tools.ant.module.run.LoggerTrampoline;

/**
 * Describes the structure of a task.
 * Each instance corresponds to one task or nested element in a build script.
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.12
 */
public final class TaskStructure {

    static {
        LoggerTrampoline.TASK_STRUCTURE_CREATOR = new LoggerTrampoline.Creator() {
            public AntSession makeAntSession(LoggerTrampoline.AntSessionImpl impl) {
                throw new AssertionError();
            }
            public AntEvent makeAntEvent(LoggerTrampoline.AntEventImpl impl) {
                throw new AssertionError();
            }
            public TaskStructure makeTaskStructure(LoggerTrampoline.TaskStructureImpl impl) {
                return new TaskStructure(impl);
            }
        };
    }
    
    private final LoggerTrampoline.TaskStructureImpl impl;
    private TaskStructure(LoggerTrampoline.TaskStructureImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Get the element name.
     * XXX precise behavior w.r.t. namespaces etc.
     * @return a name, never null
     */
    public String getName() {
        return impl.getName();
    }
    
    /**
     * Get a single attribute.
     * It will be unevaluated as configured in the script.
     * If you wish to find the actual runtime value, you may
     * use {@link AntEvent#evaluate}.
     * @param name the attribute name
     * @return the raw value of that attribute, or null
     */
    public String getAttribute(String name) {
        return impl.getAttribute(name);
    }
    
    /**
     * Get a set of all defined attribute names.
     * @return a set of names suitable for {@link #getAttribute}; may be empty but not null
     */
    public Set<String> getAttributeNames() {
        return impl.getAttributeNames();
    }
    
    /**
     * Get configured nested text.
     * It will be unevaluated as configured in the script.
     * If you wish to find the actual runtime value, you may
     * use {@link AntEvent#evaluate}.
     * @return the raw text contained in the element, or null
     */
    public String getText() {
        return impl.getText();
    }

    /**
     * Get any configured child elements.
     * @return a list of child structure elements; may be empty but not null
     */
    public TaskStructure[] getChildren() {
        return impl.getChildren();
    }
    
    @Override
    public String toString() {
        return impl.toString();
    }
    
}
