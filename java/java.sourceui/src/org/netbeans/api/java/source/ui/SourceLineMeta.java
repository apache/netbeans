package org.netbeans.api.java.source.ui;


import java.util.List;

public class SourceLineMeta {

    String actualSourceLine;
    int actualSourceLineStartPos;
    List<MarkUpTag> thisLineMarkUpTags;
    String sourceLineWithoutComment;

    public String getActualSourceLine() {
        return actualSourceLine;
    }

    public void setActualSourceLine(String actualSourceLine) {
        this.actualSourceLine = actualSourceLine;
    }

    public int getActualSourceLineStartPos() {
        return actualSourceLineStartPos;
    }

    public void setActualSourceLineStartPos(int actualSourceLineStartPos) {
        this.actualSourceLineStartPos = actualSourceLineStartPos;
    }

    public List<MarkUpTag> getThisLineMarkUpTags() {
        return thisLineMarkUpTags;
    }

    public void setThisLineMarkUpTags(List<MarkUpTag> thisLineMarkUpTags) {
        this.thisLineMarkUpTags = thisLineMarkUpTags;
    }

    public String getUncommentSourceLine() {
        return sourceLineWithoutComment;
    }

    public void setSourceLineWithoutComment(String sourceLineWithoutComment) {
        this.sourceLineWithoutComment = sourceLineWithoutComment;
    }


}
