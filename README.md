# KuFlow - RPA (UI.Vision) example

## What we will create?

This tutorial will guide us in building a simple Temporal.io worker workflow (*when we apply the Workflow as Code paradigm*) and connecting to an RPA tool (**UI.Vision**) to show more possibilities of integration for KuFlow. Our use case will be a simple automated task that will perform the following actions: open a new web browser tab, insert text in a Google search textbox, take a screenshot of the results page, and upload it to Kuflow's User Interface using our CLI (command-line interface tool that allows you to interact with the KuFlow Rest API without the need to interact with the API explicitly or through the application GUI interface).


## Prerequisites

Before starting your Workflow for the first time, you must register in [KuFlow (app.kuflow.com)](https://app.kuflow.com/). After familiarizing yourself with the user interface by navigating through the menus or visiting our [Youtube channel](https://www.youtube.com/channel/UCXoRtHICa86YfX8P_wu1f6Q) with many videos that will help you in this task, you are ready to perform the necessary configurations for our Worker. To do so, click on the `Management` menu.

### Create the Credentials for the Worker

We will configure an `APPLICATION` that will provide us with the necessary credentials so that our Worker (written in Java and located in your own machine) can interface with KuFlow.

Go to the `Settings > Applications` menu and click on `Add application`. We establish the name we want and save. Next, you will get the first data needed to configure our Worker.

- **Identifier**: Unique identifier of the application. For this tutorial: *myApp*
  - Later in this tutorial, we will configure it in the `kuflow.api.application-id` property of our example.
- **Token**: Password for the application.
  - Later in this tutorial, we will configure it in the `kuflow.api.token` property of our example.
- **Namespace**: Temporal's namespace.
  - Later in this tutorial, we will configure it in the `application.temporal.namespace` property of our example.

Next, we will proceed to create the certificates that will serve us to configure the Mutual TLS with which our Worker will perform the authentication against Temporal. To do this we click on "Add certificate", set the name we want, and choose the `PKCS8` type for the encryption of the private key. This is important since the example code in this tutorial works with this encoding. We will get the following:

- **Certificate**: It is the public part that is presented in the *mTLS* connection.
  - Later in this tutorial, we will configure it in the `application.temporal.mutual-tls.cert-data` property of our example.
- **Private Key**: It is the private key for *mTLS*.
  - Later in this tutorial, we will configure it in the `application.temporal.mutual-tls.key-data` property of our example.

It is also necessary to indicate the CA certificate, which is the root certificate with which all certificates are issued. It is a public certificate and for convenience you can find it in the same `Application` screen, under the name of *CA Certificate*. This certificate will always be the same between workers.

- **CA Certificate**: Root certificate with which all certificates (client and server certificates) are issued.
  - Later in this tutorial, we will configure it in the `kuflow.activity.kuflow.key-data` property of our example.

Finally, you get something like:

<div class="text--center">

![](/img/TUT-01-App.png)

</div>

## Preparing Scenario
### KuFlow's CLI
 We'll use a CLI which implements a client of our API, saving you from having to create your own implementation to interact with KuFlow. To install this CLI on your system the steps will be found in our [CLI documentation](https://docs.kuflow.com/developers/kuflowctl/). Depending on how you decide to authenticate the CLI, you may need to adjust the value of command #6 of the following step to pass them the appropriate credentials. For this example we'll assume that the CLI is authenticated with a configuration file located in the default path.

### UI.Vision Tools
In the KuFlow SDK we have an implementation of this activity ready to use. Its operation is simple. Basically what you do is to run an external process installed on the machine, which in this case is none other than a Chrome browser with the [UI.Vision extension](https://ui.vision/#get) configured and a robot registered to it. If this implementation does not fit your needs, you can always make your own using the provided activity as a reference.

To learn more about this implementation, see our [document section about UI.Vision.](https://docs.kuflow.com/developers/rpa/uivision)

We'll assume that you have the knowledge to implement a robot in UI.Vision; otherwise [documentation](https://ui.vision/rpa/docs) is available on their website and their forum can be of great help.   r robot will simply open a new web browser tab, insert text in a Google search textbox, take a screenshot of the results page, and upload it to Kuflow's User Interface using our CLI. The robot specification is the following:

|    | Command           | Target                  | Value                                                                                             |
|----|-------------------|-------------------------|---------------------------------------------------------------------------------------------------|
| 1  | open              | https://www.google.com  |                                                                                                   |
| 2  | click             | name=q                  |                                                                                                   |
| 3  | type              | name=q                  | kuflow                                                                                            |
| 4  | clickAndWait      | name=btnK               |                                                                                                   |
| 5  | captureScreenshot | kuflow_image.png        |                                                                                                   |
| 6  | XRunAndWait       | kuflowctl               | --silent save-element-document --task-id=${!cmd_var1} --element-code=SCREENSHOT kuflow_image.png  |
| 7  | store             | ${!xrun_exitcode}       | result                                                                                            |
| 8  | echo              | "Exit code: ${result}"  |                                                                                                   |
| 9  | selectWindow      | title=KuFlow Backoffice |                                                                                                   |
| 10 | assert            | result                  | 0                                                                                                                                                                                               

Interaction with external processes from a UI.Vision robot is limited. However, it allows us to run external processes and get their output code. This is enough for our robot to interact with the KuFlow API and upload the results in the task.

For this example you have to install UI.Vision with its [Xmodules module](https://ui.vision/rpa/x/download) too, in the Google Chrome browser.

:::info IMPORTANT
Make sure that in the extension details, the "Allow access to file URLs" option is checked.
:::

**Some important details about the robot instructions:**

- Our activity passes to the robot the taskId of the task in which it should upload the capture taken.
- To identify if the robot execution has finished successfully, we need to check the CLI response code (command #10), otherwise the robot execution would always finish successfully even if the command execution was unsuccessful.

<div class="text--center">

![](/img/tut11-uivisiongui.jpg)

</div>

### Create the process definition

We need to create the definition of the process that will execute our Workflow. In this section we will configure the KuFlow tasks of which it is made up as well as the information necessary to complete said tasks, the process access rules (i.e. *RBAC*), as well as another series of information. To do this we go to the `Setting > Processes` menu and create a new process. You can take a quick look at this [video](https://youtu.be/a8042IkqchQ) to get an idea of the user interface for process definition.

A ***Process Definition*** with the following data:

- **Process name**
  - UI.Vision Example
- **Description**
  - Free text description about the Workflow.
- **Workflow**
  - **Workflow Engine**
    - *KuFlow Engine*, because we are designing a Temporal-based Worker.
  - **Workflow Application**
    - The application to which our Worker will connect to
  - **Task queue**
    - The name of the Temporal queue where the KuFlow tasks will be set. You can choose any name, later you will set this same name in the appropriate configuration in your Worker. For example: `UIVisionQueue`.
  - **Name**
    - It must match the name of the Java interface of the Workflow. In our example, `UIVisionSampleWorkflow` is the name you should type in this input.
- **Permissions**
  1. At least one user or group of users with the role of `INITIATOR` in order to instantiate the process through the application.
     1. Optional: In order to view administrative actions in the GUI, it may interesting to set the `Manager` role as well for some user.


A ***Task Definition*** in the process with the following data:

- Task **"My robot results"**
  - **Description**: Free text description about the Task.
  - **Code**: ROBOT_RESULTS
  - **Candidates**:
    - This is an automated task, i.e. a system (in this case, our robot) will be in charge of performing it. Therefore, the application we have configured is indicated here as a candidate.However, we'll need to add a user with the viewer role too.
  - **Element to fill the email**:
    - **Name**: Field's label, for example *"Screenshot"*
    - **Code**: SCREENSHOT
    - **Type**: Field
    - **Properties**: *Mandatory*, checked.
    - **Field Type**: Document

You'll get something like:

<div class="text--center">

![](/img/tut11-proccessdefinition.jpg)

</div>

### Publish the process and download the template for the Workflow Worker​
By clicking on the “Publish” button you’ll receive a confirmation request message, once you have confirmed the process will be published.

<div class="text--center">

![](/img/tut11-publish.jpg)

![](/img/tut11-publish2.jpg)

</div>

Now, you can download a sample Workflow Implementation from the Process Definition main page. You'll be able to choose from several programming languages according to your preference or environment. For more information ake a look to this [blog entry.](https://kuflow.com/blog/en/new-templates/)

<div class="text--center">

![](/img/tut11-template.jpg)

![](/img/tut11-template2.jpg)

</div>

This code will serve as a starting point for implementing our worker. As we'll use the Java template, the requirements for its use are the following:

- **Java JDK**
  - You need to have a Java JDK installed on your system. The current example code uses version 17, but is not required for the KuFlow SDK. You can use for example [Adoptium](https://adoptium.net/) distribution or any other. We recommend you to use a tool like [SDKMan](https://sdkman.io/jdks) to install Java SDK distributions in a comfortable way.
- **IDE**
  - An IDE with good Java support is necessary to work comfortably. You can use *VSCode*, *IntelliJ Idea,* *Eclipse* or any other with corresponding Java plugins.

### Main technologies used in the example
To make things simpler, the following technologies have been mainly used in our example:

- **Maven**
  - To build the example. It is distributed in an integrated way so it is not necessary to install it in isolation.
- **Spring Boot and Spring Cloud**
  - To wire all our integrations.
- **Temporal Java SDK**
  - To perform GRPC communications with the KuFlow temporal service.
- **KuFlow Java SDK**
  - To implement the KuFlow API Rest client, some Temporal activities and others.
- **KuFlow CLI (kuflowctl)**
  -  A command line tool that allows you to interact with the KuFlow Rest API.
- **UI.Vision**
  - RPA framework that allows us to automate operations in web and desktop applications


## Implementation

**Note:** You can download or clone the source code of this tutorial from our [public Github repository](https://github.com/kuflow/kuflow-engine-samples-java), be sure to add all the tokens and secrets from your KuFlow account and, if is the case, 3rd party API developers.

### Resolve dependencies

We need to modify `pom.xml`, to include this new dependencies:
```xml
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-core</artifactId>
      <version>1.50.2</version>
    </dependency>
	<dependency>
      <groupId>com.kuflow</groupId>
      <artifactId>kuflow-temporal-activity-uivision</artifactId>
      <version>2.0.1</version>
    </dependency>
```

### Using Credentials

Now, in this step we are filling up the application configuration information. You must complete all the settings and replace the example values. 

#### KuFlow’s Credentials

The appropriate values can be obtained from the KuFlow application. Check out the [Create the credentials](#create-the-credentials-for-the-worker) section of this tutorial.

**NOTE:** Remember that this scenario is running over a Windows SO environment, in the  code  examples comments you'll find Linux paths but Windows paths are used.

```yaml
# ===================================================================
# PLEASE COMPLETE ALL CONFIGURATIONS BEFORE STARTING THE WORKER
# ===================================================================

kuflow:
  api:

    # ID of the APPLICATION configured in KUFLOW.
    # Get it in "Application details" in the Kuflow APP.
    client-id: FILL_ME

    # TOKEN of the APPLICATION configured in KUFLOW.
    # Get it in "Application details" in the Kuflow APP.
    client-secert: FILL_ME

activity:
  ui-vision:

    # Browser with UI.VISION pluging.
    # Example linux: /user/bin/google-chrome
    launch-command: C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe

    # A directory where the robot can set its logs.
    # Example: /home/user/logs
    log-directory: C:\\Users\\USERNAME\\Documents\\Kuflow\\UIVision app\\worker-sample\\etc\\logs

    # Path to the UI.VISION autorun html
    # Example: /home/user/ui.vision.html
    # See in: kuflow-samples-temporal-uivision-spring/etc/autostarthtml/ui.vision.html
    auto-run-html: C:\\Users\\USERNAME\\Documents\\Kuflow\\UIVision app\\worker-sample\\etc\\autostarthtml\\ui.vision.html

    # UI.Vision macro to run
    # Example: KuFlowScreenshot.json
    # see in: kuflow-samples-temporal-uivision-spring/etc/macro/KuFlowScreenshot.json
    macro: KuFlowScreenshot.json

    # Close browser when the macro is completed.
    # Optional:
    closeBrowser: false

    # Close UI.Vision RPA when the macro is completed.
    # Optional:
    # close-rpa: true

    # It should be less than the duration specified in the StartToCloseTimeout of the UI.Vision Temporal activity.
    execution-timeout: 20m

application:
  temporal:

    # Temporal Namespace. Get it in "Application details" in the KUFLOW APP.
    namespace: FILL_ME

    # Temporal Queue. Configure it in the "Process definition" in the KUFLOW APP.
    kuflow-queue: FILL_ME

    mutual-tls:
      # Client certificate
      # Get it in "Application details" in the KUFLOW APP.
      cert-data: |
        -----BEGIN CERTIFICATE-----
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        …
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        -----END CERTIFICATE-----

      # Private key
      # Get it in "Application details" in the KUFLOW APP.
      # IMPORTANT: This example works with PKCS8, so ensure PKCS8 is selected
        #            when you generate the certificates in the KUFLOW App
      key-data: |
        -----BEGIN CERTIFICATE-----
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        …
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        -----END CERTIFICATE-----

      # KUFLOW Certification Authority (CA) of the certificates issued in KUFLOW
      ca-data: |
        -----BEGIN CERTIFICATE-----
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        …
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        -----END CERTIFICATE-----
```

Please note that this is a `YAML`, respect the indentation.

#### KuFlows CLI credentials

Create a file called `.kuflow.yml` in your user home folder (*C:\Users\USERNAME\ for Windows OS*) with the following text:

```yaml
kuflow:
  # ID of the APPLICATION configured in KUFLOW.
  # Get it in "Application details" in the Kuflow APP.
  client-id: YOUR_IDENTIFIER

  # TOKEN of the APPLICATION configured in KUFLOW.
  # Get it in "Application details" in the Kuflow APP.
  client-secret: YOUR_SECRET
  
  # OPTIONAL KUFLOW REST API. Default is: https://api.kuflow.com
  endpoint: https://api.kuflow.com
```

### Folder Structures and files

At the root of the project, we have to create a folder called **"etc"** and inside it two others:

- **"autostarthtml"** with the file `ui.vision.html`

- **"macro"** with the file `KuflowScreenshot.json`

**NOTE:** both files could be downloaded from the [Github repository](https://github.com/kuflow/kuflow-engine-samples-java) or from the UI.Vision Interface installed on your computer.

If you will import the [macro](https://github.com/kuflow/kuflow-samples-java/blob/main/kuflow-samples-temporal-uivision-spring/etc/macro/KuFlowScreenshot.json) from github, use the UI.Vision gui (`Import JSON` action).

:::info IMPORTANT
Make sure the `Storage Mode: File system (on hard drive)` is selected, in order to store the results of the robot (the capture and logs) on the hard drive and not in the browser storage.
:::

### Configurations

Now, due to the complexity of the code we're adding, it's recommended that you download and replace the [ApplicationProperties.java](https://github.com/kuflow) file on your downloaded template. We've added all the UI.Vision robot properties for you to avoid writing them all (*about 90 lines*).

### Workflow and Activity register

To implement the activities that perform the communication with the KuFlow REST API and the UIVision activities we need to register them in the Temporal bootstrap class.

**NOTE:** *We add a comment **"//ADD this line:"** before each line to highlight new code.*

[TemporalBootstrap.java](https://github.com/kuflow)

	@Component
	public class TemporalBootstrap implements InitializingBean, DisposableBean {

	    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalBootstrap.class);
	
	    private final WorkerFactory factory;
	
	    private final KuFlowSyncActivities kuFlowSyncActivities;
	
	    private final KuFlowAsyncActivities kuFlowAsyncActivities;
	
	    private final ApplicationProperties applicationProperties;
	
	    //ADD this line:
	    private final UIVisionActivities uiVisionActivities;

	    public TemporalBootstrap(
	        ApplicationProperties applicationProperties,
	        WorkerFactory factory,
	        KuFlowSyncActivities kuFlowSyncActivities,
	        KuFlowAsyncActivities kuFlowAsyncActivities,
	        //ADD this line:
	        UIVisionActivities uiVisionActivities
	    ) {
	        this.applicationProperties = applicationProperties;
	        this.factory = factory;
	        this.kuFlowSyncActivities = kuFlowSyncActivities;
	        this.kuFlowAsyncActivities = kuFlowAsyncActivities;
	        //ADD this line:
	        this.uiVisionActivities = uiVisionActivities;
	    }

	    @Override
	    public void afterPropertiesSet() {
	        this.startWorkers();
	        LOGGER.info("Temporal connection initialized");
	    }
	
	    @Override
	    public void destroy() {
	        this.factory.shutdown();
	        this.factory.awaitTermination(1, TimeUnit.MINUTES);
	        LOGGER.info("Temporal connection shutdown");
	    }
	
	    private void startWorkers() {
	        Worker worker = this.factory.newWorker(this.applicationProperties.getTemporal().getKuflowQueue());
	        worker.registerWorkflowImplementationTypes(SampleWorkflowImpl.class);
	        worker.registerActivitiesImplementations(this.kuFlowSyncActivities);
	        worker.registerActivitiesImplementations(this.kuFlowAsyncActivities);
	        //ADD this line:
	        worker.registerActivitiesImplementations(this.uiVisionActivities);
	
	        this.factory.start();
    	}
	}

#### Workflow Implementation

In this section, we will make the fundamental steps to creating the most basic workflow for this business process:

- Users in the organization could execute an external process through an RPA tool ([UI.Vision](https://ui.vision/)) to open a new tab, complete a form, get information (*a screenshot in this case*), and view it in KuFlow's UI.
  
The entry point to the Workflow execution is determined by the `@WorkflowMethod` annotation that it'll be our code the main method.

[SampleWorkflowImpl.java](https://github.com/kuflow)

	@Override
	public WorkflowResponse runWorkflow(WorkflowRequest workflowRequest) {
	    this.kuflowGenerator = new KuFlowGenerator(workflowRequest.getProcessId());
	
	    this.createTaskRobotResults(workflowRequest);
	
	    CompleteProcessResponse completeProcess = this.completeProcess(workflowRequest);
	
	    LOGGER.info("UiVision process finished. {}", workflowRequest.getProcessId());
	
	    return this.completeWorkflow(completeProcess);
	}

The structure is very simple:

- Initialize a identifier generator in order to use deterministic Id in idempotent calls.
- Create a KuFlow Task to execute the bot.
- Report completed Workflow.
- Here the most relevant thing is the method that the robot executes.

An the orchestration is like this:

- Create task
- Claim the task
- Execute the robot
- Complete the task

Below you will find an example of the code used to perform the aforementioned. You can choose to modify it in your previously downloaded template or clone the project from our [public Github repository.](https://github.com/kuflow/)

**NOTE:** *We add comments like **"//ADD this line:"** before each line to highlight new code.*

	private void createTaskRobotResults(WorkflowRequest workflowRequest) {
	    UUID taskId = this.kuflowGenerator.randomUUID();
	
	    // Create task in KuFlow
	    TaskDefinitionSummary tasksDefinition = new TaskDefinitionSummary();
	    tasksDefinition.setCode(TASK_ROBOT_RESULTS);
	
	    Task task = new Task();
	    task.setId(taskId);
	    task.setProcessId(workflowRequest.getProcessId());
	    task.setTaskDefinition(tasksDefinition);
	
	    CreateTaskRequest createTaskRequest = new CreateTaskRequest();
	    createTaskRequest.setTask(task);
	    this.kuFlowSyncActivities.createTask(createTaskRequest);
	
	    // Claim task by the worker because is a valid candidate.
	    // We could also claim it by specifying the "owner" in the above creation call.
	    // We use the same application for the worker and for the robot.
	    ClaimTaskRequest claimTaskRequest = new ClaimTaskRequest();
	    claimTaskRequest.setTaskId(taskId);
	    this.kuFlowSyncActivities.claimTask(claimTaskRequest);
	
	    // Executes the Temporal activity to run the robot.
	    ExecuteUIVisionMacroRequest executeUIVisionMacroRequest = new ExecuteUIVisionMacroRequest();
	    executeUIVisionMacroRequest.setTaskId(taskId);
	    this.uiVisionActivities.executeUIVisionMacro(executeUIVisionMacroRequest);
	
	    // Complete the task.
	    CompleteTaskRequest completeTaskRequest = new CompleteTaskRequest();
	    completeTaskRequest.setTaskId(taskId);
	    this.kuFlowSyncActivities.completeTask(completeTaskRequest);
	}

The final step with the code is including some imports needed for this tutorial using some feature of your IDE (like pressing SHIFT+ ALT + O in Visual Studio Code).

## Testing

We can test all that we have done by running the worker (like pressing F5 in Visual Studio Code):

<div class="text--center">

![](/img/tut11-vscodef5.jpg)

</div>

And initiating the process in KuFlow’s UI, you'll get something like this:

<div class="text--center">

![](/img/tut11-test.gif)

</div>

## Summary

In this tutorial, we have covered the basics of creating a Temporal.io based workflow in KuFlow using a RPA Tool (UI.Ivision). We have defined a new process definition and we have built a workflow that contemplates the following business rules involving automated, simulating human tasks:

1. Open a new web browser tab
2. Complete a form with a specified text
3. Wait a webpage to load and take information from it (*in this case a screenshot*)
4. Show this information in KuFlow’s UI.

We have created a special video with the entire process:

Here you can watch all steps:

<a href="https://youtu.be/nTLGa2zheF0" target="_blank" title="Play me!">
  <p align="center">
    <img width="75%" src="https://img.youtube.com/vi/nTLGa2zheF0/maxresdefault.jpg" alt="Play me!"/>
  </p>
</a>


We sincerely hope that this step-by-step guide will help you to understand better how KuFlow can help your business to have better and more solid business processes.
