package me.datafox.dynamica.test;

import me.datafox.dynamica.Obj;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author datafox
 */
public class DynamicaTest {
    @Test
    public void objTest() {
        Obj obj = Obj.of(new ArrayList<>());
        assertTrue(Obj.equals("[]", obj.call("toString")));
        obj.call("add", Obj.of("first element"));
        obj.call("add", Obj.of("second element"));
        obj.call("add", Obj.create());
        obj.call("add", Obj.of("fourth element"));
        obj.call("get", Obj.of(2))
                .register("toString", params -> Obj.of("third element"));
        assertTrue(Obj.equals("[first element, second element, third element, fourth element]", obj.call("toString")));
    }
}
