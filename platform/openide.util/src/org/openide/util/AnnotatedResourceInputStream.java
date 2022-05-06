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
package org.openide.util;

import java.io.IOException;
import java.io.InputStream;
import static java.util.Objects.requireNonNull;
import static java.lang.System.err;

/*******************************************************************************
 * Wrapper input stream which parses the text as it goes and adds annotations.
 * Resource-bundle values are annotated with their current line number and also
 * the supplied it, so e.g. if in the original input stream on line 50 we have:
 * somekey=somevalue so in the wrapper stream (id 123) this line will read:
 * somekey=somevalue (123:50) Since you see on stderr what #123 is, you can then
 * pinpoint where any bundle key originally came from, assuming NbBundle loaded
 * it from a *.properties file.
 *
 * @see {@link Properties#load} for details on the syntax of *.properties files.
 ******************************************************************************/
final class AnnotatedResourceInputStream extends InputStream {

    // this.state constants
    private static final byte WAITING_FOR_KEY = 0;
    private static final byte IN_COMMENT = 1;
    private static final byte IN_KEY = 2;
    private static final byte IN_KEY_BACKSLASH = 3;
    private static final byte AFTER_KEY = 4;
    private static final byte WAITING_FOR_VALUE = 5;
    private static final byte IN_VALUE = 6;
    private static final byte IN_VALUE_BACKSLASH = 7;

    private final static String I18N = "#I18N"; // NOI18N
    private final static String NOI18N = "#NOI18N"; // NOI18N
    private final static String PARTI18N = "#PARTI18N"; // NOI18N
    private final static String PARTNOI18N = "#PARTNOI18N"; // NOI18N

    private final InputStream baseStream;
    private final int id;
    private final boolean localizable;
    // text of currently read comment, including leading comment character
    private final StringBuilder lastComment = new StringBuilder();
    // text of currently read value, ignoring escapes for now
    private final StringBuilder currentValue = new StringBuilder();
    private final StringBuilder annotation = new StringBuilder();// in reversed order

    private int lineNumber = 0;
    private int keyLine = 0; //line number in effect for last-encountered key
    private byte state = WAITING_FOR_KEY;
    // if true, the last char was a CR, waiting to see if we get a NL too
    private boolean twixtCrAndNl = false;
    // if true, the next value encountered should be localizable if normally it
    // would not be, or vice-versa
    private boolean reverseLocalizable = false;

    /***************************************************************************
     * Create a new InputStream which will annotate resource bundles. Bundles
     * named Bundle*.properties will be treated as localizable by default, and
     * so annotated; other bundles will be treated as nonlocalizable and not
     * annotated. Messages can be individually marked as localizable or not to
     * override this default, in accordance with some I18N conventions for
     * NetBeans.
     *
     * @param base the unannotated stream
     * @param id an identifying number to use in annotations
     * @param localizable if true, this bundle is expected to be localizable
     * @see http://www.netbeans.org/i18n/
     **************************************************************************/
    AnnotatedResourceInputStream(final InputStream base, final int id,
            final boolean localizable) {

        requireNonNull(base);

        this.baseStream = base;
        this.id = id;
        this.localizable = localizable;
    }

    //--------------------------------------------------------------------------
    @Override
    public void close() throws IOException {

        this.baseStream.close();
    }

    //--------------------------------------------------------------------------
    @Override
    public int read() throws IOException {

        if (this.annotation.length() > 0) {
            return popLastFromAnnotation();
        }
        
        final int next = this.baseStream.read();
        if (next == '\n') {
            this.twixtCrAndNl = false;
            this.lineNumber++;
        } else if (next == '\r') {
            if (this.twixtCrAndNl) {
                this.lineNumber++;
            } else {
                this.twixtCrAndNl = true;
            }
        } else {
            this.twixtCrAndNl = false;
        }
        
        switch (this.state) {
            case WAITING_FOR_KEY:
                switch (next) {
                    case '#':
                    case '!':
                        this.state = IN_COMMENT;
                        this.lastComment.setLength(0);
                        this.lastComment.append((char) next);
                        return next;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                    case -1:
                        return next;
                    case '\\':
                        this.state = IN_KEY_BACKSLASH;
                        return next;
                    default:
                        this.state = IN_KEY;
                        this.keyLine = this.lineNumber + 1;
                        return next;
                }
            case IN_COMMENT:
                switch (next) {
                    case '\n':
                    case '\r':
                        final String comment = this.lastComment.toString();

                        if (localizable && comment.equals(NOI18N)) { 
                            this.reverseLocalizable = true;
                        } else if (localizable && comment.equals(PARTNOI18N)) { 
                            err.println("NbBundle WARNING (" + id + ":" + this.lineNumber
                                    + "): #PARTNOI18N encountered, will not annotate I18N parts"); // NOI18N
                            this.reverseLocalizable = true;
                        } else if (!localizable && comment.equals(I18N)) { 
                            this.reverseLocalizable = true;
                        } else if (!localizable && comment.equals(PARTI18N)) { 
                            err.println("NbBundle WARNING (" + id + ":" + this.lineNumber
                                    + "): #PARTI18N encountered, will not annotate I18N parts"); // NOI18N
                            this.reverseLocalizable = false;
                        } else if ((localizable && (comment.equals(I18N) || comment.equals(PARTI18N)))
                                || 
                                (!localizable && (comment.equals(NOI18N) || comment.equals(PARTNOI18N)))) { 
                            err.println("NbBundle WARNING (" + id + ":" + this.lineNumber + 
                                    "): incongruous comment " + comment + " found for bundle"); // NOI18N
                            this.reverseLocalizable = false;
                        }
                        this.state = WAITING_FOR_KEY;
                        return next;
                    default:
                        this.lastComment.append((char) next);
                        return next;
                }
            case IN_KEY:
                switch (next) {
                    case '\\':
                        this.state = IN_KEY_BACKSLASH;
                        return next;
                    case ' ':
                    case '\t':
                        this.state = AFTER_KEY;
                        return next;
                    case '=':
                    case ':':
                        this.state = WAITING_FOR_VALUE;
                        return next;
                    case '\r':
                    case '\n':
                        this.state = WAITING_FOR_KEY;
                        return next;
                    default:
                        return next;
                }
            case IN_KEY_BACKSLASH:
                this.state = IN_KEY;
                return next;
            case AFTER_KEY:
                switch (next) {
                    case '=':
                    case ':':
                        this.state = WAITING_FOR_VALUE;
                        return next;
                    case '\r':
                    case '\n':
                        this.state = WAITING_FOR_KEY;
                        return next;
                    default:
                        return next;
                }
            case WAITING_FOR_VALUE:
                switch (next) {
                    case '\r':
                    case '\n':
                        this.state = WAITING_FOR_KEY;
                        return next;
                    case ' ':
                    case '\t':
                        return next;
                    case '\\':
                        this.state = IN_VALUE_BACKSLASH;
                        return next;
                    default:
                        this.state = IN_VALUE;
                        this.currentValue.setLength(0);
                        return next;
                }
            case IN_VALUE:
                switch (next) {
                    case '\\':
                        // Gloss over distinction between simple escapes and \u1234, which is not important for us.
                        // Also no need to deal specially with continuation lines; for us, there is an escaped
                        // newline, after which will be more value, and that is all that is important.
                        this.state = IN_VALUE_BACKSLASH;
                        return next;
                    case '\n':
                    case '\r':
                    case -1:
                        // End of value. This is the tricky part.
                        boolean revLoc = this.reverseLocalizable;
                        this.reverseLocalizable = false;
                        this.state = WAITING_FOR_KEY;
                        if (localizable ^ revLoc) {
                            // This value is intended to be localizable. Annotate it.
                            prepareReversedAnnotation(next);
                            this.keyLine = 0;
                            // Now return the space before the rest of the string explicitly.
                            return ' ';
                        } else {
                            // This is not supposed to be a localizable value, leave it alone.
                            return next;
                        }
                    default:
                        this.currentValue.append((char) next);
                        return next;
                }
            case IN_VALUE_BACKSLASH:
                this.state = IN_VALUE;
                return next;
            default:
                throw new IOException("Should never happen"); // NOI18N
        }
    }

    //--------------------------------------------------------------------------
    private int popLastFromAnnotation() {

        final char result = this.annotation.charAt(this.annotation.length() - 1);
        this.annotation.setLength(this.annotation.length() - 1);
        return result;
    }

    //--------------------------------------------------------------------------
    private void prepareReversedAnnotation(final int next) {

        this.annotation.append("(").append(id).append(":").append(this.keyLine).
                append(")"); // NOI18N
        if (next != -1) {
            this.annotation.append((char) next);
        }
        this.annotation.reverse();
    }
}
