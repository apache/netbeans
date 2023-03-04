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
package org.netbeans.modules.jshell.support;

import java.util.List;
import javax.swing.event.ChangeListener;
import jdk.jshell.Snippet;

/**
 *
 * @author sdedic
 */
public interface ShellHistory {
    /**
     * Gets all history items
     * @return 
     */
    public List<Item>   getHistory();
    
    public void         clear();
    
    /**
     * Records an item into the history. Fires change event on history change.
     * 
     * @param item 
     */
    public void         pushItems(List<Item> text);
    
    public void         addChangeListener(ChangeListener l);
    
    public void         removeChangeListener(ChangeListener l);
    
    public final class Item {
        private final Snippet.Kind    kind;
        private final boolean         shellCommand;
        private final String          contents;

        public Item(Snippet.Kind kind, boolean shellCommand, String contents) {
            this.kind = kind;
            this.shellCommand = shellCommand;
            this.contents = contents;
        }

        public Snippet.Kind getKind() {
            return kind;
        }

        public boolean isShellCommand() {
            return shellCommand;
        }

        public String getContents() {
            return contents;
        }
    }
}
