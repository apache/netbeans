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

import java.io.File;

/**
 * Factory for model impls.
 * @author Jesse Glick
 */
public class Phadhails {

    private Phadhails() {}

    public static Phadhail synchronous(File f) {
        return SynchPhadhail.create(f);
    }

    public static Phadhail monitored(File f) {
        return MonitoredPhadhail.create(f);
    }

    public static Phadhail locked(File f) {
        return LockedPhadhail.create(f);
    }

    public static Phadhail eventHybridLocked(File f) {
        return EventHybridLockedPhadhail.create(f);
    }
    
    public static Phadhail spun(File f) {
        return SpunPhadhail.forPhadhail(locked(f));
    }
    
    public static Phadhail swung(File f) {
        return SwungPhadhail.forPhadhail(locked(f));
    }
    
}
