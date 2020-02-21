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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import java.util.Stack;
import java.io.PrintStream;

import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTerm;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 * Knows how to render stuff into a term and/or a stream for SaveAstext
 */

abstract class Renderer {

    static private class TermRenderer extends Renderer {
	private final ActiveTerm term;
	private final Hyperlink.Resolver resolver;
	private final HyperlinkKeyProcessor hyperlinkProcessor;

	TermRenderer(ActiveTerm term,
		     Hyperlink.Resolver resolver,
		     HyperlinkKeyProcessor hyperlinkProcessor ) {
	    this.term = term;
	    this.resolver = resolver;
	    this.hyperlinkProcessor = hyperlinkProcessor;
	}

	protected void setBold() {
	    term.setAttribute(1);   // bold
	}

	protected void setNormal() {
	    term.setAttribute(0);   // default
	}

	protected void addSeparator() {
	    if (term.getCursorCol() != 0) {
		term.putChar('\r');
		term.putChar('\n');
	    }
	    
	    // How do I get the width?
	    int width = term.getColumns();
	    for (int i = 0; i < width; i++) {
		term.putChar('_');
	    }
	    term.putChar('\r');
	    term.putChar('\n');
	    flush();
	}
	
	protected void append(String str) {
	    final boolean repaint = false;
	    term.appendText(str, repaint);
	}

	protected void flush() {
	    final boolean repaint = true;
	    term.appendText("", repaint);
	}

	public void clear() {
	    term.clearHistory();
	    term.clear(); // Necessary? couldn't quite tell from docs
	}

	private final Stack<ActiveRegion> regions = new Stack<ActiveRegion>();

	protected void beginRegion(String url) {
	    Hyperlink link = new Hyperlink(resolver, url);

	    ActiveRegion region = term.beginRegion(true);
	    region.setSelectable(false);
	    region.setFeedbackEnabled(true);
	    region.setUserObject(link);

	    regions.push(region);
	}

	protected void endRegion(boolean hilite) {
	    term.endRegion();
	    ActiveRegion region = regions.pop();
	    if (hilite && hyperlinkProcessor != null) {
		hyperlinkProcessor.setHyperlink(region);
	    }
	}

    }

    static private class TextRenderer extends Renderer {
	private final PrintStream out;

	TextRenderer(PrintStream out) {
	    this.out = out;
	}

	protected void setBold() {
	}

	protected void setNormal() {
	}

	protected void addSeparator() {
	    out.print("\n________________________________________________________________________________\n"); // NOI18N
	    
	}
	
	protected void append(String str) {
	    out.print(str);
	}

	protected void flush() {
	}

	public void clear() {
	}

	protected void beginRegion(String url) {
	}

	protected void endRegion(boolean hilite) {
	}

    }

    private boolean detailedReport = false;
    private boolean detailedStack = false;

    protected Renderer() {
    }

    public static Renderer newTermRenderer(ActiveTerm term,
					   Hyperlink.Resolver resolver,
					   HyperlinkKeyProcessor hyperlinkProcessor ) {
	return new TermRenderer(term, resolver, hyperlinkProcessor);
    }

    public static Renderer newTextRenderer(PrintStream out) {
	return new TextRenderer(out);
    }

    public void setDetailedStack(boolean detailedStack) {
	this.detailedStack = detailedStack;
    }

    abstract public void clear();

    abstract protected void setBold();

    abstract protected void setNormal();

    abstract protected void addSeparator();
    
    abstract protected void append(String str);

    abstract protected void flush();

    abstract protected void beginRegion(String url);

    abstract protected void endRegion(boolean hilite);

    private void appendN(String str, int count) {
	for (int i = 0; i < count; i++)
	    append(str);
    }

    private String renderAddress(RtcModel.Run run, long address) {
	/* OLD
	if (run.isSixtyFourBit()) {
	    // XXXX Leading zeroes!  16! And do I actually
	    // have a long here?
	    // XXX format as %#016llx
	    return Address.toHexString(address);
	} else {
	    // XXXX leading zeroes!  Format as %#08llx
	    return Address.toHexString((int)address);
	}
	*/
	return Address.toHexString0x(address, run.isSixtyFourBit());
    }

    /**
     * Print out information about one frame
     */
    private void renderFrame(RtcModel.Run run, RtcModel.Frame frame) {

	String text;
	String func;		// or hex address if no name is available

	String st;	// SavedText ... to go into SaveAsText file

	if (frame.func() != null) {
	    text = frame.func();
	    func = text + "()"; // NOI18N
	} else {
	    text = renderAddress(run, frame.pc());
	    func = text;
	}

	if ( ! IpeUtils.isEmpty(frame.source()) ) {
	    // Have source information

	    final String url = "editor:" + frame.source() + ":" +// NOI18N
				frame.lineno() + ":frame";	// NOI18N
	    beginRegion(url);
	    
	    if (detailedStack) {
		text = Catalog.format("FMT_RtcStackDesc1",
				      frame.frameno(),
				      func,
				      frame.lineno(),
				      CndPathUtilities.getBaseName(frame.source()));
	    }
	    append(text);

	    endRegion(false);

	} else {
	    // Only assembly information
	    // SHOULD add hyperlinks for the day we have disassembly view

	    if (detailedStack) {
		text = Catalog.format("FMT_RtcStackDesc2",
				      frame.frameno(), func);
	    }
	    append(text);
	}
    }

    private void renderArrow() {
	setBold();
	append("<-");	// NOI18N
	setNormal();
    }

    /**
     * Print out a stack on one line.
     * accessForm: slightly different formatting for access errors
     */
    private void renderStack(RtcModel.Stack stack, boolean accessForm) {
	int i = 0;
	for (; i < stack.frame().length; i++) {
	    if (accessForm) {
		if (i > 0) {
		    if (detailedStack) {
			append("\n\t");	// NOI18N
		    } else {
			renderArrow();
		    }
		} else {
		    append("\t");		// NOI18N
		}
	    } else {
		if (i > 0) {
		    if (detailedStack) {
			append("\n");	// NOI18N
			if (detailedReport)
			    append("\t");		// NOI18N
			else
			    appendN(" ", 31); // NOI18N
		    } else {
			renderArrow();
		    }
		} else if (detailedReport) {
		    append("\t");		// NOI18N
		}
	    }

	    RtcModel.Frame frame = stack.frame()[i];
	    renderFrame(stack.run(), frame);
	}

	// Flush!
	if (accessForm) {
	    if (i > 0)
		append("\n");		// NOI18N
	} else {
	    append("\n");		// NOI18N
	    if (detailedStack) {
		// otherwise stacks "run" into each other and are hard
		// to distinguish.
		append("\n");		// NOI18N
	    }
	    flush();
	}
    }


    public void renderMemuseItem(RtcModel.MemoryReportItem item) {
	append(item.message());
	renderStack(item.stack(), false);
    }

    public void renderAccessItem(RtcModel.AccessError item) {
	// Separate the items with a ruler
	addSeparator();

	// Main information passed from dbx
	append(item.description());

	renderStack(item.stack(), true);

	String var = item.variableName();
	if (!IpeUtils.isEmpty(var)) {
	    setBold();
	    append(Catalog.format("FMT_VariableIs", var));
	    setNormal();
	}

	//
	// Print error location if given
	//
	append(Catalog.get("ErrorLoc")); // NOI18N

	if ( !IpeUtils.isEmpty(item.location().source()) ) {

	    final String url =
		   "editor:" + item.location().source() + ":" +	// NOI18N
		   item.location().lineno() + ":error";		// NOI18N
	    beginRegion(url);

	    String fileName = CndPathUtilities.getBaseName(item.location().source());

	    String text = Catalog.format("FMT_RtcFileLine",
					 fileName,
					 item.location().lineno());
	    
	    append(text);
	    endRegion(true);
	}

	if (! IpeUtils.isEmpty(item.location().source()) &&
	    ! IpeUtils.isEmpty(item.location().func()) ) {
	    append(", ");	// NOI18N
	}

	if (! IpeUtils.isEmpty(item.location().func()) ) {
	    append(item.location().func() + "()");	// NOI18N
	}

	append("\n\n"); // NOI18N
	flush();
    }

    public void renderRunBegin(RtcModel.Run run, String kind) {
	String path = run.runExecutable();
	if (path == null || path.equals("-")) {     // NOI18N
	    path = Catalog.format("FMT_AttachedProcessPid", run.runPid());
	}
	append(java.text.MessageFormat.format(kind, path));
    }

    public void renderMemoryReportBegin(RtcModel.MemoryReportHeader h) {
	detailedReport = h.verbose();
	setBold();
	append(h.message());
	flush();
	setNormal();
    }

    public void renderAccessEnd() {
	append(Catalog.get("AccessEnded")); // NOI18N
	addSeparator();
    }

    public void renderMemuseEnd() {
	addSeparator();
    }

    public void renderLeaksEnd(boolean isActual) {
	if (isActual)
	    addSeparator();
    }

    public void accessErrorsCleared() {
	append("\n"); // NOI18N
	setBold();
	append(Catalog.get("ClearedAccessErrors"));
	setNormal();
	append("\n"); // NOI18N
    }

    public void reportCleared() {
	append("\n"); // NOI18N
	setBold();
	append(Catalog.get("ClearedReport"));
	setNormal();
	append("\n"); // NOI18N
    }
}
