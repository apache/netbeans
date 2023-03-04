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

package org.netbeans.modules.terminal.ioprovider;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Set;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.Term;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOPosition;
import org.openide.windows.IOTab;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.ui.IOVisibility;

import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.ui.IOTerm;
import org.openide.windows.IOSelect;
import org.netbeans.modules.terminal.ioprovider.Task.ValueTask;
import org.netbeans.modules.terminal.test.IOTest;
import org.openide.util.Exceptions;
//import org.netbeans.modules.terminal.test.IOTest;

/**
 * An implementation of {@link InputOutput} based on
 * {@link org.netbeans.lib.terminalemulator.Term}.
 * <p>
 * This class is public to allow access to the underlying Term.
 * <p>
 * A note on println()'s with OutputListeners:
 * <ul>
 * <li>
 * outputLineAction() works when hyperlinks are clicked.
 * <p>
 * <li>
 * outputLineCleared() didn't make much sense for output2 because output2 had
 * "infinte" history. However, it did make sense when the buffer was cleared.
 * <p>
 * For us issuing Cleared() when the buffer is cleared makes sense but isn't
 * implemented.
 * <br>
 * Issuing Cleared() when a hyperlink scrolls out of the history window
 * also makes sense and is even more work to implement.
 * <li>
 * outputLineSelected() tracked the "caret" in output2. However output2 was
 * "editor" based whereas we're a terminal and a terminals cursor is not
 * a caret ... it doesn't move around that much. (It can move under the
 * control of a program, like vi, but one doesn't generally use hyperlinks
 * in such situations).
 * <p>
 * Term can in principle notify when the cursor is hovering over a hyperlink
 * and perhaps that is the right time to issue Selected().
 * </ul>
 * @author ivan
 */
public final class TerminalInputOutput implements InputOutput, Lookup.Provider {

    private final IOContainer ioContainer;
    private final String name;

    private final Terminal terminal;

    private OutputWriter outputWriter;
    private OutputWriter errWriter;

    // shadow copies in support of IOTab
    private Icon icon;
    private String toolTipText;

    private final Lookup lookup = Lookups.fixed(new MyIOColorLines(),
                                                new MyIOColors(),
                                                new MyIOPosition(),
						new MyIOResizable(),
						new MyIOEmulation(),
						new MyIOTerm(),
                                                new MyIOTab(),
						new MyIOVisibility(),
						new MyIOConnect(),
						new MyIONotifier(),
						new MyIOTest(),
						new MyIOSelect()
                                                );


    private final Map<Color, Integer> colorMap = new HashMap<Color, Integer>();
    private int allocatedColors = 0;

    private final Map<IOColors.OutputType, Color> typeColorMap =
        new EnumMap<IOColors.OutputType, Color>(IOColors.OutputType.class);

    private int outputColor = 0;

    private PropertyChangeSupport pcs;
    private VetoableChangeSupport vcs;

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    /* package */ PropertyChangeSupport pcs() {
	if (pcs == null)
	    pcs = new PropertyChangeSupport(this);
	return pcs;
    }

    /* package */ VetoableChangeSupport vcs() {
	if (vcs == null)
	    vcs = new VetoableChangeSupport(this);
	return vcs;
    }

    /**
     * Convert a Color to an ANSI Term color index.
     * @param color
     * @return
     */
    private int customColor(Color color) {
        if (color == null)
            return -1;

        if (!colorMap.containsKey(color)) {
            if (allocatedColors >= 8)
                return -1;  // ran out of slots for custom colors
	    Task task = new Task.SetCustomColor(terminal, allocatedColors, color);
	    task.post();
	    // +50 was to get old custom colors
	    // +90 now we temporarily use "bright" colors
            colorMap.put(color, (allocatedColors++)+90);
        }
        int customColor = colorMap.get(color);
        return customColor;
    }

    private void setColor(int color) {
	getOut().append((char) 27);
	getOut().append('[');
	getOut().append(Integer.toString(color));
	getOut().append('m');
    }

    private void println(CharSequence text, Color color) {
        int customColor = customColor(color);
        if (customColor == -1) {        // ran out of colors
            getOut().println(text);
        } else {
	    setColor(customColor);
            getOut().println(text);
	    setColor(outputColor);
        }
    }

    private void println(CharSequence text, OutputListener listener, boolean important, Color color) {
        if (color == null) {
            // If color isn't overriden, use default colors.
            if (listener != null) {
                if (important)
                    color = typeColorMap.get(IOColors.OutputType.HYPERLINK_IMPORTANT);
                else
                    color = typeColorMap.get(IOColors.OutputType.HYPERLINK);
            } else {
                // color = typeColorMap.get(IOColors.OutputType.OUTPUT);
            }
        }

        if (listener != null) {
	    // This splitting of the transaction won't work well if we
	    // have two separate threads writing to the same IO.
	    // But everything else willbe garbled as well ... won't it?
	    Task task = new Task.BeginActiveRegion(terminal, listener);
	    task.post();

	    // this println will block!
            println(text, color);

	    task = new Task.EndActiveRegion(terminal);
	    task.post();
        } else {
            println(text, color);
        }
    }

    private class MyIOColorLines extends IOColorLines {
        @Override
        protected void println(CharSequence text, OutputListener listener, boolean important, Color color) {
            TerminalInputOutput.this.println(text, listener, important, color);
        }
    }

    private class MyIOColors extends IOColors {

        @Override
        protected Color getColor(OutputType type) {
            return typeColorMap.get(type);
        }

        @Override
        protected void setColor(OutputType type, Color color) {
            typeColorMap.put(type, color);
            if (type == OutputType.OUTPUT) {
                outputColor = customColor(color);
                if (outputColor == -1)
                    outputColor = 0;
		TerminalInputOutput.this.setColor(outputColor);
            }
        }
    }

    private static class MyPosition implements IOPosition.Position {
        private final Terminal terminal;
        private final Coord coord;

        MyPosition(Terminal terminal, Coord coord) {
            this.terminal = terminal;
            this.coord = coord;
        }

	@Override
        public void scrollTo() {
	    if (coord == null)
		return;
	    Task task = new Task.Scroll(terminal, coord);
	    task.post();
        }
    }

    private class MyIOPosition extends IOPosition {

        @Override
        protected Position currentPosition() {
	    ValueTask<Coord> task = new Task.GetPosition(terminal);
	    task.post();
	    Coord coord = task.get();
            return new MyPosition(TerminalInputOutput.this.terminal, coord);
        }
    }

    private class MyIOTab extends IOTab {

        @Override
        protected Icon getIcon() {
            return icon;
        }

        @Override
        protected void setIcon(Icon icon) {
	    TerminalInputOutput.this.icon = icon;
	    Task task = new Task.SetIcon(ioContainer, terminal, icon);
	    task.post();
        }

        @Override
        protected String getToolTipText() {
	    return toolTipText;
        }

        @Override
        protected void setToolTipText(String text) {
	    TerminalInputOutput.this.toolTipText = text;
	    Task task = new Task.SetToolTipText(ioContainer, terminal, text);
	    task.post();
        }
    }

    /* LATER
    private class MyIOColorPrint extends IOColorPrint {

        private final Map<Color, Integer> colorMap = new HashMap<Color, Integer>();
        private int index = 0;

        public MyIOColorPrint() {
            // preset standard colors
            colorMap.put(Color.black, 30);
            colorMap.put(Color.red, 31);
            colorMap.put(Color.green, 32);
            colorMap.put(Color.yellow, 33);
            colorMap.put(Color.blue, 34);
            colorMap.put(Color.magenta, 35);
            colorMap.put(Color.cyan, 36);
            colorMap.put(Color.white, 37);
        }

        private int customColor(Color color) {
            if (!colorMap.containsKey(color)) {
                if (index >= 8)
                    return -1;  // ran out of slots for custom colors
                term().setCustomColor(index, color);
                colorMap.put(color, (index++)+50);
            }
            int customColor = colorMap.get(color);
            return customColor;

        }

        @Override
        protected void print(CharSequence text, Color color) {
            if ( !(term instanceof ActiveTerm))
                throw new UnsupportedOperationException("Term is not an ActiveTerm");

            int customColor = customColor(color);
            if (customColor == -1) {
                outputWriter.print(text);
            } else {
                term().setAttribute(customColor);
                outputWriter.print(text);
                term().setAttribute(0);
            }
        }
    }
    */

    private static final class MyIOResizable extends IOResizable {
    }

    private class MyIOEmulation extends IOEmulation {

	private boolean disciplined = false;

	@Override
	protected String getEmulation() {
	    // Use ValueTask LATER because emulation is at the
	    // moment an immutable value
	    if (term() == null)
		return "";		// strongly closed
	    else
		return term().getEmulation();
	}

	@Override
	protected boolean isDisciplined() {
	    return disciplined;
	}

	@Override
	protected void setDisciplined() {
	    if (this.disciplined)
		return;
	    this.disciplined = true;
	    if (disciplined) {
		Task task = new Task.SetDisciplined(terminal, disciplined);
		task.post();
	    }
	}
    }

    private class MyIOVisibility extends IOVisibility {

	@Override
	protected void setVisible(boolean visible) {
	    final Task task;
	    if (visible) {
		Set<IOSelect.AdditionalOperation> extraOps =
		    EnumSet.noneOf(IOSelect.AdditionalOperation.class);
		task = new Task.Select(ioContainer, terminal, extraOps);
		task.post();
	    } else {
		task = new Task.DeSelect(ioContainer, terminal);
		task.post();
	    }
	}

	@Override
	protected void setClosable(boolean closable) {
	    Task task = new Task.SetClosable(ioContainer, terminal, closable);
	    task.post();
	}

	@Override
	protected boolean isClosable() {
	    ValueTask<Boolean> task = new Task.IsClosable(terminal);
	    task.post();
	    boolean isClosable = task.get();
            return isClosable;
	}

	@Override
	protected boolean isSupported() {
	    return true;
	    // LATER return ioContainer instanceof TerminalContainerImpl;
	    // We really can't do the above.
	    // However after IOVisibilityControl.isClosable() switches to
	    // the push model we'll be able to answer this question more
	    // accurately by asking ioContainer if it has the IOClosability
	    // capability.
	}
    }

    private class MyIOConnect extends IOConnect {

	@Override
	protected boolean isConnected() {
	    return terminal.isConnected();
	}

	@Override
	protected void disconnectAll(Runnable continuation) {
	    // don't use getOut().close() as convenient as that might be
	    // because getOut() will change states and fire properties.
	    terminal.setOutConnected(false);	// also "closes" Err
	    IOTerm.disconnect(TerminalInputOutput.this, continuation);
	}
    }

    private class MyIOTerm extends IOTerm {

	@Override
	protected Term term() {
	    return terminal.term();
	}

	@Override
	protected void connect(OutputStream pin, InputStream pout, InputStream perr, String charset, Runnable postConnectionTask) {
	    Task task = new Task.Connect(terminal, pin, pout, perr, charset);
	    task.post();
            if (postConnectionTask != null) {
                try {
                    /**
                     * We call invokeAndWait here to be sure that connect is done.
                     * This is because connection is asynchronous operation that
                     * occurs in EDT. return from connect() guarantees that event
                     * was posted to EDT. So our event is queued after that one. So
                     * as soon as our is processed we are sure that connection is
                     * done.
                     */
                    SwingUtilities.invokeAndWait(postConnectionTask);
                } catch (InterruptedException | InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
	}
	
	@Override
	protected void disconnect(final Runnable continuation) {
	    Task task = new Task.Disconnect(terminal, continuation);
	    task.post();
	}

        @Override
        protected void setReadOnly(boolean isReadOnly) {
            term().setReadOnly(isReadOnly);
        }

        @Override
        protected void requestFocus() {
            Term term = term();

            if (term != null) {
                JComponent screen = term.getScreen();
                if (screen != null) {
                    screen.requestFocusInWindow();
                }
            } else { // avoid random NPE in tests
                setFocusTaken(true);
            }
        }
     
        
    }

    private class MyIONotifier extends IONotifier {

	@Override
	protected void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs().addPropertyChangeListener(listener);
	}

	@Override
	protected void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs().removePropertyChangeListener(listener);
	}

	@Override
	public void addVetoableChangeListener(VetoableChangeListener listener ) {
	    vcs().addVetoableChangeListener(listener);
	}

	@Override
	public void removeVetoableChangeListener(VetoableChangeListener listener ) {
	    vcs().removeVetoableChangeListener(listener);
	}
    }

    private class MyIOTest extends IOTest {
	@Override
	protected boolean isQuiescent() {
	    return Task.isQuiescent();
	}

	@Override
	protected void performCloseAction() {
	    // "conditional" close
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    if (!terminal().isClosable())
			return;
		    terminal().close();
		}
	    });

	}
    }

    private class MyIOSelect extends IOSelect {

	@Override
	protected void select(Set<IOSelect.AdditionalOperation> extraOps) {
	    Task task = new Task.Select(ioContainer, terminal, extraOps);
	    task.post();
	}
    }


    /**
     * Delegate prints and writes to a Term via TermWriter.
     */
    private class TermOutputWriter extends OutputWriter {
	private final Terminal owner;

        TermOutputWriter(Terminal owner, Writer writer) {
            super(writer);
	    this.owner = owner;
        }

        @Override
        public void println(String s, OutputListener l) throws IOException {
            TerminalInputOutput.this.println(s, l, false, null);
        }

        @Override
        public void println(String s, OutputListener l, boolean important) throws IOException {
            TerminalInputOutput.this.println(s, l, important, null);
        }

        @Override
        public void reset() throws IOException {
	    Task task = new Task.ClearHistory(terminal);
	    task.post();
        }

	@Override
	public void close() {
	    // Don't really close it
	    // super.close();
	    owner.setOutConnected(false);
	}
    }

    /**
     * Delegate prints and writes to a Term via TermWriter.
     */
    static final Color ERROR_COLOR = new Color(235, 0, 0); // IZ#204301
    private final class TermErrWriter extends OutputWriter {
	private final Terminal owner;
	
	TermErrWriter(Terminal owner, Writer writer) {
	    super(writer);
	    this.owner = owner;
	}

	@Override
	public void println(String s, OutputListener l) throws IOException {
	    TerminalInputOutput.this.println(s, l, false, ERROR_COLOR);
	}

	@Override
	public void println(String s, OutputListener l, boolean important) throws IOException {
	    TerminalInputOutput.this.println(s, l, important, ERROR_COLOR);
	}

	@Override
	public void println(String x) {
	    TerminalInputOutput.this.println(x, ERROR_COLOR);
	}

	@Override
	public void reset() throws IOException {
	    // no-op
	}

	@Override
	public void close() {
	    // Don't really close it
	    // super.close();
	    owner.setErrConnected(false);
	}
    }

    TerminalInputOutput(String name, Action[] actions, IOContainer ioContainer) {
	this.name = name;
        this.ioContainer = ioContainer;

        terminal = new Terminal(ioContainer, this, name);

	Task task = new Task.Add(ioContainer, terminal, actions);
	task.post();

        // preset standard colors
        colorMap.put(Color.black, 30);
        colorMap.put(Color.red, 31);
        colorMap.put(Color.green, 32);
        colorMap.put(Color.yellow, 33);
        colorMap.put(Color.blue, 34);
        colorMap.put(Color.magenta, 35);
        colorMap.put(Color.cyan, 36);
        colorMap.put(Color.white, 37);
    }

    void dispose() {
	if (outputWriter != null) {
	    // LATER outputWriter.dispose();
	    outputWriter = null;
	}
	// LATER getIn().eof();
	// LATER focusTaken = null;
    }


    private StreamTerm term() {
        return terminal.term();
    }

    Terminal terminal() {
        return terminal;
    }

    String name() {
	return name;
    }

    /**
     * Stream to read from stuff typed into the terminal destined for the process.
     * @return the reader.
     */
    @Override
    public Reader getIn() {
	ValueTask<Reader> task = new Task.GetIn(terminal);
	task.post();
	Reader reader = task.get();
	return reader;
    }

    /**
     * Stream to write to stuff being output by the process destined for the
     * terminal.
     * @return the writer.
     */
    @Override
    public OutputWriter getOut() {
	// Ensure we  don't get two of them due to requests on
	// different threads.
	synchronized (this) {
	    if (outputWriter == null) {
		ValueTask<Writer> task = new Task.GetOut(terminal);
		task.post();
		Writer writer = task.get();
		outputWriter = new TermOutputWriter(terminal, writer);
	    }
	}
	terminal.setOutConnected(true);
        return outputWriter;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Output written to this Writer may appear in a different tab (not
     * supported) or different color (easily doable).
     * <p>
     * I'm hesitant to implement this because traditionally separation of
     * stdout and stderr (as done by {@link Process#getErrorStream}) is a dead
     * end. That is why {@link ProcessBuilder}'s redirectErrorStream property is
     * false by default. It is also why
     * {@link org.netbeans.lib.termsupport.TermExecutor#start} will
     * pre-combine stderr and stdout.
     */
    @Override
    public OutputWriter getErr() {
	// Ensure we  don't get two of them due to requests on
	// different threads.
	synchronized (this) {
	    // workaround for #182063: -  UnsupportedOperationException
	    if (errWriter == null) {
		ValueTask<Writer> task = new Task.GetOut(terminal);
		task.post();
		Writer writer = task.get();
		errWriter = new TermErrWriter(terminal, writer);
	    }
	}
	terminal.setErrConnected(true);
	return errWriter;
    }

    @Override
    public void closeInputOutput() {
	// Need to remove it from IOProvider first, doing that from EDT is loo late
	// because we may issue another getIO from the same thread (IZ 199441)
	TerminalIOProvider.remove(this);
	
	if (outputWriter != null)
	    outputWriter.close();
	Task task = new Task.StrongClose(ioContainer, terminal);
	task.post();
    }

    @Override
    public boolean isClosed() {
        return ! terminal.isVisibleInContainer();
    }

    @Override
    public void setOutputVisible(boolean value) {
        // no-op in output2
    }

    @Override
    public void setErrVisible(boolean value) {
        // no-op in output2
    }

    @Override
    public void setInputVisible(boolean value) {
        // no-op
    }

    @Override
    public void select() {
	Set<IOSelect.AdditionalOperation> extraOps =
	    EnumSet.of(IOSelect.AdditionalOperation.OPEN,
		       IOSelect.AdditionalOperation.REQUEST_VISIBLE);
	Task task = new Task.Select(ioContainer, terminal, extraOps);
	task.post();
    }

    @Override
    public boolean isErrSeparated() {
        return false;
    }

    @Override
    public void setErrSeparated(boolean value) {
        // no-op in output2
    }

    @Override
    public boolean isFocusTaken() {
        return false;
    }

    /**
     * output2 considered this to be a "really bad" operation so we will
     * outright not support it.
     */
    @Override
    public void setFocusTaken(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");	// NOI18N
    }

    @Deprecated
    @Override
    public Reader flushReader() {
	return getIn();
    }
}
