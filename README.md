# ![](misc/logo.svg)

This repository aims to provide a java alternative to [hwchase17/langchain](https://github.com/hwchase17/langchain).
It was born from the need to create an enterprise QA application.

- [Dependency](#dependency)
- [Chains](#chains)
    - [Base](#base)
        - [Logging](#logging)
    - [Data](#data)
        - [Reader](#reader)
            - [Read Documents from In Memory PDF](#read-documents-from-in-memory-pdf)
            - [Read Documents from PDF](#read-documents-from-pdf)
        - [Retrieval](#retrieval)
            - [Retrieve Documents from Elasticsearch Index](#retrieve-documents-from-elasticsearch-index)
            - [Retrieve Documents from Lucene Directory](#retrieve-documents-from-lucene-directory)
			- [Retrieve Documents from a relational database](#retrieve-documents-from-rdbms)
        - [Writer](#writer)
            - [Write Documents to Elasticsearch Index](#write-documents-to-elasticsearch-index)
            - [Write Documents to Lucene Directory](#write-documents-to-lucene-directory)
    - [LLM](#llm)
        - [Azure](#azure)
            - [Azure Chat](#azure-chat)
            - [Azure Completions](#azure-completions)
        - [OpenAI](#openai)
            - [OpenAI Chat](#openai-chat)
            - [OpenAI Completions](#openai-completions)
    - [QA](#qa)
        - [Modify Documents](#modify-documents)
        - [Combine Documents](#combine-documents)
        - [Map LLM results to answers with sources](#map-llm-results-to-answers-with-sources)
        - [Split Documents](#split-documents)
- [Usage behind a corporate proxy](#usage-behind-a-corporate-proxy)
- [Use Cases](#use-cases)
    - [Document Comparison](#document-comparison)
    - [Retrieval Question-Answering Chain](#retrieval-question-answering-chain)

## Dependency
java-langchains requires Java 8 or higher


To group this repository with other related repositories in the future we lately transferred this repository to the freshly created organization [cupybara](https://github.com/cupybara).
Therefore we changed the package names from *com.github.hakenadu* to *io.github.cupybara* and also changed the groupId.
The latest artifact is therefore available by using the following dependency:

```xml
<dependency>
    <groupId>io.github.cupybara</groupId>
    <artifactId>java-langchains</artifactId>
    <version>0.6.3</version>
</dependency>
```

### deprecated older dependency

Packages up to version 0.5.0 are available using the groupId com.github.hakenadu.
These artifacts are not updates anymore so we don't suggest to use them.
Please switch to **io.github.cupybara**.

<details>
  <summary>old dependency</summary>
  
  ```xml
  <dependency>
      <groupId>com.github.hakenadu</groupId>
      <artifactId>java-langchains</artifactId>
      <version>0.5.0</version>
  </dependency>
  ```
</details>


## Chains
Modular components implement the [Chain](src/main/java/io/github/cupybara/javalangchains/chains/Chain.java) interface.
This provides an easy way to modularize the application and enables us to reuse them for various use cases.

This section describes the usage of all chains that are currently available.

### Base

#### Logging
The [LoggingChain](src/main/java/io/github/cupybara/javalangchains/chains/base/logging/LoggingChain.java) can be used to log the previous chain's output.
Take a look at the [RetrievalQaTest](src/test/java/io/github/cupybara/javalangchains/usecases/RetrievalQaTest.java) to see some example usages (logging chains indented for improved readability):

```java
final Chain<String, AnswerWithSources> qaChain = retrievalChain
		.chain(summarizeDocumentsChain)
			.chain(new ApplyToStreamInputChain<>(new LoggingChain<>(LoggingChain.defaultLogPrefix("SUMMARIZED_DOCUMENT"))))
		.chain(combineDocumentsChain)
			.chain(new LoggingChain<>(LoggingChain.defaultLogPrefix("COMBINED_DOCUMENT")))
		.chain(openAiChatChain)
			.chain(new LoggingChain<>(LoggingChain.defaultLogPrefix("LLM_RESULT")))
		.chain(mapAnswerWithSourcesChain);
```

The summarizeDocumentsChain in this example provides a Stream as an output. To log each item of the Stream the LoggingChain can be wrapped in an 
[ApplyToStreamInputChain](src/main/java/io/github/cupybara/javalangchains/chains/base/ApplyToStreamInputChain.java).

This example provides the following log output running the RetrievalQaTest:

```
========================================================================================================================================================
SUMMARIZED_DOCUMENT
========================================================================================================================================================
{source=book-of-john-1.pdf, question=who is john doe?, content=John Doe is a highly skilled and experienced software engineer with a passion for problem-solving and creating innovative solutions. He has been working in the technology industry for over 15 years and has gained a reputation for his exceptional programming abilities and attention to detail.}

========================================================================================================================================================
SUMMARIZED_DOCUMENT
========================================================================================================================================================
{source=book-of-john-3.pdf, question=who is john doe?, content=John Doe is described as someone with a diverse range of hobbies and interests. Some of his notable hobbies include music production, culinary adventures, photography and travel, fitness and outdoor activities, and being a book club enthusiast. He is also involved in volunteering and community service, language learning, gardening, DIY projects, and astronomy.}

========================================================================================================================================================
COMBINED_DOCUMENT
========================================================================================================================================================
{question=who is john doe?, content=Content: John Doe is described as someone with a diverse range of hobbies and interests. Some of his notable hobbies include music production, culinary adventures, photography and travel, fitness and outdoor activities, and being a book club enthusiast. He is also involved in volunteering and community service, language learning, gardening, DIY projects, and astronomy.
Source: book-of-john-3.pdf

Content: John Doe is a highly skilled and experienced software engineer with a passion for problem-solving and creating innovative solutions. He has been working in the technology industry for over 15 years and has gained a reputation for his exceptional programming abilities and attention to detail.
Source: book-of-john-1.pdf}

========================================================================================================================================================
LLM_RESULT
========================================================================================================================================================
John Doe is described as someone with a diverse range of hobbies and interests, including music production, culinary adventures, photography, travel, fitness, outdoor activities, being a book club enthusiast, volunteering, community service, language learning, gardening, DIY projects, and astronomy. Additionally, John Doe is a highly skilled and experienced software engineer with a passion for problem-solving and creating innovative solutions. He has been working in the technology industry for over 15 years and is known for his exceptional programming abilities and attention to detail.
SOURCES: book-of-john-3.pdf, book-of-john-1.pdf
```

### Data

#### Reader

##### Read Documents from In Memory PDF
See [ReadDocumentsFromInMemoryPdfChainTest](src/test/java/io/github/cupybara/javalangchains/chains/data/read/ReadDocumentsFromInMemoryPdfChainTest.java)

Read the in memory pdf into a single document

```java
InMemoryPdf inMemoryPdf = new InMemoryPdf(
	IOUtils.toByteArray(ReadDocumentsFromInMemoryPdfChainTest.class.getResourceAsStream("/pdf/qa/book-of-john-3.pdf")),
	"my-in-memory.pdf");
	
Stream<Map<String, String>> readDocuments = new ReadDocumentsFromInMemoryPdfChain().run(inMemoryPdf)
	
// the readDocuments contains a (pdfContent, "my-in-memory.pdf") pair
```

Read documents for each page of the in memory pdf

```java
InMemoryPdf inMemoryPdf = new InMemoryPdf(
	IOUtils.toByteArray(ReadDocumentsFromInMemoryPdfChainTest.class.getResourceAsStream("/pdf/qa/book-of-john-3.pdf")),
	"my-in-memory.pdf");
	
Stream<Map<String, String>> readDocuments = new ReadDocumentsFromInMemoryPdfChain(PdfReadMode.PAGES).run(inMemoryPdf)
	
// the readDocuments contains (content, source) pairs for all read pdf pages (source is "my-in-memory.pdf" + the pdf page number)
```

##### Read Documents from PDF
See [ReadDocumentsFromPdfChainTest](src/test/java/io/github/cupybara/javalangchains/chains/data/read/ReadDocumentsFromPdfChainTest.java)

Read each pdf in the given directory into a single document each

```java
Stream<Map<String, String>> readDocuments = new ReadDocumentsFromPdfChain()
	.run(Paths.get("path/to/my/pdf/folder"))
	
// the readDocuments contains (content, source) pairs for all read pdfs (source is the pdf filename)
```

Read each page of each pdf in the given directory into a single document each

```java
Stream<Map<String, String>> readDocuments = new ReadDocumentsFromPdfChain(PdfReadMode.PAGES)
	.run(Paths.get("path/to/my/pdf/folder"))
	
// the readDocuments contains (content, source) pairs for all read pdf pages (source is the pdf filename + the pdf page number)
```

#### Retrieval

##### Retrieve Documents from Elasticsearch Index
See [ElasticsearchRetrievalChainIT](src/test/java/io/github/cupybara/javalangchains/chains/data/retrieval/ElasticsearchRetrievalChainIT.java)

```java
RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost("localhost", 9200));

Chain<Path, Void> createElasticsearchIndexChain = new ReadDocumentsFromPdfChain()
	.chain(new WriteDocumentsToElasticsearchIndexChain("my-index", restClientBuilder));

Path pdfDirectoryPath = Paths.get(ElasticsearchRetrievalChainTest.class.getResource("/pdf/qa").toURI());

// create and fill elasticsearch index with read pdfs (source, content)-pairs
createElasticsearchIndexChain.run(pdfDirectoryPath);

// retrieve documents relevant to a specific question
try (RestClient restClient = restClientBuilder.build();
		ElasticsearchRetrievalChain retrievalChain = new ElasticsearchRetrievalChain("my-index", restClient, 1)) {

	// retrieve the most relevant documents for the passed question
	Stream<Map<String, String>> retrievedDocuments = retrievalChain.run("who is john doe?").collect(Collectors.toList());

	// ...
}
```

##### Retrieve Documents from Lucene Directory
See [LuceneRetrievalChainTest](src/test/java/io/github/cupybara/javalangchains/chains/data/retrieval/LuceneRetrievalChainTest.java)

```java
// create lucene index
Directory directory = new MMapDirectory(Files.createTempDirectory("myTempDir"));

// fill lucene index
try (IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
	List<String> documents = Arrays.asList("My first document", "My second document", "My third document");

	for (String content : documents) {
		Document doc = new Document();
		doc.add(new TextField(PromptConstants.CONTENT, content, Field.Store.YES));
		doc.add(new StringField(PromptConstants.SOURCE, String.valueOf(documents.indexOf(content) + 1), Field.Store.YES));
		indexWriter.addDocument(doc);
	}

	indexWriter.commit();
}

// create retrieval chain
RetrievalChain retrievalChain = new LuceneRetrievalChain(directory, 2 /* max count of retrieved documents */);

// retrieve the most relevant documents for the passed question
Stream<Map<String, String>> retrievedDocuments = retrievalChain.run("my question?");
```


##### Retrieve Documents from RDBMS
See [JdbcRetrievalChainIT](src/test/java/io/github/cupybara/javalangchains/chains/data/retrieval/JdbcRetrievalChainIT.java)

```java
Supplier<Connection> connectionSupplier = () -> {
	try {
		return DriverManager.getConnection(connectionString, username, password);
	} catch (SQLException e) {
		throw new IllegalStateException("error creating database connection", e);
	}
};

RetrievalChain retrievalChain = new JdbcRetrievalChain(connectionSupplier, 2 /* max count of retrieved documents */);

Stream<Map<String, String>> retrievedDocuments = retrievalChain.run("my question?");
```

#### Writer

##### Write Documents to Elasticsearch Index
```java
RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost("localhost", 9200));

// this chain reads documents from a folder of pdfs and writes them to an elasticsearch index
Chain<Path, Void> fillElasticsearchIndexChain = new ReadDocumentsFromPdfChain()
  .chain(new WriteDocumentsToElasticsearchIndexChain("my-index", restClientBuilder));

Path pdfDirectoryPath = Paths.get(getClass().getResource("/pdf/qa").toURI());

fillElasticsearchIndexChain.run(pdfDirectoryPath);
```

##### Write Documents to Lucene Directory
```java
Path tempIndexPath = Files.createTempDirectory("lucene")

// this chain reads documents from a folder of pdfs and writes them to an index directory
Chain<Path, Directory> createLuceneIndexChain = new ReadDocumentsFromPdfChain()
	.chain(new WriteDocumentsToLuceneDirectoryChain(tempIndexPath));

Path pdfDirectoryPath = Paths.get(getClass().getResource("/pdf/qa").toURI());

Directory directory = createLuceneIndexChain.run(pdfDirectoryPath);
```

### LLM

#### Azure

##### Azure Chat
See [AzureOpenAiChatCompletionsChainIT](src/test/java/io/github/cupybara/javalangchains/chains/llm/azure/chat/AzureOpenAiChatCompletionsChainIT.java)

```java
AzureOpenAiChatCompletionsChain chain = new AzureOpenAiChatCompletionsChain(
	"my-azure-resource-name",
	"gpt-35-turbo", // deployment name
	"2023-05-15", // api version
	"Hello, this is ${name}", 
	new OpenAiChatCompletionsParameters().temperature(0D), // also allows to set more parameters
	System.getenv("OPENAI_API_KEY"),
	"You are a helpful assistant who answers questions to ${name}" // optional systemTemplate 
);

String result = chain.run(Collections.singletonMap("name", "Manuel")); 
// the above outputs something like: "Hello Manuel, how are you"
```

##### Azure Completions
```java
AzureOpenAiCompletionsChain chain = new AzureOpenAiCompletionsChain(
	"my-azure-resource-name",
	"text-davinci-003", // deployment name
	"2023-05-15", // api version
	"Hello, this is ${name}", 
	new OpenAiCompletionsParameters().temperature(0D), // also allows to set more parameters
	System.getenv("OPENAI_API_KEY"),
	"You are a helpful assistant who answers questions to ${name}" // optional systemTemplate 
);

String result = chain.run(Collections.singletonMap("name", "Manuel"));
// the above outputs something like: "Hello Manuel, how are you"
```

#### OpenAI

##### OpenAI Chat
See [OpenAiChatCompletionsChainIT](src/test/java/io/github/cupybara/javalangchains/chains/llm/openai/chat/OpenAiChatCompletionsChainIT.java)

```java
OpenAiChatCompletionsChain chain = new OpenAiChatCompletionsChain(
	"Hello, this is ${name}", 
	new OpenAiChatCompletionsParameters().model("gpt-3.5-turbo").temperature(0D), // also allows to set more parameters
	System.getenv("OPENAI_API_KEY"),
	"You are a helpful assistant who answers questions to ${name}" // optional systemTemplate 
);

String result = chain.run(Collections.singletonMap("name", "Manuel"));
// the above outputs something like: "Hello Manuel, how are you"
```

#### OpenAI Completions
```java
OpenAiCompletionsChain chain = new OpenAiCompletionsChain(
	"Hello, this is ${name}", 
	new OpenAiCompletionsParameters().model("text-davinci-003").temperature(0D), // also allows to set more parameters
	System.getenv("OPENAI_API_KEY"),
	"You are a helpful assistant who answers questions to ${name}" // optional systemTemplate 
);

String result = chain.run(Collections.singletonMap("name", "Manuel"));
// the above outputs something like: "Hello Manuel, how are you"
```

### QA

#### Modify Documents
The ModifyDocumentsContentChain can be used for document summarization (for example).

```java
// create the llm chain which is used for summarization
LargeLanguageModelChain llmChain = new OpenAiChatCompletionsChain(
		PromptTemplates.QA_SUMMARIZE, 
		new OpenAiChatCompletionsParameters().temperature(0D).model("gpt-3.5-turbo"),
		System.getenv("OPENAI_API_KEY"));

// create the ModifyDocumentsContentChain which is used to apply the llm chain to each passed document
ModifyDocumentsContentChain summarizeDocumentsChain = new ModifyDocumentsContentChain(llmChain);

// create some example documents
Map<String, String> myFirstDocument = new HashMap<String, String>();
myFirstDocument.put(PromptConstants.CONTENT, "this is my first document content");
myFirstDocument.put(PromptConstants.SOURCE, "this is my first document source");
// the default summarize prompt PromptTemplates.QA_SUMMARIZE also expects the question used for retrieval in the document
myFirstDocument.put(PromptConstants.QUESTION, "who is John Doe?");

Map<String, String> mySecondDocument = new HashMap<String, String>();
mySecondDocument.put(PromptConstants.CONTENT, "this is my second document content");
mySecondDocument.put(PromptConstants.SOURCE, "this is my second document source");
mySecondDocument.put(PromptConstants.QUESTION, "how old is John Doe?"); // see comment above

// input for the summarize chain is a stream of documents
Stream<Map<String, String>> documents = Stream.of(myFirstDocument, mySecondDocument);

// output contains the passed documents with summarized content-Value
Stream<Map<String, String>> summarizedDocuments = summarizeDocumentsChain.run(documents);
```

#### Combine Documents
```java
CombineDocumentsChain combineDocumentsChain = new CombineDocumentsChain();

Map<String, String> myFirstDocument = new HashMap<String, String>();
myFirstDocument.put(PromptConstants.CONTENT, "this is my first document content");
myFirstDocument.put(PromptConstants.SOURCE, "this is my first document source");

Map<String, String> mySecondDocument = new HashMap<String, String>();
mySecondDocument.put(PromptConstants.CONTENT, "this is my second document content");
mySecondDocument.put(PromptConstants.SOURCE, "this is my second document source");

Stream<Map<String, String>> documents = Stream.of(myFirstDocument, mySecondDocument);

Map<String, String> combinedDocument = combineDocumentsChain.run(documents);
/* 
 * Content: this is my first document content
 * Source: this is my first document source
 *
 * Content: this is my second document content
 * Source: this is my second document source
 * 
 * (stored with key "content" inside the map)
 */
```

#### Map LLM results to answers with sources
```java
MapAnswerWithSourcesChain mapAnswerWithSourcesChain = new MapAnswerWithSourcesChain();

AnswerWithSources answerWithSources = mapAnswerWithSourcesChain.run("The answer is bla bla bla.\nSOURCES: page 1 book xy, page 2 book ab");

System.out.println(answerWithSources.getAnswer());  // The answer is bla bla bla.
System.out.println(answerWithSources.getSources()); // [page 1 book xy, page 2 book ab]

```

#### Split Documents
See [SplitDocumentsChainTest](src/test/java/io/github/cupybara/javalangchains/chains/qa/split/SplitDocumentsChainTest.java)

```java

// 1. Create Documents

List<Map<String, String>> documents = new LinkedList<>();

Map<String, String> firstDocument = new LinkedHashMap<>();
firstDocument.put(PromptConstants.SOURCE, "book of john");
firstDocument.put(PromptConstants.CONTENT, "This is a short text. This is another short text.");
documents.add(firstDocument);

Map<String, String> secondDocument = new LinkedHashMap<>();
secondDocument.put(PromptConstants.SOURCE, "book of jane");
secondDocument.put(PromptConstants.CONTENT, "This is a short text.");
documents.add(secondDocument);

// 2. Split Documents

/*
 * We create a TextSplitter that splits a text into partitions using a JTokkit
 * Encoding. We use the cl100k_base encoding (which btw is the default for
 * gpt-3.5-turbo)
 */
TextSplitter textSplitter = new JtokkitTextSplitter(
		Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE), 10);

/*
 * we now instantiate the SplitDocumentsChain which will split our documents
 * using the above created TextSplitter on the "content" field.
 */
SplitDocumentsChain splitDocumentsChain = new SplitDocumentsChain(textSplitter);

List<Map<String, String>> splitDocuments = splitDocumentsChain.run(documents.stream())
		.collect(Collectors.toList());

// splitDocuments: [
//   {content=This is a short text. , source=book of john},
//   {content=This is another short text., source=book of john},
//   {content=This is a short text., source=book of jane}
// ]
```

## Usage behind a corporate proxy
If a chain needs to access to an external service, there will be a constructor parameter for passing the http client.
The [WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html) is used for the following chains:
* [AzureOpenAiChatCompletionsChain](src/main/java/io/github/cupybara/javalangchains/chains/llm/azure/chat/AzureOpenAiChatCompletionsChain.java)
* [AzureOpenAiCompletionsChain](src/main/java/io/github/cupybara/javalangchains/chains/llm/azure/completions/AzureOpenAiCompletionsChain.java)
* [OpenAiChatCompletionsChain](src/main/java/io/github/cupybara/javalangchains/chains/llm/openai/chat/OpenAiChatCompletionsChain.java)
* [OpenAiCompletionsChain](src/main/java/io/github/cupybara/javalangchains/chains/llm/openai/completions/OpenAiCompletionsChain.java)

There exists plenty of public documentation on how to configure a http proxy for those cases.
One example is [this one from Baeldung](https://www.baeldung.com/spring-webflux-timeout).

For accessing an Elasticsearch cluster the [Elasticsearch Low Level Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/java-rest-low.html) is used.
The [official documentation](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.8/java-rest-low-usage-initialization.html) shows how to use a proxy in this case. 

## Use Cases
Multiple chains can be chained together to create more powerful chains for complex use cases.

### Document Comparison
The [following unit test](src/test/java/io/github/cupybara/javalangchains/usecases/DocumentComparisonTest.java) shows how the existing chains may be used to compare 2 or more documents.
I guess more abstraction would be useful. I will target that in one of the next releases and then also include example code in this README.

The following diagram shows how the implementation for this usecase works:

![](misc/drawio/docment-comparison.svg)


### Retrieval Question-Answering Chain 
The [following unit test](src/test/java/io/github/cupybara/javalangchains/usecases/RetrievalQaTest.java) provides a comprehensive solution for an information retrieval and summarization task, with the aim to provide concise, informative and relevant answers from a large set of documents. It combines multiple processes into a Question-Answering (QA) chain, each responsible for a specific task.

```java
/*
 * take a look at src/test/resources/pdf of this repository
 * the pdf directory contains three documents about a fictional person named john doe
 * which we want to query using our retrieval based qa with sources chain
 */
Path pdfDirectoryPath = Paths.get(RetrievalQaTest.class.getResource("/pdf/qa").toURI());

/*
 * We are creating and running an initializing chain which reads document from our pdf folder
 * and writes them to a lucene index directory
 */
Directory directory =  new ReadDocumentsFromPdfChain().chain(new WriteDocumentsToLuceneDirectoryChain()).run(pdfDirectoryPath);

// we got multiple OpenAI LLM Chains and define our parameters at first
OpenAiChatCompletionsParameters openAiChatParameters = new OpenAiChatCompletionsParameters()
		.temperature(0D)
		.model("gpt-3.5-turbo");

/*
 * Chain 1: The retrievalChain is used to retrieve relevant documents from an
 * index by using bm25 similarity
 */
try (LuceneRetrievalChain retrievalChain = new LuceneRetrievalChain(directory /* implies a filled lucene directory */, 2)) {

	/*
	 * Chain 2: The summarizeDocumentsChain is used to summarize documents to only
	 * contain the most relevant information. This is achieved using an OpenAI LLM
	 * (gpt-3.5-turbo in this case)
	 */
	ModifyDocumentsContentChain summarizeDocumentsChain = new ModifyDocumentsContentChain(new OpenAiChatCompletionsChain(
			PromptTemplates.QA_SUMMARIZE, openAiChatParameters, System.getenv("OPENAI_API_KEY")));

	/*
	 * Chain 3: The combineDocumentsChain is used to combine the retrieved documents
	 * in a single prompt
	 */
	CombineDocumentsChain combineDocumentsChain = new CombineDocumentsChain();

	/*
	 * Chain 4: The openAiChatChain is used to process the combined prompt using an
	 * OpenAI LLM (gpt-3.5-turbo in this case)
	 */
	OpenAiChatCompletionsChain openAiChatChain = new OpenAiChatCompletionsChain(PromptTemplates.QA_COMBINE,
			openAiChatParameters, System.getenv("OPENAI_API_KEY"));

	/*
	 * Chain 5: The mapAnswerWithSourcesChain is used to map the llm string output
	 * to a complex object using a regular expression which splits the sources and
	 * the answer.
	 */
	MapAnswerWithSourcesChain mapAnswerWithSourcesChain = new MapAnswerWithSourcesChain();

	// we combine all chain links into a self contained QA chain
	Chain<String, AnswerWithSources> qaChain = retrievalChain
		.chain(summarizeDocumentsChain)
		.chain(combineDocumentsChain)
		.chain(openAiChatChain)
		.chain(mapAnswerWithSourcesChain);

	// the QA chain can now be called with a question and delivers an answer
	AnswerWithSources answerWithSources = qaChain.run("who is john doe?");
	
	/*
	 * answerWithSources.getAnwswer() provides the answer to the question based on the retrieved documents
	 * answerWithSources.getSources() provides a list of source strings for the retrieved documents
	 */
}
```

The QA chain performs the following tasks:

1. **Document Retrieval**: This step is responsible for retrieving the most relevant documents related to a given query from a large collection. It uses an index-based search algorithm to find documents containing information related to the input query. This functionality can be facilitated by any `RetrievalChain` implementation. `LuceneRetrievalChain`, which utilizes the BM25 similarity metric, is just an example used in the test case.

2. **Document Summarization**: Once relevant documents are retrieved, they need to be summarized to extract the most essential information. The `SummarizeDocumentsChain` uses an instance of `LargeLanguageModelChain` for this task. In the provided example, OpenAI's GPT-3.5-turbo model via `OpenAiChatCompletionsChain` is used to reduce the information to its most relevant content.

3. **Document Combination**: The `CombineDocumentsChain` combines the summarized documents into a single prompt. This forms the input to the next stage of the process.

4. **Answer Generation**: The `OpenAiChatCompletionsChain` uses the combined prompt to generate a response. Any instance of `LargeLanguageModelChain` can be used for this step. In the given example, OpenAI's GPT-3.5-turbo model is utilized.

5. **Mapping and Answer Extraction**: Finally, the `MapAnswerWithSourcesChain` maps the string output to a complex object using a regular expression, which splits the answer from the sources of information. This provides a structured output that includes both the answer to the query and the sources from which the answer was derived.

In conclusion, the QA chain represents a comprehensive solution for document-based question-answering tasks, providing not only the most relevant answer but also citing the sources from which the information was retrieved. This chain is particularly useful in contexts where understanding the origin of information is as crucial as the answer itself.