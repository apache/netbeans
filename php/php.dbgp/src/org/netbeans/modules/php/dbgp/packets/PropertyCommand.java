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
public abstract class PropertyCommand extends DbgpCommand {
    private static final String NAME_ARG = "-n "; // NOI18N
    private static final String MAX_SIZE_ARG = "-m "; // NOI18N
    private static final String CONTEXT_ARG = "-c "; // NOI18N
    private static final String DEPTH_ARG = "-d "; // NOI18N
    private static final String PAGE_ARG = "-p "; // NOI18N
    private int myPage;
    private int myContext;
    private int myDepth;
    private String myName;
    private int mySize;

    PropertyCommand(String command, String transactionId) {
        super(command, transactionId);
        myDepth = -1;
        myContext = -1;
        mySize = -1;
        myPage = -1;
    }

    public void setStackDepth(int depth) {
        myDepth = depth;
    }

    public void setContext(int id) {
        myContext = id;
    }

    public void setName(String name) {
        myName = name;
    }

    public void setMaxDataSize(int size) {
        mySize = size;
    }

    public void setDataPage(int page) {
        myPage = page;
    }

    protected String getName() {
        return myName;
    }

    protected int getContext() {
        return myContext;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder(NAME_ARG);
        builder.append(myName);
        setDepth(builder);
        setContext(builder);
        setMaxSize(builder);
        setPage(builder);
        return builder.toString();
    }

    private void setPage(StringBuilder builder) {
        if (myPage == -1) {
            return;
        }
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(PAGE_ARG);
        builder.append(myPage);
    }

    private void setMaxSize(StringBuilder builder) {
        if (mySize == -1) {
            return;
        }
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(MAX_SIZE_ARG);
        builder.append(mySize);
    }

    private void setContext(StringBuilder builder) {
        if (myContext == -1) {
            return;
        }
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(CONTEXT_ARG);
        builder.append(myContext);
    }

    private void setDepth(StringBuilder builder) {
        if (myDepth == -1) {
            return;
        }
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(DEPTH_ARG);
        builder.append(myDepth);
    }

}
