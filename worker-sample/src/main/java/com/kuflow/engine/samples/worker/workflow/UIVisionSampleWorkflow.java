/*
 * Copyright (c) 2023-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker.workflow;

import com.kuflow.temporal.common.model.WorkflowRequest;
import com.kuflow.temporal.common.model.WorkflowResponse;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface UIVisionSampleWorkflow {
    String WORKFLOW_NAME = UIVisionSampleWorkflow.class.getSimpleName();

    @WorkflowMethod
    WorkflowResponse runWorkflow(WorkflowRequest request);
}
