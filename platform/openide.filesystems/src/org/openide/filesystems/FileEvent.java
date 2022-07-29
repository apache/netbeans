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

package org.openide.filesystems;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Event for listening on filesystem changes.
* <P>
* By calling {@link #getFile} the original file where the action occurred
* can be obtained.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public class FileEvent extends EventObject {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 1028087432345400108L;
    
    private static final Logger LOG = Logger.getLogger(FileEvent.class.getName());

    /** Original file object where the action took place. */
    private FileObject file;

    /** time when this event has been fired */
    private long time;

    /** is expected? */
    private boolean expected;

    /***/
    private EventControl.AtomicActionLink atomActionID;
    private transient Collection<Runnable> postNotify;

    /** Creates new <code>FileEvent</code>. The <code>FileObject</code> where the action occurred
    * is assumed to be the same as the source object.
    * Delegates to {@link #FileEvent(org.openide.filesystems.FileObject, org.openide.filesystems.FileObject, boolean, long) 
    * FileEvent(src, src, false, -1)}.
    * @param src source file which sent this event
    */
    public FileEvent(FileObject src) {
        this(src, src, false, -1);
    }

    /** Creates new <code>FileEvent</code>, specifying the action object.
    * Delegates to {@link #FileEvent(org.openide.filesystems.FileObject, org.openide.filesystems.FileObject, boolean, long) 
    * FileEvent(src, file, false, -1)}.
    * 
    * @param src source file which sent this event
    * @param file <code>FileObject</code> where the action occurred */
    public FileEvent(FileObject src, FileObject file) {
        this(src, file, false, -1);
    }

    /** Creates new <code>FileEvent</code>. 
    * Delegates to {@link #FileEvent(org.openide.filesystems.FileObject, org.openide.filesystems.FileObject, boolean, long) 
    * FileEvent(src, file, false, time)}.
    */
    FileEvent(FileObject src, FileObject file, long time) {
        this(src, file, false, time);
    }

    /** Creates new <code>FileEvent</code>, specifying the action object.
    * Delegates to {@link #FileEvent(org.openide.filesystems.FileObject, org.openide.filesystems.FileObject, boolean, long) 
    * FileEvent(src, file, expected, -1)}.
    * 
    * @param src source file which sent this event
    * @param file <code>FileObject</code> where the action occurred
    * @param expected sets flag whether the value was expected*/
    public FileEvent(FileObject src, FileObject file, boolean expected) {
        this(src, file, expected, -1);
    }
    
    
    /** Creates new <code>FileEvent</code>, specifying all its details.
     * <p>
     * Note that the two arguments of this method need not be identical
     * in cases where it is reasonable that a different file object from
     * the one affected would be listened to by other components. E.g.,
     * in the case of a file creation event, the event source (which
     * listeners are attached to) would be the containing folder, while
     * the action object would be the newly created file object.
     * <p>
     * The time is usually {@link System#currentTimeMillis()} (which is the one
     * used if the parameter value is lower or equal to zero), but it may
     * be any other suitable time. For example when a file was changed, it 
     * may be reasonable to use the timestamp of the file after its stream
     * has been closed.
     * 
     * @param src source file which sends this event
     * @param file {@link FileObject} where the action occured
     * @param expected whether the change has been expected or not
     * @param time the time when the change happened
     * @since  7.54
     */
    public FileEvent(FileObject src, FileObject file, boolean expected, long time) {
        super(src);
        this.file = file;
        this.time = time <= 0L ? System.currentTimeMillis() : time;
        this.expected = expected;
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "FileEvent({0}, {1}, {2}, {3})", new Object[]{src, file, expected, this.time});
        }
        MIMESupport.freeCaches();
        FileUtil.freeCaches();
    }

    /** @return the original file where action occurred
    */
    public final FileObject getFile() {
        return file;
    }

    /** The time when this event has been created.
    * @return the milliseconds
    */
    public final long getTime() {
        return time;
    }

    /** Getter to test whether the change has been expected or not.
    */
    public final boolean isExpected() {
        return expected;
    }

    /** Support for <em>batch</em> processing of events. In some situations
     * you may want to delay processing of received events until the last
     * known one is delivered. For example if there is a lot of operations
     * done inside {@link FileUtil#runAtomicAction(java.lang.Runnable)}
     * action, there can be valid reason to do the processing only after
     * all of them are delivered. In such situation attach your {@link Runnable}
     * to provided event. Such {@link  Runnable} is then guaranteed to be called once.
     * Either immediately (if there is no batch delivery in progress)
     * or some time later (if there is a batch delivery). You can attach
     * single runnable multiple times, even to different events in the
     * same batch section and its {@link  Runnable#run()} method will still be
     * called just once. {@link Object#equals(java.lang.Object)} is used
     * to check equality of two {@link Runnable}s.
     *
     * @since 7.24
     * @param r the runnable to execute when batch event deliver is over
     *   (can be even executed immediately)
     */
    public final void runWhenDeliveryOver(Runnable r) {
        Collection<Runnable> to = postNotify;
        if (to != null) {
            to.add(r);
        } else {
            r.run();
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getName().replaceFirst(".+\\.", ""));
        b.append('[');
        FileObject src = (FileObject) getSource();
        if (src != file) {
            b.append("src=");
            b.append(FileUtil.getFileDisplayName(src));
            b.append(',');
        }
        b.append("file=");
        b.append(FileUtil.getFileDisplayName(file));
        b.append(",time=");
        b.append(new Date(time));
        b.append(",expected=");
        b.append(expected);
        insertIntoToString(b);
        b.append(']');
        return b.toString();
    }
    void insertIntoToString(StringBuilder b) {}

    /** */
    void setAtomicActionLink(EventControl.AtomicActionLink atomActionID) {
        this.atomActionID = atomActionID;
    }

    /** Tests if this event has been generated from given atomic action. This can
     * be used to <em>filter out</em> own changes - when one modifies a file
     * that one also observes, one can use following trick:
     * <pre>
     * <b>class</b> MyAction <b>implements</b> {@link FileSystem.AtomicAction} {
     *   <b>public void </b>run() throws {@link IOException} {
     *     // change a file
     *   }
     * 
     *   <b>public boolean</b> equals(Object obj) {
     *     <b>return</b> obj != null &amp;&amp; obj.getClass() == {@link #getClass()};
     *   }
     * 
     *   <b>public int</b> hashCode() {
     *     return getClass().{@link #hashCode()};
     *   }
     * }
     * 
     * // later when an event is delivered to {@link FileChangeListener your listener} one can check:
     * <b>public void</b> fileChangedEvent({@link FileEvent} ev) {
     *   if (!ev.firedFrom(new MyAction()) {
     *     // the event is not caused by my action, so react somehow
     *   }
     * }
     * 
     * </pre>
     * 
     * @param run is tested atomic action.
     * @return true if fired from run.
     * @since 1.35
     */
    public boolean firedFrom(FileSystem.AtomicAction run) {
        EventControl.AtomicActionLink currentPropID = this.atomActionID;

        if (run == null) {
            return false;
        }

        while (currentPropID != null) {
            final Object aa = currentPropID.getAtomicAction();
            if (aa != null && aa.equals(run)) {
                return true;
            }

            currentPropID = currentPropID.getPreviousLink();
        }

        return false;
    }
    
    final boolean isAsynchronous() {
        EventControl.AtomicActionLink currentPropID = this.atomActionID;
        while (currentPropID != null) {
            final Object atomicAction = currentPropID.getAtomicAction();
            if (atomicAction != null && atomicAction.getClass().getName().indexOf("AsyncRefreshAtomicAction") != -1) {
                return true;
            }
            if (atomicAction instanceof FileSystem.AsyncAtomicAction) {
                if (((FileSystem.AsyncAtomicAction)atomicAction).isAsynchronous()) {
                    return true;
                }
            }
            currentPropID = currentPropID.getPreviousLink();
        }

        return false;
    }

    void setPostNotify(Collection<Runnable> runs) {
        // cannot try to set the postNotify field twiced
        assert postNotify == null || runs == null;
        this.postNotify = runs;
    }

    void inheritPostNotify(FileEvent ev) {
        // cannot try to set the postNotify field twiced
        assert postNotify == null;
        this.postNotify = ev.postNotify;
    }
}
