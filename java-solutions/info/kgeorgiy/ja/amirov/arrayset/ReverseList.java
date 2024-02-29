package info.kgeorgiy.ja.amirov.arrayset;

import java.util.*;
import static java.util.Collections.emptyList;

public class ReverseList<T> extends AbstractList<T> {

    private final List<T> list;
    private boolean isReversed = false;

    public ReverseList(Collection<T> collection) {
        this.list = List.copyOf(collection);
    }

    public ReverseList(ReverseList<T> ReverseList, boolean isReversed) {
        this.list = ReverseList.list;
        this.isReversed = ReverseList.isReversed ^ isReversed;
    }

    public ReverseList() {
        this.list = emptyList();
    }

    private int reversedIndex(int index) {
        return size() - index - 1;
    }

    @Override
    public ReverseList<T> subList(int from, int to) {
        if (isReversed) {
            return new ReverseList<>(list.subList(reversedIndex(to - 1), reversedIndex(to) + 1));
        } else {
            return new ReverseList<>(list.subList(from, to));
        }
    }

    @Override
    public T get(int index) {
        return list.get(isReversed ? reversedIndex(index) : index);
    }

    @Override
    public int size() {
        return list.size();
    }
}