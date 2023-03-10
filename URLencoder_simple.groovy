import java.util.Properties;
import java.lang.*;
import java.io.InputStream;
import java.nio.file.* ;
import java.nio.file.attribute.* ;

/* new encoder */
String.metaClass.encodeURL = {
   java.net.URLEncoder.encode(delegate, "UTF-8")
}
/* new decoder */
String.metaClass.decodeURL = {
   java.net.URLDecoder.decode(delegate, "UTF-8")
}


//exaample text
// text = "FI101S851204 / Finnair/Grönfors"; 
text = "https://finnair.coupahost.com/api/suppliers/%3Fcustom_fields%5Bovt_number%5D%3D0010438464%26status%3Dactive"
text = "ID_230988_AYBJSKC????_(18).xls"
println text;
//
queryString = text.encodeURL();
println queryString;
queryString = queryString.replaceAll("\\+","%20"); // if necessary?
println queryString;
queryString = text.decodeURL();
println queryString;