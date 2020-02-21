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
package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 */
public final class APTFileInfoQuerySupport {

    private APTFileInfoQuerySupport() {
    }

    public static List<CsmReference> getMacroUsages(FileImpl fileImpl, final Interrupter interrupter) throws IOException {
        return APTFindMacrosWalker.getAPTMacroUsagesImpl(fileImpl, interrupter);
    }

    public static CsmOffsetable getGuardOffset(FileImpl fileImpl) {
        return APTFindMacrosWalker.getGuardOffsetImpl(fileImpl);
    }

    public static boolean hasGuardBlock(FileImpl fileImpl) {
        return APTFindMacrosWalker.hasGuardBlockImpl(fileImpl);
    }

    public static String expand(FileImpl fileImpl, String code, PreprocHandler handler, ProjectBase base, int offset) {
        return StopOnOffsetParseFileWalker.expandImpl(fileImpl, code, handler, base, offset);
    }
}
