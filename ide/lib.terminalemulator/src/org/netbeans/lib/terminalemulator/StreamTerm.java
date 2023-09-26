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
package org.netbeans.lib.terminalemulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

public class StreamTerm extends Term {

    private boolean connected = false;

    // Objects used with getIn() and getOut()
    private transient Writer writer;      // processes writes from child process
    private Pipe pipe;          // buffers keystrokes to child process

    // Objects used with connect()
    private OutputStreamWriter outputStreamWriter;	// writes to child process
    private InputMonitor stdinMonitor;	// pass keybd input to process stdin
    private OutputMonitor stdoutMonitor;
    private OutputMonitor stderrMonitor;

    /*
     * Return the OutputStreamWriter used for writing to the child.
     *
     * This can be used to send characters to the child process explicitly
     * as if they were typed at the keyboard.
     */
    public OutputStreamWriter getOutputStreamWriter() {
	if (!connected)
	    throw new IllegalStateException("getOutputStreamWriter() can only be used after connect()"); //NOI18N
        return outputStreamWriter;
    }

    /*
     * Transfers typed keystrokes to an OutputStreamWriter which usually
     * passes stuff on to an external process.
     *
     * When the external process goes away writes/flushes will fail with
     * IOException("Bad file number"). There's no way to test for this (e.g.
     * isClosed()) or catch a specific exception about this, so we're just
     * stay quiet about it.
     * Eventually disconnect() will get called and remove us as a listener.
     */
    private static final class InputMonitor implements TermInputListener {
	private final OutputStreamWriter outputStreamWriter;
        private final ExecutorService singlePool = Executors.newSingleThreadExecutor();
	public InputMonitor(OutputStreamWriter outputStreamWriter) {
	    this.outputStreamWriter = outputStreamWriter;
	}
        
        @Override
        public void sendChars(final char c[], final int offset, final int count) {
            final char[] copy = new char[count];
            System.arraycopy(c, offset, copy, 0, count);
            
            singlePool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStreamWriter.write(copy, 0, count);
                        outputStreamWriter.flush();
                    } catch (IOException x) {
                        // no-op
                    } catch (Exception x) {
                        Logger.getLogger(StreamTerm.class.getName()).log(Level.SEVERE, null, x);
                    }
                }
            });
        }

        @Override
        public void sendChar(final char c) {
            singlePool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStreamWriter.write(c);
                        // writer is buffered, need to use flush!
                        // perhaps SHOULD use an unbuffered writer?
                        // Also fix send_chars()
                        outputStreamWriter.flush();
                    } catch (IOException x) {
                        // no-op
                    } catch (Exception x) {
                        Logger.getLogger(StreamTerm.class.getName()).log(Level.SEVERE, null, x);
                    }
                }
            });
        }
    }
    
    public StreamTerm() {
    }

    private static final int BUFSZ = 1024;
    /*
     * Monitor output from process and forward to terminal
     */
    private static final class OutputMonitor extends Thread {
 
        private final char[] buf = new char[BUFSZ];
        private final Term term;
        private final InputStreamReader reader;

        OutputMonitor(InputStreamReader reader, Term term) {
            super("StreamTerm.OutputMonitor");	// NOI18N
            this.reader = reader;
            this.term = term;

            // Fix for bug 4921071
            // NetBeans has many request processors running at P1 so
            // a default priority of this thread will swamp all the RPs
            // if we have a firehose sub-process.
            setPriority(1);
        }

	public void close() {
	    try {
		reader.close();
	    } catch (IOException ex) {
		Logger.getLogger(StreamTerm.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

        private void db_echo_receipt(char buf[], int offset, int count) {
            /*
             * Debugging function
             */
            System.out.println("Received:");	// NOI18N
            final int width = 20;
            int cx = 0;
            while (cx < count) {
                // print numbers
                int cx0 = cx;
		System.out.printf("%4d: ", cx);		// NOI18N
                for (int x = 0; x < width && cx < count; cx++, x++) {
		    System.out.printf("%02x ", (int) buf[offset+cx]);	// NOI18N
                }
                System.out.println();

                // print charcters
                cx = cx0;
                System.out.print("      ");	// NOI18N
                for (int x = 0; x < width && cx < count; cx++, x++) {
                    char c = buf[offset + cx];
                    if (Character.isISOControl(c)) {
                        c = ' ';
                    }
		    System.out.printf("%2c ", c);	// NOI18N
                }
                System.out.println();
            }
        }

        private final class Trampoline implements Runnable {

            public int nread;

	    @Override
            public void run() {
                term.putChars(buf, 0, nread);
            }
        }

        @Override
        public void run() {
            Trampoline tramp = new Trampoline();

            // A note on catching IOExceptions:
            //
            // In general a close of the fd's writing to 'reader' should
            // generate an EOF and cause 'read' to return -1.
            // However, in practice we get miscellaneous bizarre behaviour:
            // - On linux, with non-packet ptys, we get an
            //   "IOException: Input/output" error ultimately from an EIO
            //   returned by read(2).
            //   I suspect this to be a linux bug which hasn't become visible
            //   because single-threaded termulators like xterm on konsole
            //   use poll/select and after detecting an exiting child just
            //   remove the fd from poll and close it etc. I.e. they don't depend 
            //   on read seeing on EOF.
            // At least one java based termulator I've seen also doesn't
            // bother with -1 and silently handles IOException as here.

            try {
                while (true) {
                    int nread = -1;
                    try {
                        nread = reader.read(buf, 0, BUFSZ);
                    } catch (IOException x) {
                    }
                    if (nread == -1) {
                        // This happens if someone closes the input stream,
                        // say the master end of the pty.
			/* When we clean up this gets closed so it's not
                        always an error.
                        System.err.println("com.sun.spro.Term.OutputMonitor: " +	// NOI18N
                        "Input stream closed");inp	// NOI18N
                         */
                        break;
                    }
                    if (term.debugInput()) {
                        db_echo_receipt(buf, 0, nread);
                    }

                    if (false) {
                        term.putChars(buf, 0, nread);
                    } else {
                        // InvokeAndWait() is surprisingly fast and
                        // eliminates one whole set of MT headaches.
                        tramp.nread = nread;
                        SwingUtilities.invokeAndWait(tramp);
                    }
                }
                reader.close();
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }
    /**
     * Connect an I/O stream pair or triple to this Term.
     * Call disconnect() before attempting to connect() again.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     * @param charSet Character set to use for pout and perr.
     *                See InputStreamReader(InputStream, String).
     */
    public void connect(OutputStream pin, InputStream pout, InputStream perr, String charSet) {

	if (connected)
	    throw new IllegalStateException("Cannot call connect() twice"); //NOI18N

        // Now that we have a stream force resize notifications to be sent out.
        updateTtySize();

        if (pin != null) {
            if(charSet == null) {
                outputStreamWriter = new OutputStreamWriter(pin);
            } else {
                try {
                    outputStreamWriter = new OutputStreamWriter(pin, charSet);
                } catch (UnsupportedEncodingException ex) {
                    outputStreamWriter = new OutputStreamWriter(pin);
                }
            }
	    stdinMonitor = new InputMonitor(outputStreamWriter);
	    addInputListener(stdinMonitor);
        }

	if (pout != null) {
	    InputStreamReader pout_reader;
            if (charSet == null) {
                pout_reader = new InputStreamReader(pout);
            } else {
                try {
                    pout_reader = new InputStreamReader(pout, charSet);
                } catch (UnsupportedEncodingException ex) {
                    pout_reader = new InputStreamReader(pout);
                }
            }
	    stdoutMonitor = new OutputMonitor(pout_reader, this);
	    stdoutMonitor.start();
	}

        if (perr != null) {
            InputStreamReader err_reader;
            if (charSet == null) {
                err_reader = new InputStreamReader(perr);
            } else {
                try {
                    err_reader = new InputStreamReader(perr, charSet);
                } catch (UnsupportedEncodingException ex) {
                    err_reader = new InputStreamReader(perr);
                }
            }
            stderrMonitor = new OutputMonitor(err_reader, this);
            stderrMonitor.start();
        }
	connected = true;
    }

    /**
     * Connect an I/O stream pair or triple to this Term.
     * Call disconnect() before attempting to connect() again.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     */
    public void connect(OutputStream pin, InputStream pout, InputStream perr) {
        connect(pin, pout, perr, null);
    }

    private void disconnectWork() {
	if (stdoutMonitor != null) {
	    try {
		stdoutMonitor.join();
	    } catch (InterruptedException ex) {
		Logger.getLogger(StreamTerm.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	if (stderrMonitor != null) {
	    try {
		stderrMonitor.join();
	    } catch (InterruptedException ex) {
		Logger.getLogger(StreamTerm.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	connected = false;
    }

    /**
     * Disconnect previously connected Streams and free resources.
     * Arrange to wait until all pending output from a terminated or exited
     * process has been rendered in the terminal and then call
     * continuation.run() on the EDT thread.
     * Only then can connect() be called again.
     * @param continuation The continuation to run after all output has been
     *        drained.
     */
    public void disconnect(final Runnable continuation) {
	if (!connected) {
	    // System.out.printf("disconnect() called redundantly\n");
	    return;
	}

	if (stdinMonitor != null)
	    removeInputListener(stdinMonitor);

	// Wait until the output monitors exit and then pass on control
	// to the continuation.
	Thread drainer = new Thread() {
	    @Override
	    public void run() {
		disconnectWork();
		if (continuation != null)
		    SwingUtilities.invokeLater(continuation);
	    }
	};
	drainer.start();
	/* Do this way when debugging
	disconnectWork();
	continuation.run();
	 */
    }

    /**
     * Help pass keystrokes to process.
     */
    private static final class Pipe {
        private final PipedReader pipedReader;
        private final PipedWriter pipedWriter;

        private final class TermListener implements TermInputListener {
	    @Override
            public void sendChars(char[] c, int offset, int count) {
                try {
                    pipedWriter.write(c, offset, count);
                    pipedWriter.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

	    @Override
            public void sendChar(char c) {
                try {
                    pipedWriter.write(c);
                    pipedWriter.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        Pipe(Term term) throws IOException {
            pipedReader = new PipedReader();
            pipedWriter = new PipedWriter(pipedReader);

            term.addInputListener(new TermListener());
        }

        Reader reader() {
            return pipedReader;
        }
    }

    /**
     * Delegate writes to a Term.
     */
    private final class TermWriter extends Writer {

        private boolean closed = false;

        TermWriter() {
        }

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
            if (closed)
                throw new IOException();

	    if (SwingUtilities.isEventDispatchThread()) {
		putChars(cbuf, off, len);
	    } else {
		try {
		    SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
			    putChars(cbuf, off, len);
			}
		    });
		} catch (InterruptedException | InvocationTargetException ex) {
		    Logger.getLogger(StreamTerm.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
        }

        @Override
        public void flush() throws IOException {
            if (closed)
                throw new IOException();
            //flush();
        }

        @Override
        public void close() throws IOException {
            if (closed)
                return;
            flush();
            closed = true;
        }
    }


    /**
     * Stream to read from stuff typed into the terminal.
     * @return the reader.
     */
    public Reader getIn() {
        if (pipe == null) {
            try {
                pipe = new Pipe(this);
            } catch (IOException ex) {
                return null;
            }
        }
        return pipe.reader();
    }

    /**
     * Stream to write to stuff being destined for the terminal.
     * @return the writer.
     */
    public Writer getOut() {
        if (writer == null)
            writer = new TermWriter();
        return writer;
    }
}
