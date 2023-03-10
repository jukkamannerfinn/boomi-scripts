import java.lang.Byte;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
    }
    return data;
}

    Charset utf8charset = Charset.forName("UTF-8");
    Charset iso88591charset = Charset.forName("ISO-8859-1");

    byte[] utf8bytes = hexStringToByteArray("5461726b6bE2809961");
    String realUTFstring = new String ( utf8bytes, utf8charset );
    byte[] realUTFbytes = realUTFstring.getBytes(utf8charset);
    printf "apostrophe: ";
    for (byte b : realUTFbytes)
        printf("0x"+"%02x ", b);
    println realUTFstring;

    string = "Tarkk’a"; 
    string = "Tarkk'ampujankatu";
    string = string.replaceAll("’","'");
    byte[] UTFbytes = string.getBytes(utf8charset);
    printf "Windows UTF:";
    for (byte b : UTFbytes)
        printf("0x"+"%02x ", b);
    String UTFstring = new String(UTFbytes, utf8charset);
    println UTFstring;
    
    byte[] iso88591bytes = string.getBytes(iso88591charset);
    printf "ISO-8859-1: ";
    for (byte b : iso88591bytes)
        printf("0x"+"%02x ", b);
    String ISOstring = new String ( iso88591bytes, iso88591charset );
    println ISOstring;

    byte[] iso88591conversion = UTFstring.getBytes(iso88591charset);
    printf "Converted:  ";
    for (byte b : iso88591conversion)
        printf("0x"+"%02x ", b);
    String ISOstringFinal = new String ( iso88591conversion, iso88591charset );
    println ISOstringFinal;
    

    
    
