package me.datafox.shitty.megashort;

/**
 * Generates the written name of a number. Despite taking in an {@code int}, it only works from -999999 to 999999, as it
 * is only meant for the range of a {@code short}.
 *
 * @author datafox
 */
public class NameFactory {
    public String getName(int i) {
        if(i == 0) {
            return "ZERO";
        }
        StringBuilder sb = new StringBuilder();
        if(i < 0) {
            sb.append("NEGATIVE_");
            i = Math.abs(i);
        }
        if(i >= 1000) {
            sb.append(getName((i / 1000))).append("_THOUSAND_");
            i %= 1000;
        }
        if(i >= 100) {
            sb.append(getOnes(i / 100)).append("_HUNDRED_");
            i %= 100;
        }
        if(i != 0 && !sb.isEmpty()) {
            sb.append("AND_");
        }
        if(i >= 20) {
            sb.append(getTens(i / 10));
            i %= 10;
        } else if(i >= 10) {
            sb.append(getTeens(i));
            return sb.toString();
        }
        if(i != 0) {
            sb.append(getOnes(i));
        }
        String str = sb.toString();
        if(str.endsWith("_")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    private String getTens(int i) {
        return switch(i) {
            case 9 -> "NINETY_";
            case 8 -> "EIGHTY_";
            case 7 -> "SEVENTY_";
            case 6 -> "SIXTY_";
            case 5 -> "FIFTY_";
            case 4 -> "FORTY_";
            case 3 -> "THIRTY_";
            case 2 -> "TWENTY_";
            default -> "";
        };
    }

    private String getTeens(int i) {
        return switch(i) {
            case 19 -> "NINETEEN";
            case 18 -> "EIGHTEEN";
            case 17 -> "SEVENTEEN";
            case 16 -> "SIXTEEN";
            case 15 -> "FIFTEEN";
            case 14 -> "FOURTEEN";
            case 13 -> "THIRTEEN";
            case 12 -> "TWELVE";
            case 11 -> "ELEVEN";
            case 10 -> "TEN";
            default -> "";
        };
    }

    private String getOnes(int i) {
        return switch(i) {
            case 9 -> "NINE";
            case 8 -> "EIGHT";
            case 7 -> "SEVEN";
            case 6 -> "SIX";
            case 5 -> "FIVE";
            case 4 -> "FOUR";
            case 3 -> "THREE";
            case 2 -> "TWO";
            case 1 -> "ONE";
            default -> "";
        };
    }
}
