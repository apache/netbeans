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

package org.netbeans.modules.cnd.apt.impl.structure;

import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTEndif;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * #endif directive implementation
 */
public final class APTEndifNode extends APTTokenBasedNode 
                                implements APTNodeBuilder, APTEndif, Serializable {
    private static final long serialVersionUID = 6797353042752788870L;
    
    private int endOffset = 0;

    /** Copy constructor */
    /**package*/APTEndifNode(APTEndifNode orig) {
        super(orig);
    }
    
    /** Constructor for serialization */
    protected APTEndifNode() {
    }
    
    /** Creates a new instance of APTEndifNode */
    public APTEndifNode(APTToken token) {
        super(token);
    }    
    
    @Override
    public final int getType() {
        return APT.Type.ENDIF;
    }
    
    @Override
    public APT getFirstChild() {
        // #endif doesn't have subtree
        return null;
    }

    @Override
    public boolean accept(APTFile curFile,APTToken token) {
        assert (token != null);
        int ttype = token.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE        
        if (APTUtils.isEndDirectiveToken(ttype)) {
            endOffset = token.getOffset();
            return false;
        } else {
            return true;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
      
    @Override
    public void setFirstChild(APT child) {
        // do nothing
        assert (false) : "endif doesn't support children"; // NOI18N
    }
    
    @Override
    public APTBaseNode getNode() {
        return this;
    }
}
