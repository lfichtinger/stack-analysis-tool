import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Main {

    public static Connection conn = null;

    public static void main(String[] args) {

        InputStream inp = null;
        Properties prop = new Properties();

        try {
            inp = new FileInputStream("config.properties");
            prop.load(inp);

            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://" + prop.getProperty("host") + ":" + prop.getProperty("port") + "/" + prop.getProperty("db"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
            System.out.println("Opened database");
            conn.setAutoCommit(false);

            //  Modeller cordovaMod = new FrameworkModeller("2017-07-27-framework-diag/cordova3-10-10-model.10");

           // Modeller xamarinMod = new FrameworkModeller(new String[]{"xamarin"});
            //xamarinMod.run(new int[]{200}, new int[]{2000}, null, "2017-07-27-framework-diag/xamarin");

           // Modeller reactMod = new FrameworkModeller(new String[]{"react-native"});
            //reactMod.run(new int[]{200}, new int[]{2000}, null, "2017-07-27-framework-diag/react-native");



            // selected ''security'' relevant topics from cordova results
           // List<Integer> topics = new ArrayList<Integer>(Arrays.asList(11, 112, 111, 114, 42, 2, 81, 194, 186, 45, 180, 179, 196, 91, 130, 5, 121, 62, 24, 39, 66, 137, 143, 61, 47, 80, 150, 184, 189, 17, 165, 70, 22, 183, 123, 63, 4, 90, 68, 54, 138, 41, 21, 131, 18, 100, 35, 55, 77, 82, 132, 106, 109, 135, 188, 176, 37, 167, 10, 59, 141, 53, 86, 79, 9, 129, 57, 40, 58));
           // Modeller modeller = new DocsTopicsModeller("cordova-null-200-2000-1499160867210-docs-topics.txt", topics);
          //  modeller.run(new int[]{50}, new int[]{2000}, null);

            // selected relevant topics from xamarin results
          //  List<Integer> xamarinTopics = new ArrayList<Integer>(Arrays.asList(6,7,9,11,16,17,22,23,25,26,37,39,43,45,48,53,54,56,57,59,63,65,66,74,75,90,92,97,102,106,108,112,113,114,115,119,124,127,132,135,138,140,150,151,161,162,163,166,167,175,176,177,179,185,191,192,193,197));
          //  Modeller xamarinModeller = new DocsTopicsModeller("xamarin-null-200-2000-1499165759220-docs-topics.txt", xamarinTopics);
           // xamarinModeller.run(new int[]{50}, new int[]{2000}, null, "doctopics-modeller-xamarin/xamarin-200-2000-1499165759220");

            // selected relevant topics from xamarin results
         /*   List<Integer> reactnativeTopics = new ArrayList<Integer>(Arrays.asList(0,1,13,19,20,21,25,33,36,38,40,44,45,55,56,61,78,81,86,93,97,100,102,105,107,108,110,114,116,120,121,123,124,126,128,129,131,134,135,144,146,157,163,164,166,170,172,174,175,176,177,183,184,189,198));
            Modeller reactnativeModeller = new DocsTopicsModeller("react-native-null-200-2000-1499166054905-docs-topics.txt", reactnativeTopics);
            reactnativeModeller.run(new int[]{50}, new int[]{2000}, null, "2017-07-06_doctopics-modeller-reactnative/reactnative-200-2000-1499166054905");
*/

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
