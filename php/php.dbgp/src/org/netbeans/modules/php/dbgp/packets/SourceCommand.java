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
package org.netbeans.modules.php.dbgp.packets;

/**
 * @author ads
 *
 */
public class SourceCommand extends DbgpCommand {
    static final String SOURCE = "source"; // NOI18N
    private static final String FILE_ARG = "-f "; // NOI18N
    private static final String BEGIN_ARG = "-b "; // NOI18N
    private static final String END_ARG = "-e "; // NOI18N
    private String myFileUri;
    private int myBegin;
    private int myEnd;

    public SourceCommand(String transactionId) {
        super(SOURCE, transactionId);
        myBegin = -1;
        myEnd = -1;
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

    public void setFile(String uri) {
        myFileUri = uri;
    }

    public void setBeginLine(int line) {
        myBegin = line;
    }

    public void setEndLine(int line) {
        myEnd = line;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder(FILE_ARG);
        builder.append(myFileUri);
        if (myBegin != -1) {
            builder.append(BrkpntSetCommand.SPACE);
            builder.append(BEGIN_ARG);
            builder.append(myBegin);
        }
        if (myEnd != -1) {
            builder.append(BrkpntSetCommand.SPACE);
            builder.append(END_ARG);
            builder.append(myEnd);
        }
        return builder.toString();
    }

}
