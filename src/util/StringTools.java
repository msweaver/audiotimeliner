package util;

import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;


/**
 * Provide some utilities for String manipulation.
 *
 */

public class StringTools extends Object {

    private static Logger log = Logger.getLogger(StringTools.class);
    private static HashSet<Character> diacritics = null;
    private static HashSet<Character> punctuation = null;

    static {
        diacritics = new HashSet<Character>();
        diacritics.add(Character.valueOf('\u0309'));
        diacritics.add(Character.valueOf('\u0300'));
        diacritics.add(Character.valueOf('\u0301'));
        diacritics.add(Character.valueOf('\u0302'));
        diacritics.add(Character.valueOf('\u0303'));
        diacritics.add(Character.valueOf('\u0304'));
        diacritics.add(Character.valueOf('\u0306'));
        diacritics.add(Character.valueOf('\u0307'));
        diacritics.add(Character.valueOf('\u0308'));
        diacritics.add(Character.valueOf('\u030C'));
        diacritics.add(Character.valueOf('\u030A'));
        diacritics.add(Character.valueOf('\uFE20'));
        diacritics.add(Character.valueOf('\uFE21'));
        diacritics.add(Character.valueOf('\u0315'));
        diacritics.add(Character.valueOf('\u030B'));
        diacritics.add(Character.valueOf('\u0310'));
        diacritics.add(Character.valueOf('\u0327'));
        diacritics.add(Character.valueOf('\u0328'));
        diacritics.add(Character.valueOf('\u0323'));
        diacritics.add(Character.valueOf('\u0324'));
        diacritics.add(Character.valueOf('\u0325'));
        diacritics.add(Character.valueOf('\u0333'));
        diacritics.add(Character.valueOf('\u0332'));
        diacritics.add(Character.valueOf('\u0326'));
        diacritics.add(Character.valueOf('\u031C'));
        diacritics.add(Character.valueOf('\u032E'));
        diacritics.add(Character.valueOf('\uFE22'));
        diacritics.add(Character.valueOf('\uFE23'));

        punctuation = new HashSet<Character>();
        punctuation.add(Character.valueOf('\u0021'));
        punctuation.add(Character.valueOf('\u0023'));
        punctuation.add(Character.valueOf('\u0024')); // dollar sign
        punctuation.add(Character.valueOf('\u0025'));
        punctuation.add(Character.valueOf('\u0026'));
        punctuation.add(Character.valueOf('\''));     // apostrophe

        // Don't strip parentheses, since we want to handle them separately.
        // In boolean searches, they may be logically meaningful.
        //punctuation.add(Character.valueOf('\u0028'));
        //punctuation.add(Character.valueOf('\u0029'));

        punctuation.add(Character.valueOf('\u002A'));
        punctuation.add(Character.valueOf('\u002B'));
        punctuation.add(Character.valueOf('\u002C'));
        punctuation.add(Character.valueOf('\u002D')); // hyphen
        punctuation.add(Character.valueOf('\u002E'));
        punctuation.add(Character.valueOf('\u002F')); // forward slash
        punctuation.add(Character.valueOf('\u003A'));
        punctuation.add(Character.valueOf('\u003B'));
        punctuation.add(Character.valueOf('\u003C'));
        punctuation.add(Character.valueOf('\u003D'));
        punctuation.add(Character.valueOf('\u003E'));
        punctuation.add(Character.valueOf('\u003F'));
        punctuation.add(Character.valueOf('\u005B'));
        punctuation.add(Character.valueOf('\\'));     // backslash
        punctuation.add(Character.valueOf('\u005D'));
        punctuation.add(Character.valueOf('\u005E'));
        punctuation.add(Character.valueOf('\u005F'));
        punctuation.add(Character.valueOf('\u0060')); // backquote
        punctuation.add(Character.valueOf('\u007B'));
        punctuation.add(Character.valueOf('\u007C'));
        punctuation.add(Character.valueOf('\u007D'));
        punctuation.add(Character.valueOf('\u007E'));
        punctuation.add(Character.valueOf('\u00A1'));
        punctuation.add(Character.valueOf('\u00BF'));
    }

    /**
     * Prohibit instantiation
     */
    private StringTools() {}


    /**
     * Remove punctuation from a String, replacing it with spaces. Parentheses are removed as well.
     */
    public static String removePunctuation(String data) {
        return removePunctuation(data, true);
    }

    /**
     * Remove punctuation from a String, replacing it with spaces.
     */
    public static String removePunctuation(String data, boolean removeParens) {
        log.assertLog(data != null, "null data");

        StringBuffer buf = new StringBuffer();
        boolean lastCharIsSpace = true;

        for (int i=0; i < data.length(); i++) {
            Character chr = Character.valueOf(data.charAt(i));
            if (punctuation.contains(chr) ||
                (removeParens && (data.charAt(i) == '(' || data.charAt(i) == ')'))) {
                if(!lastCharIsSpace) {
                    buf.append(' ');
                    lastCharIsSpace = true;
                }
            } else {
                buf.append(chr);
                lastCharIsSpace = Character.isWhitespace(data.charAt(i));
            }
        }

        return buf.toString();
    }


    /**
     * Removes trailing punctuation and white space. Leaves closing parens, brackets, and
     * braces, so they do not become unbalanced.
     */
    public static String removeTrailingPunctuation(String data) {
        int i = data.length() - 1;
        while (i >= 0) {
            char theChar = data.charAt(i);
            Character charObj = Character.valueOf(theChar);
            if ((theChar == ')') ||  // closing paren
                (theChar == '}') ||  // closing brace
                (theChar == ']') ||  // closing bracket
                (!punctuation.contains(charObj) &&
                !Character.isWhitespace(theChar))) {
                break;
            }
            i--;
        }

        return data.substring(0,i+1);
    }

    /**
     *  Convert string of form [term1 "term2 term3" term4] into a vector
     *  containing [term1, "term2 term3", term4] total 3 terms
     */
    public static Vector<String> split(String values) {
        StringTokenizer tokenizer = new StringTokenizer(values, " \"", true);
        Vector<String> result = new Vector<String>();
        final int OUT = 0;
        final int OUT_IN = 1;
        final int IN = 2;
        final int IN_OUT = 3;
        int state = OUT;
        String phrase = null;
        while (tokenizer.hasMoreTokens()) {
            String term = tokenizer.nextToken();
        if (term.equals("\"")) {
            state = (state + 1) %4;
        }
        switch (state) {
        case OUT:
            if (!term.trim().equals("")) {
            result.add(term);
        }
            break;
        case OUT_IN:
            phrase = "\"";
            state = IN;
            break;
        case IN:
            phrase = phrase + term;
            break;
        case IN_OUT:
                phrase = phrase + "\"";
            result.add(phrase);
            state = OUT;
            break;
            }
    }
        return result;
    }


    /**
       Forces a string to be a given length. If the string is too short, spaces are added.
       If the string is too long, trailing characters are stripped. This is similar to the
       LEFT function in BASIC or SQL.
    **/
    public static String forceLength(String s, int length) {
        String result = s;

        if(s.length() > length) {
            result = s.substring(0, length);
        }
        else if (s.length() < length) {
            result = s;
            while(result.length() < length) {
                result = result + " ";
            }
        }

        return result;
    }

    /**
     * Returns true if the lookFor string occurs in the base string.
     */
    public static boolean contains(String base, String lookFor) {
        boolean result = false;

        int index = base.indexOf(lookFor);
        if(index != -1)
            result = true;

        return result;
    }


}






