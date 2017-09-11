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

package org.netbeans.editor.ext;

import java.io.IOException;

/**
 *   DataAccessor for Code Completion DB files
 *
 *   @author  Martin Roskanin
 */
public interface DataAccessor {

    /** Opens DataAccessor file resource
     *  @param requestWrite if true, file is opened for read/write operation.
     */
    public void open(boolean requestWrite) throws IOException;

    /** Closes DataAccessor file resource */
    public void close() throws IOException;

    /** Reads up to len bytes of data from this file resource into an array of bytes.
     * @param buffer the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     */
    public void read(byte buffer[], int off, int len) throws IOException;
    
    /** Appends exactly <code>len</code> bytes, starting at <code>off</code> of the buffer pointer
     *  to the end of file resource.
     *  @param  buffer the buffer from which the data is appended.
     *  @param  off    the start offset of the data in the buffer.
     *  @param  len    the number of bytes to append.
     *  @return        the actual file offset.
     */
    public void append(byte buffer[], int off, int len) throws IOException;
    
    /**
     * Returns the current offset in this file.
     *
     * @return     the offset from the beginning of the file, in bytes,
     *             at which the next read or write occurs.
     */
    public long getFilePointer() throws IOException;
    
    /** Clears the file and sets the offset to 0 */
    public void resetFile() throws IOException;
    
    /**
     * Sets the file-pointer offset, measured from the beginning of this
     * file, at which the next read or write occurs.
     */
    public void seek(long pos) throws IOException;
    
    public int getFileLength();
}

