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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.java.queries;

import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.classpath.SimplePathResourceImplementation;
import org.netbeans.modules.java.queries.SFBQImpl2Result;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * The query is used for finding sources for binaries.
 * The examples of usage of this query are:
 * <ul>
 * <li><p>finding source for library</p></li>
 * <li><p>finding src.zip for platform</p></li>
 * <li><p>finding source folder for compiled jar or build folder</p></li>
 * </ul>
 * @see SourceForBinaryQueryImplementation
 * @since org.netbeans.api.java/1 1.4
 */
public class SourceForBinaryQuery {
    
    private static final Logger LOG = Logger.getLogger(SourceForBinaryQuery.class.getName());
    
    private static final Lookup.Result<? extends SourceForBinaryQueryImplementation> implementations =
        Lookup.getDefault().lookupResult (SourceForBinaryQueryImplementation.class);

    private SourceForBinaryQuery () {
    }

    /**
     * Returns the source root for given binary root (for example, src folder for jar file or build folder).
     * @param binaryRoot the ClassPath root of compiled files. The root URL must refer to folder.
     * In the case of an archive file the jar protocol URL must be used. The folder URL has to end with '/'
     * The {@link FileUtil#urlForArchiveOrDir} can be used to create folder URLs.
     * @return a result object encapsulating the answer (never null)
     */
    public static Result findSourceRoots (URL binaryRoot) {
        // XXX consider deleting since ClassPath ctor now checks these things and that is most common URL source
        SimplePathResourceImplementation.verify(binaryRoot, null);
        for (SourceForBinaryQueryImplementation impl : implementations.allInstances()) {
            Result result = impl.findSourceRoots(binaryRoot);
            if (result != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "findSourceRoots({0}) -> {1} from {2}", new Object[] {binaryRoot, Arrays.asList(result.getRoots()), impl});
                }
                return result;
            }
        }
        LOG.log(Level.FINE, "findSourceRoots({0}) -> nil", binaryRoot);
        return EMPTY_RESULT;
    }
    
    /**
     * Returns the source root for given binary root (for example, src folder for jar file or build folder).
     * In addition to the original {@link SourceForBinaryQuery#findSourceRoots(java.net.URL)} it provides 
     * information if the source root(s) should be preferred over the binaries used by the java infrastructure.
     * Most of the clients don't need this information, so thay can use the original
     * {@link SourceForBinaryQuery#findSourceRoots(java.net.URL)} method.
     * @param binaryRoot the ClassPath root of compiled files. The root URL must refer to folder.
     * In the case of an archive file the jar protocol URL must be used. The folder URL has to end with '/'
     * The {@link FileUtil#urlForArchiveOrDir} can be used to create folder URLs.
     * @return a result object encapsulating the answer (never null)
     * @since 1.15
     */
    public static Result2 findSourceRoots2 (URL binaryRoot) {
        // XXX as above, consider deleting
        SimplePathResourceImplementation.verify(binaryRoot, null);
        for (SourceForBinaryQueryImplementation impl : implementations.allInstances()) {
            Result2 result = null;
            if (impl instanceof SourceForBinaryQueryImplementation2) {
                SourceForBinaryQueryImplementation2.Result _result = ((SourceForBinaryQueryImplementation2)impl).findSourceRoots2(binaryRoot);
                if (_result != null) {                    
                    result = new Result2(_result);
                }
            }
            else {
                Result _result = impl.findSourceRoots(binaryRoot);
                if (_result != null) {
                    result = new Result2(new SFBQImpl2Result(_result));
                }
            }
            if (result != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "findSourceRoots2({0}) -> {1} from {2}", new Object[] {binaryRoot, Arrays.asList(result.getRoots()), impl});
                }
                return result;
            }
        }
        LOG.log(Level.FINE, "findSourceRoots2({0}) -> nil", binaryRoot);
        return EMPTY_RESULT;
        
    }

    /**
     * Result of finding sources, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public interface Result {
        
        /**
         * Get the source roots.         
         * @return array of roots of sources (may be empty but not null)
         */
        FileObject[] getRoots();
        
        /**
         * Add a listener to changes in the roots.
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Remove a listener to changes in the roots.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
        
    }
    
    /**
     * Result of finding sources, encapsulating the answer as well as the
     * ability to listen to it.
     * In addition to the Result it provides information if the source root(s)
     * should be preferred over the binaries used by the java infrastructure.
     * Most of the clients don't need this information, so thay can use the
     * original {@link Result}.
     * @since 1.15
     */
    public static class Result2 implements Result {
        
        SourceForBinaryQueryImplementation2.Result delegate;
        //@GuardedBy(this)
        private ChangeListener spiListener;
        private final ChangeSupport changeSupport;
        
        private Result2 (final  SourceForBinaryQueryImplementation2.Result result) {
            assert result != null;
            this.delegate = result;
            this.changeSupport = new ChangeSupport(this);
        }

        public FileObject[] getRoots() {
            return this.delegate.getRoots();
        }

        public void addChangeListener(ChangeListener l) {
            Parameters.notNull("l", l);     //NOI18N
            synchronized (this) {
                if (this.spiListener == null) {
                    this.spiListener = new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            changeSupport.fireChange();
                        }
                    };
                    this.delegate.addChangeListener(WeakListeners.change(this.spiListener, this.delegate));
                }
            }
            this.changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            Parameters.notNull("l", l);
            this.changeSupport.removeChangeListener(l);
        }

        /**
         * This method is used by the java infrastructure to find out whether the
         * sources should be preferred over the binaries.
         * @see SourceForBinaryQueryImplementation2
         * @return true if sources should be used by the java infrastructure
         */
        public boolean preferSources() {
            return this.delegate.preferSources();
        }
    }

    private static final Result2 EMPTY_RESULT = new Result2 (new EmptyResult());
    private static final class EmptyResult implements SourceForBinaryQueryImplementation2.Result {
        private static final FileObject[] NO_ROOTS = new FileObject[0];
        EmptyResult() {}
        public FileObject[] getRoots() {
            return NO_ROOTS;
        }
        @Override
        public boolean preferSources() {
            return false;
        }
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
    }

}
