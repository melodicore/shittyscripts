package me.datafox.dynamica;

import java.lang.reflect.InvocationTargetException;

/**
 * An interface that represents a typeless function.
 *
 * @author datafox
 */
@FunctionalInterface
public interface Func {
    /**
     * Every function call as one function
     *
     * @param aThis object calling this function
     * @param params parameters for the function
     * @return resulting typeless object or {@code null}
     *
     * @throws InvocationTargetException if an underlying native method could not be invoked
     * @throws IllegalAccessException if an underlying native method could not be accessed
     */
    Obj call(Obj aThis, Obj ... params) throws InvocationTargetException, IllegalAccessException;
}
