/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.openide.util.RequestProcessor;

/**
 * The Index is constructed lazily. It is initiated by call of the method
 * findByNameAndType(...).
 *
 * The index is build only for global level schema components (children of the schema
 * component) and if it's total amount exceeds GLOBAL_COMPONENT_INDEX_CREATE_THRESHOLD.
 *
 * On the contrary, if the total amount less them GLOBAL_COMPONENT_INDEX_REMOVE_THRESHOLD
 * the index is removed automatically. 
 *
 * @author Nikita Krjukov
 */
public class GlobalComponentsIndexSupport implements Runnable {

    private static HashSet<Class<? extends SchemaComponent>> mIndexedTypes =
            new HashSet<Class<? extends SchemaComponent>>();
    private static int GLOBAL_COMPONENT_INDEX_CREATE_THRESHOLD = 60;
    private static int GLOBAL_COMPONENT_INDEX_REMOVE_THRESHOLD = 50;

    private static int INDEX_RECALCULATION_DELAY = 100;

    private static RequestProcessor mIndexRecalculationRP;

    static {
        mIndexedTypes.add(GlobalAttribute.class);
        mIndexedTypes.add(GlobalAttributeGroup.class);
        mIndexedTypes.add(GlobalGroup.class);
        mIndexedTypes.add(GlobalType.class);
        mIndexedTypes.add(GlobalSimpleType.class);
        mIndexedTypes.add(GlobalComplexType.class);
        mIndexedTypes.add(GlobalElement.class);

        // 3 Schema models can be processed in parallel.
        mIndexRecalculationRP = new RequestProcessor("IndexRecalculator", 3, true);
    }

    private SchemaModelImpl mSModel;

    // Index of global components
    // Value can be either global schema component or a list of such components
    private TreeMap<String, Object> mGlobalComponentIndex = null;
    private ComponentListener mComponentListener = null;
    private RequestProcessor.Task mIndexGenerationTask;

    private boolean mSupportIndex = false; // indicates if the schema model support the index

    private final Object lock = new Object();

    public GlobalComponentsIndexSupport(SchemaModelImpl model) {
        mSModel = model;
    }

    public <T extends NamedReferenceable> T findByNameAndType(String localName, Class<T> type) {
        synchronized(lock) {
            checkGlobalComponentIndexRequired();
            //
            boolean indexNotAccessible = 
                    !mSupportIndex ||
                    mGlobalComponentIndex == null ||
                    !mIndexedTypes.contains(type) ||
                    mSModel.isIntransaction();
            //
            // The index is not used if the schema model is in transaction
            // because the index can be not synchronized if something is changed
            // inside of the transaction. The index can be rebuilt after
            // transaction commited. 
            //
            if (!indexNotAccessible) {
                T result = null;
                Object cached = mGlobalComponentIndex.get(localName);
                if (cached != null) {
                    if (type.isInstance(cached)) {
                        result = type.cast(cached);
                    } else if (cached instanceof List) {
                        // #207608 - may be a single element, whose type is different than searched for.
                        List compList = List.class.cast(cached);
                        for (Object sComp : compList) {
                            if (type.isInstance(sComp)) {
                                result = type.cast(sComp);
                            }
                        }
                    }
                }
                //
                return result;
            }
        }
        //
        // If it didn't managed to use the index then go ordinary way
        SchemaImpl schema = mSModel.getSchema();
        if (schema == null) {
            return null;
        }
        return new FindGlobalReferenceVisitor<T>().find(type, localName, schema);
    }

    /**
     * Calculates if the index required and set up or remove all infrastructure.
     * It has to be called in synchronized context. 
     */
    private void checkGlobalComponentIndexRequired() {
        //
        SchemaImpl schema = mSModel.getSchema();
        if (schema == null) {
            return;
        }
        //
        boolean indexAllowed = true;
        if (mTestSupport != null) {
            indexAllowed = mTestSupport.isIndexAllowed();
        }
        //
        int childrenCount = schema.getChildrenCount();
        //
        boolean indexCreateRequired =
                childrenCount > GLOBAL_COMPONENT_INDEX_CREATE_THRESHOLD;
        if (indexCreateRequired && !mSupportIndex && indexAllowed) {
            //
            if (mTestSupport != null) {
                mTestSupport.log("Switch ON components' index for Schema model: " +
                        mSModel.toString()); // NOI18N
            }
            //
            if (mComponentListener == null) {
                mComponentListener = new ComponentListener() {

                    public void valueChanged(ComponentEvent evt) {
                        // Ignore this event
                    }

                    public void childrenAdded(ComponentEvent evt) {
                        if (evt.getSource() == mSModel.getSchema()) {
                            initiateIndexRebuld();
                        }
                    }

                    public void childrenDeleted(ComponentEvent evt) {
                        if (evt.getSource() == mSModel.getSchema()) {
                            initiateIndexRebuld();
                        }
                    }
                };
                mSModel.addComponentListener(mComponentListener);
            }
            //
            if (mGlobalComponentIndex == null && mIndexGenerationTask == null) {
                //
                if (mTestSupport != null) {
                    mTestSupport.log("initiate new index building for Schema model: " +
                            mSModel.toString()); // NOI18N
                }
                //
                buildIndex(0);
            }
            mSupportIndex = true;
            //
            return;
        }
        //
        boolean indexRemoveRequired =
                childrenCount < GLOBAL_COMPONENT_INDEX_REMOVE_THRESHOLD;
        if ((!indexAllowed || indexRemoveRequired) && mSupportIndex) {
            //
            if (mTestSupport != null) {
                mTestSupport.log("Switch OFF components' index for Schema model: " +
                        mSModel.toString()); // NOI18N
            }
            //
            mGlobalComponentIndex = null;
            if (mIndexGenerationTask != null) {
                mIndexGenerationTask.cancel();
                mIndexGenerationTask = null;
            }
            //
            if (mComponentListener != null) {
                mSModel.removeComponentListener(mComponentListener);
                mComponentListener = null;
            }
            //
            mSupportIndex = false;
        }
    }

    private void initiateIndexRebuld() {
        synchronized(lock) {
            if (mTestSupport != null) {
                mTestSupport.log("initiate index rebuilding for Schema model: " +
                        mSModel.toString()); // NOI18N
            }
            //
            mGlobalComponentIndex = null; // The old index is absolete
            if (mIndexGenerationTask != null) {
                mIndexGenerationTask.cancel();
            }
            //
            // A delay is necessary because multiple modification events can come after
            // a transactin is commited. The delay helps avoid multiple index's
            // rebuilding per single transaction.
            buildIndex(INDEX_RECALCULATION_DELAY);
        }
    }

    /**
     * Initiates building index in a separate thread
     * It has to be executed in synchronized context.
     * @param delay
     */
    private void buildIndex(int delay) {
        mIndexGenerationTask = mIndexRecalculationRP.post(
                this, delay, Thread.NORM_PRIORITY);
    }

    /**
     * This method is executed by RequestProcessor.
     * The only instance of Runnable is enough because the process is stateless.
     */
    public void run() {
        TreeMap<String, Object> newIndex = buildGlobalComponentIndex();
        if (!Thread.interrupted()) {
            synchronized(lock) {
                mGlobalComponentIndex = newIndex;
                mIndexGenerationTask = null;
            }
        }
    }

    private TreeMap<String, Object> buildGlobalComponentIndex() {
        if (Thread.interrupted()) {
            return null;
        }
        //
        long before = 0;
        if (mTestSupport != null) {
            before = System.nanoTime();
            mTestSupport.log("buildComponentIndex STARTED for Schema model: " +
                    mSModel.toString()); // NOI18N
        }
        //
        TreeMap<String, Object> resultIndex = new TreeMap<String, Object>();
        //
        Schema schema = mSModel.getSchema();
        if (schema == null) {
            return null;
        }
        List<SchemaComponent> globalSCompList = schema.getChildren();
        //
        for (SchemaComponent globalSComp : globalSCompList) {
            //
            if (Thread.interrupted()) {
                return null;
            }
            //
            Class<? extends SchemaComponent> componentType = globalSComp.getComponentType();
            if (mIndexedTypes.contains(componentType)) {
                assert globalSComp instanceof Named;
                String name = Named.class.cast(globalSComp).getName();
                if (name == null || name.length() == 0) {
                    // Skip components without a name attribute or
                    // with empty attributes' value.
                    continue;
                }
                //
                Object value = resultIndex.get(name);
                if (value == null) {
                    resultIndex.put(name, globalSComp);
                    continue;
                }
                //
                if (value instanceof List) {
                    List valuesList = List.class.cast(value);
                    valuesList.add(globalSComp);
                } else {
                    List valuesList = new ArrayList();
                    valuesList.add(value);
                    valuesList.add(globalSComp);
                    resultIndex.put(name, valuesList);
                }
            }
        }
        //
        if (mTestSupport != null) {
            long after = System.nanoTime();
            float delay = (after - before) / 1000000F;
            mTestSupport.log("buildComponentIndex FINISHED for Schema model: " +
                    mSModel.toString()); // NOI18N
            mTestSupport.log("Index contains " + resultIndex.size() + " items. " +
                    "Building has taken " + 
                    new DecimalFormat("#0.00#").format(delay) + " ms"); // NOI18N
        }
        //
        return resultIndex;
    }

    // -------------------------------------------------------------------------
    // JUnit Test Support

    private JUnitTestSupport mTestSupport;

    public JUnitTestSupport getJUnitTestSupport() {
        if (mTestSupport == null) {
            mTestSupport = new JUnitTestSupport(this);
        } 
        return mTestSupport;
    }

    public static class JUnitTestSupport {
        private GlobalComponentsIndexSupport mIndexSupport;
        private List<String> mMsgLog;
        private boolean mIndexAllowed = true;

        public JUnitTestSupport(GlobalComponentsIndexSupport indexSupport) {
            mIndexSupport = indexSupport;
            mMsgLog = new ArrayList<String>();
        }

        public void log(String msg) {
            mMsgLog.add(msg);
        }

        public String printLog() {
            List<String> logCopy = new ArrayList<String>(mMsgLog);
            StringBuilder sb = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            for (String msg : logCopy) {
                sb.append(msg).append(newLine);
            }
            return sb.toString();
        }

        public int getIndexSize() {
            synchronized (mIndexSupport.lock) {
                Map index = mIndexSupport.mGlobalComponentIndex;
                if (index != null) {
                    return index.size();
                }
            }
            return 0;
        }

        public boolean indexContains(String compName) {
            synchronized (mIndexSupport.lock) {
                Map index = mIndexSupport.mGlobalComponentIndex;
                if (index != null) {
                    return index.containsKey(compName);
                }
            }
            return false;
        }

        /**
         * @return current index using status.
         */
        public boolean isSupportIndex() {
            synchronized(mIndexSupport.lock) {
                mIndexSupport.checkGlobalComponentIndexRequired();
                return mIndexSupport.mSupportIndex;
            }
        }

        public void setIndexAllowed(boolean status) {
            mIndexAllowed = status;
        }

        public boolean isIndexAllowed() {
            return mIndexAllowed;
        }

    }

}
