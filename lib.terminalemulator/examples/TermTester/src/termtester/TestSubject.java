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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.richexecution.Pty;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.lib.terminalemulator.Term;

public abstract class TestSubject {

    private final String title;

    private static final BlockingQueue<Character> input = new LinkedBlockingQueue<Character>();

    public abstract PrintWriter pw();
    public abstract void finish();

    public abstract Term term();

    protected abstract Program makeProgram(Context context, Pty pty);

    protected TestSubject(String title) {
        this.title = title;
    }

    protected final String title() {
        return title;
    }

    static public char receive() {
        try {
            return input.take();
        } catch (InterruptedException ex) {
            Logger.getLogger(TestSubject.class.getName()).log(Level.SEVERE, null, ex);
        }
        return '\n';
    }

    static public void put(char c) {
        try {
            input.put(c);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestSubject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected final Thread createShuttle(final InputStream is) {
        return new Thread() {
            @Override
            public void run() {
                while (true) {
                    char c;
                    try {
                        c = (char) is.read();
                    } catch (IOException ex) {
                        Logger.getLogger(InternalTestSubject.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                    TestSubject.put(c);
                }
            }
        };
    }

    protected final Thread createShuttle(final Reader is) {
        return new Thread() {
            @Override
            public void run() {
                while (true) {
                    char c;
                    try {
                        c = (char) is.read();
                    } catch (IOException ex) {
                        Logger.getLogger(InternalTestSubject.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                    TestSubject.put(c);
                }
            }
        };
    }
}
