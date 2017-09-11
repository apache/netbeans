/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.ui.warmup;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ReflectionException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/** Task executed early after startup to log diagnostic data about 
 * JVM - memory settings, JIT type, some hardware information.
 *
 * @author  Radim Kubacki
 */
@ServiceProvider(service=Runnable.class, path="WarmUp")
public final class DiagnosticTask implements Runnable {
    private static final Logger LOG = Logger.getLogger(DiagnosticTask.class.getName());
    private static boolean executed;

    public DiagnosticTask() {}
    
    /** Performs DnD pre-heat.
     */
    public void run() {
        if (executed) {
            return;
        }
        String diagInfo = logParams();
        LOG.info(diagInfo);
        logEnv();
        if (Boolean.getBoolean("netbeans.full.hack")) {
            LOG.info("Using netbeans.full.hack=true; see http://wiki.netbeans.org/DevFaqNetBeansFullHack");
        }
        executed = true;
    }
    
    private void logEnv() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MBeanServer mserver = ManagementFactory.getPlatformMBeanServer();
            // w/o dependency on Sun's JDK
            // long totalMem = ((com.sun.management.OperatingSystemMXBean)osBean).getTotalPhysicalMemorySize();
            long totalMem = (Long)mserver.getAttribute(osBean.getObjectName(), "TotalPhysicalMemorySize");   // NOI18N
            LOG.log(Level.INFO, "Total memory {0}", totalMem);

            LogRecord lr = new LogRecord(Level.INFO, "MEMORY");
            lr.setResourceBundle(NbBundle.getBundle(DiagnosticTask.class));
            lr.setParameters(new Object[] {totalMem});
            Logger.getLogger("org.netbeans.ui.performance").log(lr);
        } catch (SecurityException ex) {
            LOG.log(Level.INFO, null, ex);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, null, ex);
        } catch (MBeanException ex) {
            LOG.log(Level.INFO, null, ex);
        } catch (AttributeNotFoundException ex) {
            LOG.log(Level.INFO, null, ex);
        } catch (InstanceNotFoundException ex) {
            LOG.log(Level.INFO, null, ex);
        } catch (ReflectionException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }

    private void logMemoryUsage(StringBuilder sb, MemoryUsage usage, String label) {
//        long used, commited;
        long init, max;
        init = usage.getInit();
//        used = usage.getUsed();
//        commited = usage.getCommitted();
        max = usage.getMax();
//        sb.append(label).append(" usage: used ").append(formatBytes(used)) // NOI18N
//                .append(" of ").append(formatBytes(commited)); // NOI18N
        sb.append(label).append(" usage: initial ").append(formatBytes(init)) // NOI18N
                .append(" maximum ").append(formatBytes(max)).append('\n'); // NOI18N
    }
    
    /** Format the number to readable string using kB or MB.
     */
    private String formatBytes(long bytes) {
        if (bytes  > 1024L * 1024L) {
            return MessageFormat.format("{0,number,0.0MB}", bytes / 1024.0 / 1024.0); // NOI18N
        } else if (bytes  > 1024L) {
            return MessageFormat.format("{0,number,0.0kB}", bytes / 1024.0); // NOI18N
        } else {
            return MessageFormat.format("{0,number,0b}", bytes); // NOI18N
        }
    }

    private String formatTime(long time) {
        StringBuilder sb = new StringBuilder();
        if (time  > 1000L * 60L * 60L * 24L) {
            sb.append(MessageFormat.format("{0,number,0d}", time/ (1000L * 60L * 60L * 24L))); // NOI18N
            time %= 1000L * 60L * 60L * 24L;
        } 
        if (time  > 1000L * 60L * 60L || sb.length() > 0) {
            sb.append(MessageFormat.format("{0,number,0h}", time/ (1000L * 60L * 60L))); // NOI18N
            time %= 1000L * 60L * 60L;
        } 
        if (time  > 1000L * 60L || sb.length() > 0) {
            sb.append(MessageFormat.format("{0,number,0m}", time/ (1000L * 60L))); // NOI18N
            time %= 1000L * 60L;
        }
        sb.append(MessageFormat.format("{0,number,0s}", time/ 1000L)); // NOI18N
        return sb.toString();
    }

    private String logParams() {
        StringBuilder sb = new StringBuilder(500);
        sb.append("Diagnostic information\n");
        try {
            RuntimeMXBean         rmBean     = ManagementFactory.getRuntimeMXBean();
            CompilationMXBean     cmpMBean   = ManagementFactory.getCompilationMXBean();
//            ThreadMXBean          tmBean     = ManagementFactory.getThreadMXBean();
            MemoryMXBean          memoryBean = ManagementFactory.getMemoryMXBean();
            ClassLoadingMXBean    clMBean    = ManagementFactory.getClassLoadingMXBean();

//            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//            ObjectName hsDiag = new ObjectName("com.sun.management:name=HotSpotDiagnostic");
//            com.sun.management.OperatingSystemMXBean sunOSMBean  =
//               ManagementFactory.getSunOperatingSystemMXBean();

//            Map<String, String> props = new TreeMap<String, String>(rmBean.getSystemProperties());
//            System.out.println("System properties");
//            for (Map.Entry<String, String> entry: props.entrySet()) {
//                System.out.println("Property: "+entry.getKey()+" Value: "+entry.getValue());
//            }
            sb.append("Input arguments:");
            for (String s: rmBean.getInputArguments()) {
                sb.append("\n\t").append(s);
            }
            
            if (cmpMBean != null) {
                sb.append("\nCompiler: "+cmpMBean.getName()).append('\n');
            }
            
            // Memory
            MemoryUsage usage = memoryBean.getHeapMemoryUsage();
            logMemoryUsage(sb, usage, "Heap memory");
            usage = memoryBean.getNonHeapMemoryUsage();
            logMemoryUsage(sb, usage, "Non heap memory");
            for (GarbageCollectorMXBean gcMBean: ManagementFactory.getGarbageCollectorMXBeans()) {
                sb.append("Garbage collector: ").append(gcMBean.getName())
                        .append(" (Collections=").append(gcMBean.getCollectionCount())
                        .append(" Total time spent=").append(formatTime(gcMBean.getCollectionTime()))
                        .append(")\n");
            }
            
            // classes
            int clsLoaded;
            long clsTotal, clsUnloaded;
            clsLoaded = clMBean.getLoadedClassCount();
            clsTotal = clMBean.getTotalLoadedClassCount();
            clsUnloaded = clMBean.getUnloadedClassCount();
            sb.append("Classes: loaded=").append(clsLoaded)
                    .append(" total loaded=").append(clsTotal)
                    .append(" unloaded ").append(clsUnloaded).append('\n');

//        } catch (MalformedObjectNameException ex) {
//            Logger.getLogger("global").log(Level.WARNING, null, ex);
        } catch (NullPointerException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return sb.toString();
    }
    
}
