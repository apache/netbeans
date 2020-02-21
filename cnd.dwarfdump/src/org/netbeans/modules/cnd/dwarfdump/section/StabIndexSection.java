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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitStab;
import org.netbeans.modules.cnd.dwarfdump.Dwarf.CompilationUnitIterator;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.reader.ByteStreamReader;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.source.CompileLineOrigin;
import org.netbeans.modules.cnd.dwarfdump.source.DefaultDriver;

/**
 *
 */
public class StabIndexSection extends ElfSection {
    private final StabIndexStrSection strings;
    private final List<CompilationUnitInterface> list = new ArrayList<CompilationUnitInterface>();
    private static final int N_UNDF    = 0x0;   /* undefined */
    private static final int N_ILDPAD  = 0x4c;  /* now used as ild pad stab value=strtab delta was designed for 'function start.end' */
    private static final int N_FUN     = 0x24;  /* procedure: name,,0,linenumber,0 */
    private static final int N_SO      = 0x64;  /* source file name: name,,0,0,0 */
    private static final int N_OBJ     = 0x38;  /* object file path or name */
    private static final int N_CMDLINE = 0x34;  /* command line info */
    private static final int N_MAIN    = 0x2a;  /* name of main routine : name,,0,0,0 */
    
    
    public StabIndexSection(DwarfReader reader, int sectionIdx) throws IOException {
        super(reader, sectionIdx);
        strings =  (StabIndexStrSection) reader.getSection(SECTIONS.STAB_INDEXSTR);
        read();
    }

    public CompilationUnitIterator compilationUnits() throws IOException {
        return new DwarfDebugInfoSection.ListIterator(list.iterator());

    }
    
    @Override
    public final StabIndexSection read() throws IOException {
        long sectionStart = header.getSectionOffset();
        long sectionEnd = header.getSectionSize() + sectionStart;
        reader.seek(sectionStart);
        long StabStrtab = 0;
        long StrTabSize = 0;
        //System.out.println("N\tr_offset\tr_info\tr_addend");
        String compileDir = ""; //NOI18N
        String source = ""; //NOI18N
        String line = ""; //NOI18N
        String object = ""; //NOI18N
        String lastUndef = ""; //NOI18N
        String cuName = ""; //NOI18N
        boolean isMain = false;
        int mainLine = 0;
        int state = 1;
        int lang = 0;
        while(reader.getFilePointer() < sectionEnd) {
            int offset = reader.readInt();
            int type = ByteStreamReader.ubyteToInt(reader.readByte());
            int other = ByteStreamReader.ubyteToInt(reader.readByte());
            int desc = reader.readShort();
            int value = reader.readInt();
            //System.err.println(" "+offset+" "+type+" "+other+" "+desc+" "+value );
            if (type == N_UNDF || type == N_ILDPAD) {
                /* Start of new stab section (or padding) */
                StabStrtab += StrTabSize;
                StrTabSize = value;
            }
            long str;
            if (offset != 0) {
                String s;
                if (type == N_FUN && other == 1) {
                    if (offset == 1) {
                        StrTabSize++;
                    }
                    str = StabStrtab + StrTabSize;
                    // Each COMDAT string must be sized to find the next string:
                    s = strings.getString(str);
                    StrTabSize += s.length()+1;
                } else {
                    str = StabStrtab + offset;
                    s = strings.getString(str);
                }
                switch (type) {
                    case N_SO:
                        //System.err.println("Source file\t"+s);
                        if (state != 1) {
                            list.add(createCompilationUnitStab(compileDir, source, line, object, isMain, mainLine, lang, cuName));
                            compileDir = ""; //NOI18N
                            source = ""; //NOI18N
                            line = ""; //NOI18N
                            object = ""; //NOI18N
                            cuName = ""; //NOI18N
                            isMain = false;
                            mainLine = 0;
                            lang = 0;
                        }
                        if (compileDir.isEmpty()) {
                            cuName = lastUndef;
                            compileDir = s;
                        } else {
                            source = s;
                        }
                        state = 1;
                        if (lang == 0 && desc != 0) {
                            lang = desc;
                        }
                        break;
                    case N_OBJ:
                        //System.err.println("Object file\t"+s);
                        object += s;
                        state = 2;
                        break;
                    case N_CMDLINE:
                        //System.err.println("Command line\t"+s);
                        line = s;
                        state = 3;
                        break;
                    //case N_FUN:
                    //    if (mainLine == 0 && "main".equals(s)) { //NOI18N
                    //        mainLine = value;
                    //    }
                    //    break;
                    case N_MAIN:
                        //System.err.println("Main function\t"+s);
                        isMain = true;
                        mainLine = value;
                        break;
                    case N_UNDF:
                        //System.err.println(""+type+" "+s);
                        lastUndef = s;
                        break;
                    default:
                        //System.err.println(""+type+" "+s);
                        break;
                }
            }
        }
        if (state >= 1) {
            list.add(createCompilationUnitStab(compileDir, source, line, object, isMain, mainLine, lang, cuName));
        }
        return null;
    }

    private CompilationUnitStab createCompilationUnitStab(String compileDir, String sourceName, String line, String objectFile, boolean hasMain, int mainLine, int desc, String lastUndef) {
        if (lastUndef.length() > 0 && !sourceName.endsWith(lastUndef)) {
            // try to find source file instead of .y or .l
            int i = line.indexOf(';');
            if (i > 0) {
                String compileLine = line.substring(i+1).trim();
                if (compileLine.length() > 0) {
                    DefaultDriver driver = new DefaultDriver();
                    for(String s : driver.splitCommandLine(compileLine, CompileLineOrigin.DwarfCompileLine)) {
                        if (s.endsWith(lastUndef)) {
                            //System.err.println("Fix source name ["+sourceName+"]->["+s+"]");
                            sourceName = s;
                            break;
                        }
                    }
                }
            }
        }
        String trueSource;
        if (sourceName.startsWith("/")) { // NOI18N
            trueSource = sourceName;
        } else {
            trueSource = compileDir + sourceName;
        }
        return new CompilationUnitStab(trueSource, line, objectFile, hasMain, mainLine, desc);
    }
}
