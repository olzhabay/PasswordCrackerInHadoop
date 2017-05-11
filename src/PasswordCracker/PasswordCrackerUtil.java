package PasswordCracker;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Utility class for PasswordCracker.

public class PasswordCrackerUtil {
    private static final String PASSWORD_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz";    // Possible Password symbol (NUMBER(0~9) + CHARACTER(A to Z))
    private static final int PASSWORD_LEN = 6;
    public static final long TOTAL_PASSWORD_RANGE_SIZE = (long) Math.pow(PASSWORD_CHARS.length(), PASSWORD_LEN);

    public static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot use MD5 Library:" + e.getMessage());
        }
    }

    public static String encrypt(String password, MessageDigest messageDigest) {
        messageDigest.update(password.getBytes());
        byte[] hashedValue = messageDigest.digest();
        return byteToHexString(hashedValue);
    }

    public static String byteToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    // Tries i'th candidate (rangeBegin <= i < rangeEnd) and compares against encryptedPassword
    // If original password is found, return the password;
    // if not, return null.

    public static String findPasswordInRange(long rangeBegin, long rangeEnd, String encryptedPassword, TerminationChecker checker)
            throws IOException {
        int[] passwordIterator = new int[PASSWORD_LEN];
        transformDecToBase36(rangeBegin, passwordIterator);
        for (long iterator = rangeBegin; iterator <= rangeEnd; iterator++) {
            if (checker.isTerminated()) return null;
            String password = transformIntoStr(passwordIterator);
            String hashedPassword = encrypt(password, getMessageDigest());
            if (hashedPassword.equals(encryptedPassword)) {
                checker.setTerminated();
                return password;
            }
            getNextCandidate(passwordIterator);
        }
        return null;
    }

    /* ###  transformDecToBase36  ###
     * The transformDecToBase36 transforms decimal into numArray that is base 36 number system
     * If you don't understand, refer to the homework01 overview
    */

    private static void transformDecToBase36(long numInDec, int[] numArrayInBase36) {
        for (int index = PASSWORD_LEN - 1; index >= 0; index--) {
            numArrayInBase36[index] = (int) (numInDec % 36);
            numInDec = numInDec / 36;
        }
    }

    private static void getNextCandidate(int[] candidateChars) {
        int reminder = 1;
        for (int index = PASSWORD_LEN - 1; index >= 0; index--) {
            candidateChars[index] += reminder;
            reminder = candidateChars[index] / 36;
            candidateChars[index] %= 36;
        }
    }

    private static String transformIntoStr(int[] candidateChars) {
        char[] password = new char[candidateChars.length];
        for (int i = 0; i < password.length; i++) {
            password[i] = PASSWORD_CHARS.charAt(candidateChars[i]);
        }
        return new String(password);
    }
}
