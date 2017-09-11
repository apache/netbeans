/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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

    private final static int NCREATORS = 10;
    private final static int NWRITERS = 10;

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