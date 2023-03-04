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

package org.netbeans.modules.maven.api.output;



/**
 * A specialized output parser that's able to annotate and process the maven output for given
 * goal sequences. Like parse compilation output etc.
 * @author  Milos Kleint
 */
public interface OutputProcessor {
    
    
    /**
     * specify the segments that this processor is interested in.
     * there are 2 kinds of sequences project-execute and mojo-execute.
     *The format for the registration is as follows: <type>#<goal>
     * Eg mojo-execute#compile:compile
     */
    String[] getRegisteredOutputSequences();
    
    /**
     * for each line of output, the registered processor will get a chance to
     * add highlighting etc.
     */
    void processLine(String line, OutputVisitor visitor);
    
    /**
     * callback when one of the registered sequences starts
     */
    void sequenceStart(String sequenceId, OutputVisitor visitor);
    
    /**
     * callback when one of the registered sequences ends
     */
    void sequenceEnd(String sequenceId, OutputVisitor visitor);

    /**
     * callback when one of the registered sequences ends
     */
    void sequenceFail(String sequenceId, OutputVisitor visitor);
    
}
