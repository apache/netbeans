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

package org.netbeans.modules.java.api.common.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Support for reading/writting classpath like properties.
 * @author Petr Hrebejk, Radko Najman, David Konecny
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class ClassPathSupport {

    /**
     * Classpath for endorsed libraries. See {@link ClassPath} for other classpath
     * types.
     * @since org.netbeans.modules.java.api.common/0 1.11
     */
    public static final String ENDORSED = "classpath/endorsed";
                
    // Prefixes and suffixes of classpath
    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    private static final String LIBRARY_SUFFIX = ".classpath}"; // NOI18N

    // Prefixes and suffixes of classpath
    private static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
    
    private PropertyEvaluator evaluator;
    private ReferenceHelper referenceHelper;
    private AntProjectHelper antProjectHelper;
    private UpdateHelper updateHelper;
    //@GuardedBy("ClassPathSupport.class")
    private static Set<String> wellKnownPaths;
    private static String antArtifactPrefix = ANT_ARTIFACT_PREFIX;
        
    private Callback callback;

    /** Creates a new instance of ClassPathSupport */
    public ClassPathSupport( PropertyEvaluator evaluator, 
                              ReferenceHelper referenceHelper,
                              AntProjectHelper antProjectHelper,
                              UpdateHelper updateHelper,
                              Callback callback) {
        assert referenceHelper != null;
        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
        this.updateHelper = updateHelper;
        this.callback = callback;
    }

    /** Creates list of <CODE>Items</CODE> from given property.
     */    
    public Iterator<Item> itemsIterator(String propertyValue) {
        return itemsIterator(propertyValue, null);
    }
    
    public Iterator<Item> itemsIterator( String propertyValue, String projectXMLElement ) {
        // XXX More performance frendly impl. would retrun a lazzy iterator.
        return itemsList( propertyValue, projectXMLElement ).iterator();
    }
    
    public List<Item> itemsList(String propertyValue) {
        return itemsList(propertyValue, null);
    }
    
    public List<Item> itemsList( String propertyValue, String projectXMLElement ) {    
        
        String pe[] = PropertyUtils.tokenizePath( propertyValue == null ? "": propertyValue ); // NOI18N        
        List<Item> items = new ArrayList<Item>( pe.length );        
        for( int i = 0; i < pe.length; i++ ) {
            Item item;

            // First try to find out whether the item is well known classpath
            if ( isWellKnownPath( pe[i] ) ) {
                // Some well know classpath
                item = Item.create( pe[i]);
            } 
            else if ( isLibrary( pe[i] ) ) {
                //Library from library manager
                String libraryName = getLibraryNameFromReference(pe[i]);
                assert libraryName != null : "Not a library reference: "+pe[i];
                Library library = referenceHelper.findLibrary(libraryName);
                if ( library == null ) {
                    item = Item.createBroken( Item.TYPE_LIBRARY, pe[i]);
                }
                else {
                    item = Item.create( library, pe[i]);
                }
            } 
            else if ( isAntArtifact( pe[i] ) ) {
                // Ant artifact from another project
                Object[] ret = referenceHelper.findArtifactAndLocation(pe[i]);
                if ( ret[0] == null || ret[1] == null ) {
                    item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i]);
                }
                else {
                    AntArtifact artifact = (AntArtifact)ret[0];
                    URI uri = (URI)ret[1];
                    File usedFile = antProjectHelper.resolveFile(evaluator.evaluate(pe[i]));
                    File artifactFile = Utilities.toFile(BaseUtilities.normalizeURI(Utilities.toURI(artifact.getScriptLocation()).resolve(uri)));
                    if (usedFile.equals(artifactFile)) {
                        item = Item.create( artifact, uri, pe[i]);
                    }
                    else {
                        item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i]);
                    }
                }
            } else {
                // Standalone jar or property
                String eval = evaluator.evaluate( pe[i] );
                boolean valid = false, multi = false;
                if (eval != null) {
                    final String[] pathElements = PropertyUtils.tokenizePath(eval);
                    multi = pathElements.length > 1;
out:                for (int tmp = 0; tmp == 0; tmp++) {
                        for (String pathElement : pathElements) {
                            final File f = antProjectHelper.resolveFile( pathElement );
                            if (!f.exists()) {
                                break out;
                            }
                        }
                        valid = true;
                    }
                }

                String propertyName = CommonProjectUtils.getAntPropertyName(pe[i]);
                String variableBaseProperty = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(propertyName);
                if (!isVariableBasedReference(variableBaseProperty)) {
                    variableBaseProperty = null;
                }

                if (multi) {
                    if (valid) {
                        item = Item.create(pe[i]);
                    } else {
                        item = Item.createBroken(Item.TYPE_CLASSPATH, pe[i]);
                    }
                } else {
                    if (valid) {
                        item = Item.create( eval, FileUtil.toFile(antProjectHelper.getProjectDirectory()), pe[i], variableBaseProperty);
                    } else {
                        item = Item.createBroken( eval, FileUtil.toFile(antProjectHelper.getProjectDirectory()), pe[i]);
                    }
                    item.initSourceAndJavadoc(antProjectHelper);
                }
            }
            
            items.add( item );
        }
        if (projectXMLElement != null) {
            callback.readAdditionalProperties(items, projectXMLElement);
        }

        return items;        
    }
    
    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     * !! This method may update project.xml !!
     */
    public String[] encodeToStrings(List<Item> classpath) {
        return encodeToStrings(classpath, null);
    }
    public String[] encodeToStrings( List<Item> classpath, String projectXMLElement) {
        return encodeToStrings(classpath, projectXMLElement, "classpath");
    }
    
    /**
     * 
     * @param classpath
     * @param projectXMLElement
     * @param libraryVolumeType
     * @return
     * @since 1.22
     */
    public String[] encodeToStrings( List<Item> classpath, String projectXMLElement, String libraryVolumeType ) {
        List<String> items = new ArrayList<String>();
        for (Item item : classpath) {
            String reference = null;

            switch( item.getType() ) {

                case Item.TYPE_JAR:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }
                    if (reference == null) {
                        // pass null as expected artifact type to always get file reference
                        reference = referenceHelper.createForeignFileReferenceAsIs(item.getFilePath(), null);
                        if (item.getVariableBasedProperty() != null) {
                            // replace file reference with variable based reference:
                            EditableProperties ep = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            ep.setProperty(CommonProjectUtils.getAntPropertyName(reference), item.getVariableBasedProperty());
                            antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                        }
                        item.setReference(reference);
                    }
                    item.saveSourceAndJavadoc(referenceHelper, updateHelper);
                    break;
                case Item.TYPE_LIBRARY:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }                    
                    Library library = item.getLibrary();                                       
                    if (reference == null) {
                        if ( library == null ) {
                            break;
                        }
                        reference = getLibraryReference( item, libraryVolumeType );
                        item.setReference(reference);
                    }
                    break;    
                case Item.TYPE_ARTIFACT:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }
                    AntArtifact artifact = item.getArtifact();                                       
                    if ( reference == null) {
                        if ( artifact == null ) {
                            break;
                        }
                        reference = referenceHelper.addReference( item.getArtifact(), item.getArtifactURI());
                        item.setReference(reference);
                    }
                    break;
                case Item.TYPE_CLASSPATH:
                    reference = item.getReference();
                    break;
            }
            items.add(reference + ":");
        }

        if ( projectXMLElement != null ) {
            callback.storeAdditionalProperties(classpath, projectXMLElement );
        }
        String arr[] = items.toArray(new String[0]);
        // remove ":" from last item:
        if (arr.length != 0) {
            arr[arr.length-1] = arr[arr.length-1].substring(0, arr[arr.length-1].length()-1);
        }
        return arr;
    }
    
    public String getLibraryReference( Item item ) {
        return getLibraryReference(item, "classpath");
    }
    
    /**
     * 
     * @param item
     * @param volumeType
     * @return
     * @since 1.22
     */
    public String getLibraryReference( Item item, String volumeType ) {
        if ( item.getType() != Item.TYPE_LIBRARY ) {
            throw new IllegalArgumentException( "Item must be of type LIBRARY" ); // NOI18N
        }
        return referenceHelper.createLibraryReference(item.getLibrary(), volumeType); // NOI18N
    }    
    
    // Private methods ---------------------------------------------------------

    private boolean isWellKnownPath( String property ) {
        return getWellKnownPaths().contains( property );
    }
    
    private boolean isAntArtifact( String property ) {        
        return antArtifactPrefix == null ? false : property.startsWith( antArtifactPrefix );
    }
    
    private static boolean isLibrary( String property ) {
        return property.startsWith(LIBRARY_PREFIX) && property.endsWith(LIBRARY_SUFFIX);
    }

    @NonNull
    private static synchronized Set<String> getWellKnownPaths() {
        if (wellKnownPaths == null) {
             wellKnownPaths = Collections.unmodifiableSet(
                     new HashSet<String>(Arrays.asList(ProjectProperties.WELL_KNOWN_PATHS)));
        }
        return wellKnownPaths;
    }

    public static boolean isVariableBasedReference(String ref) {
        return ref != null && ref.startsWith("${var."); // NOI18N
    }
        
    // Innerclasses ------------------------------------------------------------
    
    /** Item of the classpath.
     */    
    public static class Item {
        
        // Types of the classpath elements
        public static final int TYPE_JAR = 0;
        public static final int TYPE_LIBRARY = 1;
        public static final int TYPE_ARTIFACT = 2;
        public static final int TYPE_CLASSPATH = 3;

        private static final String REF_START = "${file.reference."; //NOI18N
        private static final int REF_START_INDEX = REF_START.length();
        private static final String JAVADOC_START = "${javadoc.reference."; //NOI18N
        private static final String SOURCE_START = "${source.reference."; //NOI18N
        
        private Object object;
        private URI artifactURI;
        private int type;
        private String property;
        private boolean broken;
        private String sourceFilePath;
        private String javadocFilePath;
        
        private String initialSourceFilePath;
        private String initialJavadocFilePath;
        private String libraryName;
        private String variableBasedProperty;
        
        private Map<String, String> additionalProperties = new HashMap<String, String>();

        private Item( int type, Object object, String property, boolean broken) {
            this.type = type;
            this.object = object;
            this.broken = broken;
            if (object == null || type == TYPE_CLASSPATH || broken ||
                    (type == TYPE_JAR && object instanceof RelativePath) ||
                    (type == TYPE_ARTIFACT && (object instanceof AntArtifact)) ||
                    (type == TYPE_LIBRARY && (object instanceof Library))) {
                this.property = property;
            } else {
                throw new IllegalArgumentException ("invalid classpath item, type=" + type + " object type:" + object.getClass().getName());
            }
        }
        
        private Item( int type, Object object, URI artifactURI, String property) {
            this( type, object, property);
            this.artifactURI = artifactURI;
        }
        
        private Item(int type, Object object, String property) {
            this(type, object, property, false);
        }
              
        public String getAdditionalProperty(String key) {
            return additionalProperties.get(key);
        }
        
        public void setAdditionalProperty(String key, String value) {
            additionalProperties.put(key, value);
        }
        
        // Factory methods -----------------------------------------------------
        
        
        public static Item create( Library library, String property) {
            if ( library == null ) {
                throw new IllegalArgumentException( "library must not be null" ); // NOI18N
            }
                        
            String libraryName = library.getName();
            Item itm = new Item( TYPE_LIBRARY, library, property); //NOI18N
            itm.libraryName = libraryName;
            itm.reassignLibraryManager( library.getManager() );
            return itm;
        }
        
        public static Item create( AntArtifact artifact, URI artifactURI, String property) {
            if ( artifactURI == null ) {
                throw new IllegalArgumentException( "artifactURI must not be null" ); // NOI18N
            }
            if ( artifact == null ) {
                throw new IllegalArgumentException( "artifact must not be null" ); // NOI18N
            }
            return new Item( TYPE_ARTIFACT, artifact, artifactURI, property);
        }
        
        public static Item create( String filePath, File base, String property, String variableBasedProperty) {
            if ( filePath == null ) {
                throw new IllegalArgumentException( "file path must not be null" ); // NOI18N
            }
            Item i = new Item( TYPE_JAR, RelativePath.createRelativePath(filePath, base), property);
            i.variableBasedProperty = variableBasedProperty;
            return i;
        }
        
        public static Item create( String property) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null" ); // NOI18N
            }
            return new Item ( TYPE_CLASSPATH, null, property);
        }
        
        public static Item createBroken( int type, String property) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null in broken items" ); // NOI18N
            }
            Item itm = new Item( type, null, property, true);
            if (type == TYPE_LIBRARY) {
                Pattern LIBRARY_REFERENCE = Pattern.compile("\\$\\{libs\\.([^${}]+)\\.[^${}]+\\}"); // NOI18N
                Matcher m = LIBRARY_REFERENCE.matcher(property);
                if (m.matches()) {
                    itm.libraryName = m.group(1);
                } else {
                    assert false : property;
                }
            }
            return itm;
        }
        
        public static Item createBroken(String filePath, File base, String property) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null in broken items" ); // NOI18N
            }
            return new Item(TYPE_JAR, RelativePath.createRelativePath(filePath, base), property, true);
        }
        
        // Instance methods ----------------------------------------------------
        
        public int getType() {
            return type;
        }
        
        public Library getLibrary() {
            if ( getType() != TYPE_LIBRARY ) {
                throw new IllegalArgumentException( "Item is not of required type - LIBRARY" ); // NOI18N
            }
            if (isBroken()) {
                return null;
            }
            assert object == null || object instanceof Library :
                "Invalid object type: "+object.getClass().getName()+" instance: "+object.toString()+" expected type: Library";   //NOI18N
            return (Library)object;
        }
        
        public File getResolvedFile() {
            if ( getType() != TYPE_JAR ) {
                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
            }
            // for broken item: one will get java.io.File or null (#113390)
            return ((RelativePath)object).getResolvedFile();
        }
        
        public String getFilePath() {
            if ( getType() != TYPE_JAR ) {
                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
            }
            // for broken item: one will get java.io.File or null (#113390)
            return ((RelativePath)object).getFilePath();
        }
        
        public String getVariableBasedProperty() {
            if ( getType() != TYPE_JAR ) {
                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
            }
            return variableBasedProperty;
        }

        public AntArtifact getArtifact() {
            if ( getType() != TYPE_ARTIFACT ) {
                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
            }
            if (isBroken()) {
                return null;
            }
            return (AntArtifact)object;
        }
        
        public URI getArtifactURI() {
            if ( getType() != TYPE_ARTIFACT ) {
                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
            }
            return artifactURI;
        }
        
        public void reassignLibraryManager(LibraryManager newManager) {
            if (getType() != TYPE_LIBRARY) {
                throw new IllegalArgumentException(" reassigning only works for type - LIBRARY");
            }
            assert libraryName != null;
            if (getLibrary() == null || newManager != getLibrary().getManager()) {
                Library lib = newManager.getLibrary(libraryName);
                if (lib == null) {
                    broken = true;
                    object = null;
                } else {
                    object = lib;
                    broken = false;
                }
            }
        }
        
        public String getReference() {
            return property;
        }
        
        public void setReference(String property) {
            this.property = property;
        }

        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        private String getSourceProperty() {
            if (property == null || !property.startsWith(REF_START)) {
                return null;
            }            
            return CommonProjectUtils.getAntPropertyName(SOURCE_START + property.substring(REF_START_INDEX));
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        private String getJavadocProperty() {
            if (property == null || !property.startsWith(REF_START)) {
                return null;
            }            
            return CommonProjectUtils.getAntPropertyName(JAVADOC_START + property.substring(REF_START_INDEX));
        }
        
        public boolean canEdit () {            
            if (isBroken()) {
                //Broken item cannot be edited
                return false;
            }
            if (getType() == TYPE_JAR) {
                //Jar can be edited only for ide created reference
                if (property == null) {
                    //Just added item, allow editing
                    return true;
                }
                return getSourceProperty() != null && getJavadocProperty() != null;
            }
            else if (getType() == TYPE_LIBRARY) {
                //Library can be edited
                return true;
            }
            //Otherwise: project, classpath - cannot be edited 
            return false;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        public String getSourceFilePath() {
            return sourceFilePath;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        public String getJavadocFilePath() {
            return javadocFilePath;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         */
        public void setJavadocFilePath(String javadoc) {
            javadocFilePath = javadoc;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         */
        public void setSourceFilePath(String source) {
            sourceFilePath = source;
        }
        
        private void setInitialSourceAndJavadoc(String source, String javadoc) {
            initialSourceFilePath = source;
            initialJavadocFilePath = javadoc;
            sourceFilePath = source;
            javadocFilePath = javadoc;
        }
        
        private boolean hasChangedSource() {
            if (initialSourceFilePath == null && sourceFilePath == null) {
                return false;
            }
            if (initialSourceFilePath != null && sourceFilePath != null) {
                return ! initialSourceFilePath.equals(sourceFilePath);
            }
            return true;
        }
        
        private boolean hasChangedJavadoc() {
            if (initialJavadocFilePath == null && javadocFilePath == null) {
                return false;
            }
            if (initialJavadocFilePath != null && javadocFilePath != null) {
                return ! initialJavadocFilePath.equals(javadocFilePath);
            }
            return true;
        }
        
        public boolean isBroken() {
            return broken;
        }
                        
        // TODO ideally this should be called from constructor but becaue of missing 'evaluator'
        // I'm making it private method which needs to be called after construction of JAR Item
        public void initSourceAndJavadoc(AntProjectHelper helper) {
            assert getType() == Item.TYPE_JAR : getType();
            EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String value = null;
            String ref = getSourceProperty();                
            if (ref != null) {
                value = ep.getProperty(ref);
            }
            String value2 = null;
            ref = getJavadocProperty();
            if (ref != null) {
                value2 = ep.getProperty(ref);
            }
            setInitialSourceAndJavadoc(value, value2);
        }
        

        public void saveSourceAndJavadoc(ReferenceHelper referenceHelper, UpdateHelper updateHelper) {
            assert getType() == Item.TYPE_JAR : getType();
            if (hasChangedSource()) {
                if (getSourceFilePath() != null) {
                    referenceHelper.createExtraForeignFileReferenceAsIs(getSourceFilePath(), getSourceProperty());
                } else {
                    removeSource(updateHelper);
                }
            }
            if (hasChangedJavadoc()) {
                if (getJavadocFilePath() != null) {
                    referenceHelper.createExtraForeignFileReferenceAsIs(getJavadocFilePath(), getJavadocProperty());
                } else {
                    removeJavadoc(updateHelper);
                }
            }
        }
        
        public void removeSourceAndJavadoc(UpdateHelper updateHelper) {
            assert getType() == Item.TYPE_JAR : getType();
            removeSource(updateHelper);
            removeJavadoc(updateHelper);
        }
    
        private void removeSource(UpdateHelper updateHelper) {
            //oh well, how do I do this otherwise??
            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            if (getSourceProperty() != null) {
                ep.remove(getSourceProperty());
            }
            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    
        private void removeJavadoc(UpdateHelper updateHelper) {
            //oh well, how do I do this otherwise??
            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            if (getJavadocProperty() != null) {
                ep.remove(getJavadocProperty());
            }
            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    
        public void updateJarReference(AntProjectHelper helper) {
            assert getType() == Item.TYPE_JAR : getType();
            EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String value = ep.getProperty(CommonProjectUtils.getAntPropertyName(getReference()));
            if (value != null) {
                RelativePath rp = (RelativePath)object;
                rp.setFilePath(value);
            }
            
            String ref = getSourceProperty();
            if (ref != null) {
                value = ep.getProperty(ref);
                if (value != null) {
                    setSourceFilePath(value);
                }
            }
            ref = getJavadocProperty();
            if (ref != null) {
                value = ep.getProperty(ref);
                if (value != null) {
                    setJavadocFilePath(value);
                }
            }        
        }
        
        @Override
        public int hashCode() {
        
            int hash = getType();

            if (broken) {
                return 42;
            }
            
            switch ( getType() ) {
                case TYPE_ARTIFACT:
                    hash += getArtifact().getType().hashCode();                
                    hash += getArtifact().getScriptLocation().hashCode();
                    hash += getArtifactURI().hashCode();
                    break;
                case TYPE_CLASSPATH:
                    hash += property.hashCode();
                    break;
                default:
                    hash += object.hashCode();
            }

            return hash;
        }
    
        @Override
        public boolean equals( Object itemObject ) {

            if ( !( itemObject instanceof Item ) ) {
                return false;
            }
            
            Item item = (Item)itemObject;

            if ( getType() != item.getType() ) {
                return false;
            }
            
            if ( isBroken() != item.isBroken() ) {
                return false;
            }
            
            if ( isBroken() ) {
                return getReference().equals( item.getReference() );
            }

            switch ( getType() ) {
                case TYPE_ARTIFACT:
                    if (!(getArtifact().getType()).equals(item.getArtifact().getType())) {
                        return false;
                    }

                    if ( !getArtifact().getScriptLocation().equals( item.getArtifact().getScriptLocation() ) ) {
                        return false;
                    }

                    if ( !getArtifactURI().equals( item.getArtifactURI() ) ) {
                        return false;
                    }
                    return true;
                case TYPE_CLASSPATH:
                    return property.equals( item.property );
                default:
                    return object.equals( item.object );
            }

        }
     
        @Override
        public String toString() {
            return "artifactURI=" + artifactURI
                    + ", type=" + type 
                    + ", property=" + property
                    + ", object=" + object
                    + ", broken=" + broken
                    + ", additional=" + additionalProperties;
        }
        
    }
            
    /**
     * Returns library name if given property represents library reference 
     * otherwise return null.
     * 
     * @param property property to test
     * @return library name or null
     */
    public static String getLibraryNameFromReference(String property) {
        if (!isLibrary(property)) {
            return null;
        }
        return property.substring(LIBRARY_PREFIX.length(), property.lastIndexOf('.')); //NOI18N
    }
    
    private static final class RelativePath {
        private String filePath;
        private final File base;
        private final File resolvedFile;

        private RelativePath(@NonNull String filePath, @NonNull File base) {
            Parameters.notNull("filePath", filePath);
            Parameters.notNull("base", base);
            this.filePath = filePath;
            this.base = base;
            this.resolvedFile = PropertyUtils.resolveFile(base, filePath);
        }

        public static RelativePath createRelativePath(String filePath, File base) {
            return new RelativePath(filePath, base);
        }
        
        public String getFilePath() {
            return filePath;
        }

        private void setFilePath(@NonNull final String filePath) {
            Parameters.notNull("filePath", filePath);   //NOI18N
            this.filePath = filePath;
        }

        public File getResolvedFile() {
            return resolvedFile;
        }

        @Override
        public int hashCode() {
            return filePath.hashCode() + base.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RelativePath)) {
                return false;
            }
            RelativePath other = (RelativePath)obj;
            return filePath.equals(other.filePath) && base.equals(other.base);
        }
        
    }
    
    /**
     * Optional callback to customize classpath support behaviour.
     */
    public static interface Callback {
        
        /**
         * Reads additional information from project XML for classpath items.
         */
        void readAdditionalProperties(List<Item> items, String projectXMLElement);
        
        /**
         * Writes additional information from classpath items to project XML.
         */
        void storeAdditionalProperties(List<Item> items, String projectXMLElement);
        
    }

}
