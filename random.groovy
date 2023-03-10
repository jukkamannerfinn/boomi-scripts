import java.text.DateFormat;
import java.text.SimpleDateFormat;


def z = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"]
Random rnd = new Random()
for (j=0; j < 10; j++) {
    y = "";
    for (i=0; i < 14; i++) {
       y = y + z[rnd.nextInt(z.size)];
    }
   //println y
}
println(y)
