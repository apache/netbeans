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

import java.util.ArrayList;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;

import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileSystem;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * Notifications to RtcView
 */

public class RtcModel {

    static class Completable {
	boolean complete = false;

	void setComplete() {
	    complete = true;
	}

	boolean isComplete() {
	    return complete;
	}
    }

    /*package*/ static class Location {
	private final String func;
	private final String source;
	private final int lineno;
	private final long pc;

	public Location(String func,
			String source,
			int lineno,
			long pc) {
	    this.func = func;
	    this.source = source;
	    this.lineno = lineno;
	    this.pc = pc;
	}

	public String func() { return func; }
	public String source() { return source; }
	public int lineno() { return lineno; }
	public long pc() { return pc; }
    }

    static public class Frame extends Location {
	private final String args;
	private final int frameno;

	public Frame(int frameno, 
		     String func,
		     String args,
		     String source,
		     int lineno,
		     long pc) {
	    super(func, source, lineno, pc);
	    this.frameno = frameno;
	    this.args = args;
	}

	public int frameno() { return frameno; }
	public String args() { return args; }
    }

    /*package*/ static class Stack {
	private final Frame frame[];
	private final Run run;

	Stack(Run run, int nframes) {
	    this.run = run;
	    frame = new Frame[nframes];
	}

	/*package*/ void setFrame(int fx, Frame f) {
	    frame[fx] = f;
	}

	/*package*/ Frame[] frame() { return frame; }

	/*package*/ Run run() { return run; }
    }

    public static enum ErrorCode {
	ADDRESS_IN_BLOCK,	// 0
	ADDRESS_IN_REGISTER,	// 1
        BAD_FREE,		// 2
        BLOCK_INUSE,		// 3
        DUPLICATE_FREE,		// 4
        MEMORY_LEAK,		// 5
        MISALIGNED_FREE,	// 6
        MISALIGNED_READ,	// 7
        MISALIGNED_WRITE,	// 8
        OUT_OF_MEMORY,		// 9
        READ_FROM_UNALLOCATED,	// 10
        READ_FROM_UNINITIALIZED,// 11
        WRITE_TO_READ_ONLY,	// 12
        WRITE_TO_UNALLOCATED,	// 13
        WRITE_TO_OUT_OF_BOUND,	// 14
        READ_FROM_OUT_OF_BOUND,	// 15
        UNKNOWN;		// 16
    }

    // aka MprofHeader
    static public class MemoryReportHeader {

	private final ErrorCode type;		// one of:
					// BLOCK_INUSE
					// MEMORY_LEAK
					// ADDRESS_IN_BLOCK (possible leak)

	private String message;		// aka buf

	// the following are settings in effect at the time of the report
	private final int match;		// -m
	private final int limit;		// -n
	private final boolean verbose;	// -v
	private final boolean all;		// -a (as opposed to incremental)

	private final int count;		// ... of items
	private final long totalBytes;

	public MemoryReportHeader(ErrorCode type,
	                          String message,
				  int match,
				  int limit,
				  boolean verbose,
				  boolean all,
				  int count,
				  long totalBytes) {

	    this.type = type;
	    this.message = message;
	    this.match = match;
	    this.limit = limit;
	    this.verbose = verbose;
	    this.all = all;
	    this.count = count;
	    this.totalBytes = totalBytes;
	}


	public ErrorCode type() { return type; }
	public String message() { return message; }
	public int match() { return match; }
	public int limit() { return limit; }
	public boolean verbose() { return verbose; }
	public boolean all() { return all; }
	public int count() { return count; }
	public long totalBytes() { return totalBytes; }

	// for use by XML decoder:

	void setMessage(String message) {
	    this.message = message;
	}
    }

    // aka MprofItem
    public static class MemoryReportItem {
	private final ErrorCode type;
	private String message;
	private final int count;		// ... of blocks allocated at this stack
	private final long address;		// of allocated/leaked block
					// if more than one block, address of 
					// first one?
	private final long size;		// of allocated/leaked block
	private final int percentage;		// only for BLOCK_INUSE
	private Stack stack;		// ... at the time of allocation

	/*package*/ MemoryReportItem(ErrorCode type,
				String message,
				int count,
				long address,
				long size,
				int percentage,
				Stack stack) {
	    this.type = type;
	    this.message = message;
	    this.count = count;
	    this.address = address;
	    this.size = size;
	    this.percentage = percentage;
	    this.stack = stack;
	}

	public ErrorCode type() { return type; }
	public String message() { return message; }
	public int count() { return count; }
	public long address() { return address; }
	public long size() { return size; }
	public int percentage() { return percentage; }
	/*package*/ Stack stack() { return stack; }

	// for use by XML decoder:

	void setStack(Stack stack) {
	    this.stack = stack;
	}

	void setMessage(String message) {
	    this.message = message;
	}
    }

    static class MemoryReport extends Completable {
	private final MemoryReportHeader header;
	private final ArrayList<MemoryReportItem> items;
	private boolean interrupted;

	MemoryReport(MemoryReportHeader header) {
	    this.header = header;
	    items = new ArrayList<MemoryReportItem>();
	}

	void interrupt() {
	    interrupted = true;
	}

	public boolean isInterrupted() { return interrupted; }

	public boolean isDetailed() { return header.verbose(); }
	public boolean isSummary() { return !header.verbose(); }
	public boolean isVerbose() { return header.verbose(); }	// aka isDetailed
	public boolean isActualLeaks() {
	    return header.type() == RtcModel.ErrorCode.MEMORY_LEAK;
	}

	public boolean isPossibleLeaks() {
	    // I don't think we get ADDRESS_IN_REGISTER as the type for a
	    // whole report.
	    return header.type() == RtcModel.ErrorCode.ADDRESS_IN_BLOCK ||
		   header.type() == RtcModel.ErrorCode.ADDRESS_IN_REGISTER;
	}

	public MemoryReportHeader header() { return header; }
	public ArrayList<MemoryReportItem> items() { return items; }
    }

    /**
     * Common subclass to MultiMemuseReport and MultiLeaksReport.
     */
    static private abstract class MultiReport {
	protected MemoryReport summaryReport;
	protected MemoryReport detailedReport;

	public MemoryReport summaryReport() {
	    return summaryReport;
	}

	public MemoryReport detailedReport() {
	    return detailedReport;
	}

	/**
	 * Is there room in 'this' for 'report'?
	 * If true is returned this.add(report) is guaranteed to succeed.
	 * Returning false requires the creation of a new MultiReport.
	 */

	public abstract boolean roomFor(MemoryReport report);

	/**
	 * Is there room in 'mr' for 'report'?
	 * If true is returned mr.add(report) is guaranteed to suceed.
	 * Returning false requires the creation of a new MultiReport.
	 * If 'mr' is null we also return false in order to force creation of
	 * a new report. That is why this method is static.
	 */
	public static boolean roomInFor(MultiReport mr,
					MemoryReport report) {

	    if (mr == null)
		return false;	// force creation of new one
	    return mr.roomFor(report);
	}

	/**
	 * Return true if all slots have been fillled.
	 */

	public abstract boolean isFull();

	public abstract void add(MemoryReport report);
    }

    static class MultiMemuseReport extends MultiReport {

	// implement MultiReport
	public boolean roomFor(MemoryReport report) {
	    if (report.isVerbose()) {
		// needs to go into the detailed slot
		if (detailedReport == null)
		    return true;	// there's room for it
		else
		    return false;
	    } else {
		// needs to go into the summary slot
		if (summaryReport == null)
		    return true;	// there's room for it
		else
		    return false;
	    }
	}

	/**
	 * Return true if both the detailed and summary slots have been fillled.
	 */

	// implement MultiReport
	public boolean isFull() {
	    return detailedReport != null && summaryReport != null;
	}

	// implement MultiReport
	public void add(MemoryReport report) {
	    if (report.isVerbose()) {
		// needs to go into the detailed slot
		assert detailedReport == null :
		       "MultiMemuseReport.add(): detailed slot is occupied"; // NOI18N
		detailedReport = report;
	    } else {
		// needs to go into the summary slot
		assert summaryReport == null :
		       "MultiMemuseReport.add(): summary slot is occupied"; // NOI18N
		summaryReport = report;
	    }
	}
    }

    static class MultiLeaksReport extends MultiReport {
	private MemoryReport summaryPossiblesReport;
	private MemoryReport detailedPossiblesReport;

	public MemoryReport summaryPossiblesReport() {
	    return summaryPossiblesReport;
	}

	public MemoryReport detailedPossiblesReport() {
	    return detailedPossiblesReport;
	}

	// implement MultiReport
	public boolean roomFor(MemoryReport report) {
	    if (report.isActualLeaks()) {
		if (report.isVerbose()) {
		    // needs to go into the detailed slot
		    if (detailedReport == null)
			return true;	// there's room for it
		    else
			return false;
		} else {
		    // needs to go into the summary slot
		    if (summaryReport == null)
			return true;	// there's room for it
		    else
			return false;
		}
	    } else if (report.isPossibleLeaks()){
		if (report.isVerbose()) {
		    // needs to go into the detailed slot
		    if (detailedPossiblesReport == null)
			return true;	// there's room for it
		    else
			return false;
		} else {
		    // needs to go into the summary slot
		    if (summaryPossiblesReport == null)
			return true;	// there's room for it
		    else
			return false;
		}
	    } else {
		return false;
	    }
	}


	/**
	 * Return true if both the detailed and summary slots for both actual
	 * and possible leaks have been fillled.
	 */

	// implement MultiReport
	public boolean isFull() {
	    return detailedReport != null &&
		   summaryReport != null &&
		   detailedPossiblesReport != null &&
		   summaryPossiblesReport != null;
	}

	public void add(MemoryReport report) {
	    if (report.isActualLeaks()) {
		if (report.isVerbose()) {
		    // needs to go into the detailed slot
		    assert detailedReport == null :
			   "MultiLeaksReport.add(): detailed actuals slot is occupied"; // NOI18N
		    detailedReport = report;
		} else {
		    // needs to go into the summary slot
		    assert summaryReport == null :
			   "MultiLeaksReport.add(): summary actuals slot is occupied"; // NOI18N
		    summaryReport = report;
		}
	    } else if (report.isPossibleLeaks()) {
		if (report.isVerbose()) {
		    // needs to go into the detailed slot
		    assert detailedPossiblesReport == null :
			   "MultiLeaksReport.add(): detailed possibles slot is occupied"; // NOI18N
		    detailedPossiblesReport = report;
		} else {
		    // needs to go into the summary slot
		    assert summaryPossiblesReport == null :
			   "MultiLeaksReport.add(): summary possibles slot is occupied"; // NOI18N
		    summaryPossiblesReport = report;
		}
	    }
	}
    }

    static public class AccessError {		// aka RtcItem
	public ErrorCode type;
	public String description;
	public long address;		// ... which was accessed
	public long size;		// ... of access
	public String region;		// stack, heap, heap block etc
	/*package*/ Stack stack;		// ... at the time of allocation
	/*package*/ Location location;	// ... of the accessing instruction
	public String variableName;	// ... variable associated with address

	/*package*/ AccessError(ErrorCode type,
			   String description,
			   long address,
			   long size,
			   String region,
			   Stack stack,
			   Location location,
			   String variableName) {

	    this.type = type;
	    this.description = description;
	    this.address = address;
	    this.size = size;
	    this.region = region;
	    this.stack = stack;
	    this.location = location;
	    this.variableName = variableName;
	}

	public ErrorCode type() { return type; }
	public String description() { return description; }
	public long address() { return address; }
	public long size() { return size ; }
	public String region() { return region; }
	/*package*/ Stack stack() { return stack; }
	/*package*/ Location location() { return location; }
	public String variableName() { return variableName; }


	// for use by XML decoder:

	void setStack(Stack stack) {
	    this.stack = stack;
	}

	void setLocation(Location location) {
	    this.location = location;
	}

	void setDescription(String description) {
	    this.description = description;
	}
    }

    static interface Listener {
	public void profileChanged();

	public void modelChanged();

	public void runBegin(Run run);
	public void runEnd();

	public void accessStateChanged(RtcState state);
	public void accessItem(AccessError item);

	public void memuseStateChanged(RtcState state);

	public void memuseBegin(MemoryReportHeader header);
	public void memuseItem(MemoryReportItem item);
	public void memuseEnd();
	public void memuseInterrupted();

	public void leaksBegin(MemoryReportHeader header);
	public void leakItem(MemoryReportItem item);
	public void leaksEnd();
	public void leaksInterrupted();
    };


    /**
     * An RTC experiment may contain more than one "run"/
     * The current structure of dbx is such that the executable (along
     * with all it's properties like 64-bitness) may change from run to run.
     */
    static public class Run extends Completable {

	private boolean accessErrorsCleared;
	private boolean memuseErrorsCleared;
	private boolean leakErrorsCleared;

	MemoryReport currentMemoryReport;
	MultiReport currentMultiReport;

	/**
	 * Used to defeat clearing of reports.
	 *
	 * Ideally we SHOULD disable the clear actions while we're in the middle
	 * of reports, but it's unlikley thatusers will clear in such
	 * circumstances we'll just ignore the clear to protect the integrity 
	 * of the models state.
	 */

	private boolean isInMiddleOfReport() {
	    return currentMemoryReport != null || currentMultiReport != null;
	}
	    

	//
	// properties
	//
	String runExecutable;
	int runPid;
	boolean sixtyfourbit;

	public String runExecutable() {
	    return runExecutable;
	}

	public int runPid() {
	    return runPid;
	}

	public boolean isSixtyFourBit() {
	    return sixtyfourbit;
	}


	//
	// main model content
	//
	ArrayList<MultiMemuseReport> memuseReports = new ArrayList<MultiMemuseReport>();
	ArrayList<MultiLeaksReport> leakReports = new ArrayList<MultiLeaksReport>();
	ArrayList<AccessError> accessErrors = new ArrayList<AccessError>();

	/*package*/ ArrayList<MultiMemuseReport> memuseReports() {
	    return memuseReports;
	}

	/*package*/ ArrayList<MultiLeaksReport> leakReports() {
	    return leakReports;
	}

	public ArrayList<AccessError> accessErrors() {
	    return accessErrors;
	}

	void clearAccessErrors() {
	    accessErrors.clear();
	    accessErrorsCleared = true;
	}

	void clearMemuseErrors() {
	    if (isInMiddleOfReport())
		return;
	    memuseReports.clear();
	    memuseErrorsCleared = true;
	}

	void clearLeakErrors() {
	    if (isInMiddleOfReport())
		return;
	    leakReports.clear();
	    leakErrorsCleared = true;
	}

	boolean isAccessErrorsCleared() {
	    return accessErrorsCleared;
	}

	boolean isMemuseErrorsCleared() {
	    return memuseErrorsCleared;
	}

	boolean isLeakErrorsCleared() {
	    return leakErrorsCleared;
	}
    }

    private final ArrayList<Run> runs = new ArrayList<Run>();

    private Run currentRun;

    private RtcProfile profile;

    private final String name;

    public RtcModel(String name) {
	this.name = name;
    }

    /*package*/ ArrayList<Run> runs() {
	return runs;
    }

    /*package*/ Run currentRun() {
	return currentRun;
    }

    public void setProfile(RtcProfile profile) {
	RtcProfile oldProfile = this.profile;
	this.profile = profile;

	if (oldProfile != profile)
	    for (Listener l : listeners) 
		l.profileChanged();
    }

    public RtcProfile getProfile() {
	return profile;
    }

    public String getName() {
	if (lastSavedIn != null)
	    return lastSavedIn;
	else
	    return name;
    }


    //
    // Helpers for constructing parts of model
    // 

    /*package*/ Stack newStack(int nframes) {
	return new Stack(currentRun, nframes);
    }

    //
    // Listener registration
    //
    private final  ArrayList<Listener> listeners = new ArrayList<Listener>();

    /*package*/ void addListener(Listener listener) {
	listeners.add(listener);
    }

    /*package*/ void removeListener(Listener listener) {
	listeners.remove(listener);
    }


    //
    // Alterations of the model
    //
    public void accessStateChanged(RtcState state) {
	for (Listener l : listeners) 
	    l.accessStateChanged(state);
    }

    public Run runBegin(String executable, int pid, boolean sixtyfourbit) {
	currentRun = new Run();
	runs.add(currentRun);

	currentRun.runExecutable = executable;
	currentRun.runPid = pid;
	currentRun.sixtyfourbit = sixtyfourbit;

	for (Listener l : listeners) 
	    l.runBegin(currentRun);
	return currentRun;
    }

    public void runEnd() {
        if (currentRun != null) {
            currentRun.setComplete();
        }
	currentRun = null;
	for (Listener l : listeners) 
	    l.runEnd();
    }


    public void memuseStateChanged(RtcState state) {
	for (Listener l : listeners) 
	    l.memuseStateChanged(state);
    }

    public void memuseBegin(MemoryReportHeader header) {
	currentRun.currentMemoryReport = new MemoryReport(header);

	if (! MultiReport.roomInFor(currentRun.currentMultiReport,
				    currentRun.currentMemoryReport)) {
	    MultiMemuseReport multiMemuseReport = new MultiMemuseReport();
	    currentRun.currentMultiReport = multiMemuseReport;
	    currentRun.memuseReports.add(multiMemuseReport);
	}
	currentRun.currentMultiReport.add(currentRun.currentMemoryReport);

	for (Listener l : listeners) 
	    l.memuseBegin(header);
    }

    public void memuseItem(MemoryReportItem item) {
	if (currentRun.currentMemoryReport != null)
	    currentRun.currentMemoryReport.items.add(item);
	for (Listener l : listeners) 
	    l.memuseItem(item);
    }

    public void memuseEnd() {
	currentRun.currentMemoryReport.setComplete();
	currentRun.currentMemoryReport = null;

	if (currentRun.currentMultiReport.isFull())
	    currentRun.currentMultiReport = null;

	for (Listener l : listeners) 
	    l.memuseEnd();
    }

    public void memuseInterrupted() {
	if (currentRun.currentMemoryReport != null) {
	    currentRun.currentMemoryReport.interrupt();
	    currentRun.currentMemoryReport = null;
	}
	for (Listener l : listeners) 
	    l.memuseInterrupted();
    }



    public void leaksBegin(MemoryReportHeader header) {
	currentRun.currentMemoryReport = new MemoryReport(header);

	if (! MultiReport.roomInFor(currentRun.currentMultiReport,
				    currentRun.currentMemoryReport)) {
	    MultiLeaksReport multiLeaksReport = new MultiLeaksReport();
	    currentRun.currentMultiReport = multiLeaksReport;
	    currentRun.leakReports.add(multiLeaksReport);
	}
	currentRun.currentMultiReport.add(currentRun.currentMemoryReport);

	for (Listener l : listeners) 
	    l.leaksBegin(header);
    }

    public void leakItem(MemoryReportItem item) {
	if (currentRun.currentMemoryReport != null)
	    currentRun.currentMemoryReport.items.add(item);
	for (Listener l : listeners) 
	    l.leakItem(item);
    }

    public void leaksEnd() {
	currentRun.currentMemoryReport.setComplete();
	currentRun.currentMemoryReport = null;

	if (currentRun.currentMultiReport.isFull())
	    currentRun.currentMultiReport = null;

	for (Listener l : listeners) 
	    l.leaksEnd();
    }

    public void leaksInterrupted() {
	if (currentRun.currentMemoryReport != null) {
	    currentRun.currentMemoryReport.interrupt();
	    currentRun.currentMemoryReport = null;
	}
	for (Listener l : listeners) 
	    l.leaksInterrupted();
    }

    public void accessItem(AccessError item) {
	currentRun.accessErrors.add(item);
	for (Listener l : listeners) 
	    l.accessItem(item);
    }

    private void modelChanged() {
	for (Listener l : listeners) 
	    l.modelChanged();
    }



    public void clearAccess() {
	for (Run run : runs)
	    run.clearAccessErrors();
	modelChanged();
    }

    public void clearMemuse() {
	for (Run run : runs)
	    run.clearMemuseErrors();
	modelChanged();
    }

    public void clearLeaks() {
	for (Run run : runs)
	    run.clearLeakErrors();
	modelChanged();
    }

    public void clearAll() {
	// remove all runs but the current run
	Iterator<Run> i = runs.iterator();
	while (i.hasNext()) {
	    Run iRun = i.next();
	    if (iRun != currentRun)
		i.remove();
	}
	if (currentRun != null) {
	    currentRun.clearAccessErrors();
	    currentRun.clearMemuseErrors();
	    currentRun.clearLeakErrors();
	}
	modelChanged();
    }

    //
    // model traversal
    //

    private void traverseRun(Run run, Listener listener, boolean detailed) {
	listener.runBegin(run);

	for (AccessError item : run.accessErrors)
	    listener.accessItem(item);

	for (MultiLeaksReport leakReport : run.leakReports()) {
	    MemoryReport report;

	    // actuals
	    report = detailed? leakReport.detailedReport(): 
			       leakReport.summaryReport();
	    if (report == null)
		continue;
	    listener.leaksBegin(report.header);
	    for (MemoryReportItem item : report.items)
		listener.leakItem(item);
	    if (report.isComplete())
		listener.leaksEnd();

	    // possibles
	    report = detailed? leakReport.detailedPossiblesReport(): 
			       leakReport.summaryPossiblesReport();
	    if (report == null)
		continue;
	    listener.leaksBegin(report.header);
	    for (MemoryReportItem item : report.items)
		listener.leakItem(item);
	    if (report.isComplete())
		listener.leaksEnd();
	}

	for (MultiMemuseReport memuseReport : run.memuseReports()) {
	    MemoryReport report = detailed? memuseReport.detailedReport(): 
			                    memuseReport.summaryReport();
	    if (report == null)
		continue;
	    listener.memuseBegin(report.header);
	    for (MemoryReportItem item : report.items)
		listener.memuseItem(item);
	    if (report.isComplete())
		listener.memuseEnd();
	}

	if (run.isComplete())
	    listener.runEnd();
    }
     
    /**
     * Traverse the whole model dispatching to any observers via Listener
     * events.
     */
    /*package*/ void traverse(Listener listener, boolean detailed) {
	for (Run run : runs)
	    traverseRun(run, listener, detailed);
    }

    private String lastSavedIn;

    public String lastSavedIn() {
	return lastSavedIn;
    }

    public void save() {
	if (profile == null) {
	    if (Log.Rtc.debug)
		System.out.printf("RtcModel.save(): model but no model\n"); // NOI18N
	    return;
	}

	final String baseDir = profile.getBaseDir();
	final String dir     = profile.getExperimentDir();
	String absoluteDir;

	if (org.netbeans.modules.cnd.utils.CndPathUtilities.isPathAbsolute(dir))
	    absoluteDir = dir;
	else
	    absoluteDir = baseDir + File.separatorChar + dir;

	String name = profile.getExperimentName();
	name = name.trim();

	String fullPathName = "";

	try {
	    FileObject rootFo = FileUtil.toFileObject(new File("/")); // NOI18N
	    FileSystem fs = rootFo.getFileSystem();
	    FileObject folder = FileUtil.createFolder(rootFo, absoluteDir);

	    String template = "RtcExperiment"; // NOI18N

	    if ( ! IpeUtils.isEmpty(name)) {
		// if user specified a name use it as a template
		template = IpeUtils.stripSuffix(name);
	    }

	    name = FileUtil.findFreeFileName(folder,
					     template,
					     RtcDataObject.EXTENSION);

	    // name returned from findFreeFileName is without extension
	    name += "." + RtcDataObject.EXTENSION; // NOI18N


	    fullPathName = absoluteDir + File.separatorChar + name;

	    if (Log.Rtc.debug) {
		System.out.printf("RtcView.save()\n"); // NOI18N
		System.out.printf("\t     baseDir %s\n", baseDir); // NOI18N
		System.out.printf("\t         dir %s\n", dir); // NOI18N
		System.out.printf("\t absoluteDir %s\n", absoluteDir); // NOI18N
		System.out.printf("\t        name %s\n", name); // NOI18N
		System.out.printf("\tfullPathName %s\n", fullPathName); // NOI18N
	    }

	    RtcExperimentXMLWriter xw =
		new RtcExperimentXMLWriter(absoluteDir, name, this);
	    xw.write();

	    lastSavedIn = fullPathName;

	} catch (IOException e) {
	    String msg = Catalog.format("FailedToWriteOutExperiment",
				        fullPathName, e.getMessage());
	    ErrorManager.getDefault().annotate(e,
					       ErrorManager.USER,
					       msg,
					       msg,
					       null,
					       null);
	    ErrorManager.getDefault().notify(e);
	}
    }

    public void read(FileObject fo) throws IOException {
	RtcExperimentXMLReader xr = new RtcExperimentXMLReader(fo, this);
	xr.read();
    }

}
