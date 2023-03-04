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
package org.netbeans.core.output2;

import java.awt.Color;
import org.openide.windows.OutputListener;

import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.util.Collection;
import org.openide.util.Pair;
import org.openide.windows.IOColors;

/**
 * An interface representing the data written to an OutWriter, in terms of lines of text, with
 * methods for handling line wrapping.
 */
public interface Lines {
    /**
     * Get an array of all line numbers which have associated OutputListeners
     * @return an array of line numbers
     */
    int[] getLinesWithListeners();

    /**
     * Get the length, in characters, of a given line index
     *
     * @param idx the line number
     * @return the length
     */
    int length (int idx);

    int lengthWithTabs (int idx);

    /**
     * Get the character position corresponding to the start of a line
     *
     * @param line A line number
     * @return The character in the total text at which this line starts
     */
    int getLineStart (int line);

    /**
     * Get the line index of the nearest line start to this character position in the
     * entire stored text
     * @param position
     * @return The line on which this character position occurs
     */
    int getLineAt (int position);

    /**
     * Get the number of lines in the stored text
     * @return A line count
     */
    int getLineCount();

    /**
     * Get the output listeners associated with this line
     * @param line A line number
     * @return Collection of OutputListeners associated with this line
     */
    Collection<OutputListener> getListenersForLine(int line);

    /**
     * Get informations for line
     * @param line A line number
     * @return LineInfo for specified line
     */
    LineInfo getLineInfo(int line);

    /**
     * Sets default colors
     * @param type line type
     * @param color color
     */
    void setDefColor(IOColors.OutputType type, Color color);

    /**
     * Gets default color
     * @param type output type
     * @return corresponding Color
     */
    Color getDefColor(IOColors.OutputType type);

    /**
     * Get the index of the first line which has a listener
     * @return A line number, or -1 if there are no listeners
     */
    int firstListenerLine ();
    
    /**
     * Get the index of the first line which has an important listener
     * @return A line number, or -1 if there are no important listeners
     */
    int firstImportantListenerLine();

    /**
     * Check whether specified line contains important listener
     * @param line line to be checked
     * @return true if this line contains important listener
     */
    boolean isImportantLine(int line);

    /**
     *
     * @param startSearchPos starting position for searching
     * @param backward direction of searching
     * @param range [startPos, endPosition] of listener
     * @return nearest listener to specified startSearchPosition
     */
    OutputListener nearestListener(int startSearchPos, boolean backward, int[] range);

    /**
     * Get the length of the longest line in the storage
     * @return The longest line's length
     */
    int getLongestLineLength();

    /**
     * Get the count of logical (wrapped) lines above the passed index.  The passed index should be a line
     * index in a physical coordinate space in which lines are wrapped at charCount.  It will return the
     * number of logical (wrapped) lines above this line.
     *
     * @param logicalLine A line index in wrapped, physical space
     * @param charCount The number of characters at which line wrapping happens
     * @return The number of  logical lines above this one.
     */
    int getLogicalLineCountAbove (int logicalLine, int charCount);

    /**
     * Get the total number of logical lines required to display the stored text if wrapped at the specified
     * character count.
     * @param charCount The number of characters at which line wrapping happens
     * @return The number of logical lines needed to fit all of the text
     */
    int getLogicalLineCountIfWrappedAt (int charCount);

    /**
     * Get number of physical characters for the given logical (expanded TABs) length.
     * @param offset
     * @param logicalLength
     * @param tabShiftPtr
     * @return
     */
    public int getNumPhysicalChars(int offset, int logicalLength, int[] tabShiftPtr);

    public int getNumLogicalChars(int offset, int physicalLength);
    
    /**
     * Determine if a character position indicates the first character of a line.
     *
     * @param chpos A character index in the stored text
     * @return Whether or not it's the first character of a line
     */
    boolean isLineStart (int chpos);

    /**
     * Get a line of text
     *
     * @param idx A line number
     * @return The text
     * @throws IOException If an error occurs and the text cannot be read
     */
    String getLine (int idx) throws IOException;

    /**
     * Determine if there are any lines with associated output listeners
     * @return True if there are any listeners
     */
    boolean hasListeners();

    /**
     * Get listener on specified position
     * @param pos position where look for listener
     * @param range if hyperlink exists on specified position [start position,
     * end position] is returned in this array (may be null)
     * @return listener (hyperlink) on specified position or null
     */
     OutputListener getListener(int pos, int[] range);

     /**
      * Check if listener exists on specified position
      * @param start start position of listener
      * @param end end position of listener
      * @return true if listener exists on specified position
      */
     boolean isListener(int start, int end);

    /**
     * Count the total number of characters in the stored text
     *
     * @return The number of characters that have been written
     */
    int getCharCount();

    /**
     * Fetch a getText of the entire text
     * @param start A character position < end
     * @param end A character position > start
     * @return A String representation of the text between these points, including newlines
     */
    String getText (int start, int end);

    /**
     * Fetch a getText of the entire text into a character array
     * @param start A character position < end
     * @param end A character position >  start
     * @param chars A character array at least as large as end-start, or null
     * @return A character array containing the range of text specified
     */
    char[] getText (int start, int end, char[] chars);

    /**
      * Get a physical (real) line index for logical (wrapped) line index.
      * This is to accomodate line wrapping using fixed width fonts. This
      * method answers the question "Which physical (real) line corresponds
      * to certain logical (wrapped) line if we wrap at <code>charsPerLine</code>.
      * It will also return number of wrapped lines for found physical line and
      * the position (index) on this wrapped line.
      *
      * @param info A 3 entry array.  Element 0 should be the logical (wrapped)
      *        line when called;
      *        the other two elements are ignored.  On return,
      *        it contains: <ul>
      *         <li>[0] The physical (real) line index for the passed line</li>
      *         <li>[1] Index of logical line within this physical line</li>
      *         <li>[2] The total number of line wraps for the physical line</li>
      *         </ul>
      */
    void toPhysicalLineIndex (int[] info, int charsPerLine);

    /**
     * Save the contents of the buffer to a file, in platform default encoding.
     *
     * @param path The file to save to
     * @throws IOException If there is a problem writing or encoding the data, or if overwrite is false and the
     *    specified file exists
     */
    void saveAs(String path) throws IOException;

    /**
     *
     * @param start start position for search
     * @param pattern pattern to search
     * @param regExp pattern is regexp (false to escape meta chars)
     * @param matchCase true to match case
     * @return [start, end] position of match
     */
    public int[] find(int start, String pattern, boolean regExp, boolean matchCase);

    /**
     *
     * @param start start position for reverse search
     * @param pattern pattern to search
     * @param regExp pattern is regexp (false to escape meta chars)
     * @param matchCase true to match case
     * @return [start, end] position of match
     */
    public int[] rfind(int start, String pattern, boolean regExp, boolean matchCase);

    /**
     * Acquire a read lock - while held, other threads cannot modify this Lines object.
     *
     * @return
     */
    Object readLock();

    /**
     * Add a change listener, which will detect when lines are written. <strong>Changes are
     * <u>not</u> fired for every write; they should be fired when an initial line is written,
     * when the writer is flushed, or when it is closed.</strong>  Clients which respond to ongoing
     * writes should use a timer and poll via <code>checkDirty()</code> to see if new data has
     * been written.
     *
     * @param cl A change listener
     */
    void addChangeListener (ChangeListener cl);

    /**
     * Remove a change listener.
     *
     * @param cl
     */
    void removeChangeListener (ChangeListener cl);

    /**
     * Allows clients that wish to poll to see if there is new output to do
     * so.  When any thread writes to the output, the dirty flag is set.
     * Calling this method returns its current value and clears it.  If it
     * returns true, a view of the data may need to repaint itself or something
     * such.  This mechanism can be used in preference to listener based
     * notification, by running a timer to poll as long as the output is
     * open, for cases where otherwise the event queue would be flooded with
     * notifications for small writes.
     *
     * @param clear Whether or not to clear the dirty flag
     */
    boolean checkDirty(boolean clear);

    /**
     * Determine whether or not the storage backing this Lines object is being actively written to.
     * @return True if there is still an open stream which may write to the backing storage and no error has occured
     */
    boolean isGrowing();

    /**
     * Show lines in fold that starts at {@code foldStartIndex}.
     */
    void showFold(int foldStartIndex);

    /**
     * Hide lines in fold that starts at {@code foldStartIndex}.
     */
    void hideFold(int foldStartIndex);

    /**
     * Show all parent folds of the fold starting at line
     * {@code foldStartIndex}.
     *
     * @param foldStartIndex Real index of the line where the fold starts.
     */
    void showFoldAndParentFolds(int foldStartIndex);

    /**
     * Expand all parent folds of a line, so that the line is visible.
     *
     * @param realLineIndex Real line index of the line to show.
     */
    void showFoldsForLine(int realLineIndex);

    /**
     * Show all folds in the output, including nested folds.
     */
    void showAllFolds();

    /**
     * Hide all folds in the output, including nested folds.
     */
    void hideAllFolds();

    /**
     * Show fold and all its nested folds.
     *
     * @param foldStartIndex Real index of the line at which the fold starts.
     */
    void showFoldTree(int foldStartIndex);

    /**
     * Hide fold and all its nested folds.
     *
     * @param foldStartIndex Real index of the line at which the fold starts.
     */
    void hideFoldTree(int foldStartIndex);

    /**
     * Check whether a line is visible, e.i. it's not inside a collapsed folds.
     *
     * @param realLineIndex Index of the line, including hidden lines.
     */
    boolean isVisible(int realLineIndex);

    int getVisibleLineCount();

//    /**
//     * Test whether a line is visible, e.g. it is not inside a hidden fold.
//     */
//    boolean isVisible(int lineIndex);
//
//    /**
//     * Get count of visible lines.
//     */
//    int visibleLineCount();
//
    /**
     * Convert real line index to visible line index.
     *
     * @return Visible line index, or -1 if the line is invisible.
     */
    int realToVisibleLine(int realLineIndex);

    /**
     * Convert visible line index to a real line index.
     */
    int visibleToRealLine(int visibleLineIndex);

    /**
     * Get index of the line at which fold containing line {@code realLineIndex}
     * starts. If the line itself is starting line of a fold, or the line is
     * outside of any fold, {@code realLineIndex} is returnded.
     */
    int getFoldStart(int realLineIndex);

    /**
     * Get index of the line at which fold containing line {@code realLineIndex}
     * starts. If the line is not inside a fold, value -1 is returned.
     */
    int getParentFoldStart(int realLineIndex);

    /**
     * Remove characters from the end of the last unfinished line.
     *
     * @param length Number of characters to remove, -1 to remove all characters
     * in the last line.
     * @return Pair, where the first items is number of removed characters (can
     * be different from parameter {@code length}), and the second item is
     * number of removed tab spaces.
     */
    public Pair<Integer, Integer> removeCharsFromLastLine(int length);
}
