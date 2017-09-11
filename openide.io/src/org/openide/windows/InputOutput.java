/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

