package info.kgeorgiy.ja.amirov.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * A wrapper for {@link Method} to use in collections, ensuring uniqueness based on method signature.
 */
public record MethodWrapper(Method method) {

    /**
     * Determines whether another object is equal to this {@code MethodWrapper}.
     * <p>
     * Equality is based on the method's name, parameter types, and return type.
     *
     * @param other the object to be compared for equality with this {@code MethodWrapper}
     * @return {@code true} if the specified object is equal to this {@code MethodWrapper}; {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (other instanceof MethodWrapper otherMethod) {
            return method.getName().equals(otherMethod.method.getName()) &&
                    Arrays.equals(method.getParameterTypes(), otherMethod.method.getParameterTypes()) &&
                    method.getReturnType().equals(otherMethod.method.getReturnType());
        }
        return false;
    }

    /**
     * Returns a hash code value for this {@code MethodWrapper}.
     * <p>
     * The hash code is generated from the method's name, parameter types, and return type.
     *
     * @return a hash code value for this {@code MethodWrapper}
     */
    @Override
    public int hashCode() {
        return Objects.hash(method.getName(), Arrays.hashCode(method.getParameterTypes()), method.getReturnType());
    }
}
