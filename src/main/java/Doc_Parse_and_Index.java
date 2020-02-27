import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.lucene.analysis.standard.ClassicAnalyzer.STOP_WORDS_SET;

public class Doc_Parse_and_Index
{
    //Creates a List of book(i.e documents extracted from cran.all.1400)
    static List<Book> books = new ArrayList<>();
    private static void parse(BufferedReader br) throws IOException
    {
        String field = "";
        Book book = null;
        // readLine() returns the entire line read from the Buffered Reader until a line break is found
        String currentLine = br.readLine();
        // Reads each line till we reach end of the file
        while(currentLine != null)
        {
            // Checks if the current line is an Id, ie .I*
            if (currentLine.matches("(.I)( )(\\d*)"))
            {
                // Checks if the book is not null (i.e when atleast 1 book is created) and then adds the book to the list of books
                if(book != null)
                {
                    books.add(book);
                }
                // Creates an new instance of the book class whenever a Id is found
                book = new Book();
                // Splits the current like from the space to extract the document/book number
                book.setIndex(Integer.parseInt(currentLine.split(" ")[1]));
                // Sets the field to index
                field = "index";
            }
            //Checks if the current line is a .T
            // Assigns title to field variable
            // Setter method of the book class is called to set the title
            else if(currentLine.matches("(.T)") && book != null)
            {
                field = "title";
                book.setTitle("");
            }
            //Checks if the current line is a  .A
            // Assigns author to field variable
            // Setter method of the book class is called to set the author
            else if(currentLine.matches("(.A)") && book != null)
            {
                field = "author";
                book.setAuthor("");
            }
            //Checks if the current line is a  .B
            // Assigns author to field variable
            // Setter method of the book class is called to set bibliography
            else if(currentLine.matches("(.B)") && book != null)
            {
                field = "bibliography";
                book.setBibliography("");
            }
            //Checks if the current line is a  .W
            // Assigns author to field variable
            // Setter method of the book class is called to set word
            else if(currentLine.matches("(.W)") && book != null){
                field = "word";
                book.setWord("");
            }
            // When the current line is neither .T, .A, .B nor .W
            // It checks for the field type and calls the setter methods as well as the getter for that particular field type and adds the current line to it.
            else{
                switch(field){
                    case "title":
                        book.setTitle(book.getTitle()+currentLine+" ");
                        break;
                    case "author":
                        book.setAuthor(book.getAuthor()+currentLine+" ");
                        break;
                    case "bibliography":
                        book.setBibliography(book.getBibliography()+currentLine+" ");
                        break;
                    case "word":
                        book.setWord(book.getWord()+currentLine+" ");
                        break;
                    default:
                        break;
                }
            }
            // Reads the current line
            currentLine = br.readLine();
        }
        // This is done for the last piece of the document in the cran.1400.all file
        if(book != null){
            books.add(book);
        }
    }

    // Function that creates index for the document that is created by parsing cran.all.1400 text file
    private static void createIndex(List<Book> books) throws IOException
    {
        //Creating a list of StopWords
        List<String> stopWordList = Arrays.asList("a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from", "further", "had", "has", "have", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "it", "it's", "its", "itself", "let's", "me", "more", "most", "my", "myself", "nor", "of", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "she'd", "she'll", "she's", "should", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "we'd", "we'll", "we're", "we've", "were", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "would", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves");
        CharArraySet stopWordSet = new CharArraySet( stopWordList, true);
        // Analyzer breaks texts into indexed tokens and filters out unwanted words
        //Analyzer analyzer = new StandardAnalyzer(stopWordSet);
        Analyzer analyzer = new EnglishAnalyzer(stopWordSet);
        //Analyzer analyzer = new EnglishAnalyzer();
       // Analyzer analyzer = new StopAnalyzer(stopWordSet);
        //Analyzer analyzer = new ClassicAnalyzer(stopWordSet);
        // To store an index on disk
        // Path where we want index to be stored is given
        Directory directory = FSDirectory.open(Paths.get("src/main/resources/cran/index"));
        //IndexWriterConfig holds all configurations for Index Writer
        IndexWriterConfig config = new IndexWriterConfig(analyzer); //0.3602 // English Analyser : 0.3698
        config.setSimilarity(new BM25Similarity()); //0.3602 //With Pram : 0.3607 //0.3760
        //config.setSimilarity(new LMDirichletSimilarity()); // With Pram (5000/3000) : 0.2843 (1000) : 0.3230 //0.3092 //1500 : 0.3158
        //config.setSimilarity(new ClassicSimilarity());  //0.2405 //Vector Space Model
        //config.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7)); //With Parm : 0.1 : 0.3066 //0.5 : 0.3302 //0.7 : 0.3329 //0.9 : 0.3262 //0.3472 //With stop words0.3472

        // Index opening mode
        // IndexWriterConfig.OpenMode.CREATE = create a new index
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter iwriter = new IndexWriter(directory, config);

        // Create a new document
        for(Book b: books)
        {
            Document doc = new Document();
            doc.add(new TextField("Id",b.getIndex()+"",Field.Store.YES));
            doc.add(new TextField("Title", b.getTitle(), Field.Store.YES));
            doc.add(new TextField("Author", b.getAuthor(), Field.Store.YES));
            doc.add(new TextField("Bibliograpghy", b.getBibliography(), Field.Store.YES));
            doc.add(new TextField("Word", b.getWord(), Field.Store.YES));

            // Save the document to the index
            iwriter.addDocument(doc);
        }
        // Commit changes and close everything
        iwriter.close();
        directory.close();
    }


    public static void main(String[] a) throws IOException {
        //Assigning the path of the Cran.all.1400 document in file variable of datatype File.
        File file = new File("src/main/resources/cran/cran.all.1400");
        //Reading the text from the file
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        //Calling the parse function which parses the cran.all.1400 text file and passing the bufferReader as parameter to it
        parse(bufferedReader);
        // Calling the createIndex function to Index each document that we extracted from cran.all.1400
        // Books is a list which consists of each document( stored as book)
        createIndex(books);
        // To print the total number of documents that are created
        System.out.println(books.size());
        //Iterates through the list of books and then prints them by calling the function print book
        for(Book book: books){
            book.printBook();
        }
    }
}

// A book class is created which consists of properties like index,title,author,bibliography,word

class Book{
    private int index;
    private String title;
    private String author;
    private String bibliography;
    private String word;

    // A empty constructor is created
    // A constructor is a function with the same name as its class
    Book() {

    }
// A parameterized constructor is created
    Book(int index, String title, String author, String bibliography, String word)
    {
        this.index = index;
        this.title = title;
        this.author = author;
        this.bibliography = bibliography;
        this.word = word;
    }
//Getter methods for index,title,author,bibliography,word
    public int getIndex() {
        return index;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getBibliography() {
        return bibliography;
    }
    public String getWord() {
        return word;
    }

    //Setter methods for index,title,author,bibliography,word
    public void setIndex(int index) {
        this.index = index;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setBibliography(String bibliography) {
        this.bibliography = bibliography;
    }
    public void setWord(String word) {
        this.word = word;
    }

    // A function to print all the values present inside a book
    public void printBook(){
        System.out.println("Index: "+getIndex());
        System.out.println("Title: "+getTitle());
        System.out.println("Author: "+getAuthor());
        System.out.println("Bibliography: "+getBibliography());
        System.out.println("Word: "+getWord());
        System.out.println();
    }
}