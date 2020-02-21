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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ACCESS;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.VIS;
import org.netbeans.modules.cnd.dwarfdump.reader.ByteStreamReader;

/**
 *
 */
public class DwarfEntry {
    private final CompilationUnit compilationUnit;
    private final DwarfAbbriviationTableEntry abbriviationTableEntry;
    private final List<Object> values;
    private final List<DwarfEntry> children;
    private final long refference;
    private final int hierarchyLevel;
    private String qualifiedName = null;
    private String name = null;
    private DwarfEntry parent;

    /** Creates a new instance of DwarfEntry */

    public DwarfEntry(CompilationUnit compilationUnit, DwarfAbbriviationTableEntry abbrEntry, long refference, int hierarchyLevel) {
        this.abbriviationTableEntry = abbrEntry;
        this.compilationUnit = compilationUnit;
        this.refference = refference;
        this.hierarchyLevel = hierarchyLevel;
        if (abbriviationTableEntry.hasChildren()) {
            children = new ArrayList<DwarfEntry>();
        } else {
            children = Collections.<DwarfEntry>emptyList();
        }
        values = new ArrayList<Object>(abbriviationTableEntry.getAttributesCount());
    }

    public TAG getKind() {
        return abbriviationTableEntry.getKind();
    }

    public String getName() throws IOException {
        if (name == null) {
            Object nameAttr = getAttributeValue(ATTR.DW_AT_name);
            name = (nameAttr == null) ? "" : stripComments((String)nameAttr); // NOI18N
        }

        return name;
    }

    public String getQualifiedName() throws IOException {
        if (qualifiedName == null) {
            DwarfEntry specification = getSpecification();

            if (specification != null) {
                return specification.getQualifiedName();
            }

            qualifiedName = constructQualifiedName();
        }

        return qualifiedName;
    }

    private String constructQualifiedName() throws IOException {
        if (parent == null) {
            return getName();
        }

        TAG kind = parent.getKind();
        switch (kind) {
            case DW_TAG_compile_unit:
                return getName();
            case DW_TAG_lexical_block:
                return getName();
        }

        String aName = getName();
        String pname = parent.getQualifiedName();
        String qname = (pname != null && aName != null && !pname.equals("") && !aName.equals("")) ? pname + "::" + aName : aName;  // NOI18N

        return qname;
    }

    private String stripComments(String str) {
        if (str == null) {
            return null;
        }

        int idx = str.indexOf('#');

        if (idx != -1) {
            str = str.substring(0, idx);
        }

        return str.trim();
    }

    public void setQualifiedName(String qualifiedName) throws IOException {
        this.qualifiedName = qualifiedName;

        DwarfEntry origin = getAbstractOrigin();
        if (origin != null) {
            origin.setQualifiedName(qualifiedName);
        }

        DwarfEntry specification = getSpecification();
        if (specification != null) {
            specification.setQualifiedName(qualifiedName);
        }
    }

    public String getType() throws IOException {
        return compilationUnit.getType(this);
    }

    public int getUintAttributeValue(ATTR attr) throws IOException {
        Object value = getAttributeValue(attr);

        if (value == null) {
            return -1;
        }
        int result = ((Number)value).intValue();

        if (result < 0) {
            result &= 0xFF;
        }

        return result;
    }

    public Object getAttributeValue(ATTR attr) throws IOException {
        return getAttributeValue(attr, true);
    }

    public Object getAttributeValue(ATTR attr, boolean recursive) throws IOException {
        Object attrValue = null;

        // Get the index of this attribute from the abbriviation table entry
        // associated with this one.

        int attrIdx = abbriviationTableEntry.getAttribute(attr);

        // If there is no such attribute in this entry - try to get this
        // attribute from "abstract origin" or "specification" entry (if any)

        if (attrIdx == -1) {
            if (recursive) {
                Integer offset = -1;

                if (abbriviationTableEntry.getAttribute(ATTR.DW_AT_abstract_origin) >= 0) {
                    offset = (Integer)getAttributeValue(ATTR.DW_AT_abstract_origin);
                } else if (abbriviationTableEntry.getAttribute(ATTR.DW_AT_specification) >= 0) {
                    offset = (Integer)getAttributeValue(ATTR.DW_AT_specification);
                }


                if (offset >= 0) {
                    DwarfEntry attrEntry = compilationUnit.getEntry(offset);
                    if (attrEntry != null) {
                        attrValue = attrEntry.getAttributeValue(attr);
                    }
                }
            }
        } else {
            // Attribute has been found
            attrValue = values.get(attrIdx);
        }

        return attrValue;
    }

    public void addValue(Object value) {
        values.add(value);
    }

    public List<DwarfEntry> getChildren() {
        return children;
    }

    /**
     * Gets an entry, for which this entry is referred as specification
     * (via DW_AT_specification).
     * Note that this works only after all entries have been read.
     */
    public DwarfEntry getDefinition() throws IOException {
        return compilationUnit.getDefinition(this);
    }

    /**
     * Gets an entry that is referred by this is entry as specification
     * (via DW_AT_specification).
     * Note that this works only after all entries have been read.
     */
    public DwarfEntry getSpecification() throws IOException {
        Object o = getAttributeValue(ATTR.DW_AT_specification);
        if( o instanceof Integer ) {
            return compilationUnit.getEntry(((Integer) o).intValue());
        }
        return null;
    }

    public DwarfEntry getAbstractOrigin() throws IOException {
        Object o = getAttributeValue(ATTR.DW_AT_abstract_origin);
        if (o instanceof Integer) {
            return compilationUnit.getEntry(((Integer)o).intValue());
        }
        return null;
    }

    public boolean hasChildren() {
        return abbriviationTableEntry.hasChildren();
    }

    public void addChild(DwarfEntry child) {
        children.add(child);
        child.setParent(this);
    }

    public DwarfEntry getParent() {
        return parent;
    }

    private void setParent(DwarfEntry parent) {
        this.parent = parent;
    }

    public long getRefference() {
        return refference;
    }

    public String getParametersString() throws IOException {
        return getParametersString(true);
    }

    public String getParametersString(boolean withNames) throws IOException {
        List<DwarfEntry> params = getParameters();
        StringBuilder paramStr = new StringBuilder(); // NOI18N
        DwarfEntry param = null;

        paramStr.append('(');

        for (Iterator<DwarfEntry> it = params.iterator(); it.hasNext();) {
            param = it.next();

            if (param.getKind().equals(TAG.DW_TAG_unspecified_parameters)) {
                paramStr.append("..."); // NOI18N
            } else {
                paramStr.append(param.getType());
                if (withNames) {
                    paramStr.append(" "); // NOI18N
                    paramStr.append(param.getName());
                }
            }

            if (it.hasNext()) {
                paramStr.append(", "); // NOI18N
            }
        }

        paramStr.append(')'); // NOI18N

        return paramStr.toString();
    }

    public DwarfDeclaration getDeclaration() throws IOException {
        TAG kind = getKind();
        String aName = getQualifiedName();
        String type = getType();
        String paramStr = ""; // NOI18N

        if (kind.equals(TAG.DW_TAG_subprogram)) {
            paramStr += getParametersString();
        }

        String declarationString = type + " " + (aName == null ? getName() : aName) + paramStr; // NOI18N

        int declarationLine = getLine();
        int declarationColumn = getColumn();

        String declarationPosition = ((declarationLine == -1) ? "" : declarationLine) +  // NOI18N
                ((declarationColumn == -1) ? "" : ":" + declarationColumn); // NOI18N

        declarationPosition += " <" + refference + " (0x" + Long.toHexString(refference) + ")>"; // NOI18N

        return new DwarfDeclaration(kind.toString(), declarationString, getDeclarationFilePath(), declarationPosition);
    }

    public int getLine() throws IOException {
        return getUintAttributeValue(ATTR.DW_AT_decl_line);
    }

    public int getColumn() throws IOException {
        return getUintAttributeValue(ATTR.DW_AT_decl_column);
    }

    public List<DwarfEntry> getParameters() throws IOException {
        List<DwarfEntry> result = new ArrayList<DwarfEntry>();
        List<DwarfEntry> aChildren = getChildren();

        for (DwarfEntry child: aChildren) {
            if (child.isParameter() && !child.isArtifitial()) {
                result.add(child);
            }
        }

        return result;
    }

    public List<DwarfEntry> getMembers() throws IOException {
        List<DwarfEntry> result = new ArrayList<DwarfEntry>();
        List<DwarfEntry> aChildren = getChildren();

        for (DwarfEntry child: aChildren) {
            if (child.isMember() && !child.isArtifitial()) {
                result.add(child);
            }
        }

        return result;
    }

    public TAG getTag() {
        return abbriviationTableEntry.getKind();
    }

    public void dump(PrintStream out) {
        out.print("<" + hierarchyLevel + "><" + Long.toHexString(refference) + ">: "); // NOI18N
        abbriviationTableEntry.dump(out, this);

        for (int i = 0; i < children.size(); i++) {
            children.get(i).dump(out);
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

    public boolean isArtifitial() throws IOException {
        Object isArt = getAttributeValue(ATTR.DW_AT_artificial);
        return ((isArt != null) && ((Boolean)isArt).booleanValue());
    }

    public boolean hasAbastractOrigin() throws IOException {
        Object abastractOrigin = getAttributeValue(ATTR.DW_AT_abstract_origin);
        return (abastractOrigin != null);
    }

    public boolean isExternal() throws IOException {
        Object result = getAttributeValue(ATTR.DW_AT_external);
        return ((result != null) && ((Boolean)result).booleanValue());
    }

    public VIS getVisibility() throws IOException {
        Object result = getAttributeValue(ATTR.DW_AT_visibility);
        if (result instanceof Byte) {
            return VIS.get((Byte)result);
        }
        return null;
    }

    public boolean isNamespace() {
        return getKind().equals(TAG.DW_TAG_namespace);
    }


    public ACCESS getAccessibility() throws IOException {
        Object result = getAttributeValue(ATTR.DW_AT_accessibility);
        return (result == null) ? null : ACCESS.get(((Number)result).intValue());
    }

    public boolean isParameter() {
        TAG kind = getKind();
        return kind.equals(TAG.DW_TAG_formal_parameter) || kind.equals(TAG.DW_TAG_unspecified_parameters);
    }

    public boolean isMember() {
        TAG kind = getKind();
        //return kind.equals(TAG.DW_TAG_member);
        return !kind.equals(TAG.DW_TAG_inheritance);
    }

    public boolean isEntryDefinedInFile(int fileEntryIdx) throws IOException {
        int fileIdx = getUintAttributeValue(ATTR.DW_AT_decl_file);
        return (fileIdx == fileEntryIdx);
    }

    public String getDeclarationFilePath() throws IOException {
        int fileIdx = (Integer)getUintAttributeValue(ATTR.DW_AT_decl_file);
        return (fileIdx <= 0) ? null : compilationUnit.getStatementList().getFilePath(fileIdx);
    }

    public String getTypeDef() throws IOException {
        if (getKind().equals(TAG.DW_TAG_typedef)) {
            return getType();
        }

        Object typeRefIdx = getAttributeValue(ATTR.DW_AT_type);
        DwarfEntry typeRef = null;
        if (typeRefIdx instanceof Integer) {
            typeRef = compilationUnit.getTypedefFor((Integer)typeRefIdx);
        } else if (typeRefIdx instanceof Long) {
            typeRef = compilationUnit.getTypedefFor((Long)typeRefIdx);
        }

        return (typeRef == null) ? getType() : typeRef.getType();
    }

    public long getSibling() throws IOException{
        Object refIdx = getAttributeValue(ATTR.DW_AT_sibling, false);
        if (refIdx instanceof Integer) {
            return ((Integer)refIdx).intValue();
        } else if (refIdx instanceof Long) {
            return ((Long)refIdx).longValue();
        }
        return -1;
    }

    List<Object> getValues() {
        return values;
    }

    public long getLowAddress() throws IOException{
        byte[] bytes = (byte[])getAttributeValue(ATTR.DW_AT_low_pc);
        if (bytes != null) {
            return getAddress(bytes);
        }
        return 0;
    }

    public long getHighAddress() throws IOException{
        byte[] bytes = (byte[])getAttributeValue(ATTR.DW_AT_high_pc);
        if (bytes != null) {
            return getAddress(bytes);
        }
        return 0;
    }

    public long getAddress(byte[] bytes){
        long n = 0;
        int size = bytes.length;
        for (int i = 0; i < size; i++) {
            long u = 0;
            if (compilationUnit.getDataEncoding() == ByteStreamReader.LSB) {
                u = (0xff & bytes[i]);
            } else {
                u = (0xff & bytes[size - i - 1]);
            }
            n |= (u << (i * 8));
        }
        return n;
    }
}
