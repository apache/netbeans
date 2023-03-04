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

package org.netbeans.core.execution;

import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;

final class WriterPrintStream extends PrintStream {

    private boolean stdOut;

    /**
     * Create a new print stream.  This stream will not flush automatically.
     *
     * @param  out        The output stream to which values and objects will be
     *                    printed
     *
     */
    public WriterPrintStream(OutputStream out, boolean stdOut) {
	super(out, true);

        this.stdOut = stdOut;
    }

    /** Empty */
    @Override
    public void close() {
    }

    @Override
    public void flush() {
        try {
            if (stdOut) {
                ExecutionEngine.getTaskIOs().getOut().flush();
            } else {
                ExecutionEngine.getTaskIOs().getErr().flush();
            }
        } catch (IOException e) {
            setError();
        }
    }
    
    private void write(String s) {
        try {
            if (stdOut) {
                ExecutionEngine.getTaskIOs().getOut().write(s);
            } else {
                ExecutionEngine.getTaskIOs().getErr().write(s);
            }
        } catch (IOException e) {
            setError();
        }
    }

    /**
     * Print a boolean value.  The string produced by <code>{@link
     * java.lang.String#valueOf(boolean)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      b   The <code>boolean</code> to be printed
     */
    @Override
    public void print(boolean b) {
	write(b ? "true" : "false"); // NOI18N
    }

    /**
     * Print a character.  The character is translated into one or more bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      c   The <code>char</code> to be printed
     */
    @Override
    public void print(char c) {
        try {
            if (stdOut) {
                ExecutionEngine.getTaskIOs().getOut().write(c);
            } else {
                ExecutionEngine.getTaskIOs().getErr().write(c);
            }
        } catch (IOException e) {
            setError();
        }
    }

    /**
     * Print an integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(int)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      i   The <code>int</code> to be printed
     * @see        java.lang.Integer#toString(int)
     */
    @Override
    public void print(int i) {
	write(String.valueOf(i));
    }

    /**
     * Print a long integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(long)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      l   The <code>long</code> to be printed
     * @see        java.lang.Long#toString(long)
     */
    @Override
    public void print(long l) {
	write(String.valueOf(l));
    }

    /**
     * Print a floating-point number.  The string produced by <code>{@link
     * java.lang.String#valueOf(float)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      f   The <code>float</code> to be printed
     * @see        java.lang.Float#toString(float)
     */
    @Override
    public void print(float f) {
	write(String.valueOf(f));
    }

    /**
     * Print a double-precision floating-point number.  The string produced by
     * <code>{@link java.lang.String#valueOf(double)}</code> is translated into
     * bytes according to the platform's default character encoding, and these
     * bytes are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param      d   The <code>double</code> to be printed
     * @see        java.lang.Double#toString(double)
     */
    @Override
    public void print(double d) {
	write(String.valueOf(d));
    }

    /**
     * Print an array of characters.  The characters are converted into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      s   The array of chars to be printed
     * 
     * @throws  NullPointerException  If <code>s</code> is <code>null</code>
     */
    @Override
    public void print(char s[]) {
        try {
            if (stdOut) {
                ExecutionEngine.getTaskIOs().getOut().write(s);
            } else {
                ExecutionEngine.getTaskIOs().getErr().write(s);
            }
        } catch (IOException e) {
            setError();
        }
    }

    /**
     * Print a string.  If the argument is <code>null</code> then the string
     * <code>"null"</code> is printed.  Otherwise, the string's characters are
     * converted into bytes according to the platform's default character
     * encoding, and these bytes are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      s   The <code>String</code> to be printed
     */
    @Override
    public void print(String s) {
	if (s == null) {
	    s = "null"; // NOI18N
	}
	write(s);
    }

    /**
     * Print an object.  The string produced by the <code>{@link
     * java.lang.String#valueOf(Object)}</code> method is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      obj   The <code>Object</code> to be printed
     * @see        java.lang.Object#toString()
     */
    @Override
    public void print(Object obj) {
	write(String.valueOf(obj));
    }


    /* Methods that do terminate lines */

    /**
     * Terminate the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     */
    @Override
    public void println() {
        print(getNewLine());
    }

    /**
     * Print a boolean and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(boolean)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>boolean</code> to be printed
     */
    @Override
    public void println(boolean x) {
        String out = (x ? "true" : "false"); // NOI18N
        write(out.concat(getNewLine()));
    }

    /**
     * Print a character and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(char)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>char</code> to be printed.
     */
    @Override
    public void println(char x) {
        String nline = getNewLine();
        int nlinelen = nline.length();
        char[] tmp = new char[nlinelen + 1];
        tmp[0] = x;
        for (int i = 0; i < nlinelen; i++) {
            tmp[i + 1] = nline.charAt(i);
        }
        try {
            if (stdOut) {
                ExecutionEngine.getTaskIOs().getOut().write(tmp);
            } else {
                ExecutionEngine.getTaskIOs().getErr().write(tmp);
            }
        } catch (IOException e) {
            setError();
        }
    }

    /**
     * Print an integer and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(int)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>int</code> to be printed.
     */
    @Override
    public void println(int x) {
	write(String.valueOf(x).concat(getNewLine()));
    }

    /**
     * Print a long and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(long)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  a The <code>long</code> to be printed.
     */
    @Override
    public void println(long x) {
	write(String.valueOf(x).concat(getNewLine()));
    }

    /**
     * Print a float and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(float)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>float</code> to be printed.
     */
    @Override
    public void println(float x) {
	write(String.valueOf(x).concat(getNewLine()));
    }

    /**
     * Print a double and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(double)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>double</code> to be printed.
     */
    @Override
    public void println(double x) {
	write(String.valueOf(x).concat(getNewLine()));
    }

    /**
     * Print an array of characters and then terminate the line.  This method
     * behaves as though it invokes <code>{@link #print(char[])}</code> and
     * then <code>{@link #println()}</code>.
     *
     * @param x  an array of chars to print.
     */
    @Override
    public void println(char x[]) {
        String nline = getNewLine();
        int nlinelen = nline.length();
        char[] tmp = new char[x.length + nlinelen];
        System.arraycopy(x, 0, tmp, 0, x.length);
        for (int i = 0; i < nlinelen; i++) {
            tmp[x.length + i] = nline.charAt(i);
        }
        x = null;
        try {
            if (stdOut) {
                ExecutionEngine.getTaskIOs().getOut().write(tmp);
            } else {
                ExecutionEngine.getTaskIOs().getErr().write(tmp);
            }
        } catch (IOException e) {
            setError();
        }
    }

    /**
     * Print a String and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>String</code> to be printed.
     */
    @Override
    public void println(String x) {
        if (x == null) {
            x = "null"; // NOI18N
        }
        print(x.concat(getNewLine()));
    }

    /**
     * Print an Object and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(Object)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>Object</code> to be printed.
     */
    @Override
    public void println(Object x) {
        if (x == null) {
	    print("null".concat(getNewLine())); // NOI18N
        } else {
	    String s = x.toString();
	    if(s == null) {
		print("<null>".concat(getNewLine())); // NOI18N
	    } else {
    		print(s.concat(getNewLine()));
	    }
	}
    }
    
    private static String newLine;
    private static String getNewLine() {
        if (newLine == null) {
            newLine = System.getProperty("line.separator");
        }
        return newLine;
    }
}
