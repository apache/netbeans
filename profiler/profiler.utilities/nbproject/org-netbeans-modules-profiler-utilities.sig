#Signature file v4.1
#Version 1.60

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract org.netbeans.modules.profiler.utilities.Delegate<%0 extends java.lang.Object>
cons public init()
meth protected {org.netbeans.modules.profiler.utilities.Delegate%0} getDelegate()
meth public void setDelegate({org.netbeans.modules.profiler.utilities.Delegate%0})
supr java.lang.Object
hfds delegate

CLSS public org.netbeans.modules.profiler.utilities.OutputParameter<%0 extends java.lang.Object>
cons public init({org.netbeans.modules.profiler.utilities.OutputParameter%0})
meth public boolean equals(java.lang.Object)
meth public boolean isSet()
meth public int hashCode()
meth public void setValue({org.netbeans.modules.profiler.utilities.OutputParameter%0})
meth public {org.netbeans.modules.profiler.utilities.OutputParameter%0} getValue()
supr java.lang.Object
hfds value

CLSS public final org.netbeans.modules.profiler.utilities.ProfilerUtils
cons public init()
meth public static org.openide.ErrorManager getProfilerErrorManager()
meth public static org.openide.util.RequestProcessor getProfilerRequestProcessor()
meth public static void runInProfilerRequestProcessor(java.lang.Runnable)
meth public static void runInProfilerRequestProcessor(java.lang.Runnable,int)
supr java.lang.Object
hfds profilerErrorManager,profilerRequestProcessor

CLSS public abstract interface org.netbeans.modules.profiler.utilities.Visitable<%0 extends java.lang.Object>
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(org.netbeans.modules.profiler.utilities.Visitor<org.netbeans.modules.profiler.utilities.Visitable<{org.netbeans.modules.profiler.utilities.Visitable%0}>,{%%0},{%%1}>,{%%1})
meth public abstract {org.netbeans.modules.profiler.utilities.Visitable%0} getValue()

CLSS public abstract interface org.netbeans.modules.profiler.utilities.Visitor<%0 extends org.netbeans.modules.profiler.utilities.Visitable, %1 extends java.lang.Object, %2 extends java.lang.Object>
meth public abstract {org.netbeans.modules.profiler.utilities.Visitor%1} visit({org.netbeans.modules.profiler.utilities.Visitor%0},{org.netbeans.modules.profiler.utilities.Visitor%2})

