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
package org.netbeans.modules.css.visual;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.lib.api.properties.FixedTextGrammarElement;
import org.netbeans.modules.css.lib.api.properties.GrammarElementVisitor;
import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.Token;
import org.netbeans.modules.css.lib.api.properties.UnitGrammarElement;
import org.netbeans.modules.css.model.api.*;
import org.netbeans.modules.css.visual.api.DeclarationInfo;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 * A node representing a CSS rule with no children. The node properties
 * represents the css rule properties.
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "rule.properties=Properties",
    "rule.properties.description=Properties of the css rule",
    "rule.properties.add.declaration.tooltip=Enter a value to add this property to the selected rule",
    "rule.global.set.displayname=All Categories",
    "rule.global.set.tooltip=Properties from All Categories"
})
public class RuleEditorNode extends AbstractNode {

    private static String COLOR_CODE_GRAY = "777777";
    private static String COLOR_CODE_RED = "ff7777";
    public static String NONE_PROPERTY_NAME = "<none>";
    private String filterText;
    private PropertySetsInfo propertySetsInfo;
    private RuleEditorPanel panel;
    private Map<PropertyDefinition, PropertyDeclaration> addedDeclarations = new HashMap<>();
    private Rule lastRule;
    
    //cache the model.canApplyChanges() as it is very costly operation
    private boolean readOnlyMode;

    public RuleEditorNode(RuleEditorPanel panel) {
        super(new RuleChildren());
        this.panel = panel;
    }

    public Model getModel() {
        return panel.getModel();
    }
    
    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public FileObject getFileObject() {
        return getModel().getLookup().lookup(FileObject.class);
    }

    public Rule getRule() {
        return panel.getRule();
    }
    
    public boolean isShowAllProperties() {
        return panel.getViewMode().isShowAllProperties();
    }

    public boolean isShowCategories() {
        return panel.getViewMode().isShowCategories();
    }

    public boolean isAddPropertyMode() {
        return panel.isAddPropertyMode();
    }

    //called by the RuleEditorPanel when user types into the filter text field
    void setFilterText(String prefix) {
        this.filterText = prefix;
        fireContextChanged(true); //recreate the property sets
    }

    //called by the RuleEditorPanel when any of the properties affecting 
    //the PropertySet-s generation changes.
    public void fireContextChanged(boolean forceRefresh) {
        boolean oldReadOnlyModel = readOnlyMode;
        readOnlyMode = getModel() == null || !getModel().canApplyChanges();
        if(oldReadOnlyModel != readOnlyMode) {
            //refresh the PS as the read only mode changes
            forceRefresh = true;
        }
        
        try {
            PropertySetsInfo oldInfo = getCachedPropertySetsInfo();
            PropertySetsInfo newInfo = createPropertySetsInfo();
            
            PropertyCategoryPropertySet[] oldSets = oldInfo.getSets();
            PropertyCategoryPropertySet[] newSets = newInfo.getSets();

            if (!forceRefresh) {
                //the client doesn't require the property sets to be really recreated,
                //we may try to update them only if possible

                //compare old and new sets, if they contain same sets with same properties,
                //then update the PropertyDefinition-s so they contain reference to the current
                //css model vertion.
                //
                //if there's a new PropertySet or one of the PropertySets contains more or less
                //properties than the original, then do not do the incremental update but
                //refresh the PropertySets completely.
                update:
                {
                    //check if the "created declaration" flag has changed or not
                    if(oldInfo.isCreatedDeclaration() != newInfo.isCreatedDeclaration()) {
                        break update; //dpn't merge
                    }

                    //old DeclarationProperty to new value map
                    if (oldSets.length == newSets.length) {
                        for (int i = 0; i < oldSets.length; i++) {
                            PropertyCategoryPropertySet o = oldSets[i];
                            PropertyCategoryPropertySet n = newSets[i];

                            Map<PropertyDeclaration, DeclarationProperty> om = o.declaration2PropertyMap;
                            Map<PropertyDeclaration, DeclarationProperty> nm = n.declaration2PropertyMap;

                            if (om.size() != nm.size()) {
                                break update;
                            }
                            //same number of declarations

                            //notice: the same order of the properties as in the last model
                            //is ensured by the getUniquePropertyName() method which adds 
                            //index of the property in the rule to its name.

                            //create declaration name -> declaration maps se we may compare 
                            //(as the css source model elements do not comparable by equals/hashcode)
                            Map<String, PropertyDeclaration> oName2DeclarationMap = new HashMap<>();
                            for (PropertyDeclaration d : om.keySet()) {
                                if (lastRule.getModel() != d.getModel()) {
                                    break update; // Issue 234155
                                }
                                oName2DeclarationMap.put(PropertyUtils.getDeclarationId(lastRule, d), d);
                            }
                            Map<String, PropertyDeclaration> nName2DeclarationMap = new HashMap<>();
                            for (PropertyDeclaration d : nm.keySet()) {
                                nName2DeclarationMap.put(PropertyUtils.getDeclarationId(getRule(), d), d);
                            }

                            //compare the names of the properties in the old and new map,
                            //they must be the same otherwise we wont' marge but recreate 
                            //the whole property sets
                            Collection<String> oldNames = oName2DeclarationMap.keySet();
                            Collection<String> newNames = nName2DeclarationMap.keySet();
                            Collection<String> comp = new HashSet<>(oldNames);
                            if (comp.retainAll(newNames)) { //assumption: the collections size are the same
                                break update; //canot merge - the collections differ
                            }

                            for (Entry<String, PropertyDeclaration> entry : oName2DeclarationMap.entrySet()) {
                                String declarationName = entry.getKey();
                                PropertyDeclaration oldD = entry.getValue();
                                PropertyDeclaration newD = nName2DeclarationMap.get(declarationName);

                                //update the existing DeclarationProperty with the fresh
                                //Declaration object from the new model instance
                                DeclarationProperty declarationProperty = om.get(oldD);
                                declarationProperty.updateDeclaration(newD);

                                //also update the declaration2PropertyMap itself 
                                //as we now use new Declaration object
                                om.remove(oldD);
                                om.put(newD, declarationProperty);
                            }

                        }
                        return;
                    }

                }
            }

            //refresh the sets completely
            propertySetsInfo = newInfo;
            firePropertySetsChange(oldSets, newSets);
        } finally {
            this.lastRule = getRule();
        }
    }

    void fireDeclarationInfoChanged(PropertyDeclaration declaration, DeclarationInfo declarationInfo) {
        DeclarationProperty dp = getDeclarationProperty(declaration);
        if (dp != null) {
            dp.setDeclarationInfo(declarationInfo);
        }
    }

    DeclarationProperty getDeclarationProperty(PropertyDeclaration declaration) {
        for (PropertyCategoryPropertySet set : getCachedPropertySetsInfo().getSets()) {
            DeclarationProperty declarationProperty = set.getDeclarationProperty(declaration);
            if (declarationProperty != null) {
                return declarationProperty;
            }
        }
        return null;
    }

    @Override
    public synchronized PropertySet[] getPropertySets() {
        this.lastRule = getRule();
        return getCachedPropertySetsInfo().getSets();
    }

    private synchronized PropertySetsInfo getCachedPropertySetsInfo() {
        if (propertySetsInfo == null) {
            propertySetsInfo = createPropertySetsInfo();
        }
        return propertySetsInfo;
    }

    private boolean matchesFilterText(String text) {
        if (filterText == null) {
            return true;
        } else {
            return text.contains(filterText);
        }
    }

    private Collection<PropertyDefinition> filterByPrefix(Collection<PropertyDefinition> defs) {
        Collection<PropertyDefinition> filtered = new ArrayList<>();
        for (PropertyDefinition pd : defs) {
            if (matchesFilterText(pd.getName())) {
                filtered.add(pd);
            }
        }
        return filtered;
    }

    /**
     * Creates property sets of the node.
     *
     * @return property sets of the node.
     */
    private PropertySetsInfo createPropertySetsInfo() {
        if (getModel() == null || getRule() == null) {
            return new PropertySetsInfo(new PropertyCategoryPropertySet[0], panel.getCreatedDeclaration() != null);
        }
        Collection<PropertyCategoryPropertySet> sets = new ArrayList<>();
        List<PropertyDeclaration> declarations = PropertyUtils.getPropertyDeclarations(getRule());

        FileObject file = getFileObject();

        if (isShowCategories()) {
            //create property sets for property categories

            Map<PropertyDefinition, PropertyDeclaration> created = new HashMap<>();
            Map<PropertyCategory, List<PropertyDeclaration>> categoryToDeclarationsMap = new EnumMap<>(PropertyCategory.class);
            for (PropertyDeclaration d : declarations) {
                if (addedDeclarations.containsValue(d)) {
                    continue; //skip those added declarations
                }
                //check the declaration
                org.netbeans.modules.css.model.api.Property property = d.getProperty();
                PropertyValue propertyValue = d.getPropertyValue();
                if (property != null && propertyValue != null) {
                    if (matchesFilterText(property.getContent().toString())) {
                        PropertyDefinition def = Properties.getPropertyDefinition(property.getContent().toString());
                        
                        String declarationId = PropertyUtils.getDeclarationId(getRule(), d);
                        if(panel.getCreatedDeclarationsIdsList().contains(declarationId)) {
                            created.put(def, d);
                        }
                        
                        PropertyCategory category;
                        if (def != null) {
                            category = def.getPropertyCategory();
                        } else {
                            category = PropertyCategory.UNKNOWN;
                        }

                        List<PropertyDeclaration> values = categoryToDeclarationsMap.get(category);
                        if (values == null) {
                            values = new LinkedList<>();
                            categoryToDeclarationsMap.put(category, values);
                        }
                        values.add(d);
                    }
                }
            }

            Map<PropertyCategory, PropertyCategoryPropertySet> propertySetsMap = new EnumMap<>(PropertyCategory.class);
            for (Entry<PropertyCategory, List<PropertyDeclaration>> entry : categoryToDeclarationsMap.entrySet()) {

                List<PropertyDeclaration> categoryDeclarations = entry.getValue();
                
                if(isShowAllProperties()) {
                    //remove the "just created"
                    categoryDeclarations.removeAll(created.values());
                }
                
                //sort alpha
                categoryDeclarations.sort(PropertyUtils.getDeclarationsComparator());

                PropertyCategoryPropertySet propertyCategoryPropertySet = new PropertyCategoryPropertySet(entry.getKey());
                propertyCategoryPropertySet.addAll(categoryDeclarations);

                propertySetsMap.put(entry.getKey(), propertyCategoryPropertySet);
                sets.add(propertyCategoryPropertySet);
            }

            if (isShowAllProperties()) {
                //Show all properties
                for (PropertyCategory cat : PropertyCategory.values()) {
                    //now add all the remaining properties
                    List<PropertyDefinition> allInCat = new LinkedList<>(filterByPrefix(getCategoryProperties(cat)));
                    if (allInCat.isEmpty()) {
                        continue; //skip empty categories (when filtering)
                    }
                    allInCat.sort(PropertyUtils.getPropertyDefinitionsComparator());

                    PropertyCategoryPropertySet propertySet = propertySetsMap.get(cat);
                    if (propertySet == null) {
                        propertySet = new PropertyCategoryPropertySet(cat);
                        sets.add(propertySet);
                    }

                    //remove already used
                    for (PropertyDeclaration d : propertySet.getDeclarations()) {
                        PropertyDefinition def = Properties.getPropertyDefinition(d.getProperty().getContent().toString());
                        allInCat.remove(def);
                    }

                    //add the rest of unused properties to the property set
                    for (PropertyDefinition pd : allInCat) {
                        PropertyDeclaration alreadyAdded = addedDeclarations.get(pd);
                        if(alreadyAdded == null) {
                            alreadyAdded = created.get(pd);
                        }
                        if (alreadyAdded != null) {
                            propertySet.add(alreadyAdded, true);
                        } else {
                            propertySet.add(file, pd);
                        }
                    }

                }

            }

        } else {
            //not showCategories
            //just create one top level property set for virtual category (the items actually don't belong to the category)
            PropertyCategoryPropertySet set = new PropertyCategoryPropertySet(PropertyCategory.DEFAULT);

            if(!isShowAllProperties()) {
                //set properties only view
                List<PropertyDeclaration> filtered = new ArrayList<>();
                for (PropertyDeclaration d : declarations) {
                    if (addedDeclarations.containsValue(d)) {
                        continue; //skip those added declarations
                    }
                    String declarationId = PropertyUtils.getDeclarationId(getRule(), d);
                    if(panel.getCreatedDeclarationsIdsList().contains(declarationId)) {
                        //created declaration--ignore filter
                        filtered.add(d);
                    } else {
                        //check the declaration
                        org.netbeans.modules.css.model.api.Property property = d.getProperty();
                        PropertyValue propertyValue = d.getPropertyValue();
                        if (property != null && propertyValue != null) {
                            if (matchesFilterText(property.getContent().toString())) {
                                filtered.add(d);
                            }
                        }
                    }
                }
                //sort aplha
                Comparator<PropertyDeclaration> comparator = PropertyUtils.createDeclarationsComparator(getRule(), panel.getCreatedDeclarationsIdsList());
                filtered.sort(comparator);
                set.addAll(filtered);
                
                //do NOT show all properties
                //Add the fake "Add Property" FeatureDescriptor at the end of the set
                if(!readOnlyMode && panel.getCreatedDeclaration() == null) {
                    //do not add the "Add Property" item when we are editing value of the just added property
                    set.add_Add_Property_FeatureDescriptor();
                }
            } else {
                //all properties view
                List<PropertyDeclaration> filteredExisting = new ArrayList<>();
                Map<PropertyDefinition, PropertyDeclaration> filteredCreated = new HashMap<>();
                for (PropertyDeclaration d : declarations) {
                    if (addedDeclarations.containsValue(d)) {
                        continue; //skip those added declarations
                    }
                    String declarationId = PropertyUtils.getDeclarationId(getRule(), d);
                    if(panel.getCreatedDeclarationsIdsList().contains(declarationId)) {
                        //created declaration--ignore filter
                        filteredCreated.put(d.getResolvedProperty().getPropertyDefinition(), d);
                    } else {
                        //check the declaration
                        org.netbeans.modules.css.model.api.Property property = d.getProperty();
                        PropertyValue propertyValue = d.getPropertyValue();
                        if (property != null && propertyValue != null) {
                            if (matchesFilterText(property.getContent().toString())) {
                                filteredExisting.add(d);
                            }
                        }
                    }
                }
                
                set.addAll(filteredExisting);
                
                List<PropertyDefinition> all = new ArrayList<>(filterByPrefix(Properties.getPropertyDefinitions(file, true)));
                all.sort(PropertyUtils.getPropertyDefinitionsComparator());

                //remove already used
                for (PropertyDeclaration d : set.getDeclarations()) {
                    PropertyDefinition def = Properties.getPropertyDefinition(d.getProperty().getContent().toString());
                    all.remove(def);
                }

                //add the rest of unused properties to the property set
                for (PropertyDefinition pd : all) {
                    //boz<i' gula's<:
                    PropertyDeclaration alreadyAdded = addedDeclarations.get(pd); //added in "ADD PROPERTY MODE"
                    if(alreadyAdded == null) {
                        alreadyAdded = filteredCreated.get(pd); //added in normal mode
                    }
                    
                    if (alreadyAdded != null) {
                        set.add(alreadyAdded, true);
                    } else {
                        set.add(file, pd);
                    }
                }
            }
            
            //overrride the default descriptions
            set.setDisplayName(Bundle.rule_global_set_displayname());
            set.setShortDescription(Bundle.rule_global_set_tooltip());
            
            sets.add(set);
        }



        return new PropertySetsInfo(sets.toArray(new PropertyCategoryPropertySet[0]), panel.getCreatedDeclaration() != null);
    }

    /**
     * Returns a list of *visible* properties with this category.
     */
    public List<PropertyDefinition> getCategoryProperties(PropertyCategory cat) {
        Collection<PropertyDefinition> defs = Properties.getPropertyDefinitions(getModel().getLookup().lookup(FileObject.class), true);
        List<PropertyDefinition> defsInCat = new ArrayList<>();
        for (PropertyDefinition d : defs) {
            if (d.getPropertyCategory() == cat) {
                defsInCat.add(d);
            }
        }
        return defsInCat;
    }

    private String getPropertyDisplayName(PropertyDeclaration declaration) {
        return declaration.getProperty().getContent().toString();
    }

    public void applyModelChanges() {
        final Model model = getModel();
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                try {
                    model.applyChanges();
                } catch (IOException | BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    class PropertyCategoryPropertySet extends PropertySet {

        private List<Property> properties = new ArrayList<>();
        private Map<PropertyDeclaration, DeclarationProperty> declaration2PropertyMap = new HashMap<>();

        public PropertyCategoryPropertySet(PropertyCategory propertyCategory) {
            super(propertyCategory.name(), //NOI18N
                    propertyCategory.getDisplayName(),
                    propertyCategory.getShortDescription());
        }

        public void add_Add_Property_FeatureDescriptor() {
            properties.add(create_Add_Property_Feature_Descriptor());
        }

        public void add(PropertyDeclaration declaration, boolean markAsModified) {
            DeclarationProperty property = createDeclarationProperty(declaration, markAsModified);
            declaration2PropertyMap.put(declaration, property);
            properties.add(property);
        }

        public void addAll(Collection<PropertyDeclaration> declarations) {
            for (PropertyDeclaration d : declarations) {
                add(d, false);
            }
        }

        public Collection<PropertyDeclaration> getDeclarations() {
            return declaration2PropertyMap.keySet();
        }

        public DeclarationProperty getDeclarationProperty(PropertyDeclaration declaration) {
            return declaration2PropertyMap.get(declaration);
        }

        public void add(FileObject context, PropertyDefinition propertyDefinition) {
            properties.add(createPropertyDefinitionProperty(context, propertyDefinition));
        }

        @Override
        public Property<String>[] getProperties() {
            return properties.toArray(new Property[]{});
        }
    }

    private Property createPropertyDefinitionProperty(FileObject context, PropertyDefinition definition) {
        PropertyDefinition pmodel = Properties.getPropertyDefinition(definition.getName());
        return new PropertyDefinitionProperty(definition, createPropertyValueEditor(context, pmodel, null, false));
    }

    private PropertyValuesEditor createPropertyValueEditor(FileObject context, PropertyDefinition pmodel, PropertyDeclaration declaration, boolean addNoneProperty) {
        final Collection<UnitGrammarElement> unitElements = new ArrayList<>();
        final Collection<FixedTextGrammarElement> fixedElements = new ArrayList<>();

        if (pmodel != null) {
            GroupGrammarElement rootElement = pmodel.getGrammarElement(context);

            rootElement.accept(new GrammarElementVisitor() {
                private Set<GroupGrammarElement> seen = Collections.newSetFromMap(new IdentityHashMap<>());

                @Override
                public boolean visit(UnitGrammarElement element) {
                    unitElements.add(element);
                    return true;
                }

                @Override
                public boolean visit(FixedTextGrammarElement element) {
                    fixedElements.add(element);
                    return true;
                }

                @Override
                public boolean visit(GroupGrammarElement element) {
                    if (seen.contains(element)) {
                        return false;
                    }
                    seen.add(element);
                    return true;
                }

            });
        }

        return new PropertyValuesEditor(panel, pmodel, getModel(), fixedElements, unitElements, declaration, addNoneProperty);

    }

    private abstract class AbstractPDP<T> extends PropertySupport<T> {

        private PropertyDefinition def;
        private PropertyEditor editor;

        public AbstractPDP(PropertyDefinition def, PropertyEditor editor, String name, Class<T> type, String displayName, String shortDescription, boolean canR, boolean canW) {
            super(name, type, displayName, shortDescription, canR, canW);
            this.def = def;
            this.editor = editor;
        }

        public AbstractPDP(PropertyDefinition def, PropertyEditor editor, Class<T> clazz, String shortDescription) {
            super(def.getName(),
                    clazz,
                    def.getName(),
                    shortDescription,
                    true,
                    getRule().isValid() && !readOnlyMode);
            this.def = def;
            this.editor = editor;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return getEmptyValue();
        }

        @Override
        public void setValue(T val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (getEmptyValue().equals(val)) {
                return; //no change
            }

            //add a new declaration to the rule
            ElementFactory factory = getModel().getElementFactory();

            Rule rule = getRule();
            Declarations declarations = rule.getDeclarations();

            if (declarations == null) {
                //empty rule, create declarations node as well
                declarations = factory.createDeclarations();
                rule.setDeclarations(declarations);
            }

            org.netbeans.modules.css.model.api.Property property = factory.createProperty(def.getName());
            Expression expr = factory.createExpression(convertToString(val));
            PropertyValue value = factory.createPropertyValue(expr);
            PropertyDeclaration newPropertyDeclaration = factory.createPropertyDeclaration(property, value, false);
            Declaration newDeclaration = factory.createDeclaration();
            newDeclaration.setPropertyDeclaration(newPropertyDeclaration);
            declarations.addDeclaration(newDeclaration);

            //save the model to the source
            if (!isAddPropertyMode()) {
                panel.setCreatedDeclaration(rule, newPropertyDeclaration);
                applyModelChanges();
            } else {
                //add property mode - just refresh the content
                addedDeclarations.put(def, newPropertyDeclaration); //remember what we've added during this dialog cycle
                fireContextChanged(true);
            }
        }

        protected abstract String convertToString(T val);

        protected abstract T getEmptyValue();
    }
    private static String EMPTY_STRING = "";

    private class PlainPDP extends AbstractPDP<String> {

        public PlainPDP(PropertyDefinition def, PropertyEditor editor, String shortDescription) {
            super(def, editor, String.class, shortDescription);
        }

        @Override
        protected String convertToString(String val) {
            return val;
        }

        @Override
        protected String getEmptyValue() {
            return EMPTY_STRING;
        }
    }

    private class PropertyDefinitionProperty extends PlainPDP {

        public PropertyDefinitionProperty(PropertyDefinition def, PropertyEditor editor) {
            super(def,
                    editor,
                    Bundle.rule_properties_add_declaration_tooltip());
        }
    }

    private DeclarationProperty createDeclarationProperty(PropertyDeclaration declaration, boolean markAsModified) {
        ResolvedProperty resolvedProperty = declaration.getResolvedProperty();
        PropertyDefinition propertyDefinition = resolvedProperty != null ? resolvedProperty.getPropertyDefinition() : null;
        return new DeclarationProperty(declaration,
                PropertyUtils.getDeclarationId(getRule(), declaration),
                getPropertyDisplayName(declaration),
                markAsModified,
                createPropertyValueEditor(getFileObject(), propertyDefinition, declaration, true));
    }

    @NbBundle.Messages({
        "property.set.at.prefix=Set at ",
        "property.value.unexpected.token={0}, unexpected character(s) \"{1}\" found",
        "property.value.not.resolved={0}, error in property value",
        "property.erroneous={0}, erroneous property",
        "property.unknown={0}, unknown property",
        "property.inactive={0}, not affecting the selected element",
        "property.overridden={0}, overridden by another property",
        "property.description={0}",
        "property.no.file=No File"
    })
    public class DeclarationProperty extends PropertySupport {

        private final String propertyName;
        private final PropertyEditor editor;
        private final boolean markAsModified;
        private PropertyDeclaration propertyDeclaration;
        private DeclarationInfo info;
        private String shortDescription;
        private String valueSet;
        private String locationPrefix;

        public DeclarationProperty(PropertyDeclaration declaration, String propertyName, String propertyDisplayName, boolean markAsModified, PropertyEditor editor) {
            super(propertyName,
                    String.class,
                    propertyDisplayName,
                    null, true, !readOnlyMode && getRule().isValid());
            this.propertyName = propertyName;
            this.propertyDeclaration = declaration;
            this.markAsModified = markAsModified;
            this.editor = editor;

            checkForErrors();

            //one may set a custom inplace editor by 
            //setValue("inplaceEditor", new MyInplaceEditor());

        }
        
        public PropertyDeclaration getDeclaration() {
            return propertyDeclaration;
        }

        /**
         * Updates the {@link #info} field to {@link DeclarationInfo#ERRONEOUS}
         * if the active declaration contains errors.
         */
        private void checkForErrors() {
            //suppress the errors for just added property
            //it doesn't have the value yet, but this doesn't mean
            //we want to mark it as erroneous while adding the value
            if (getDeclaration().equals(panel.getCreatedDeclaration())) {
                return; 
            }

            String property = propertyDeclaration.getProperty().getContent().toString().trim();
            PropertyDefinition model = Properties.getPropertyDefinition(property);
            if (model == null) {
                //flag as unknown
                info = DeclarationInfo.ERRONEOUS;
                shortDescription = Bundle.property_unknown(getLocationPrefix());
                return ;
            }
             
            //so we have a property model...
            
            //but before checking the property value ensure we are not trying
            //to do so for vendor specific property. Values of these properties
            //are not supposed to be checked as the grammars are not very much
            //up-to-date and reliable.
            if(Properties.isVendorSpecificProperty(model)) {
                shortDescription = Bundle.property_description(getLocationPrefix());
                return ;
            }

            PropertyValue value = propertyDeclaration.getPropertyValue();
            if (value != null) {
                Expression expression = value.getExpression();
                CharSequence content = expression != null ? expression.getContent() : "";
                ResolvedProperty rp = new ResolvedProperty(getFileObject(), model, content);
                if (!rp.isResolved()) {
                    List<Token> unresolvedTokens = rp.getUnresolvedTokens();
                    if(unresolvedTokens.isEmpty()) {
                        //no value token/s
                        info = DeclarationInfo.ERRONEOUS;
                        shortDescription = Bundle.property_value_not_resolved(getLocationPrefix());
                        return ;
                    }
                    
                    //we have some unresolved token,
                    //lets check if the token is vendor specific value token
                    Token unexpectedToken = unresolvedTokens.iterator().next();
                    String unexpectedText = unexpectedToken.image().toString();
                    
                    if(!org.netbeans.modules.css.editor.module.spi.Utilities.isVendorSpecificPropertyValueToken(getFileObject(), unexpectedText)) {
                        //no, it seems to be a common value token
                        shortDescription = Bundle.property_value_unexpected_token(getLocationPrefix(), unexpectedText);
                        return;
                    }
                }
            }

            //else everything seems to be all right
            shortDescription = Bundle.property_description(getLocationPrefix());
        }

        /**
         * Returns the file:line prefix for the tooltip
         */
        private CharSequence getLocationPrefix() {
            if (locationPrefix == null) {
                final StringBuilder sb = new StringBuilder();
                sb.append(Bundle.property_set_at_prefix());
                Model model = getModel();
                Lookup lookup = model.getLookup();
                FileObject file = lookup.lookup(FileObject.class);
                if (file == null) {
                    sb.append(Bundle.property_no_file());
                } else {
                    sb.append(file.getNameExt());
                }
                Snapshot snap = lookup.lookup(Snapshot.class);
                final Document doc = lookup.lookup(Document.class);
                if (snap != null && doc != null) {
                    PropertyDeclaration decl = getDeclaration();
                    int ast_from = decl.getStartOffset();
                    if (ast_from != -1) {
                        //source element, not virtual which is not persisted yet
                        final int doc_from = snap.getOriginalOffset(ast_from);
                        if (doc_from != -1) {
                            doc.render(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        int lineOffset = 1 + Utilities.getLineOffset((BaseDocument) doc, doc_from);
                                        sb.append(':');
                                        sb.append(lineOffset);
                                    } catch (BadLocationException ex) {
                                        //no-op
                                    }
                                }
                            });
                        }
                    }
                }
                locationPrefix = sb.toString();
            }

            return locationPrefix;
        }

        private void updateDeclaration(PropertyDeclaration declaration) {
            assert PropertyUtils.getDeclarationId(getRule(), declaration).equals(propertyName);

            //update the declaration
            String oldValue = getValue();
            this.propertyDeclaration = declaration;
            String newValue = getValue();

            locationPrefix = null; //reset the prefix as it was computed for the original declaration

            /* Reset DeclarationInfo to default state (null) as the contract 
             * doesn't require/expect the RuleEditorController.setDeclarationInfo(...) 
             * to be called for each "plain" declaration with null DeclarationInfo argument.
             */
            DeclarationInfo oldInfo = info;
            info = null;

            String oldShortDescription = shortDescription;

            //possibly set the DeclarationInfo to ERRONEOUS
            checkForErrors();

            if (!shortDescription.equals(oldShortDescription)) {
                fireShortDescriptionChange(oldShortDescription, shortDescription);
            }

            //now we need to fire property name property change with some 
            //change so call setDeclarationInfo() which does property change
            //from null to current value and hence forces the PS to repaint 
            //the property
            if (info != oldInfo) {
                //DeclarationInfo has changed
                setDeclarationInfo(info);
            } else {
                //no change to DeclarationInfo 
                setDisplayName(getHtmlDisplayName());
                //and fire property change to the node 
                //this will trigger the property name and value repaint
                firePropertyChange(propertyName, oldValue, newValue);
            }

        }

        @Override
        public String getShortDescription() {
            return shortDescription;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        public void setDeclarationInfo(DeclarationInfo info) {
            if(this.info == info) {
                return ; //no change
            }
            
            this.info = info;
            setDisplayName(getHtmlDisplayName());
            
            //tooltip update
            String oldShortDescription = shortDescription;
            switch(info) {
                case ERRONEOUS:
                    shortDescription = Bundle.property_erroneous(getLocationPrefix());
                    break;
                case INACTIVE:
                    shortDescription = Bundle.property_inactive(getLocationPrefix());
                    break;
                case  OVERRIDDEN:
                    shortDescription = Bundle.property_overridden(getLocationPrefix());
                    break;
            }
            fireShortDescriptionChange(oldShortDescription, shortDescription);
            
            //force the property repaint - stupid way but there's
            //doesn't seem to be any better way
            firePropertyChange(propertyName, null, getValue());
        }

        private boolean isOverridden() {
            return info != null && info == DeclarationInfo.OVERRIDDEN;
        }

        private boolean isInactive() {
            return info != null && info == DeclarationInfo.INACTIVE;
        }

        private boolean isErroneous() {
            return info != null && info == DeclarationInfo.ERRONEOUS;
        }

        @Override
        public String getHtmlDisplayName() {
            StringBuilder b = new StringBuilder();

            String color = null;
            boolean bold = false;
            boolean strike = false;

            if (isShowAllProperties()) {
                if (isAddPropertyMode() && !markAsModified) {
                    color = COLOR_CODE_GRAY;
                } else {
                    bold = true;
                }
            }
            if (isOverridden()) {
                strike = true;
            }
            if (isInactive()) {
                color = COLOR_CODE_GRAY;
                strike = true;
            }
            if (isErroneous()) {
                strike = true;
                color = COLOR_CODE_RED;
            }

            //render
            if (bold) {
                b.append("<b>");//NOI18N
            }
            if (strike) {
                b.append("<s>"); //use <del>?
            }
            if (color != null) {
                b.append("<font color="); //NOI18N
                b.append(color);
                b.append(">"); //NOI18N
            }

            b.append(getPropertyDisplayName(propertyDeclaration));

            if (color != null) {
                b.append("</font>"); //NOI18N
            }
            if (strike) {
                b.append("</s>"); //use <del>?
            }
            if (bold) {
                b.append("</b>");//NOI18N
            }

            return b.toString();
        }

        @Override
        public String getValue() {
            if (valueSet != null) {
                return valueSet;
            }
            PropertyValue val = propertyDeclaration.getPropertyValue();
            return val == null ? null : val.getExpression().getContent().toString().trim();
        }

        @Override
        public void setValue(final Object o) {
            assert SwingUtilities.isEventDispatchThread();

            final String asString = (String) o;
            if (asString == null || asString.isEmpty()) {
                return;
            }
            String currentValue = getValue();
            if (asString.equals(currentValue)) {
                //same value, ignore
                return;
            }

            this.valueSet = asString;
            SAVE_CHANGE_TASK.schedule(200);

        }
        private Task SAVE_CHANGE_TASK = RuleEditorPanel.RP.create(new Runnable() {
            @Override
            public void run() {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        //all the access to valueSet field is safe as 
                        //the field is only set in setValue() which always
                        //runs id EDT

                        //The tasks may schedule in such way that more than one tasks
                        //runs after the setValue(...) method called.
                        //In such case the first task sets the valueSet field to null
                        //and the other tasks cannot rule (they do not have anything
                        //to do anyway) so just quit in such case.
                        if (valueSet == null) {
                            return;
                        }

                        Model model = getModel();
                        model.runWriteTask(new Model.ModelTask() {
                            @Override
                            public void run(StyleSheet styleSheet) {
                                if (NONE_PROPERTY_NAME.equals(valueSet)) {
                                    //remove the whole declaration
                                    Declaration declaration = (Declaration) propertyDeclaration.getParent();
                                    Declarations declarations = (Declarations)declaration.getParent();
                                    declaration.removeElement(propertyDeclaration);
                                    declarations.removeDeclaration(declaration);
                                } else {
                                    //update the value
                                    RuleEditorPanel.LOG.log(Level.FINE, "updating property to {0}", valueSet);
                                    propertyDeclaration.getPropertyValue().getExpression().setContent(valueSet);
                                }
                            }
                        });

                        if (!isAddPropertyMode()) {
                            //save changes
                            applyModelChanges();

                            //the model save request will cause the source model's 
                            //Model.CHANGES_APPLIED_TO_DOCUMENT property change event fired
                            //and the RuleEditorPanel's listener will SYNCHRONOUSLY
                            //refresh the css source model.
                            //
                            //...so now we have a new instance of model reflecting
                            //the changes made by the writetask above
                        }

                        valueSet = null;
                    }
                });

            }
        });
    }

    private Property create_Add_Property_Feature_Descriptor() {
//        return ADD_PROPERTY_FD;
        //TODO put back the shared instance once Standa fixes the multiple setValue(...) calls so the innser property state can be removed.
        return new AddPropertyFD();
    }
    
    private Property ADD_PROPERTY_FD = new AddPropertyFD();

    @NbBundle.Messages({
        "AddProperty.displayName.html=<html><body><b>Add Property</b></body></html>",
        "AddProperty.displayName=Add Property",
        "AddProperty.shortDescription=Click here to add a new property."
    })
    
    private class AddPropertyFD extends Property<String> {

        private String valueSet;
        
        public AddPropertyFD() {
            super(String.class);
            setName(AddPropertyFD.class.getSimpleName());
            setDisplayName(Bundle.AddProperty_displayName());
            setShortDescription(Bundle.AddProperty_shortDescription());
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new AddPropertyPropertyEditor(this);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return "";
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        //called from AddPropertyPropertyEditor when a value is entered
        @Override
        public void setValue(final String propertyName) {
            if (propertyName == null) {
                return;
            }
            if(propertyName.trim().isEmpty()) {
                return ; //ignore no value
            }
            
            if(valueSet != null) {
                RuleEditorPanel.LOG.log(Level.WARNING, "Trying to set property value more than once!, relaxing...");
                return ;
            }
            valueSet = propertyName;

            
            //1.create the property
            //2.select the corresponding row in the PS
            
            final Model model = getModel();
            final Rule rule = getRule();
            model.runWriteTask(new Model.ModelTask() {
                @Override
                public void run(StyleSheet styleSheet) {
                    //add the new declaration to the model.
                    //the declaration is not complete - the value is missing and it is necessary to 
                    //enter in the PS otherwise the model become invalid.
                    ModelUtils utils = new ModelUtils(model);
                    Declarations decls = rule.getDeclarations();
                    if (decls == null) {
                        decls = model.getElementFactory().createDeclarations();
                        rule.setDeclarations(decls);
                    }

                    PropertyDeclaration pdeclarationElement = utils.createPropertyDeclaration(propertyName + ":");
                    Declaration declarationElement = model.getElementFactory().createDeclaration();
                    declarationElement.setPropertyDeclaration(pdeclarationElement);
                    decls.addDeclaration(declarationElement);

                    //do not save the model (apply changes) - once the write task finishes
                    //the embedded property sheet will be refreshed from the modified model.

                    //remember the created declaration so once the model change is fired
                    //and the property sheet is refreshed, we can find and select the corresponding
                    //FeatureDescriptor
                    panel.setCreatedDeclaration(rule, pdeclarationElement);
                }
            });
        }
    }

    private class AddPropertyPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private AddPropertyFD property;

        public AddPropertyPropertyEditor(AddPropertyFD property) {
            this.property = property;
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            env.getFeatureDescriptor().setValue("custom.cell.renderer", 
                    new AddPropertyCellRendererComponent()); //NOI18N
            
            env.getFeatureDescriptor().setValue("custom.cell.editor", 
                    new AddPropertyCellEditorComponent(new AutocompleteJComboBox(getFileObject()), property)); //NOI18N
        }
    }

    private class AddPropertyCellRendererComponent implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int i, int i1) {
            return new JLabel(Bundle.AddProperty_displayName_html());
        }
    }

    private class AddPropertyCellEditorComponent extends DefaultCellEditor {

        private AutocompleteJComboBox editor;
        private AddPropertyFD property;
        
        private boolean cancelled;

        public AddPropertyCellEditorComponent(AutocompleteJComboBox jcb, AddPropertyFD addFDProperty) {
            super(jcb);
            this.property = addFDProperty;
            this.editor = jcb;
            this.editor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if(!cancelled) {
                        property.setValue((String)editor.getSelectedItem());
                    }
                }
            });
            
        }
        @Override
        public Component getTableCellEditorComponent(JTable jtable, Object o, boolean bln, int i, int i1) {
            editor.setSelectedIndex( -1 );
            return editor;
        }

        @Override
        protected void fireEditingCanceled() {
            cancelled = true;
            super.fireEditingCanceled();
        }
        
    }

    /**
     * Empty children keys
     */
    private static class RuleChildren extends Children.Keys {

        @Override
        protected Node[] createNodes(Object key) {
            return new Node[]{};
        }
    }
    
    private static class PropertySetsInfo {
        
        private final PropertyCategoryPropertySet[] sets;
        private final boolean createdDeclaration; 

        public PropertySetsInfo(PropertyCategoryPropertySet[] sets, boolean createdDeclaration) {
            this.sets = sets;
            this.createdDeclaration = createdDeclaration;
        }

        public PropertyCategoryPropertySet[] getSets() {
            return sets;
        }

        /**
         * Returns true if the propertysets were created when there was 
         * "created declaration" set in the RuleEditorPanel.
         */
        public boolean isCreatedDeclaration() {
            return createdDeclaration;
        }
        
    }
}
