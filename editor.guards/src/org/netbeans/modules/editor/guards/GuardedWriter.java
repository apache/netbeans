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

package org.netbeans.modules.editor.guards;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;

/** This stream is able to insert special guarded comments.
*/
final class GuardedWriter extends Writer {
    /** Encapsulated writer. */
    private Writer writer;

    private CharArrayWriter buffer;

    private final AbstractGuardedSectionsProvider gw;

    private boolean isClosed = false;

    private final List<GuardedSection> sections;

    
    /** Creates new GuardedWriter.
    * @param os Encapsulated output stream.
    * @param list The list of the guarded sections.
    */
    public GuardedWriter(AbstractGuardedSectionsProvider gw, OutputStream os, List<GuardedSection> list, Charset encoding) {
        if (encoding == null)
            writer = new OutputStreamWriter(os);
        else
            writer = new OutputStreamWriter(os, encoding);
        this.gw = gw;
        sections = list;
    }

    /** Writes chars to underlying writer */
    public void write(char[] cbuf, int off, int len) throws IOException {
        
        if (buffer == null) {
            buffer = new CharArrayWriter(10240);
        }
        
        buffer.write(cbuf, off, len);
    }

    /** Calls underlying writer flush */
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        isClosed = true;
        if (buffer != null) {
            char[] content = this.gw.writeSections(sections, buffer.toCharArray());
            writer.write(content);
        }
        writer.close();
    }

    /** Calls underlying writer flush */
    public void flush() throws IOException {
    }
    
}
