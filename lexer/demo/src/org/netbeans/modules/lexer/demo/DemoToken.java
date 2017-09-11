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

package org.netbeans.modules.lexer.demo;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.inc.RawOffsetToken;
import org.netbeans.spi.lexer.util.IntegerCache;

/**
 * Simple token implementation for demo purposes.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DemoToken implements RawOffsetToken, CharSequence {

    private final DemoTokenUpdater tokenUpdater;

    private final TokenId id;

    private int rawOffset;

    private final int length;

    private int lookahead;
    
    private int lookback;
    
    private Object state;

    
    DemoToken(DemoTokenUpdater tokenUpdater, TokenId id, int rawOffset, int length) {
        if (tokenUpdater == null) {
            throw new NullPointerException();
        }
        
        if (id == null) {
            throw new NullPointerException();
        }
        
        if (rawOffset < 0) {
            throw new IllegalArgumentException("rawOffset=" + rawOffset);
        }
        
        if (length < 0) {
            throw new IllegalArgumentException("length=" + length);
        }
        
        this.tokenUpdater = tokenUpdater;
        this.id = id;
        this.rawOffset = rawOffset;
        this.length = length;
    }
    
    protected final DemoTokenUpdater getTokenUpdater() {
        return tokenUpdater;
    }
    
    public TokenId getId() {
        return id;
    }

    public CharSequence getText() {
        return this;
    }
    
    public int getOffset() {
        return tokenUpdater.getOffset(rawOffset);
    }
    
    public int getRawOffset() {
        return rawOffset;
    }
    
    public void setRawOffset(int rawOffset) {
        this.rawOffset = rawOffset;
    }
    
    public void updateRawOffset(int diff) {
        rawOffset += diff;
    }
    
    public int length() {
        return length;
    }
    
    public char charAt(int index) {
        if (index < 0 || index >= length) {
            throw new IllegalStateException("index=" + index + ", length=" + length);
        }

        return tokenUpdater.charAt(rawOffset, index);
    }
    
    public CharSequence subSequence(int start, int end) {
        if (start < 0 || end < 0 || start > end || end > length()) {
            throw new IndexOutOfBoundsException(
                "start=" + start + ", end=" + end + ", length()=" + length());
        }

        return (CharSequence)(Object)toString().substring(start, end); // 1.3 compilability
    }
    
    public int getLookahead() {
        return lookahead;
    }

    void setLookahead(int lookahead) {
        this.lookahead = lookahead;
    }

    public int getLookback() {
        return lookback;
    }
    
    void setLookback(int lookback) {
        this.lookback = lookback;
    }
    
    public Object getState() {
        return state;
    }
    
    void setState(Object state) {
        this.state = state;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length(); i++) {
            sb.append(charAt(i));
        }
        return sb.toString();
    }

}

