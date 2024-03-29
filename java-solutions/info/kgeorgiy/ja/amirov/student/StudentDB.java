package info.kgeorgiy.ja.amirov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class StudentDB implements GroupQuery{
    private final Comparator<Student> STUDENT_COMPARATOR = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparing(Student::getId, Comparator.reverseOrder());

    @Override
    public List<Group> getGroupsByName(final Collection<Student> students) {
        return getGroupsSortedBy(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(final Collection<Student> students) {
        return getGroupsSortedBy(students, this::sortStudentsById);
    }

    @Override
    public GroupName getLargestGroup(final Collection<Student> students) {
        return getBiggestGroupBy(students, List::size, Comparator.naturalOrder());
    }

    @Override
    public GroupName getLargestGroupFirstName(final Collection<Student> students) {
        return getBiggestGroupBy(students, s -> getDistinctFirstNames(s).size(), Comparator.reverseOrder());
    }

    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return transformToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(final List<Student> students) {
        return transformToList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(final List<Student> students) {
        return transformToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(final List<Student> students) {
        return transformToList(students, s -> s.getFirstName() + " " + s.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toSet());
    }

    @Override
    public String getMaxStudentFirstName(final List<Student> students) {
        return students
                .stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(final Collection<Student> students) {
        return sortBy(students, Comparator.comparing(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(final Collection<Student> students) {
        return sortBy(students, STUDENT_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(final Collection<Student> students, final String name) {
        return findBy(students, s -> s.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(final Collection<Student> students, final String name) {
        return findBy(students, s -> s.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(final Collection<Student> students, final GroupName group) {
        return findBy(students, s -> s.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }

    private List<Group> getGroupsSortedBy(final Collection<Student> students,
                                          final Function<List<Student>, List<Student>> mapper) {
        return students
                .stream()
                .collect(Collectors.groupingBy(Student::getGroup, Collectors.collectingAndThen(Collectors.toList(), mapper)))
                .entrySet()
                .stream()
                .map(entry -> new Group(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }


    private GroupName getBiggestGroupBy(
            final Collection<Student> students,
            final Function<List<Student>, Integer> mapper,
            final Comparator<GroupName> keyComparator) {
        return students
                .stream()
                .collect(Collectors.groupingBy(Student::getGroup)) // :NOTE: same
                .entrySet()
                .stream()
                .max(Comparator.comparing((Map.Entry<GroupName, List<Student>> entry) -> mapper.apply(entry.getValue()))
                        .thenComparing(Map.Entry::getKey, keyComparator)) // :NOTE: reuse comp
                .map(Map.Entry::getKey)
                .orElse(null);
    }


    private List<Student> findBy(final Collection<Student> students,
                                 final Predicate<Student> predicate) {
        return students
                .stream()
                .filter(predicate)
                .sorted(STUDENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    private List<Student> sortBy(final Collection<Student> students,
                                 final Comparator<Student> comparator) {
        return students
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private <E> List<E> transformToList(final Collection<Student> students, final Function<Student, E> mapper) {
        return students
                .stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
}
