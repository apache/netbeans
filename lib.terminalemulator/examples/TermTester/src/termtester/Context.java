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

import interp.Interp;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.terminalemulator.Term;

public final class Context {

    public final static class Margin {
        public final int low;
        public final int hi;

        public Margin(int low, int hi) {
            this.low = low;
            this.hi = hi;
        }
    }
    public final Interp interp;

    private Term term;
    private Margin margin;

    private final List<TestSubject> testSubjects =
        new LinkedList<TestSubject>();

    private static int width = 80;
    private static int height = 24;

    // SHOULD have a command to toggle this
    private static final boolean quiet = true;

    private final Semaphore nextSema = new Semaphore(0);

    public Context(Interp interp) {
        this.interp = interp;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public void addTestSubject(TestSubject ts) {
        testSubjects.add(ts);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void finish() {
        for (TestSubject ts : testSubjects)
            ts.finish();
    }

    public void sendRaw(String fmt, Object... args) {
        for (TestSubject ts : testSubjects) {
            ts.pw().printf(fmt, args);
            ts.pw().flush();
        }
    }

    public char receive() {
        return TestSubject.receive();
    }

    public void next() {
        nextSema.release();
    }

    public void pause() {
        interp.printf("Next?\n");
        try {
            nextSema.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Context.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendEscape(String s) {
        StringBuilder buf = new StringBuilder();

        for (int sx = 0; sx < s.length(); sx++) {
            char c = s.charAt(sx);
            switch(c) {
                case '\\':
                    char c1 = s.charAt(sx+1);
                    if (c1 == '\\') {
                        buf.append('\\');
                        sx+=1;
                    } else if (Character.isDigit(c1)) {
                        String hexString = s.substring(sx+1, 2+1);
                        c = (char) Integer.parseInt(hexString, 16);
                        buf.append(c);
                        sx+=2;
                    } else {
                        int len = 2;
                        int ec = Util.mnemonicToChar(s.substring(sx+1, sx+len+1));
                        if (ec == -1) {
                            len = 3;
                            ec = Util.mnemonicToChar(s.substring(sx+1, sx+len+1));
                        }
                        if (ec == -1)
                            interp.error("Unregonized mnemonic after \\");
                        if (ec == -2)
                            interp.error("Use \\0e for SO and \\01 for SOH");
                        buf.append((char) ec);
                        sx+=len;
                    }
                    break;
               default:
                   buf.append(c);
                   break;

            }
        }

        sendRaw("%s", buf);
    }

    private static String removeNewlines(String s) {
        final StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public void sendQuiet(String fmt, Object... args) {
        String s = String.format(fmt, args);
        sendEscape(s);
        if (!quiet)
            interp.printf("Sent: '%s'\n", removeNewlines(s));
    }

    public void send(String fmt, Object... args) {
        String s = String.format(fmt, args);
        sendEscape(s);
        interp.printf("Sent: '%s'\n", removeNewlines(s));
    }

    public Margin getMargin() {
        return margin;
    }

    public void setMargin(Margin margin) {
        this.margin = margin;
        // sendQuiet("\\ESC[%d;%dr", margin.low, margin.hi);
    }

    public void sendMargin() {
        if (margin != null)
            send("\\ESC[%d;%dr", margin.low, margin.hi);
    }
}
