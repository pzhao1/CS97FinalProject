package edu.swarthmore.cs.moodtracker.util;

import java.util.ArrayList;

/**
 * Created by rliang on 11/3/14.
 *
 *  Implementation of a function which encodes a String
 *  array into a specially-formatted String and another function
 *  which decodes such a specially-formatted String into its
 *  original String array. main() is a test of the encoding/decoding.
 *
 *  We use ","(comma) as the delimiter of our String and "?"(question
 *  mark) as our "break" character in order to distinguish "actual" commas
 *  from the delimiters as well as distinguish "actual" question marks
 *  from the break characters.
 *
 *  Our encoding would work like so:
 *    For every single "?" and "," in each string, prepend a "?" to it.
 *    Also, concatenate the modified String elements together, adding a
 *    delimiter between each String as well as after the last String.
 *    So for ["a?b", c,d"], the encoded String would be: "a??b,c?,d,"
 *
 *  Our decoding function would process an encoded String
 *  as follows:
 *    Initialize an integer counter as the "starting-point" index
 *    Marching through the String, for every "?" seen, remove it and
 *    skip the following character. This will remove any confusion we may
 *    have about "?"s and ","s in the actual Strings. Additionally, for
 *    every "," seen(we now know for sure that this is a
 *    delimiter, add what we have amassed so far from the starting-point
 *    to the list: we have isolated a list-element!
 *
 *    This will convert "a??b,c?,d," back to ["a?b", c,d"].
 */

public class StringEncodeUtil {
    /**
     *  Function: encode
     *  --------------------
     *  Encode a String array into a single String.
     *
     *  toEncode: the String array to be encoded.
     *
     *  returns: the encoded string.
     */
    public static String encode(String[] toEncode)
    {
        StringBuilder strBuf = new StringBuilder();
        for (String str : toEncode)
        {
            /*Retrieve a representation of the current string element that is
            easier to modify.*/
            StringBuilder curString = new StringBuilder(str);
            for(int j = 0; j < curString.length(); j++)
                //If we see a "break" character, prepend another "break" character to it.
                if(curString.charAt(j) == '?')
                {
                    curString.insert(j, '?');
                    j++;
                }
                //If we see a delimiter character, prepend "break" character to it.
                else if(curString.charAt(j) == ',')
                {
                    curString.insert(j, '?');
                    j++;
                }

            //Add the delimiter to the encoded string.
            strBuf.append(curString).append(",");

        }
        return strBuf.toString();
    }

    /**
     *   Function: decode
     *  --------------------
     *  Decode an encoded string into its original array..
     *
     *  toDecode: the String to be decoded.
     *
     *  returns: the decoded String array.
     */
    public static String[] decode(String toDecode)
    {
        /*We utilize an ArrayList, since the exact number of
        string elements is inderterminate*/
        ArrayList<String> decoded = new ArrayList<String>();

        /*Retrieve a representation of the current string element that is
        easier to modify.*/
        StringBuilder encodedStr = new StringBuilder(toDecode);

        //The starting point of the current isolated string to add/append.
        int currIndex = 0;

        for(int i = 0; i < encodedStr.length(); i++)
        {
            /*If we see the "break character" remove it, and skip
            the following character*/
            if(encodedStr.charAt(i) == '?')
                encodedStr.deleteCharAt(i);

            /*If we see a delimiter, add what we have amassed
            so far to the list.*/
            else if(encodedStr.charAt(i) == ',')
            {
                decoded.add(encodedStr.substring(currIndex, i));

                //Update the starting point.
                currIndex = i + 1;
            }
        }

        String[] toReturn = new String[decoded.size()];
        return decoded.toArray(toReturn);
    }
}
