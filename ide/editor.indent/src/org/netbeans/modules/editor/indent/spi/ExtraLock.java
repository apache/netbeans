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

package org.netbeans.modules.editor.indent.spi;

/**
 * Extra locking may be necessary for indentation/reformatting
 * before the document gets write-locked and the actual
 * indentation/reformatting gets started.
 * <br/>
 * For example java infrastructure requires this.
 * <br/>
 * The infrastructure guarantees this processing:
 * <pre>
 *   extraLock.lock();
 *   try {
 *       doc.atomicLock(); // either BaseDocument or e.g. NbDocument.runAtomic()
 *       try {
 *           // indent or reformat ...
 *       } finally {
 *           doc.atomicUnlock();
 *       }
 *   } finally {
 *       extraLock.unlock();
 *   }
 * </pre>
 *
 * @author Miloslav Metelka
 */

public interface ExtraLock {

    void lock();
        
    void unlock();
    
}
