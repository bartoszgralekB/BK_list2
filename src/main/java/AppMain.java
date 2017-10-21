import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.io.BaseEncoding;
import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AppMain {
    public static void main(String[] args) {

        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        ArrayList<String> arrayList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for(int a = 'a'; a <= 'z'; a++)
        {
            stringBuilder.append((char) a);
            arrayList.add(stringBuilder.toString());
            stringBuilder = new StringBuilder();
        }

        for(int a = 0; a <= 9; a++) {
            stringBuilder.append(String.valueOf(a));
            arrayList.add(stringBuilder.toString());
            stringBuilder = new StringBuilder();
        }

        long startTime = System.currentTimeMillis();
        try {
            generateCombinations("key.txt","iv.txt","cipher.txt",arrayList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        //System.out.println("Time: " + estimatedTime/36.0 + "ms");


    }

    private String decryptCommand(String keyFile,String ivFile,String cipherFile) throws IOException {
        File file = new File(keyFile);
        String key = FileUtils.readFileToString(file,"UTF-8");

        file = new File(ivFile);
        String iv = FileUtils.readFileToString(file,"UTF-8");

        int size = 64 - key.length();

        return "openssl enc -A -aes-256-cbc -d -base64 -K " + key + " -iv " + iv + " -in " + cipherFile;
    }

    private static String[] executeCommand(String command) {
        String[] output = new String[2];

        StringBuffer outputBuffer = new StringBuffer();

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader readerError =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));


            String line = "";
            while ((line = reader.readLine())!= null) {
                outputBuffer.append(line + "\n");
            }

            output[0] = outputBuffer.toString();
            outputBuffer = new StringBuffer();

            while ((line = readerError.readLine())!= null) {
                outputBuffer.append(line + "\n");
            }

            p.waitFor();
            output[1] = outputBuffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                p.getInputStream().close();
                p.getErrorStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output;

    }

    public static void generateCombinations(String keyFile,String ivFile,String cipherFile, ArrayList<String> possibleValues) throws IOException {
        File file = new File(keyFile);
        String key = FileUtils.readFileToString(file,"UTF-8");

        file = new File(ivFile);
        String iv = FileUtils.readFileToString(file,"UTF-8");

        int arraySize = 64 - key.length();
        String command;
        String output[];
        //return "openssl enc -A -aes-256-cbc -d -base64 -K " + key + " -iv " + iv + " -in " + cipherFile;

        int carry;
        int[] indices = new int[arraySize];
        StringBuffer stringBuffer = new StringBuffer();
        String prefix = null;
        do
        {
            for(int index : indices)
                stringBuffer.append(possibleValues.get(index));
            prefix = stringBuffer.toString();
            key = prefix + key;

            command = "openssl enc -A -aes-256-cbc -d -base64 -K " + key + " -iv " + iv + " -in " + cipherFile;

            output = executeCommand(command);
            if(output[1].isEmpty()) {
                System.out.println("Great!");
                System.out.println("Key: " + key);
                System.out.println("Message: " + output[0]);
                System.out.println("Error: " + output[1]);
            }

            key = key.substring(prefix.length(),key.length());
            stringBuffer = new StringBuffer();

            carry = 1;
            for(int i = indices.length - 1; i >= 0; i--)
            {
                if(carry == 0)
                    break;

                indices[i] += carry;
                carry = 0;

                if(indices[i] == possibleValues.size())
                {
                    carry = 1;
                    indices[i] = 0;
                }
            }
        }
        while(carry != 1); // Call this method iteratively until a carry is left over
    }
}


