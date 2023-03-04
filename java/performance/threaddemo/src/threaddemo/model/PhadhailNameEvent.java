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

package threaddemo.model;

/**
 * Event object for phadhail name change events.
 * Old and new names may be null.
 * @author Jesse Glick
 */
public final class PhadhailNameEvent extends PhadhailEvent {

    /** factory */
    public static PhadhailNameEvent create(Phadhail ph, String oldName, String newName) {
        return new PhadhailNameEvent(ph, oldName, newName);
    }

    private final String oldName, newName;

    private PhadhailNameEvent(Phadhail ph, String oldName, String newName) {
        super(ph);
        this.oldName = oldName;
        this.newName = newName;
    }
    
    public String getOldName() {
        return oldName;
    }
    
    public String getNewName() {
        return newName;
    }
    
    public String toString() {
        return "PhadhailNameEvent[" + getPhadhail() + ":" + oldName + " -> " + newName + "]";
    }
    
}
