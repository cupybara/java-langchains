# ![](misc/logo.svg)
This repository aims to provide a java alternative to [hwchase17/langchain](https://github.com/hwchase17/langchain).
It was born from an initiative to create an enterprise QA application.

## Base Concept
Modular components implement the [Chain](src/main/java/com/github/hakenadu/javalangchain/chains/Chain.java) interface.
This provides an easy way to modularize the application and enables us to reuse them for various use cases.

## Use Cases
**W. I. P.**

### Retrieval based QA with sources
The one for which this project was created. Take a look at [RetrievalQaTest](src/test/java/com/github/hakenadu/javalangchain/chains/qa/RetrievalQaTest.java) for an example using the [LuceneRetrievalChain](src/main/java/com/github/hakenadu/javalangchain/chains/retrieval/lucene/LuceneRetrievalChain.java) for document retrieval and the [OpenAiChatChain](src/main/java/com/github/hakenadu/javalangchain/chains/llm/openai/OpenAiChatChain.java) for summarization and combined processing of relevant documents.