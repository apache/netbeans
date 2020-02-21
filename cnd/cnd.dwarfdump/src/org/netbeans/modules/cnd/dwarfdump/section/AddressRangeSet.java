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
 * AddressRangeSet.java
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AddressRangeSet {
    long length;
    int  version;
    long info_offset;
    byte address_size;
    byte segment_descriptor_size;
    
    private List<AddressRange> ranges = new ArrayList<AddressRange>();
    
    void addRange(long address, long length) {
        ranges.add(new AddressRange(address, length));
    }
    
    public void dump(PrintStream out) {
        out.println();
        out.println("  Length:\t\t" + length); // NOI18N
        out.println("  Version:\t\t" + version); // NOI18N
        out.println("  Offset info .debug_info: " + info_offset); // NOI18N
        out.println("  Pointer size:\t\t" + address_size); // NOI18N
        out.println("  Segment size:\t\t" + segment_descriptor_size); // NOI18N

        out.println("\n\tAddress\t\tLength"); // NOI18N

        for (AddressRange addressRange : ranges) {
            addressRange.dump(out);
        }
        
        out.println();
    }
    
}
