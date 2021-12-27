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
package org.netbeans.modules.cpplite.project.runner;

import java.util.List;
import org.netbeans.modules.cpplite.project.Utils;

/**
 *
 * @author lahvac
 */
public class Runner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        for (List<String> command : Utils.decode(args[0])) {
            int result = new ProcessBuilder(command).inheritIO().start().waitFor();
            if (result != 0) {
                System.exit(result);
            }
        }
    }
}
