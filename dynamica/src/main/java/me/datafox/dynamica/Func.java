package me.datafox.dynamica;

import java.lang.reflect.InvocationTargetException;

/**
 * @author datafox
 */
@FunctionalInterface
public interface Func {
    Obj call(Obj ... params) throws InvocationTargetException, IllegalAccessException;
}
