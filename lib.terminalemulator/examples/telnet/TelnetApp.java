/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *			
 * Copyright (c) 2012, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import de.mud.telnet.*;

import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermInputListener;
import org.netbeans.lib.terminalemulator.WordDelineator;


public class TelnetApp extends JFrame {
    TermInputListener input_listener;
    Term term;
    TelnetWrapper tio;

    /*
     * Override de.mud.telnet.TelnetWrapper so we can specialize
     * the terminal type.
     */
    static class MyTelnetWrapper extends TelnetWrapper {

	private String terminalType;

	MyTelnetWrapper(String terminalType) {
	    this.terminalType = terminalType;
	} 

	public String getTerminalType() {
	    return terminalType;
	}
    }

    TelnetApp(Font font) {
	super("TelnetApp");

	input_listener = new TermInputListener() {
	    public void sendChar(char c) {
		// System.out.println(c);
		final char tmp[]= new char[1];
		tmp[0] = c;
		String s = new String(tmp);
		try {
		    tio.write(s.getBytes());
		} catch (Exception x) {
		    x.printStackTrace();
		} 
	    }
	    public void sendChars(char[] c, int offset, int count) {
		// System.out.println(c);
		String s = new String(c, offset, count);
		try {
		    tio.write(s.getBytes());
		} catch (Exception x) {
		    x.printStackTrace();
		} 
	    }
	};

	term = new Term();
	term.addInputListener(input_listener);

	term.setEmulation("ansi");
	term.setBackground(Color.white);
	term.setClickToType(false);
	term.setHighlightColor(Color.orange);
	term.setWordDelineator(new WordDelineator() {
	    public int charClass(char c) {
		if (Character.isJavaIdentifierPart(c))
		    return 1;
		else
		    return 0;
	    }
	} );
    } 

    public String terminalType() {
	return term.getEmulation();
    } 

    private void setup_gui() {
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	setContentPane(term);
	pack();
    } 

    private void setup_telnet(String host, String terminalType) {
	tio = new MyTelnetWrapper(terminalType);
	try {
	    System.out.println("connecting to '" + host + "' ...");
	    tio.connect(host, 23);
	} catch (Exception x) {
	    x.printStackTrace();
	} 
    } 

    private void run() {
	byte[] buf = new byte[512];
	int count;
	while (true) {
	    try {
		count = tio.read(buf);
	    } catch (Exception x) {
		x.printStackTrace();
		break;
	    } 
	    if (count == -1)
		break;
	    String s = new String(buf, 0, count);
	    char[] tmp = new char[s.length()];
	    s.getChars(0, s.length(), tmp, 0);
	    term.putChars(tmp, 0, s.length());
	}
	System.exit(0);
    } 

    private static void usage(String msg) {
	System.out.println(msg);
	System.out.println("usage: TelnetApp [ <hostname> ]");
	System.exit(1);
    }

    public static void main(String[] args) {

	String host = "localhost";

	// process cmdline args
	for (int cx = 0; cx < args.length; cx++) {
	    if (args[cx].startsWith("-")) {
		usage("Unrecognized option: " + args[cx]);
	    } else {
		host = args[cx];
	    }
	}

	Font term_font = new Font("Helvetica", Font.PLAIN, 10);
	TelnetApp app = new TelnetApp(term_font);
	app.setup_gui();
	app.setVisible(true);
	app.setup_telnet(host, app.terminalType());
	app.run();
    }
}
