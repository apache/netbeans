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
