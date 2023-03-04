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
package org.openide.text;

import org.openide.util.RequestProcessor;

import java.io.*;

import java.util.*;


/** Class that holds line information about one document.
* Defines operations that can be executed on the objects, the implementation
* can change when we find that it is too slow.
*
* @author Jaroslav Tulach
*/
final class LineStruct extends Object {
    /** max number of lines to work with */
    private static final int MAX = Integer.MAX_VALUE / 2;

    /** processor for all requests */
    private static final RequestProcessor PROCESSOR = new RequestProcessor("LineStruct Processor", // NOI18N
            1, false, false);

    /** list of Info objects that represents the whole document */
    private List<Info> list;

    /** Constructor.
    */
    public LineStruct() {
        list = new LinkedList<Info>();
        list.add(new Info(MAX, MAX));
    }

    /** Converts original numbering to the new one.
    * @param line the line number in the original
    * @return line number in the new numbering
    */
    public int convert(int line, final boolean currentToOriginal) {
        // class to compute in the request processor thread
        class Compute extends Object implements Runnable {
            public int result;

            public Compute(int i) {
                result = i;
            }

            public void run() {
                if (currentToOriginal) {
                    result = originalToCurrentImpl(result);
                } else {
                    result = currentToOriginalImpl(result);
                }
            }
        }

        Compute c = new Compute(line);

        // post the computation and wait till it is finished
        PROCESSOR.post(c).waitFinished();

        // return result
        return c.result;
    }

    /** Inserts line(s) at given position.
    * @param line the line number in current numbering
    * @param count number of lines inserted
    */
    public void insertLines(final int line, final int count) {
        PROCESSOR.post(
            new Runnable() {
                public void run() {
                    insertLinesImpl(line, count);
                }
            }
        );
    }

    /** Method that deletes some lines in the current state of
    * the document.
    *
    * @param line the line number in current numbering
    * @param
    */
    public void deleteLines(final int line, final int count) {
        PROCESSOR.post(
            new Runnable() {
                public void run() {
                    deleteLinesImpl(line, count);
                }
            }
        );
    }

    /** Converts original numbering to the new one.
    * @param line the line number in the original
    * @return line number in the new numbering
    */
    private int originalToCurrentImpl(int line) {
        Iterator<Info> it = list.iterator();
        int cur = 0;

        for (;;) {
            Info i = it.next();

            if (i.original > line) {
                // ok we found the segment that contained this line
                return (line > i.current) ? (cur + i.current) : (cur + line);
            }

            cur += i.current;
            line -= i.original;
        }
    }

    /** Converts the current numbering to original
    * @param line the line number now
    * @return line number in the original numbering
    */
    private int currentToOriginalImpl(int line) {
        Iterator<Info> it = list.iterator();
        int cur = 0;

        for (;;) {
            Info i = it.next();

            if (i.current > line) {
                // ok we found the segment that contained this line
                return (line > i.original) ? (cur + i.original) : (cur + line);
            }

            cur += i.original;
            line -= i.current;
        }
    }

    /** Inserts line(s) at given position.
    * @param line the line number in current numbering
    * @param count number of lines inserted
    */
    private void insertLinesImpl(int line, int count) {
        ListIterator<Info> it = list.listIterator();

        for (;;) {
            Info i = it.next();

            if (i.current >= line) {
                for (;;) {
                    count = i.insert(line, count, it);

                    if (count == 0) {
                        return;
                    }

                    i = it.next();
                    line = 0;
                }
            }

            line -= i.current;
        }
    }

    /** Method that deletes some lines in the current state of
    * the document.
    *
    * @param line the line number in current numbering
    * @param
    */
    private void deleteLinesImpl(int line, int count) {
        ListIterator<Info> it = list.listIterator();

        for (;;) {
            Info i = it.next();

            if (i.current >= line) {
                // information to hold both the number of lines to delete (original)
                // and the number of lines to mark as delete at the end (current)
                Info stat = new Info(count, 0);

                for (;;) {
                    stat = i.delete(line, stat, it);

                    if (stat.original == 0) {
                        break;
                    }

                    i = it.next();
                    line = 0;
                }

                // insert the amount of lines to mark deleted before current position
                if ((stat.current > 0) && it.hasPrevious()) {
                    Info prev = it.previous();
                    boolean hasPrev = it.hasPrevious();

                    if (hasPrev) {
                        prev = it.previous();
                    }

                    if (prev.current == 0) {
                        prev.original += stat.current;
                    } else {
                        if (hasPrev) {
                            it.next();
                        }

                        it.add(new Info(stat.current, 0));
                    }
                }

                return;
            }

            line -= i.current;
        }
    }

    /** Holding the original and current number of lines.
    */
    private static final class Info extends Object {
        /** constants for distintion of the type of info */
        public static final int AREA_ORIGINAL = 0;
        public static final int AREA_INSERT = 1;
        public static final int AREA_REMOVE = -1;

        /** original number */
        public int original;

        /** current number */
        public int current;

        public Info(int o, int c) {
            original = o;
            current = c;
        }

        /** Finds the type.
        */
        public int type() {
            if (current == original) {
                return AREA_ORIGINAL;
            }

            if (current == 0) {
                return AREA_REMOVE;
            }

            if (original == 0) {
                return AREA_INSERT;
            }

            throw new IllegalStateException("Original: " + original + " current: " + current); // NOI18N
        }

        /** Performs insert on this Info object.
        * @param pos position to insert to
        * @param count how much objects to insert
        * @param it iterator that just returned this object
        * @return how much lines to insert after this object
        */
        public int insert(int pos, int count, ListIterator<Info> it) {
            switch (type()) {
            case AREA_INSERT:

                // insert area, add to it all
                current += count;

                return 0;

            case AREA_ORIGINAL:

                if (pos == current) {
                    // if the insert position is at the end,
                    // then let all the characters be added by next
                    // item
                    return count;
                }

                if (pos == 0) {
                    // prepend the insert area before the current
                    // Info in the chain
                    Info ni = new Info(original, original);
                    original = 0;
                    current = count;
                    it.add(ni);

                    // everything has been prepended
                    return 0;
                }

                // we have to devided the interval to two parts
                // and insert insert block between them
                Info ni = new Info(original - pos, original - pos);

                // the area from 0 to pos
                original = current = pos;

                // insert the insert area
                it.add(new Info(0, count));

                // the rest of the area
                it.add(ni);

                return 0;

            case AREA_REMOVE:

                // supposing that pos == 0
                if (pos != 0) {
                    throw new IllegalStateException("Pos: " + pos); // NOI18N
                }

                // check the previous Info if it cannot be merged
                Info prev = it.previous(); // current item

                if (it.hasPrevious()) {
                    prev = it.previous(); // previous
                    it.next(); // previous
                }

                it.next(); // current

                if (count < original) {
                    if (prev.type() == AREA_ORIGINAL) {
                        prev.original += count;
                        prev.current += count;

                        // modify this remove object
                        original -= count;
                    } else {
                        ni = new Info(original - count, 0);

                        // turn this to regular part
                        original = current = count;

                        // insert the new delete part
                        it.add(ni);
                    }

                    // everything processed
                    return 0;
                } else {
                    if (prev.type() == AREA_ORIGINAL) {
                        prev.current += original;
                        prev.original += original;
                        it.remove();

                        return count - original;
                    } else {
                        // turn whole delete part to regular one
                        current = original;

                        // the rest of characters to proceed
                        return count - current;
                    }
                }

            default:
                throw new IllegalStateException("Type: " + type()); // NOI18N
            }
        }

        /** A method that handles the delete operation.
        * @param pos position in the Info block where delete started
        * @param info
        *   info.original the amount of lines to be deleted
        *   info.current the amount of lines that should be later marked as deleted
        * @param it the iterator that previously returned this instance
        * @return
        *   info.original the amount of lines to be yet deleted
        *   info.current the amount of lines that needs to be later marked as deleted
        *     this will be put before the
        */
        public Info delete(int pos, Info info, ListIterator<Info> it) {
            switch (type()) {
            case AREA_ORIGINAL:

                if (pos != 0) {
                    // specials
                    int size = current - pos;
                    current = original = pos;

                    if (size >= info.original) {
                        // delete is whole only in this block
                        Info ni = new Info(size, size);
                        it.add(ni);
                        info.current += info.original;
                        info.original = 0;

                        return info;
                    } else {
                        // something is resting after this block
                        info.original -= size;
                        info.current += size;

                        return info;
                    }
                } else {
                    // deleting from first position
                    if (current >= info.original) {
                        // something is resting from me (at the end)
                        // number of lines to mark as deleted
                        info.current += info.original;

                        // number of lines in this block is decreased
                        current -= info.original;
                        original = current;

                        // number of lines to be yet deleted
                        info.original = 0;

                        return info;
                    } else {
                        // I am completelly deleted
                        it.remove();

                        // number of lines to mark as deleted
                        info.current += current;
                        info.original -= current;

                        return info;
                    }
                }

            case AREA_INSERT:

                if (pos != 0) {
                    // specials
                    int size = current - pos;

                    if (size >= info.original) {
                        // delete is whole only in this block
                        current -= info.original;

                        info.original = 0;

                        return info;
                    } else {
                        // something is resting after this block
                        current = pos;

                        info.original -= size;

                        return info;
                    }
                } else {
                    // deleting from first position
                    if (current >= info.original) {
                        // something is resting from me (at the end)
                        // number of lines in this block is decreased
                        current -= info.original;

                        // number of lines to be yet deleted
                        info.original = 0;

                        it.remove();

                        return info;
                    } else {
                        // I am completelly deleted
                        it.remove();

                        // how much lines to be deleted yet
                        info.original -= current;

                        return info;
                    }
                }

            case AREA_REMOVE:

                // only derease the number of lines that needs to be deleted
                // because this area can absorb some
                original += info.current;
                info.current = 0;

                return info;

            default:
                throw new IllegalStateException("Type: " + type()); // NOI18N
            }
        }
    }
}
