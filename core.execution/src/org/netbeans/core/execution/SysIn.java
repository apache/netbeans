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

package org.netbeans.core.execution;

import java.io.IOException;
import java.io.InputStream;

/** demutiplexes in-requests to task specific window
*
* @author Ales Novak
* @version 0.10 Dec 04, 1997
*/
final class SysIn extends InputStream {

    public SysIn() {
    }

    /** reads one char */
    public int read() throws IOException {
        return ExecutionEngine.getTaskIOs().getIn().read ();
    }

    /** reads an array of bytes */
    public int read(byte[] b, int off, final int len) throws IOException {
        char[] b2 = new char[len];
        int ret = ExecutionEngine.getTaskIOs().getIn().read(b2, 0, len);
        for (int i = 0; i < len; i++) {
            b[off + i] = (byte) b2[i];
        }
        return ret;
    }

    /** closes the stream */
    public void close() throws IOException {
        ExecutionEngine.getTaskIOs().getIn().close();
    }

    /** marks position at position <code>x</code> */
    public void mark(int x) {
        try {
            ExecutionEngine.getTaskIOs().getIn().mark(x);
        } catch (IOException e) {
            // [TODO]
        }
    }

    /** resets the stream */
    public void reset() throws IOException {
        ExecutionEngine.getTaskIOs().getIn().reset();
    }

    /**
    * @return true iff mark is supported false otherwise
    */
    public boolean markSupported() {
        return ExecutionEngine.getTaskIOs().getIn().markSupported();
    }

    /** skips <code>l</code> bytes
    * @return number of skipped bytes
    */
    public long skip(long l) throws IOException {
        return ExecutionEngine.getTaskIOs().getIn().skip(l);
    }
}
