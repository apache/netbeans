#Signature file v4.1
#Version 1.37

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

CLSS public abstract org.netbeans.modules.sampler.Sampler
meth public final void cancel()
meth public final void start()
meth public final void stop()
meth public final void stopAndWriteTo(java.io.DataOutputStream)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.sampler.Sampler createManualSampler(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.sampler.Sampler createSampler(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds MAX_AVERAGE,MAX_SAMPLES,MAX_SAMPLING_TIME,MAX_STDDEVIATION,MIN_SAMPLES,SAMPLER_RATE,devSquaresSum,laststamp,max,min,name,nanoTimeCorrection,out,running,samples,samplesStream,startTime,stopped,sum,timer

