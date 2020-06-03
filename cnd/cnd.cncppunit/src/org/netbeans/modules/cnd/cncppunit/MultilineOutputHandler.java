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
package org.netbeans.modules.cnd.cncppunit;

import java.util.regex.Matcher;
import org.netbeans.modules.cnd.testrunner.spi.TestRecognizerHandler;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;

/**
 *
 */
public abstract class MultilineOutputHandler extends TestRecognizerHandler {

    public abstract void updateUI(Manager manager, TestSession session);

    private class DummyRecognizer extends TestRecognizerHandler {

        public DummyRecognizer(String regex) {
            super(regex);
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
        }
    }

    private String compoundLine;

    private final TestRecognizerHandler startHandler;
    private String startMatch;

    private final TestRecognizerHandler endHandler;

    MultilineOutputHandler(String start, String end) {
//        super(wrap(start) + "|" + wrap(end));
        super(start + end, false, false);

        startHandler = new DummyRecognizer(start);
        endHandler = new DummyRecognizer(end);
    }

    @Override
    public boolean matchesImpl(String line) {
        if (startHandler.matches(line)) {
            if (startMatched()) {
                // Previous one failed
            }
            startMatch = line;
        }
        if (startMatched()) {
            if (!line.equals(startMatch)) {
                compoundLine = startMatch + line;
            } else {
                compoundLine = startMatch;
            }
            if (endHandler.matches(line)) {
//                callback.matched();
                return true;
            }
        }
        return false;
    }

    @Override
    public Matcher getMatcher() {
        if (compoundLine == null) {
            return super.getMatcher();
        } else {
            Matcher matcher = pattern.matcher(compoundLine);
            matcher.matches();
            return matcher;
        }
    }

    private boolean startMatched() {
        return startMatch != null;
    }

    public void reset() {
        startMatch = null;
    }
}
