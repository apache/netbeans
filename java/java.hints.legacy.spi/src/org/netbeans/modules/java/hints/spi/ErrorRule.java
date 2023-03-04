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

package org.netbeans.modules.java.hints.spi;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.Fix;

/** 
 * Represents a rule to be run on the java source in case the compiler 
 * issued an error or a warning.
 *
 * @author Petr Hrebejk, Jan Lahoda
 */
public interface ErrorRule<T> extends Rule {//XXX: should ErrorRule extend Rule?

    /** Get the diagnostic codes this rule should run on
     */
    public Set<String> getCodes();

    /** Return possible fixes for a given diagnostic report.
     */
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<T> data);

    
    public static final class Data<T> {
        private T o;
        public synchronized T getData() {
            return o;
        }
        
        public synchronized void setData(T o) {
            this.o = o;
        }
    }
}
