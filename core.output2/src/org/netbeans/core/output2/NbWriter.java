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

import org.openide.windows.OutputWriter;
import org.openide.windows.OutputListener;
import java.io.IOException;
import org.openide.util.Exceptions;


/**
 * Wrapper around a replacable instance of OutWriter.  An OutWriter can be disposed on any thread, but it may
 * still be visible in the GUI until the tab change gets handled in the EDT;  also, a writer once obtained
 * should be reusable, but OutWriter is useless once it has been disposed.  So this class wraps an OutWriter,
 * which it replaces when reset() is called;  an OutputDocument is implemented directly over an 
 * OutWriter, so the immutable OutWriter lasts until the OutputDocument is destroyed.
 */
class NbWriter extends OutputWriter {
    private final NbIO owner;
    /**
     * Make an output writer.
     */
    public NbWriter(OutWriter real, NbIO owner) {
        super(real);
        this.owner = owner;
    }

    public void println(String s, OutputListener l) throws IOException {
        ((OutWriter) out).println (s, l);
    }

    @Override
    public void println(String s, OutputListener l, boolean important) throws IOException {
        ((OutWriter) out).println (s, l, important);
    }

    /**
     * Replaces the wrapped OutWriter.
     *
     * @throws IOException
     */
    public synchronized void reset() throws IOException {
        if (!out().isDisposed() && out().isEmpty()) {
            return;
        }
        if (out != null) {
            if (Controller.LOG) Controller.log ("Disposing old OutWriter");
            out().dispose();
        }
        if (Controller.LOG) Controller.log ("NbWriter.reset() replacing old OutWriter");
        out = new OutWriter(owner);
        lock = out;
        if (err != null) {
            err.setWrapped((OutWriter) out);
        }
        owner.reset();
    }

    OutWriter out() {
        return (OutWriter) out;
    }
    
    ErrWriter err() {
        return err;
    }

    private ErrWriter err = null;
    public synchronized ErrWriter getErr() {
        if (err == null) {
            err = new ErrWriter ((OutWriter) out, this);
        }
        return err;
    }

    @Override
    public void close() {
        boolean wasClosed = isClosed();
        if (Controller.LOG) Controller.log ("NbWriter.close wasClosed=" + wasClosed + " out is " + out + " out is closed " + ((OutWriter) out).isClosed());
        if (!wasClosed || !((OutWriter) out).isClosed()) {
            synchronized (lock) {
                try {
                    if (Controller.LOG) Controller.log ( "Now closing OutWriter");
                    out.close();
                    if (err != null) {
                        err.close();
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        boolean isClosed = isClosed();
        if (wasClosed != isClosed) {
            if (Controller.LOG) Controller.log ("Setting streamClosed on InputOutput to " + isClosed);
        }
        owner.setStreamClosed(isClosed);
    }

    public boolean isClosed() {
        OutWriter ow = (OutWriter) out;
        synchronized (ow) {
            boolean result = ow.isClosed();
            if (result && err != null && !(ow.checkError())) {
                result &= err.isClosed();
            }
            return result;
        }
    }

    public void notifyErrClosed() {
        if (isClosed()) {
            if (Controller.LOG) Controller.log ("NbWriter.notifyErrClosed - error stream has been closed");
            owner.setStreamClosed(isClosed());
        }
    }
    
    /**
     * If not overridden, the super impl will append extra \n's
     */
    @Override
    public void println (String s) {
        synchronized (lock) {
            ((OutWriter) out).println(s);
        }
    }
}
