import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lisa on 05.07.2017.
 */
public class DocsTopicsModeller extends Modeller {

    private static String queryById = "select p.id, " +
            " trim(p.data->>'body') " +
            " || array_to_string(array(select trim(elem->>'body'::text) from jsonb_array_elements(p.data->'comments') elem), ',', '')" +
            " || array_to_string(array(select trim(elem->>'body'::text) from jsonb_array_elements(p.data->'answers') elem), ',', '') " +
            " || array_to_string(array(select trim(elem->>'body'::text) from jsonb_array_elements(p.data->'answers') ans, jsonb_array_elements(ans->'comments') elem), ',', '') as text " +
            " from posts p " +
            " where p.id::text like any(?) " +
            " order by p.data->>'view_count' desc";

    private String filename;

    // contain docIds for each topic - index is topic name, value is list with doc names
    private List<String>[] topicIds;
    private List<String> topics;

    public DocsTopicsModeller(String filename, List<Integer> intTopics) throws IOException {
        this.filename = filename;
        readDocIdsFromFile(intTopics);
    }

    private void readDocIdsFromFile(List<Integer> intTopics) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line = bufferedReader.readLine();
        topics = new ArrayList<>();
        for(Integer t : intTopics) {
            topics.add(t.toString());
        };

        this.topicIds = new List[topics.size()];
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
    }

    public List<String>[] getTopicIds() {
        return topicIds;
    }



    @Override
    public void run(int[] numTopics, int[] numIterations, TopicModel model, String outPath) throws SQLException {
        PreparedStatement preparedStatement = Main.conn.prepareStatement(queryById);

        for(int j = 0; j < topicIds.length; j++) {
            if(topicIds[j] != null) {
                preparedStatement.setArray(1, Main.conn.createArrayOf("text", this.topicIds[j].toArray()));
                String outFile = outPath + "-t" + this.topics.get(j);
                model = createModel(preparedStatement, outFile);

                for(int ntop: numTopics) {
                    for(int niter: numIterations) {
                        try {
                            System.out.println("topicModel run, " + ntop + " topics, " + niter + " iterations");
                            model.run(ntop, niter, null, null, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }



}
