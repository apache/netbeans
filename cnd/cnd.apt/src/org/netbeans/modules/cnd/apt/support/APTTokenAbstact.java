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
