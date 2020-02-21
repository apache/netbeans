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

package org.netbeans.modules.cnd.apt.impl.support;

import java.util.List;
import java.util.logging.Level;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 */
public class APTSystemMacroMap extends APTBaseMacroMap {

    private APTMacroMap preMacroMap;
    private final long startCRC;

//    /** Creates a new instance of APTSystemMacroMap */
    protected APTSystemMacroMap(long crc) {
        preMacroMap = new APTPredefinedMacroMap();
        startCRC = crc;
    }

    public APTSystemMacroMap(List<String> sysMacros) {
        this(calculateCRC(sysMacros));
        fill(sysMacros, true);
    }

    @Override
    protected APTMacro createMacro(CharSequence file, APTDefine define, Kind macroType) {
        return new APTMacroImpl(file, define, macroType);
    }

    @Override
    public boolean pushPPDefined() {
        APTUtils.LOG.log(Level.SEVERE, "pushPPDefined is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }

    @Override
    public boolean popPPDefined() {
        APTUtils.LOG.log(Level.SEVERE, "popPPDefined is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }

    @Override
    public boolean pushExpanding(APTToken token) {
        APTUtils.LOG.log(Level.SEVERE, "pushExpanding is not supported", new IllegalAccessException());// NOI18N
        return false;
    }

    @Override
    public void popExpanding() {
        APTUtils.LOG.log(Level.SEVERE, "popExpanding is not supported", new IllegalAccessException());// NOI18N
    }

    @Override
    public boolean isExpanding(APTToken token) {
        APTUtils.LOG.log(Level.SEVERE, "isExpanding is not supported", new IllegalAccessException());// NOI18N
        return false;
    }

    @Override
    public APTMacro getMacro(APTToken token) {
        APTMacro res = super.getMacro(token);

        if(res == null) {
            res = preMacroMap.getMacro(token);
        }
        // If UNDEFINED_MACRO is found then the requested macro is undefined, return null
        return (res != APTMacroMapSnapshot.UNDEFINED_MACRO) ? res : null;
    }

    @Override
    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        assert parent == null : "parent must be null";
        return new APTMacroMapSnapshot((APTMacroMapSnapshot)null);
    }

    @Override
    public void define(APTFile file, APTDefine define, Kind macroType) {
        throw new UnsupportedOperationException("Can not modify immutable System macro map"); // NOI18N
    }

    @Override
    public void undef(APTFile file, APTToken name) {
        throw new UnsupportedOperationException("Can not modify immutable System macro map"); // NOI18N
    }

    //@Override
    public long getCompilationUnitCRC() {
        return startCRC;
    }

    private static long calculateCRC(List<String> sysMacros) {
	Checksum checksum = new Adler32();
	for( String s : sysMacros ) {
             checksum.update(s.getBytes(SupportAPIAccessor.INTERNAL_CHARSET), 0, s.length());
	}
	return checksum.getValue();
    }
}
