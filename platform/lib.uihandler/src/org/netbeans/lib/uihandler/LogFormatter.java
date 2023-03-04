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

package org.netbeans.lib.uihandler;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.util.Lookup;
import static java.util.Calendar.*;

/**this class uses most of XML Formater code, but it adds cause of throwable
 *
 * @author Jindrich Sedek
 */
class LogFormatter extends XMLFormatter{

    private static final int MAX_NUM_CAUSE = 20; // Do not recurse more than this number of causes during exception printing

    private final Pattern javaHome;
    private final Pattern userHome;
    private final Pattern netbeansUserDir;
    private final Pattern netbeansHome;
    private final List<Pattern> installDirs;
    private final Pattern filePrefix = Pattern.compile("file:", Pattern.LITERAL);
    private final Pattern nbjclPrefix = Pattern.compile("nbjcl:", Pattern.LITERAL);
    private final Pattern jarPrefix = Pattern.compile("jar:", Pattern.LITERAL);
    private final Pattern hexPattern = Pattern.compile("@[0-9a-fA-F]*");

    /** Creates a new instance of LogFormatter */
    public LogFormatter() {
        javaHome = convert(System.getProperty("java.home", ""));// NOI18N
        userHome = convert(System.getProperty("user.home", ""));// NOI18N
        netbeansUserDir = convert(System.getProperty("netbeans.user", ""));// NOI18N
        netbeansHome = convert(System.getProperty("netbeans.home", ""));// NOI18N
        String nbdirsStr = System.getProperty("netbeans.dirs");// NOI18N
        if (nbdirsStr != null){
            String [] fields = nbdirsStr.split(File.pathSeparator);
            Pattern [] resultFields = new Pattern[fields.length];
            for (int i = 0; i < fields.length; i++) {
                resultFields[i] = convert(fields[i]);
            }
            installDirs = Arrays.asList(resultFields);
        }else{
            installDirs = Collections.emptyList();
        }
    }

    private Pattern convert(String str){
        try{
            String name = new File(str).toURI().toURL().toString();
            return Pattern.compile(name, Pattern.LITERAL);
        }catch(MalformedURLException exc){
            Logger.getLogger(LogFormatter.class.getName()).log(Level.INFO, "unaccessible file", exc);// NOI18N
        }
        return null;
    }
    
    private void a2(StringBuffer sb, int x) {
        if (x < 10) {
            sb.append('0');
        }
        sb.append(x);
    }
    
    private void escape(StringBuffer sb, String text) {
        if (text == null) {
            text = "<null>";// NOI18N
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '<') {
                sb.append("&lt;");// NOI18N
            } else if (ch == '>') {
                sb.append("&gt;");// NOI18N
            } else if (ch == '&') {
                sb.append("&amp;");// NOI18N
            } else if (ch < 0x20 && ch != '\t' && ch != '\r' && ch != '\n') { // #119820
                sb.append('^').append((char) (ch + 0x40));
            } else {
                sb.append(ch);
            }
        }
    }
    private String doReplace(String where, Pattern pattern, String replacement){
        return pattern.matcher(where).replaceAll(Matcher.quoteReplacement(replacement));
    }
    
    private void printFrame(StackTraceElement frame, StringBuffer sb){
        if (frame == null){ // might be caused by OOM bugs see #145298
            return;
        }
        sb.append("    <frame>\n");// NOI18N
        sb.append("      <class>");// NOI18N
        escape(sb, frame.getClassName());
        sb.append("</class>\n");// NOI18N
        sb.append("      <method>");// NOI18N
        escape(sb, frame.getMethodName());
        sb.append("</method>\n");// NOI18N
        // Check for a line number.
        if (frame.getLineNumber() >= 0) {
            sb.append("      <line>");// NOI18N
            sb.append(frame.getLineNumber());
            sb.append("</line>\n");// NOI18N
        }
        sb.append("      <file>");// NOI18N
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        Class clazz=null;
        URL jarName=null;
        String fileName=null;
        try{
            clazz = loader.loadClass(frame.getClassName());
        }catch(Throwable exc){
            Logger.getLogger(LogFormatter.class.getName()).log(Level.FINE, "Class loading error", exc);// NOI18N
        }
        if (clazz != null){
            String[] fields = clazz.getName().split("\\.");// NOI18N
            if (fields.length> 0){
                jarName = clazz.getResource(fields[fields.length-1]+".class");// NOI18N
            }
            if (jarName!= null){
                fileName = jarName.toString();
                int index = fileName.indexOf("!");// NOI18N
                if (index!= -1){
                    fileName = fileName.substring(0, index);
                }
                fileName = doReplace(fileName, jarPrefix, "");// NOI18N
                if (javaHome != null){
                    fileName = doReplace(fileName, javaHome, "${java.home}");// NOI18N
                }
                if (netbeansHome != null){
                    fileName = doReplace(fileName, netbeansHome, "${netbeans.home}");// NOI18N
                }
                if (netbeansUserDir != null){
                    fileName = doReplace(fileName, netbeansUserDir, "${user.dir}");// NOI18N
                }
                for (Iterator<Pattern> it = installDirs.iterator(); it.hasNext();) {
                    fileName = doReplace(fileName, it.next(), "${netBeansDir}");// NOI18N
                }
                if (userHome != null){
                    fileName = doReplace(fileName, userHome, "${user.home}");// NOI18N
                }
                fileName = doReplace(fileName, filePrefix, "");// NOI18N
                fileName = doReplace(fileName, nbjclPrefix, "");// NOI18N
                escape(sb, fileName);
            }
        }
        sb.append("</file>\n");// NOI18N
        sb.append("    </frame>\n");// NOI18N
    }
    
    
    private void printCause(Throwable th, StringBuffer sb, StackTraceElement[] causedTrace){
        printCause(th, sb, causedTrace, 0);
    }
    
    private void printCause(Throwable th, StringBuffer sb, StackTraceElement[] causedTrace, int depth){
        sb.append("  <exception>\n");// NOI18N
        sb.append("   <message>");// NOI18N
        escape(sb, th.toString());
        sb.append("</message>\n");// NOI18N
        StackTraceElement[] trace = th.getStackTrace();
        int m = trace.length-1;
        int n = causedTrace.length-1;
        while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;
        
        for (int i=0; i <= m; i++) {
            printFrame(trace[i], sb);
        }
        sb.append("   <more>");// NOI18N
        sb.append(framesInCommon);
        sb.append("</more>\n");// NOI18N
        sb.append("  </exception>\n");// NOI18N
        if (th.getCause() != null && depth < MAX_NUM_CAUSE){
            printCause(th.getCause(), sb, trace, depth + 1);
        }
    }
    
    // Report on the state of the throwable.
    private void printThrown(Throwable th, StringBuffer sb){
        sb.append("  <exception>\n");// NOI18N
        sb.append("    <message>");// NOI18N
        escape(sb, th.toString());
        sb.append("</message>\n");// NOI18N
        StackTraceElement trace[] = th.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            printFrame(trace[i], sb);
        }
        sb.append("  </exception>\n");// NOI18N
        if (th.getCause() != null){
            printCause(th.getCause(), sb, trace);
        }
    }
    
    // Append the time and date in ISO 8601 format
    private void appendISO8601(StringBuffer sb, long millis) {
        Date date = new Date(millis);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        sb.append(calendar.get(YEAR));
        sb.append('-');
        a2(sb, calendar.get(MONTH) + 1);
        sb.append('-');
        a2(sb, calendar.get(DAY_OF_MONTH));
        sb.append('T');
        a2(sb, calendar.get(HOUR_OF_DAY));
        sb.append(':');
        a2(sb, calendar.get(MINUTE));
        sb.append(':');
        a2(sb, calendar.get(SECOND));
    }
    
    /**
     * Format the given message to XML.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public @Override String format(LogRecord record) {
        StringBuffer sb = new StringBuffer(1000);
        sb.append("<record>\n");// NOI18N
        
        sb.append("  <date>");// NOI18N
        appendISO8601(sb, record.getMillis());
        sb.append("</date>\n");// NOI18N
        
        sb.append("  <millis>");// NOI18N
        sb.append(record.getMillis());
        sb.append("</millis>\n");// NOI18N
        
        sb.append("  <sequence>");// NOI18N
        sb.append(record.getSequenceNumber());
        sb.append("</sequence>\n");// NOI18N
        
        String name = record.getLoggerName();
        if (name != null) {
            sb.append("  <logger>");// NOI18N
            escape(sb, name);
            sb.append("</logger>\n");// NOI18N
        }
        
        sb.append("  <level>");// NOI18N
        String level = Integer.toString(record.getLevel().intValue());
        escape(sb, level);
        sb.append("</level>\n");// NOI18N
        
        if (record.getSourceClassName() != null) {
            sb.append("  <class>");// NOI18N
            escape(sb, record.getSourceClassName());
            sb.append("</class>\n");// NOI18N
        }
        
        if (record.getSourceMethodName() != null) {
            sb.append("  <method>");// NOI18N
            escape(sb, record.getSourceMethodName());
            sb.append("</method>\n");// NOI18N
        }
        
        sb.append("  <thread>");// NOI18N
        sb.append(record.getThreadID());
        sb.append("</thread>\n");// NOI18N
        
        String message = record.getMessage();
        if (message != null) {
            sb.append("  <message>");// NOI18N
            escape(sb, message);
            sb.append("</message>\n");// NOI18N
        }
                
        // If the message is being localized, output the key, resource
        // bundle name, and params.
        ResourceBundle bundle = record.getResourceBundle();
        try {
            if (bundle != null && bundle.getString(message) != null) {
                sb.append("  <key>");// NOI18N
                escape(sb, message);
                sb.append("</key>\n");// NOI18N
                sb.append("  <catalog>");// NOI18N
                escape(sb, record.getResourceBundleName());
                sb.append("</catalog>\n");// NOI18N
            }
        } catch (Exception exc) {
            // The message is not in the catalog.  Drop through.
            Logger.getLogger(LogFormatter.class.getName()).log(Level.FINE, "Catalog loading error", exc);// NOI18N
        }

        Object parameters[] = record.getParameters();
        //  Check to see if the parameter was not a messagetext format
        //  or was not null or empty
        if ( parameters != null && parameters.length != 0
                && (message == null || message.indexOf("{") == -1) ) {
            for (int i = 0; i < parameters.length; i++) {
                sb.append("  <param>");// NOI18N
                try {
                    escape(sb, paramToString(parameters[i]));
                } catch (Exception ex) {
                    sb.append("???");// NOI18N
                }
                sb.append("</param>\n");// NOI18N
            }
        }
        
        if (record.getThrown() != null) {
            printThrown(record.getThrown(), sb);
        }
        
        sb.append("</record>\n");// NOI18N
        return sb.toString();
    }
    
    private String paramToString(Object obj) {
        if (obj == null) {
            return "null"; // NOI18N
        }
        
        if (obj instanceof JMenuItem) {
            JMenuItem ab = (JMenuItem)obj;
            Action a = ab.getAction();
            if (a == null) {
                // fall thru to AbstractButton
            } else {
                return ab.getClass().getName() + '[' + paramToString(a) + ']';
            }
        }
        if (obj instanceof AbstractButton) {
            AbstractButton ab = (AbstractButton)obj;
            return ab.getClass().getName() + '[' + ab.getText() + ']';
        }
        if (obj instanceof Action) {
            Action a = (Action)obj;
            if (
                    a.getClass().getName().endsWith("$DelegateAction") && // NOI18N
                    a.getClass().getName().startsWith("org.openide") // NOI18N
                    ) {
                return hexPattern.matcher(a.toString()).replaceAll("," + a.getValue(Action.NAME));
            }
            return a.getClass().getName() + '[' + a.getValue(Action.NAME) + ']';
        }
        if (obj instanceof Component) {
            Component c = (Component)obj;
            return c.getClass().getName() + '[' + c.getName() + ']'; // NOI18N
        }
        
        return obj.toString();
    }
}
