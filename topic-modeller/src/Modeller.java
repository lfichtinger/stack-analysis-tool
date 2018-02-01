import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Lisa on 05.07.2017.
 */
public abstract class Modeller {


    public abstract void run(int[] numTopics, int[] numIterations, TopicModel model, String path) throws SQLException;

    public String html2text(String html) {
        Document doc = Jsoup.parse(html);
        String t = doc.text();
        int c = 0;
        while (t.contains("<div>") || t.contains("&lt;div&gt;")) {
            // still html tags in text - e.g. code sections
            Document doc1 = Jsoup.parse(t);
            t = doc1.text();
            c++;
            if (c > 3) {
                System.out.println("html2text " + c);
            }
        }
        return t;
    }

    protected TopicModel createModel(PreparedStatement preparedStatement, String outFile) throws SQLException {
        TopicModel model = new TopicModel(outFile);
        if (preparedStatement != null) {
            // load data from db and create model instances
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String bodyHtml = rs.getString("text");
                String bodyText = html2text(bodyHtml);
                bodyText = bodyText.trim().replaceAll("[\n],{1,}", " ");
                model.addInstance(rs.getString("id"), bodyText, bodyHtml);
            }
            rs.close();
        }
        return model;
    }

}
