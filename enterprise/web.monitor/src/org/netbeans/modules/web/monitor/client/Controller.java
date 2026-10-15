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

/*
 * @author Ana von Klopp
 */

package  org.netbeans.modules.web.monitor.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.web.monitor.data.*;

class Controller  {

    // REPLAY strings - must be coordinated with server.MonitorFilter
    static final String REPLAY="netbeans.replay"; //NOI18N
    static final String PORT="netbeans.replay.port"; //NOI18N
    static final String REPLAYSTATUS="netbeans.replay.status"; //NOI18N
    static final String REPLAYSESSION="netbeans.replay.session"; //NOI18N
    static final boolean debug = false;
    //private transient static boolean starting = true;

    // Test server location and port
    // Should use InetAddress.getLocalhost() instead
    private static transient String server = "localhost"; //NOI18N
    private static transient int port = 8080;

    // Location of the files
    private static FileObject monDir = null;
    private static FileObject currDir = null;
    private static FileObject saveDir = null;
    private static FileObject replayDir = null;

    static final String monDirStr = "HTTPMonitor"; // NOI18N
    static final String currDirStr = "current"; // NOI18N
    static final String saveDirStr = "save"; // NOI18N
    static final String replayDirStr = "replay"; // NOI18N

    // Constant nodes etc we need to know about
    private transient  NavigateNode root = null;
    private Children.SortedArray currTrans = null;
    private Children.SortedArray  savedTrans = null;

    // These are the ones that should go. 
    private Hashtable currBeans = null;
    private Hashtable saveBeans = null;
    
    private transient Comparator comp = null;

    private boolean useBrowserCookie = true;

    private static Controller instance = null; 
    private Date startDate;
    
    private Controller() {
        // TODO: setting the startup date to 30s earlier from now to prevent deletion of the 
        // first request (#56880), the ideal fix should be to use the IDE startup time
        startDate = new Date(System.currentTimeMillis() - 30000);
	currBeans = new Hashtable();
	saveBeans = new Hashtable();
	createNodeStructure();
        /*
	registerBrowserListener();
        */
    }

    static Controller getInstance() { 
	if(instance == null) 
	    instance = new Controller(); 
	return instance;
    }
 
    /**
     * Invoked at startup, creates the root folder and the folder for
     * current and saved transactions (and their children arrays).
     */
    private void createNodeStructure() {

	comp = new CompTime(true);
	currTrans = new Children.SortedArray();
	currTrans.setComparator(comp);
	savedTrans = new Children.SortedArray();
	savedTrans.setComparator(comp);

	CurrNode currNode = new CurrNode(currTrans);
	SavedNode savedNode = new SavedNode(savedTrans);

	Node[] kids = new Node[2];
	kids[0] = currNode;
	kids[1] = savedNode;

	Children children = new Children.Array();
	children.add(kids);
	root = new NavigateNode(children);

	    
    }

    /**
     * Adds a transaction to the list of current transactions.
     */
    void addTransaction(String id) {

	if(debug) log("Creating node for " + id);
	TransactionNode[] nodes = new TransactionNode[1];
	MonitorData md = retrieveMonitorData(id, currDirStr); 
	try { 
	    nodes[0] = createTransactionNode(md, true);
	    currTrans.add(nodes);
	}
	catch(Exception ex) {
	    // If there is some kind of parsing exception, do nothing
	}
    }

    /**
     * Adds a transaction to the list of current transactions.
     */
    protected NavigateNode getRoot() {
	return root;
    }


    protected static FileObject getMonDir() throws FileNotFoundException {
	
	if(monDir == null || !monDir.isFolder()) {
	    try {
		createDirectories();
	    }
	    catch(FileNotFoundException ex) {
		throw ex;
	    }
	}
	return monDir;
    }
    

    protected static FileObject getCurrDir() throws FileNotFoundException {
	 
	if(currDir == null || !currDir.isFolder()) {
	    try{
		createDirectories();
	    }
	    catch(FileNotFoundException ex) {
		throw ex;
	    }
	}
	return currDir;
    }

    protected static FileObject getReplayDir() throws FileNotFoundException {
	 
	if(replayDir == null || !replayDir.isFolder()) {
	    try{
		createDirectories();
	    }
	    catch(FileNotFoundException ex) {
		throw ex;
	    }
	}
	return replayDir;
    }
    

    protected static FileObject getSaveDir() throws FileNotFoundException {
	 
	if(saveDir == null || !saveDir.isFolder()) {
	    try{
		createDirectories();
	    }
	    catch(FileNotFoundException ex) {
		throw ex;
	    }
	}
	return saveDir;
    }

    boolean haveDirectories() {
	if(currDir == null) {
	    try {
		currDir = getCurrDir();
	    }
	    catch(Exception ex) {
		return false;
	    }
	}
	
	if(saveDir == null) {
	    try {
		saveDir = getSaveDir();
	    }
	    catch(Exception ex) {
		return false;
	    }
	}
	return true;
    }
    

    private static void createDirectories() throws FileNotFoundException {

	if(debug) log("Now in createDirectories()"); // NOI18N
	
	FileObject rootdir = 
	    FileUtil.getConfigRoot();
	if(debug) {
	    log("Root directory is " + rootdir.getName()); // NOI18N
	    File rootF = FileUtil.toFile(rootdir);
	    log("Root directory abs path " + // NOI18N
		rootF.getAbsolutePath());
	}

	FileLock lock = null;

	if(monDir == null || !monDir.isFolder()) {
	    try {
		monDir = rootdir.getFileObject(monDirStr);
	    }
	    catch(Exception ex) {
	    }
	    
	    if(monDir == null || !monDir.isFolder()) {
		if(monDir != null) {
		    try {
			lock = monDir.lock();
			monDir.delete(lock);
		    }
		    catch(FileAlreadyLockedException falex) {
			throw new FileNotFoundException();
		    }
		    catch(IOException ex) {
			throw new FileNotFoundException();
		    }
		    finally { 
			if(lock != null) lock.releaseLock();
		    }
		}
		try {
		    monDir = rootdir.createFolder(monDirStr);
		}
		catch(IOException ioex) {
		    if(debug) ioex.printStackTrace();
		}
	    }
	    if(monDir == null || !monDir.isFolder()) 
		throw new FileNotFoundException();
	}

	if(debug) 
	    log("monitor directory is " + monDir.getName());// NOI18N

	// Current directory

	if(currDir == null || !currDir.isFolder()) {

	    try {
		currDir = monDir.getFileObject(currDirStr);
	    }
	    catch(Exception ex) { }
	    
	    if(currDir == null || !currDir.isFolder()) {
		lock = null;
		if(currDir != null) {
		    try {
			lock = currDir.lock();
			currDir.delete(lock);
		    }
		    catch(FileAlreadyLockedException falex) {
			throw new FileNotFoundException();
		    }
		    catch(IOException ex) {
			throw new FileNotFoundException();
		    }
		    finally { 
			if(lock != null) lock.releaseLock();
		    }
		}
		try {
		    currDir = monDir.createFolder(currDirStr);
		}
		catch(IOException ex) {
		    if(debug) ex.printStackTrace();
		}
	    }
	    if(currDir == null || !currDir.isFolder()) 
		throw new FileNotFoundException();
	}
	
	if(debug) log("curr directory is " + currDir.getName()); // NOI18N

	// Save Directory
	if(saveDir == null || !saveDir.isFolder()) {
	    try {
		saveDir = monDir.getFileObject(saveDirStr);
	    }
	    catch(Exception ex) { }
	    
	    if(saveDir == null || !saveDir.isFolder()) {
		if(saveDir != null) {
		    lock = null;
		    try {
			lock = saveDir.lock();
			saveDir.delete(lock);
		    }
		    catch(FileAlreadyLockedException falex) {
			throw new FileNotFoundException();
		    }
		    catch(IOException ex) {
			throw new FileNotFoundException();
		    }
		    finally { 
			if(lock != null) lock.releaseLock();
		    }
		}
		try {
		    saveDir = monDir.createFolder(saveDirStr);
		}
		catch(IOException ex) {
		    if(debug) ex.printStackTrace();
		}
	    }
	    if(saveDir == null || !saveDir.isFolder()) 
		throw new FileNotFoundException();

	    if(debug) 
		log("save directory is " + saveDir.getName()); // NOI18N
	}

	// Replay Directory

	if(replayDir == null || !replayDir.isFolder()) {

	    try {
		replayDir = monDir.getFileObject(replayDirStr);
	    }
	    catch(Exception ex) { }
	    
	    if(replayDir == null || !replayDir.isFolder()) {
		if(replayDir != null) {
		    lock = null;
		    try {
			lock = replayDir.lock();
			replayDir.delete(lock);
		    }
		    catch(FileAlreadyLockedException falex) {
			throw new FileNotFoundException();
		    }
		    catch(IOException ex) {
			throw new FileNotFoundException();
		    }
		    finally { 
			if(lock != null) lock.releaseLock();
		    }
		}
		try {
		    replayDir = monDir.createFolder(replayDirStr);
		}
		catch(Exception ex) {
		    if(debug) ex.printStackTrace();
		}
	    }
	    if(replayDir == null || !replayDir.isFolder()) 
		throw new FileNotFoundException();

	    if(debug) 
		log("replay directory is " + replayDir.getName());// NOI18N
	}
    }


    /**
     * Invoked by ReplayAction. Replays the transaction corresponding to
     * the selected node.
     *
     * PENDING - it would be better if the nodes know which server
     * they were processed on. This would be the case if we listed the 
     * nodes separately depending on the server that collected the
     * data. 
     *
     */
    void replayTransaction(Node node) {

	if(debug) 
	    log("replayTransaction(Node node) from node " + 
		node.getName()); // NOI18N

	if(!checkServer(true)) return;

	TransactionNode tn = null; 
	try {
	    tn = (TransactionNode)node;
	}
	catch(ClassCastException cce) {
	    if(debug) 
		log("Selected node was not a transaction node");//NOI18N
	    return;
	}
	
	MonitorData md = getMonitorData(tn, false, false);
        if (md == null) {
            String msg = NbBundle.getMessage(Controller.class, "MSG_NoMonitorData");
            Logger.getLogger("global").log(Level.INFO, msg);
            return;
        }
	if(!useBrowserCookie) 
	    md.getRequestData().setReplaceSessionCookie(true);

	if(debug) { 
	    log("Replace is " +  // NOI18N
		String.valueOf(md.getRequestData().getReplaceSessionCookie()));
	    String fname = md.createTempFile("control-replay.xml"); // NOI18N
	    log("Wrote replay data to " + fname);// NOI18N 
	}
    
	String status;
	if(tn.isCurrent()) status = currDirStr; 
	else status = saveDirStr; 
	try {
	    replayTransaction(md, status);
	}
	catch(UnknownHostException uhe) {
	    // Notify the user that there is no host

	    Object[] options = {
		NbBundle.getBundle(Controller.class).getString("MON_OK"),
	    };

	    NotifyDescriptor noServerDialog = 
		new NotifyDescriptor(NbBundle.getMessage(Controller.class, "MON_Exec_server_no_host", md.getServerName()),
				     NbBundle.getBundle(Controller.class).getString("MON_Exec_server"),
				     NotifyDescriptor.DEFAULT_OPTION,
				     NotifyDescriptor.INFORMATION_MESSAGE,
				     options,
				     options[0]);
	    DialogDisplayer.getDefault().notify(noServerDialog);

	}
	catch(IOException ioe) {

	    if(debug) { 
		log(ioe.getMessage()); 
		ioe.printStackTrace();
	    }
	    
	    // Notify the user that the server is not running
	    Object[] options = {
		NbBundle.getBundle(Controller.class).getString("MON_OK"),
	    };

	    NotifyDescriptor noServerDialog = 
		new NotifyDescriptor(NbBundle.getMessage(Controller.class, "MON_Exec_server_start", md.getServerAndPort()), 
				     NbBundle.getBundle(Controller.class).getString("MON_Exec_server"),
				     NotifyDescriptor.DEFAULT_OPTION,
				     NotifyDescriptor.INFORMATION_MESSAGE,
				     options,
				     options[0]);
	    DialogDisplayer.getDefault().notify(noServerDialog);
	}
    }

    /**
     * Invoked by EditPanel. Replays the transaction corresponding to
     * the selected node. 
     */
    void replayTransaction(MonitorData md) 
	throws UnknownHostException, IOException  {

	// PENDING - can't make UI changes right now for Sierra
	// any exception thrown in this method indicates that we
	// couldn't even get to the monitor data, and we should add an
	// additional panel to that effect. Also, unreadable monitor
	// data should cause the transaction to be removed from the
	// pane. 
	
	if(debug) log("replayTransaction(MonitorData md)"); //NOI18N

	FileObject fo; 	
	FileLock lock = null;
	OutputStream out = null;
	PrintWriter pw = null;

	if(debug) log("Creating record for replay"); //NOI18N

	String id = md.getAttributeValue("id"); // NOI18N

	try {
	    // This will fail if the file already exists. This can
	    // happen if the replay previously failed because the
	    // server was not running. This is dealt with in the
	    // catch clause below. 
	    fo = getReplayDir().createData(id, "xml"); //NOI18N
	    if(debug) log(" Created file for replay data"); 
	}
	catch(IOException ioex) { 

	    try { 
		fo = getReplayDir().getFileObject(id, "xml"); 
	    }
	    catch(IllegalArgumentException iaex) { 
		// This is only thrown if getReplayDir() is not a
		// folder. This should not happen. 
		throw new IOException("No replay dir"); 
	    } 

	    if(!fo.isData()) { 
		throw new IOException("Can't create file, giving up"); 
	    } 

	    try { 
		 lock = fo.lock(); 
	    } 
	    catch(FileAlreadyLockedException falex) { 
		throw new IOException("Old file exist, islocked"); 
	    } 

	    try { 
		fo.delete(lock); 
	    } 
	    catch(IOException ioex2) { 
		throw new IOException("Couldn't delete old file"); 
	    }
	    finally { 
		if(lock != null) lock.releaseLock(); 
	    }
	
	    try { 
		fo = getReplayDir().createData(id, "xml"); //NOI18N
	    }
	    catch(IOException ioex2) { 
		if(debug) log(" Couldn't create file for replay data"); 
		throw ioex2;
	    }
	} 

	try { 
	    lock = fo.lock();
	} 
	catch(FileAlreadyLockedException fale) { 
	    if(debug) log("Can't get a file lock for the replay file");
	    throw new IOException(); 
	} 

	try { 
	    out = fo.getOutputStream(lock);
	    pw = new PrintWriter(out);
	    md.write(pw);	    
	    if(debug) log("...record complete"); //NOI18N

	    if(debug) {
		String fname = 
		    md.createTempFile("control-record.xml"); // NOI18N
		log("Wrote replay data to " + fname); // NOI18N
	    }
	}
	catch(IOException ioex) {
	    throw ioex;
	}
	finally {
	    if(lock != null) lock.releaseLock(); 
	    try {
		pw.close();
	    }
	    catch(Throwable t) {
	    }  
	    try {
		out.close();
	    }
	    catch(Throwable t) {
	    }  
	}
	
	try {
	    replayTransaction(md, replayDirStr); 
	}
	catch(UnknownHostException uhe) {
	    throw uhe;
	}
	catch(IOException ioe) {
	    throw ioe;
	}
    }
    
    /**
     *
     */
    void replayTransaction(MonitorData md, String status)
	throws UnknownHostException, IOException  {
	
	if(debug) log("replayTransaction(MonitorData md, String status )"); //NOI18N

	URL url = null;
	try {
	    String name = md.getServerName();
	    int port = md.getServerPort();
	    
	    StringBuffer uriBuf = new StringBuffer(128);
	    uriBuf.append(md.getRequestData().getAttributeValue("uri")); //NOI18N 
	    uriBuf.append("?"); //NOI18N 
	    uriBuf.append(REPLAY); 
	    uriBuf.append("="); //NOI18N 
	    uriBuf.append(md.getAttributeValue("id")); //NOI18N 
	    uriBuf.append("&"); //NOI18N 
	    uriBuf.append(REPLAYSTATUS); 
	    uriBuf.append("="); //NOI18N 
	    uriBuf.append(status);

	    String portS = null; 
	    try { 
	    URL u = getSampleHTTPServerURL();
		portS = 
		    String.valueOf(u.getPort()/*HttpServer.getRepositoryRoot().getPort()*/);
	    }
	    catch(Exception ex) {
		// No internal HTTP server, do nothing
	    } 
	    if(portS != null) { 
		uriBuf.append("&"); //NOI18N 
		uriBuf.append(PORT); 
		uriBuf.append("="); //NOI18N 
		uriBuf.append(portS);
	    }


	    if(md.getRequestData().getReplaceSessionCookie()) { 
		uriBuf.append("&"); //NOI18N 
		uriBuf.append(REPLAYSESSION); 
		uriBuf.append("="); //NOI18N 
		uriBuf.append(md.getRequestData().getSessionID());
	    }
	    url = new URL("http", name, port, uriBuf.toString()); //NOI18N 
	}
	catch(MalformedURLException me) { 
	    if(debug) log(me.getMessage());
	}
	catch(NumberFormatException ne) { 
	    if(debug) log(ne.getMessage());
	}

	// Send the url to the browser.
	try {
	    showReplay(url);
	}
	catch(UnknownHostException uhe) {
	    throw uhe;
	}
	catch(IOException ioe) {
	    throw ioe;
	}
    }

    void saveTransaction(Node[] nodes) {

	if(!haveDirectories()) {
	    // PENDING - report the error properly
	    // This should not happen
	    log("Couldn't get the directory"); //NOI18N
	    return;
	}

	Node[] newNodes = new Node[nodes.length];
	TransactionNode mvNode; 
	String id;
	 
	boolean error = false; 

	for(int i=0; i < nodes.length; ++i) {
	    
	    mvNode = (TransactionNode)nodes[i];
	    id = mvNode.getID();
	    
	    if(debug) log(" Saving " + id); //NOI18N 

	    if(currBeans.containsKey(id)) 
		saveBeans.put(id, currBeans.remove(id)); 
	    
	    // Note I didn't load the bean here yet. Will only do that 
	    // if the data is displayed. 
		
	    FileLock lock = null; 
	    try {
		FileObject fold = 
		    currDir.getFileObject(id, "xml"); //NOI18N
		lock = fold.lock();
		fold.copy(saveDir, id, "xml"); //NOI18N
		if(debug) log(fold.getName());
		fold.delete(lock);
		mvNode.setCurrent(false);
		newNodes[i] = mvNode;
	    }
	    catch(FileAlreadyLockedException falex) {
		error = true;
		// PENDING report properly
	    }
	    catch(IOException ioex) {
		error = true;
		// PENDING report properly
	    }
	    catch(Exception ex) {
		error = true; 
		// PENDING report properly
	    }
	    finally { 
		if(lock != null) lock.releaseLock(); 
	    }
	    
	}
	if(!error) currTrans.remove(nodes);
	savedTrans.add(newNodes);
    }
  
    /**
     * Invoked by DeleteAction.  Deletes a saved transaction 
     */

    void deleteTransaction(final Node[] nodes) {

	if(!haveDirectories()) {
	    // PENDING - report the error property
	    // This should not happen
	    log("Couldn't get the directory"); //NOI18N 
	    return;
	}

	// PENDING
	if((nodes == null) || (nodes.length == 0)) return;
        
        final ProgressMonitor progressMonitor = new ProgressMonitor();
        
        RequestProcessor.getDefault().post(new Runnable () {
            public void run() {
                // give awt thread chance to draw the progress monitor
                Thread.yield();
                // remove nodes
                currTrans.remove(nodes);
                savedTrans.remove(nodes);
                
                int oldValue = 0;
                for(int i=0; i < nodes.length; i++) {
                    TransactionNode node = (TransactionNode)nodes[i];
                    FileObject fileObject = null;
                    if (node.isCurrent()) {
                        String id = node.getID();
                        fileObject = currDir.getFileObject(id, "xml");
                        currBeans.remove(id);
                    } else {
                        String id = node.getID();
                        fileObject = saveDir.getFileObject(id, "xml");
                        saveBeans.remove(id);
                    }
                    if (fileObject != null) {
                        // delete the file
                        FileLock lock = null;
                        try {
                            lock = fileObject.lock();
                            fileObject.delete(lock);
                        } catch(FileAlreadyLockedException falex) {
                            Logger.getLogger("global").log(Level.INFO, null, falex);
                        } catch(IOException exception) {
                            Logger.getLogger("global").log(Level.INFO, null, exception);
                        } finally { 
                            if(lock != null) {
                                lock.releaseLock();
                            }
                        }
                    }
                    // update the progress monitor if needed
                    final int newValue = 100*(i+1)/nodes.length;
                    if (newValue > oldValue) {
                        oldValue = newValue;
                        SwingUtilities.invokeLater(new Runnable () {
                            public void run (){
                                progressMonitor.setValue(newValue);
                            }
                        });
                    }
                }
                SwingUtilities.invokeLater(new Runnable () {
                    public void run (){
                        progressMonitor.close();
                    }
                });
            }
        });
        progressMonitor.setVisible(true);
    }

    void deleteDirectory(String dir) {

	if(!haveDirectories()) {
	    // PENDING - report the error property
	    // This should not happen
	    log("Couldn't get the directory"); //NOI18N 
	    return;
	}

	final FileObject directory;
	if(dir.equals(saveDirStr)) {
	    directory = saveDir;
	    savedTrans.remove(savedTrans.getNodes());
	    saveBeans.clear();
	}
	
	else {   
	    directory = currDir;
	    currTrans.remove(currTrans.getNodes());
	    currBeans.clear();
	}
        if (directory.getChildren().length == 0) {
            return;
        }
        final ProgressMonitor progressMonitor = new ProgressMonitor();
        
        RequestProcessor.getDefault().post(new Runnable () {
            public void run() {
                Thread.yield();

                int number = directory.getChildren().length;
                int oldValue = -1;
                int i = 0;
                
                for(Enumeration<? extends FileObject> e = directory.getData(false); e.hasMoreElements(); ++i) {
                    FileObject fo = (FileObject) e.nextElement();
                    FileLock lock = null;
                    try {
                        lock = fo.lock();
                        fo.delete(lock);
                    } catch(FileAlreadyLockedException falex) {
                        Logger.getLogger("global").log(Level.INFO, null, falex);
                    } catch(IOException exception) {
                        Logger.getLogger("global").log(Level.INFO, null, exception);
                    } finally { 
                        if(lock != null) {
                            lock.releaseLock();
                        }
                    }
                    // update the progress monitor if needed
                    final int newValue = 100 * i/number;
                    if (newValue > oldValue) {
                        oldValue = newValue;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                progressMonitor.setValue(newValue);
                            }
                        });
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        progressMonitor.close();
                    }
                });
            }
        });
        progressMonitor.setVisible(true);
    }

    void deleteTransactions() {
	deleteDirectory(Constants.Files.save);
	deleteDirectory(Constants.Files.current);
	savedTrans.remove(savedTrans.getNodes());
	currTrans.remove(currTrans.getNodes());
    }


    void getTransactions() {

	if(debug) log("getTransactions"); //NOI18N 
       
	if(!haveDirectories()) {
	    // PENDING - report the error property
	    // This should not happen
	    log("Couldn't get the directory"); //NOI18N 
	    return;
	}

	Enumeration<? extends FileObject> e = null;
	Vector<TransactionNode> nodes = new Vector<>(); 
	int numtns = 0;
	TransactionNode[] tns = null;
	FileObject fo = null;
	String id = null;
	MonitorData md = null;
	
	currTrans.remove(currTrans.getNodes());
	if(debug) log("getTransactions removed old nodes"); //NOI18N 

	e = currDir.getData(false);
        final List<FileObject> fileObjectsToDelete = new ArrayList<>();
	while(e.hasMoreElements()) {

	    fo = (FileObject)e.nextElement();
	        // #43213 - avoiding ModuleInstall class, delaying deletion of old records until now
            if (fo.lastModified().after(startDate)) {
                id = fo.getName();
                if(debug) 
                    log("getting current transaction: " + id); //NOI18N 

                // Retrieve the monitordata
                md = retrieveMonitorData(id, currDir); 
                if (md != null) {
                    nodes.add(createTransactionNode(md, true)); 
                }
            } else {
                fileObjectsToDelete.add(fo);
            }
	}
        RequestProcessor.getDefault().post(new Runnable () {
            public void run() {
                for (Iterator<FileObject> it = fileObjectsToDelete.iterator(); it.hasNext(); ) {
                    try {
                        it.next().delete();
                    } catch (IOException e) {
                        Logger.getLogger("global").log(Level.INFO, null, e);
                    }
                }
            }
        });
	
	numtns = nodes.size();
 	tns = new TransactionNode[numtns]; 
	for(int i=0;i<numtns;++i) 
	    tns[i] = nodes.elementAt(i);
	currTrans.add(tns);


	savedTrans.remove(savedTrans.getNodes());
	nodes = new Vector<>();
	e = saveDir.getData(false);
	while(e.hasMoreElements()) {

	    fo = (FileObject)e.nextElement();
	    id = fo.getName();
	    if(debug) 
		log("getting current transaction: " + id); //NOI18N 
	    // Retrieve the monitordata 
	    md = retrieveMonitorData(id, saveDir); 
            if (md != null) {
                nodes.add(createTransactionNode(md, false));
            }
	}
	 
	numtns = nodes.size();
	tns = new TransactionNode[numtns]; 
	for(int i=0;i<numtns;++i) {
	    tns[i] = nodes.elementAt(i);
	    if(debug) 
		log("Adding saved node" + tns[i].toString()); //NOI18N 
		    
	}
	savedTrans.add(tns);
    }
    
    private int parseStatusCode(String statusCode) {
        if (statusCode == null) {
            return -1;
        }
        // The statusCode is expected to look like e.g. "404: Not Found", if not 
        // the status code was not resolved, which mostly means there was no error => 200
        int statusCodeNum = 200;
        try {
            int idx = statusCode.indexOf(':');
            if (idx != -1) {
                statusCode = statusCode.substring(0, idx);
                statusCodeNum = Integer.valueOf(statusCode);
            }
        } catch(NumberFormatException nfe) {
            // ignore
        }
        return statusCodeNum;
    }
	    
    private TransactionNode createTransactionNode(MonitorData md, boolean current) {

	if(debug) log("createTransactionNode(MonitorData)"); //NOI18N 
	Dispatches dis = null;
        RequestData rd = md.getRequestData();
	try { 
	    dis = md.getDispatches();
	}
	catch(Exception ex) { 
	    // Any parsing exception at this point, just ignore this
	    // part of the request
	} 

	TransactionNode node = null;
	
	// No dispatched requests, we add a regular transaction node
	if(dis == null || dis.sizeDispatchData() == 0 ) {
	    
	    if(debug) log("No dispatched requests"); //NOI18N 
	    node = new TransactionNode(md.getAttributeValue("id"), // NOI18N
				       md.getAttributeValue("method"), // NOI18N
				       md.getAttributeValue("resource"), //NOI18N
				       current,
                                       parseStatusCode((rd == null) ? null : rd.getAttributeValue("status"))); // NOI18N
	}
	else {

	    int numChildren = dis.sizeDispatchData();
	    if(debug) log("We had some dispatched requests: " + //NOI18N 
			  String.valueOf(numChildren));
	    if(debug) log("\t for id " + //NOI18N 
			  md.getAttributeValue("resource")); //NOI18N 
	    // Create all the children. 1
	    Children.Array nested = new Children.Array();
	    
	    // First we create an array of children that has the same
	    // size as the set of nodes. 
	    NestedNode[] nds = new NestedNode[numChildren];
	    for(int i=0; i<numChildren; ++i) {
		if(debug) { 
		    log("Getting a new monitor data object"); //NOI18N 
		    log(dis.getDispatchData(i).getAttributeValue("resource")); //NOI18N 
		}
		nds[i] = createNestedNode(dis.getDispatchData(i),
					  md.getAttributeValue("method"), // NOI18N
					  null, i); 
	    }
	    
	    nested.add(nds);
	    node = new TransactionNode(md.getAttributeValue("id"), // NOI18N
				       md.getAttributeValue("method"), // NOI18N
				       md.getAttributeValue("resource"), //NOI18N
				       nested, 
                                       current,
                                       parseStatusCode((rd == null) ? null: rd.getAttributeValue("status"))); // NOI18N

	}
	return node;
    }
    

    private NestedNode createNestedNode(DispatchData dd, 
					String method,
					int[] locator,
					int index) {


	Dispatches dis = dd.getDispatches();
	NestedNode node = null;

	int[] newloc = null;
	if(locator != null) {
	    newloc = new int[locator.length + 1];
	    int j=0;
	    while(j<locator.length) { 
		newloc[j] = locator[j];
		++j;
	    }
	    newloc[j]=index;
	}
	else {
	    newloc = new int[1]; 
	    newloc[0] = index;
	}
	
	// No dispatched requests, we add a regular transaction node
	if(dis == null || dis.sizeDispatchData() == 0 ) {
            RequestData rd = dd.getRequestData();
	    node = new NestedNode(dd.getAttributeValue("resource"),// NOI18N
                            method, 
                            newloc, 
                            parseStatusCode((rd == null) ? null : rd.getAttributeValue("status"))); // NOI18N
	}
	else {
	    int numChildren = dis.sizeDispatchData();
	    Children.Array nested = new Children.Array();
	    NestedNode[] nds = new NestedNode[numChildren];
	    for(int i=0; i<numChildren; ++i) {
		nds[i] = createNestedNode(dis.getDispatchData(i),
					  method, newloc, i); 
	    }
	    
	    nested.add(nds);
	    node = new NestedNode(dd.getAttributeValue("resource"), // NOI18N
                              method, 
                              nested, 
                              newloc, 
                              parseStatusCode(dd.getRequestData().getAttributeValue("status"))); // NOI18N
	}
	return node;
    }


    /**
     * Sets the machine name and port of the web server. Not used in
     * this version, we do not support remote debugging.
     */
    static void setServer(String loc, int p) {
	port = p;
	server = loc;
	return;
    }

    void setComparator(Comparator comp) {
	currTrans.setComparator(comp);
	savedTrans.setComparator(comp);
    }

    /** This method toggles whether the request uses the browser's
     * cookie or a cookie specified by the user. In 3.6, it is not
     * possible to configure the monitor to use user-specified
     * cookies, but I leave the method, in case it becomes possible in
     * the future. Basically, we can no longer set the cookie on the
     * server side (the Servlet APIs does not provide any method for
     * doing this) but we could technically tell the browser that
     * issues the replay request to send another cookie (the APIs for
     * that are not there now). If so, the feature can be
     * reintroduced. 
     */
    void setUseBrowserCookie(boolean value) { 
	useBrowserCookie = value;
	if(debug) 
	    log("Setting useBrowserCookie to " + //NOI18N
		String.valueOf(useBrowserCookie));
    }

    boolean getUseBrowserCookie() { 
	return useBrowserCookie; 
    }
    
    /**
     * @param node A node on the Monitor GUI
     * @return a data record
     * Convenience method - this gets the DataRecord corresponding to
     * a node on the TransactionView panel from the cache if it is
     * present. This is used to display the data from the node. 
     */
    DataRecord getDataRecord(AbstractNode node) {
	return getDataRecord(node, true);
    }
        
    /**
     * @param node A node on the Monitor GUI
     * @param fromCache true if it is OK to get the data record from
     * the cache
     * @return a data record
     */
    DataRecord getDataRecord(AbstractNode anode, boolean fromCache) {

	if(debug) log("Entered getDataRecord()"); //NOI18N
	 
	if(anode instanceof TransactionNode) {

	    if(debug) log("TransactionNode"); //NOI18N
	    
	    // Since this method is used to retrieve data records for
	    // the purposes of displaying the transaction, we cache
	    // the result
            MonitorData md = getMonitorData((TransactionNode)anode,
					    fromCache, true);
            if (md == null) {
                String msg = NbBundle.getMessage(Controller.class, "MSG_NoMonitorData");
                Logger.getLogger("global").log(Level.INFO, msg);
                return null;
            }
	    return (DataRecord)md;
	}
	else if(anode instanceof NestedNode) {

	    NestedNode node = (NestedNode)anode;
	    
	    if(debug) log(node.toString()); 

	    int index[] = node.getIndex();

	    AbstractNode parent = (AbstractNode)node.getParentNode();
	    if(parent == null) {
		if(debug) log("null parent, something went wrong!"); //NOI18N
		return null;
	    }
	    
	    while(parent != null && !(parent instanceof TransactionNode)) {
		if(debug) log("Parent is not transaction node"); //NOI18N
		if(debug) log(parent.toString()); 
		parent = (AbstractNode)(parent.getParentNode());
	    }
	    
	    if(debug) log("We got the transaction node"); //NOI18N

	    // We get the data record, from cache if it is present,
	    // and cache the node also
            MonitorData md = getMonitorData((TransactionNode)parent,
					    true, true); 
            if (md == null) {
                String msg = NbBundle.getMessage(Controller.class, "MSG_NoMonitorData");
                Logger.getLogger("global").log(Level.INFO, msg);
                return null;
            }
	    DataRecord dr = (DataRecord)md;
	    int[] nodeindex = node.getIndex();
	    
	    int c = 0;
	    while(c<nodeindex.length) {
		if(debug) log("Doing the data record cycle"); //NOI18N
		if(debug) log(String.valueOf(c) + ":" + //NOI18N
			      String.valueOf(nodeindex[c])); 
		Dispatches dis = dr.getDispatches();
		dr = (DataRecord)dis.getDispatchData(nodeindex[c]);
		++c;
	    }
	    return dr;
	}
	return null;
    }
    
    /**
     * @param node A node on the Monitor GUI
     * @param fromCache true if it is OK to get the data record from
     * the cache
     * @param cacheIt true if it is OK to cache the data that we
     * retrieve 
     * @return a data record, <code>null</code> if monitor date could not be got
     */
    MonitorData getMonitorData(TransactionNode node, 
				      boolean fromCache,
				      boolean cacheIt) {

	String id = node.getID();
	Hashtable ht = null;
	FileObject dir = null;
	 
	if(node.isCurrent()) {
	    ht = currBeans;
	    dir = currDir;
	    if(debug) log("node is current"); //NOI18N 
	}
	else {
	    ht = saveBeans;
	    dir = saveDir;
	}
	
	if(debug) {
	    log("node id is " + node.getID()); //NOI18N 
	    log("using directory " + dir.getName()); //NOI18N 
	}

	if(fromCache && ht.containsKey(id)) 
	    return (MonitorData)(ht.get(id));
	    
	MonitorData md = retrieveMonitorData(id, dir);
	if(cacheIt && md != null) ht.put(id, md);
	return md;
    }

    /**
     * @param id The ID of the record
     * @param dirS The name of the directory in which the transaction
     * resides 
     **/    
    MonitorData retrieveMonitorData(String id, String dirS) {

	if(debug) 
	    log("retrieveMonitorData(String, String)"); //NOI18N 
	if(!haveDirectories()) {
	    // PENDING - report the error property
	    log("Couldn't get the directory"); //NOI18N 
	    return null;
	}
	
	FileObject dir = null;
	
	if (dirS.equalsIgnoreCase(currDirStr))  dir = currDir;
	else if (dirS.equalsIgnoreCase(saveDirStr)) dir = saveDir;
	else if (dirS.equalsIgnoreCase(replayDirStr)) dir = replayDir;

	if(debug) log("Directory = " + dir.getName()); //NOI18N 
	return retrieveMonitorData(id, dir);
    }
    
    /**
     * @param id The ID of the record.
     * @param dir The directory in which the transaction resides.
     * @return monitor date, <code>null</code> if monitor date could not be retrieved.
     */
    MonitorData retrieveMonitorData(String id, FileObject dir) {

	// PENDING - this method needs an error reporting mechanism in
	// case the monitor data cannot be retrieved. Now it will just
	// return null. 
	if(debug)
	    log("retrieveMonitorData(String, FileObject)"); //NOI18N 
	if(!haveDirectories()) {
	    // PENDING - report the error property
	    log("Couldn't get the directory"); //NOI18N 
	    return null;
	}
	
	MonitorData md = null;
	FileObject fo = null;
	FileLock lock = null; 
	InputStreamReader in = null;
	
	try {
	    fo = dir.getFileObject(id, "xml"); // NOI18N
	    if(debug) log("From file: " + //NOI18N 
			  FileUtil.toFile(fo).getAbsolutePath()); 
	    if(debug) log("Locking it..."); //NOI18N 
	    lock = fo.lock();
	    if(debug) log("Getting InputStreamReader"); //NOI18N 
	    in = new InputStreamReader(fo.getInputStream()); 
	    if(debug) log("Creating monitor data"); //NOI18N 
	    md = MonitorData.createGraph(in);
	    try {
		if(dir == replayDir) fo.delete(lock);
	    }
	    catch(IOException ioex2) {} 
	} 
	catch(FileAlreadyLockedException falex) {
            Logger.getLogger("global").log(Level.INFO, null, falex);
	    if(debug) { 
		log("File is locked: " + fo.getNameExt()); //NOI18N 
		falex.printStackTrace();
	    }
	}
	catch(IOException ioex) {
            Logger.getLogger("global").log(Level.INFO, null, ioex);
	    if(debug) { 
		log("Couldn't read data file: " + fo.getNameExt()); //NOI18N 
		ioex.printStackTrace();
	    }
	}
	catch(Exception ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
	    if(debug) { 
		log("Something went wrong when retrieving record"); //NOI18N 
		ex.printStackTrace();
	    }
	}
	finally {
	    try { in.close(); }
	    catch(Throwable t) {}
	    if(lock != null) lock.releaseLock();
	}
	if(debug) log("We're done!"); //NOI18N 
	return md;
    }

    private void showReplay(final URL url) throws UnknownHostException,
	                                    IOException {
	
	if(debug) 
	    log("showReplay(URL url) url is " + url.toString()); // NOI18N
	// First we check that we can find a host of the name that's
	// specified 
	ServerCheck sc = new ServerCheck(url.getHost());
	if(debug) log("host is " + url.getHost()); //NOI18N
	Thread t = new Thread(sc);
	t.start();
	try {
	    t.join(2000);
	}
	catch(InterruptedException ie) {
	}
	t = null; 
	if(!sc.isServerGood()) {
	    if(debug) 
		log("showReplay(): No host"); // NOI18N
	    throw new UnknownHostException();
	}
	
	if(debug) log("performed server check"); // NOI18N

	// Next we see if we can connect to the server
	try {
	    Socket server = new Socket(url.getHost(), url.getPort());
	    server.close();
	    server = null;
	}
	catch(UnknownHostException uhe) {
	    if(debug) log("showReplay(): uhe2"); // NOI18N
	    throw uhe;
	}
	catch(IOException ioe) {
	    if(debug) 
		log("showReplay(): No service"); // NOI18N
	    throw ioe;
	}
	
	if(debug) log("showReplay(): reaching the end..."); // NOI18N

        // window system code must be run in AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
	    }});
    }

    // PENDING - use the logger instead
    private static void log(final String s) {
	System.out.println("Controller::" + s); //NOI18N
    }


    private static URL getSampleHTTPServerURL() {
	    FileObject fo = FileUtil.getConfigFile("HTTPServer_DUMMY");
	    if (fo == null) {
	        return null;
	    }
	    URL u = URLMapper.findURL(fo, URLMapper.NETWORK);
	    return u;
    }

    boolean checkServer(boolean replay) { 

	try { 
	    URL u = getSampleHTTPServerURL();
	    if(debug) log("Getting HTTP server - url " + u);
	    if (u.getProtocol().equals("http")) {
    	    //HttpServer.getRepositoryRoot();
	        if(debug) log("Got the HTTP server");
	        return true;
	    }
	}
	catch(Throwable t) { 

	    if(debug) { 
		log("Exception: " + t.getMessage());
		t.printStackTrace();
	    }

	    Object[] options = {
		NbBundle.getBundle(Controller.class).getString("MON_OK"),
	    };
	    String msg = null;
	    if(replay) 
		msg = NbBundle.getBundle(Controller.class).getString("MON_CantReplay"); 
	    else
		msg = NbBundle.getBundle(Controller.class).getString("MON_NoServer");

	    
	    NotifyDescriptor noServerDialog = 
		new NotifyDescriptor(msg,
				     NbBundle.getBundle(Controller.class).getString("MON_NoServerTitle"),
				     NotifyDescriptor.DEFAULT_OPTION,
				     NotifyDescriptor.INFORMATION_MESSAGE,
				     options,
				     options[0]);
	    DialogDisplayer.getDefault().notify(noServerDialog);
	}
	return false;
    }
    
    /**
     * Does the server we try to replay on exist? 
     */
    class ServerCheck implements Runnable {	 

	boolean serverGood = false;
	String serverName = null;
	
	ServerCheck(String name) {
	    serverName = name;
	}
	
	public void run() {
	    try {
		InetAddress.getByName(serverName);
		serverGood = true;
		
	    }
	    catch (UnknownHostException e) {
		serverGood = false; 
	    }	 
	}
	
	boolean isServerGood() {
	    return serverGood;
	}
	
    }

    /**
     * Sort by time
     */
    class CompTime implements Comparator {

	boolean descend = true;

	CompTime(boolean descend) {
	    this.descend = descend;
	}

	public int compare(Object o1, Object o2) {

	    if(debug) { 
		log("In compareTime"); //NOI18N
		log("Comparing " + String.valueOf(o1) + //NOI18N
		    " and " + String.valueOf(o2)); //NOI18N
		log("Cast the nodes"); //NOI18N
	    }

	    TransactionNode n1 = (TransactionNode)o1;
	    TransactionNode n2 = (TransactionNode)o2;

	    if(debug) {
		try {
		    log(n1.getID());
		    log(n2.getID());
		}
		catch(Exception ex) {}
	    }

	    int result;
	    if(descend)
		result = n1.getID().compareTo(n2.getID());
	    else result = n2.getID().compareTo(n1.getID());
	    if(debug) log("End of compareTime"); //NOI18N
	    return result;
	}
    }

    // PENDING 
    // Really dumb way of forcing this, but I couldn't get the tree to 
    // repaint... Will remove this method when that works. 
    void updateNodeNames() {
	
	TransactionNode tn;
	
	Node[] nodes = currTrans.getNodes();
	int size = nodes.length;
	for(int i=0; i<size; ++i) {
	    tn = (TransactionNode)nodes[i];
	    tn.setNameString();
	}
	
	nodes = savedTrans.getNodes();
	size = nodes.length;
	for(int i=0; i<size; ++i) {
	    tn = (TransactionNode)nodes[i];
	    tn.setNameString();
	}
    }
    
    /**
     * Sort alphabetically
     */
    class CompAlpha implements Comparator {

	public int compare(Object o1, Object o2) {
	    if(debug) log("In compareAlpha"); //NOI18N
	    TransactionNode n1 = (TransactionNode)o1;
	    TransactionNode n2 = (TransactionNode)o2;
	    if(debug) log("cast the nodes"); //NOI18N
	    if(debug) {
		log("Comparing " + String.valueOf(o1) + //NOI18N
		    " and " + String.valueOf(o2)); //NOI18N
		try {
		    log("names"); //NOI18N
		    log(n1.getName());
		    log(n2.getName());
		    log("IDs");  //NOI18N
		    log(n1.getID());
		    log(n2.getID());
		}
		catch(Exception ex) {}
	    }
	    int diff = n1.getName().compareTo(n2.getName());
	    if(diff == 0)
		return n1.getID().compareTo(n2.getID());
	    else
		return diff;
	}
    }
} // Controller
