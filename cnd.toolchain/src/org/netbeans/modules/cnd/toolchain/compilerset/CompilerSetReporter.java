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

package org.netbeans.modules.cnd.toolchain.compilerset;

import java.io.IOException;
import java.io.Writer;
import org.openide.util.NbBundle;

/**
 * Sometimes we need to make process of setting up compiler set verbose.
 * This class allows to plug in a writer to report the process of
 * compiler set setup to
 */
public class CompilerSetReporter {

    private static Writer writer;

    private CompilerSetReporter() {
    }

    /**
     * Sets a writer to report the process of compiler set setup to
     * @param writer if null, no reporting occurs
     */
    public static synchronized void setWriter(Writer writer) {
        CompilerSetReporter.writer = writer;
    }

    /* package-local */
    public static void report(String msgKey) {
        report(msgKey, true);
    }

    /* package-local */
    public static synchronized void report(String msgKey, boolean addLineFeed, Object... params) {
        if (writer != null) {
            try {
                writer.write(NbBundle.getMessage(CompilerSetReporter.class, msgKey, params));
                if (addLineFeed) {
                    writer.write('\n');
                }
                writer.flush();
            } catch (IOException ex) {
            }
        }
    }

    /* package-local */
    public static boolean canReport() {
        return writer != null;
    }

}
