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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.extexecution.base.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputReader;
import org.netbeans.api.extexecution.base.input.InputReaders;

/**
 * This class is <i>NotThreadSafe</i>.
 *
 * @author Petr Hejl
 */
public class FileInputReader implements InputReader {

    private static final Logger LOGGER = Logger.getLogger(FileInputReader.class.getName());

    private static final int BUFFER_SIZE = 512;

    private final InputReaders.FileInput.Provider fileProvider;

    private final char[] buffer = new char[BUFFER_SIZE];

    private InputReaders.FileInput currentFile;

    private Reader reader;

    private long fileLength;

    private boolean closed;

    public FileInputReader(InputReaders.FileInput.Provider fileProvider) {
        assert fileProvider != null;

        this.fileProvider = fileProvider;
    }

    @Override
    public int readInput(InputProcessor inputProcessor) {
        if (closed) {
            throw new IllegalStateException("Already closed reader");
        }

        int fetched = 0;
        try {
            InputReaders.FileInput file = fileProvider.getFileInput();

            if ((currentFile != file && (currentFile == null || !currentFile.equals(file)))
                    || fileLength > currentFile.getFile().length() || reader == null) {

                if (reader != null) {
                    reader.close();
                }

                currentFile = file;

                if (currentFile != null && currentFile.getFile().exists()
                        && currentFile.getFile().canRead()) {

                    reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(currentFile.getFile()), currentFile.getCharset()));
                }
                if (fileLength > 0) {
                    inputProcessor.reset();
                }
                fileLength = 0;
            }

            if (reader == null) {
                return fetched;
            }

            int size = reader.read(buffer);
            if (size > 0) {
                fileLength += size;
                fetched += size;

                if (inputProcessor != null) {
                    char[] toProcess = new char[size];
                    System.arraycopy(buffer, 0, toProcess, 0, size);
                    inputProcessor.processInput(toProcess);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            // we will try the next loop (if any)
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException iex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        }

        return fetched;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

}
