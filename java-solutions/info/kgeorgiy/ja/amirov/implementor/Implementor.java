package info.kgeorgiy.ja.amirov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.lang.reflect.Type;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Executable;
import java.lang.reflect.Constructor;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static info.kgeorgiy.ja.amirov.implementor.UtilWords.*;

/**
 * Implementor provides automatic implementation of interfaces and abstract classes.
 * It can also package the generated implementations into a JAR file.
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Checks if the given arguments are not null.
     *
     * @param args arguments to check
     * @throws ImplerException if any argument is null
     */
    private static void checkNullArgs(Object... args) throws  ImplerException {
        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new ImplerException("Null arguments are not allowed.");
        }
    }

    /**
     * Creates directories for the given path if they do not exist.
     *
     * @param path the path for which to create directories
     * @throws ImplerException if directories cannot be created
     */
    private static void createDirectories(Path path) throws ImplerException {
        if (path.toAbsolutePath().getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can't create directories for output file", e);
            }
        }
    }

    /**
     * Generates the name for the implementation class.
     *
     * @param clazz the class to implement
     * @return the name of the implemented class
     */
    private static String implementationName(Class<?> clazz) {
        return clazz.getSimpleName() + IMPL;
    }

    /**
     * Creates the path for the generated source file.
     *
     * @param root  the root directory
     * @param clazz the class to implement
     * @return the path of the generated source file
     */
    private static Path createPath(Path root, Class<?> clazz) {
        return root.resolve(clazz.getPackageName().replace(DOT, File.separator))
                .resolve(implementationName(clazz) + DOT + JAVA);
    }

    /**
     * Generates the package declaration for the implementation class.
     *
     * @param clazz the class to implement
     * @return the package declaration string
     */

    private static String createPackage(Class<?> clazz) {
        Package packageObj = clazz.getPackage();

        return Objects.isNull(packageObj) ? EMPTY :
                String.join(SPACE, PACKAGE, packageObj.getName(), SEMICOLON, System.lineSeparator());
    }

    /**
     * Generates the class declaration for the implementation class.
     *
     * @param clazz the class to implement
     * @return the class declaration string
     */
    private static String createDeclaration(Class<?> clazz) {
        return String.join(SPACE, PUBLIC, CLASS,
                implementationName(clazz),
                (clazz.isInterface() ? IMPLEMENTS : EXTENDS),
                clazz.getCanonicalName()
        );
    }

    /**
     * Writes the header of the implementation class to the writer.
     *
     * @param clazz  the class to implement
     * @param writer the writer to use
     * @throws IOException if an I/O error occurs
     */
    private static void generateClassHead(Class<?> clazz, Writer writer) throws IOException {
        writer.write(toUnicode(createPackage(clazz) + EOL + createDeclaration(clazz) + SPACE + LEFT_BRACE + EOL));
    }

    /**
     * Joins the types with a comma and a space.
     *
     * @param typeVariables the types to join
     * @return a string representation of the types
     */
    private static String createTemplates(TypeVariable<?>[] typeVariables) {
        return typeVariables.length == 0 ? EMPTY :
                String.format("<%s>", smartJoin(typeVariables, TypeVariable::getName));
    }

    /**
     * Joins types into a string with comma separation.
     *
     * @param types the types to join
     * @return a string representation of the types
     */
    private static String smartJoinTypes(Type[] types) {
        return smartJoin(types, Type::getTypeName);
    }

    /**
     * Generates constructors for the implementation class.
     *
     * @param clazz  the class to implement
     * @param writer the writer to use
     * @throws ImplerException if no suitable constructors are found
     * @throws IOException if an I/O error occurs
     */
    private static void generateConstructors(Class<?> clazz, Writer writer)
            throws  ImplerException, IOException {
        List<Constructor<?>> constructors = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(it -> !Modifier.isPrivate(it.getModifiers())).toList();

        if (constructors.isEmpty()) {
            throw new ImplerException("No appropriate constructors find at all");
        }

        for (Constructor<?> constructor : constructors) {
            writer.write(createExec(constructor) + EOL);
        }
    }

    /**
     * Generates the declaration of a class or interface that is to be implemented or extended.
     *
     * @param executable An instance of {@link Executable}, which could be a method or a constructor.
     * @return A string representing the declaration of the class or interface.
     */
    private static String createDeclaringClass(Executable executable) {
        if (executable instanceof Method) {
            return String.join(SPACE, createTemplates(executable.getTypeParameters()),
                    ((Method) executable).getGenericReturnType().getTypeName(), executable.getName());
        }
        return implementationName(((Constructor<?>) executable).getDeclaringClass());
    }

    /**
     * Constructs the signature for method parameters and exceptions for an executable element.
     *
     * @param executable An executable element such as a method or a constructor.
     * @return A string representation of the method's or constructor's parameters and exceptions.
     */
    private static String createLogic(Executable executable) {
        Type[] exceptions = executable.getGenericExceptionTypes();
        return LEFT_BRACKET +
                smartJoin(executable.getParameters(), it -> it.toString().replace(CASH_MONEY_AP, DOT)) +
                RIGHT_BRACKET +
                (exceptions.length == 0 ? EMPTY : SPACE + THROWS + SPACE + smartJoinTypes(exceptions));
    }

    /**
     * Creates the body of a method or constructor.
     *
     * @param body The body content as a string.
     * @return A formatted string representing the body enclosed in braces.
     */
    private static String createFunction(String body) {
        return SPACE + LEFT_BRACE + SPACE + EOL + TAB.repeat(2) + body + SEMICOLON + EOL + TAB + RIGHT_BRACE;
    }

    /**
     * Generates the default implementation for a method.
     *
     * @param method The method for which to generate the implementation.
     * @return A string containing the default implementation of the method.
     */
    private static String createMethod(Method method) {
        Class<?> returnValue = method.getReturnType();
        String string;
        if (returnValue.equals(void.class)) {
            string = EMPTY;
        } else if (returnValue.equals(boolean.class)) {
            string = FALSE;
        } else if (returnValue.isPrimitive()) {
            string = ZERO;
        } else {
            string = NULL;
        }
        return createFunction(RETURN + SPACE + string);
    }

    /**
     * Generates a constructor call to the super class.
     *
     * @param constructor The constructor for which to generate the super call.
     * @return A string containing the call to the super constructor.
     */
    private static String createConstructor(Constructor<?> constructor) {
        return createFunction(Arrays.stream(constructor.getParameters()).map(Parameter::getName)
                .collect(Collectors.joining(COMMA + SPACE, SUPER + LEFT_BRACKET, RIGHT_BRACKET)));
    }

    /**
     * Creates the body for an executable element, either a method or a constructor.
     *
     * @param executable An executable element such as a method or a constructor.
     * @return A string containing the body of the executable element.
     */
    private static String createBody(Executable executable) {
        if (executable instanceof Method) {
            return createMethod((Method) executable);
        }
        return createConstructor((Constructor<?>) executable);
    }

    /**
     * Generates the full declaration for an executable element, including modifiers, return type, name, parameters, and exceptions.
     *
     * @param exec The executable element for which to generate the declaration.
     * @return A string representing the full declaration of the executable element.
     */
    private static String createExec(Executable exec) {
        StringBuilder stringBuilder = new StringBuilder();
        final int modifiers = exec.getModifiers() & ~Modifier.ABSTRACT
                & ~Modifier.NATIVE & ~Modifier.TRANSIENT;

        stringBuilder
                .append(TAB)
                .append(Modifier.toString(modifiers))
                .append(SPACE)
                .append(createDeclaringClass(exec))
                .append(createLogic(exec))
                .append(createBody(exec))
                .append(EOL);

        return toUnicode(stringBuilder.toString()).replace(CASH_MONEY_AP, DOT);
    }

    /**
     * Creates a set of unique method wrappers for the methods of the specified class
     * that match the given predicate.
     *
     * @param clazz The class whose methods are to be wrapped.
     * @param filter A predicate to filter methods that should be included.
     * @return A set of {@link MethodWrapper} objects for methods satisfying the predicate.
     * @see MethodWrapper
     */
    private static Set<MethodWrapper> createMethods(Class<?> clazz, Predicate<Method> filter) {
        return Arrays.stream(clazz.getMethods())
                .filter(filter)
                .map(MethodWrapper::new)
                .collect(Collectors.toSet());
    }

    /**
     * Collects a set of {@link MethodWrapper} objects representing all unique methods
     * (considering method name and parameter types) that the specified class or interface
     * needs to implement, directly or indirectly.
     *
     * @param clazz The class or interface to analyze.
     * @return A set of {@link MethodWrapper} objects representing all methods that need to be implemented.
     */
    private static Set<MethodWrapper> getMethodSet(Class<?> clazz) {
        Set<MethodWrapper> methods = new HashSet<>(createMethods(clazz, m -> Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())));

        while (clazz != null) {
            methods.addAll(Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> Modifier.isAbstract(m.getModifiers()) &&
                            (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())))
                    .map(MethodWrapper::new)
                    .collect(Collectors.toSet()));
            clazz = clazz.getSuperclass();
        }
        return methods;
    }


    /**
     * Generates implementation for all abstract methods of the class.
     *
     * @param clazz  the class to implement
     * @param writer the writer to use
     * @throws IOException if an I/O error occurs
     */
    private static void generateMethods(Class<?> clazz, Writer writer) throws IOException {
        for (MethodWrapper methodWrapper : getMethodSet(clazz)) {
            Method method = methodWrapper.method();
            if (clazz.isInterface() || Modifier.isAbstract(method.getModifiers())) {
                writer.write(createExec(methodWrapper.method()) + EOL);
            }
        }
        writer.write(RIGHT_BRACE);
    }

    /**
     * Generates the complete body of the implementation class.
     *
     * @param clazz  the class to implement
     * @param writer the writer to use
     * @throws ImplerException if an error occurs during generation
     * @throws IOException if an I/O error occurs
     */
    private static void generateBody(Class<?> clazz, Writer writer) throws ImplerException, IOException {
        if (!clazz.isInterface()) {
            generateConstructors(clazz, writer);
        }
        generateMethods(clazz, writer);
    }

    /**
     * Checks whether the given class can be implemented.
     *
     * @param clazz the class to check
     * @return true if the class can be implemented, false otherwise
     */
    protected boolean isImplementationPossible(final Class<?> clazz) {
        final int modifiers = clazz.getModifiers();

        return !(clazz.isArray() ||
                clazz.isPrimitive() ||
                clazz == Enum.class ||
                Modifier.isFinal(modifiers) ||
                Modifier.isPrivate(modifiers) ||
                clazz.isAssignableFrom(Record.class)
        );
    }


    /**
     * Compiles the generated source files for the specified class.
     * This method utilizes the system's Java compiler to compile the source files.
     *
     * @param clazz The class whose implementation has been generated.
     * @param path The path where the source files are located.
     * @throws ImplerException If the compilation fails or a compiler is not available.
     */
    private void compile(final Class<?> clazz, final Path path) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("No compiler is provided");
        }

        try {
            List<String> args = getCLIArgs(clazz, path);

            if (compiler.run(null, null, null, args.toArray(new String[0])) != 0) {
                throw new ImplerException("Compilation error");
            }
        } catch (URISyntaxException e) {
            throw new ImplerException("Error obtaining class path", e);
        }

    }

    /**
     * Prepares command line arguments for the Java compiler.
     * This method constructs a list of command line arguments to be passed to the compiler,
     * including the encoding, classpath, and source file paths.
     *
     * @param clazz The class to be compiled.
     * @param path The path where the source file is located.
     * @return A list of command line arguments for the compiler.
     * @throws URISyntaxException If an error occurs while constructing the classpath.
     */
    private static List<String> getCLIArgs(Class<?> clazz, Path path) throws URISyntaxException {
        final Path classPath = Path.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        final String filePath = path.resolve(
                Path.of(
                        clazz.getPackageName().replace(DOT, File.separator),
                        String.format("%s%s" + DOT + JAVA, clazz.getSimpleName(), IMPL)
                )
        ).toString();

        return List.of(
                "-encoding", "UTF-8",
                "-cp", path + File.pathSeparator + classPath,
                filePath
        );
    }

    /**
     * Generates the manifest file for the JAR and writes the implementation class into the JAR file.
     * This method constructs the manifest to include the main class and packages the compiled class files into the JAR.
     *
     * @param clazz The class whose implementation is to be packaged.
     * @param path The directory containing the compiled class files.
     * @param jarPath The output path for the JAR file.
     * @throws ImplerException If an error occurs during manifest generation or JAR creation.
     */

    private void generateManifest(final Class<?> clazz, final Path path, final Path jarPath) throws ImplerException {
        final var manifest = new Manifest();
        final var mainAttributes = manifest.getMainAttributes();

        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, ONE + DOT + ZERO);
        String classFilePath = clazz.getPackageName().replace(DOT, SLASH) + SLASH + implementationName(clazz) + DOT + CLASS;

        try (final JarOutputStream jarOut = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            jarOut.putNextEntry(new ZipEntry(classFilePath));
            Path classFileFullPath = path.resolve(classFilePath.replace(SLASH, File.separator));
            Files.copy(classFileFullPath, jarOut);
        } catch (IOException e) {
            throw new ImplerException(String.format("Error during writing to jarOut %s", e.getMessage()));
        }
    }

    /**
     * Implements the specified class and packages the implementation into a JAR file.
     * This method generates the implementation source code, compiles it, and then packages the compiled class files into a JAR.
     *
     * @param clazz The class to be implemented.
     * @param jarFile The output path for the JAR file.
     * @throws ImplerException If an error occurs during any stage of JAR file creation.
     */
    @Override
    public void implementJar(Class<?> clazz, Path jarFile) throws ImplerException {
        checkNullArgs(clazz, jarFile);
        createDirectories(jarFile);
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "tmp");
            if (isImplementationPossible(clazz)) {
                implement(clazz, tmpDir);
                compile(clazz, tmpDir);
                generateManifest(clazz, tmpDir, jarFile);
            } else {
                throw new ImplerException(String.format("Class: %s not supported: ", clazz.getCanonicalName()));
            }
        } catch (IOException e) {
            throw new ImplerException("Error during creation of temp dir: ", e);
        } finally {
            if (tmpDir != null) {
                try (Stream<Path> paths = Files.walk(tmpDir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    System.err.println("Failed deleting temp file " + path + ": " + e.getMessage());
                                }
                            });
                } catch (IOException e) {
                    System.err.println("Failed to walk temp dir " + tmpDir + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * The main method serves as the entry point for the Implementor application.
     * It processes command-line arguments to generate implementations for specified classes or interfaces,
     * and optionally packages the compiled files into a JAR.
     * <p>
     * Usage:
     * <ul>
     *     <li>To generate source code: {@code java Implementor <fully-qualified-classname> <output-directory>}</li>
     *     <li>To generate a JAR file: {@code java -jar Implementor -jar <fully-qualified-classname> <jar-file>}</li>
     * </ul>
     * <p>
     * The application supports two modes of operation based on the input arguments:
     * <ol>
     *     <li>
     *         Direct implementation generation mode, which requires two arguments:
     *         the fully qualified name of the class or interface to implement,
     *         and the path to the output directory where the source code will be saved.
     *     </li>
     *     <li>
     *         JAR generation mode, indicated by the presence of "-jar" as the first argument,
     *         followed by the fully qualified name of the class or interface and the path to the output JAR file.
     *     </li>
     * </ol>
     * In both cases, the specified class or interface must not be a primitive type, array, final class, or private inner class.
     *
     * @param args The command-line arguments, specifying the operation mode and necessary parameters.
     */

    public static void main(String[] args) {
        try {
            checkNullArgs(args, args[0]);
            JarImpler implementor = new Implementor();

            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            } else if (args.length == 3 && args[0].equals(JAR_CMD_CONST)) {
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
            } else {
                System.err.println("Wrong cmd for implementor");
            }
        } catch (InvalidPathException e) {
            System.err.println("Wrong path to root: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Wrong class name: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Implementation error: " + e.getMessage());
        }
    }

    @Override
    public void implement(Class<?> clazz, Path root) throws ImplerException {
        checkNullArgs(clazz, root);

        if (!isImplementationPossible(clazz)) {
            throw new ImplerException(String.format("Class: %s not supported.", clazz.getCanonicalName()));
        }

        root = createPath(root, clazz);
        createDirectories(root);

        try (Writer writer = Files.newBufferedWriter(root)) {
            generateClassHead(clazz, writer);
            generateBody(clazz, writer);
        } catch (IOException e) {
            throw new ImplerException("Error in writing in output file", e);
        }
    }
}