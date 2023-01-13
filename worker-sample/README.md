# Sample implementation of your Workflow

This project contains an example of the implementation of a **Workflow** generated through the configuration of the process in the KuFlow application. This is a very simple code whose purpose is to show the creation of a **Worker** that implements a business process. KuFlow is an implementation language agnostic Cloud platform, but this implementation is an example integration in JAVA.

The provided **Workflow** orchestrates the KuFlow tasks defined by you in the application in a sequential way. That is, to start a task, the previous task must be completed. The candidates in charge of carrying out these tasks are the ones that you configure in the KuFlow application. For more info, see [Configure Task Candidates](https://docs.kuflow.com/how-to/task-candidates).

In a similar way, you must also configure which candidates are the ones that can start the process within your organization (for more info, see [Configure Process Permissions](https://docs.kuflow.com/how-to/process-permissions), as well as which Worker will be in charge of implementing your business process.

In the following documentation we will guide you step by step in the execution of your **Workflow** in a satisfactory way.

## Prerequisites

We are happy

- Maven
  - To build the project (>=3.8.1)
- JDK
  - Java 17

## Required configuration

Open the file `application-fillme.yml` and fill the properties values marked with a "fill_me" text. All the values should be obtained from the "Management>Application" section of KuFlow, for more info, see [Create an Application](https://docs.kuflow.com/how-to/applications)

#### kuflow.api.application-id

In the "_Workflow Application_" field of your Process definition in KuFlow, you need to configure, if you haven't already done, a KuFlow Application. In this property you must set the identifier of that application.

```yaml
kuflow.api.application-id: f28c7940-c355-46c3-a540-09b774c00a20 # Example
```

#### kuflow.api.token

When you created the app, in addition to the identifier, you got an access token. You need to set that token here.

```yaml
kuflow.api.token: "mdKK3k6k2)hn*s98" # Example
```

#### application.temporal.mutual-tls.cert-data

In this property you must configure the certificate that you have configured in KuFlow (**Certificate**).

```yaml
application.temporal.mutual-tls.cert-data: |
  -----BEGIN CERTIFICATE-----
  ................................................................
  -----END CERTIFICATE-----
```

#### application.temporal.key-data

In this property you must configure the private key of the certificate that you have configured in KuFlow (**Private Key**). Important, this code example expects PKCS8 encoding, make sure you have selected this format in KuFlow.

```yaml
application.temporal.key-data: |
  -----BEGIN PRIVATE KEY-----
  ................................................................
  -----END PRIVATE KEY-----
```

#### application.temporal.ca-data

In this property you must configure the certificate of the KuFlow certification authority (**CA Certificate**).

```yaml
application.temporal.ca-data: |
  -----BEGIN CERTIFICATE-----
  ................................................................
  -----END CERTIFICATE-----
```

## Run

Next, you must start the Worker. Normally, you will do this through the IDE. But if you prefer the console line, in the repository root folder type:

```bash
mvn spring-boot:run
```

## Generated code guide

`SampleWorkflow.java`

> The Java interface that defines our Workflow. Only a single method "runWorkflow" has been defined which represents the starting point of the execution of our Workflow. The method is annotated with the "Workflow Type" configured by you in the application. It is very important that it matches.

`SampleWorkflowImpl.java`

> This class implements our Workflow. There are several aspects to take into account. A set of configuration options have been defined in the constructor that we will use for the correct configuration of the Temporal activities. Of course, you can use your own configurations, remember that this is just an example.
>
> The generated template uses a set of generated activities for the purpose of using the KuFlow REST API without you having to implement them yourself. One important thing you will notice is that specific options have been defined for the `createTaskAndWaitTermination` activity. This is because this activity uses a Temporal mechanism that allows the activity to be executed without termination, waiting for its completion to be explicitly indicated later. Normally we will use this approach when we want to execute activities that create tasks in KuFlow that must be completed by a human.

`TemporalBootstrap.java`

> In this Spring bean we perform startup configurations of the Worker. In the `starWorkers` method we indicate that WorkFlows and Activities implement this Worker.

`TemporalConfiguration.java`

> In order to connect our Worker to the KuFlow's Temporal cloud, it is necessary to configure the correct Authentication and Authorization mechanisms. In this configuration class we add the mTLS options to the Temporal client and the JWT authentication header which is a token that is negotiated through the KuFlow Rest API.

`ApplicationProperties.java`

> A simple Spring `ConfigrationProperties` Bean that connects the properties configured in YAML or Environment Variables.

`SampleEngineWorkerApp.java`

> It is the startup Java class of our Worker. Contains the "main" method that executes the Java process.

## More info

More information is available in the [KuFlow documentation](https://docs.kuflow.com/) and on our [forums](https://community.kuflow.com/), as well as in the general [Temporal documentation](https://docs.temporal.io/).
