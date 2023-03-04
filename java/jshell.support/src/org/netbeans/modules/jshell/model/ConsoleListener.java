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

import java.util.List;

/**
 * Informs that a console section has been changed
 * @author sdedic
 */
public interface ConsoleListener {
    /**
     * A section in a console has been defined.
     * @param section the section
     */
    public void sectionCreated(ConsoleEvent e);
    
    /**
     * A section in the console has been updated or
     * redefined.
     * @param section 
     */
    public void sectionUpdated(ConsoleEvent e);
    
    /**
     * The execution has started, or has been ended.
     * @param e 
     */
    public void executing(ConsoleEvent e);
    
    /**
     * Called when the active JShell is closed.
     * @param e 
     */
    public void closed(ConsoleEvent e);
}
