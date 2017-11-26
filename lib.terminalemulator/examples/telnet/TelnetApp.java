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
