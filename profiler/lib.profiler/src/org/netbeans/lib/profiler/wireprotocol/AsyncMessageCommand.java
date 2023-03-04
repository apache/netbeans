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

package org.netbeans.lib.profiler.wireprotocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Asynchronous message that the server may occasionally send to the client.
 *
 * @author Misha Dmitriev
 */
public class AsyncMessageCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String message;
    private boolean positive;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public AsyncMessageCommand(boolean positive, String message) {
        super(MESSAGE);
        this.positive = positive;

        if (message == null) {
            message = ""; // NOI18N
        }

        this.message = message;
    }

    // Custom serialization support
    AsyncMessageCommand() {
        super(MESSAGE);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getMessage() {
        return message;
    }

    public boolean isPositive() {
        return positive;
    }

    public String toString() {
        return "Async message: " + (isPositive() ? "positive" : "negative") + ", message = " + message; // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        positive = in.readBoolean();
        message = in.readUTF();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(positive);
        out.writeUTF(message);
    }
}
