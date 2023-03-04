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

package org.netbeans.lib.profiler.marker;


/**
 *
 * @author Jaroslav Bachorik
 */
public class Mark implements Cloneable {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    
    public static final short DEFAULT_ID = 0;
    public static final char ID_NONE = (char) 0;
    private static short counter = 1;
    public static final Mark DEFAULT = new Mark(DEFAULT_ID); 
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    public final short id;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of Mark */
    public Mark(short value) {
        this.id = value;
    }
    
    public Mark() {
        this.id = counter++;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public short getId() {
        return id;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (!(other instanceof Mark)) {
            return false;
        }

        return id == ((Mark) other).id;
    }

    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.id;
        return hash;
    }
}
