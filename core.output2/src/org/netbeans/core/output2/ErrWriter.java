/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.output2;

import java.util.logging.Logger;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import java.io.IOException;

/**
 * Wrapper OutputWriter for the standard out which marks its lines as being
 * stderr.
 *
 * @author  Tim Boudreau
 */
class ErrWriter extends OutputWriter {
    private OutWriter wrapped;
    private final NbWriter parent;
    /** Creates a new instance of ErrWriter */
    ErrWriter(OutWriter wrapped, NbWriter parent) {
        super (new OutWriter.DummyWriter());
        this.wrapped = wrapped;
        this.parent = parent;
    }

    synchronized void setWrapped (OutWriter wrapped) {
        this.wrapped = wrapped;
        closed = true;
    }

    public void println(String s, OutputListener l) throws java.io.IOException {
        println(s, l, false);
    }

    @Override
    public void println(String s, OutputListener l, boolean important) throws java.io.IOException {
        closed = false;
        wrapped.print(s, l, important, null, null, OutputKind.ERR, true);
    }

    public void reset() throws IOException {
        Logger.getAnonymousLogger().warning("Do not call reset() on the error io," +
        " only on the output IO.  Reset on the error io does nothing.");
        closed = false;
    }
    
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            parent.notifyErrClosed();
        }
    }

    boolean closed = true;
    boolean isClosed() {
        return closed;
    }

    @Override
    public void flush() {
        wrapped.flush();
    }
    
    @Override
    public boolean checkError() {
        return wrapped.checkError();
    }    
    
    @Override
    public void write(int c) {
        print(String.valueOf(c), false);
    }

    @Override
    public void write(char buf[], int off, int len) {
        print(new OutWriter.CharArrayWrapper(buf, off, len), false);
    }

    @Override
    public void write(String s, int off, int len) {
        print(s.substring(off, off + len), false);
    }

    @Override
    public void println(boolean x) {
        print(x ? "true" : "false", true);
    }

    @Override
    public void println(int i) {
        print(String.valueOf(i), true);
    }

    @Override
    public void println(char c) {
        print(String.valueOf(c), true);
    }

    @Override
    public void println(long l) {
        print(String.valueOf(l), true);
    }

    @Override
    public void println(float x) {
        print(String.valueOf(x), true);
    }

    @Override
    public void println(double x) {
        print(String.valueOf(x), true);
    }

    @Override
    public void println(char buf[]) {
        print(new OutWriter.CharArrayWrapper(buf), true);
    }

    @Override
    public void println(String s) {
        print(s, true);
    }

    @Override
    public void println(Object x) {
        print(String.valueOf(x), true);
    }

    @Override
    public void print(char[] buf) {
        print(new OutWriter.CharArrayWrapper(buf), false);
    }

    @Override
    public void print(Object obj) {
        print(String.valueOf(obj), false);
    }

    @Override
    public void print(char c) {
        print(String.valueOf(c), false);
    }

    @Override
    public void print(int i) {
        print(String.valueOf(i), false);
    }

    @Override
    public void print(String s) {
        print(s, false);
    }

    @Override
    public void print(boolean b) {
        print(b ? "true" : "false", false);
    }

    @Override
    public void println() {
        closed = false;
        wrapped.println();
    }

    private void print(CharSequence s, boolean addLineSep) {
        closed = false;
        wrapped.print(s, null, false, null, null, OutputKind.ERR, addLineSep);
    }
}
