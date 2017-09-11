package test;

import java.util.Enumeration;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.AbstractElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

/**
 *
 * @author sdedic
 */
class MethodCountAbstract extends AbstractElement {

    public MethodCountAbstract(AbstractDocument ad, Element parent, AttributeSet a) {
        ad.super(parent, a);
    }
    public void a1() {}
    public void a2() {}
    public void a3() {}
    public void a4() {}
    public void a5() {}
    public void a6() {}
    public void a7() {}
    public void a8() {}
    public void a9() {}
    public void a10() {}
    public void a11() {}
    public void a12() {}
    public void a13() {}
    
    @Override
    public int getStartOffset() {
        return 0;
    }

    @Override
    public int getEndOffset() {
        return 0;
    }

    @Override
    public Element getElement(int index) {
        return null;
    }

    @Override
    public int getElementCount() {
        return 0;
    }

    @Override
    public int getElementIndex(int offset) {
        return 0;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public Enumeration children() {
        return null;
    }
}
