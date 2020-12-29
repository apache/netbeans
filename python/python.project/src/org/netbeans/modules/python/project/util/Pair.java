/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.util;

public final class Pair<P,K> {

    public final P first;
    public final K second;

    private Pair (P first, K second) {
        this.first = first;
        this.second = second;
    }


    public static <P,K> Pair<P,K> of (P first, K second) {
        return new Pair<> (first,second);
    }


    @Override
    public int hashCode () {
        int hashCode  = 0;
        hashCode ^= first == null ? 0 : first.hashCode();
        hashCode ^= second == null ? 0: second.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals (final Object other) {
        if (other instanceof Pair) {
            Pair otherPair = (Pair) other;
            return (this.first == null ? otherPair.first == null : this.first.equals(otherPair.first)) &&
                   (this.second == null ? otherPair.second == null : this.second.equals(otherPair.second));
        }
        return false;
    }

    @Override
    public String toString () {
        return String.format("Pair[%s,%s]", first,second);
    }
}
