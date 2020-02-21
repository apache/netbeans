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

/*
 * DwarfArangesSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;

/**
 *
 */
public class DwarfArangesSection extends ElfSection {
    private final List<AddressRangeSet> addressRangeSets = new ArrayList<AddressRangeSet>();
    
    /** Creates a new instance of DwarfArangesSection */
    public DwarfArangesSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }
    
    void addAddressRangeSet(AddressRangeSet addressRangeSet) {
        addressRangeSets.add(addressRangeSet);
    }
    
    public List<AddressRangeSet> getAddressRangeSets() throws IOException {
        if (addressRangeSets.isEmpty()) {
            read();
        }
        
        return addressRangeSets;
    }
    
    @Override
    public DwarfArangesSection read() throws IOException {
        long sectionStart = header.getSectionOffset();
        long sectionEnd = header.getSectionSize() + sectionStart;
        
        reader.seek(sectionStart);
        
        while (reader.getFilePointer() != sectionEnd) {
            AddressRangeSet addressRangeSet = new AddressRangeSet();
            addressRangeSet.length = reader.readInt();
            addressRangeSet.version = reader.readShort();
            addressRangeSet.info_offset = reader.readInt();
            addressRangeSet.address_size = (byte)(0xff & reader.readByte());
            addressRangeSet.segment_descriptor_size = (byte)(0xff & reader.readByte());
            
            //  The first tuple following the header in each set begins at an
            // offset that is a multiple of the size of a single tuple
            int multTupleSize = addressRangeSet.address_size * 2;
            int hLength = 12; /* header size */
            
            while (multTupleSize < hLength) {
                multTupleSize <<= 1;
            }
            
            reader.skipBytes(multTupleSize - hLength);
            
            long address, length;

            do {
                address = reader.readNumber(addressRangeSet.address_size);
                length = reader.readNumber(addressRangeSet.address_size);
                addressRangeSet.addRange(address, length);
            } while (address != 0 || length != 0);

            addAddressRangeSet(addressRangeSet);
        }
        
        return this;
    }
    
    @Override
    public void dump(PrintStream out) {
        super.dump(out);
        try {
            for (AddressRangeSet addressRangeSet : getAddressRangeSets()) {
                addressRangeSet.dump(out);
            }
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.INFO, "Cannot read adress range "+reader.getFileName(), ex); //NOI18N
        }
        
        out.println();
    }
}
