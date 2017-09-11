/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.impl.actions.clipboardhistory;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

public class ClipboardHistoryElement implements ClipboardOwner{

    private final String content;
    private Transferable transferable = null;
    private static final int MAXSIZE = 30;
    private static final String ENDING = "..."; // NOI18N

    public ClipboardHistoryElement(Transferable transferable, String text) {
        this(text);
        this.transferable = transferable;
    }

    ClipboardHistoryElement(String text) {
        this.content = text;
    }

    public String getShortenText() {
        String output = content.trim();
        if (isShorten()) {
            if (output.length() < MAXSIZE) {
                return output + ENDING;
            } else {
                return output.substring(0, MAXSIZE) + ENDING;
            }
        }
        return output;
        
    }

    public Transferable getTransferable() {
        return transferable;
    }

    public String getFullText() {
        return content;
    }
    
    public boolean isShorten() {
        return content.length() > MAXSIZE || content.trim().isEmpty();
    }

    public String getNumber() {
        return "" + (ClipboardHistory.getInstance().getPosition(this) + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClipboardHistoryElement) {
            return content.equals(((ClipboardHistoryElement) obj).content);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.content != null ? this.content.hashCode() : 0);
        return hash;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
    
    
}
