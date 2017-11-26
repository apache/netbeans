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

import java.awt.Font;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.netbeans.lib.richexecution.PtyException;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.Term;

abstract class InternalTestSubject extends TestSubject {

    private final PrintWriter pw;
    private final StreamTerm term;
    private final JFrame frame;
    private final Thread keyShuttle;

    public InternalTestSubject(Context context, String title, int xoff, int yoff) throws PtyException {
        super(title);

        frame = new JFrame();
        frame.setTitle(title());
        frame.setLocation(xoff, yoff);

        term = new StreamTerm();
        term.setHistorySize(1000);
        term.setRowsColumns(context.height(), context.width());
        term.setHorizontallyScrollable(false);
        term.setEmulation("xterm");
        term.setFont(new Font("Monospaced", Font.PLAIN, 10));

        term.setDebugFlags(Term.DEBUG_KEYS);
        
        frame.add(term);
        frame.pack();
        frame.setVisible(true);

        final Reader is = term.getIn();
        Writer os = term.getOut();

        keyShuttle = createShuttle(is);
        keyShuttle.start();

        context.setTerm(term);

        pw = new PrintWriter(os);
    }

    public StreamTerm term() {
        return term();
    }

    public PrintWriter pw() {
        return pw;
    }

    public void finish() {
        keyShuttle.interrupt();
        try {
            keyShuttle.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(InternalTestSubject.class.getName()).log(Level.SEVERE, null, ex);
        }
        frame.dispose();
    }
}
