# Search Engine implementation using Apache Lucene 8.4.1

First run the Doc_Parse_and_Index file.

This will create index files under `src/main/resources/cran/`.

Run Query_Parse_and_Search to parse queries and search them in the index created.

Results file will be created under `src/main/resources/cran/cran_results.txt`

Check the Accuracy of your search using Trec_eval :
1. Download the Trec_eval file from the internet.

2. Run this command in your command prompt/terminal, inside the folder in which trec_eval folder is placed:
  
    ./trec_eval-9.0.7/trec_eval ../QRelsCorrectedforTRECeval src/main/resources/cran/cran_results.txt
