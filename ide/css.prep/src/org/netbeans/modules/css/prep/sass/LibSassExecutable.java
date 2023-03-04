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
package org.netbeans.modules.css.prep.sass;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing <tt>libsass</tt> command line tool (any of its frontend).
 * <p>
 * Currently, enabled via a system property only, adding no additional command line parameters.
 */
class LibSassExecutable extends SassCli {

    static final String[] EXECUTABLE_NAMES = new String[] {
        "sassc", // NOI18N
        "node-sass", // NOI18N
    };

    LibSassExecutable(String sassPath) {
        super(sassPath);
    }

    @Override
    protected List<String> getParameters(File inputFile, File outputFile, List<String> compilerOptions) {
        List<String> params = new ArrayList<>();
        // compiler options
        params.addAll(compilerOptions);
        // input
        params.add(inputFile.getAbsolutePath());
        // output
        params.add(outputFile.getAbsolutePath());
        return params;
    }

}
