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
package org.netbeans.modules.cnd.source;

/**
 *  A node representing this Fortran object. The Fortran file could be F77,
 *  F90, or F95.
 */
public class FortranDataNode extends SourceDataNode {

    /** The base name of the Fortran source icon */
    static final String FortranSrcIcon =
            "org/netbeans/modules/cnd/source/resources/FortranSrcIcon.gif"; // NOI18N

    public FortranDataNode(SourceDataObject obj) {
        super(obj, obj.getLookup(), FortranSrcIcon);
    }
}
