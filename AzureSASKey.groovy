import java.util.Properties;
import java.lang.*;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
/*
Get SAS Key and check the expiration date
*/
/*
import com.boomi.execution.ExecutionTask;
import com.boomi.execution.ExecutionUtil;
logger = ExecutionUtil.getBaseLogger();
*/

/* new encoder */
String.metaClass.encodeURL = {
   java.net.URLEncoder.encode(delegate, "UTF-8")
}
String.metaClass.decodeURL = {
   java.net.URLDecoder.decode(delegate, "UTF-8")
}
  Date date = new Date();
  def pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'";
 
  

/*
String SASKey = ExecutionUtil.getDynamicProcessProperty("DPP_WNS_AZURE_SASKEY");
*/
String SASKey="?sp=racwdlmeop&st=2021-06-09T07:22:18Z&se=2022-06-09T15:22:18Z&spr=https&sv=2020-02-10&sr=c&sig=H0tncpVx%2B%2FpzegwUndz1pKIhXh39fFZdTePVTTHk2jU%3D"
SASKey=SASKey.substring(1);
splitter=SASKey.split("&");
for (i=0; i < splitter.size();i++)
{
    splitter2 = splitter[i].split("=");
     switch (splitter2[0])
     {
         case "sv":
             signedversion=splitter2[1].decodeURL();
             println "signedversion="+signedversion;
             // logger.info("signedversion="+signedversion);
             break;
         case "st":
             signedstart=splitter2[1].decodeURL();
             def startDate = new Date().parse(pattern,signedstart);
             if (date < startDate) 
             {
                 println "SAS Key is not yet valid:"+startDate.format("yyyy-MM-dd hh:mm:ss");
                 // logger.info("SAS Key is not yet valid:"+startDate.format("yyyy-MM-dd hh:mm:ss"));
               }
             break;
         case "se":
             signedexpiry=splitter2[1].decodeURL();
             def expiryDate = new Date().parse(pattern, signedexpiry);
             println "SAS Key will expire in "+(expiryDate-date)+" days: "+expiryDate.format("yyyy-MM-dd hh:mm:ss");
             if (date > expiryDate) 
             {
                 println "SAS Key has been expired!: "+expiryDate.format("yyyy-MM-dd hh:mm:ss");
                 // logger.info("SAS Key has been expired:"+expiryDate.format("yyyy-MM-dd hh:mm:ss"));
             }
             if (date+7 > expiryDate) 
             {
                 println "SAS Key will be expired soon, please renew the SAS Key from WNS: "+expiryDate.format("yyyy-MM-dd hh:mm:ss");
                 // logger.info("SAS Key has been expired:"+expiryDate.format("yyyy-MM-dd hh:mm:ss"));
             }
             break;
         case "se":
             signedresource=splitter2[1].decodeURL();
             println "signedresource="+signedresource;
             // logger.info("signedresource="+signedresource);
             break;
         case "sp":
             signedpermissions=splitter2[1].decodeURL();
             println "signedpermissions="+signedpermissions;
             // logger.info("signedpermissions="+signedpermissions);
             break;
         case "sig":
             signature=splitter2[1].decodeURL();
             println "signature="+signature;
             // logger.info("signature="+signature);
             break;
     }
}
strAccess = "";
if (signedpermissions.contains('r')) strAccess = "Read ";
if (signedpermissions.contains('c')) strAccess = strAccess+"Create ";
if (signedpermissions.contains('w')) strAccess = strAccess+"Write ";
if (signedpermissions.contains('a')) strAccess = strAccess+"Append ";
if (signedpermissions.contains('d')) strAccess = strAccess+"Delete ";
if (signedpermissions.contains('x')) strAccess = strAccess+"Delete Version ";
if (signedpermissions.contains('l')) strAccess = strAccess+"List ";
if (signedpermissions.contains('t')) strAccess = strAccess+"Tag";
println strAccess
// logger.info("SAS Key Access:"+strAccess));
