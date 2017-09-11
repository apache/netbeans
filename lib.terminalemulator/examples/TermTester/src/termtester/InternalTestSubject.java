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
