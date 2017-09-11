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
package org.openide.filesystems;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.openide.util.Exceptions;

/** Iterator for list of lines.
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class FileObjectLineIterator implements ListIterator<String> {
    private final String encoding;
    private final FileObjectLines lines;
    private byte[] buffer;
    private BufferedReader reader;
    private String line;
    private int index;

    public FileObjectLineIterator(FileObjectLines lines, String encoding) throws IOException {
        this.lines = lines;
        this.encoding = encoding;
        initReader();
    }

    private FileObjectLineIterator(FileObjectLineIterator orig) {
        this.lines = orig.lines;
        this.encoding = orig.encoding;
        this.buffer = orig.buffer;
        try {
            initReader();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public synchronized boolean hasNext() {
        if (line == null) {
            try {
                line = reader.readLine();
                if (line == null) {
                    reader.close();
                }
                index++;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return line != null;
    }

    public synchronized String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        String l = line;
        line = null;
        return l;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public synchronized boolean hasPrevious() {
        return index > 0;
    }

    public synchronized String previous() {
        if (index == 0) {
            throw new NoSuchElementException();
        }
        try {
            int pos = index - 1;
            initReader();
            String last = null;
            while (index <= pos) {
                last = next();
            }
            index--;
            return last;
        } catch (IOException ex) {
            throw (NoSuchElementException)new NoSuchElementException().initCause(ex);
        }
    }

    public synchronized int nextIndex() {
        return index;
    }

    public synchronized int previousIndex() {
        return index - 1;
    }

    public void set(String e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void add(String e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initReader() throws UnsupportedEncodingException, IOException {
        InputStream is;
        if (lines.fo.getSize() < 64 * 1024) {
            if (buffer == null) {
                buffer = lines.fo.asBytes();
            }
            is = new ByteArrayInputStream(buffer);
        } else {
            is = lines.fo.getInputStream();
        }
        this.reader = new BufferedReader(new InputStreamReader(is, encoding));
        this.index = 0;
        while (this.index < index) {
            next();
        }
    }

    final FileObjectLineIterator cloneIterator() {
        return new FileObjectLineIterator(this);
    }
}
