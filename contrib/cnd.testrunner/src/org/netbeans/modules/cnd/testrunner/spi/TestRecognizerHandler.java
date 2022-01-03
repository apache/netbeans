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
package org.netbeans.modules.cnd.testrunner.spi;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;

/**
 * Base class for test recognizer handlers. 
 * 
 */
public abstract class TestRecognizerHandler {

    private static final Logger LOGGER = Logger.getLogger(TestRecognizerHandler.class.getName());
    
    protected final Pattern pattern;
    private Matcher matcher;
    protected String line;
    private final boolean performOutput;

    public TestRecognizerHandler(String regex) {
        this(regex, true, false);
    }

    public TestRecognizerHandler(String regex, boolean wrapRegex, boolean performOutput) {
        if (wrapRegex) {
            regex = wrap(regex);
        }
        this.pattern = Pattern.compile(regex, Pattern.DOTALL);
        this.performOutput = performOutput;
    }

    public boolean isPerformOutput() {
        return performOutput;
    }

    public final boolean matches(String line) {
        match(line);
        return matchesImpl(line);
    }

    public Matcher getMatcher() {
        return matcher;
    }

    /**
     * Don't use directly. For compound handlers, which have to poll all
     * underlying to decide if the line mathes or not
     */
    public boolean matchesImpl(String line) {
        return matcher.matches();
    }

    /**
     * <i>Package private for unit tests, otherwise don't use directly</i>.
     */
    public final Matcher match(String line) {
        this.line = line;
        this.matcher = pattern.matcher(line);
        return matcher;
    }

    public abstract void updateUI(Manager manager, TestSession session);

    /**
     * Gets the RecognizedOutput for output that should be passed on 
     * for printing to Output. Override in subclasses as needed, the default
     * implementation processes all output (i.e. everything is passed on
     * for printing).
     * 
     * @return the RecognizedOutput for output that should be passed on
     * for printing to Output. 
     */
    public List<String> getRecognizedOutput() {
        return Collections.singletonList(line);
    }

    protected static int toMillis(String timeInSeconds) {
        try {
            Double elapsedTimeMillis = Double.parseDouble(timeInSeconds) * 1000;
            return elapsedTimeMillis.intValue();
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.WARNING, "Could not parse time, returning 0", nfe);
        }
        return 0;
    }

    protected static String wrap(String string) {
        String regex = string;
        // handle newline chars at the end -- see #143508
        if (!regex.endsWith(".*")) { //NOI18N
            regex += ".*";  //NOI18N
        }
        // see #151725
        if (!regex.startsWith(".*") && !regex.startsWith("(.*)")) { //NOI18N
            regex = ".*" + regex; //NOI18N
        }

        return regex;
    }
}
