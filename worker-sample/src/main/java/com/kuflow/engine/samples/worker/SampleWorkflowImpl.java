/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker;

import com.kuflow.rest.model.Task;
import com.kuflow.rest.model.TaskDefinitionSummary;
import com.kuflow.temporal.activity.kuflow.KuFlowAsyncActivities;
import com.kuflow.temporal.activity.kuflow.KuFlowSyncActivities;
import com.kuflow.temporal.activity.kuflow.model.ClaimTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.CompleteProcessRequest;
import com.kuflow.temporal.activity.kuflow.model.CompleteProcessResponse;
import com.kuflow.temporal.activity.kuflow.model.CompleteTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.CreateTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.RetrieveTaskRequest;
import com.kuflow.temporal.activity.kuflow.model.RetrieveTaskResponse;
import com.kuflow.temporal.activity.uivision.UIVisionActivities;
import com.kuflow.temporal.activity.uivision.model.ExecuteUIVisionMacroRequest;
import com.kuflow.temporal.common.KuFlowGenerator;
import com.kuflow.temporal.common.model.WorkflowRequest;
import com.kuflow.temporal.common.model.WorkflowResponse;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;

public class SampleWorkflowImpl implements SampleWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(SampleWorkflowImpl.class);

    private static final String TASK_CODE_MY_ROBOT_RESULTS = "ROBOT_RESULTS";

    private final KuFlowSyncActivities kuFlowSyncActivities;

    private final KuFlowAsyncActivities kuFlowAsyncActivities;

    //Add1
    private final UIVisionActivities uiVisionActivities;
    
    private KuFlowGenerator kuFlowGenerator;

    public SampleWorkflowImpl() {
        RetryOptions defaultRetryOptions = RetryOptions.newBuilder().validateBuildWithDefaults();

        ActivityOptions defaultActivityOptions = ActivityOptions
            .newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(10))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        ActivityOptions asyncActivityOptions = ActivityOptions
            .newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofDays(1))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        this.kuFlowSyncActivities = Workflow.newActivityStub(KuFlowSyncActivities.class, defaultActivityOptions);
        this.kuFlowAsyncActivities = Workflow.newActivityStub(KuFlowAsyncActivities.class, asyncActivityOptions);
        //Add2
        this.uiVisionActivities = Workflow.newActivityStub(UIVisionActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponse runWorkflow(WorkflowRequest request) {
        LOGGER.info("Process {} started", request.getProcessId());

        this.kuFlowGenerator = new KuFlowGenerator(request.getProcessId());

        this.createTaskMyRobotResults(request);

        CompleteProcessResponse completeProcess = this.completeProcess(request.getProcessId());

        LOGGER.info("Process {} finished", request.getProcessId());

        return this.completeWorkflow(completeProcess);
    }

    private WorkflowResponse completeWorkflow(CompleteProcessResponse completeProcess) {
        WorkflowResponse workflowResponse = new WorkflowResponse();
        workflowResponse.setMessage("Completed process " + completeProcess.getProcess().getId());

        return workflowResponse;
    }

    private CompleteProcessResponse completeProcess(UUID processId) {
        CompleteProcessRequest request = new CompleteProcessRequest();
        request.setProcessId(processId);

        return this.kuFlowSyncActivities.completeProcess(request);
    }

    /**
     * Create task "My robot results" in KuFlow and wait for its completion
     *
     * @param workflowRequest workflow request
     * @return task created
     */
    //Add3 cambio Task por void
     private void createTaskMyRobotResults(WorkflowRequest workflowRequest) {
        UUID taskId = this.kuFlowGenerator.randomUUID();

        TaskDefinitionSummary tasksDefinition = new TaskDefinitionSummary();
        tasksDefinition.setCode(TASK_CODE_MY_ROBOT_RESULTS);

        Task task = new Task();
        task.setId(taskId);
        task.setProcessId(workflowRequest.getProcessId());
        task.setTaskDefinition(tasksDefinition);

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTask(task);
        this.kuFlowAsyncActivities.createTaskAndWaitFinished(createTaskRequest);
        this.kuFlowSyncActivities.createTask(createTaskRequest);

        /*quitamos este bloque:
        RetrieveTaskRequest retrieveTaskRequest = new RetrieveTaskRequest();
        retrieveTaskRequest.setTaskId(taskId);
        RetrieveTaskResponse retrieveTaskResponse = this.kuFlowSyncActivities.retrieveTask(retrieveTaskRequest);
        Y lo reemplazamos por este: */
        ClaimTaskRequest claimTaskRequest = new ClaimTaskRequest();
        claimTaskRequest.setTaskId(taskId);
        this.kuFlowSyncActivities.claimTask(claimTaskRequest);

        // Executes the Temporal activity to run the robot.
        ExecuteUIVisionMacroRequest executeUIVisionMacroRequest = new ExecuteUIVisionMacroRequest();
        executeUIVisionMacroRequest.setTaskId(taskId);
        this.uiVisionActivities.executeUIVisionMacro(executeUIVisionMacroRequest);

        //Borramos porque cambiamos de task a void 
        //return retrieveTaskResponse.getTask();
        
        // Complete the task.
        CompleteTaskRequest completeTaskRequest = new CompleteTaskRequest();
        completeTaskRequest.setTaskId(taskId);
        this.kuFlowSyncActivities.completeTask(completeTaskRequest);
    }
}
