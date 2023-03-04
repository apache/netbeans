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

package org.netbeans.modules.terminal;

import java.awt.Color;
import java.io.IOException;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOPosition;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

public class T5_MTStress_Test extends TestSupport {

    private static final int NCREATORS = 10;
    private static final int NWRITERS = 10;

    public T5_MTStress_Test(String testName) {
	super(testName);
    }


    @Override
    protected void setUp() throws Exception {
	super.setUp(false);
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    private void exercise(InputOutput xio, OutputWriter ow, final int nlines) {

	IOPosition.Position positions[] = new IOPosition.Position[nlines];

	for (int lx = 0; lx < nlines; lx++) {
	    ow.println("Line " + lx + "\r");
	    try {
		IOColorLines.println(xio, "Colored line\r", Color.blue);
		positions[lx] = IOPosition.currentPosition(io);
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}

	sleep(1);
	xio.select();
	for (int lx = 0; lx < nlines; lx+=2) {
	    positions[lx].scrollTo();
	    sleep(1);
	}
    }

    /**
     * Create many IO's in parallel and write to them.
     */
    public void testMultiCreate() {
	System.out.printf("testMultiCreate()\n");

	Thread threads[] = new Thread[NCREATORS];

	for (int tx = 0; tx < NCREATORS; tx++) {
	    final int tn = tx;
	    Thread t = new Thread(new Runnable() {
		InputOutput iot;

		@Override
		public void run() {
		    iot = ioProvider.getIO("test " + tn, null, ioContainer);
		    iot.select();
		    sleep(2);
		    OutputWriter ow = iot.getOut();
		    exercise(iot, ow, 100);
		}
	    });
	    threads[tx] = t;
	    t.start();
	}

	for (int tx = 0; tx < NCREATORS; tx++) {
	    try {
		threads[tx].join();
	    } catch (InterruptedException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}
    }

    /**
     * Create one IO and many writers write to it in parallel.
     */
    public void testMultiWriter() {
	System.out.printf("testMultiWriter()\n");

	final InputOutput iot = ioProvider.getIO("test2", null, ioContainer);
	iot.select();
	sleep(1);

	Thread threads[] = new Thread[NWRITERS];
	for (int tx = 0; tx < NWRITERS; tx++) {
	    final int tn = tx;
	    Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
		    OutputWriter ow = iot.getOut();
		    exercise(iot, ow, 50);
		}
	    });
	    threads[tx] = t;
	    t.start();
	}

	for (int tx = 0; tx < NWRITERS; tx++) {
	    try {
		threads[tx].join();
	    } catch (InterruptedException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}
    }
}