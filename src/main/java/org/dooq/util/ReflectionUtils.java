package org.dooq.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author alex
 */
public class ReflectionUtils {

    public static boolean hasDiamondInterface(@NotNull Field field) {
        return field.getGenericType().getTypeName().contains("<");
    }

    public static @Nullable Class<?> getGenericType(@NotNull Field field) {

        var s = field.getGenericType()
                .getTypeName();

        if (!s.contains("<")) return null;

        var clazz = s.substring(s.lastIndexOf("<") + 1, s.indexOf(">"));

        if (clazz.contains(",")) {
            clazz = clazz.substring(clazz.indexOf(",") + 1)
                    .trim();
        }

        if (clazz.contains("? extends ")) {
            clazz = clazz.substring("? extends ".length())
                    .trim();
        }

        try {
            return Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static @Nullable Class<?> getGenericType(@NotNull Type type) {

        var s = type.getTypeName();

        if (!s.contains("<")) return null;

        var clazz = s.substring(s.lastIndexOf("<") + 1, s.indexOf(">"));

        if (clazz.contains(",")) {
            clazz = clazz.substring(clazz.indexOf(",") + 1)
                    .trim();
        }

        if (clazz.contains("? extends ")) {
            clazz = clazz.substring("? extends ".length())
                    .trim();
        }

        try {
            return Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Map<String, Field> mapFields(@NotNull Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, b -> b));
    }


    public static boolean hasInterface(@NotNull Class<?> target, @NotNull Class<?> inter) {

        if (!inter.isInterface()) {
            throw new IllegalArgumentException(inter + " is not a interface");
        }

        if (target == inter) {
            throw new IllegalStateException(inter + " is the same as " + target);
        }

        return Arrays.asList(target.getInterfaces()).contains(inter);
    }

}
