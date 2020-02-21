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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfMacroInfoSection;

/**
 *
 */
public class DwarfMacinfoTable {
    private long baseSourceTableOffset = -1;
    private long fileSourceTableOffset = -1;
    private final DwarfMacroInfoSection section;
    private final List<DwarfMacinfoEntry> baseSourceTable = new ArrayList<DwarfMacinfoEntry>();
    private final List<DwarfMacinfoEntry> fileSourceTable = new ArrayList<DwarfMacinfoEntry>();
    private List<Integer> commandIncludedFilesTable;
    private boolean baseSourceTableRead;
    private boolean fileSourceTableRead;
    private boolean commandIncludedFilesRead;

    public DwarfMacinfoTable(DwarfMacroInfoSection section, long offset) {
        this.section = section;
        this.baseSourceTableRead = false;
        this.fileSourceTableRead = false;
        baseSourceTableOffset = fileSourceTableOffset = offset;
    }

    public void addEntry(DwarfMacinfoEntry entry) {
        if (entry.fileIdx == -1) {
            baseSourceTable.add(entry);
        } else {
            fileSourceTable.add(entry);
        }
    }

    private List<DwarfMacinfoEntry> getBaseSourceTable() throws IOException {

        if (baseSourceTableRead) {
            return baseSourceTable;
        }

        readBaseSourceTable();

        return baseSourceTable;
    }

    private List<DwarfMacinfoEntry> getFileSourceTable() throws IOException {
        if (fileSourceTableRead) {
            return fileSourceTable;
        }

        readFileSourceTable();
        return fileSourceTable;
    }

    private void readBaseSourceTable() throws IOException {
        long length = section.readMacinfoTable(this, baseSourceTableOffset, true);
        fileSourceTableOffset = baseSourceTableOffset + length;
        baseSourceTableRead = true;
    }

    private void readFileSourceTable() throws IOException {
        /*long length =*/ section.readMacinfoTable(this, fileSourceTableOffset, false);
        fileSourceTableRead = true;
    }

    public List<Integer> getCommandLineIncludedFiles() throws IOException{
        if (!commandIncludedFilesRead) {
            commandIncludedFilesTable = section.getCommandIncudedFiles(this, baseSourceTableOffset, baseSourceTableOffset);
            commandIncludedFilesRead = true;
        }
        return commandIncludedFilesTable;
    }

    public List<DwarfMacinfoEntry> getCommandLineMarcos() throws IOException {
        List<DwarfMacinfoEntry> entries = getBaseSourceTable();
        int size = entries.size();

        if (size == 0) {
            return entries;
        }

        int idx = 0;
        if (size > 2) {
            DwarfMacinfoEntry firstEntry = entries.get(0);
            DwarfMacinfoEntry secondEntry = entries.get(1);
            if (firstEntry.fileIdx == -1 && secondEntry.fileIdx == -1 &&
                firstEntry.lineNum == secondEntry.lineNum) {
                // there is a section with same line index information, so we
                // can not extract predefined compiler macros for sure;
                // return all entries with "-1" information in file index
                // and let client filter them out
                ArrayList<DwarfMacinfoEntry> retain = new ArrayList<DwarfMacinfoEntry>();
                for (int i = idx; i < entries.size(); i++) {
                    DwarfMacinfoEntry entry = entries.get(i);
                    if (entry.fileIdx == -1) {
                        retain.add(entry);
                    } else {
                        break;
                    }
                }
                return retain;
            }
        }
        ArrayList<DwarfMacinfoEntry> result = new ArrayList<DwarfMacinfoEntry>();
        int currLine = entries.get(idx).lineNum;
        int prevLine = -1;
        int count = 0;
        // Skip non-command-line entries...
        while (currLine > prevLine && idx < size) {
            prevLine = currLine;
            if (idx == size -1){
                return result;
            }
            currLine = entries.get(++idx).lineNum;
            count++;
        }
        if (count < 10 && currLine == 1){
            // it seems all system and user macros have the same lineNum == 1
            idx = 0;
            DwarfMacinfoEntry entry = entries.get(idx);
            do {
                result.add(entry);
                currLine = entry.lineNum;
                idx++;
            } while (idx < size && (entry = entries.get(idx)).lineNum == 1);
            return result;
        }
        DwarfMacinfoEntry entry = entries.get(idx);

        do {
            result.add(entry);
            currLine = entry.lineNum;
            idx++;
        } while (idx < size && (entry = entries.get(idx)).lineNum - currLine == 1);

        return result;
    }

    public ArrayList<DwarfMacinfoEntry> getMacros(int fileIdx) throws IOException {
        ArrayList<DwarfMacinfoEntry> result = new ArrayList<DwarfMacinfoEntry>();

        for (DwarfMacinfoEntry entry : getFileSourceTable()) {
            if (entry.fileIdx == fileIdx && (entry.type.equals(MACINFO.DW_MACINFO_define) || entry.type.equals(MACINFO.DW_MACINFO_undef))) {
                result.add(entry);
            }
        }

        return result;
    }

    public void dump(PrintStream out) {
        out.printf("%nMACRO Table (offset = %d [0x%08x]):%n%n", baseSourceTableOffset, baseSourceTableOffset); // NOI18N
        try {
            for (DwarfMacinfoEntry entry : getBaseSourceTable()) {
                entry.dump(out);
            }

            for (DwarfMacinfoEntry entry : getFileSourceTable()) {
                entry.dump(out);
            }
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.INFO, "Cannot read eteries", ex);
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
