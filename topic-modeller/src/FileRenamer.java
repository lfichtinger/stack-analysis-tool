import java.io.File;
import java.io.FileDescriptor;

/**
 * Created by Lisa on 06.07.2017.
 */
public class FileRenamer {

    public static void main(String[] args) {
        File dir = new File("2017-07-06_doctopics-modeller-cordova");
        File[] allFiles = dir.listFiles();

        for(File f: dir.listFiles()) {
            String name = f.getName();
            if(name.startsWith("cordova")) {
                String[] parts = name.split("-");
                StringBuilder sb = new StringBuilder();
                sb.append("2017-07-06_doctopics-modeller-cordova/");
                for(int i=0; i < parts.length; i++) {
                    if(i != 7) {
                        sb.append(parts[i]);
                        if(i != parts.length-1) {
                            sb.append("-");
                        }
                    }
                }
                System.out.println(name);
                System.out.println(sb.toString());
                f.renameTo(new File(sb.toString()));
            }
        }

    }
}
