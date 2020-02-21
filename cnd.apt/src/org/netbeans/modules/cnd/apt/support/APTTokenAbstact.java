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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public abstract class APTTokenAbstact implements APTToken {
    @Override
    public int getOffset() {return -1;};
    @Override
    public void setOffset(int o) {};
    
    @Override
    public int getEndOffset() {return -1;};
    @Override
    public void setEndOffset(int o) {};
    
    @Override
    public int getEndColumn() {return -1;};
    @Override
    public void setEndColumn(int c) {};
    
    @Override
    public int getEndLine() {return -1;};
    @Override
    public void setEndLine(int l) {};
    
    @Override
    public CharSequence getTextID() {return CharSequences.empty();};
    @Override
    public void setTextID(CharSequence id) {};
    
    @Override
    public int getColumn() {return -1;};
    @Override
    public void setColumn(int c) {};

    @Override
    public int getLine() {return -1;};
    @Override
    public void setLine(int l) {};

    @Override
    public String getFilename() {return null;};
    @Override
    public void setFilename(String name) {};
    
    @Override
    public String getText() {return "<empty>";};// NOI18N
    @Override
    public void setText(String t) {};

    @Override
    public int getType() {return INVALID_TYPE;};
    @Override
    public void setType(int t) {};
    
    @Override
    public String toString() {
        return "[\"" + getText() + "\",<" + APTUtils.getAPTTokenName(getType()) + ">,line=" + getLine() + ",col=" + getColumn() + "]" + ",offset="+getOffset()+",file="+getFilename(); // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final APTTokenAbstact other = (APTTokenAbstact) obj;
        if (this.getType() != other.getType()) {
            return false;
        }
        if (this.getOffset() != other.getOffset()) {
            return false;
        }
        if (!this.getTextID().equals(other.getTextID())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.getType();
        hash = 59 * hash + this.getOffset();
        hash = 59 * hash + this.getTextID().hashCode();
        return hash;
    }
    
    @Override
    public Object getProperty(Object key) {
        return null;
    }    
}
