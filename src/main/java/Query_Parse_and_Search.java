import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import static org.apache.lucene.analysis.standard.ClassicAnalyzer.STOP_WORDS_SET;

public class Query_Parse_and_Search
{
    // Path where index file is stored
    private static final String INDEX_DIR = "src/main/resources/cran/index";

    // Function to parse the query file, cran.qry
    private static void parse(BufferedReader br) throws Exception
    {
        // Assiging the result of createSearcher function to searcher variable of IndexSearcher
        IndexSearcher searcher = createSearcher();
        // Assigning the path of the result file to cran_result_file
        //File cran_result_file = new File("src/main/resources/cran/cran_results");
        PrintWriter writer = new PrintWriter("src/main/resources/cran/cran_results.txt","UTF-8");
        // Reading the current line
        String currentLine = br.readLine();
        // Stores the index of each query
        //String index = "";
        int index=0;
        boolean flag = false;
        //String builder is used to create mutable(modifiable) string
        //search is created which is of String builder type
        StringBuilder search = new StringBuilder();
        int i=0;
        while(currentLine != null)
        {

            if (currentLine.matches("(.I)( )(\\d*)"))
            {
                // This if will be executed when the first Index is not stored
                //if(!index.equals(""))
                if(index !=0)
                {
                    TopDocs topDocs = SearchByAll(search.toString().replace("*", "").replace("?", ""), searcher);
                    for (ScoreDoc sd : topDocs.scoreDocs)
                    {
                        Document d = searcher.doc(sd.doc);
                        System.out.println(index + "\t"+ "Q0"+ "\t" +String.format(d.get("Id")) + "\t" + i + "\t" + sd.score +"\t"+"STANDARD");
                        writer.println(index + "\t"+ "Q0"+ "\t" +String.format(d.get("Id")) + "\t" + i + "\t" + sd.score +"\t"+"STANDARD");
                        i++;
                    }

                }
                //Splits the .I to extract the number and stores it as index
                //index = currentLine.split(" ")[1];
                index=index+1;
                flag = false;
            }
            else if(currentLine.matches("(.W)"))
            {
                //Reads the current line and stores it into search
                search = new StringBuilder();
                // Changes the value of flag to true (stating that the query is witnessed)
                flag = true;
            }
            else {
                // If flag is true (i.e if .W is witnessed in the current line)
                if (flag)
                {
                    //Appends the current line to the search
                    // If the query is in two lines, search stores the previous line and current line stores the current line
                    // Then append current line to previous line(search)
                    search.append(currentLine + " ");
                }
            }
            // Reads the current line
            currentLine = br.readLine();
        }
        //int i =0;
        TopDocs topDocs = SearchByAll(search.toString().replace("*", "").replace("?", ""), searcher);
        for (ScoreDoc sd : topDocs.scoreDocs)
        {
            Document d = searcher.doc(sd.doc);
            System.out.println(index + "\t"+ "Q0"+ "\t" +String.format(d.get("Id")) + "\t" + i + "\t" + sd.score +"\t"+"STANDARD");
            writer.println(index + "\t"+ "Q0"+ "\t" +String.format(d.get("Id")) + "\t" + i + "\t" + sd.score +"\t"+"STANDARD");
            //System.out.println(index + "\t\t0\t\t" + String.format(d.get("Id")) + "\t\t" + sd.score);
            i++;
        }
        writer.close();
    }

    private static TopDocs SearchByAll(String search, IndexSearcher searcher) throws Exception
    {
        //Creating a list of StopWords
        List<String> stopWordList = Arrays.asList("a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from", "further", "had", "has", "have", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "it", "it's", "its", "itself", "let's", "me", "more", "most", "my", "myself", "nor", "of", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "she'd", "she'll", "she's", "should", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "we'd", "we'll", "we're", "we've", "were", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "would", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves");
        CharArraySet stopWordSet = new CharArraySet( stopWordList, true);
        // MultiFieldQueryParser is created to search acrosss multiple fields
        //MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[]{"Id", "Title", "Author", "Bibliography", "Word"}, new StandardAnalyzer(stopWordSet));
        MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[]{"Id", "Title", "Author", "Bibliography", "Word"}, new EnglishAnalyzer(stopWordSet));
        // MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[]{"Id", "Title", "Author", "Bibliography", "Word"}, new EnglishAnalyzer());
        ///MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[]{"Id", "Title", "Author", "Bibliography", "Word"}, new StopAnalyzer(stopWordSet));
        //MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[]{"Id", "Title", "Author", "Bibliography", "Word"}, new ClassicAnalyzer(stopWordSet));
        // parse the query with the parser (qp)
        Query query = qp.parse(search);
        //Collect enough docs to show
        TopDocs hits = searcher.search(query, 30);
        return hits;
    }

    private static IndexSearcher createSearcher() throws IOException
    {
        // Opens the folder that contains our search index
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        // create objects to read and search across the index
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity());
        //searcher.setSimilarity(new LMDirichletSimilarity());
        //searcher.setSimilarity(new ClassicSimilarity());
        //searcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
        return searcher;
    }

    public static void main(String[] args) throws Exception
    {
        //Assigning the path of the Cran.qry in the file variable., of datatype File.
        File file = new File("src/main/resources/cran/cran.qry");
        //Reading the text from the file
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        //Calling the parse function which parses the Cran.qry file and passing the bufferReader as parameter to it
        parse(bufferedReader);
    }
}