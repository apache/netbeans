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
package org.netbeans.modules.php.spi.testing.run;

import org.openide.windows.OutputWriter;

/**
 * Line handler to use for printing while running tests.
 * It can be used e.g. for creating a hyperlinks for files.
 */
public interface OutputLineHandler {

    /**
     * Print the given {@code text} to the given {@code out}.
     * @param out the output where to print
     * @param text the text (line) to print
     */
    void handleLine(OutputWriter out, String text);

}
