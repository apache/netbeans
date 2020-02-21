/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;

/**
 *
 */
public class StabIndexStrSection extends ElfSection {
    private byte[] stringtable = null;

    public StabIndexStrSection(ElfReader reader, int sectionIdx) throws IOException {
        super(reader, sectionIdx);
        read();
    }

    @Override
    public final StabIndexStrSection read() throws IOException {
        long filePos = reader.getFilePointer();
        reader.seek(header.getSectionOffset());
        stringtable = new byte[(int)header.getSectionSize()];
        reader.read(stringtable);
        reader.seek(filePos);
        return this;
    }

    public byte[] getStringTable() {
        return stringtable;
    }

    public List<String> getStrings() {
        List<String> res = new ArrayList<String>();
        if (stringtable == null) {
            return res;
        }
        int offset = 0;
        while (offset < stringtable.length) {
            String string = getString(offset);
            res.add(string);
            offset += string.length() + 1;
        }
        return res;
    }

    public String getString(long offset) {
        StringBuilder str = new StringBuilder();

        for (int i = (int)offset; i < stringtable.length; i++) {
            if (stringtable[i] == 0) {
                break;
            }
            str.append((char)stringtable[i]);
        }

        return str.toString();
    }

    @Override
    public void dump(PrintStream out) {
        super.dump(out);

        if (stringtable == null) {
            out.println("<Empty table>"); // NOI18N
            return;
        }

        int offset = 0;
        int idx = 0;

        out.printf("No.\tOffset\tString%n"); // NOI18N

        while (offset < stringtable.length) {
            String string = getString(offset);
            out.printf("%d.\t%d\t%s%n", ++idx, offset, string); // NOI18N
            offset += string.length() + 1;
        }
    }

    @Override
    public String toString() {
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(st, false, "UTF-8"); // NOI18N
            dump(out);
            return st.toString("UTF-8"); //NOI18N
        } catch (IOException ex) {
            return ""; // NOI18N
        }
    }
}
