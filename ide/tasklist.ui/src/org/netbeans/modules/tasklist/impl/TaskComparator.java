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

package org.netbeans.modules.tasklist.impl;

import java.net.URL;
import java.util.Comparator;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;

/**
 * @author S. Aubrecht
 */
public class TaskComparator {
    
    private static Comparator<Task> DEFAULT_COMPARATOR;
    
    /** Creates a new instance of DefaultTaskComparator */
    private TaskComparator() {
    }
    
    public static Comparator<Task> getDefault() {
        if( null == DEFAULT_COMPARATOR ) {
            DEFAULT_COMPARATOR = new Comparator<Task>() {
                public int compare( Task t1, Task t2 ) {
                    int result = 0;
                    //compare groups
                    result = Accessor.getGroup( t1 ).compareTo( Accessor.getGroup( t2 ) );

                    //compare file
                    if( 0 == result ) {
                        URL u1 = Accessor.getURL(t1);
                        URL u2 = Accessor.getURL(t2);
                        if( null == u1 && null != u2 ) {
                            result = -1;
                        } else if( null != u1 && null == u2 ) {
                            result = 1;
                        } else if( null != u1 && null != u2 ) {
                            result = u1.toString().compareTo( u2.toString() );
                        } else {
                            FileObject f1 = Accessor.getFile(t1);
                            FileObject f2 = Accessor.getFile(t2);
                            result = f1.getPath().compareTo( f2.getPath() );
                        }
                    }

                    //compare line number
                    if( 0 == result ) {
                        if( Accessor.getLine(t1) <= 0 && Accessor.getLine(t2) > 0 )
                            result = -1;
                        else if( Accessor.getLine(t1) > 0 && Accessor.getLine(t2) <= 0 )
                            result = 1;
                        else if( Accessor.getLine(t1) > 0 && Accessor.getLine(t2) > 0 )
                            result = Accessor.getLine(t1)- Accessor.getLine(t2);
                    }

                    //compare description
                    if( 0 == result ) {
                        result = Accessor.getDescription(t1).compareTo( Accessor.getDescription(t2) );
                    }
                    return result;
                }
            };
        }
        return DEFAULT_COMPARATOR;
    }
    
    public static Comparator<Task> getDescriptionComparator( boolean asc ) {
        return new DescriptionComparator( asc );
    }
    
    public static Comparator<Task> getLocationComparator( boolean asc ) {
        return new LocationComparator( asc );
    }
    
    public static Comparator<Task> getFileComparator( boolean asc ) {
        return new FileComparator( asc );
    }
    
    private static class DescriptionComparator implements Comparator<Task> {
        private boolean asc;
        public DescriptionComparator( boolean asc ) {
            this.asc = asc;
        }

        public int compare( Task t1, Task t2 ) {
            int result = Accessor.getDescription(t1).compareTo( Accessor.getDescription(t2) );
            if( 0 == result )
                result = getDefault().compare( t1, t2 );
            else if( !asc )
                result *= -1;
            return result;
        }
    
        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final DescriptionComparator test = (DescriptionComparator) o;

            if (this.asc != test.asc)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 69 * hash + (this.asc ? 1 : 0);
            return hash;
        }
    }
    
    private static class FileComparator implements Comparator<Task> {
        private boolean asc;
        public FileComparator( boolean asc ) {
            this.asc = asc;
        }

        public int compare( Task t1, Task t2 ) {
            int result = 0;
            
            String f1 = Accessor.getFileNameExt(t1);
            String f2 = Accessor.getFileNameExt(t2);
            if( null == f1 && null != f2 )
                result = -1;
            else if( null != f1 && null == f2 ) 
                result = 1;
            else if( null != f1 && null != f2 ) {
                result = f1.compareTo( f2 );
            }
            
            if( 0 == result )
                result = getDefault().compare( t1, t2 );
            else if( !asc )
                result *= -1;
            
            return result;
        }
    
        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final FileComparator test = (FileComparator) o;

            if (this.asc != test.asc)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.asc ? 1 : 0);
            return hash;
        }
    }
    
    private static class LocationComparator implements Comparator<Task> {
        private boolean asc;
        public LocationComparator( boolean asc ) {
            this.asc = asc;
        }

        @Override
        public int compare( Task t1, Task t2 ) {
            int result = 0;
            
            String f1 = Accessor.getPath(t1);
            String f2 = Accessor.getPath(t2);
            if( null == f1 && null != f2 )
                result = -1;
            else if( null != f1 && null == f2 ) 
                result = 1;
            else if( null != f1 && null != f2 ) {
                result = f1.compareTo( f2 );
            }

            if( 0 == result )
                result = Accessor.getLine(t1) - Accessor.getLine(t2);
            
            if( 0 == result )
                result = getDefault().compare( t1, t2 );
            else if( !asc )
                result *= -1;
            
            return result;
        }
    
        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final LocationComparator test = (LocationComparator) o;

            if (this.asc != test.asc)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 99 * hash + (this.asc ? 1 : 0);
            return hash;
        }
    }
}
