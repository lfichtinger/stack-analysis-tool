import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Lisa on 05.07.2017.
 */
public class DocIdExtracter {

    public static void getDocIds(String filename, List<Integer> intTopics, String outFile, boolean append) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line = bufferedReader.readLine();
        List topics = new ArrayList<>();
        for (Integer t : intTopics) {
            topics.add(t.toString());
        }
        ;

        Map<String, String> docIds = new HashMap<String, String>();
        // get ids of docs that belong to the given topic on the first 3 places
        // other possibility woudl be to take the top 100 docs of each topic from file topic-docs.txt
        // but there are docs that occur 8-9 times in an topic, while other docs do not appear
        // in this approach, we look at each doc and select only by the topics - so we get all docs of an topic
        while ((line = bufferedReader.readLine()) != null) {
            String[] columns = line.split("\t");
            for (int i = 2; i < 8; i = i + 2) {
                int foundIdx = topics.indexOf(columns[i]);
                if (foundIdx != -1) {
                    // found
                    docIds.putIfAbsent(columns[1], "");
                }
            }
        }

        PrintWriter pw = new PrintWriter(new File(outFile));
        StringBuilder sb = new StringBuilder();
        for (String id : docIds.keySet()) {
            sb.append(id);
            sb.append(",");
        }
        sb.delete(sb.lastIndexOf(","), sb.lastIndexOf(",") + 1);
        if (append) {
            pw.append(sb);
        } else {
            pw.print(sb);
        }
        pw.flush();
        pw.close();
    }

    public static void getDocIdsByTopic(String filename, List<Integer> intTopics, String outFile, Boolean topicSeparated) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line = bufferedReader.readLine();
        List topics = new ArrayList<>();
        for (Integer t : intTopics) {
            topics.add(t.toString());
        }
        ;

        List[] topicIds = new List[topics.size()];
        // get ids of docs that belong to the given topic on the first 3 places
        // other possibility woudl be to take the top 100 docs of each topic from file topic-docs.txt
        // but there are docs that occur 8-9 times in an topic, while other docs do not appear
        // in this approach, we look at each doc and select only by the topics - so we get all docs of an topic
        while ((line = bufferedReader.readLine()) != null) {
            String[] columns = line.split("\t");
            for (int i = 2; i < 8; i = i + 2) {
                int foundIdx = topics.indexOf(columns[i]);
                if (foundIdx != -1) {
                    // found
                    if (topicIds[foundIdx] == null) {
                        topicIds[foundIdx] = new ArrayList<String>();
                    }
                    topicIds[foundIdx].add(columns[1]);
                }
            }
        }

        PrintWriter pw = new PrintWriter(new File(outFile));
        for (int i = 0; i < topicIds.length; i++) {
            StringBuilder sb = new StringBuilder();

            if (topicSeparated) {
                sb.append(topics.get(i));
                sb.append("\t");
            }

            Iterator<String> it = topicIds[i].iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                sb.append(",");
            }

            if (topicSeparated) {
                pw.println(sb);
            } else {
                pw.print(sb);
            }

            pw.flush();
        }
        pw.close();
    }

    public static void getFirst100DocsByTopic(String inFile, List<Integer> intTopics, String outFile, Boolean topicSeparated) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inFile));
        String line = bufferedReader.readLine();
        List topics = new ArrayList<>();
        for (Integer t : intTopics) {
            topics.add(t.toString());
        }
        ;

        List[] topicIds = new List[topics.size()];
        // other possibility woudl be to take the top 100 docs of each topic from file topic-docs.txt
        // but there are docs that occur 8-9 times in an topic, while other docs do not appear
        while ((line = bufferedReader.readLine()) != null) {
            // whitespace separated
            String[] columns = line.split(" ");
            int foundIdx = topics.indexOf(columns[0]);
            if (foundIdx != -1) {
                // found
                if (topicIds[foundIdx] == null) {
                    topicIds[foundIdx] = new ArrayList<String>();
                }
                topicIds[foundIdx].add(columns[2]);
            }
        }

        PrintWriter pw = new PrintWriter(new File(outFile));
        for (int i = 0; i < topicIds.length; i++) {
            StringBuilder sb = new StringBuilder();
            if (topicSeparated) {
                sb.append(topics.get(i));
                sb.append("\t");
            }

            Iterator<String> it = topicIds[i].iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                sb.append(",");
            }

            if (topicSeparated) {
                int idx = sb.lastIndexOf(",");
                sb.replace(idx, idx, "");
                pw.println(sb);
            } else {
                pw.print(sb);
            }
            pw.flush();
        }
        pw.close();
    }

    public static void main(String[] args) {

        String cordovaTopic;
        List<Integer> topics;

        //cordovaTopic = "t2";
        //topics = new ArrayList<Integer>(Arrays.asList(10,23,2,46,29,24,12,26,39,33,43,30,13,17,25,3,18,42,0,9,48,32,37,34,1,5,47,36,49,7,22,16,28,41,15));

        // cordovaTopic = "t10";
        // topics = new ArrayList<>(Arrays.asList(8,9,13,12,0,11,14,2,39,19,10,3,15,24,37,35,36,5,17));

        /*cordovaTopic = "t2";
        topics = new ArrayList<>(Arrays.asList(24,39,13,3,37,7,22,43,17,34,10,1));

        String docTopicsFile = "2017-07-06_doctopics-modeller-cordova/cordova-200-2000-1499160867210-" + cordovaTopic + "-50-2000-docs-topics.txt";
        try {
           // DocIdExtracter.getDocIdsByTopic(docTopicsFile, topics, "2017-07-06_doctopics-modeller-cordova/" + cordovaTopic + "-docIds.txt");
            DocIdExtracter.getDocIds(docTopicsFile, topics, "2017-07-06_doctopics-modeller-cordova/" + "comm" + "-docIds-only.txt", false);
        } catch (IOException e) {
            e.printStackTrace();
        } */

        /*
        Map<String, List<Integer>> commMap = new HashMap<String, List<Integer>>();
   //     commMap.put("t2", new ArrayList<>(Arrays.asList(24,39,13,3,37,7,22,43,17,34,10,1)));
        commMap.put("t2", new ArrayList<>(Arrays.asList(24,39,13,3,37,7,22,43,17,34,10,1)));
        commMap.put("t4", new ArrayList<>(Arrays.asList(23,13,32,36,46,28,33,42,27,29,45,25,47,38,24,1,16)));
        commMap.put("t5", new ArrayList<>(Arrays.asList(30,29)));
        commMap.put("t11", new ArrayList<>(Arrays.asList(41,2)));
        commMap.put("t17", new ArrayList<>(Arrays.asList(17,3,28,4,32,34,5,29)));
        commMap.put("t18", new ArrayList<>(Arrays.asList(29,20,32,14,7,9,41,42,22,40)));

        getDocsCommuncation(commMap); */

        //  getCordova(new ArrayList<>(Arrays.asList(2, 4, 35, 37, 40, 57, 59, 70, 80, 100, 109, 111, 123, 180, 183, 186)));


        Map<String, List<Integer>> metaCordova = new HashMap<String, List<Integer>>();
     /*  metaCordova.put("ssl", new ArrayList<>(Arrays.asList(40)));
        metaCordova.put("http", new ArrayList<>(Arrays.asList(18,165,198)));
        metaCordova.put("ajax", new ArrayList<>(Arrays.asList(186)));
        metaCordova.put("xhr", new ArrayList<>(Arrays.asList(57)));
        metaCordova.put("websocket,webrtc", new ArrayList<>(Arrays.asList(35)));
        metaCordova.put("network", new ArrayList<>(Arrays.asList(4,59,183)));
        metaCordova.put("cors", new ArrayList<>(Arrays.asList(180)));
        metaCordova.put("whitelist", new ArrayList<>(Arrays.asList(39,97)));
        metaCordova.put("browser", new ArrayList<>(Arrays.asList(11,48,64,95,116,143,150,179,196)));
        metaCordova.put("iframe", new ArrayList<>(Arrays.asList(131)));
        metaCordova.put("intent", new ArrayList<>(Arrays.asList(17,39,77,86,99,189)));
        metaCordova.put("permissions", new ArrayList<>(Arrays.asList(137)));
        metaCordova.put("database, storage", new ArrayList<>(Arrays.asList(66,184)));
        metaCordova.put("load", new ArrayList<>(Arrays.asList(2,10,42,47,49,54,94,115,147,151,177)));
        metaCordova.put("authentication", new ArrayList<>(Arrays.asList(45,167)));


        */

        // whitelist + content-security-policy + cors
        List<Integer> all = new ArrayList<>();
        for(int i=0; i < 200; i++) {
            metaCordova.put(i + "", new ArrayList<>(Arrays.asList(i)));
        }

       // getCordova(metaCordova);

        getTopicsHere("xamarin", metaCordova);
        getTopicsHere("react-native", metaCordova);

    }

    public static void getTopicsHere(String framework, Map<String, List<Integer>> topics) {
        String topicDocsFile = "2017-07-04_frameworks-200-2000/" + framework + "-null-200-2000-topic-docs.txt";
        try {
            for (String metaTopic : topics.keySet()) {
                String outFile = "2017-07-13-meta-topic-docs/" + framework + "-docs-" + metaTopic + ".txt";
                DocIdExtracter.getFirst100DocsByTopic(topicDocsFile, topics.get(metaTopic), outFile, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getDocsCommuncation(Map<String, List<Integer>> topics) {
        for (String topic : topics.keySet()) {
            String docTopicsFile = "2017-07-06_doctopics-modeller-cordova/cordova-200-2000-1499160867210-" + topic + "-50-2000-docs-topics.txt";
            try {
                DocIdExtracter.getDocIds(docTopicsFile, topics.get(topic), "2017-07-06_doctopics-modeller-cordova/" + "communication" + "-docIds-only1.txt", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
