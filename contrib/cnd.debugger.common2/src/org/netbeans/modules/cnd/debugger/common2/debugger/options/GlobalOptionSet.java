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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import org.openide.ErrorManager;
import org.openide.filesystems.FileStateInvalidException;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.XMLDocReader;
import org.netbeans.modules.cnd.api.xml.XMLDocWriter;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;

import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetSupport;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetXMLCodec;


public class GlobalOptionSet extends OptionSetSupport {

    // MAKE SURE ...
    // Any new options ...
    // - Have their mnemonic flag turned on in constructor
    // - Have a MNEM_ entry in the Bundle file.
    // MAKE SURE ...

    private static final Option[] options = {
	DebuggerOption.SESSION_REUSE,
	//DebuggerOption.MAIN_FUNC_WARNING,
	//DebuggerOption.SUPPRESS_STARTUP_MESSAGE,
	//DebuggerOption.OPTION_EXEC32,

	DebuggerOption.FRONT_IDE,
	DebuggerOption.FRONT_DBGWIN,
	DebuggerOption.FRONT_PIO,
	DebuggerOption.FRONT_DBX,
	DebuggerOption.FRONT_ACCESS,
	DebuggerOption.FRONT_MEMUSE,
	DebuggerOption.OPEN_THREADS,
	DebuggerOption.OPEN_SESSIONS,
	DebuggerOption.FINISH_SESSION,
	DebuggerOption.OUTPUT_LIST_SIZE,
	// DebuggerOption.OUTPUT_MAX_OBJECT_SIZE,

	DebuggerOption.SAVE_BREAKPOINTS,
	// LATER DebuggerOption.SAVE_WATCHES,

	DebuggerOption.TRACE_SPEED,
	DebuggerOption.RUN_AUTOSTART,
	DebuggerOption.BALLOON_EVAL,
        DebuggerOption.ARGS_VALUES_IN_STACK,
        DebuggerOption.DO_NOT_POPUP_DEBUGGER_ERRORS_DIALOG,
    };


    public GlobalOptionSet() {
	setup(options);
    } 

    public GlobalOptionSet(GlobalOptionSet that) {
	setup(options);
	copy(that);
    } 


    @Override
    public OptionSet makeCopy() {
	return new GlobalOptionSet(this);
    }

    //////////////////////////////////////////////////////////////////////
    // XML persistence
    //////////////////////////////////////////////////////////////////////

    /** Have we read in the options already? (reverse logic actually) */
    private boolean needOpen = true;
    
    static class GOXMLWriter extends XMLDocWriter implements UserdirFile.Writer {
	private UserdirFile userdirFile;
	private OptionSetXMLCodec encoder;

	GOXMLWriter(UserdirFile userdirFile, OptionSet optionSet) {
	    this.userdirFile = userdirFile;
	    encoder = new OptionSetXMLCodec(optionSet);
	} 

	// interface UserdirFile.Writer
        @Override
	public void writeTo(OutputStream os)
	    throws IOException, FileStateInvalidException {

	    write(os);
	}

	public void write() throws IOException, FileStateInvalidException {
	    // will call back to writeTo()
	    userdirFile.write(this);
	}

	// interface XMLEncoder
        @Override
	public void encode(XMLEncoderStream xes) {
	    encoder.encode(xes);
	}
    }

    // interface OptionSet
    @Override
    public void save() {
	if (!needSave) {
	    return;
	}
	
	GOXMLWriter xw = new GOXMLWriter(userdirFile, this);
	try {
	    xw.write();
	} catch(Exception e) {
	    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
	}
	needSave = false;
    }
    
    static class GOXMLReader extends XMLDocReader implements UserdirFile.Reader {
	private UserdirFile userdirFile;

	GOXMLReader(UserdirFile userdirFile, OptionSet optionSet) {
	    this.userdirFile = userdirFile;
	    registerXMLDecoder(new OptionSetXMLCodec(optionSet));
	} 

	// interface UserdirFile.Reader
        @Override
	public void readFrom(InputStream is) throws IOException {
	    read(is, "global debugger options"); // NOI18N
	}

	public void read() throws IOException {
	    // will call back to readFrom()
	    userdirFile.read(this);
	}

	// interface XMLDecoder
        @Override
	protected String tag() {
	    return null;
	}

	// interface XMLDecoder
        @Override
	public void start(Attributes atts) {
	}

	// interface XMLDecoder
        @Override
	public void end() {
	}

	// interface XMLDecoder
        @Override
	public void startElement(String name, Attributes atts) {
	}
		
	// interface XMLDecoder
        @Override
	public void endElement(String name, String currentText) {
	}
    }

    private static final String moduleFolderName = "DbxGui";	// NOI18N
    private static final String folderName = "DbxDebugOptions";	// NOI18N
    private static final String filename = "DbxDebugOptions";	// NOI18N

    static final UserdirFile userdirFile =
	new UserdirFile(moduleFolderName, folderName, filename);

    // interface OptionSet
    @Override
    public void open() {
	if (!needOpen) {
	    return;
	}

	GOXMLReader xr = new GOXMLReader(userdirFile, this);
	try {
	    xr.read();
	} catch(Exception e) {
	    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
	}

	needSave = false;
	needOpen = false;
    }

    // interface OptionSet
    @Override
    public String tag() {
	return "GlobalDebugOptions";		// NOI18N
    }

    // interface OptionSet
    @Override
    public String description() {
	return "global debugger options";	// NOI18N
    }
}
