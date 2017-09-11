/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
