/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
