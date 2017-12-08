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
package nbterm;

import org.netbeans.lib.terminalemulator.TermStream;

/**
 * Utility to inject text into the terminal i/o stream as if it
 * was typed by the user.
 */
class Injector extends TermStream {

    /**
     * Inject text into the terminal i/o stream as if it was typed
     * by the user.
     */
    public void inject(String text) {
        toDCE.sendChars(text.toCharArray(), 0, text.length());
    }

    @Override
    public void flush() {
        toDTE.flush();
    }

    @Override
    public void putChar(char c) {
        toDTE.putChar(c);
    }

    @Override
    public void putChars(char[] buf, int offset, int count) {
        toDTE.putChars(buf, offset, count);
    }

    @Override
    public void sendChar(char c) {
        toDCE.sendChar(c);
    }

    @Override
    public void sendChars(char[] c, int offset, int count) {
        toDCE.sendChars(c, offset, count);
    }
}
