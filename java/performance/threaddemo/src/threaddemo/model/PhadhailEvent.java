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

import java.util.EventObject;

/**
 * Event object for phadhail events (suitable as is for children changes).
 * @author Jesse Glick
 */
public class PhadhailEvent extends EventObject {

    /** factory */
    public static PhadhailEvent create(Phadhail ph) {
        return new PhadhailEvent(ph);
    }

    /** cannot be subclassed outside package */
    PhadhailEvent(Phadhail ph) {
        super(ph);
    }

    public Phadhail getPhadhail() {
        return (Phadhail)getSource();
    }

    public String toString() {
        return "PhadhailEvent[" + getPhadhail() + "]";
    }
    
}
