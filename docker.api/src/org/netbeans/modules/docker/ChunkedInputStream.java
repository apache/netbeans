/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Petr Hejl
 */
// @NotThreadSafe
public class ChunkedInputStream extends FilterInputStream {

    private boolean started;

    private boolean finished;

    private int remaining;

    public ChunkedInputStream(InputStream is) {
        super(is);
    }

    @Override
    public int read() throws IOException {
        int current = fetchData();
        if (current < 0) {
            return -1;
        }
        remaining--;
        return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int current = fetchData();
        if (current < 0) {
            return -1;
        }

        int count = 0;
        int limit = off + Math.min(len, remaining);
        for (int i = off; i < limit; i++) {
            int value = in.read();
            if (value < 0) {
                return count;
            }
            count++;
            b[i] = (byte) value;
        }
        remaining -= count;
        return count;
    }

    @Override
    public int available() throws IOException {
        // FIXME this is not really true as theoretically it might block anyway
        return remaining;
    }

    private int fetchData() throws IOException {
        if (finished) {
            return -1;
        }
        if (remaining == 0) {
            if (started) {
                // read end of previous chunk
                String line = HttpUtils.readResponseLine(in);
                if (!line.isEmpty()) {
                    throw new IOException("Chunk content has additional data: " + line);
                }
            } else {
                started = true;
            }
            String line = HttpUtils.readResponseLine(in);
            if (line == null) {
                finished = true;
                return -1;
            }
            int semicolon = line.indexOf(';');
            if (semicolon > 0) {
                line = line.substring(0, semicolon);
            }
            try {
                remaining = Integer.parseInt(line, 16);
                if (remaining == 0) {
                    // end of chunk stream
                    line = HttpUtils.readResponseLine(in);
                    if (!line.isEmpty()) {
                        throw new IOException("End of chunk stream contains additional data: " + line);
                    }
                    finished = true;
                    return -1;
                }
            } catch (NumberFormatException ex) {
                throw new IOException("Wrong chunk size");
            }
        }
        return remaining;
    }
}
