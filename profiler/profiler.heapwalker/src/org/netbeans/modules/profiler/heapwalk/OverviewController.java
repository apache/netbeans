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

package org.netbeans.modules.profiler.heapwalk;

import java.io.File;
import java.lang.Thread.State;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapSummary;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.JavaFrameGCRoot;
import org.netbeans.lib.profiler.heap.ThreadObjectGCRoot;
import org.netbeans.modules.profiler.api.GoToSource;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.heapwalk.details.api.DetailsSupport;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.netbeans.modules.profiler.heapwalk.ui.OverviewControllerUI;
import org.netbeans.modules.profiler.heapwalk.ui.icons.HeapWalkerIcons;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
@NbBundle.Messages({
    "OverviewController_NotAvailableMsg=&lt;not available&gt;",
    "OverviewController_SystemPropertiesString=System properties:",
    "OverviewController_SummaryString=Basic info:",
    "OverviewController_EnvironmentString=Environment:",
    "OverviewController_FileItemString=<b>File: </b>{0}",
    "OverviewController_FileSizeItemString=<b>File size: </b>{0}",
    "OverviewController_DateTakenItemString=<b>Date taken: </b>{0}",
    "OverviewController_UpTimeItemString=<b>JVM Uptime: </b>{0}",
    "OverviewController_TotalBytesItemString=<b>Total bytes: </b>{0}",
    "OverviewController_TotalClassesItemString=<b>Total classes: </b>{0}",
    "OverviewController_TotalInstancesItemString=<b>Total instances: </b>{0}",
    "OverviewController_ClassloadersItemString=<b>Classloaders: </b>{0}",
    "OverviewController_GcRootsItemString=<b>GC roots: </b>{0}",
    "OverviewController_FinalizersItemString=<b>Number of objects pending for finalization: </b>{0}",
    "OverviewController_OOMELabelString=<b>Heap dumped on OutOfMemoryError exception</b>",
    "OverviewController_OOMEItemString=<b>Thread causing OutOfMemoryError exception: </b>{0}",
    "OverviewController_OsItemString=<b>OS: </b>{0} ({1}) {2}",
    "OverviewController_ArchitectureItemString=<b>Architecture: </b>{0} {1}",
    "OverviewController_JavaHomeItemString=<b>Java Home: </b>{0}",
    "OverviewController_JavaVersionItemString=<b>Java Version: </b>{0}",
    "OverviewController_JavaVendorItemString=<b>Java Vendor: </b>{0}",
    "OverviewController_JvmItemString=<b>JVM: </b>{0}  ({1}, {2})",
    "OverviewController_ShowSysPropsLinkString=Show System Properties",
    "OverviewController_ThreadsString=Threads at the heap dump:",
    "OverviewController_ShowThreadsLinkString=Show Threads"
})
public class OverviewController extends AbstractController {

    public static final String SHOW_SYSPROPS_URL = "file:/sysprops"; // NOI18N
    public static final String SHOW_THREADS_URL = "file:/threads"; // NOI18N
    private static final String OPEN_THREADS_URL = "file:/stackframe/";     // NOI18N
    private static final String CLASS_URL_PREFIX = "file://class/"; // NOI18N
    private static final String INSTANCE_URL_PREFIX = "file://instance/";   // NOI18N
    private static final String THREAD_URL_PREFIX = "file://thread/";   // NOI18N
    private static final String LINE_PREFIX = "&nbsp;&nbsp;&nbsp;&nbsp;"; // NOI18N
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    private HeapFragmentWalker heapFragmentWalker;
    private SummaryController summaryController;
    private boolean systemPropertiesComputed = false;
    private Properties systemProperties;
    private String stackTrace;
    private JavaClass java_lang_Class;
    private ThreadObjectGCRoot oome;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public OverviewController(SummaryController summaryController) {
        this.summaryController = summaryController;
        heapFragmentWalker = summaryController.getHeapFragmentWalker();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public SummaryController getSummaryController() {
        return summaryController;
    }

    // --- Internal interface ----------------------------------------------------

    protected AbstractButton createControllerPresenter() {
        return ((OverviewControllerUI) getPanel()).getPresenter();
    }

    // --- Protected implementation ----------------------------------------------
    protected JPanel createControllerUI() {
        return new OverviewControllerUI(this);
    }

    void showInThreads(Instance instance) {
        ((OverviewControllerUI) getPanel()).showInThreads(instance);
    }
    
    public String computeSummary() {
        File file = heapFragmentWalker.getHeapDumpFile();
        Heap heap = heapFragmentWalker.getHeapFragment();
        HeapSummary hsummary = heap.getSummary();
        long finalizers = computeFinalizers(heap);
        NumberFormat numberFormat = (NumberFormat)NumberFormat.getInstance().clone();
        numberFormat.setMaximumFractionDigits(1);
        
        oome = getOOMEThread(heap);
        int nclassloaders = heapFragmentWalker.countClassLoaders();
        
        String filename = LINE_PREFIX
                + Bundle.OverviewController_FileItemString(
                    file != null && file.exists() ? file.getAbsolutePath() : 
                        Bundle.OverviewController_NotAvailableMsg());
        
        String filesize = LINE_PREFIX
                + Bundle.OverviewController_FileSizeItemString(
                    file != null && file.exists() ?
                        numberFormat.format(file.length()/(1024 * 1024.0)) + " MB" : // NOI18N
                        Bundle.OverviewController_NotAvailableMsg());
        
        String dateTaken = LINE_PREFIX
                + Bundle.OverviewController_DateTakenItemString(new Date(hsummary.getTime()).toString());
        
        String liveBytes = LINE_PREFIX
                + Bundle.OverviewController_TotalBytesItemString(numberFormat.format(hsummary.getTotalLiveBytes()));
        
        String liveClasses = LINE_PREFIX
                + Bundle.OverviewController_TotalClassesItemString(numberFormat.format(heap.getAllClasses().size()));
        
        String liveInstances = LINE_PREFIX
                + Bundle.OverviewController_TotalInstancesItemString(numberFormat.format(hsummary.getTotalLiveInstances()));
        
        String classloaders = LINE_PREFIX
                + Bundle.OverviewController_ClassloadersItemString(numberFormat.format(nclassloaders));
        
        String gcroots = LINE_PREFIX
                + Bundle.OverviewController_GcRootsItemString(numberFormat.format(heap.getGCRoots().size()));
        
        String finalizersInfo = LINE_PREFIX
                + Bundle.OverviewController_FinalizersItemString(
                          finalizers >= 0 ? numberFormat.format(finalizers) :
                          Bundle.OverviewController_NotAvailableMsg()
                );

        String oomeString = "";
        if (oome != null) {
            Instance thread = oome.getInstance();
            String threadName = htmlize(getThreadName(heap, thread));
            String threadUrl = "<a href='"+ THREAD_URL_PREFIX + thread.getJavaClass().getName() + "/" + thread.getInstanceId() + "'>" + threadName + "</a>"; // NOI18N
            oomeString = "<br><br>" + LINE_PREFIX // NOI18N
                + Bundle.OverviewController_OOMELabelString() + "<br>" + LINE_PREFIX // NOI18N
                + Bundle.OverviewController_OOMEItemString(threadUrl);
        }
        String memoryRes = Icons.getResource(ProfilerIcons.HEAP_DUMP);
        return "<b><img border='0' align='bottom' src='nbresloc:/" + memoryRes + "'>&nbsp;&nbsp;" // NOI18N
                + Bundle.OverviewController_SummaryString() + "</b><br><hr>" + dateTaken + "<br>" + filename + "<br>" + filesize + "<br><br>" + liveBytes // NOI18N
                + "<br>" + liveClasses + "<br>" + liveInstances + "<br>" + classloaders + "<br>" + gcroots + "<br>" + finalizersInfo + oomeString; // NOI18N
    }

    public String computeEnvironment() {
        String sysinfoRes = Icons.getResource(HeapWalkerIcons.SYSTEM_INFO);
        String header =  "<b><img border='0' align='bottom' src='nbresloc:/" + sysinfoRes + "'>&nbsp;&nbsp;" // NOI18N
                + Bundle.OverviewController_EnvironmentString() + "</b><br><hr>";   // NOI18N
        Properties sysprops = getSystemProperties();
        
        if (sysprops == null) {
            return header + LINE_PREFIX + Bundle.OverviewController_NotAvailableMsg();
        }
        
        Heap heap = heapFragmentWalker.getHeapFragment();
        HeapSummary hsummary = heap.getSummary();
        long startupTime = computeStartupTime(heap);

        String patchLevel = sysprops.getProperty("sun.os.patch.level", ""); // NOI18N
        String os = LINE_PREFIX
                + Bundle.OverviewController_OsItemString(
                    sysprops.getProperty("os.name", Bundle.OverviewController_NotAvailableMsg()), // NOI18N
                    sysprops.getProperty("os.version", ""), // NOI18N
                    ("unknown".equals(patchLevel) ? "" : patchLevel) // NOI18N
        );
        
        String arch = LINE_PREFIX
                + Bundle.OverviewController_ArchitectureItemString(
                    sysprops.getProperty("os.arch", Bundle.OverviewController_NotAvailableMsg()), // NOI18N
                    sysprops.getProperty("sun.arch.data.model", "?") + "bit" // NOI18N
        );
        
        String jdk = LINE_PREFIX
                + Bundle.OverviewController_JavaHomeItemString(
                    sysprops.getProperty("java.home", Bundle.OverviewController_NotAvailableMsg())); // NOI18N

        String version = LINE_PREFIX
                + Bundle.OverviewController_JavaVersionItemString(
                    sysprops.getProperty("java.version", Bundle.OverviewController_NotAvailableMsg())); // NOI18N
        
        String jvm = LINE_PREFIX
                + Bundle.OverviewController_JvmItemString(
                    sysprops.getProperty("java.vm.name", Bundle.OverviewController_NotAvailableMsg()), // NOI18N
                    sysprops.getProperty("java.vm.version", ""), // NOI18N
                    sysprops.getProperty("java.vm.info", "") // NOI18N
        );

        String vendor = LINE_PREFIX
                + Bundle.OverviewController_JavaVendorItemString(
                    sysprops.getProperty("java.vendor", Bundle.OverviewController_NotAvailableMsg())); // NOI18N

        String uptimeInfo = LINE_PREFIX
                + Bundle.OverviewController_UpTimeItemString(startupTime >= 0 ? getTime(hsummary.getTime()-startupTime) :
                          Bundle.OverviewController_NotAvailableMsg()
                );

        
        return header + os + "<br>" + arch + "<br>" + jdk + "<br>" + version + "<br>" + jvm + "<br>" + vendor + // NOI18N
                "<br>" + uptimeInfo ; // NOI18N
    }
    
    public String computeSystemProperties(boolean showSystemProperties) {
        String propertiesRes = Icons.getResource(HeapWalkerIcons.PROPERTIES);
        String header = "<b><img border='0' align='bottom' src='nbresloc:/" + propertiesRes + "'>&nbsp;&nbsp;" // NOI18N
                + Bundle.OverviewController_SystemPropertiesString() + "</b><br><hr>"; // NOI18N
        Properties sysprops = getSystemProperties();
        
        if (sysprops == null) {
            return header + LINE_PREFIX + Bundle.OverviewController_NotAvailableMsg();
        }
        
        return header 
                + (showSystemProperties ? formatSystemProperties(sysprops)
                : (LINE_PREFIX + "<a href='" + SHOW_SYSPROPS_URL + "'>" + Bundle.OverviewController_ShowSysPropsLinkString() + "</a>")); // NOI18N
    }
    
    public String computeThreads(boolean showThreads) {
        String threadsWindowRes = Icons.getResource(ProfilerIcons.WINDOW_THREADS);
        return "<b><img border='0' align='bottom' src='nbresloc:/" + threadsWindowRes + "'>&nbsp;&nbsp;" // NOI18N
                + Bundle.OverviewController_ThreadsString() + "</b><br><hr>" // NOI18N
                + (showThreads ? getStackTrace()
                : (LINE_PREFIX + "<a href='" + SHOW_THREADS_URL + "'>" + Bundle.OverviewController_ShowThreadsLinkString() + "</a><br>&nbsp;")); // NOI18N
        // NOTE: the above HTML string should be terminated by newline to workaround HTML rendering bug in JDK 5, see Issue 120157
    }
    
    public void showURL(final String _urls) {
        BrowserUtils.performTask(new Runnable() {
            public void run() {
                String urls = _urls;
                if (urls.startsWith(OPEN_THREADS_URL)) {
                    urls = urls.substring(OPEN_THREADS_URL.length());
                    String parts[] = urls.split("\\|"); // NOI18N
                    String className = parts[0];
                    String method = parts[1];
                    int linenumber = Integer.parseInt(parts[2]);
                    GoToSource.openSource(heapFragmentWalker.getHeapDumpProject(), className, method, linenumber);
                } else if (urls.startsWith(INSTANCE_URL_PREFIX)) {
                    urls = urls.substring(INSTANCE_URL_PREFIX.length());

                    final String[] id = urls.split("/"); // NOI18N
                    long instanceId = Long.parseLong(id[2]);
                    final Instance i = heapFragmentWalker.getHeapFragment().getInstanceByID(instanceId);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (i != null) {
                                heapFragmentWalker.getClassesController().showInstance(i);
                            } else {
                                ProfilerDialogs.displayError(Bundle.AnalysisController_CannotResolveInstanceMsg(id[1], id[0]));
                            }
                        }
                    });
                } else if (urls.startsWith(CLASS_URL_PREFIX)) {
                    urls = urls.substring(CLASS_URL_PREFIX.length());
                    final String[] id = urls.split("/"); // NOI18N
                    long jclsid = Long.parseLong(id[1]);

                    final JavaClass c = heapFragmentWalker.getHeapFragment().getJavaClassByID(jclsid);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (c != null) {
                                heapFragmentWalker.getClassesController().showClass(c);
                            } else {
                                ProfilerDialogs.displayError(Bundle.AnalysisController_CannotResolveClassMsg(id[0]));
                            }
                        }
                    });
                } else if (urls.startsWith(THREAD_URL_PREFIX)) {
                    urls = urls.substring(THREAD_URL_PREFIX.length());
                    String[] id = urls.split("/"); // NOI18N
                    long threadid = Long.parseLong(id[1]);
                    final Instance i = heapFragmentWalker.getHeapFragment().getInstanceByID(threadid);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (i != null) {
                                showInThreads(i);
                            } else {
                                System.err.println(">>> OverviewController: unexpected null instance for showInThreads"); // NOI18N
                            }
                        }
                    });
                }
            }
        });
    }
            
    private long computeFinalizers(Heap heap) {
        JavaClass finalizerClass = heap.getJavaClassByName("java.lang.ref.Finalizer"); // NOI18N
        if (finalizerClass != null) {
            Instance queue = (Instance) finalizerClass.getValueOfStaticField("queue"); // NOI18N
            if (queue != null) {
                Long len = (Long) queue.getValueOfField("queueLength"); // NOI18N
                if (len != null) {
                    return len.longValue();
                }
            }
        }
        return -1;
    }

    private long computeStartupTime(Heap heap) {
        JavaClass jmxFactoryClass = heap.getJavaClassByName("sun.management.ManagementFactoryHelper"); // NOI18N
        if (jmxFactoryClass == null) {
            jmxFactoryClass = heap.getJavaClassByName("sun.management.ManagementFactory"); // NOI18N
        }
        if (jmxFactoryClass != null) {
            Instance runtimeImpl = (Instance) jmxFactoryClass.getValueOfStaticField("runtimeMBean"); // NOI18N
            if (runtimeImpl != null) {
                Long len = (Long) runtimeImpl.getValueOfField("vmStartupTime"); // NOI18N
                if (len != null) {
                    return len.longValue();
                }
            }
        }
        return -1;
    }
    
    private ThreadObjectGCRoot getOOMEThread(Heap heap) {
        Collection<GCRoot> roots = heap.getGCRoots();

        for (GCRoot root : roots) {
            if(root.getKind().equals(GCRoot.THREAD_OBJECT)) {
                ThreadObjectGCRoot threadRoot = (ThreadObjectGCRoot)root;
                StackTraceElement[] stackTrace = threadRoot.getStackTrace();
                
                if (stackTrace!=null && stackTrace.length>=1) {
                    StackTraceElement ste = stackTrace[0];
                    
                    if (OutOfMemoryError.class.getName().equals(ste.getClassName()) && "<init>".equals(ste.getMethodName())) {  // NOI18N
                        return threadRoot;
                    }
                }
            }
        }
        return null;
    }
    
    private Properties getSystemProperties() {
        if (!systemPropertiesComputed) {
            systemProperties = heapFragmentWalker.getHeapFragment().getSystemProperties();
            systemPropertiesComputed = true;
        }
        
        return systemProperties;
    }
    
    private String formatSystemProperties(Properties properties) {
        StringBuilder text = new StringBuilder(200);
        List keys = new ArrayList();
        Enumeration en = properties.propertyNames();
        Iterator keyIt;
        
        while (en.hasMoreElements()) {
            keys.add(en.nextElement());
        }
        Collections.sort(keys);
        keyIt = keys.iterator();
        
        while (keyIt.hasNext()) {
            String key = (String) keyIt.next();
            String val = properties.getProperty(key);
            
            if ("line.separator".equals(key) && val != null) {  // NOI18N
                val = val.replace("\n", "\\n"); // NOI18N
                val = val.replace("\r", "\\r"); // NOI18N
            }
            
            text.append("<nobr>"+ LINE_PREFIX +"<b>"); // NOI18N
            text.append(key);
            text.append("</b>="); // NOI18N
            text.append(val);
            text.append("</nobr><br>"); // NOI18N
        }
        
        return text.toString();
    }
    
    private synchronized String getStackTrace() {
        if(stackTrace == null) {
            boolean gotoSourceAvailable = heapFragmentWalker.getHeapDumpProject() != null && GoToSource.isAvailable();
            StringBuilder sb = new StringBuilder();
            Heap h = heapFragmentWalker.getHeapFragment();
            Collection<GCRoot> roots = h.getGCRoots();
            Map<ThreadObjectGCRoot,Map<Integer,List<JavaFrameGCRoot>>> javaFrameMap = computeJavaFrameMap(roots);
            // Use this to enable VisualVM color scheme for threads dumps:
            // sw.append("<pre style='color: #cc3300;'>"); // NOI18N
            sb.append("<pre>"); // NOI18N
            for (GCRoot root : roots) {
                if(root.getKind().equals(GCRoot.THREAD_OBJECT)) {
                    ThreadObjectGCRoot threadRoot = (ThreadObjectGCRoot)root;
                    Instance threadInstance = threadRoot.getInstance();
                    if (threadInstance != null) {
                        String threadName = getThreadName(h, threadInstance);
                        Boolean daemon = (Boolean)threadInstance.getValueOfField("daemon"); // NOI18N
                        Integer priority = (Integer)threadInstance.getValueOfField("priority"); // NOI18N
                        Long threadId = (Long)threadInstance.getValueOfField("tid");    // NOI18N
                        Integer threadStatus = (Integer)threadInstance.getValueOfField("threadStatus"); // NOI18N
                        StackTraceElement stack[] = threadRoot.getStackTrace();
                        Map<Integer,List<JavaFrameGCRoot>> localsMap = javaFrameMap.get(threadRoot);
                        String style="";

                        if (threadRoot.equals(oome)) {
                            style="style=\"color: #FF0000\""; // NOI18N
                        }                        
                        // --- Use this to enable VisualVM color scheme for threads dumps: ---
                        // sw.append("&nbsp;&nbsp;<span style=\"color: #0033CC\">"); // NOI18N
                        sb.append("&nbsp;&nbsp;<a name=").append(threadInstance.getInstanceId()).append("></a><b ").append(style).append(">");   // NOI18N
                        // -------------------------------------------------------------------
                        sb.append("\"").append(htmlize(threadName)).append("\"").append(daemon.booleanValue() ? " daemon" : "").append(" prio=").append(priority);   // NOI18N
                        if (threadId != null) {
                            sb.append(" tid=").append(threadId);    // NOI18N
                        }
                        if (threadStatus != null) {
                            State tState = toThreadState(threadStatus.intValue());
                            sb.append(" ").append(tState);          // NOI18N
                        }
                        // --- Use this to enable VisualVM color scheme for threads dumps: ---
                        // sw.append("</span><br>"); // NOI18N
                        sb.append("</b><br>");   // NOI18N
                        // -------------------------------------------------------------------
                        if(stack != null) {
                            for(int i = 0; i < stack.length; i++) {
                                String stackElHref;
                                StackTraceElement stackElement = stack[i];
                                String stackElementText = htmlize(stackElement.toString());
                                
                                if (gotoSourceAvailable) {
                                    String className = stackElement.getClassName();
                                    String method = stackElement.getMethodName();
                                    int lineNo = stackElement.getLineNumber();
                                    String stackUrl = OPEN_THREADS_URL+className+"|"+method+"|"+lineNo; // NOI18N
                                    
                                    // --- Use this to enable VisualVM color scheme for threads dumps: ---
                                    // stackElHref = "&nbsp;&nbsp;<a style=\"color: #CC3300;\" href=\""+stackUrl+"\">"+stackElement+"</a>"; // NOI18N
                                    stackElHref = "<a href=\""+stackUrl+"\">"+stackElementText+"</a>";    // NOI18N
                                    // -------------------------------------------------------------------
                                } else {
                                    stackElHref = stackElementText;
                                }
                                sb.append("    at ").append(stackElHref).append("<br>");  // NOI18N
                                if (localsMap != null) {
                                    List<JavaFrameGCRoot> locals = localsMap.get(Integer.valueOf(i));
                                    
                                    if (locals != null) {
                                        for (JavaFrameGCRoot localVar : locals) {
                                            Instance localInstance = localVar.getInstance();
                                            
                                            if (localInstance != null) {
                                                sb.append("       Local Variable: ").append(printInstance(localInstance)).append("<br>"); // NOI18N
                                            } else {
                                                sb.append("       Unknown Local Variable<br>"); // NOI18N                                                
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        sb.append("&nbsp;&nbsp;Unknown thread<br>"); // NOI18N
                    }
                    sb.append("<br>");  // NOI18N
                }
            }
            sb.append("</pre>"); // NOI18N
            stackTrace = sb.toString();
        }
        return stackTrace;
    }

    private String getThreadName(final Heap heap, final Instance threadInstance) {
        Object threadName = threadInstance.getValueOfField("name");  // NOI18N
        
        if (threadName == null) {
            return "*null*"; // NOI18N
        }
        return DetailsSupport.getDetailsString((Instance) threadName, heap);
    }


    private Map<ThreadObjectGCRoot,Map<Integer,List<JavaFrameGCRoot>>> computeJavaFrameMap(Collection<GCRoot> roots) {
        Map<ThreadObjectGCRoot,Map<Integer,List<JavaFrameGCRoot>>> javaFrameMap = new HashMap();
        
        for (GCRoot root : roots) {
            if (GCRoot.JAVA_FRAME.equals(root.getKind())) {
                JavaFrameGCRoot frameGCroot = (JavaFrameGCRoot) root;
                ThreadObjectGCRoot threadObj = frameGCroot.getThreadGCRoot();
                Integer frameNo = Integer.valueOf(frameGCroot.getFrameNumber());
                Map<Integer,List<JavaFrameGCRoot>> stackMap = javaFrameMap.get(threadObj);
                List<JavaFrameGCRoot> locals;
                
                if (stackMap == null) {
                    stackMap = new HashMap();
                    javaFrameMap.put(threadObj,stackMap);
                }
                locals = stackMap.get(frameNo);
                if (locals == null) {
                    locals = new ArrayList(2);
                    stackMap.put(frameNo,locals);
                }
                locals.add(frameGCroot);
            }
        }
        return javaFrameMap;
    }

    private String printInstance(Instance in) {
        String className;
        JavaClass jcls = in.getJavaClass();
        
        if (jcls == null) {
            return "unknown instance #"+in.getInstanceId(); // NOI18N
        }
        if (jcls.equals(getJavaClass())) {
            JavaClass javaClass = heapFragmentWalker.getHeapFragment().getJavaClassByID(in.getInstanceId());
            
            if (javaClass != null) {
                className = javaClass.getName();
                return "<a href='"+ CLASS_URL_PREFIX + className + "/" + javaClass.getJavaClassId() + "'>class " + className + "</a>"; // NOI18N
            }
        }
        className = jcls.getName();
        return "<a href='"+ INSTANCE_URL_PREFIX + className + "/" + in.getInstanceNumber() + "/" + in.getInstanceId() + "' name='" + in.getInstanceId() + "'>" + className + '#' + in.getInstanceNumber() + "</a>"; // NOI18N
    }

    private JavaClass getJavaClass() {
        if (java_lang_Class == null) {
            java_lang_Class = heapFragmentWalker.getHeapFragment().getJavaClassByName(Class.class.getName());
        }
        return java_lang_Class;
    }

    private static String htmlize(String value) {
            return value.replace(">", "&gt;").replace("<", "&lt;");     // NOI18N
    }

    @NbBundle.Messages({
        "OverviewController_FORMAT_hms={0} hrs {1} min {2} sec",
        "OverviewController_FORMAT_ms={0} min {1} sec"
    })
    private static String getTime(long millis) {
        // Hours
        long hours = millis / 3600000;
        String sHours = (hours == 0 ? "" : "" + hours); // NOI18N
        millis = millis % 3600000;

        // Minutes
        long minutes = millis / 60000;
        String sMinutes = (((hours > 0) && (minutes < 10)) ? "0" + minutes : "" + minutes); // NOI18N
        millis = millis % 60000;

        // Seconds
        long seconds = millis / 1000;
        String sSeconds = ((seconds < 10) ? "0" + seconds : "" + seconds); // NOI18N

        if (sHours.length() == 0) {
            return Bundle.OverviewController_FORMAT_ms(sMinutes, sSeconds);
        } else {
            return Bundle.OverviewController_FORMAT_hms(sHours, sMinutes, sSeconds);
        }
    }

    /** taken from sun.misc.VM
     * 
     * Returns Thread.State for the given threadStatus
     */
    private static Thread.State toThreadState(int threadStatus) {
        if ((threadStatus & JVMTI_THREAD_STATE_RUNNABLE) != 0) {
            return State.RUNNABLE;
        } else if ((threadStatus & JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER) != 0) {
            return State.BLOCKED;
        } else if ((threadStatus & JVMTI_THREAD_STATE_WAITING_INDEFINITELY) != 0) {
            return State.WAITING;
        } else if ((threadStatus & JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT) != 0) {
            return State.TIMED_WAITING;
        } else if ((threadStatus & JVMTI_THREAD_STATE_TERMINATED) != 0) {
            return State.TERMINATED;
        } else if ((threadStatus & JVMTI_THREAD_STATE_ALIVE) == 0) {
            return State.NEW;
        } else {
            return State.RUNNABLE;
        }
    }

    /* The threadStatus field is set by the VM at state transition
     * in the hotspot implementation. Its value is set according to
     * the JVM TI specification GetThreadState function.
     */
    private final static int JVMTI_THREAD_STATE_ALIVE = 0x0001;
    private final static int JVMTI_THREAD_STATE_TERMINATED = 0x0002;
    private final static int JVMTI_THREAD_STATE_RUNNABLE = 0x0004;
    private final static int JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 0x0400;
    private final static int JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 0x0010;
    private final static int JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 0x0020;
}
