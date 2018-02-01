import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Lisa on 05.07.2017.
 */
public class FrameworkModeller extends Modeller {

    private String[] frameworks;
    private String modelInputFilename;
    private String keywords;

    public FrameworkModeller(String[] frameworks) {
        this.frameworks = frameworks;
        this.modelInputFilename = null;
    }

    public FrameworkModeller(String modelInputFilename) {
        this.frameworks = null;
        this.modelInputFilename = modelInputFilename;
    }

    private String query = "" +
            "select p.id, " +
            " trim(p.data->>'body') " +
            " || array_to_string(array(select trim(elem->>'body'::text) from jsonb_array_elements(p.data->'comments') elem), ',', '')" +
            " || array_to_string(array(select trim(elem->>'body'::text) from jsonb_array_elements(p.data->'answers') elem), ',', '') " +
            " || array_to_string(array(select trim(elem->>'body'::text) from jsonb_array_elements(p.data->'answers') ans, jsonb_array_elements(ans->'comments') elem), ',', '') as text " +
            " from posts p " +
            " where query_tags ilike all(?) and (p.data->>'view_count')::bigint > 100 " +
            " order by p.data->'view_count' desc ";

/*
    public TopicModel createModel(String framework) throws Exception {
        Statement stmt = Main.conn.createStatement();
        System.out.println("query database: ");
        ResultSet rs = stmt.executeQuery(quBodyCommentsAnswers(framework, null));

        TopicModel topicModel = new TopicModel(framework + "-");
        int rowCount = 0;
        while (rs.next()) {
            String bodyHtml = rs.getString("text");
            String bodyText = html2text(bodyHtml);
            bodyText = bodyText.trim().replaceAll("[\n],{1,}", " ");
            topicModel.addInstance(rs.getString("id"), bodyText, bodyHtml);
            rowCount++;
        }
        rs.close();
        return topicModel;
    }*/
/*
    public void run(TopicModel model, int numTopics, int numIterations) {
        numTopics = numTopics > 0 ? numTopics : 100;
        numIterations = numIterations > 0 ? numIterations : 2000;
        try {
            System.out.println("topicModel run, " + numTopics + " topics, " + numIterations + " iterations");
            model.run(numTopics, numIterations, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/



    @Override
    public void run(int[] numTopics, int[] numIterations, TopicModel model, String path) throws SQLException {
        PreparedStatement preparedStatement = null;
        if(this.modelInputFilename == null) {
            System.out.println("prepare statement");
            preparedStatement = Main.conn.prepareStatement(query);
            preparedStatement.setArray(1, Main.conn.createArrayOf("text", this.frameworks));
        }

        String outFile = path;
        model = createModel(preparedStatement, outFile);
        for (int ntop : numTopics) {
            for (int niter : numIterations) {
                try {
                    System.out.println("topicModel run, " + ntop + " topics, " + niter + " iterations");
                    model.run(ntop, niter, null, null, this.modelInputFilename);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
