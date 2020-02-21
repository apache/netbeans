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

import java.io.IOException;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.openide.util.Exceptions;

/**
 *
 */
/*package*/
final class StopOnOffsetParseFileWalker extends APTParseFileWalker {

    private final int stopOffset;

    public StopOnOffsetParseFileWalker(ProjectBase base, APTFile apt, FileImpl file, int offset, PreprocHandler preprocHandler, APTFileCacheEntry cacheEntry) {
        super(base, apt, file, preprocHandler, false, null, cacheEntry);
        stopOffset = offset;
    }

    @Override
    protected boolean onAPT(APT node, boolean wasInBranch) {
        if (node.getEndOffset() >= stopOffset) {
            stop();
            return false;
        }
        return super.onAPT(node, wasInBranch);
    }
    
    /*package*/ static String expandImpl(FileImpl fileImpl, String code, PreprocHandler handler, ProjectBase base, int offset) {
      assert !APTTraceFlags.USE_CLANK;
      APTFile aptLight = null;
      try {
        aptLight = APTDriver.findAPTLight(fileImpl.getBuffer(), APTHandlersSupport.getAPTFileKind(handler));
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
      if (aptLight == null) {
        return code;
      }

      // create concurrent entry if absent
      APTFileCacheEntry cacheEntry = fileImpl.getAPTCacheEntry(handler.getState(), Boolean.FALSE);
      StopOnOffsetParseFileWalker walker = new StopOnOffsetParseFileWalker(base, aptLight, fileImpl, offset, handler,cacheEntry);
      walker.visit();
      // we do not remember cache entry because it is stopped before end of file
      // fileImpl.setAPTCacheEntry(handler, cacheEntry, false);
      TokenStream ts = APTTokenStreamBuilder.buildTokenStream(code, fileImpl.getFileLanguage());
      ts = new APTMacroExpandedStream(ts, (APTMacroCallback)handler.getMacroMap(), true);

      // skip comments, see IZ 207378
      ts = new APTCommentsFilter(ts);

      StringBuilder sb = new StringBuilder(""); // NOI18N
      try {
        APTToken t = (APTToken) ts.nextToken();
        while (t != null && !APTUtils.isEOF(t)) {
          sb.append(t.getTextID());
          t = (APTToken) ts.nextToken();
        }
      } catch (TokenStreamException ex) {
        Exceptions.printStackTrace(ex);
      }
      return sb.toString();
    }    
}
