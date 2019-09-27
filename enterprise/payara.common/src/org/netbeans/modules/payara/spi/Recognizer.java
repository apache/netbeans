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

package org.netbeans.modules.payara.spi;

import org.openide.windows.OutputListener;

/**
 * Implement this interface to be able to augment server log window processing
 *
 * Useful for enabling links and actions in the output window.  General design
 * inspired by Payara and Ruby/Rails project output window support since
 * V3 needs to support both (and more, e.g. Phobos, Grails, etc.)
 *
 * @author Peter Williams
 */
public interface Recognizer {

    /**
     * Process the text field and return an appropriate OutputListener object
     * if the text field is deemed intersting to this recognizer.
     *
     * @param text field to process
     * @return listener to use with this printed field or null if not interested
     */
    public OutputListener processLine(String text);

}
