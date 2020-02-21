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
