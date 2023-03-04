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

package org.openide.windows;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import org.openide.util.io.NullOutputStream;
import org.openide.util.io.NullInputStream;

/** An I/O connection to one tab on the Output Window.  To acquire an instance
 * to write to, call, e.g.,
 * <code>IOProvider.getDefault().getInputOutput("someName", false)</code>.
 * To get actual streams to write to, call <code>getOut()</code> or <code>
 * getErr()</code> on the returned instance.
 * <p>
 * Generally it is preferable not to hold a reference to an instance of 
 * {@link org.openide.windows.InputOutput}, but rather to fetch it by name from {@link org.openide.windows.IOProvider} as
 * needed.<p>
 * <b>Note:</b> For historical reasons, the mechanism to clear an output tab
 * is via the method {@link org.openide.windows.OutputWriter#reset}, though it would have
 * made more sense implemented here.
 * 
 * @see OutputWriter
 * @author   Ian Formanek, Jaroslav Tulach, Petr Hamernik, Ales Novak, Jan Jancura
 */
public interface InputOutput {


    /** Null InputOutput */
    /*public static final*/ InputOutput NULL = new InputOutput$Null();

    /** Acquire an output writer to write to the tab.
    * This is the usual use of a tab--it writes to the main output pane.
    * @return the writer
    */
    public OutputWriter getOut();

    /** Get a reader to read from the tab.
    * If a reader is ever requested, an input line is added to the
    * tab and used to read one line at a time.
    * @return the reader
    */
    public Reader getIn();

    /** Get an output writer to write to the tab in error mode.
    * This might show up in a different color than the regular output, e.g., or
    * appear in a separate pane.
    * @return the writer
    */
    public OutputWriter getErr();

    /** Closes this tab.  The effect of calling any method on an instance
     * of InputOutput after calling <code>closeInputOutput()</code> on it is undefined.
     */
    public void closeInputOutput();

    /** Test whether this tab has been closed, either by a call to <code>closeInputOutput()</code>
    * or by the user closing the tab in the UI.
    *
    * @see #closeInputOutput
    * @return <code>true</code> if it is closed
    */
    public boolean isClosed();

    /** Show or hide the standard output pane, if separated. Does nothing in either
    * of the available implementations of this API.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setOutputVisible(boolean value);

    /** Show or hide the error pane, if separated.  Does nothing in either
    * of the available implementations of this API.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setErrVisible(boolean value);

    /** Show or hide the input line.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setInputVisible(boolean value);

    /**
    * Ensure this pane is visible.
    */
    public void select ();

    /** Test whether the error output is mixed into the regular output or not.
    * Always true for both available implementations of this API.
    * @return <code>true</code> if separate, <code>false</code> if mixed in
    */
    public boolean isErrSeparated();

    /** Set whether the error output should be mixed into the regular output or not.
    * Note that this method is optional and is not supported by either of the
    * current implementations of InputOutput (core/output and core/output2).
    * @param value <code>true</code> to separate, <code>false</code> to mix in
    */
    public void setErrSeparated(boolean value);

    /** Test whether the output window takes focus when anything is written to it.
    * @return <code>true</code> if any write to the tab should cause it to gain
    * keyboard focus <strong>(not recommended)</strong>
    */
    public boolean isFocusTaken();

    /** Set whether the output window should take focus when anything is written to it.
    * <strong>Note that this really means the output window will steal keyboard
    * focus whenever a line of output is written.  This is generally an extremely
    * bad idea and strongly recommended against by most UI guidelines.</strong> 
    * @param value <code>true</code> to take focus
    */
    public void setFocusTaken(boolean value);

    /** Flush pending data in the input-line's reader.
    * Called when the reader is about to be reused.
    * @return the flushed reader
    * @deprecated meaningless, does nothing
    */
    @Deprecated
    public Reader flushReader();

    /** @deprecated Use {@link #NULL} instead. */
    @Deprecated
    /*public static final*/ Reader nullReader = InputOutput$Null.NULL_READER;

    /** @deprecated Use {@link #NULL} instead. */
    @Deprecated
    /*public static final*/ OutputWriter nullWriter = InputOutput$Null.NULL_WRITER;

}

@SuppressWarnings("deprecation")
final class InputOutput$Null extends Object implements InputOutput {
    static Reader NULL_READER = new InputStreamReader(new NullInputStream());
    static OutputWriter NULL_WRITER = new InputOutput$NullOutputWriter();
    public InputOutput$Null () {
    }
    
    public OutputWriter getOut() {
        return NULL_WRITER;
    }
    public Reader getIn() {
        return NULL_READER;
    }
    public OutputWriter getErr() {
        return NULL_WRITER;
    }
    public void closeInputOutput() {
    }
    public boolean isClosed() {
        return true;
    }
    public void setOutputVisible(boolean value) {
    }
    public void setErrVisible(boolean value) {
    }
    public void setInputVisible(boolean value) {
    }
    public void select () {
    }
    public boolean isErrSeparated() {
        return false;
    }
    public void setErrSeparated(boolean value) {
    }
    public boolean isFocusTaken() {
        return false;
    }
    public void setFocusTaken(boolean value) {
    }
    public Reader flushReader() {
        return NULL_READER;
    }
}

final class InputOutput$NullOutputWriter extends OutputWriter {
    InputOutput$NullOutputWriter() {
        super(new OutputStreamWriter(new NullOutputStream()));
    }
    public void reset() {
    }
    public void println(String s, OutputListener l) {
    }
}

