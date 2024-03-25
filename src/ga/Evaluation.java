/** Tyler Crane
  * October 14 2019
  *
  * This class contains methods related to evaluating a chromosome in the GA
  *
  * Was written as part of assignment 2 for Cosc 3P71
  */
package ga; //decryptga

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class contains methods related to decrypting text and evaluating the fitness of decryption keys.
 */
public class Evaluation {

    //decrypt the text (t) using key (k)
    public static String decrypt(String k, String t) {
        //Sanitize the cipher and the key
        String cipher = t.toLowerCase();
        cipher = cipher.replaceAll("[^a-z]", "");
        cipher = cipher.replaceAll("\\s", "");

        String ke = k.toLowerCase();
        ke = ke.replaceAll("[^a-z]", "");
        ke = ke.replaceAll("\\s", "");

        char[] key = ke.toCharArray();
        for(int i = 0; i < key.length; i++) key[i] = (char)(key[i]-97);

        //Run the decryption
        String plain = "";
        int keyPtr = 0;
        for(int i = 0; i < cipher.length(); i++) {
            char keyChar = (char)0;
            if(key.length > 0) {
                //Ignore any value not in the expected range
                while(key[keyPtr] >25 || key[keyPtr] < 0) {
                    keyPtr = (keyPtr + 1)%key.length;
                }
                keyChar = key[keyPtr];
                keyPtr = (keyPtr + 1)%key.length;
            }
            plain += ((char)(((cipher.charAt(i)-97+26-keyChar)%26)+97));
        }
        return plain;
    }

    //Encrypt text (t) using the provided key (k) -- can use this for testing if needed
    public static String encrypt(String k, String t) {
        //Sanitize the cipher and the key
        String plain = t.toLowerCase();
        plain = plain.replaceAll("[^a-z]", "");
        plain = plain.replaceAll("\\s", "");
        String cipher = "";

        String ke = k.toLowerCase();
        ke = ke.replaceAll("[^a-z]", "");
        ke = ke.replaceAll("\\s", "");

        char[] key = ke.toCharArray();
        for(int i = 0; i < key.length; i++) key[i] = (char)(key[i]-97);

        //Encrypt the text
        int keyPtr = 0;
        for(int i = 0; i < plain.length(); i++) {
            char keyChar = (char)0;
            if(key.length > 0) {
                //Ignore any value not in the expected range
                while(key[keyPtr] >25 || key[keyPtr] < 0) {
                    keyPtr = (keyPtr + 1)%key.length;
                }
                keyChar = key[keyPtr];
                keyPtr = (keyPtr + 1)%key.length;
            }
            cipher += ((char)(((plain.charAt(i)-97+keyChar)%26)+97));
        }
        return cipher;
    }

    //This is a very simple fitness function based on the expected frequency of each letter in english
      //There is lots of room for improvement in this function.
        // k is the key, and t is the encrypted text
      public static double fitness(String k, String t) {
        //The expected frequency of each character in english language text according to
        //http://practicalcryptography.com/cryptanalysis/letter-frequencies-various-languages/english-letter-frequencies/
        double[] expectedFrequencies = new double[26];
        expectedFrequencies[0] = 0.085; //Expected frequency of a
        expectedFrequencies[1] = 0.016; //Expected frequency of b
        expectedFrequencies[2] = 0.0316; //Expected frequency of c
        expectedFrequencies[3] = 0.0387; //Expected frequency of d
        expectedFrequencies[4] = 0.121; //Expected frequency of e
        expectedFrequencies[5] = 0.0218; //Expected frequency of f
        expectedFrequencies[6] = 0.0209; //Expected frequency of g
        expectedFrequencies[7] = 0.0496; //Expected frequency of h
        expectedFrequencies[8] = 0.0733; //Expected frequency of i
        expectedFrequencies[9] = 0.0022; //Expected frequency of j
        expectedFrequencies[10] = 0.0081; //Expected frequency of k
        expectedFrequencies[11] = 0.0421; //Expected frequency of l
        expectedFrequencies[12] = 0.0253; //Expected frequency of m
        expectedFrequencies[13] = 0.0717; //Expected frequency of n
        expectedFrequencies[14] = 0.0747; //Expected frequency of o
        expectedFrequencies[15] = 0.0207; //Expected frequency of p
        expectedFrequencies[16] = 0.001; //Expected frequency of q
        expectedFrequencies[17] = 0.0633; //Expected frequency of r
        expectedFrequencies[18] = 0.0673; //Expected frequency of s
        expectedFrequencies[19] = 0.0894;//Expected frequency of t
        expectedFrequencies[20] = 0.0268;//Expected frequency of u
        expectedFrequencies[21] = 0.0106; //Expected frequency of v
        expectedFrequencies[22] = 0.0183;//Expected frequency of w
        expectedFrequencies[23] = 0.0019;//Expected frequency of x
        expectedFrequencies[24] = 0.0172;//Expected frequency of y
        expectedFrequencies[25] = 0.0011;//Expected frequency of z

        //Sanitize the cipher text and key
        String d = t.toLowerCase();
        d = d.replaceAll("[^a-z]", "");
        d = d.replaceAll("\\s", "");
        int[] cipher = new int[t.length()];
        for(int x = 0; x < cipher.length; x++) {
          cipher[x] = ((int)d.charAt(x))-97;
        }

        String ke = k.toLowerCase();
        ke = ke.replaceAll("[^a-z]", "");
        ke = ke.replaceAll("\\s", "");

        char[] key = ke.toCharArray();
        for(int i = 0; i < key.length; i++) key[i] = (char)(key[i]-97);


        int[] charCounts = new int[26];
        for(int i = 0; i < charCounts.length; i++) charCounts[i] = 0;

        int[] plain = new int[cipher.length];

        //Decrypt each character
        int keyPtr = 0;
        for(int i = 0; i < cipher.length; i++) {
          char keyChar = (char)0;
          if(key.length > 0) {
            //Ignore any value not in the expected range
            while(key[keyPtr] >25 || key[keyPtr] < 0) {
              keyPtr = (keyPtr + 1)%key.length;
            }
            keyChar = key[keyPtr];
            keyPtr = (keyPtr + 1)%key.length;
          }
          plain[i] = ((26 + cipher[i] - keyChar)%26);

        }

        //Count the occurences of each character
        for(int x : plain) {
            charCounts[x]++;
        }
        //Calculate the total difference between the expected frequencies and the actual frequencies
        double score = 0;
        for(int y =0; y < charCounts.length; y++) {
            score += Math.abs((((float)charCounts[y])/plain.length)-expectedFrequencies[y]);
        }

        return score;
      }

}
