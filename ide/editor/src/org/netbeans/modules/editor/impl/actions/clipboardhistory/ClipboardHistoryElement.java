/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
