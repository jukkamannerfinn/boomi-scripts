// Use for testing. Remove when adding to Data Process shape.
import com.boomi.execution.ContextCreator
import com.boomi.execution.ExecutionUtil
import org.apache.tools.ant.types.selectors.SelectSelector

// Place directory for multiple files and file name for single file
String pathFiles = "C:/Users/AY54337/IdeaProjects/Boomi_Maven/input_files/emptyfile.txt"
println pathFiles
dataContext = new ContextCreator()
dataContext.AddFiles(pathFiles)
ExecutionUtil ExecutionUtil = new ExecutionUtil()

/* Add any Dynamic Process Properties or Dynamic Document Properties. If
   setting DDPs for multiple files, then index needs to be set for each and
   the index range starts at 0. */
ExecutionUtil.setDynamicProcessProperty("ACCOUNT_KEY", "enter-your-key-here", false)
ExecutionUtil.setDynamicProcessProperty("ACCOUNT_NAME", "exflowinvoices", false)
//document.dynamic.userdefined.XXXXXXX
dataContext.addDynamicDocumentPropertyValues(0, "verb", "PUT")
dataContext.addDynamicDocumentPropertyValues(0, "path", "inbound/invoices")
dataContext.addDynamicDocumentPropertyValues(0, "operation", "")



// Place script after this line.
//----------------------------------------------------------------------------------------------------

import java.util.Properties;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import com.boomi.execution.ExecutionUtil;
import java.text.SimpleDateFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;

logger = ExecutionUtil.getBaseLogger();


for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss",Locale.ENGLISH);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    String date = sdf.format(new Date()) + " GMT";

    // Generic params
    String accountKey = ExecutionUtil.getDynamicProcessProperty("ACCOUNT_KEY");
    String accountName = ExecutionUtil.getDynamicProcessProperty("ACCOUNT_NAME");
    String verb = props.getProperty("document.dynamic.userdefined.verb");
    String path = props.getProperty("document.dynamic.userdefined.path");
    String operation = props.getProperty("document.dynamic.userdefined.operation").replaceAll("&","\n").replaceAll("=",":");
    String version = "2017-07-29";

    // PUT operation params
    String write = props.getProperty("document.dynamic.userdefined.x-ms-write");

    // Create signature string
    // HTTP Verb

    String signature = verb;
    if(verb.equals("GET")) {
        signature = signature + "\n\n\n\n\n\n\n\n\n\n\n";
        signature = signature + "\nx-ms-date:" + date;
        signature = signature + "\nx-ms-version:"+version;
        signature = signature + "\n/" + accountName + "/"+ path;
        if(!operation.equals(""))
            signature = signature + "\n" + operation;
    }
    // 'Create File' operation
    else if(verb.equals("PUT") && operation.equals("")) {
        /*
         PUT + "\n" +
         Content-Encoding + "\n" +
         Content-Language + "\n" +
         Content-Length + "\n" +
         Content-MD5 + "\n" +
         Content-Type + "\n" +
         Date + "\n" +
         If-Modified-Since + "\n" +
         If-Match + "\n" +
         If-None-Match + "\n" +
         If-Unmodified-Since + "\n" +
         Range + "\n" +
         CanonicalizedHeaders +
         CanonicalizedResource;
        */
        // Java 8
        // in Java 9 this would be byte[] content = is.readAllBytes()
        byte[] content = readAllBytes(is);
        long contentLength = content.size();
        logger.info("Length: "+contentLength.toString())
        signature = signature + "\n\n\n\n\napplication/binary\n\n\n\n\n\n";
        signature = signature + "\nx-ms-content-length:" + contentLength;
        signature = signature + "\nx-ms-date:" + date;
        signature = signature + "\nx-ms-type:file";
        signature = signature + "\nx-ms-version:"+version;
        signature = signature + "\n/" + accountName + "/"+ path;
        logger.info("Signature: "+signature)

        props.setProperty("document.dynamic.userdefined.x-ms-content-length", "" + contentLength);
    }
    // 'Put Range' operation
    else if(verb.equals("PUT") && operation.equals("comp:range")) {
        // Java 8
        // in Java 9 this would be byte[] content = is.readAllBytes()
        byte[] content = readAllBytes(is);
        long contentLength = content.size();

        signature = signature + "\n\n\n";

        long rangeEnd = 0;
        if(contentLength > 0) {
            signature = signature + contentLength;
            rangeEnd = contentLength-1;
        }

        signature = signature + "\n\napplication/binary\n\n\n\n\n\n";
        signature = signature + "\nx-ms-date:" + date;
        signature = signature + "\nx-ms-range:bytes=0-" + rangeEnd;
        signature = signature + "\nx-ms-version:"+version;
        signature = signature + "\nx-ms-write:update";
        signature = signature + "\n/" + accountName + "/"+ path;
        signature = signature + "\n" + operation;

        props.setProperty("document.dynamic.userdefined.x-ms-range", "bytes=0-" + rangeEnd);
        is = new ByteArrayInputStream(content);
    }
    Mac mac = Mac.getInstance("HmacSHA256");
    byte[] key = Base64.getDecoder().decode(accountKey);
    mac.init(new SecretKeySpec(key, "HmacSHA256"));
    String authKey = new String(Base64.getEncoder().encode(mac.doFinal(signature.getBytes("UTF-8"))));
    String auth = "SharedKey " + accountName + ":" + authKey;
    logger.info(auth)

    props.setProperty("document.dynamic.userdefined.Authorization", auth);
    props.setProperty("document.dynamic.userdefined.x-ms-date", date);

    logger.info(auth)
    logger.info(date)

    dataContext.storeStream(is, props);
}

public static byte[] readAllBytes(InputStream inputStream) throws IOException {
    final int bufLen = 1024;
    byte[] buf = new byte[bufLen];
    int readLen;
    IOException exception = null;

    try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
            outputStream.write(buf, 0, readLen);

        return outputStream.toByteArray();
    } catch (IOException e) {
        exception = e;
        throw e;
    } finally {
        if (exception == null) inputStream.close();
        else try {
            inputStream.close();
        } catch (IOException e) {
            exception.addSuppressed(e);
        }
    }
}

