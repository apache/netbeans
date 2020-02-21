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

package org.netbeans.modules.cnd.dwarfdump;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTableEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfNameLookupTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.FORM;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAbbriviationTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAttribute;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection.LineNumber;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfMacroInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfNameLookupTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfRelaDebugInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.StringTableSection;

/**
 *
 */
public class CompilationUnit implements CompilationUnitInterface{
    private final DwarfReader reader;

    private final long debugInfoSectionOffset;
    public final long unit_offset;
    private long unit_length;
    private long unit_total_length;
    private int  version;
    private long debug_abbrev_offset;
    private byte address_size;
    private DwarfEntry root = null;

    private DwarfAbbriviationTable abbr_table = null;
    private DwarfStatementList statement_list = null;
    private DwarfLineInfoSection lineInfoSection = null;
    private DwarfMacinfoTable macrosTable = null;
    private DwarfNameLookupTable pubnamesTable = null;
    private DwarfRelaDebugInfoSection rela = null;

    private long debugInfoOffset;

    private final Map<Long, Long> specifications = new HashMap<Long, Long>();
    private final Map<Long, DwarfEntry> entries = new HashMap<Long, DwarfEntry>();

    /** Creates a new instance of CompilationUnit */
    public CompilationUnit(DwarfReader reader, long sectionOffset, long unitOffset) throws IOException {
        this.reader = reader;
        this.debugInfoSectionOffset = sectionOffset;
        this.unit_offset = unitOffset;
        readCompilationUnitHeader();
        root = getDebugInfo(false);
    }

    public int getDataEncoding(){
        return reader.getDataEncoding();
    }

    public String getProducer() throws IOException {
        return (String)root.getAttributeValue(ATTR.DW_AT_producer);
    }

    public String getCompilationDir() throws IOException {
        return (String)root.getAttributeValue(ATTR.DW_AT_comp_dir);
    }

    public String getSourceFileName() throws IOException {
        if (root != null) {
            return (String)root.getAttributeValue(ATTR.DW_AT_name);
        }
        return null;
    }

    public String getCommandLine() throws IOException {
        Object cl = root.getAttributeValue(ATTR.DW_AT_SUN_command_line);
        return (cl == null) ? null : (String)cl;
    }

    public String getCompileOptions() throws IOException {
        Object cl = root.getAttributeValue(ATTR.DW_AT_SUN_compile_options);
        return (cl == null) ? null : (String)cl;
    }

    public String getSourceFileAbsolutePath() throws IOException {
        String result;

        String dir = getCompilationDir();
        String name = getSourceFileName();
        if (dir != null) {
            if (isAbsolute(name)) {
                result = name;
            } else {
                if (dir.endsWith("/") || dir.endsWith("\\")) { // NOI18N
                    result = dir+name;
                } else {
                    result = dir+ File.separator + name;
                }
            }
        } else {
            result = name;
        }

        return result;
    }

    private boolean isAbsolute(String path) {
        if (path.startsWith("/") || path.length() > 2 && path.charAt(1) == ':') { // NOI18N
            return true;
        }
        return false;
    }

    public String getSourceLanguage() throws IOException {
        if (root != null) {
            Object lang = root.getAttributeValue(ATTR.DW_AT_language);
            if (lang != null) {
                return lang.toString();
            }
        }
        return null;
    }

    public DwarfEntry getReferencedType(DwarfEntry entry) throws IOException{
        Object typeRef = entry.getAttributeValue(ATTR.DW_AT_type);
        if (typeRef instanceof Integer) {
            return getEntry((Integer)typeRef);
        } else if (typeRef instanceof Long) {
            return getEntry((Long)typeRef);
        }
        return null;
    }

    public DwarfEntry getReferencedFriend(DwarfEntry entry) throws IOException{
        Object typeRef = entry.getAttributeValue(ATTR.DW_AT_friend);
        if (typeRef instanceof Integer) {
            return getEntry((Integer)typeRef);
        } else if (typeRef instanceof Long) {
            return getEntry((Long)typeRef);
        }
        return null;
    }

    public String getType(DwarfEntry entry) throws IOException {
        TAG entryKind = entry.getKind();

        if (entryKind.equals(TAG.DW_TAG_unspecified_parameters)) {
            return "null"; // NOI18N
        }

        Object typeRef = entry.getAttributeValue(ATTR.DW_AT_type);
        long ref;
        if (typeRef instanceof Integer) {
            ref =(Integer)typeRef;
        } else if (typeRef instanceof Long) {
            ref =(Long)typeRef;
        } else {
            return "void"; // NOI18N
        }

        DwarfEntry typeEntry = getEntry(ref);
        TAG kind = typeEntry.getKind();

        if (kind.equals(TAG.DW_TAG_base_type)) {
            String name = typeEntry.getName();

            // TODO: Is it OK?
            if (name.equals("long unsigned int")) { // NOI18N
                name = "unsigned long"; // NOI18N
            } else if (name.equals("long int")) { // NOI18N
                name = "long"; // NOI18N
            }

            return name;
        }

        if (kind.equals(TAG.DW_TAG_structure_type) ||
                kind.equals(TAG.DW_TAG_enumeration_type) ||
                kind.equals(TAG.DW_TAG_union_type) ||
                kind.equals(TAG.DW_TAG_typedef) ||
                kind.equals(TAG.DW_TAG_class_type)) {
            return typeEntry.getName();
        }

        if (kind.equals(TAG.DW_TAG_const_type)) {
            // TODO: Check algorithm!

            Object atType = typeEntry.getAttributeValue(ATTR.DW_AT_type);

            if (atType == null) {
                return "const void"; // NOI18N
            }
            DwarfEntry refTypeEntry = null;
            if (atType instanceof Integer) {
                refTypeEntry = getEntry((Integer)atType);
            } else if (atType instanceof Long) {
                refTypeEntry = getEntry((Long)atType);
            }
            if (refTypeEntry != null) {
                if (refTypeEntry.getKind().equals(TAG.DW_TAG_reference_type) ||
                        refTypeEntry.getKind().equals(TAG.DW_TAG_array_type)) {
                    return getType(typeEntry);
                } else {
		    if( refTypeEntry.getKind() == TAG.DW_TAG_pointer_type ) {
			return getType(typeEntry) + " const"; // NOI18N
		    }
		    else {
			return "const " + getType(typeEntry); // NOI18N
		    }
                }
            }

//            return "const " + getType(typeEntry); // NOI18N

        }

        if (kind.equals(TAG.DW_TAG_reference_type)) {
            return getType(typeEntry) + "&"; // NOI18N
        }

        if (kind.equals(TAG.DW_TAG_array_type)) {
            return getType(typeEntry) + "[]"; // NOI18N
        }

        if (kind.equals(TAG.DW_TAG_pointer_type) || kind.equals(TAG.DW_TAG_ptr_to_member_type)) {
            return getType(typeEntry) + "*"; // NOI18N
        }

        if (kind.equals(TAG.DW_TAG_subroutine_type)) {
            return typeEntry.getParametersString(false);
        }

        if (kind.equals(TAG.DW_TAG_volatile_type)) {
            return getType(typeEntry);
        }

        if (kind.equals(TAG.DW_TAG_union_type)) {
            return getType(typeEntry);
        }

        return "<" + kind + ">"; // NOI18N
    }

    public DwarfEntry getEntry(long sectionOffset) throws IOException {
        //return entryLookup(getDebugInfo(true), sectionOffset);
        DwarfEntry entry = entries.get(sectionOffset);

        if (entry == null) {
            entry = entryLookup(getDebugInfo(true), sectionOffset);
            entries.put(sectionOffset, entry);
        }

        return entry;
    }

    public DwarfEntry getDefinition(DwarfEntry entry) throws IOException {
        Long ref = specifications.get(entry.getRefference());
        if( ref != null ) {
            return getEntry(ref);
        }
        return null;
    }

    private DwarfEntry entryLookup(DwarfEntry entry, long refference) {
        if (entry == null) {
            return null;
        }

        if (entry.getRefference() == refference) {
            return entry;
        }

        for (DwarfEntry child : entry.getChildren()) {
            DwarfEntry res = entryLookup(child, refference);
            if (res != null) {
                return res;
            }
        }

        return null;
    }

    public DwarfEntry getRoot() {
        return root;
    }

    public DwarfEntry getTypedefFor(long typeRef) throws IOException {
        // TODO: Rewrite not to iterate every time.

        for (DwarfEntry entry : getDebugInfo(true).getChildren()) {
            if (entry.getKind().equals(TAG.DW_TAG_typedef)) {
                Object entryTypeRef = entry.getAttributeValue(ATTR.DW_AT_type);
                if (entryTypeRef instanceof Integer) {
                    if (((Integer)entryTypeRef).equals((int)typeRef)) {
                        return entry;
                    }
                } else if (entryTypeRef instanceof Long) {
                    if (((Long)entryTypeRef).equals(typeRef)) {
                        return entry;
                    }
                }
            }
        }

        return null;
    }

    /**
     * unit_length represents the length of the .debug_info contribution for
     * this compilation unit, not including the length field itself. So this
     * method returns unit_length + sizeof(unit_length field). I.e. 4 or 4 + 8.
     * @return the total bytes number occupied by this CU.
     */

    public long getUnitTotalLength() {
        return unit_total_length;
    }

    private void readCompilationUnitHeader() throws IOException {
        reader.seek(debugInfoSectionOffset + unit_offset);
        // offset size is detected from readDWlen
        // if first int is -1 when it is 64 bit section
        //unit_length = reader.readDWlen();
        int aLegth = reader.readInt();
        if (aLegth == -1) {
            unit_length = reader.readLong();
            reader.setFileClass(ElfConstants.ELFCLASS64);
        } else {
            unit_length = aLegth;
            reader.setFileClass(ElfConstants.ELFCLASS32)            ;
        }
        // The total length of this CU is unit_lenght + sizeof(unit_lenght field).

        long pos = reader.getFilePointer();
        unit_total_length = unit_length + pos - (debugInfoSectionOffset + unit_offset);

        version             = reader.readShort();
        debug_abbrev_offset = reader.read3264();
        address_size        = (byte)(0xff & reader.readByte());

        debugInfoOffset = reader.getFilePointer();

        reader.setAddressSize(address_size);

        rela = (DwarfRelaDebugInfoSection)reader.getSection(SECTIONS.RELA_DEBUG_INFO);
        if (rela != null) {
            Long abbrAddend = rela.getAbbrAddend(unit_offset + 6);
            if (abbrAddend != null) {
                debug_abbrev_offset += abbrAddend;
            }
        }
        DwarfAbbriviationTableSection abbrSection = (DwarfAbbriviationTableSection)reader.getSection(SECTIONS.DEBUG_ABBREV);
        abbr_table = abbrSection.getAbbriviationTable(debug_abbrev_offset);
    }

    public DwarfStatementList getStatementList() throws IOException {
        if (statement_list == null) {
            initStatementList();
        }

        return statement_list;
    }

    public  Set<LineNumber> getLineNumbers() throws IOException{
        if (statement_list == null) {
            initStatementList();
        }
        Number statementListOffset = (Number)root.getAttributeValue(ATTR.DW_AT_stmt_list);
        if (statementListOffset != null) {
            return lineInfoSection.getLineNumbers(statementListOffset.longValue());
        }
        return null;
    }

    public LineNumber getLineNumber(long target) throws IOException{
        if (statement_list == null) {
            initStatementList();
        }
        Number statementListOffset = (Number)root.getAttributeValue(ATTR.DW_AT_stmt_list);
        if (statementListOffset != null) {
            return lineInfoSection.getLineNumber(statementListOffset.longValue(), target);
        }
        return null;
    }

    public DwarfMacinfoTable getMacrosTable() throws IOException {
        if (macrosTable == null) {
            initMacrosTable();
        }

        return macrosTable;
    }

    private DwarfNameLookupTable getPubnamesTable() throws IOException {
        if (pubnamesTable == null) {
            initPubnamesTable();
        }

        return pubnamesTable;
    }


    private DwarfEntry getDebugInfo(boolean readChildren) throws IOException {
        if (root == null || (readChildren && root.getChildren().isEmpty())) {
            //getPubnamesTable();
            reader.seek(debugInfoOffset);
            root = readEntry(0, readChildren);

            if (readChildren) {
                setSpecializations(root);
            }
        }

        return root;
    }

    private void setSpecializations(DwarfEntry entry) throws IOException {
        Object o = entry.getAttributeValue(ATTR.DW_AT_specification);

        if (o instanceof Integer) {
            specifications.put(Long.valueOf(((Integer) o).intValue()), entry.getRefference());
        }

        for (DwarfEntry child : entry.getChildren()) {
            setSpecializations(child);
        }
    }

    private DwarfEntry readEntry(int level) throws IOException {
        long refference = reader.getFilePointer() - debugInfoSectionOffset - unit_offset;
        long idx = reader.readUnsignedLEB128();
        if (idx == 0) {
            return null;
        }
        DwarfAbbriviationTableEntry abbreviationEntry = abbr_table.getEntry(idx);
        if (abbreviationEntry == null) {
            return null;
        }
        DwarfEntry entry = new DwarfEntry(this, abbreviationEntry, refference, level);
        for (int i = 0; i < abbreviationEntry.getAttributesCount(); i++) {
            DwarfAttribute attr = abbreviationEntry.getAttribute(i);
            long dif = reader.getFilePointer() - debugInfoSectionOffset;
            Long replace = null;
            if (rela != null){
                replace = rela.getAddend(dif);
            }
            if (replace != null && attr.valueForm == FORM.DW_FORM_strp) {
                reader.readAttrValue(attr);
                String s = ((StringTableSection)reader.getSection(SECTIONS.DEBUG_STR)).getString(replace.longValue());
                entry.addValue(s);
            } else if (replace != null && attr.valueForm == FORM.DW_FORM_sec_offset) {
                reader.readAttrValue(attr);
                entry.addValue(replace);
            } else {
                Object readAttrValue = reader.readAttrValue(attr);
                //if (replace != null) {
                //    readAttrValue = replace;
                //}
                entry.addValue(readAttrValue);
            }
        }
        return entry;
    }

    private DwarfEntry readEntry(int level, boolean readChildren) throws IOException {
        DwarfEntry entry = readEntry(level);
        if (entry == null) {
            return null;
        }
        entries.put(entry.getRefference(), entry);
        if (readChildren  && entry.hasChildren()) {
            DwarfEntry child;
            while ((child = readEntry(level + 1, true)) != null) {
                entry.addChild(child);
            }
        }
        return entry;
    }

    private void initStatementList() throws IOException {
        lineInfoSection = (DwarfLineInfoSection)reader.getSection(SECTIONS.DEBUG_LINE);

        if (root == null) {
            return;
        }
        //System.out.println(root);
        Number statementListOffset = (Number)root.getAttributeValue(ATTR.DW_AT_stmt_list);
        if (statementListOffset != null) {
            statement_list = lineInfoSection.getStatementList(statementListOffset.longValue());
        }
    }

    private void initMacrosTable() throws IOException {
        Object macroInfoOffset;
        DwarfMacroInfoSection macroInfoSection = (DwarfMacroInfoSection)reader.getSection(SECTIONS.DEBUG_MACINFO);
        boolean isMacro =false;
        if (macroInfoSection == null) {
            macroInfoSection = (DwarfMacroInfoSection)reader.getSection(SECTIONS.DEBUG_MACRO);
            if (macroInfoSection == null) {
                return;
            } else {
                macroInfoOffset = root.getAttributeValue(ATTR.DW_AT_GNU_macros);
                isMacro = true;
            }
        } else {
            macroInfoOffset = root.getAttributeValue(ATTR.DW_AT_macro_info);
        }

        if (macroInfoOffset instanceof Integer) {
            macrosTable = macroInfoSection.getMacinfoTable((Integer)macroInfoOffset);
        } else if (macroInfoOffset instanceof Long) {
            macrosTable = macroInfoSection.getMacinfoTable((Long)macroInfoOffset);
        }
    }

    private void initPubnamesTable() throws IOException {
        DwarfNameLookupTableSection dwarfNameLookupTableSection = (DwarfNameLookupTableSection)reader.getSection(SECTIONS.DEBUG_PUBNAMES);

        if (dwarfNameLookupTableSection != null) {
            pubnamesTable = dwarfNameLookupTableSection.getNameLookupTableFor(unit_offset);
        }
    }

    public List<DwarfEntry> getDeclarations() throws IOException {
        return getDeclarations(true);
    }

    public List<DwarfEntry> getEntries() throws IOException {
        // Read pubnames section first
        getPubnamesTable();
        return getDebugInfo(true).getChildren();
    }

    public List<DwarfEntry> getTopLevelEntries() throws IOException {
        // Read pubnames section first
        getPubnamesTable();
        reader.seek(debugInfoOffset);
        DwarfEntry aRoot = readEntry(0);
        if (aRoot == null) {
            return null;
        }
        if (aRoot.hasChildren()) {
            while (true) {
                DwarfEntry child = readEntry(1);
                if (child == null) {
                    break;
                }
                aRoot.addChild(child);
                if (child.hasChildren()) {
                    long sibling = child.getSibling();
                    if (sibling <= 0) {
                        break;
                    } else {
                        if (sibling <= child.getRefference()) {
                            System.err.println("Infinite loop in sibling chain");
                            System.err.println(""+child);
                            break;
                        }
                        long refference = debugInfoSectionOffset + unit_offset + sibling;
                        reader.seek(refference);
                    }
                }
            }
        }
        return aRoot.getChildren();
    }

    /**
     * Used to get a list of declarations defined/used in this CU.
     * @param limitedToFile <code>true</code> means return declarations defined in the current source file only
     * @return returns a list of declarations defined/used in this CU.
     */
    public List<DwarfEntry> getDeclarations(boolean limitedToFile) throws IOException {
        boolean reportExcluded = false;
        int fileEntryIdx = 0;

        // make sure that pubnames table has been read ...
        getPubnamesTable();

        List<DwarfEntry> result = new ArrayList<DwarfEntry>();

        if (limitedToFile) {
            fileEntryIdx = getStatementList().getFileEntryIdx(getSourceFileName());
        }

        for (DwarfEntry child : getEntries()) {
            if (!limitedToFile || child.isEntryDefinedInFile(fileEntryIdx)) {
                // TODO: Check algorythm
                // Do not add definitions that have DW_AT_abstract_origin attribute.
                // Do not add entries that's names start with _GLOBAL__F | _GLOBAL__I | _GLOBAL__D

                if (!child.hasAbastractOrigin()) {
                    String qname = child.getQualifiedName();
                    if (qname != null && !qname.startsWith("_GLOBAL__")) { // NOI18N
                        result.add(child);
                    } else if (reportExcluded) {
                        System.out.println("Exclude declaration: " + child.getDeclaration()); // NOI18N
                    }
                }
            }
        }

        return result;
    }


    public void dump(PrintStream out) throws IOException {
        if (root == null) {
            out.println("*** No compilation units for " + reader.getFileName()); // NOI18N
            return;
        }

        out.println("*** " + getSourceFileAbsolutePath() + " ***"); // NOI18N
        out.println("  Compilation Unit @ offset " + Long.toHexString(unit_offset) + ":"); // NOI18N
        out.println("    Length: " + unit_length); // NOI18N
        out.println("    Version: " + version); // NOI18N
        out.println("    Abbrev Offset: " + debug_abbrev_offset); // NOI18N
        out.println("    Pointer Size: " + address_size); // NOI18N

        /*
         * getPubnamesTable() will not only set pubnamesTable (if not set yet)
         * but also setup qualified names from appropriate pubnames table.
         */

        getPubnamesTable();

        getDebugInfo(true).dump(out);
        DwarfStatementList stList = getStatementList();
        if (stList != null) {
            stList.dump(out);
        }

        // Still pubnamesTable could be null (if not present for this
        // Compilation Unit)

        if (pubnamesTable != null) {
            pubnamesTable.dump(out);
        }

        DwarfMacinfoTable macinfoTable = getMacrosTable();
        if( macinfoTable != null ) {
            macinfoTable.dump(out);
        }

        Set<LineNumber> numbers = getLineNumbers();
        if (numbers != null && numbers.size() > 0) {
            numbers = new TreeSet<LineNumber>(numbers);
            for(LineNumber line : numbers) {
                out.println(line);
            }
        }


        out.println();
    }

    @Override
    public String toString() {
        try {
            StringWriter sw = new StringWriter();
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(st, false, "UTF-8"); // NOI18N
            dump(out);
            return st.toString("UTF-8"); // NOI18N
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.INFO, "File "+reader.getFileName(), ex); // NOI18N
            return ""; // NOI18N
        }
    }

    public boolean hasMain() throws IOException {
        List<DwarfEntry> topLevelEntries = getTopLevelEntries();
        for(DwarfEntry entry : topLevelEntries) {
            if (entry.getKind() == TAG.DW_TAG_subprogram) {
                if ("main".equals(entry.getName())) { // NOI18N
                    if (entry.isExternal()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getMainLine() throws IOException {
        List<DwarfEntry> topLevelEntries = getTopLevelEntries();
        for(DwarfEntry entry : topLevelEntries) {
            if (entry.getKind() == TAG.DW_TAG_subprogram) {
                if ("main".equals(entry.getName())) { // NOI18N
                    if (entry.isExternal()) {
                        return entry.getLine();
                    }
                }
            }
        }
        return 0;
    }
}
