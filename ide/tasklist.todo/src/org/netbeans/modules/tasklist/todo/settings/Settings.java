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

package org.netbeans.modules.tasklist.todo.settings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
public final class Settings {
    
    public static final String PROP_PATTERN_LIST = "patternList"; //NOI18N
    public static final String PROP_SCAN_COMMENTS_ONLY = "scanCommentsOnly"; //NOI18N
    public static final String PROP_IDENTIFIERS_LIST = "identifiersList"; //NOI18N
    
    private static Settings theInstance;
    private static final String OBJECT_DELIMITER = "|"; //NOI18N
    private static final String FIELD_DELIMITER = ","; //NOI18N

    private final ArrayList<String> patterns = new ArrayList<String>( 10 );
    private Map<String, ExtensionIdentifier> ext2comments = new HashMap<String, ExtensionIdentifier>();
    private Map<String, MimeIdentifier> mime2comments = new HashMap<String, MimeIdentifier>();
    private final Map<String, ExtensionIdentifier> ext2commentsDefault = new HashMap<String, ExtensionIdentifier>(25);
    private final Map<String, MimeIdentifier> mime2commentsDefault = new HashMap<String, MimeIdentifier>(25);
    private boolean scanCommentsOnly = true;
    
    private PropertyChangeSupport propertySupport;
    private static final String MIME_IDENTIFIERS = "mimeidentifiers";
    private static final String EXT_IDENTIFIERS = "extidentifiers";
    
    /** Creates a new instance of Settings */
    private Settings() {
        patterns.addAll( decodePatterns( getPreferences().get( "patterns",  //NOI18N
                "@todo|TODO|FIXME|XXX|PENDING|<<<<<<<" )) ); //NOI18N
        
        scanCommentsOnly = getPreferences().getBoolean( "scanCommentsOnly", true ); //NOI18N
        
        ext2commentsDefault.put( "JAVA", new ExtensionIdentifier("JAVA", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "JS", new ExtensionIdentifier("JS", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "C", new ExtensionIdentifier("C", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "CPP", new ExtensionIdentifier("CPP", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "CXX", new ExtensionIdentifier("CXX", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "CC", new ExtensionIdentifier("CC", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "H", new ExtensionIdentifier("H", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "HPP", new ExtensionIdentifier("HPP", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "HTML", new ExtensionIdentifier("HTML", new CommentTags( "<!--", "-->"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "XHTML", new ExtensionIdentifier("XHTML", new CommentTags( "<!--", "-->"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "HTM", new ExtensionIdentifier("HTM", new CommentTags( "<!--", "-->"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "XML", new ExtensionIdentifier("XML", new CommentTags( "<!--", "-->"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "JSP", new ExtensionIdentifier("JSP", new CommentTags( "<%--", "--%>"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "CSS", new ExtensionIdentifier("CSS", new CommentTags( "/*", "*/"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "SCSS", new ExtensionIdentifier("SCSS", new CommentTags("//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "LESS", new ExtensionIdentifier("LESS", new CommentTags("//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "PROPERTIES", new ExtensionIdentifier("PROPERTIES", new CommentTags("#"))); //NOI18N //NOI18N
        ext2commentsDefault.put( "SH", new ExtensionIdentifier("SH", new CommentTags("#"))); //NOI18N //NOI18N
        ext2commentsDefault.put( "RB", new ExtensionIdentifier("RB", new CommentTags("#"))); //NOI18N //NOI18N
        ext2commentsDefault.put( "PHP", new ExtensionIdentifier("PHP", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "PY", new ExtensionIdentifier("PY", new CommentTags( "#", "\"\"\"", "\"\"\""))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "SCALA", new ExtensionIdentifier("SCALA", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "GROOVY", new ExtensionIdentifier("GROOVY", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "FX", new ExtensionIdentifier("FX", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "TWIG", new ExtensionIdentifier("TWIG", new CommentTags( "{#", "#}"))); //NOI18N //NOI18N //NOI18N
        ext2commentsDefault.put( "TPL", new ExtensionIdentifier("TPL", new CommentTags( "{*", "*}"))); //NOI18N //NOI18N //NOI18N
        
        mime2commentsDefault.put( "text/x-java", new MimeIdentifier("text/x-java", "Java Files", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        mime2commentsDefault.put( "text/html", new MimeIdentifier("text/html", "HTML Files", new CommentTags( "<!--", "-->"))); //NOI18N //NOI18N //NOI18N
        mime2commentsDefault.put( "application/x-httpd-eruby", new MimeIdentifier("application/x-httpd-eruby", "", new CommentTags( "<!--", "-->"))); //NOI18N //NOI18N //NOI18N
        mime2commentsDefault.put( "text/x-yaml", new MimeIdentifier("text/x-yaml", "Yaml Files", new CommentTags("#"))); //NOI18N //NOI18N
        mime2commentsDefault.put( "text/x-python", new MimeIdentifier("text/x-python", "Python Files", new CommentTags("#", "\"\"\"", "\"\"\""))); //NOI18N //NOI18N
        mime2commentsDefault.put( "text/x-fx", new MimeIdentifier("text/x-fx", "JavaFX Files", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N

        // Ruby, PHP, etc have file extensions listed above, but they are listed here by mime type as well
        // because there are many other common file extensions used for them.
        mime2commentsDefault.put( "text/x-ruby", new MimeIdentifier("text/x-ruby", "Ruby Files", new CommentTags("#"))); //NOI18N //NOI18N
        mime2commentsDefault.put( "text/x-php5", new MimeIdentifier("text/x-php", "PHP Files", new CommentTags( "//", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N
        mime2commentsDefault.put( "text/sh", new MimeIdentifier("text/sh", "", new CommentTags("#"))); //NOI18N //NOI18N
        mime2commentsDefault.put( "text/x-sql", new MimeIdentifier("text/x-sql", "SQL Files", new CommentTags( "--", "/*", "*/"))); //NOI18N //NOI18N //NOI18N //NOI18N


        String encodedMime = getPreferences().get(MIME_IDENTIFIERS, "");
        if (encodedMime.isEmpty()) {
            mime2comments = new HashMap<String, MimeIdentifier>(mime2commentsDefault);
        } else {
            mime2comments = decodeMimeIdentifiers(encodedMime);
        }

        String encodedExt = getPreferences().get(EXT_IDENTIFIERS, "");
        if (encodedExt.isEmpty()) {
            ext2comments = new HashMap<String, ExtensionIdentifier>(ext2commentsDefault);
        } else {
            ext2comments = decodeExtIdentifiers(encodedExt);
        }
    }
    
    public static final Settings getDefault() {
        if( null == theInstance )
            theInstance = new Settings();
        return theInstance;
    }
    
    public Collection<String> getPatterns() {
        return Collections.unmodifiableCollection( patterns );
    }
    
    public void setPatterns( Collection<String> newPatterns ) {
        patterns.clear();
        patterns.addAll( newPatterns );
        getPreferences().put( "patterns", encodePatterns(newPatterns) ); //NOI18N
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.firePropertyChange( PROP_PATTERN_LIST, null, getPatterns() );
    }

    public List<MimeIdentifier> getMimeIdentifiers() {
        ArrayList<MimeIdentifier> arrayList = new ArrayList<MimeIdentifier>(mime2comments.values());
        Collections.sort(arrayList);
        return arrayList;
    }

    public List<ExtensionIdentifier> getExtensionIdentifiers() {
        ArrayList<ExtensionIdentifier> arrayList = new ArrayList<ExtensionIdentifier>(ext2comments.values());
        Collections.sort(arrayList);
        return arrayList;
    }

    public void setIdentifiers(List<MimeIdentifier> mimeIdentifiers, List<ExtensionIdentifier> extensionIdentifiers) {
        mime2comments.clear();
        for (MimeIdentifier mimeIdentifier : mimeIdentifiers) {
            mime2comments.put(mimeIdentifier.getId(), mimeIdentifier);
        }
        getPreferences().put(MIME_IDENTIFIERS, encodeMimeIdentifiers(mimeIdentifiers)); //NOI18N

        ext2comments.clear();
        for (ExtensionIdentifier extensionIdentifier : extensionIdentifiers) {
            ext2comments.put(extensionIdentifier.getId(), extensionIdentifier);
        }
        getPreferences().put(EXT_IDENTIFIERS, encodeExtIdentifiers(extensionIdentifiers)); //NOI18N
        if (null == propertySupport) {
            propertySupport = new PropertyChangeSupport(this);
        }
        propertySupport.firePropertyChange(PROP_IDENTIFIERS_LIST, null, getPatterns());
    }
    
    public boolean isExtensionSupported( String fileExtension ) {
        return null != ext2comments.get( fileExtension.toUpperCase() );
    }
    
    public boolean isMimeTypeSupported( String mimeType ) {
        return null != mime2comments.get( mimeType );
    }
  
     public String getLineComment(String fileExtension, String mime) {
        FileIdentifier identifier = mime2comments.get(mime);
        if (null == identifier) {
            identifier = ext2comments.get(fileExtension.toUpperCase());
        }
        return null == identifier ? null : identifier.getCommentTags().getLineComment();
    }

    public String getBlockCommentStart(String fileExtension, String mime) {
        FileIdentifier identifier = mime2comments.get(mime);
        if (null == identifier) {
            identifier = ext2comments.get(fileExtension.toUpperCase());
        }
        return null == identifier ? null : identifier.getCommentTags().getBlockCommentStart();
    }

    public String getBlockCommentEnd(String fileExtension, String mime) {
        FileIdentifier identifier = mime2comments.get(mime);
        if (null == identifier) {
            identifier = ext2comments.get(fileExtension.toUpperCase());
        }
        return null == identifier ? null : identifier.getCommentTags().getBlockCommentEnd();
    }
    
    public boolean isScanCommentsOnly() {
        return scanCommentsOnly;
    }
    
    public void setScanCommentsOnly( boolean val ) {
        boolean oldVal = scanCommentsOnly;
        this.scanCommentsOnly = val;
        getPreferences().putBoolean( "scanCommentsOnly", val ); //NOI18N
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.firePropertyChange( PROP_SCAN_COMMENTS_ONLY, oldVal, val );
    }
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.addPropertyChangeListener( l );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        if( null != propertySupport )
            propertySupport.removePropertyChangeListener( l );
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( Settings.class );
    }
    
    private static Collection<String> decodePatterns( String encodedPatterns ) {
        StringTokenizer st = new StringTokenizer( encodedPatterns, OBJECT_DELIMITER, false );
        
        Collection<String> patterns = new ArrayList<String>();
        
        while( st.hasMoreTokens() ) {
            String im = st.nextToken();
            patterns.add(im);
        }
        
        return patterns;
    }
    
    private static String encodePatterns( Collection<String> patterns ) {
        StringBuilder sb = new StringBuilder();
        
        for( String p : patterns ) {
            sb.append( p );
            sb.append( OBJECT_DELIMITER );
        }
        
        return sb.toString();
    }

    private static Map<String, MimeIdentifier> decodeMimeIdentifiers(String encodedIdentifiers) {
        StringTokenizer st = new StringTokenizer(encodedIdentifiers, OBJECT_DELIMITER, false);

        Map<String, MimeIdentifier> mimeIdentifiers = new HashMap<String, MimeIdentifier>(st.countTokens());
        while (st.hasMoreTokens()) {
            String im = st.nextToken();
            String[] fields = im.split(FIELD_DELIMITER, 5);
            MimeIdentifier mimeIdentifier = new MimeIdentifier(fields[0], fields[1], new CommentTags(fields[2], fields[3], fields[4]));
            mimeIdentifiers.put(mimeIdentifier.getId(), mimeIdentifier);
        }
        return mimeIdentifiers;
    }

    private static String encodeMimeIdentifiers(Collection<MimeIdentifier> identifiers) {
        StringBuilder sb = new StringBuilder();

        for (MimeIdentifier identifier : identifiers) {
            sb.append(identifier.getMimeType());
            sb.append(FIELD_DELIMITER);
            sb.append(identifier.getMimeName());
            sb.append(FIELD_DELIMITER);
            sb.append(encodeCommentTags(identifier.getCommentTags()));
            sb.append(OBJECT_DELIMITER);
        }
        return sb.toString();
    }

    private static Map<String, ExtensionIdentifier> decodeExtIdentifiers(String encodedIdentifiers) {
        StringTokenizer st = new StringTokenizer(encodedIdentifiers, OBJECT_DELIMITER, false);

        Map<String, ExtensionIdentifier> extensionIdentifiers = new HashMap<String, ExtensionIdentifier>(st.countTokens());
        while (st.hasMoreTokens()) {
            String im = st.nextToken();
            String[] fields = im.split(FIELD_DELIMITER, 4);
            ExtensionIdentifier extensionIdentifier = new ExtensionIdentifier(fields[0], new CommentTags(fields[1], fields[2], fields[3]));
            extensionIdentifiers.put(extensionIdentifier.getId(), extensionIdentifier);
        }
        return extensionIdentifiers;
    }

    private static String encodeExtIdentifiers(Collection<ExtensionIdentifier> identifiers) {
        StringBuilder sb = new StringBuilder();

        for (ExtensionIdentifier identifier : identifiers) {
            sb.append(identifier.getExtension());
            sb.append(FIELD_DELIMITER);
            sb.append(encodeCommentTags(identifier.getCommentTags()));
            sb.append(OBJECT_DELIMITER);
        }
        return sb.toString();
    }

    private static String encodeCommentTags(CommentTags tags) {
        StringBuilder sb = new StringBuilder();
        sb.append(tags.isLineCommentEnabled() ? tags.getLineComment() : "");
        sb.append(FIELD_DELIMITER);
        sb.append(tags.isBlockCommentEnabled() ? tags.getBlockCommentStart() : "");
        sb.append(FIELD_DELIMITER);
        sb.append(tags.isBlockCommentEnabled() ? tags.getBlockCommentEnd() : "");
        return sb.toString();
    }
}
