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


package org.netbeans.modules.cnd.asm.model.xml;

import java.util.*;
import org.netbeans.modules.cnd.asm.model.lang.instruction.InstructionArgs;
import org.netbeans.modules.cnd.asm.model.lang.operand.Operand;

public class DefaultXMLBaseInstruction extends XMLBaseInstruction {
    public static final Collection<Integer> DEFAULT_READ = Collections.singletonList(2);
    public static final Collection<Integer> DEFAULT_WRITE = Collections.singletonList(1);

    //TODO: There are some instructions with fixed read,write registers, not in arguments
    private Collection<Integer> readArgsIdx = DEFAULT_READ;
    private Collection<Integer> writeArgsIdx = DEFAULT_WRITE;

    public DefaultXMLBaseInstruction(String name, String groupName, String desc,
                                    Collection<InstructionArgs> args) {
        super(name, desc, groupName, args);
    }

    public boolean isSupportive(Operand[] ops) {
        return false;
    }

    public void setWrite(String write) {
        this.writeArgsIdx = parseArgs(Util.descriptionTokenizer(write));
        if (this.readArgsIdx == DEFAULT_READ) {
            this.readArgsIdx = Collections.emptyList();
        }
    }

    public void setRead(String read) {
        this.readArgsIdx = parseArgs(Util.descriptionTokenizer(read));
        if (this.writeArgsIdx == DEFAULT_WRITE) {
            this.writeArgsIdx = Collections.emptyList();
        }
    }

    private static final String ARG_PREFIX = "%arg"; // NOI18N
    private static Collection<Integer> parseArgs(List<String> args) {
        Collection<Integer> res = new ArrayList<Integer>();
        for (String arg : args) {
            if (arg.startsWith(ARG_PREFIX)) {
                String argNo = arg.substring(ARG_PREFIX.length(), arg.length()-1);
                try {
                    res.add(Integer.valueOf(argNo));
                } catch (NumberFormatException nfe) {
                    // do nothing if was unable to parse
                }
            }
        }
        return res;
    }

    public Collection<Integer> getReadArgIdxs() {
        return readArgsIdx;
    }

    public Collection<Integer> getWriteArgIdxs() {
        return writeArgsIdx;
    }
}
