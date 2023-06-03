# ![](misc/logo.svg)

- [Introduction](#introduction)
- [Dependency](#dependency)
- [Base Concept](#base-concept)
- [Chains](#chains)
    - [LLM](#llm)
        - [Azure](#azure)
            - [Azure Chat](#azure-chat)
            - [Azure Completions](#azure-completions)
        - [OpenAI](#openai)
            - [OpenAI Chat](#openai-chat)
            - [OpenAI Completions](#openai-completions)
- [Use Cases](#use-cases)
    - [Retrieval Question-Answering Chain](#retrieval-question-answering-chain)

## Introduction
This repository aims to provide a java alternative to [hwchase17/langchain](https://github.com/hwchase17/langchain).
It was born from the need to create an enterprise QA application.

## Dependency
```xml
<dependency>
    <groupId>com.github.hakenadu</groupId>
    <artifactId>java-langchains</artifactId>
    <version>0.0.3</version>
</dependency>
```

## Base Concept
Modular components implement the [Chain](src/main/java/com/github/hakenadu/javalangchains/chains/Chain.java) interface.
This provides an easy way to modularize the application and enables us to reuse them for various use cases.

## Chains
This section describes the usage of all chains that are currently available.

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

String result = chain.run(Collections.singletonMap("name", "Manuel")); // outputs something like: "Hello Manuel, how are you"
```

##### Azure Completions
```java
AzureOpenAiCompletionsChain chain = new AzureOpenAiCompletionsChain(
	"my-azure-resource-name",
	"gpt-35-turbo", // deployment name
	"2023-05-15", // api version
	"Hello, this is ${name}", 
	new OpenAiCompletionsParameters(),
	System.getenv("OPENAI_API_KEY")
);

String result = chain.run(Collections.singletonMap("name", "Manuel")); // outputs something like: "Hello Manuel, how are you"
```

#### OpenAI

##### OpenAI Chat
```java
OpenAiChatCompletionsChain chain = new OpenAiChatCompletionsChain(
	"Hello, this is ${name}", 
	new OpenAiChatCompletionsParameters().model("gpt-3.5-turbo"),
	System.getenv("OPENAI_API_KEY")
);

String result = chain.run(Collections.singletonMap("name", "Manuel")); // outputs something like: "Hello Manuel, how are you"
```

#### OpenAI Completions
```java
OpenAiCompletionsChain chain = new OpenAiCompletionsChain(
	"Hello, this is ${name}", 
	new OpenAiCompletionsParameters().model("text-davinci-003"),
	System.getenv("OPENAI_API_KEY")
);

String result = chain.run(Collections.singletonMap("name", "Manuel")); // outputs something like: "Hello Manuel, how are you"
```

### Retrieval

#### LuceneRetrievalChain
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

### Summarization

#### SummarizeDocumentsChain
```java
// create the llm chain which is used for summarization
LargeLanguageModelChain llmChain = new OpenAiChatCompletionsChain(
		PromptTemplates.QA_SUMMARIZE, 
		new OpenAiChatCompletionsParameters().temperature(0).model("gpt-3.5-turbo"),
		System.getenv("OPENAI_API_KEY"));

// create the SummarizeDocumentsChain which is used to apply the llm chain to each passed document
SummarizeDocumentsChain summarizeDocumentsChain = new SummarizeDocumentsChain(llmChain);

// create some example documents
Map<String, String> myFirstDocument = new HashMap<String, String>();
myFirstDocument.put(PromptConstants.CONTENT, "this is my first document content");
myFirstDocument.put(PromptConstants.SOURCE, "this is my first document source");

Map<String, String> mySecondDocument = new HashMap<String, String>();
mySecondDocument.put(PromptConstants.CONTENT, "this is my second document content");
mySecondDocument.put(PromptConstants.SOURCE, "this is my second document source");

// input for the summarize chain is a stream of documents
Stream<Map<String, String>> documents = Stream.of(myFirstDocument, mySecondDocument);

// output contains the passed documents with summarized content-Value
Stream<Map<String, String>> summarizedDocuments = summarizeDocumentsChain.run(documents);
```

## Use Cases

### Retrieval Question-Answering Chain 
The [following integration test](src/test/java/com/github/hakenadu/javalangchains/chains/qa/RetrievalQaIT.java) provides a comprehensive solution for an information retrieval and summarization task, with the aim to provide concise, informative and relevant answers from a large set of documents. It combines multiple processes into a Question-Answering (QA) chain, each responsible for a specific task.

The QA chain performs the following tasks:

1. **Document Retrieval**: This step is responsible for retrieving the most relevant documents related to a given query from a large collection. It uses an index-based search algorithm to find documents containing information related to the input query. This functionality can be facilitated by any `RetrievalChain` implementation. `LuceneRetrievalChain`, which utilizes the BM25 similarity metric, is just an example used in the test case.

2. **Document Summarization**: Once relevant documents are retrieved, they need to be summarized to extract the most essential information. The `SummarizeDocumentsChain` uses an instance of `LargeLanguageModelChain` for this task. In the provided example, OpenAI's GPT-3.5-turbo model via `OpenAiChatCompletionsChain` is used to reduce the information to its most relevant content.

3. **Document Combination**: The `CombineDocumentsChain` combines the summarized documents into a single prompt. This forms the input to the next stage of the process.

4. **Answer Generation**: The `OpenAiChatCompletionsChain` uses the combined prompt to generate a response. Any instance of `LargeLanguageModelChain` can be used for this step. In the given example, OpenAI's GPT-3.5-turbo model is utilized.

5. **Mapping and Answer Extraction**: Finally, the `MapAnswerWithSourcesChain` maps the string output to a complex object using a regular expression, which splits the answer from the sources of information. This provides a structured output that includes both the answer to the query and the sources from which the answer was derived.

In conclusion, the QA chain represents a comprehensive solution for document-based question-answering tasks, providing not only the most relevant answer but also citing the sources from which the information was retrieved. This chain is particularly useful in contexts where understanding the origin of information is as crucial as the answer itself.