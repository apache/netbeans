/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.performance.guitracker;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import java.util.Date;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Tracks activity within a GUI program, this activity loosely being the major
 * events within the lifetime of the application. The activity is recorded in a
 * simple object (@see ActionTracker.Tuple). Activity is tracked in "groups" of
 * actions (@see ActionTracker.EventList), and a new "group" is begun for each
 * TRACK_START received.
 */
public class ActionTracker {

    /**
     * Start of a sequence of recorded events.
     */
    public static final int TRACK_START = 1;

    /**
     * Painting happened. @see Painter
     */
    public static final int TRACK_PAINT = 2;

    /**
     * MOUSE_PRESSED event.
     */
    public static final int TRACK_MOUSE_PRESS = 10;
    /**
     * MOUSE_RELEASED event.
     */
    public static final int TRACK_MOUSE_RELEASE = 11;
    /**
     * MOUSE_DRAGGED event.
     */
    public static final int TRACK_MOUSE_DRAGGED = 12;
    /**
     * MOUSE_MOVED event.
     */
    public static final int TRACK_MOUSE_MOVED = 13;

    /**
     * KEY_PRESSED event.
     */
    public static final int TRACK_KEY_PRESS = 20;
    /**
     * KEY_RELEASED event.
     */
    public static final int TRACK_KEY_RELEASE = 21;

    private static final int TRACK_FRAME = 1000;
    private static final int TRACK_DIALOG = 2000;
    private static final int TRACK_COMPONENT = 3000;

    /**
     * COMPONENT_SHOWN event happened on a Frame or JFrame.
     */
    public static final int TRACK_FRAME_SHOW = TRACK_FRAME + ComponentEvent.COMPONENT_SHOWN;
    /**
     * COMPONENT_HIDDEN event happened on a Frame or JFrame.
     */
    public static final int TRACK_FRAME_HIDE = TRACK_FRAME + ComponentEvent.COMPONENT_HIDDEN;
    /**
     * COMPONENT_RESIZED event happened on a Frame or JFrame.
     */
    public static final int TRACK_FRAME_RESIZE = TRACK_FRAME + ComponentEvent.COMPONENT_RESIZED;

    /**
     * COMPONENT_SHOWN event happened on a Dialog or JDialog.
     */
    public static final int TRACK_DIALOG_SHOW = TRACK_DIALOG + ComponentEvent.COMPONENT_SHOWN;
    /**
     * COMPONENT_HIDDEN event happened on a Dialog or JDialog.
     */
    public static final int TRACK_DIALOG_HIDE = TRACK_DIALOG + ComponentEvent.COMPONENT_HIDDEN;
    /**
     * COMPONENT_RESIZED event happened on a Dialog or JDialog.
     */
    public static final int TRACK_DIALOG_RESIZE = TRACK_DIALOG + ComponentEvent.COMPONENT_RESIZED;

    /**
     * COMPONENT_SHOWN event happened on a Component.
     */
    public static final int TRACK_COMPONENT_SHOW = TRACK_COMPONENT + ComponentEvent.COMPONENT_SHOWN;
    /**
     * COMPONENT_HIDDEN event happened on a Component.
     */
    public static final int TRACK_COMPONENT_HIDE = TRACK_COMPONENT + ComponentEvent.COMPONENT_HIDDEN;
    /**
     * COMPONENT_RESIZED event happened on a Component.
     */
    public static final int TRACK_COMPONENT_RESIZE = TRACK_COMPONENT + ComponentEvent.COMPONENT_RESIZED;

    /**
     * Any messages the application wants to send.
     */
    public static final int TRACK_APPLICATION_MESSAGE = 50;

    /**
     * Any messages the application wants to send.
     */
    public static final int TRACK_CONFIG_APPLICATION_MESSAGE = 51;

    /**
     * Before/After messages.
     */
    public static final int TRACK_TRACE_MESSAGE = 52;
    /**
     * Should be used only for value of MY_START_EVENT or MY_END_EVENT
     */
    public static final int TRACK_OPEN_BEFORE_TRACE_MESSAGE = TRACK_TRACE_MESSAGE + 1;
    /**
     * Should be used only for value of MY_START_EVENT or MY_END_EVENT
     */
    public static final int TRACK_OPEN_AFTER_TRACE_MESSAGE = TRACK_TRACE_MESSAGE + 2;

    /**
     * FOCUS_GAINED event
     */
    public static final int TRACK_FOCUS_GAINED = 80;
    /**
     * FOCUS_LOST event
     */
    public static final int TRACK_FOCUS_LOST = 81;
    /**
     * unknown event
     */
    public static final int TRACK_INVOCATION = 82;
    /**
     * unknown event
     */
    public static final int TRACK_UNKNOWN = 83;

    /**
     * The name of the root element in generated XML.
     */
    public static final String TN_ROOT_ELEMENT = "action-tracking";
    /**
     * The name of the event-list element in generated XML.
     */
    public static final String TN_EVENT_LIST = "event-list";
    /**
     * The name of each event element in generated XML.
     */
    public static final String TN_EVENT = "event";
    /**
     * The attribute name for start time.
     */
    public static final String ATTR_START = "start";
    /**
     * The attribute name for descriptive phrase.
     */
    public static final String ATTR_NAME = "name";
    /**
     * The attribute name for the node-type.
     */
    public static final String ATTR_TYPE = "type";
    /**
     * The attribute name for the timestamp.
     */
    public static final String ATTR_TIME = "time";
    /**
     * The attribute name for calculated time difference since the start.
     */
    public static final String ATTR_TIME_DIFF_START = "diff";
    /**
     * The attribute name for calculated time difference since the last
     * MOUSE_DRAGGED event.
     */
    public static final String ATTR_TIME_DIFF_DRAG = "diffdrag";
    /**
     * The attribute measured for events we are measuring one start + one stop.
     */
    public static final String ATTR_MEASURED = "measured";

    // Instance of the ActionTracker
    private static ActionTracker instance = null;

    // List of event lists
    private LinkedList<EventList> eventLists = null;

    // Events gathered during one event tracking period
    private EventList/*Tuple*/ currentEvents = null;

    // Our AWT Event listener
    private OurAWTEventListener awt_listener = null;

    // Flag to handle connection to AWT
    private boolean connected = false;

    // Output file name
    private String fnActionOutput = null;

    // Flag to handle finished application
    private boolean exportXmlWhenScenarioFinished = false;

    // Flag to handle recording
    private boolean allowRecording = true;

    // document builder
    private DocumentBuilder dbld = null;

    // document builder factory
    private DocumentBuilderFactory dbfactory = null;

    // tranformer factory
    private TransformerFactory tfactory = null;

    // default awt event mask
    private long default_awt_event_mask = AWTEvent.COMPONENT_EVENT_MASK
            | AWTEvent.MOUSE_EVENT_MASK
            | AWTEvent.MOUSE_MOTION_EVENT_MASK
            | AWTEvent.KEY_EVENT_MASK;

    // awt event mask
    long awt_event_mask = -1;

    /**
     * Flag to print all logged events to System.out
     */
    private boolean interactive;

    //location for "actionTrackerXsl" file
    private static String actionTrackerXslLocation = "";

    /**
     * Retrieves the ActionTracker instance for this application. Rather than
     * constructing your own ActionTracker (note that the constructor is
     * <code>private</code>, meaning you can't construct your own) use this
     * method to get the instance.
     *
     * @return return ActionTracker instance
     */
    public static ActionTracker getInstance() {
        if (instance == null) {
            instance = new ActionTracker();
        }
        return instance;
    }

    /**
     * Creates a new instance of RepaintTracker, private so that this is a
     * singleton class.
     */
    private ActionTracker() {
        awt_event_mask = default_awt_event_mask;
    }

    /**
     * Turns on/off interactive mode.
     */
    void setInteractive(boolean interactive) {
        this.interactive = interactive;

    }

    /**
     * Stop actions recording
     */
    public void stopRecording() {
        allowRecording = false;
    }

    /**
     * Start actions recording
     */
    public void startRecording() {
        allowRecording = true;
    }

    /**
     * check whether it's recording
     *
     * @return true - it's recording, false - it isn't
     */
    public boolean isRecording() {
        return allowRecording;
    }

    /**
     * Set the default file name to output to for <code>outputAsXML</code>.
     *
     * @param fn file name
     */
    public void setOutputFileName(String fn) {
        fnActionOutput = fn;
    }

    /**
     * Set exportXmlWhenScenarioFinished property when the test finished to
     * allow export XML
     *
     * @param export set exportXmlWhenScenarioFinished
     */
    public void setExportXMLWhenScenarioFinished(boolean export) {
        exportXmlWhenScenarioFinished = export;
    }

    /**
     * Get the <i>current</i> <code>EventList</code> into which events are being
     * recorded.
     *
     * @return
     */
    public EventList getCurrentEvents() {
        return currentEvents;
    }

    /**
     * Remove memory of the <i>current</i> <code>EventList</code>.
     */
    public void forgetCurrentEvents() {
        currentEvents = null;
        if (eventLists != null) {
            eventLists.removeLast();
        }
    }

    /**
     * Get list of all events
     *
     * @return list of events
     */
    public LinkedList<EventList> getEventLists() {
        return eventLists;
    }

    /**
     * Remove memory of all recorded events.
     */
    public void forgetAllEvents() {
        if (eventLists != null) {
            eventLists.clear();
        }
        if (currentEvents != null) {
            currentEvents.clear();
        }
        currentEvents = null;
    }

    /**
     * Record a TRACK_START event. This causes a clean, fresh, and new EventList
     * to be begun (with the previous current EventList to be saved away).
     *
     * @param name
     */
    public void startNewEventList(String name) {
        if (eventLists == null) {
            eventLists = new LinkedList<EventList>();
        }

        currentEvents = new EventList(name);
        eventLists.add(currentEvents);
        currentEvents.start();
        startRecording();
        add(TRACK_START, "START", currentEvents.startMillies);
    }

    /**
     * Set AWT Event mask if not default_awt_event_mask is used
     *
     * @param mask to be set
     */
    public void setAWTEventListeningMask(long mask) {
        awt_event_mask = mask;
    }

    /**
     * Set AWT Event mask
     *
     * @return used mask
     */
    public long getAWTEventListengingMask() {
        return awt_event_mask;
    }

    /**
     * Manage the connection to the AWT <code>EventQueue</code>, recording
     * interesting events that go by.
     *
     * @param connect if true - connect to AWT, else disconnect
     */
    public void connectToAWT(boolean connect) {
        if (connect) {
            if (awt_listener == null) {
                awt_listener = new OurAWTEventListener(this);
            }
            if (!connected) {
                Toolkit.getDefaultToolkit().addAWTEventListener(awt_listener,
                        awt_event_mask);
            }
            connected = true;
        } else {
            if (awt_listener != null) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(awt_listener);
            }
            connected = false;
        }
    }

    /**
     * Add the <code>Tuple</code> to the current EventList.
     *
     * @param t tuple
     */
    public void add(Tuple t) {
        if (!isRecording()) {
            return;
        }
        if (currentEvents != null) {
            currentEvents.add(t);
        }
    }

    /**
     * Add a <code>Tuple</code> matching these parameters to the current
     * EventList.
     *
     * @param code
     * @param name
     * @param millies
     */
    public void add(int code, String name, long millies) {
        EventList ce = getCurrentEvents();
        add(new Tuple(code, name, millies, ce != null ? ce.startMillies : (long) -1));
    }

    /**
     * Add a <code>Tuple</code> matching these parameters to the current
     * EventList. The <code>time</code> parameter is derived from the current
     * time.
     *
     * @param code
     * @param name
     */
    public void add(int code, String name) {
        EventList ce = getCurrentEvents();
        add(new Tuple(code, name, ce != null ? ce.startMillies : (long) -1));
    }

    /**
     * Add a <code>Tuple</code> matching these parameters to the current
     * EventList. The <code>time</code> parameter is derived from the current
     * time.
     *
     * @param code
     * @param name
     * @param measured - if we log this as measured (User action = start point,
     * appropriate paint=stop point, measured time=result
     */
    public void add(int code, String name, boolean measured) {
        EventList ce = getCurrentEvents();
        Tuple t = new Tuple(code, name, System.nanoTime(), ce != null ? ce.startMillies : (long) -1, measured);
        add(t);
    }

    /**
     * Process an AWTEvent, and if it's interesting recording it in the current
     * EventList.
     *
     * @param event
     */
    public void add(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) event;
            int id = me.getID();
            if ((id == MouseEvent.MOUSE_PRESSED
                    || id == MouseEvent.MOUSE_RELEASED)) {

                String mr = id == MouseEvent.MOUSE_PRESSED
                        ? "MOUSE PRESSED" : "MOUSE RELEASED";
                int bmask = me.getButton();

                add(id == MouseEvent.MOUSE_PRESSED
                        ? TRACK_MOUSE_PRESS : TRACK_MOUSE_RELEASE,
                        mr
                        + " bmask=" + Integer.toString(bmask)
                        + " modifiers=" + MouseEvent.getMouseModifiersText(me.getModifiers())
                );
            }
            if (id == MouseEvent.MOUSE_MOVED
                    || id == MouseEvent.MOUSE_DRAGGED) {
                String mm = id == MouseEvent.MOUSE_MOVED
                        ? "MOUSE MOVED" : "MOUSE DRAGGED";

                add(id == MouseEvent.MOUSE_MOVED
                        ? TRACK_MOUSE_MOVED : TRACK_MOUSE_DRAGGED,
                        mm + " "
                        + Integer.toString(me.getX())
                        + ","
                        + Integer.toString(me.getY())
                );
            }
        } else if (event instanceof KeyEvent) {
            KeyEvent ke = (KeyEvent) event;
            int id = ke.getID();
            if (id == KeyEvent.KEY_PRESSED
                    || id == KeyEvent.KEY_RELEASED) {

                String kr = id == KeyEvent.KEY_PRESSED
                        ? "KEY PRESSED" : "KEY RELEASED";
                int kc = ke.getKeyCode();

                add(id == KeyEvent.KEY_PRESSED
                        ? ActionTracker.TRACK_KEY_PRESS
                        : ActionTracker.TRACK_KEY_RELEASE,
                        kr
                        + " keycode=" + Integer.toString(kc)
                        + " keytext=" + KeyEvent.getKeyText(kc)
                        + " modtext=" + KeyEvent.getKeyModifiersText(ke.getModifiers()));
            }
        } else if (event instanceof WindowEvent) {
            // Silent event - not tracked
            // WindowEvent we = (WindowEvent) event;
        } else if (event instanceof FocusEvent) {
            FocusEvent fe = (FocusEvent) event;
            int id = fe.getID();
            Component opposite = fe.getOppositeComponent();
            Component thisone = fe.getComponent();
            boolean temp = fe.isTemporary();
            if (id == FocusEvent.FOCUS_GAINED) {
                add(ActionTracker.TRACK_FOCUS_GAINED,
                        (temp ? "temp " : "perm ")
                        + "opp " + opposite
                        + "this " + thisone);
            } else if (id == FocusEvent.FOCUS_LOST) {
                add(ActionTracker.TRACK_FOCUS_LOST,
                        (temp ? "temp " : "perm ")
                        + "opp " + opposite
                        + "this " + thisone);
            }
        } else if (event instanceof ComponentEvent) {
            ComponentEvent ce = (ComponentEvent) event;
            int id = ce.getID();
            // ignore ComponentEvent.COMPONENT_MOVED & ComponentEvent.COMPONENT_RESIZED
            if (id == ComponentEvent.COMPONENT_HIDDEN
                    || id == ComponentEvent.COMPONENT_SHOWN) {

                Component c = ce.getComponent();

                if (c instanceof Frame || c instanceof JFrame) {
                    add(TRACK_FRAME + id, ce.paramString() + " " + logComponentAndItsParents(c));
                } else if (c instanceof Dialog || c instanceof JDialog) {
                    add(TRACK_DIALOG + id, ce.paramString() + " " + logComponentAndItsParents(c));
                } else {
                    add(TRACK_COMPONENT + id, ce.paramString() + " " + logComponentAndItsParents(c));
                }
            }
        } else if (event instanceof InvocationEvent) {
            // there is way too many InvocationEvents
            //            InvocationEvent ie = (InvocationEvent)event;
            //            add(TRACK_INVOCATION, ie.paramString());
        } else {
            add(TRACK_UNKNOWN, "Unknown event: " + event.paramString());
        }
    }

    public static String logComponentAndItsParents(Component c) {
        if (c instanceof Container) {
            return LoggingRepaintManager.logContainerAndItsParents((Container) c);
        } else {
            return LoggingRepaintManager.logComponent(c);
        }
    }

    /**
     * Notify the ActionTracker that the scenario is finishing, and is to be
     * called by the scenario, when it's <code>run</code> method is finishing.
     * Otherwise nothing in the system knows that it's finished.
     */
    public void scenarioFinished() {
        add(TRACK_APPLICATION_MESSAGE, "ScenarioFinished");
        if (exportXmlWhenScenarioFinished) {
            try {
                exportAsXML();
            } catch (Exception e) {
                System.err.println("Unable to export to XML because " + e);
                e.printStackTrace();
            }
        }
    }

    TransformerFactory getTransformerFactory() throws TransformerConfigurationException {
        if (tfactory == null) {
            tfactory = TransformerFactory.newInstance();
        }

        return tfactory;
    }

    DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if (dbfactory == null) {
            dbfactory = DocumentBuilderFactory.newInstance();
        }

        if (dbld == null) {
            dbld = dbfactory.newDocumentBuilder();
        }

        return dbld;
    }

    /**
     * Write all recorded event information, in XML format, to
     * <code>System.out</code>.
     *
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException
     */
    public void exportAsXML()
            throws ParserConfigurationException, TransformerConfigurationException,
            TransformerException {
        PrintStream out = System.out; // Default
        if (fnActionOutput != null) {
            try {
                out = new PrintStream(
                        new FileOutputStream(
                                new File(fnActionOutput)
                        )
                );
            } catch (Exception e) {
                out = System.out;
            }
        }
        exportAsXML(null, out);
    }

    /**
     * Write all recorded event information, in XML format, to the given
     * <code>PrintStream</code>.
     *
     * @param out
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException
     */
    public void exportAsXML(PrintStream out)
            throws ParserConfigurationException, TransformerConfigurationException,
            TransformerException {
        exportAsXML(null, out);
    }

    /**
     * Write all recorded event information, in XML format, to the given
     * <code>PrintStream</code>. In addition, it is transformed by the given
     * XSLT script.
     *
     * @param style
     * @param out
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException
     */
    public void exportAsXML(Document style, PrintStream out)
            throws ParserConfigurationException, TransformerConfigurationException,
            TransformerException {
        Document doc = getDocumentBuilder().getDOMImplementation().createDocument(null, TN_ROOT_ELEMENT, null);
        Element root = doc.getDocumentElement();

        // Construct the DOM contents by scanning through all EventLists,
        // and for each event adding everything to the DOM.
        for (EventList eventList : eventLists) {
            Element evlistElement = doc.createElement(TN_EVENT_LIST);
            root.appendChild(evlistElement);
            evlistElement.setAttribute(ATTR_START, Long.toString(eventList.getStartMillis()));
            evlistElement.setAttribute(ATTR_NAME, eventList.getName());

            //don't log repeated events
            Tuple previous = new Tuple(0, "", 0, 0);

            // For each Event
            for (Tuple t : Collections.unmodifiableList(eventList)) {
                // log only if it isn't the same
                if (t != null && !t.equals(previous)) {
                    Element eventElement = doc.createElement(TN_EVENT);
                    evlistElement.appendChild(eventElement);
                    eventElement.setAttribute(ATTR_TYPE, t.getCodeName());
                    eventElement.setAttribute(ATTR_NAME, getShortenName(t.getName()));
                    eventElement.setAttribute(ATTR_TIME, getTimeMillisForLog(t));
                    eventElement.setAttribute(ATTR_TIME_DIFF_START, Long.toString(t.getTimeDifference()));

                    if (t.getMeasured()) {
                        eventElement.setAttribute(ATTR_MEASURED, "true");
                    }
                }
                previous = t;
            }
        }

        // Now, transform it out
        Transformer tr = style != null
                ? getTransformerFactory().newTransformer(new DOMSource(style))
                : getTransformerFactory().newTransformer();

        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource docSrc = new DOMSource(doc);
        StreamResult rslt = new StreamResult(out);

        out.println("<?xml version=\"1.0\" ?>");
        out.println("<?xml-stylesheet type=\"text/xsl\" href=\"" + getPathToXsl() + "\" media=\"screen\"?>");
        tr.transform(docSrc, rslt);
    }

    private static String getShortenName(String name) {
        return name.replace("javax.swing", "j")
                   .replace("org.netbeans.modules", "o.n.m")
                   .replace("org.netbeans", "o.n")
                   .replace("org.openide.awt", "o.o.a")
                   .replace("org.openide", "o.o");
    }

    /**
     * Get name for the code
     *
     * @param code one of the TRACK_xxx codes
     * @return name of the code
     */
    public static String getNameForCode(int code) {
        String cname = "unk";
        switch (code) {
            case TRACK_START:
                cname = "start";
                break;
            case TRACK_PAINT:
                cname = "paint";
                break;
            case TRACK_MOUSE_PRESS:
                cname = "user_action";
                break;
            case TRACK_MOUSE_RELEASE:
                cname = "user_action";
                break;
            case TRACK_MOUSE_DRAGGED:
                cname = "user_action";
                break;
            case TRACK_MOUSE_MOVED:
                cname = "user_action";
                break;
            case TRACK_KEY_PRESS:
                cname = "user_action";
                break;
            case TRACK_KEY_RELEASE:
                cname = "user_action";
                break;
            case TRACK_FRAME_SHOW:
                cname = "paint";
                break;
            case TRACK_FRAME_HIDE:
                cname = "app_message";
                break;
            case TRACK_DIALOG_SHOW:
                cname = "paint";
                break;
            case TRACK_DIALOG_HIDE:
                cname = "app_message";
                break;
            case TRACK_COMPONENT_SHOW:
                cname = "paint";
                break;
            case TRACK_COMPONENT_HIDE:
                cname = "app_message";
                break;
            case TRACK_INVOCATION:
                cname = "app_message";
                break;
            case TRACK_UNKNOWN:
                cname = "unknown";
                break;
            case TRACK_APPLICATION_MESSAGE:
                cname = "app_message";
                break;
            case TRACK_CONFIG_APPLICATION_MESSAGE:
                cname = "config_message";
                break;
            case TRACK_TRACE_MESSAGE:
                cname = "trace_message";
                break;
        }
        return cname;
    }

    /**
     * Get value in Mili
     *
     * @param nano - value in nanoseconds
     * @return value in miliseconds
     */
    static long nanoToMili(long nano) {
        return nano / 1000000;
    }

    /**
     * Get time in millis and only changing part of the time (1000s)
     *
     * @param tuple
     * @return time in millis
     */
    private static String getTimeMillisForLog(Tuple t) {
        return Long.toString(t.getTimeMillis() - t.getTimeMillis() / 10000000 * 10000000);
    }

    /**
     * Set location for ActionTracker.xsl file
     *
     * @param xslLocation where the ActionTracker.xsl file is going to be placed
     */
    public void setXslLocation(String xslLocation) {
        ActionTracker.actionTrackerXslLocation = xslLocation;
    }

    /**
     * Create a relative path to XSL trnaformer for ActionTracker.xml log file
     *
     * @param file in the current workdir      * <pre>
    We expect that "work" and "results" directories have the same parent :
     * test_run/test_bag/user/class/method - directory after counting/moving results
     * PARENT/work/user/class/method - working directory
     * PARENT/results/testrun        - testrun directory
     *
     * test_run/test_bag/user/class/method
     * -> up test_run/test_bag/user/class/
     * -> up test_run/test_bag/user/
     * -> up test_run/test_bag/
     * -> Up to relative path from actionTrackerXslLocation=getWorkDir()
     * -> up test_run/
     * </pre>
     */
    private static String getPathToXsl() {
        StringBuilder pathToXsl = new StringBuilder();

        String workdir = System.getProperty("nbjunit.workdir");
        pathToXsl.append(workdir).append(java.io.File.separator);
        pathToXsl.append("../../../../../src/org/netbeans/modules/performance/resources/ActionTracker.xsl");

        return pathToXsl.toString();
    }

    /**
     * Record a list of @see ActionTracker.Tuple objects. This is a
     * <code>LinkedList</code> in disguise, so to access objects in the list
     * just use that mechanism.
     */
    public final class EventList extends LinkedList<Tuple> {

        // name of the event list
        private String name = "unknown";

        // start time
        private long startMillies = -1;

        /**
         * Create empty EventList
         */
        EventList() {
            super();
        }

        /**
         * Create list of events with specific name
         *
         * @param name name of the event list
         */
        public EventList(String name) {
            this();
            if (name == null || name.length() <= 0) {
                throw new RuntimeException("Must provide a name");
            }
            this.name = name;
        }

        /**
         * Cause the "start time" to be recorded. This value will only be
         * recorded once, and comes from <code>System.nanoTime()</code>.
         */
        public void start() {
            if (startMillies == -1) {
                startMillies = System.nanoTime();
            }
        }

        /**
         * Return the recorded "start time" for this list.
         *
         * @return start time in ms
         */
        public long getStartMillis() {
            return nanoToMili(startMillies);
        }

        /**
         * Get name of the event list
         *
         * @return name of the event list
         */
        public String getName() {
            return name;
        }

        @Override
        public boolean add(Tuple o) {
            if (interactive) {
                int c = o.getCode();
                if (c != TRACK_APPLICATION_MESSAGE
                        && c != TRACK_MOUSE_MOVED) {
                    System.out.println(o.toString());
                }
                if (c == TRACK_MOUSE_RELEASE
                        || c == TRACK_MOUSE_PRESS
                        || c == TRACK_KEY_PRESS) {
                    forgetAllEvents();
                    startNewEventList("ad hoc");
                }
            }
            if (o == null) {
                return false;
            }
            return super.add(o);
        }

        /**
         * String presentation of the event list
         *
         * @return string presentation of the event list
         */
        @Override
        public String toString() {
            return getName()
                    + " (" + this.size() + ") "
                    + new Date(getStartMillis()).toString();
        }
    }

    /**
     * Events to record into an EventList. The code is one of the
     * ActionTracker.TRACK_xxx values, and the name can be any String that makes
     * sense to you. The time (millies) comes from
     * <code>System.nanoTime()</code>.
     */
    public final class Tuple {

        /**
         * One of the ActionTracker.TRACK_xxx values
         */
        private int code;

        /**
         * name of the action
         */
        private String name;

        /**
         * time when action started
         */
        private long millies;

        /**
         * difference from a "start" time in millis
         */
        private long diffies;

        /**
         * this tuple we measure - help to distinguish which action/paint has
         * been measured
         */
        private boolean measured;

        private String measurementThreadName;

        /**
         * Create a tuple to track actions.
         *
         * @param code code of the action (one of the ActionTracker.TRACK_xxx
         * values)
         * @param name name of the action
         * @param start time when action started
         */
        public Tuple(int code, String name, long start) {
            this(code, name, System.nanoTime(), start);
        }

        /**
         * Create a tuple to track actions.
         *
         * @param code code of the action (one of the ActionTracker.TRACK_xxx
         * values)
         * @param name name of the action
         * @param millies time in milliseconds
         * @param start time when action started
         */
        public Tuple(int code, String name, long millies, long start) {
            this(code, name, millies, start, false);
        }

        /**
         * Create a tuple to track actions.
         *
         * @param code code of the action (one of the ActionTracker.TRACK_xxx
         * values)
         * @param name name of the action
         * @param start time when action started
         * @param millies current time
         * @param measured true if measured
         */
        public Tuple(int code, String name, long millies, long start, boolean measured) {
            this.code = code;
            this.name = name;
            this.millies = millies;
            this.diffies = millies - start;
            this.measured = measured;
            this.measurementThreadName = Thread.currentThread().getName();
            //System.err.println("new ActionTracker.Tuple " + toString()+" ,start="+start);
        }

        /**
         * Get the translation of the code into a String.
         *
         * @return translation of the code into the String
         */
        public String getCodeName() {
            return ActionTracker.getNameForCode(code);
        }

        /**
         * Get code of the action
         *
         * @return code of the action
         */
        public int getCode() {
            return code;
        }

        /**
         * Get name of the action
         *
         * @return name of the action
         */
        public String getName() {
            return name;
        }

        /**
         * Get current time in milliseconds
         *
         * @return time in ms
         */
        public long getTimeMillis() {
            return nanoToMili(millies);
        }

        /**
         * Get difference in milliseconds
         *
         * @return difference in ms
         */
        public long getTimeDifference() {
            return nanoToMili(diffies);
        }

        /**
         * Set this tuple as the one we measure (one start and one stop)
         *
         * @param measured - if true this is going to be measured
         */
        public void setMeasured(boolean measured) {
            this.measured = measured;
        }

        /**
         * Get whether this tuple is the one we measure (one start and one stop)
         *
         * @return true - if this one is going to be measured, false else
         */
        public boolean getMeasured() {
            return this.measured;
        }

        ;
        
        /**
         * Get name of the action
         * @return name of the thread
         */
        public String getMeasurementThreadName() {
            return measurementThreadName;
        }

        /**
         * Convert tuple to the string
         *
         * @return string presentation of tuple
         */
        @Override
        public String toString() {
            return this.getCodeName()
                    + " " + this.getName()
                    + " " + Long.toString(this.getTimeMillis())
                    + " " + Long.toString(this.getTimeDifference());
        }

        /**
         * Compare two tuples, they are equals if :
         * <li> code is the same
         * <li> name is the same
         * <li> time of occurrence is the same
         * <li> difference to start time is the same
         *
         * @param obj tuple to be compared with <i>this</i>
         * @return true if are equals, false else
         */
        @Override
        public boolean equals(Object obj) {
            Tuple t = (Tuple) obj;
            return this.getCode() == t.getCode()
                    && this.getName().equalsIgnoreCase(t.getName())
                    && (this.getTimeMillis() == t.getTimeMillis()
                    || this.getTimeDifference() == t.getTimeDifference());
        }

    }

    class OurAWTEventListener implements AWTEventListener {

        ActionTracker tracker = null;

        public OurAWTEventListener(ActionTracker t) {
            tracker = t;
        }

        @Override
        public void eventDispatched(AWTEvent event) {
            tracker.add(event);
        }
    }
}
