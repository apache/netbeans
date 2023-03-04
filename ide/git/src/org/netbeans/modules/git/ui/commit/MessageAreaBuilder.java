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
package org.netbeans.modules.git.ui.commit;

import java.awt.Dimension;
import java.awt.Font;
import org.netbeans.modules.versioning.util.common.CommitMessageMouseAdapter;

/**
 *
 * @author Ondrej Vrabec
 */
public class MessageAreaBuilder {
    private String accessibleName;
    private String accessibleDesc;
    private int numberOfChars;
    private int numberOfTitleChars;

    public MessageArea build () {
        MessageArea messageTextArea = new MessageArea();
        if (numberOfChars > 0 || numberOfTitleChars > 0) {
            Font orig = messageTextArea.getFont();
            Font f = Font.decode(Font.MONOSPACED).deriveFont(orig.getStyle(), orig.getSize());
            messageTextArea.setFont(f);
        }
        messageTextArea.setColumns(60); //this determines the preferred width of the whole dialog
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(4);
        messageTextArea.setTabSize(4);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setMinimumSize(new Dimension(100, 18));
        if (accessibleName != null) {
            messageTextArea.getAccessibleContext().setAccessibleName(accessibleName);
        }
        messageTextArea.getAccessibleContext().setAccessibleDescription(accessibleDesc);
        messageTextArea.addMouseListener(new CommitMessageMouseAdapter());
        messageTextArea.setNumberOfTitleChars(numberOfTitleChars);
        messageTextArea.setNumberOfChars(numberOfChars);
        return messageTextArea;
    }

    public MessageAreaBuilder setAccessibleName (String acsn) {
        this.accessibleName = acsn;
        return this;
    }

    public MessageAreaBuilder setAccessibleDescription (String acsd) {
        this.accessibleDesc = acsd;
        return this;
    }

    public MessageAreaBuilder setWraplineHint (int numberOfChars) {
        this.numberOfChars = numberOfChars;
        return this;
    }

    public MessageAreaBuilder setTitleHint (int numberOfTitleChars) {
        this.numberOfTitleChars = numberOfTitleChars;
        return this;
    }
    
}
