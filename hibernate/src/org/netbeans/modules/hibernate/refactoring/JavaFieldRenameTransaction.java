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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.refactoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.hibernate.mapping.model.Any;
import org.netbeans.modules.hibernate.mapping.model.Array;
import org.netbeans.modules.hibernate.mapping.model.Bag;
import org.netbeans.modules.hibernate.mapping.model.Component;
import org.netbeans.modules.hibernate.mapping.model.CompositeElement;
import org.netbeans.modules.hibernate.mapping.model.CompositeId;
import org.netbeans.modules.hibernate.mapping.model.CompositeIndex;
import org.netbeans.modules.hibernate.mapping.model.CompositeMapKey;
import org.netbeans.modules.hibernate.mapping.model.DynamicComponent;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.Id;
import org.netbeans.modules.hibernate.mapping.model.Idbag;
import org.netbeans.modules.hibernate.mapping.model.Join;
import org.netbeans.modules.hibernate.mapping.model.JoinedSubclass;
import org.netbeans.modules.hibernate.mapping.model.KeyManyToOne;
import org.netbeans.modules.hibernate.mapping.model.KeyProperty;
import org.netbeans.modules.hibernate.mapping.model.List;
import org.netbeans.modules.hibernate.mapping.model.ManyToOne;
import org.netbeans.modules.hibernate.mapping.model.Map;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.hibernate.mapping.model.NaturalId;
import org.netbeans.modules.hibernate.mapping.model.NestedCompositeElement;
import org.netbeans.modules.hibernate.mapping.model.OneToOne;
import org.netbeans.modules.hibernate.mapping.model.Properties;
import org.netbeans.modules.hibernate.mapping.model.Property;
import org.netbeans.modules.hibernate.mapping.model.Set;
import org.netbeans.modules.hibernate.mapping.model.Subclass;
import org.netbeans.modules.hibernate.mapping.model.Timestamp;
import org.netbeans.modules.hibernate.mapping.model.UnionSubclass;
import org.netbeans.modules.hibernate.mapping.model.Version;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Refactor the Java field names in the Hibernate mapping files
 * 
 * @author Dongmei Cao
 */
public class JavaFieldRenameTransaction extends RenameTransaction {

    // String name used by the schema2bean model to retrive the name attribute vaules
    private final String nameAttrib = "Name"; // NOI18N
    
    private String className;

    public JavaFieldRenameTransaction(java.util.Set<FileObject> files, String className, String origFieldName, String newFieldName) {
        super(files, origFieldName, newFieldName);
        this.className = className;
    }

    /**
     * Do the actual changes
     * 
     */
    public void doChanges() {

        for (FileObject mappingFileObject : getToBeModifiedFiles()) {

            OutputStream outs = null;
            try {
                InputStream is = mappingFileObject.getInputStream();
                HibernateMapping hbMapping = null;
                try {
                    hbMapping = HibernateMapping.createGraph(is);
                } catch (RuntimeException ex) {
                    //failed to create graph, corrupted mapping file
                    Logger.getLogger(JavaFieldRenameTransaction.class.getName()).log(Level.WARNING, "Failed to refactor in {0}, verify if xml document is well formed", mappingFileObject.getPath());//NOI18N
                }
                if(hbMapping !=null ) {
                    HibernateRefactoringUtil.ChangeTracker rewriteTrack = new HibernateRefactoringUtil.ChangeTracker();
                    hbMapping.addPropertyChangeListener(rewriteTrack);

                    refactoringMyClasses(hbMapping.getMyClass());

                    refactoringSubclasses(hbMapping.getSubclass());

                    refactoringJoinedSubclasses(hbMapping.getJoinedSubclass());

                    refactoringUnionSubclasses(hbMapping.getUnionSubclass());

                    if(rewriteTrack.isChanged()){
                        outs = mappingFileObject.getOutputStream();
                        hbMapping.write(outs);
                    }
                    hbMapping.removePropertyChangeListener(rewriteTrack);
                }

            } catch (FileAlreadyLockedException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } finally {
                try {
                    if (outs != null) {
                        outs.close();
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }

    private void refactoringMyClasses(MyClass[] myClazz) {
        for (int ci = 0; ci < myClazz.length; ci++) {
            MyClass myClzz = myClazz[ci];
            String clsName = myClzz.getAttributeValue(nameAttrib);
            if (clsName != null && clsName.equals(className)) {

                // <id name="">
                Id id = myClzz.getId();
                if (id != null) {
                    String idPropName = id.getAttributeValue(nameAttrib);
                    if (idPropName != null && idPropName.equals(origName)) {
                        id.setAttributeValue(nameAttrib, newName);
                    }
                }

                // <class version="">
                Version ver = myClzz.getVersion();
                if (ver != null) {
                    String versionName = ver.getAttributeValue(nameAttrib);
                    if (versionName != null && versionName.equals(origName)) {
                        ver.setAttributeValue(nameAttrib, newName);
                    }
                }

                Timestamp tstamp = myClzz.getTimestamp();
                if (tstamp != null) {
                    String timestamp = tstamp.getAttributeValue(nameAttrib);
                    if (timestamp != null && timestamp.equals(origName)) {
                        tstamp.setAttributeValue(nameAttrib, newName);
                    }
                }

                // <composite-id name="">
                refactoringCompositeId(myClzz.getCompositeId());

                // <property name="">
                refactoringProperty(myClzz.getProperty2());

                // <join><property name="">
                refactoringJoins(myClzz.getJoin());

                //<natural-id><property name="">
                refactoringNaturalId(myClzz.getNaturalId());

                refactoringMaps(myClzz.getMap());

                refactoringSets(myClzz.getSet());

                refactoringLists(myClzz.getList());

                refactoringArrays(myClzz.getArray());

                refactoringBags(myClzz.getBag());

                refactoringIdbags(myClzz.getIdbag());

                refactoringDynamicComponents(myClzz.getDynamicComponent());

                refactoringComponents(myClzz.getComponent());

                refactoringAnys(myClzz.getAny());

                refactoringManyToOnes(myClzz.getManyToOne());

                refactoringPropertiez(myClzz.getProperties());

                refactoringOneToOnes(myClzz.getOneToOne());
            }

            // <class> element can have <subclass>, <joined-subclass> and/or <union-sbuclass> elements
            refactoringSubclasses(myClzz.getSubclass());
            refactoringJoinedSubclasses(myClzz.getJoinedSubclass());
            refactoringUnionSubclasses(myClzz.getUnionSubclass());
        }
    }

    private void refactoringSubclasses(Subclass[] subclazz) {
        // <property name="">
        for (int i = 0; i < subclazz.length; i++) {

            String clsName = subclazz[i].getAttributeValue(nameAttrib);
            if (clsName != null && clsName.equals(className)) {

                refactoringProperty(subclazz[i].getProperty2());

                // <join><property name="">
                refactoringJoins(subclazz[i].getJoin());

                refactoringMaps(subclazz[i].getMap());

                refactoringDynamicComponents(subclazz[i].getDynamicComponent());

                refactoringComponents(subclazz[i].getComponent());

                refactoringSets(subclazz[i].getSet());

                refactoringLists(subclazz[i].getList());

                refactoringArrays(subclazz[i].getArray());

                refactoringBags(subclazz[i].getBag());

                refactoringIdbags(subclazz[i].getIdbag());

                refactoringAnys(subclazz[i].getAny());

                refactoringManyToOnes(subclazz[i].getManyToOne());

                refactoringOneToOnes(subclazz[i].getOneToOne());
            }

            // <subclass> elements can be contained inside <subclass> element
            refactoringSubclasses(subclazz[i].getSubclass());
        }
    }

    private void refactoringJoinedSubclasses(JoinedSubclass[] joinedSubclazz) {
        // <property name="">
        for (int i = 0; i < joinedSubclazz.length; i++) {

            String clsName = joinedSubclazz[i].getAttributeValue(nameAttrib);
            if (clsName != null && clsName.equals(className)) {
                refactoringProperty(joinedSubclazz[i].getProperty2());

                refactoringMaps(joinedSubclazz[i].getMap());

                refactoringDynamicComponents(joinedSubclazz[i].getDynamicComponent());

                refactoringComponents(joinedSubclazz[i].getComponent());

                refactoringSets(joinedSubclazz[i].getSet());

                refactoringLists(joinedSubclazz[i].getList());

                refactoringArrays(joinedSubclazz[i].getArray());

                refactoringBags(joinedSubclazz[i].getBag());

                refactoringIdbags(joinedSubclazz[i].getIdbag());

                refactoringAnys(joinedSubclazz[i].getAny());

                refactoringManyToOnes(joinedSubclazz[i].getManyToOne());

                refactoringPropertiez(joinedSubclazz[i].getProperties());

                refactoringOneToOnes(joinedSubclazz[i].getOneToOne());
            }

            // <joined-subclass> elements can be contained inside <joined-subclass> 
            refactoringJoinedSubclasses(joinedSubclazz[i].getJoinedSubclass());
        }
    }

    private void refactoringUnionSubclasses(UnionSubclass[] unionSubclazz) {
        // <property name="">
        for (int i = 0; i < unionSubclazz.length; i++) {

            String clsName = unionSubclazz[i].getAttributeValue(nameAttrib);
            if (clsName != null && clsName.equals(className)) {
                refactoringProperty(unionSubclazz[i].getProperty2());

                refactoringMaps(unionSubclazz[i].getMap());

                refactoringDynamicComponents(unionSubclazz[i].getDynamicComponent());

                refactoringComponents(unionSubclazz[i].getComponent());

                refactoringSets(unionSubclazz[i].getSet());

                refactoringLists(unionSubclazz[i].getList());

                refactoringArrays(unionSubclazz[i].getArray());

                refactoringBags(unionSubclazz[i].getBag());

                refactoringIdbags(unionSubclazz[i].getIdbag());

                refactoringAnys(unionSubclazz[i].getAny());

                refactoringManyToOnes(unionSubclazz[i].getManyToOne());

                refactoringPropertiez(unionSubclazz[i].getProperties());

                refactoringOneToOnes(unionSubclazz[i].getOneToOne());
            }

            // <union-subclass> elements can be inside <union-subclass> 
            refactoringUnionSubclasses(unionSubclazz[i].getUnionSubclass());
        }
    }

    private void refactoringCompositeId(CompositeId compositeId) {
        if (compositeId == null) {
            return;
        }

        if (compositeId != null) {
            String compositeIdPropName = compositeId.getAttributeValue(nameAttrib);
            if (compositeIdPropName != null && compositeIdPropName.equals(origName)) {
                compositeId.setAttributeValue(nameAttrib, newName);
            }

            //<composite-id><key-property name="">
            refactoringKeyProperty(compositeId.getKeyProperty());

            //<composite-id><key-many-to-one name="">
            refactoringKeyManyToOne(compositeId.getKeyManyToOne());

        }
    }

    private void refactoringKeyManyToOne(KeyManyToOne[] keyManyToOnes) {
        for (int i = 0; i < keyManyToOnes.length; i++) {
            String name = keyManyToOnes[i].getAttributeValue(nameAttrib);
            if (name != null && name.equals(origName)) {
                keyManyToOnes[i].setAttributeValue(nameAttrib, newName);
            }
        }
    }

    private void refactoringMaps(Map[] maps) {
        for (int i = 0; i < maps.length; i++) {
            // <map name="">
            String mapName = maps[i].getAttributeValue(nameAttrib);
            if (mapName != null && mapName.equals(origName)) {
                maps[i].setAttributeValue(nameAttrib, newName);
            }

            CompositeMapKey mKey = maps[i].getCompositeMapKey();
            if (mKey != null) {
                refactoringKeyProperty(mKey.getKeyProperty());
                refactoringKeyManyToOne(mKey.getKeyManyToOne());
            }

            CompositeIndex index = maps[i].getCompositeIndex();
            if (index != null) {
                refactoringKeyProperty(index.getKeyProperty());
                refactoringKeyManyToOne(index.getKeyManyToOne());
            }

            refactoringCompositeElement(maps[i].getCompositeElement());
        }
    }

    private void refactoringKeyProperty(KeyProperty[] keyProps) {
        for (int i = 0; i < keyProps.length; i++) {
            String keyPropName = keyProps[i].getAttributeValue(nameAttrib);
            if (keyPropName != null && keyPropName.equals(origName)) {
                keyProps[i].setAttributeValue(nameAttrib, newName);
            }
        }
    }

    private void refactoringNaturalId(NaturalId nId) {
        if (nId == null) {
            return;
        }

        refactoringProperty(nId.getProperty2());

        refactoringAnys(nId.getAny());

        refactoringManyToOnes(nId.getManyToOne());
    }

    private void refactoringJoins(Join[] joins) {
        for (int i = 0; i < joins.length; i++) {
            Join theJoin = joins[i];

            //<property name="">
            refactoringProperty(theJoin.getProperty2());

            refactoringAnys(theJoin.getAny());

            refactoringManyToOnes(theJoin.getManyToOne());
        }
    }

    private void refactoringProperty(Property[] clazzProps) {
        // <property name="">
        for (int pi = 0; pi < clazzProps.length; pi++) {
            String propName = clazzProps[pi].getAttributeValue(nameAttrib);
            if (propName.equals(origName)) {
                clazzProps[pi].setAttributeValue(nameAttrib, newName); 
                break;
            }
        }
    }

    private void refactoringComponents(Component[] hbModelComps) {
        for (int i = 0; i < hbModelComps.length; i++) {
            Component thisComp = hbModelComps[i];

            refactoringMaps(thisComp.getMap());

            refactoringSets(thisComp.getSet());

            refactoringArrays(thisComp.getArray());

            refactoringBags(thisComp.getBag());

            refactoringAnys(thisComp.getAny());

            refactoringManyToOnes(thisComp.getManyToOne());

            refactoringOneToOnes(thisComp.getOneToOne());
        }
    }

    private void refactoringDynamicComponents(DynamicComponent[] hbModelDynComps) {
        for (int i = 0; i < hbModelDynComps.length; i++) {
            DynamicComponent thisComp = hbModelDynComps[i];

            refactoringMaps(thisComp.getMap());

            refactoringSets(thisComp.getSet());

            refactoringArrays(thisComp.getArray());

            refactoringBags(thisComp.getBag());

            refactoringAnys(thisComp.getAny());

            refactoringManyToOnes(thisComp.getManyToOne());

            refactoringOneToOnes(thisComp.getOneToOne());
        }
    }

    private void refactoringSets(Set[] sets) {
        for (int i = 0; i < sets.length; i++) {
            String nameValue = sets[i].getAttributeValue(nameAttrib);
            if (nameValue != null && nameValue.equals(origName)) {
                sets[i].setAttributeValue(nameAttrib, newName);
            }

            refactoringCompositeElement(sets[i].getCompositeElement());
        }
    }

    private void refactoringLists(List[] lists) {
        for (int i = 0; i < lists.length; i++) {
            String nameValue = lists[i].getAttributeValue(nameAttrib);
            if (nameValue != null && nameValue.equals(origName)) {
                lists[i].setAttributeValue(nameAttrib, newName);
            }

            refactoringCompositeElement(lists[i].getCompositeElement());
        }
    }

    private void refactoringAnys(Any[] anys) {
        for (int i = 0; i < anys.length; i++) {
            String name = anys[i].getAttributeValue(nameAttrib);
            if (name != null && name.equals(origName)) {
                anys[i].setAttributeValue(nameAttrib, newName);
            }
        }
    }

    private void refactoringManyToOnes(ManyToOne[] manyToOnes) {
        for (int i = 0; i < manyToOnes.length; i++) {
            String name = manyToOnes[i].getAttributeValue(nameAttrib);
            if (name != null && name.equals(origName)) {
                manyToOnes[i].setAttributeValue(nameAttrib, newName);
            }
        }
    }

    private void refactoringArrays(Array[] arrays) {
        for (int i = 0; i < arrays.length; i++) {
            refactoringCompositeElement(arrays[i].getCompositeElement());
        }
    }

    private void refactoringBags(Bag[] bags) {
        for (int i = 0; i < bags.length; i++) {
            refactoringCompositeElement(bags[i].getCompositeElement());
        }
    }

    private void refactoringIdbags(Idbag[] idbags) {
        for (int i = 0; i > idbags.length; i++) {
            refactoringCompositeElement(idbags[i].getCompositeElement());
        }
    }

    private void refactoringCompositeElement(CompositeElement compElem) {
        if (compElem == null) {
            return;
        }

        refactoringManyToOnes(compElem.getManyToOne());

        refactoringNestedCompositeElements(compElem.getNestedCompositeElement());
    }

    private void refactoringNestedCompositeElements(NestedCompositeElement[] elems) {
        for (int i = 0; i < elems.length; i++) {
            refactoringManyToOnes(elems[i].getManyToOne());
        }
    }

    private void refactoringPropertiez(Properties[] propertiez) {
        for (int i = 0; i < propertiez.length; i++) {
            refactoringProperty(propertiez[i].getProperty2());

            refactoringManyToOnes(propertiez[i].getManyToOne());
        }
    }

    private void refactoringOneToOnes(OneToOne[] oneToOnes) {
        for (int i = 0; i < oneToOnes.length; i++) {
            String name = oneToOnes[i].getAttributeValue(nameAttrib);
            if (name != null && name.equals(origName)) {
                oneToOnes[i].setAttributeValue(nameAttrib, newName);
            }
        }
    }
}
