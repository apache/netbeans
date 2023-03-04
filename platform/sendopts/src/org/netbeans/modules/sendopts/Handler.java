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
package org.netbeans.modules.sendopts;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.CLIHandler;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.modules.sendopts.*;
import org.openide.util.Lookup;

/**
 * A CLI handler to delegate to CommandLine.getDefault().
 * @author Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.CLIHandler.class, position=65534)
public class Handler extends CLIHandler {
    /**
     * Create a handler. Called by core.
     */
    public Handler() {
        super(WHEN_EXTRA);
    }

    protected int cli(CLIHandler.Args args) {
        return HandlerImpl.execute(
            args.getArguments(),
            args.getInputStream(),
            args.getOutputStream(),
            args.getErrorStream(),
            args.getCurrentDirectory()
        );
    }

    protected void usage(PrintWriter w) {
        HandlerImpl.usage(w);
    }
}
