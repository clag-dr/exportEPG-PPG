package exportFile2RN;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
 
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;



public class ExportFile2RN {

        //final static String secretKey = "ngDP4DR!";

        public static String dbUsername;
        public static String dbPassword;
        public static String dbString;
        private static SecretKeySpec secretKeySpec;
        final static String secretKey = "RN4DR!";
        private static byte[] key;



        public static void main(String[] args) {

                if (args.length==3) {
                        buildConf(args);
                } else if (args.length==0) {
                        System.out.println("Starting export.....");
                        readConf();
                        Connection dbConnection = getDBConnection(dbUsername,dbPassword,dbString);
                        export2File(dbConnection);
                        closeDBConnection(dbConnection);
                        System.out.println("All done.");
                } else {
                        System.out.println("Syntax: ExportFile2RN [dbUser dbPassword dbConnectString]");

                }

    }


          public static String ClobStringConversion(java.sql.Clob  clb) throws SQLException, IOException {
                if (clb == null) {
                    return "";
                }
                StringBuilder str = new StringBuilder();
                String strng;
                BufferedReader br = new BufferedReader(clb.getCharacterStream());
                while ((strng = br.readLine()) != null) {
                    str.append(strng);
                }

                return str.toString();
            }

        public static void export2File(Connection pConnection) {
                System.out.println("Exporting.....");
                java.sql.Clob jsonString;
                jsonString=null;
        try {            
            if (pConnection  != null) {
                Statement stmt = pConnection.createStatement();
                String sql = "select * from XXDR_FILE4RN where created_Date=(select max(created_Date) from XXDR_FILE4RN )";
                ResultSet rs = stmt.executeQuery(sql);
                int vId=0;
                String vReportFileName ="file2RN.json";
                rs.next();
                jsonString = (java.sql.Clob)rs.getObject("JSON_FILE");
                System.out.println(vReportFileName + " exported.");
                    
                //byte [] array = jsonString.getBytes( 1, ( int ) jsonString.length() );
                String ss = ClobStringConversion(jsonString);
                
                File file = new File(vReportFileName);

                
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                    bw.write(ss);
                    bw.close();
                } catch (IOException asd) {
                        System.out.println(asd.getMessage());
                }
                                                                             
                stmt.close();               
            }

        }  catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } finally {                
                System.out.println(" ");
        }

        }





        public static Connection getDBConnection(String pUserName, String pPassword, String pConnectString) {
        //      drLog =  new ngDPUtils.DRlog();

        //      drLog.doOutput("Entering getDBConnection");
                Connection dbConnection = null;

                try {
                        Class.forName("oracle.jdbc.OracleDriver");
                } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        String dbURL2 = "jdbc:oracle:thin:@"+pConnectString; //apxdbpack.lx.dr.dk:1594:apxprd";
        String username = pUserName; 
        String password = pPassword; 
        try {
                        dbConnection = DriverManager.getConnection(dbURL2, username, password);
                //      drLog.doOutput("Connected with database");
        } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        //drLog.doOutput("Exiting getDBConnection");
        return dbConnection;
        }



        public static void closeDBConnection(Connection dbConnection) {
                //drLog.doOutput("Entering closeDBConnection");
                try {
                        dbConnection.close();
                } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                //drLog.doOutput("Exiting closeDBConnection");
        }



    public static void setKey(String myKey) 
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); 
            secretKeySpec = new SecretKeySpec(key, "AES");
        } 
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } 
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
 
    public static String encrypt(String strToEncrypt, String secret) 
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
 
    public static String decrypt(String strToDecrypt, String secret) 
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    
    public static void buildConf(String[] pArgs) {
        String vArgument;
        FileWriter confWriter = null;

        try {
                        confWriter = new FileWriter("exportFile2RN.conf");
                } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                }
        for(int i = 0; i < pArgs.length; i++) {
           vArgument = pArgs[i];
           String encryptedString = encrypt(vArgument, secretKey);                        
           try {
                        confWriter.write(encryptedString+"\n");
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }
        try {
                        confWriter.close();
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                System.out.println("exportFile2RN.conf created");
    }
    
    public static  void readConf() {
                BufferedReader reader;
                try {

                        reader = new BufferedReader(new FileReader("exportFile2RN.conf"));
                        dbUsername       = decrypt(reader.readLine(), secretKey);
                        dbPassword    = decrypt(reader.readLine(), secretKey);
                        dbString      = decrypt(reader.readLine(), secretKey);
                //      System.out.println(dbUsername);
                //      System.out.println(dbPassword);
                //      System.out.println(dbString);

                        reader.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }

    }
    
    

}


