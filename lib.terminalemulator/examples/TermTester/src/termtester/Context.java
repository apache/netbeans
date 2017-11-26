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
