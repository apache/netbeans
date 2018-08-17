
package issue239883;

public class FormBean<T> {

    protected T selected;
    protected Class<T> e;

    protected FormBean(Class<T> e) {
        this.e = e;
    }

    public T getSelected() {
        return selected;
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }

}
