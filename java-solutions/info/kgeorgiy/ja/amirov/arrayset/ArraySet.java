package info.kgeorgiy.ja.amirov.arrayset;

import java.util.*;

public class ArraySet<T extends Comparable<T>> extends AbstractSet<T> implements NavigableSet<T> {

    private final ReverseList<T> rl;
    private final Comparator<? super T> cmp;

    public ArraySet() {
        this(new ReverseList<>(), null);
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }

    public ArraySet(Comparator<? super T> cmp) {
        this(new ReverseList<>(), cmp);
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> cmp) {
        TreeSet<T> ts = new TreeSet<>(cmp);
        ts.addAll(Objects.requireNonNull(collection));
        this.rl = new ReverseList<>(ts);
        this.cmp = cmp;
    }

    private ArraySet(ReverseList<T> rlOld, Comparator<? super T> cmp) {
        this.rl = rlOld;
        this.cmp = cmp;
    }

    private T get(int index) {
        if (0 <= index && index < size()) {
            return rl.get(index);
        }
        return null;
    }

    private T checkedGet(int index) throws NoSuchElementException {
        T result = get(index);
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    private int compare(T a, T b) {
        if (this.cmp != null) {
            return this.cmp.compare(a, b);
        }
        return (a).compareTo(b);
    }

    private int lowerIndexBound(T element, boolean lower, boolean inclusive) {
        int index = Collections.binarySearch(this.rl, element, cmp);
        int result;
        if (index >= 0) {
            result = index;
            if (!inclusive) {
                result += (lower ? -1 : 1);
            }
        } else {
            result = -index - 1 + (lower ? -1 : 0);
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(this.rl, (T) Objects.requireNonNull(o), cmp) >= 0;
    }

    @Override
    public T lower(T t) {
        return get(lowerIndexBound(t, true, false));
    }

    @Override
    public T floor(T t) {
        return get(lowerIndexBound(t, true, true));
    }

    @Override
    public T ceiling(T t) {
        return get(lowerIndexBound(t, false, true));
    }

    @Override
    public T higher(T t) {
        return get(lowerIndexBound(t, false, false));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return this.rl.iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReverseList<>(this.rl, true), Collections.reverseOrder(this.cmp));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) throws IllegalArgumentException {
        if(rl.isReversed == compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return subSetUnchecked(fromElement, fromInclusive, toElement, toInclusive);
    }


    private NavigableSet<T> subSetUnchecked(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int left = lowerIndexBound(fromElement, false, fromInclusive);
        int right = lowerIndexBound(toElement, true, toInclusive);

//        if (left == -1 || right == -1) {
//            return new ArraySet<>(new ReverseList<T>(), this.cmp);
//        }
//        if (left > right) {
//            right++;
//        }

        return new ArraySet<>(this.rl.subList(left, right + 1), this.cmp);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (this.rl.isEmpty()) {
            return new ArraySet<>(new ReverseList<T>(), this.cmp);
        }
        if(compare(first(), toElement) > 0) {
            return subSet(first(), true, toElement, inclusive);
        } else {
            return subSet(toElement, true, first(), inclusive);
        }
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (this.rl.isEmpty()) {
            return new ArraySet<>(new ReverseList<T>(), this.cmp);
        }

        if(compare(last(), fromElement) < 0) {
            return subSetUnchecked(fromElement, inclusive, last(), true);
        } else {
            return subSetUnchecked(last(), inclusive, fromElement, true);
        }
    }

    @Override
    public Comparator<? super T> comparator() {
        return this.cmp;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        return checkedGet(0);
    }

    @Override
    public T last() {
        return checkedGet(size() - 1);
    }
    @Override
    public int size() {
        return this.rl.size();
    }
}
