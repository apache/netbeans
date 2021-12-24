package org.netbeans.api.java.source.ui.snippet;


import java.util.List;

public class SourceLineMeta {

    String actualSourceLine;
    List<MarkupTag> thisLineMarkUpTags;
    String sourceLineWithoutComment;

    public String getActualSourceLine() {
        return actualSourceLine;
    }

    public void setActualSourceLine(String actualSourceLine) {
        this.actualSourceLine = actualSourceLine;
    }

    public List<MarkupTag> getThisLineMarkUpTags() {
        return thisLineMarkUpTags;
    }

    public void setThisLineMarkUpTags(List<MarkupTag> thisLineMarkUpTags) {
        this.thisLineMarkUpTags = thisLineMarkUpTags;
    }

    public String getUncommentSourceLine() {
        return sourceLineWithoutComment;
    }

    public void setSourceLineWithoutComment(String sourceLineWithoutComment) {
        this.sourceLineWithoutComment = sourceLineWithoutComment;
    }

}
