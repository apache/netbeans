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
package org.netbeans.modules.jshell.model;

import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 * Mutation event from console model. Informs that a section in console has been
 * created or updated.
 * <p/>
 * @author sdedic
 */
public final class ConsoleEvent extends EventObject {
    private final ConsoleSection theSection;
    private final List<ConsoleSection> affectedSections;
    private volatile Boolean input;
    private boolean start;
    
    public ConsoleEvent(ConsoleModel source, ConsoleSection section, boolean startStop) {
        this(source, section);
        this.start = startStop;
    }
    
    public ConsoleEvent(ConsoleModel source, ConsoleSection section) {
        super(source);
        this.theSection = section;
        this.affectedSections = null;
    }
    
    public ConsoleEvent(ConsoleModel source,List<ConsoleSection> sections) {
        super(source);
        this.affectedSections = sections;
        this.theSection = sections.isEmpty() ? null : sections.get(0);
    }
    
    public ConsoleSection getSection() {
        return theSection;
    }
    
    public List<ConsoleSection> getAffectedSections() {
        return affectedSections == null ?
                Collections.singletonList(theSection) : affectedSections;
    }
    
    public ConsoleModel getSource() {
        return (ConsoleModel)super.getSource();
    }
    
    public boolean containsInput() {
        if (input != null) {
            return input;
        }
        for (ConsoleSection s : getAffectedSections()) {
            if (s.getType().input) {
                return input = true;
            }
        }
        return input = false;
    }

    /**
     * Valid for executing event callback. True, if the execution has been started,
     * false indicates the execution finished.
     * @return 
     */
    public boolean isStart() {
        return start;
    }
}
