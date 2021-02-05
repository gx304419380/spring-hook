package com.fly.spring.hook.util;

import org.springframework.lang.Nullable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author guoxiang
 */
public class ObjectUtils {

    private ObjectUtils() {}

    public static boolean isEmpty(@Nullable Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }

        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }

        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }

        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }

        if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        }

        // else
        return false;
    }


    public static boolean isEmpty(@Nullable Object[] array) {
        return (array == null || array.length == 0);
    }


    public static boolean notEmpty(@Nullable Object[] array) {
        return (array != null && array.length > 0);
    }

    public static boolean notEmpty(@Nullable Object obj) {
        return !isEmpty(obj);
    }

}
