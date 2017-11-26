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

package org.netbeans.terminal.example.iofeatures;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.netbeans.terminal.example.TerminalIOProviderSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.IOPosition;
import org.openide.windows.IOProvider;
import org.openide.windows.IOTab;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Demonstrate ...
 * <ul>
 * <li>Internal output.
 * <li>IOProvider style hyperlinks.
 * <li>Closing via API.
 * </ul>
 * @author ivan
 */
public final class IOFeaturesAction implements ActionListener {


    private final static String iconOn = "Icon and tip on";
    private final static String iconOff = "Icon and tip off";

    private final static class Tab {
        private OutputWriter ow;
        private InputOutput io;
        private IOPosition.Position label1;
        private IOPosition.Position label2;

	private static IOProvider getIOProvider() {
	    IOProvider iop = IOProvider.get("Terminal");       // NOI18N
	    if (iop == null) {
		System.out.printf("IOProviderActionSupport.getTermIOProvider() couldn't find our provider\n");
		iop = IOProvider.getDefault();
	    }
	    return iop;
	}

        public Tab() {
            // Get a Term-based IOPRovider
            IOProvider iop = getIOProvider();

            io = iop.getIO("TermIOProvider hyperlinks", true);

	    io.select();

            // Adds a line discipline so newlines etc work correctly
            TerminalIOProviderSupport.setInternal(io, true);

            ow = io.getOut();

            try {
                // print some stuff
                ow.println("Hello");

                //
                // exercise IOPosition (part 1)
                //
                label1 = IOPosition.currentPosition(io);
                ow.println("label1: goto label2", outputListener);

                // print some stuff with standard hyperlinks
                ow.println("Press me", outputListener);
                ow.println("Press me too", outputListener);
                ow.println("Close me", outputListener);

                //
                // exercise IOColorLines
                //
                IOColorLines.println(io, "A non hyperlinked green line", Color.GREEN);
                IOColorLines.println(io, "A hyperlinked green line", outputListener, false, Color.GREEN);
                IOColorLines.println(io, "An important hyperlinked green line", outputListener, true, Color.GREEN);

                //
                // exercise tab icon and tooltip.
                //
                IOColorLines.println(io, iconOn, outputListener, false, null);
                IOColorLines.println(io, iconOff, outputListener, false, null);

                //
                // exercise IOColors
                //

                // save original colors
                Color outputColor = IOColors.getColor(io, IOColors.OutputType.OUTPUT);
                Color hyperlinkColor = IOColors.getColor(io, IOColors.OutputType.HYPERLINK);
                Color hyperlinkImportantColor = IOColors.getColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT);

                // set new colors
                IOColors.setColor(io, IOColors.OutputType.OUTPUT, Color.cyan);
                IOColors.setColor(io, IOColors.OutputType.HYPERLINK, Color.magenta);
                IOColors.setColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT, Color.yellow);

                // print some stuff
                ow.format("non-println should be in cyan\n");
                ow.println("Should be in cyan");
                ow.println("Should be in magenta", outputListener);
                ow.println("Should be in yellow", outputListener, true);

                // restore original colors
                IOColors.setColor(io, IOColors.OutputType.OUTPUT, outputColor);
                IOColors.setColor(io, IOColors.OutputType.HYPERLINK, hyperlinkColor);
                IOColors.setColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT, hyperlinkImportantColor);

                //
                // exercise IOPosition (part 2)
                //
                // put some distance between the two labels:
                for (int d = 0; d < 8; d++)
                    ow.println("<distance>");
                label2 = IOPosition.currentPosition(io);
                ow.println("label2: goto label1", outputListener);

                ow.println("Goodbye");

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private final OutputListener outputListener = new OutputListener() {

            public void outputLineSelected(OutputEvent ev) {
                ow.println("Got outputLineSelected()");
                ow.println(String.format("contents: '%s'", ev.getLine()));
            }

            public void outputLineAction(OutputEvent ev) {
                /*
                ow.println("Got outputLineAction()");
                ow.println(String.format("contents: '%s'", ev.getLine()));
                */

                int gotox = ev.getLine().indexOf("goto");
                if ("Close me".equals(ev.getLine())) {
                    ev.getInputOutput().closeInputOutput();

                } else if (iconOn.equals(ev.getLine())) {
                    String iconResource = "org/netbeans/terminal/example/resources/sunsky.png";
                    IOTab.setIcon(io, ImageUtilities.loadImageIcon(iconResource, false));
                    IOTab.setToolTipText(io, "Some tooltip text");

                } else if (iconOff.equals(ev.getLine())) {
                    IOTab.setIcon(io, null);
                    IOTab.setToolTipText(io, null);

                } else if (gotox != -1) {
                    String dst = ev.getLine().substring(gotox + 5);
                    ow.format("\tgoto '%s'\n", dst);
                    if ("label1".equals(dst))
                        label1.scrollTo();
                    else if ("label2".equals(dst))
                        label2.scrollTo();
                } else {
                    ow.println("Got outputLineAction()");
                    ow.println(String.format("contents: '%s'", ev.getLine()));
                }

            }

            public void outputLineCleared(OutputEvent ev) {
                ow.println("Got outputLineEvent()");
                ow.println(String.format("contents: '%s'", ev.getLine()));
            }
        };
    };

    public void actionPerformed(ActionEvent e) {
        new Tab();
    }
}
