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

package org.netbeans.modules.cnd.debugger.gdb2.mi;

/**
 * Representation of a 'value' sub-tree from the MI spec.
 *
 * A value can be one of:
 * <ul>
 * <li>const	MIConst
 * <li>tuple	MITuple
 * <li>list	MIList
 * </ul>
 */

public abstract class MIValue extends MITListItem {
    @Override
    public abstract String toString();

    /**
     * Return true if this MIValue is actually an MIConst.
     */
    public boolean isConst() { return false; }

    /**
     * Down-cast this to an MIConst. 
     * Return null if isConst() is false.
     */
    public MIConst asConst() { return null; }



    /**
     * Return true if this MIValue is actually an MITList.
     */
    public boolean isTList() { return false; }

    /**
     * Down-cast this to an MITList. 
     */
    public MITList asTList() { return null; }

    /**
     * Down-cast this to an MITList. 
     */
    public MITList asTuple() { return null; }



    /**
     * Down-cast this to an MITList. 
     */
    public MITList asList() { return null; }
}

