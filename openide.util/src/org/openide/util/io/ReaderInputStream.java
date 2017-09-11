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
package org.openide.util.io;

import java.io.*;


/**
* This class convert Reader to InputStream. It works by converting
* the characters to the encoding specified in constructor parameter.
*
* @author   Petr Hamernik, David Strupl
*/
public class ReaderInputStream extends InputStream {
    /** Input Reader class. */
    private Reader reader;
    private PipedOutputStream pos;
    private PipedInputStream pis;
    private OutputStreamWriter osw;

    /** Creates new input stream from the given reader.
     * Uses the platform default encoding.
    * @param reader Input reader
    */
    public ReaderInputStream(Reader reader) throws IOException {
        this.reader = reader;
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        osw = new OutputStreamWriter(pos);
    }

    /** Creates new input stream from the given reader and encoding.
     * @param reader Input reader
     * @param encoding
     */
    public ReaderInputStream(Reader reader, String encoding)
    throws IOException {
        this.reader = reader;
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        osw = new OutputStreamWriter(pos, encoding);
    }

    public int read() throws IOException {
        if (pis.available() > 0) {
            return pis.read();
        }

        int c = reader.read();

        if (c == -1) {
            return c;
        }

        osw.write(c);
        osw.flush();
        pos.flush();

        if (pis.available() > 0) {
            return pis.read();
        } else {
            throw new IOException("Cannot encode input data using " + osw.getEncoding() + " encoding.");  // NOI18N
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }

        int c = read();

        if (c == -1) {
            return -1;
        }

        b[off] = (byte) c;

        int i = 1;

        // Don't try to fill up the buffer if the reader is waiting.
        for (; (i < len) && reader.ready(); i++) {
            c = read();

            if (c == -1) {
                return i;
            }

            b[off + i] = (byte) c;
        }

        return i;
    }

    @Override
    public int available() throws IOException {
        int i = pis.available();

        if (i > 0) {
            return i;
        }

        if (reader.ready()) {
            // Char must produce at least one byte.
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
        osw.close();
        pis.close();
    }
}
