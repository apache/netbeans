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

package org.netbeans.installer.utils.progress;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 * This class encapsulates data that describes the course of an operation, such as
 * the operation description, detailed status string and completion percentage.
 *
 * <p>
 * A typical usecase would be for a client to costruct an instance of this object
 * and pass it to the operation handler, which will update the progress as the
 * operation goes on. The client can register itself as a listener for the progress
 * object; this way it will receive notifications on the progress updates.
 *
 * <p>
 * The client can also use the built-in cancellation facility to inform the
 * operation handler of the necessity to break the operation and return. Whether
 * the operation will be stopped, however, solely depends on the operation handler's
 * politeness.
 *
 * <p>
 * In some cases the opreation handler is not capable of working directly with the
 * progress that was passed to it. In this case it should construct a specialized
 * {@link Progress} instance and use the synchronization facility to keep these two
 * in sync.
 *
 * @see #addProgressListener(ProgressListener)
 * @see #setCanceled(boolean)
 * @see #synchronizeFrom(Progress)
 * @see CompositeProgress
 *
 * @author Kirill Sorokin
 *
 * @since 1.0
 */
public class Progress {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The title (i.e. human readable description) of the progress. The title is
     * expected not to change often during the course of the progress.
     */
    protected String title;
    
    /**
     * The detailed status of the progress. As opposed to the title, the detail
     * is expected to change with every couple changes in the procentage.
     */
    protected String detail;
    
    /**
     * The current state of the progress expressed in percents. Obviously the most
     * often changed property of the progress.
     */
    protected int percentage;
    
    /**
     * A flag which indicates whether or not the progress has been canceled. It is
     * expected that the updater of the progress will check the value of this
     * flag and act accordingly. It should be used to establish some basic
     * communication between the invoker of a lengthy operation and the code which
     * actually performs the operation.
     */
    protected boolean canceled;
    
    /**
     * The listener which controls the synchronization between two progresses.
     */
    private ProgressListener synchronizer;
    
    /**
     * The source of the synchronization data.
     */
    private Progress source;
    
    /**
     * The list of registered progress listeners.
     */
    private List<ProgressListener> listeners;
    
    // constructors /////////////////////////////////////////////////////////////////
    /**
     * Creates a new {@link Progress} instance. The new instance has its
     * <code>title</code> and <code>detail</code> set to empty strings,
     * <code>percentage</code> to {@link #START} and <code>canceled</code> to
     * <code>false</code>.
     */
    public Progress() {
        title = StringUtils.EMPTY_STRING;
        detail = StringUtils.EMPTY_STRING;
        percentage = START;
        
        canceled = false;
        
        synchronizer = null;
        source = null;
        
        listeners = new ArrayList<ProgressListener>();
    }
    
    /**
     * Creates a new {@link Progress} instance and registers the supplied listener.
     * 
     * <p>
     * It's important to remember that listeners in progress are not implemented via
     * weak references and hence should be handled with care. The most common case 
     * would be to dump the complete progresses tree together with all listeners. 
     * However if it's not the case, be sure to remove the listener with the 
     * {@link #removeProgressListener(ProgressListener)} method.
     *
     * @param initialListener A progress listener to register upon progress
     *      creation.
     * 
     * @see #Progress()
     * @see #removeProgressListener(ProgressListener)
     */
    public Progress(final ProgressListener initialListener) {
        this();
        
        addProgressListener(initialListener);
    }
    
    // getters/setters //////////////////////////////////////////////////////////////
    /**
     * Returns the value of the <code>title</code> property.
     *
     * @return The value of the <code>title</code> property.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the value of the <code>title</code> property. The value is updated only
     * if the supplied title is different from the current. Also the progress
     * listeners are notified only if the update actually took place.
     *
     * @param title The new value for the <code>title</code> property.
     */
    public void setTitle(final String title) {
        if (!this.title.equals(title)) {
            this.title = title;
            
            notifyListeners();
        }
    }
    
    /**
     * Returns the value of the <code>detail</code> property.
     *
     * @return The value of the <code>detail</code> property.
     */
    public String getDetail() {
        return detail;
    }
    
    /**
     * Sets the value of the <code>detail</code> property. The value is updated only
     * if the supplied detail is different from the current. Also the progress
     * listeners are notified only if the update actually took place.
     *
     * @param detail The new value for the <code>detail</code> property.
     */
    public void setDetail(final String detail) {
        if (!this.detail.equals(detail)) {
            this.detail = detail;
            
            notifyListeners();
        }
    }
    
    /**
     * Returns the value of the <code>percentage</code> property.
     *
     * @return The value of the <code>percentage</code> property.
     */
    public int getPercentage() {
        return percentage;
    }
    
    /**
     * Sets the value of the <code>percentage</code> property. The value is updated
     * only if the supplied percentage is different from the current. Also the 
     * progress listeners are notified only if the update actually took place.
     *
     * @param percentage The new value for the <code>percentage</code> property.
     */
    public void setPercentage(final int percentage) {
        if (this.percentage != percentage) {
            if ((percentage < START) || (percentage > COMPLETE)) {
                LogManager.log(StringUtils.format(
                        ERROR_WRONG_PERCENTAGE,
                        percentage,
                        START,
                        COMPLETE));
            } else {
               this.percentage = percentage; 
            }
            
            notifyListeners();
        }
    }
    
    /**
     * Sets the value of the <code>percentage</code> property. This method is the 
     * same as the {@link #setPercentage(int)} one, with the only difference that
     * it accepts a <code>long</code> parameter. The parameter is converted to an
     * <code>int</code> and the corresponding method is called.
     *
     * @param percentage The new value for the <code>percentage</code> property.
     * 
     * @throws IllegalArgumentException if the supplied percentage cannot be 
     *      set.
     */
    public void setPercentage(final long percentage) {
        setPercentage((int) percentage);
    }
    
    /**
     * Adds the specified amount to the <code>percentage</code> property. The added
     * amount can be either positive or negative. Teh percentage value will be
     * updated only if the result of the addition is different from the current
     * percentage. Also the listeners are notified only is the update actually took
     * place.
     *
     * @param addition The amount to add to the <code>percentage</code> property.
     */
    public void addPercentage(final int addition) {
        final int result = percentage + addition;
        
        if (this.percentage != result) {
            if ((result < START) || (result > COMPLETE)) {
                LogManager.log(StringUtils.format(
                        ERROR_WRONG_PERCENTAGE,
                        result,
                        START,
                        COMPLETE));
            } else {
               this.percentage = result;
            }
            
            notifyListeners();
        }
    }
    
    /**
     * Returns the value of the <code>canceled</code> property.
     *
     * @return The value of the <code>canceled</code> property.
     */
    public boolean isCanceled() {
        return canceled;
    }
    
    /**
     * Sets the value of the <code>canceled</code> property. The value is updated
     * only if the supplied value is different from the current. Also the progress
     * listeners are notified only if the update actually took place.
     *
     * <p>
     * If the progress is in being synchronized from another progress. The cancelled
     * status will be propagated to the synchronization source.
     *
     * @param canceled The new value for the <code>canceled</code> property.
     */
    public void setCanceled(final boolean canceled) {
        if (this.canceled != canceled) {
            this.canceled = canceled;
            
            // propagate the cancel status to the source
            if (source != null) {
                source.setCanceled(canceled);
            }
            
            notifyListeners();
        }
    }
    
    // synchronization //////////////////////////////////////////////////////////////
    /**
     * Sets up the synchronization of this progress object from the specified
     * source. If there was a synchronization link already set up, it is removed.
     *
     * <p>
     * Once the synchronization is established, the methods that set the core
     * progress properties do not have any effect - the properties will be correctly
     * set, but will be overwritten with the next source update.
     *
     * <p>
     * In order to remove the synchronization link on this progress, just supply
     * <code>null</code> as the parameter.
     * 
     * @param progress A progress object from which to set up the synchronization,
     *      or <code>null</code> to cancel the synchronization.
     */
    public void synchronizeFrom(final Progress progress) {
        if (source != null) {
            source.removeProgressListener(synchronizer);
        }
        
        if (progress != null) {
            synchronizer = new ProgressListener() {
                public void progressUpdated(Progress progress) {
                    setTitle(progress.getTitle());
                    setDetail(progress.getDetail());
                    setPercentage(progress.getPercentage());
                }
            };
            
            source = progress;
            source.addProgressListener(synchronizer);
        }
    }
    
    /**
     * Sets up the reverse synchronization of this progress object from the
     * specified source.
     *
     * <p>
     * This method is very similar to the {@link #synchronizeFrom(Progress)} method.
     * The only difference is in that instead of setting the percentage of this
     * progress object to the percentage of the source, it is set to the difference
     * between {@link #COMPLETE} and the source's percentage.
     *
     * <p>
     * In order to remove the synchronization link on this progress, just supply
     * <code>null</code> as the parameter.
     *
     * @param progress A progress object from which to set up the synchronization,
     *      or <code>null</code> to cancel the synchronization.
     *
     * @see #synchronizeFrom(Progress)
     */
    public void reverseSynchronizeFrom(final Progress progress) {
        // clear the current source
        if (source != null) {
            source.removeProgressListener(synchronizer);
        }
        
        if (progress != null) {
            synchronizer = new ProgressListener() {
                public void progressUpdated(Progress progress) {
                    setTitle(progress.getTitle());
                    setDetail(progress.getDetail());
                    setPercentage(Progress.COMPLETE - progress.getPercentage());
                }
            };
            
            source = progress;
            source.addProgressListener(synchronizer);
        }
    }
    
    /**
     * Sets up the synchronization of this progress object to the specified target.
     * This method just calls {@link #synchronizeFrom(Progress)} on the target
     * progress, supplying itself as the parameter.
     *
     * @param progress A progress object to which to set up the synchronization.
     *
     * @see #synchronizeFrom(Progress)
     */
    public void synchronizeTo(final Progress progress) {
        progress.synchronizeFrom(this);
    }
    
    /**
     * Sets up the reverse synchronization of this progress object to the specified
     * target. This method just calls {@link #reverseSynchronizeFrom(Progress)} on
     * the target progress, supplying itself as the parameter.
     *
     * @param progress A progress object to which to set up the synchronization.
     *
     * @see #reverseSynchronizeFrom(Progress)
     */
    public void reverseSynchronizeTo(final Progress progress) {
        progress.reverseSynchronizeFrom(this);
    }
    
    // listeners ////////////////////////////////////////////////////////////////////
    /**
     * Adds (registers) a progress listener to this progrss object. If the argument
     * is null, no action will be performed.
     * 
     * <p>
     * In most cases a single {@link Progress} instance will have only one listener, 
     * so it is feasible to use the specialized constructor overlod instead of this
     * method.
     *
     * @param listener A progress listener to add.
     *
     * @see ProgressListener
     * @see #Progress(ProgressListener)
     */
    public void addProgressListener(final ProgressListener listener) {
        if (listener == null) return;
        
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes (unregisters) a progress listener from this progress object. If the
     * argument is null or is not present in the listeners list, no action will
     * be performed.
     *
     * @param listener A progress listener to remove.
     *
     * @see ProgressListener
     */
    public void removeProgressListener(final ProgressListener listener) {
        if (listener == null) return;
        
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Notifies the registered listeners that one or more of the progress'
     * properties has changed.
     *
     * @see ProgressListener
     */
    protected void notifyListeners() {
        final ProgressListener[] clone;
        
        synchronized (listeners) {
            clone = listeners.toArray(new ProgressListener[listeners.size()]);
        }
        
        for (ProgressListener listener: clone) {
            listener.progressUpdated(this);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The initial (minimum) value of a progress' percentage.
     */
    public static final int START = 0;
    
    /**
     * The final (maximum) value of a progress' percentage.
     */
    public static final int COMPLETE = 100;
    
    /**
     * The error message which will be displayed when a user tries to set an invalid
     * percentage.
     */
    public static final String ERROR_WRONG_PERCENTAGE =
            ResourceUtils.getString(Progress.class,
            "P.error.percentage"); // NOI18N
}
