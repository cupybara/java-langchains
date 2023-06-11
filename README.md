# ![](misc/logo.svg)

This repository aims to provide a java alternative to [hwchase17/langchain](https://github.com/hwchase17/langchain).
It was born from the need to create an enterprise QA application.

- [Dependency](#dependency)
- [Chains](#chains)
    - [Data](#data)
        - [Reader](#reader)
            - [Read Documents from PDF](#read-documents-from-pdf)
        - [Writer](#writer)
            - [Write Documents to Lucene Directory](#write-documents-to-lucene-directory)
    - [LLM](#llm)
        - [Azure](#azure)
            - [Azure Chat](#azure-chat)
            - [Azure Completions](#azure-completions)
        - [OpenAI](#openai)
            - [OpenAI Chat](#openai-chat)
            - [OpenAI Completions](#openai-completions)
    - [Retrieval](#retrieval)
        - [Retrieve Documents from Lucene Directory](#retrieve-documents-from-lucene-directory)
    - [QA](#qa)
        - [Modify Documents](#modify-documents)
        - [Combine Documents](#combine-documents)
        - [Map LLM results to answers with sources](#map-llm-results-to-answers-with-sources)
- [Use Cases](#use-cases)
    - [Retrieval Question-Answering Chain](#retrieval-question-answering-chain)

## Dependency
```xml
<dependency>
    <groupId>com.github.hakenadu</groupId>
    <artifactId>java-langchains</artifactId>
    <version>0.1.0</version>
</dependency>
```
(requires Java 8 or higher)

## Chains
Modular components implement the [Chain](src/main/java/com/github/hakenadu/javalangchains/chains/Chain.java) interface.
This provides an easy way to modularize the application and enables us to reuse them for various use cases.

This section describes the usage of all chains that are currently available.

### Data

#### Reader

##### Read Documents from PDF
```java
Stream<Map<String, String>> readDocuments = new ReadDocumentsFromPdfChain()
	.run(Paths.get("path/to/my/pdf/folder"))
	
// the readDocuments contains (content, source) pairs for all read pdfs (source is the pdf filename)
```

#### Writer

##### Write Documents to Lucene Directory
```java
// this chain reads documents from a folder of pdfs and writes them to an index directory
Chain<Path, Directory> createLuceneIndexChain = new ReadDocumentsFromPdfChain()
	.chain(new WriteDocumentsToLuceneDirectoryChain(tempIndexPath));

Path pdfDirectoryPath = Paths.get(RetrievalQaIT.class.getResource("/pdf").toURI());

Directory directory = createLuceneIndexChain.run(pdfDirectoryPath);
```

### LLM

#### Azure

##### Azure Chat
```java
AzureOpenAiChatCompletionsChain chain = new AzureOpenAiChatCompletionsChain(
	"my-azure-resource-name",
	"gpt-35-turbo", // deployment name
	"2023-05-15", // api version
	"Hello, this is ${name}", 
	new OpenAiChatCompletionsParameters(),
	System.getenv("OPENAI_API_KEY")
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
	new OpenAiCompletionsParameters(),
	System.getenv("OPENAI_API_KEY")
);

String result = chain.run(Collections.singletonMap("name", "Manuel"));
// the above outputs something like: "Hello Manuel, how are you"
```

#### OpenAI

##### OpenAI Chat
```java
OpenAiChatCompletionsChain chain = new OpenAiChatCompletionsChain(
	"Hello, this is ${name}", 
	new OpenAiChatCompletionsParameters().model("gpt-3.5-turbo"),
	System.getenv("OPENAI_API_KEY")
);

String result = chain.run(Collections.singletonMap("name", "Manuel"));
// the above outputs something like: "Hello Manuel, how are you"
```

#### OpenAI Completions
```java
OpenAiCompletionsChain chain = new OpenAiCompletionsChain(
	"Hello, this is ${name}", 
	new OpenAiCompletionsParameters().model("text-davinci-003"),
	System.getenv("OPENAI_API_KEY")
);

String result = chain.run(Collections.singletonMap("name", "Manuel"));
// the above outputs something like: "Hello Manuel, how are you"
```

### Retrieval

#### Retrieve Documents from Lucene Directory
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

### QA

#### Modify Documents
The ModifyDocumentsContentChain can be used for document summarization (for example).

```java
// create the llm chain which is used for summarization
LargeLanguageModelChain llmChain = new OpenAiChatCompletionsChain(
		PromptTemplates.QA_SUMMARIZE, 
		new OpenAiChatCompletionsParameters().temperature(0).model("gpt-3.5-turbo"),
		System.getenv("OPENAI_API_KEY"));

// create the ModifyDocumentsContentChain which is used to apply the llm chain to each passed document
ModifyDocumentsContentChain summarizeDocumentsChain = new ModifyDocumentsContentChain(llmChain);

// create some example documents
Map<String, String> myFirstDocument = new HashMap<String, String>();
myFirstDocument.put(PromptConstants.CONTENT, "this is my first document content");
myFirstDocument.put(PromptConstants.SOURCE, "this is my first document source");
// the default summarize prompt PromptTemplates.QA_SUMMARIZE also expects a question in the document
myFirstDocument.put(PromptConstants.QUESTION, "who is John Doe?");

Map<String, String> mySecondDocument = new HashMap<String, String>();
mySecondDocument.put(PromptConstants.CONTENT, "this is my second document content");
mySecondDocument.put(PromptConstants.SOURCE, "this is my second document source");

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

## Use Cases
Multiple chains can be chained together to create more powerful chains for complex use cases.

### Retrieval Question-Answering Chain 
The [following integration test](src/test/java/com/github/hakenadu/javalangchains/usecases/RetrievalQaIT.java) provides a comprehensive solution for an information retrieval and summarization task, with the aim to provide concise, informative and relevant answers from a large set of documents. It combines multiple processes into a Question-Answering (QA) chain, each responsible for a specific task.

```java
/*
 * take a look at src/test/resources/pdf of this repository
 * the pdf directory contains three documents about a fictional person named john doe
 * which we want to query using our retrieval based qa with sources chain
 */
Path pdfDirectoryPath = Paths.get(RetrievalQaIT.class.getResource("/pdf").toURI());

/*
 * We are creating and running an initializing chain which reads document from our pdf folder
 * and writes them to a lucene index directory
 */
Directory directory =  new ReadDocumentsFromPdfChain().chain(new WriteDocumentsToLuceneDirectoryChain()).run(pdfDirectoryPath);

// we got multiple OpenAI LLM Chains and define our parameters at first
OpenAiChatCompletionsParameters openAiChatParameters = new OpenAiChatCompletionsParameters()
		.temperature(0)
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