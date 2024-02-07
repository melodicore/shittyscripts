package me.datafox.shitty.megashort;

/**
 * Generates an enum with all possible values of a {@code short}, from -32768 to 32767.
 *
 * @author datafox
 */
public class MegashortFactory {
    private static final String PREFIX = """
    package me.datafox.shitty.megashort;
    
    /**
     * This file was generated automatically. It is also a shitpost. It contains all possible values of a {@code short},
     * from -32768 to 32767. It also exceeds Java's internal limits and so does not compile, despite being technically
     * valid.
     *
     * @author datafox
     */
    
    public enum Megashort {""";
    private static final String SUFFIX = """
            ;
            
                private final short s;
                
                Megashort(short s) {
                    this.s = s;
                }
                
                public short getShort() {
                    return s;
                }
            }""";

    private final NameFactory nameFactory;

    public MegashortFactory() {
        nameFactory = new NameFactory();
    }

    public String getMegashort() {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX);
        for(short s = Short.MIN_VALUE; s < Short.MAX_VALUE; s++) {
            sb.append("\n    ").append(getDeclaration(s)).append(",");
        }
        sb.append("\n    ").append(getDeclaration(Short.MAX_VALUE)).append(SUFFIX);
        return sb.toString();
    }

    private String getDeclaration(short s) {
        return nameFactory.getName(s) + "((short) " + s + ")";
    }
}
