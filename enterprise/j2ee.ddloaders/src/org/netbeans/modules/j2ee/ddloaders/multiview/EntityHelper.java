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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author pfiala
 */
public class EntityHelper extends EntityAndSessionHelper {
    
    private final Entity entity;
    public final CmpFields cmpFields;
    public final EntityHelper.Queries queries;
    private EntityMethodController entityMethodController;
    
    
    public EntityHelper(EjbJarMultiViewDataObject ejbJarMultiViewDataObject, Entity entity) {
        super(ejbJarMultiViewDataObject, entity);
        this.entity = ((Entity) ejb);
        cmpFields = new CmpFields();
        queries = new Queries();
        // FIXME: entityMethodController is always null!
        entityMethodController =  null; 
//        new EntityMethodController(entity, sourceClassPath,
//                ejbJarMultiViewDataObject.getEjbJar());
        super.abstractMethodController = entityMethodController;
    }
    
    public void removeQuery(Query query) {
        entity.removeQuery(query);
        modelUpdatedFromUI();
    }
    
    public boolean hasLocalInterface() {
        return ejb.getLocal() != null;
    }
    
    public boolean hasRemoteInterface() {
        return ejb.getRemote() != null;
    }
    
    public String getPrimkeyField() {
        return entity.getPrimkeyField();
    }
    
    public String getPrimKeyClass() {
        return entity.getPrimKeyClass();
    }
    
    public void setPrimkeyFieldName(String fieldName) {
        entity.setPrimkeyField(fieldName);
    }
    
    public void setPrimkeyField(String fieldName) throws ClassNotFoundException {
        setPrimkeyFieldName(fieldName);
        modelUpdatedFromUI();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (queries == null) {
            return;
        }
        Object source = evt.getSource();
        String propertyName = evt.getPropertyName();
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        if (source == entity) {
            if ((oldValue instanceof CmpField || newValue instanceof CmpField)) {
                cmpFields.change(source, propertyName, oldValue, newValue);
            } else if ((oldValue instanceof Query || newValue instanceof Query)) {
                queries.change(source, propertyName, oldValue, newValue);
            }
        } else if (source instanceof CmpField) {
            cmpFields.change(source, propertyName, oldValue, newValue);
        } else if (source instanceof Query) {
            queries.change(source, propertyName, oldValue, newValue);
        }
    }
    
    public EntityMethodController getEntityMethodController() {
        return entityMethodController;
    }
    
    public void updateFieldAccessor(String fieldName, boolean getter, boolean local, boolean shouldExist) {
        if (local && hasLocalInterface() || !local && hasRemoteInterface()) {
            entityMethodController.updateFieldAccessor(fieldName, getter, local, shouldExist);
        }
    }
    
    public class CmpFields implements PropertyChangeSource {
        
        private List<PropertyChangeListener> listeners = new LinkedList<>();
        private Map<CmpField, CmpFieldHelper> cmpFieldHelperMap = new HashMap<>();
        private CmpFieldsTableModel cmpFieldsTableModel = new CmpFieldsTableModel(this);
        
        public int getCmpFieldCount() {
            return entity.getCmpField().length;
        }
        
        public CmpFieldHelper getCmpFieldHelper(int row) {
            CmpField field = getCmpField(row);
            CmpFieldHelper cmpFieldHelper = (CmpFieldHelper) cmpFieldHelperMap.get(field);
            if (cmpFieldHelper == null) {
                cmpFieldHelper = createCmpFieldHelper(field);
            }
            return cmpFieldHelper;
        }
        
        private CmpFieldHelper getCmpFieldHelper(String fieldName) {
            CmpFieldHelper cmpFieldHelper = (CmpFieldHelper) cmpFieldHelperMap.get(fieldName);
            if (cmpFieldHelper == null) {
                CmpField[] cmpFields = entity.getCmpField();
                for (int i = 0; i < cmpFields.length; i++) {
                    CmpField field = cmpFields[i];
                    if (fieldName.equals(field.getFieldName())) {
                        cmpFieldHelper = createCmpFieldHelper(field);
                    }
                }
            }
            return cmpFieldHelper;
        }
        
        private CmpFieldHelper createCmpFieldHelper(CmpField field) {
            CmpFieldHelper cmpFieldHelper;
            cmpFieldHelper = new CmpFieldHelper(EntityHelper.this, field);
            cmpFieldHelperMap.put(field, cmpFieldHelper);
            return cmpFieldHelper;
        }
        
        private CmpField getCmpField(int row) {
            return getCmpFields()[row];
        }
        
        public CmpField[] getCmpFields() {
            CmpField[] cmpFields = entity.getCmpField();
            Arrays.sort(cmpFields, new Comparator() {
                public int compare(Object o1, Object o2) {
                    String s1 = ((CmpField) o1).getFieldName();
                    String s2 = ((CmpField) o2).getFieldName();
                    if (s1 == null) {
                        s1 = "";
                    }
                    if (s2 == null) {
                        s2 = "";
                    }
                    return s1.compareTo(s2);
                }
            });
            return cmpFields;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.remove(listener);
        }
        
        public void change(Object source, String propertyName, Object oldValue, Object newValue) {
            if (source instanceof Entity) {
                cmpFieldHelperMap.keySet().retainAll(Arrays.asList(entity.getCmpField()));
            }
            firePropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
        }
        
        protected void firePropertyChange(PropertyChangeEvent evt) {
            for (Iterator<PropertyChangeListener> iterator = listeners.iterator(); iterator.hasNext();) {
                ((PropertyChangeListener) iterator.next()).propertyChange(evt);
            }
        }
        
        public void addCmpField() {
            modelUpdatedFromUI();
        }
        
        public CmpFieldsTableModel getCmpFieldsTableModel() {
            return cmpFieldsTableModel;
        }
        
        public int getFieldRow(CmpField cmpField) {
            final CmpField[] fields = getCmpFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].equals(cmpField)) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    public class Queries implements PropertyChangeSource {
        private List<PropertyChangeListener> listeners = new LinkedList<>();
        private Map<Query, QueryMethodHelper> queryMethodHelperMap = new HashMap<>();
        private Query[] selectMethods;
        private Query[] finderMethods;
        public static final String SELECT_PREFIX = "ejbSelect"; //NOI18N
        public static final String FIND_PREFIX = "find"; //NOI18N
        private FinderMethodsTableModel finderMethodsTableModel = new FinderMethodsTableModel(this);
        private SelectMethodsTableModel selectMethodsTableModel = new SelectMethodsTableModel(this);
        
        public Queries() {
            initQueryMethods();
        }
        
        private void initQueryMethods() {
            selectMethods = getQueries(SELECT_PREFIX);
            finderMethods = getQueries(FIND_PREFIX);
        }
        
        public QueryMethodHelper getQueryMethodHelper(Query query) {
            return null;
        }
        
        public QueryMethodHelper getFinderMethodHelper(int row) {
            return getQueryMethodHelper(finderMethods[row]);
        }
        
        public QueryMethodHelper getSelectMethodHelper(int row) {
            return getQueryMethodHelper(selectMethods[row]);
        }
        
        private Query[] getQueries(String s) {
            List<Query> list = new LinkedList<>();
            Query[] queries = entity.getQuery();
            for (int i = 0; i < queries.length; i++) {
                Query query = queries[i];
                if (query.getQueryMethod().getMethodName().startsWith(s)) {
                    list.add(query);
                    final QueryMethodHelper helper = (QueryMethodHelper) queryMethodHelperMap.get(query);
                    if (helper != null) {
                        helper.init();
                    }
                }
            }
            return (Query[]) list.toArray(new Query[0]);
        }
        
        public int getFinderMethodCount() {
            return finderMethods.length;
        }
        
        public int getSelectMethodCount() {
            return selectMethods.length;
        }
        
        public Query getFinderMethod(int rowIndex) {
            return finderMethods[rowIndex];
        }
        
        public Query getSelecMethod(int rowIndex) {
            return selectMethods[rowIndex];
        }
        
        public void change(Object source, String propertyName, Object oldValue, Object newValue) {
            initQueryMethods();
            queryMethodHelperMap.keySet().retainAll(Arrays.asList(entity.getQuery()));
            firePropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
        }
        
        public Query[] getQuery() {
            return entity.getQuery();
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.remove(listener);
        }
        
        protected void firePropertyChange(PropertyChangeEvent evt) {
            for (Iterator<PropertyChangeListener> iterator = listeners.iterator(); iterator.hasNext();) {
                ((PropertyChangeListener) iterator.next()).propertyChange(evt);
            }
        }
        
        public String getLocal() {
            return EntityHelper.this.getLocal();
        }
        
        public String getRemote() {
            return EntityHelper.this.getRemote();
        }
        
        public FinderMethodsTableModel getFinderMethodsTableModel() {
            return finderMethodsTableModel;
        }
        
        public SelectMethodsTableModel getSelectMethodsTableModel() {
            return selectMethodsTableModel;
        }
        
        public int getSelectMethodRow(Query query) {
            for (int i = 0; i < selectMethods.length; i++) {
                if (query.equals(selectMethods[i])) {
                    return i;
                }
            }
            return -1;
        }
        
        public int getFinderMethodRow(Query query) {
            for (int i = 0; i < finderMethods.length; i++) {
                if (query.equals(finderMethods[i])) {
                    return i;
                }
            }
            return -1;
        }
    }
}
