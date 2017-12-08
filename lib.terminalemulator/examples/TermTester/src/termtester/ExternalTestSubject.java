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

package termtester;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.netbeans.lib.richexecution.Pty;
import org.netbeans.lib.richexecution.PtyException;
import org.netbeans.lib.richexecution.PtyExecutor;
import org.netbeans.lib.richexecution.PtyProcess;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.lib.terminalemulator.Term;

abstract class ExternalTestSubject extends TestSubject {

    private final Program program;
    private final Pty pty;
    private final PtyProcess process;
    private final PrintWriter pw;
    private final Thread keyShuttle;
    
    public ExternalTestSubject(Context context, String title) throws PtyException {
        super(title);

        pty = Pty.create(Pty.Mode.RAW);
        program = makeProgram(context, pty);
        PtyExecutor executor = new PtyExecutor();
        
        OutputStream os = pty.getOutputStream();

        final InputStream is = pty.getInputStream();

        keyShuttle = createShuttle(is);
        keyShuttle.start();

        process = executor.start(program, pty);

        pw = new PrintWriter(os);
    }

    public PrintWriter pw() {
        return pw;
    }

    public void finish() {
        process.destroy();
    }

    public Term term() {
        return null;
    }

}
