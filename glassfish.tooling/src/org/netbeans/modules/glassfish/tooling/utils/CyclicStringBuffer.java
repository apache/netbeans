/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.utils;

import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Cyclic <code>String</code> buffer.
 * <p/>
 * Stores up to <code>&lt;size&gt;</code> characters in cyclic buffer and allows
 * to simply append new characters to the end or prepend new characters
 * to the beginning of the buffer without necessity to move buffer content.
 * <p/>
 * @author Tomas Kraus
 */
public class CyclicStringBuffer {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CyclicStringBuffer.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Buffer size. */
    private int size;

    /** Buffer content. */
    private char[] buff;

    /** Count of valid characters in the buffer. */
    private int len;

    /** Current beginning of valid characters storage. */
    private int beg;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of cyclic <code>String</code> buffer.
     * <p/>
     * @param size Cyclic <code>String</code> buffer size.
     */
    public CyclicStringBuffer(final int size) {
        this.size = size;
        this.buff = new char[size];
        len = 0;
        beg = 0;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Resize cyclic <code>String</code> buffer.
     * <p/>
     * Content of buffer will be removed.
     * <p/>
     * @param size New cyclic <code>String</code> buffer size.
     */
    public void resize(final int size) {
        if (this.size != size) {
            this.size = size;
            this.buff = new char[size];
        }
        len = 0;
        beg = 0;
    }

    /**
     * Appends character at the end of the buffer.
     * <p/>
     * First character in the buffer will be discarded when buffer is full.
     * <p/>
     * @param c Character to be appended.
     * @return  Value of <code>true</code> when buffer was full and first
     *          character in the buffer got overwritten or <code>false</code>
     *          otherwise.
     */
    public boolean append(final char c) {
        buff[(beg + len) % size] = c;
        if (len == size) {
            beg = (beg + 1) % size;
            return true;
        } else {
            len += 1;
            return false;
        }
    }

    /**
     * Appends character at the beginning of the buffer.
     * <p/>
     * Last character in the buffer will be discarded when buffer is full.
     * <p/>
     * @param c Character to be prepended.
     * @return  Value of <code>true</code> when buffer was full and last
     *          character in the buffer got overwritten or <code>false</code>
     *          otherwise.
     */
    public boolean prepend(final char c) {
        beg = (beg + size - 1) % size;
        buff[beg] = c;
        if (len == size) {
            return true;
        } else {
            len += 1;
            return false;
        }
    }

    /**
     * Compares buffer content to the specific {@link String}.
     * <p/>
     * Empty {@link String} value and <code>null</code> value are considered
     * as equivalent and are equal to zero length buffer content.
     * <p/>
     * @return Value of <code>true</code> if buffer content represents
     *         {@link String} equivalent to this provided string
     *         or <code>false</code> otherwise.
     */
    public boolean equals(final String s) {
        // Handle null value.
        if (s == null) {
            return len == 0;
        }
        // Cut evaluation for differend lengths.
        if (len != s.length()) {
            return false;
        }
        boolean result = true;
        for (int i = 0; i < len; i++) {
            if (buff[(beg + i) % size] != s.charAt(i)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Returns {@link String} stored in buffer.
     * <p/>
     * Zero length content is returned as zero length {@link String};
     * <p/>
     * @return {@link String} stored in buffer.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(buff[(beg + i) % size]);
        }
        return sb.toString();
    }

}
