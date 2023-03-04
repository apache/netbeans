/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.editor.lib.api.foreign;

import java.io.IOException;

/**
 *
 * @author marekfukala
 */
public class MaskingChSReader extends SimpleMaskingChSReader {

    private int[] positions;
    private int[] lens;
    private int positionIndex;

    public MaskingChSReader(CharSequence immutableCharSequence, int[] positions, int[] lens) {
        super(immutableCharSequence);
        this.positions = positions;
        this.lens = lens;
    }

    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (next >= length) {
                return -1;
            }
            
            if (positions.length > positionIndex) {
                //some more masked areas found
                
                int pos = positions[positionIndex];
                
                if (pos <= next) {
                    //the actual position is after or at the masked area start
                    int len = lens[positionIndex];
                    int end = pos + len;
                    
                    if (end < next) {
                        //after end of the masked area
                        return read();
                    } else if ( end == next) {
                        positionIndex++;
                        //end exclusive
                        return read();
                    } else {
                        //next < end
                        //inside masked area
                        next++; //swallow the char
                        return MASK_CHAR;
                    }
                }

            }

            return super.read();
        }
    }

}
