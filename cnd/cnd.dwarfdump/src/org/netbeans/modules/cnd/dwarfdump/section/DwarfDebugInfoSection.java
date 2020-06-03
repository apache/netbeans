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

/*
 * DwarfDebugInfoSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitStab;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.Dwarf.CompilationUnitIterator;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;

/**
 *
 */
public class DwarfDebugInfoSection extends ElfSection {
    private List<CompilationUnitInterface> compilationUnits;
    //DwarfRelaDebugInfoSection rela;

    public DwarfDebugInfoSection(DwarfReader reader, int sectionIdx) throws IOException {
        super(reader, sectionIdx);
        /*rela = (DwarfRelaDebugInfoSection)*/ reader.getSection(SECTIONS.RELA_DEBUG_INFO);
    }

    public DwarfDebugInfoSection(ElfReader reader, int sectionIdx, SectionHeader header, String sectionName) {
        super(reader, sectionIdx, header, sectionName);
    }

    public CompilationUnit getCompilationUnit(long unit_offset) throws IOException {
        for (CompilationUnitInterface unit : getCompilationUnits()) {
            if (unit instanceof CompilationUnit) {
                CompilationUnit cu = (CompilationUnit) unit;
                if (cu.unit_offset == unit_offset) {
                    return cu;
                }
            }
        }
        return null;
    }

    public CompilationUnitIterator iteratorCompilationUnits() throws IOException {
        if (compilationUnits != null) {
            return new ListIterator(compilationUnits.iterator());
        }
        return new UnitIterator();
    }

    public List<CompilationUnitInterface> getCompilationUnits() throws IOException {
        if (compilationUnits != null) {
            return compilationUnits;
        }
        compilationUnits = new ArrayList<CompilationUnitInterface>();
        int cuOffset = 0;
        while (cuOffset != header.sh_size) {
            ((DwarfReader)reader).seek(header.getSectionOffset() + cuOffset);
            if (reader.readDWlen()==0) {
                break;
            }
            CompilationUnit unit = new CompilationUnit((DwarfReader)reader, header.getSectionOffset(), cuOffset);
            compilationUnits.add(unit);
            cuOffset += unit.getUnitTotalLength();
        }
        return compilationUnits;
    }

    @Override
    public void dump(PrintStream out) {
        try {
            for (CompilationUnitInterface unit : getCompilationUnits()) {
                if (unit instanceof CompilationUnit) {
                    ((CompilationUnit)unit).dump(out);
                } else if (unit instanceof CompilationUnitStab) {
                    ((CompilationUnitStab)unit).dump(out);
                }
            }
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.OFF, "Cannot dump compilation unit "+reader.getFileName(), ex); //NOI18N
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

    private class UnitIterator implements CompilationUnitIterator {
        private int cuOffset = 0;
        private CompilationUnit unit;

        public UnitIterator() throws IOException {
            advance();
        }

        @Override
        public boolean hasNext() throws IOException {
            return unit != null;
        }

        @Override
        public CompilationUnit next() throws IOException {
            CompilationUnit res = unit;
            advance();
            return res;
        }

        private void advance() throws IOException {
            unit = null;
            if (cuOffset != header.sh_size) {
                ((DwarfReader) reader).seek(header.getSectionOffset() + cuOffset);
                if (reader.readDWlen() == 0) {
                    return;
                }
                unit = new CompilationUnit((DwarfReader) reader, header.getSectionOffset(), cuOffset);
                cuOffset += unit.getUnitTotalLength();
            }
        }
    }

    public static class ListIterator implements CompilationUnitIterator {
        private final Iterator<CompilationUnitInterface> it;
        public ListIterator(Iterator<CompilationUnitInterface> it) {
            this.it = it;
        }

        public boolean hasNext() throws IOException {
            return it.hasNext();
        }

        public CompilationUnitInterface next() throws IOException {
            return it.next();
        }
    }
}
