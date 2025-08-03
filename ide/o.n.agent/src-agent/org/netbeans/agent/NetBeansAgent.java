/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a java agent. The JAR containing this class must be
 * referenced by the JVM with the agent infrastructure. At time of writing this
 * means, that the JAR must be passed via the `-javaagent:JARPATH` construct.
 * JARPATH in this case must be the absolute path to the netbeans-javaagent.jar.
 *
 * The `premain` method in this class is then invoked by the JVM _before_ the
 * applications main method is invoked.
 *
 * This should be used as a last resort. For classes loaded throught the
 * module system that faciliy should be prefered.
 */
public class NetBeansAgent {

    public static void premain(String arg, Instrumentation instrumentation) {
        List<ClassFileTransformer> transformer = new ArrayList<>(2);
        if ((!Boolean.getBoolean(WClipboardTransformer.DEBUG_DISABLE_TRANSFORMER))
                && System.getProperty("os.name").toLowerCase().contains("windows")) {
            transformer.add(new WClipboardTransformer(instrumentation));
        }
        transformer.forEach(cft -> instrumentation.addTransformer(cft, false));
    }

}
