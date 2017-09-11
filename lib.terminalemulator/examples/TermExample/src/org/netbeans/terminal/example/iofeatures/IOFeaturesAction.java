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
