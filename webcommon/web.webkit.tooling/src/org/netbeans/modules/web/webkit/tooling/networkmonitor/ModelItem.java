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
package org.netbeans.modules.web.webkit.tooling.networkmonitor;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.netbeans.modules.web.webkit.tooling.console.BrowserConsoleLogger;
import static org.netbeans.modules.web.webkit.tooling.console.BrowserConsoleLogger.getProjectPath;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.InputOutput;

class ModelItem implements PropertyChangeListener {
    private static final RequestProcessor RP = new RequestProcessor(ModelItem.class.getName(), 5);
    private final Network.Request request;
    private final Network.WebSocketRequest wsRequest;
    private ChangeListener changeListener;
    private String data = "";
    private final BrowserFamilyId browserFamilyId;
    private final Project project;
    private final AtomicBoolean dataLoaded = new AtomicBoolean(false);

    public ModelItem(Network.Request request, Network.WebSocketRequest wsRequest,
            BrowserFamilyId browserFamilyId, Project project) {
        this.request = request;
        this.wsRequest = wsRequest;
        this.browserFamilyId = browserFamilyId;
        this.project = project;
        if (this.request != null) {
            this.request.addPropertyChangeListener(WeakListeners.propertyChange(this, this.request));
        } else {
            this.wsRequest.addPropertyChangeListener(WeakListeners.propertyChange(this, this.wsRequest));
        }
    }

    public boolean canBeShownToUser() {
        if (wsRequest != null) {
            return true;
        }
        if (("script".equals(request.getInitiatorType()) &&
                request.getResponse() != null && !"Image".equals(request.getResponseType()) ||
            (request.getResponse() != null && "XHR".equals(request.getResponseType())))) {
            return true;
        }

        if (browserFamilyId == BrowserFamilyId.JAVAFX_WEBVIEW) {
            // WebView does not have "script" initiator type:
            if (("other".equals(request.getInitiatorType()) &&
                    request.getResponse() != null && !"Image".equals(request.getResponseType()) &&
                    !"Document".equals(request.getResponseType())) ) {
                return true;
            }
        }

        if (request.getResponseCode() != -1 && request.getResponseCode() >= 400) {
            return true;
        }

        return request.isFailed();
    }

    public boolean hasPostData() {
        return request != null && request.getRequest().get("postData") != null;
    }

    public boolean hasResponseData() {
        return request != null && request.hasData();
    }

    public boolean hasFrames() {
        return wsRequest != null && !wsRequest.getFrames().isEmpty();
    }

    public boolean hasCallStack() {
        return request != null && request.getInitiator() != null &&
                (request.getInitiator().get("stackTrace") != null ||
                 request.getInitiator().get("stack") != null);
    }

    Project getProject() {
        return project;
    }

    Network.Request getRequest() {
        return request;
    }

    private String getPostData() {
        return (String)request.getRequest().get("postData");
    }

    /**
     * Returns URL of the request represented by this model item.
     * 
     * @return URL of the request represented by this model item.
     */
    String getURL() {
        String url;
        if (request != null) {
            url = (String)request.getRequest().get("url");
        } else {
            url = wsRequest.getURL();
        }
        return url;
    }

    /**
     * Returns type/method of the request represented by this model item.
     * 
     * @return returns {@code WebSocket} for WebSocket connections,
     * returns HTTP method of the request otherwise.
     */
    String getHTTPMethod() {
        String method;
        if (request != null) {
            method = (String)request.getRequest().get("method"); // NOI18N
        } else {
            method = "WebSocket"; // NOI18N
        }
        return method;
    }

    /**
     * Returns the status (response code) of the request represented by this
     * model item.
     * 
     * @return status (response code) of the request represented by this
     * model item.
     */
    int getStatus() {
        int status = 0;
        if (request != null) {
            status = request.getResponseCode();
        }
        return status;
    }

    /**
     * Returns the content type of the request represented by this model item.
     * 
     * @return content type of the request represented by this model item.
     */
    String getContentType() {
        String contentType = null;
        if (hasResponseData()) {
            contentType = stripDownContentType((JSONObject)request.getResponse().get("headers")); // NOI18N
        }
        return contentType;
    }

    @Override
    public String toString() {
        if (request != null) {
            String s = (String)request.getRequest().get("url");
            s = s.replace("http://", "").replace("https://", "").replace("file:///", "");
            int index = s.indexOf("?");
            if (index != -1) {
                s = s.substring(0, index);
            }
            return (String)request.getRequest().get("method") + " " + s;
        } else {
            String s = String.valueOf(wsRequest.getURL());
            s = s.replace("ws://", "");
            return s;
        }
    }

    void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Network.Request.PROP_RESPONSE_DATA.equals(evt.getPropertyName())) {
            startLoadingData();
        }
        fireChange();
    }

    public JSONObject getRequestHeaders() {
        if (request != null) {
            JSONObject requestHeaders = (JSONObject)request.getRequest().get("headers");
            JSONObject r = request.getResponse();
            if (r != null) {
                r = (JSONObject)r.get("requestHeaders");
                if (r != null) {
                    for (Object o : r.entrySet()) {
                        Map.Entry m = (Map.Entry)o;
                        requestHeaders.put(m.getKey(), m.getValue());
                    }
                }
            }
            return requestHeaders;
        } else {
            JSONObject r = wsRequest.getHandshakeRequest();
            if (r == null) {
                return null;
            }
            return (JSONObject)r.get("headers");
        }
    }

    public JSONObject getResponseHeaders() {
        if (request != null) {
            JSONObject r = request.getResponse();
            if (r == null) {
                return null;
            }
            return (JSONObject)r.get("headers");
        } else {
            JSONObject r = wsRequest.getHandshakeResponse();
            if (r == null) {
                return null;
            }
            return (JSONObject)r.get("headers");
        }
    }

    public void updateHeadersPane(JTextPane pane) {
        try {
            updateTextPaneImpl(pane);
            pane.setCaretPosition(0);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateTextPaneImpl(JTextPane pane) throws BadLocationException {
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyledDocument doc = pane.getStyledDocument();
        Style boldStyle = doc.addStyle("bold", defaultStyle);
        StyleConstants.setBold(boldStyle, true);
        Style errorStyle = doc.addStyle("error", defaultStyle);
        StyleConstants.setBold(errorStyle, true);
        StyleConstants.setForeground(errorStyle, Color.red);
        Style paragraphStyle = doc.addStyle("paragraph", defaultStyle);
        StyleConstants.setFontSize(paragraphStyle, StyleConstants.getFontSize(paragraphStyle)+5);
        StyleConstants.setForeground(paragraphStyle, Color.gray);
        pane.setText("");

        if (request != null) {
            doc.insertString(doc.getLength(), "Request URL: ", boldStyle);
            doc.insertString(doc.getLength(), (String)request.getRequest().get("url")+"\n", defaultStyle);
            doc.insertString(doc.getLength(), "Method: ", boldStyle);
            doc.insertString(doc.getLength(), (String)request.getRequest().get("method")+"\n", defaultStyle);
            JSONObject r = getResponseHeaders();
            if (r != null) {
                int statusCode = request.getResponseCode();
                doc.insertString(doc.getLength(), "Status: ", boldStyle);
                String status = (String)r.get("Status");
                if (status == null) {
                    status = statusCode == -1 ? "" : ""+statusCode +
                            " " + request.getResponse().get("statusText");
                }
                doc.insertString(doc.getLength(), status+"\n",
                        statusCode >= 400 ? errorStyle : defaultStyle);
                Boolean fromCache = (Boolean)r.get("fromDiskCache");
                if (Boolean.TRUE.equals(fromCache)) {
                    doc.insertString(doc.getLength(), "From Disk Cache: ", boldStyle);
                    doc.insertString(doc.getLength(), "yes\n", defaultStyle);
                }
            } else if (request.isFailed()) {
                doc.insertString(doc.getLength(), "Status: ", boldStyle);
                doc.insertString(doc.getLength(), "Request was cancelled.\n", errorStyle);
            }
        } else {
            doc.insertString(doc.getLength(), "Request URL: ", boldStyle);
            doc.insertString(doc.getLength(), wsRequest.getURL()+"\n", defaultStyle);
            doc.insertString(doc.getLength(), "Status: ", boldStyle);
            if (wsRequest.getErrorMessage() != null) {
                doc.insertString(doc.getLength(), wsRequest.getErrorMessage()+"\n", errorStyle);
            } else {
                doc.insertString(doc.getLength(), wsRequest.isClosed() ? "Closed\n" :
                    wsRequest.getHandshakeResponse() == null ? "Opening\n" : "Open\n", defaultStyle);
            }
        }

        JSONObject requestHeaders = getRequestHeaders();
        if (requestHeaders == null) {
            return;
        }
        doc.insertString(doc.getLength(), "\n", defaultStyle);
        doc.insertString(doc.getLength(), "Request Headers\n", paragraphStyle);
        printHeaders(pane, requestHeaders, doc, boldStyle, defaultStyle);

        if (getResponseHeaders() != null) {
            doc.insertString(doc.getLength(), "\n", defaultStyle);
            doc.insertString(doc.getLength(), "Response Headers\n", paragraphStyle);
            printHeaders(pane, getResponseHeaders(), doc, boldStyle, defaultStyle);
        }
    }

    private void printHeaders(JTextPane pane, JSONObject headers,
            StyledDocument doc, Style boldStyle, Style defaultStyle) throws BadLocationException {

        assert headers != null;
        Set keys = new TreeSet(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((String)o1).compareToIgnoreCase((String)o2);
            }

        });
        keys.addAll(headers.keySet());
        for (Object oo : keys) {
            String key = (String)oo;
            doc.insertString(doc.getLength(), key+": ", boldStyle);
            String value = (String)headers.get(key);
            doc.insertString(doc.getLength(), value+"\n", defaultStyle);
        }
    }

    private void fireChange() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChangeListener l = changeListener;
                if (l != null) {
                    l.stateChanged(null);
                }
            }
        });
    }

    private void loadRequestData() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                assert request.hasData();
                String res = request.getResponseData();
                data = res != null ? res : "";
                fireChange();
            }
        });
    }

    public void updateResponsePane(JEditorPane pane, boolean rawData) {
        if (!hasResponseData()) {
            return;
        }
        try {
            updateResponseDataImpl(pane, rawData);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void updateFramesPane(JEditorPane pane, boolean rawData) {
        if (!hasFrames()) {
            return;
        }
        try {
            updateFramesImpl(pane, rawData);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void startLoadingData() {
        if (!request.hasData() || !canBeShownToUser() || dataLoaded.getAndSet(true)) {
            return;
        }
        data = "loading...";
        loadRequestData();
    }

    private void updateResponseDataImpl(JEditorPane pane, boolean rawData) throws BadLocationException {
        assert data != null;
        if (rawData || data.isEmpty()) {
            pane.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
            pane.setText(data);
        } else {
            String contentType = getContentType();
            reformatAndUseRightEditor(pane, data, contentType);
        }
        pane.setCaretPosition(0);
    }

    private void updateFramesImpl(JEditorPane pane, boolean rawData) throws BadLocationException {
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyledDocument doc = (StyledDocument)pane.getDocument();
        Style timingStyle = doc.addStyle("timing", defaultStyle);
        StyleConstants.setForeground(timingStyle, Color.lightGray);
        Style infoStyle = doc.addStyle("comment", defaultStyle);
        StyleConstants.setForeground(infoStyle, Color.darkGray);
        StyleConstants.setBold(infoStyle, true);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        pane.setText("");
        StringBuilder sb = new StringBuilder();
        int lastFrameType = -1;
        for (Network.WebSocketFrame f : wsRequest.getFrames()) {
            int opcode = f.getOpcode();
            if (opcode == 0) { // "continuation frame"
                opcode = lastFrameType;
            } else {
                lastFrameType = opcode;
            }
            if (opcode == 1) { // "text frame"
                if (!rawData) {
                    doc.insertString(doc.getLength(), formatter.format(f.getTimestamp()), timingStyle);
                    doc.insertString(doc.getLength(), f.getDirection() == Network.Direction.SEND ? " SENT " : " RECV ", timingStyle);
                }
                doc.insertString(doc.getLength(), f.getPayload()+"\n", defaultStyle);
            } else if (opcode == 2) { // "binary frame"
                if (!rawData) {
                    doc.insertString(doc.getLength(), formatter.format(f.getTimestamp()), timingStyle);
                    doc.insertString(doc.getLength(), f.getDirection() == Network.Direction.SEND ? " SENT " : " RECV ", timingStyle);
                }
                // XXX: binary data???
                doc.insertString(doc.getLength(), f.getPayload()+"\n", defaultStyle);
            } else if (opcode == 8) { // "close frame"
                if (!rawData) {
                    doc.insertString(doc.getLength(), formatter.format(f.getTimestamp()), timingStyle);
                    doc.insertString(doc.getLength(), f.getDirection() == Network.Direction.SEND ? " SENT " : " RECV ", timingStyle);
                }
                doc.insertString(doc.getLength(), "Frame closed\n", infoStyle);
            }
        }
        data = sb.toString();
        pane.setCaretPosition(0);
    }

    public void updatePostDataPane(JEditorPane pane, boolean rawData) {
        if (hasPostData()) {
            if (rawData) {
                pane.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
                pane.setText(getPostData());
            } else {
                String contentType = stripDownContentType(getRequestHeaders());
                reformatAndUseRightEditor(pane, getPostData(), contentType);
            }
        }
    }

    void updateCallStack(InputOutput io) {
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (hasCallStack()) {
            List<ConsoleMessage.StackFrame> callStack = request.getInitiatorCallStack();
            for (ConsoleMessage.StackFrame sf : callStack) {
                String projectUrl = getProjectPath(project, sf.getURLString());
                io.getOut().print(sf.getFunctionName()+ " ");
                String text = "(" +
                        projectUrl+":"+sf.getLine()+":"+sf.getColumn()+")";
                BrowserConsoleLogger.MyListener l = new BrowserConsoleLogger.MyListener(project, sf.getURLString(), sf.getLine(), sf.getColumn());
                if (l.isValidHyperlink()) {
                    try {
                        io.getOut().println(text, l);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    io.getOut().println(text);
                }
            }

        }
    }

    boolean isError() {
        if (wsRequest != null) {
            return wsRequest.getErrorMessage() != null;
        } else {
            return request.isFailed() || request.getResponseCode() >= 400;
        }
    }

    boolean isLive() {
        return wsRequest != null && !wsRequest.isClosed();
    }

    private static String stripDownContentType(JSONObject o) {
        assert o != null;
        String contentType = (String)o.get("Content-Type");
        if (contentType == null) {
            contentType = (String)o.get("content-type");
        }
        if (contentType == null) {
            return null;
        }
        int index = contentType.indexOf(";");
        if (index != -1) {
            contentType = contentType.substring(0, index);
        }
        return contentType;
    }

    private static void reformatAndUseRightEditor(JEditorPane pane, String data, String contentType) {
        if (contentType == null) {
            contentType = "text/plain"; // NOI18N
        } else {
            contentType = contentType.trim();
        }
        if ("application/javascript".equals(contentType)) {
            // check whether this JSONP response, that is a JS method call returning JSON:
            String json = getJSONPResponse(data);
            if (json != null) {
                data = json;
                contentType = "application/json";
            }
        }
        if ("application/json".equals(contentType) || "text/x-json".equals(contentType)) {
            data = reformatJSON(data);
            contentType = "text/x-json";
        }
        if ("application/xml".equals(contentType)) {
            contentType = "text/xml";
        }
        EditorKit editorKit;
        try {
            editorKit = CloneableEditorSupport.getEditorKit(contentType);
        } catch (IllegalArgumentException iaex) {
            contentType = "text/plain"; // NOI18N
            editorKit = CloneableEditorSupport.getEditorKit(contentType);
        }
        pane.setEditorKit(editorKit);
        pane.setText(data);
    }

    private static String reformatJSON(String data) {
        Object o = JSONValue.parse(data);
        StringBuilder sb = new StringBuilder();
        if (o instanceof JSONArray) {
            jsonPrettyPrintArray((JSONArray)o, sb, 0);
        } else if (o instanceof JSONObject) {
            jsonPrettyPrintObject((JSONObject)o, sb, 0);
        }
        return sb.toString();
    }

    private static void jsonPrettyPrintObject(JSONObject jsonObject, StringBuilder sb, int indent) {
        print(sb, "{\n", indent);
        boolean first = true;
        for (Object o : jsonObject.entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            Map.Entry en = (Map.Entry)o;
            Object value = en.getValue();
            String key = "\"" + en.getKey() + "\"";
            if (value instanceof JSONObject) {
                print(sb, key+": ", indent+2);
                jsonPrettyPrintObject((JSONObject)value, sb, indent+2);
            } else if (value instanceof JSONArray) {
                print(sb, key+": ", indent+2);
                jsonPrettyPrintArray((JSONArray)value, sb, indent+2);
            } else if (value instanceof String) {
                print(sb, key+": \""+ ((String)value).replace("\"", "\\\"")+"\"", indent+2);
            } else {
                print(sb, key+": "+ value, indent+2);
            }
            first = false;
        }
        sb.append("\n");
        print(sb, "}", indent);
    }

    private static void jsonPrettyPrintArray(JSONArray jsonObject, StringBuilder sb, int indent) {
        print(sb, "[\n", indent);
        boolean first = true;
        for (Object value : jsonObject) {
            if (!first) {
                sb.append(",\n");
            }
            if (value instanceof JSONObject) {
                jsonPrettyPrintObject((JSONObject)value, sb, indent+2);
            } else if (value instanceof JSONArray) {
                jsonPrettyPrintArray((JSONArray)value, sb, indent+2);
            } else if (value instanceof String) {
                print(sb, "\""+((String)value).replace("\"", "\\\"")+"\"", indent+2);
            } else {
                print(sb, String.valueOf(value), indent+2);
            }
            first = false;
        }
        sb.append("\n");
        print(sb, "]", indent);
    }

    private static void print(StringBuilder sb, String text, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }
        sb.append(text);
    }

    static String getJSONPResponse(String data) {
        Pattern p = Pattern.compile("([0-9a-zA-Z_$]+?\\()([\\{\\[].*?[\\}\\]])(\\)[\\;]?[\n\r]?)", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        if (m.matches()) {
            return m.group(2);
        }
        return null;
    }

}
