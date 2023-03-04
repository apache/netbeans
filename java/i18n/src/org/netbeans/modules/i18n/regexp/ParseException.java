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


package org.netbeans.modules.i18n.regexp;

/**
 * Singals a syntax error which occured while parsing a regular expression.
 *
 * @author  Marian Petras
 */
public class ParseException extends Exception {

    /** regular expression the syntax error was found in */
    private String regexp;

    /** position of the syntax error within the regular expression */
    private int position;

    /**
     * Constructs a <code>ParseException</code>.
     *
     * @param  regexp  regular expression a syntax error was found in
     * @param  position  position of a syntax error within the regular expression
     */
    public ParseException(String regexp, int position) {
        this.regexp = regexp;
        this.position = position;
    }

    /**
     * Returns a regular expression which caused this exception to be thrown.
     *
     * @return  regular expression containing the syntax error
     */
    public String getRegexp() {
        return regexp;
    }

    /**
     * Returns a position of the syntax error within the regular expression.
     *
     * @return  position of the syntax error within the regular expression
     * @see  #getRegexp()
     */
    public int getPosition() {
        return position;
    }

}
