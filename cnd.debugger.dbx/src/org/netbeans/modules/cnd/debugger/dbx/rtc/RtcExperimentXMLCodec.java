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

import org.xml.sax.Attributes;

import org.openide.ErrorManager;

import org.netbeans.modules.cnd.api.xml.*;

class RtcExperimentXMLCodec extends XMLDecoder implements XMLEncoder {

    private final RtcModel model;

    // state while decoding
    private static enum Doing {NOTHING, LEAKS, MEMUSE};

    private Doing doing = Doing.NOTHING;
    private RtcModel.AccessError currentAccessError;
    private RtcModel.Stack currentStack;
    private int currentFrameIndex;
    private RtcModel.MemoryReportHeader currentLeaksHeader;
    private RtcModel.MemoryReportHeader currentMemuseHeader;
    private boolean sawInterrupt;
    private RtcModel.MemoryReportItem currentMemoryReportItem;

    private static final String TAG_RTC_EXPERIMENT = "rtc_experiment";// NOI18N
    private static final String TAG_RUN = "run";	// NOI18N
    private static final String TAG_ACCESS_ERRORS = "access_errors"; // NOI18N
    private static final String TAG_ACCESS_ERROR = "access_error"; // NOI18N
    private static final String TAG_LEAK_REPORTS = "leak_reports"; // NOI18N
    private static final String TAG_MEMUSE_REPORTS = "memuse_reports"; // NOI18N
    private static final String TAG_MULTI_MEMORY_REPORT = "multi_memory_report"; // NOI18N
    private static final String TAG_MEMORY_REPORT = "memory_report"; // NOI18N
    private static final String TAG_MEMORY_REPORT_ITEM = "memory_report_item"; // NOI18N
    private static final String TAG_DESCRIPTION = "description"; // NOI18N
    private static final String TAG_MESSAGE = "message"; // NOI18N
    private static final String TAG_STACK = "stack"; // NOI18N
    private static final String TAG_FRAME = "frame"; // NOI18N
    private static final String TAG_LOCATION = "location"; // NOI18N

    private static final String ATTR_EXECUTABLE = "executable"; // NOI18N
    private static final String ATTR_PID = "pid"; // NOI18N
    private static final String ATTR_SIXTYFOUR_BIT = "sixtyfour_bit"; // NOI18N
    private static final String ATTR_A_CLEARED = "a_clearaed"; // NOI18N
    private static final String ATTR_M_CLEARED = "m_clearaed"; // NOI18N
    private static final String ATTR_L_CLEARED = "l_clearaed"; // NOI18N
    private static final String ATTR_ERROR_CODE = "error_code"; // NOI18N
    // OLD private static final String ATTR_DESCRIPTION = "description";
    // OLD private static final String ATTR_MESSAGE = "message";
    private static final String ATTR_ADDRESS = "address"; // NOI18N
    private static final String ATTR_SIZE = "size"; // NOI18N
    private static final String ATTR_COUNT = "count"; // NOI18N
    private static final String ATTR_PERCENTAGE = "percentage"; // NOI18N
    private static final String ATTR_REGION = "region"; // NOI18N
    private static final String ATTR_VARIABLE_NAME = "variable_name"; // NOI18N
    private static final String ATTR_FRAMENO = "frameno"; // NOI18N
    private static final String ATTR_FUNC = "func"; // NOI18N
    private static final String ATTR_ARGS = "args"; // NOI18N
    private static final String ATTR_SOURCE = "source"; // NOI18N
    private static final String ATTR_LINENO = "lineno"; // NOI18N
    private static final String ATTR_PC = "pc"; // NOI18N
    private static final String ATTR_MATCH = "match"; // NOI18N
    private static final String ATTR_LIMIT = "limit"; // NOI18N
    private static final String ATTR_VERBOSE = "verbose"; // NOI18N
    private static final String ATTR_ALL = "all"; // NOI18N
    private static final String ATTR_TOTAL_BYTES = "total_bytes"; // NOI18N
    private static final String ATTR_INTERRUPTED = "interrupted"; // NOI18N

    /**
     * Construct a decoder form.
     */
    RtcExperimentXMLCodec(RtcModel model) {
	this.model = model;
    }

    // interface XMLDecoder
    protected String tag() {
	return TAG_RTC_EXPERIMENT;
    }

    private static int version() {
	return 1;
    }

    // interface XMLDecoder
    public void start(Attributes atts) {
	// nothing to do
    }

    // interface XMLDecoder
    public void end() {
	// nothing to do
    }

    private RtcModel.MemoryReportHeader decodeMemoryReportHeader(Attributes atts) {
	String typeStr = atts.getValue(ATTR_ERROR_CODE);
	// OLD String message = atts.getValue(ATTR_MESSAGE);
	String matchStr = atts.getValue(ATTR_MATCH);
	String limitStr = atts.getValue(ATTR_LIMIT);
	String verboseStr = atts.getValue(ATTR_VERBOSE);
	String allStr = atts.getValue(ATTR_ALL);
	String countStr = atts.getValue(ATTR_COUNT);
	String totalBytesStr = atts.getValue(ATTR_TOTAL_BYTES);
	String interruptedStr = atts.getValue(ATTR_INTERRUPTED);
	sawInterrupt = toBoolean(interruptedStr);

	RtcModel.MemoryReportHeader header =
	    new RtcModel.MemoryReportHeader(toErrorCode(typeStr),
					    // OLD message,
					    "",
					    toInt(matchStr),
					    toInt(limitStr),
					    toBoolean(verboseStr),
					    toBoolean(allStr),
					    toInt(countStr),
					    toLong(totalBytesStr));
	return header;
    }

    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
	if (element.equals(TAG_RUN)) {
	    String executable = atts.getValue(ATTR_EXECUTABLE);
	    String pidStr = atts.getValue(ATTR_PID);
	    String isSixtyFourBitStr = atts.getValue(ATTR_SIXTYFOUR_BIT);
	    RtcModel.Run run = model.runBegin(executable,
			                      toInt(pidStr),
			                      toBoolean(isSixtyFourBitStr));
	    if (toBoolean(atts.getValue(ATTR_A_CLEARED)))
		run.clearAccessErrors();
	    if (toBoolean(atts.getValue(ATTR_M_CLEARED)))
		run.clearMemuseErrors();
	    if (toBoolean(atts.getValue(ATTR_L_CLEARED)))
		run.clearLeakErrors();

	} else if (element.equals(TAG_ACCESS_ERRORS)) {
	    // nothing to do

	} else if (element.equals(TAG_LEAK_REPORTS)) {
	    doing = Doing.LEAKS;

	} else if (element.equals(TAG_MEMUSE_REPORTS)) {
	    doing = Doing.MEMUSE;

	} else if (element.equals(TAG_MULTI_MEMORY_REPORT)) {
	    ;	// nothing to do, the model handles the pairing

	} else if (element.equals(TAG_MEMORY_REPORT)) {
	    switch (doing) {
		case LEAKS:
		    currentLeaksHeader = decodeMemoryReportHeader(atts);
		    model.leaksBegin(currentLeaksHeader);
		    break;
		case MEMUSE:
		    currentMemuseHeader = decodeMemoryReportHeader(atts);
		    model.memuseBegin(currentMemuseHeader);
		    break;
	    }

	} else if (element.equals(TAG_ACCESS_ERROR)) {
	    String typeStr = atts.getValue(ATTR_ERROR_CODE);
	    // OLD String description = atts.getValue(ATTR_DESCRIPTION);
	    String addressStr = atts.getValue(ATTR_ADDRESS);
	    String sizeStr = atts.getValue(ATTR_SIZE);
	    String region = atts.getValue(ATTR_REGION);
	    String variableName = atts.getValue(ATTR_VARIABLE_NAME);

	    currentAccessError =
		new RtcModel.AccessError(toErrorCode(typeStr),
					 // OLD description,
					 "",
					 toLong(addressStr),
					 toLong(sizeStr),
					 region,
					 null,
					 null,
					 variableName);

	} else if (element.equals(TAG_MEMORY_REPORT_ITEM)) {
	    String typeStr = atts.getValue(ATTR_ERROR_CODE);
	    // OLD String message = atts.getValue(ATTR_MESSAGE);
	    String countStr = atts.getValue(ATTR_COUNT);
	    String addressStr = atts.getValue(ATTR_ADDRESS);
	    String sizeStr = atts.getValue(ATTR_SIZE);
	    String percentageStr = atts.getValue(ATTR_PERCENTAGE);

	    currentMemoryReportItem =
		new RtcModel.MemoryReportItem(toErrorCode(typeStr),
					      "",
					      // OLD message,
					      toInt(countStr),
					      toLong(addressStr),
					      toLong(sizeStr),
					      toInt(percentageStr),
					      null);

	} else if (element.equals(TAG_MESSAGE)) {
	    ;	// nothing to do

	} else if (element.equals(TAG_DESCRIPTION)) {
	    ;	// nothing to do

	} else if (element.equals(TAG_STACK)) {
	    String countStr = atts.getValue(ATTR_COUNT);
	    currentStack = model.newStack(toInt(countStr));

	} else if (element.equals(TAG_FRAME)) {
	    String framenoStr = atts.getValue(ATTR_FRAMENO);
	    String func = atts.getValue(ATTR_FUNC);
	    String args = atts.getValue(ATTR_ARGS);
	    String source = atts.getValue(ATTR_SOURCE);
	    String linenoStr = atts.getValue(ATTR_LINENO);
	    String pcStr = atts.getValue(ATTR_PC);

	    RtcModel.Frame frame =
		new RtcModel.Frame(toInt(framenoStr),
				   func,
				   args,
				   source,
				   toInt(linenoStr),
				   toLong(pcStr));
	    if (currentStack != null) {
		currentStack.setFrame(currentFrameIndex++, frame);
	    }

	} else if (element.equals(TAG_LOCATION)) {
	    String func = atts.getValue(ATTR_FUNC);
	    String source = atts.getValue(ATTR_SOURCE);
	    String linenoStr = atts.getValue(ATTR_LINENO);
	    String pcStr = atts.getValue(ATTR_PC);

	    RtcModel.Location location = 
		new RtcModel.Location(func,
				      source,
				      toInt(linenoStr),
				      toLong(pcStr));
	    if (currentAccessError != null)
		currentAccessError.setLocation(location);

	} else {
	    System.out.printf("RtcExperimentXMLCodec.startElement(%s): " + // NOI18N
			      "NOT IMPLEMENTED\n", // NOI18N
			      element);
	}
    }


    // interface XMLDecoder
    public void endElement(String element, String currentText) {
	if (element.equals(TAG_RUN)) {
	    model.runEnd();

	} else if (element.equals(TAG_ACCESS_ERRORS)) {
	    // nothing to do

	} else if (element.equals(TAG_LEAK_REPORTS)) {
	    doing = Doing.NOTHING;

	} else if (element.equals(TAG_MEMUSE_REPORTS)) {
	    doing = Doing.NOTHING;

	} else if (element.equals(TAG_MULTI_MEMORY_REPORT)) {
	    ;	// nothing to do, the model handles the pairing

	} else if (element.equals(TAG_MEMORY_REPORT)) {
	    switch (doing) {
		case LEAKS:
		    if (currentLeaksHeader != null) {
			if (sawInterrupt) {
			    model.leaksInterrupted();
			    sawInterrupt = false;
			} else {
			    model.leaksEnd();
			}
			currentLeaksHeader = null;
		    }
		    break;
		case MEMUSE:
		    if (currentMemuseHeader != null) {
			if (sawInterrupt) {
			    model.memuseInterrupted();
			    sawInterrupt = false;
			} else {
			    model.memuseEnd();
			}
			currentMemuseHeader = null;
		    }
		    break;
	    }

	} else if (element.equals(TAG_MEMORY_REPORT_ITEM)) {
	    if (currentLeaksHeader != null)
		model.leakItem(currentMemoryReportItem);
	    else if (currentMemuseHeader != null)
		model.memuseItem(currentMemoryReportItem);
	    currentMemoryReportItem = null;

	} else if (element.equals(TAG_ACCESS_ERROR)) {
	    model.accessItem(currentAccessError);
	    currentAccessError = null;

	} else if (element.equals(TAG_DESCRIPTION)) {
	    if (currentAccessError != null)
		currentAccessError.setDescription(currentText);

	} else if (element.equals(TAG_MESSAGE)) {
	    // memory report items occur inside TAG_HEADER's
	    if (currentMemoryReportItem != null)
		currentMemoryReportItem.setMessage(currentText);

	    else if (currentLeaksHeader != null)
		currentLeaksHeader.setMessage(currentText);
	    else if (currentMemuseHeader != null)
		currentMemuseHeader.setMessage(currentText);

	} else if (element.equals(TAG_STACK)) {
	    if (currentAccessError != null)
		currentAccessError.setStack(currentStack);
	    else if (currentMemoryReportItem != null)
		currentMemoryReportItem.setStack(currentStack);
	    currentStack = null;
	    currentFrameIndex = 0;

	} else if (element.equals(TAG_FRAME)) {
	    // nothing to do

	} else if (element.equals(TAG_LOCATION)) {
	    // nothing to do

	} else {
	    System.out.printf("RtcExperimentXMLCodec.endElement(%s): " + // NOI18N
			      "NOT IMPLEMENTED\n", // NOI18N
			      element);
	}
    }


    private static String str(int i) {
	return Integer.toString(i);
    }

    private static int toInt(String iStr) {
	try {
	    return Integer.parseInt(iStr);
	} catch (NumberFormatException x) {
	    ErrorManager.getDefault().annotate(x, "Bad int: " + iStr); // NOI18N
	    ErrorManager.getDefault().notify(ErrorManager.WARNING, x);
	    return 0;
	}
    }

    private static String str(long l) {
	return Long.toString(l);
    }

    private static long toLong(String lStr) {
	try {
	    return Long.parseLong(lStr);
	} catch (NumberFormatException x) {
	    ErrorManager.getDefault().annotate(x, "Bad int: " + lStr); // NOI18N
	    ErrorManager.getDefault().notify(ErrorManager.WARNING, x);
	    return 0;
	}
    }

    private static String str(RtcModel.ErrorCode code) {
	return code.name();
    }

    private static RtcModel.ErrorCode toErrorCode(String typeStr) {
	try {
	    return Enum.valueOf(RtcModel.ErrorCode.class, typeStr);
	} catch (IllegalArgumentException x) {
	    return RtcModel.ErrorCode.UNKNOWN;
	}
    }

    private static String str(boolean b) {
	return b? "true": "false"; // NOI18N
    }

    private static boolean toBoolean(String bStr) {
	if ("true".equals(bStr)) // NOI18N
	    return true;
	else if ("false".equals(bStr)) // NOI18N
	    return false;
	else
	    return false;		// SHOUOLD throw an exception?
    }

    private static String str(String s) {
	if (s == null)
	    return "";
	else
	    return s;
    }

    private void encodeLocation(XMLEncoderStream xes,
				   RtcModel.Location location) {
	AttrValuePair attrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_FUNC, location.func()),
	    new AttrValuePair(ATTR_SOURCE, location.source()),
	    new AttrValuePair(ATTR_LINENO, str(location.lineno())),
	    new AttrValuePair(ATTR_PC, str(location.pc()))
	};

	xes.element(TAG_LOCATION, attrs);
    }

    private void encodeFrame(XMLEncoderStream xes,
				   RtcModel.Frame frame) {
	AttrValuePair attrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_FRAMENO, str(frame.frameno())),
	    new AttrValuePair(ATTR_FUNC, str(frame.func())),
	    new AttrValuePair(ATTR_ARGS, str(frame.args())),
	    new AttrValuePair(ATTR_SOURCE, str(frame.source())),
	    new AttrValuePair(ATTR_LINENO, str(frame.lineno())),
	    new AttrValuePair(ATTR_PC, str(frame.pc()))
	};

	xes.element(TAG_FRAME, attrs);
    }

    private void encodeStack(XMLEncoderStream xes,
				   RtcModel.Stack stack) {
	AttrValuePair attrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_COUNT, str(stack.frame().length))
	};
	xes.elementOpen(TAG_STACK, attrs);
	for (RtcModel.Frame f : stack.frame())
	    encodeFrame(xes, f);
	xes.elementClose(TAG_STACK);
    }

    private void encodeDescription(XMLEncoderStream xes, String description) {
	// This form causes NPE
	// xes.element(TAG_DESCRIPTION, description);
	AttrValuePair attrs[] = new AttrValuePair[0];
	xes.element(TAG_DESCRIPTION, -1, attrs, description);
    }

    private void encodeMessage(XMLEncoderStream xes, String message) {
	xes.element(TAG_MESSAGE, message);
    }

    private void encodeAccessError(XMLEncoderStream xes,
				   RtcModel.AccessError item) {

	AttrValuePair attrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_ERROR_CODE, str(item.type())),
	    // OLD new AttrValuePair(ATTR_DESCRIPTION, item.description()),
	    new AttrValuePair(ATTR_ADDRESS, str(item.address())),
	    new AttrValuePair(ATTR_SIZE, str(item.size())),
	    new AttrValuePair(ATTR_REGION, str(item.region())),
	    new AttrValuePair(ATTR_VARIABLE_NAME, str(item.variableName()))
	};
	xes.elementOpen(TAG_ACCESS_ERROR, attrs);
	    encodeDescription(xes, item.description());
	    encodeStack(xes, item.stack());
	    encodeLocation(xes, item.location());
	xes.elementClose(TAG_ACCESS_ERROR);
    }

    private void encodeMemoryReportItem(XMLEncoderStream xes,
					RtcModel.MemoryReportItem item) {
	AttrValuePair attrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_ERROR_CODE, str(item.type())),
	    // OLD new AttrValuePair(ATTR_MESSAGE, str(item.message())),
	    new AttrValuePair(ATTR_COUNT, str(item.count())),
	    new AttrValuePair(ATTR_ADDRESS, str(item.address())),
	    new AttrValuePair(ATTR_SIZE, str(item.size())),
	    new AttrValuePair(ATTR_PERCENTAGE, str(item.percentage())),
	};

	xes.elementOpen(TAG_MEMORY_REPORT_ITEM, attrs);
	    encodeMessage(xes, str(item.message()));
	    encodeStack(xes, item.stack());
	xes.elementClose(TAG_MEMORY_REPORT_ITEM);
    }

    private void encodeMemoryReport(XMLEncoderStream xes,
				    RtcModel.MemoryReport report) {
	RtcModel.MemoryReportHeader header = report.header();

	AttrValuePair attrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_ERROR_CODE, str(header.type())),
	    // OLD new AttrValuePair(ATTR_MESSAGE, str(header.message())),
	    new AttrValuePair(ATTR_MATCH, str(header.match())),
	    new AttrValuePair(ATTR_LIMIT, str(header.limit())),
	    new AttrValuePair(ATTR_VERBOSE, str(header.verbose())),
	    new AttrValuePair(ATTR_ALL, str(header.all())),
	    new AttrValuePair(ATTR_COUNT, str(header.count())),
	    new AttrValuePair(ATTR_TOTAL_BYTES, str(header.totalBytes())),
	    new AttrValuePair(ATTR_INTERRUPTED, str(report.isInterrupted())),
	};

	xes.elementOpen(TAG_MEMORY_REPORT, attrs);
	encodeMessage(xes, str(header.message()));
	for (RtcModel.MemoryReportItem item : report.items())
	    encodeMemoryReportItem(xes, item);
	xes.elementClose(TAG_MEMORY_REPORT);
    }

    private void encodeRun(XMLEncoderStream xes, RtcModel.Run run) {
	AttrValuePair attrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_EXECUTABLE, str(run.runExecutable())),
	    new AttrValuePair(ATTR_PID, str(run.runPid())),
	    new AttrValuePair(ATTR_SIXTYFOUR_BIT, str(run.isSixtyFourBit())),
	    new AttrValuePair(ATTR_A_CLEARED, str(run.isAccessErrorsCleared())),
	    new AttrValuePair(ATTR_M_CLEARED, str(run.isMemuseErrorsCleared())),
	    new AttrValuePair(ATTR_L_CLEARED, str(run.isLeakErrorsCleared())),
	};

	xes.elementOpen(TAG_RUN, attrs);

	xes.elementOpen(TAG_ACCESS_ERRORS);
	for (RtcModel.AccessError item : run.accessErrors())
	    encodeAccessError(xes, item);
	xes.elementClose(TAG_ACCESS_ERRORS);

	xes.elementOpen(TAG_LEAK_REPORTS);
	for (RtcModel.MultiLeaksReport leakReport : run.leakReports()) {
	    xes.elementOpen(TAG_MULTI_MEMORY_REPORT);
		if (leakReport.detailedReport() != null)
		    encodeMemoryReport(xes, leakReport.detailedReport());
		if (leakReport.summaryReport() != null)
		    encodeMemoryReport(xes, leakReport.summaryReport());
		if (leakReport.detailedPossiblesReport() != null)
		    encodeMemoryReport(xes, leakReport.detailedPossiblesReport());
		if (leakReport.summaryPossiblesReport() != null)
		    encodeMemoryReport(xes, leakReport.summaryPossiblesReport());
	    xes.elementClose(TAG_MULTI_MEMORY_REPORT);
	}
	xes.elementClose(TAG_LEAK_REPORTS);

	xes.elementOpen(TAG_MEMUSE_REPORTS);
	for (RtcModel.MultiMemuseReport memuseReport : run.memuseReports()) {
	    xes.elementOpen(TAG_MULTI_MEMORY_REPORT);
		if (memuseReport.detailedReport() != null)
		    encodeMemoryReport(xes, memuseReport.detailedReport());
		if (memuseReport.summaryReport() != null)
		    encodeMemoryReport(xes, memuseReport.summaryReport());
	    xes.elementClose(TAG_MULTI_MEMORY_REPORT);
	}
	xes.elementClose(TAG_MEMUSE_REPORTS);

	xes.elementClose(TAG_RUN);
    }

    // pseudo-interface XMLEncoder
    public void encode(XMLEncoderStream xes) {
	xes.elementOpen(TAG_RTC_EXPERIMENT, version());
	for (RtcModel.Run run : model.runs())
	    encodeRun(xes, run);
	xes.elementClose(TAG_RTC_EXPERIMENT);
    }
}
