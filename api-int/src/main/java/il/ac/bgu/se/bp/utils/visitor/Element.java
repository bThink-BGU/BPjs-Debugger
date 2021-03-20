package il.ac.bgu.se.bp.utils.visitor;

public interface Element<T> {
    T accept(Visitor visitor);
}
