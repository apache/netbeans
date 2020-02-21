/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
