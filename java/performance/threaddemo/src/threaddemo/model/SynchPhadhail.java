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
import threaddemo.locking.RWLock;
import threaddemo.locking.Locks;

/**
 * Simple synchronous phadhail implementation that can only be used from the
 * event thread, like a Swing model.
 * (However as in Swing, listeners may be added or removed from any thread.)
 * @author Jesse Glick
 */
public class SynchPhadhail extends AbstractPhadhail {

    private static final Factory FACTORY = new Factory() {
        public AbstractPhadhail create(File f) {
            return new SynchPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private SynchPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public RWLock lock() {
        return Locks.event();
    }
    
}
