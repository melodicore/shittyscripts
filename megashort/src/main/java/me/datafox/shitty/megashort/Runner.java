package me.datafox.shitty.megashort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * This is a shitpost.
 *
 * @see Megashort
 * @see MegashortFactory
 * @author datafox
 */
public class Runner {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir"));
        Path pack = path.resolve("src/main/java/me/datafox/shitty/megashort");
        if(Files.exists(pack)) {
            path = pack;
        }
        Path file = path.resolve("Megashort.java");
        MegashortFactory builder = new MegashortFactory();
        Files.writeString(file, builder.getMegashort(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }
}
