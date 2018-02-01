import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.sql.ResultSet;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class TopicModel {

    private InstanceList instances;
    private String fileprefix;

    public TopicModel(String fileprefix) {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        InstanceList instances = null;
        this.fileprefix = fileprefix;

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());
        this.instances = new InstanceList(new SerialPipes(pipeList));
    }

    public void addInstance(String name, String value, String original) {
        this.instances.addThruPipe(new Instance(value, null, name, original));
    }

    public void run(int numTopics, int iterations, Double beta, Double alpha, String modelInputFilename) throws Exception {
        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        beta = beta != null ? beta.doubleValue() : 0.01;
        alpha = alpha != null ? alpha.doubleValue() : 50;
        ParallelTopicModel model = null;

        if (modelInputFilename != null) {
            // only read model and print its stats
            model = ParallelTopicModel.read(new File(modelInputFilename));
            printStatFiles(model, numTopics, iterations);
        } else {
            model = new ParallelTopicModel(numTopics, alpha, beta);
            model.addInstances(this.instances);

            // save model after estimation
            int interval = iterations > 100 ? 100 : iterations;
            model.setSaveSerializedModel(interval, this.fileprefix + "-" + numTopics + "-" + iterations + "-model");
            model.setSaveState(interval, this.fileprefix + "-" + numTopics + "-" + iterations + "-state");

            // Use two parallel samplers, which each look at one half the corpus and combine
            //  statistics after every iteration.
            model.setNumThreads(4);

            // Run the model for 50 iterations and stop (this is for testing only,
            //  for real applications, use 1000 to 2000 iterations)
            model.setNumIterations(iterations);
            model.setRandomSeed(12345);
            System.out.println("estimate");
            model.estimate();
            printStatFiles(model, numTopics, iterations);
        }
    }

    ;

    private void printStatFiles(ParallelTopicModel model, int numTopics, int iterations) throws FileNotFoundException {
        System.out.println("print stats");
        long timestamp = new Date().getTime();
        String prefix = this.fileprefix + "-" + numTopics + "-" + iterations + "-" + timestamp + "-";
        String postfix = ".txt";
        PrintWriter e1;
        System.out.println("TOP WORDS:");
        PrintStream ps1 = new PrintStream(new File(prefix + "top-words" + postfix));
        model.printTopWords(ps1, 10, false);
        ps1.flush();
        ps1.close();

        System.out.println("TOPICS DOCS:");
        e1 = new PrintWriter(prefix + "topic-docs" + postfix);
        model.printTopicDocuments(e1);
        e1.close();

        System.out.println("DOCS TOPICS:");
        e1 = new PrintWriter(prefix + "docs-topics" + postfix);
        //model.printDocumentTopics(e1);
        // first 100 topics, more than 5%
        model.printDocumentTopics(e1, 0.05, 200);
        e1.close();

        System.out.println("DIAGNOSTICS");
        e1 = new PrintWriter(prefix + "diagnostics" + postfix);
        TopicModelDiagnostics diagnostics = new TopicModelDiagnostics(model, 10);
        e1.print(diagnostics.toXML());
        e1.flush();
        e1.close();

        model.getTopicProbabilities(1);
    }


};
