/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker;

import com.kuflow.engine.samples.worker.workflow.UIVisionSampleWorkflowImpl;
import com.kuflow.temporal.activity.kuflow.KuFlowAsyncActivities;
import com.kuflow.temporal.activity.kuflow.KuFlowSyncActivities;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.kuflow.temporal.activity.uivision.UIVisionActivities;

@Component
public class TemporalBootstrap implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalBootstrap.class);

    private final WorkerFactory factory;

    private final KuFlowSyncActivities kuFlowSyncActivities;

    private final KuFlowAsyncActivities kuFlowAsyncActivities;

    private final ApplicationProperties applicationProperties;

    //Add 1
    private final UIVisionActivities uiVisionActivities;
    private final SampleEngineWorkerUiVisionProperties sampleEngineWorkerUiVisionProperties;

    public TemporalBootstrap(
        ApplicationProperties applicationProperties,
        WorkerFactory factory,
        KuFlowSyncActivities kuFlowSyncActivities,
        KuFlowAsyncActivities kuFlowAsyncActivities,
        //Add 2
        UIVisionActivities uiVisionActivities,
        SampleEngineWorkerUiVisionProperties sampleEngineWorkerUiVisionProperties
    ) {
        this.applicationProperties = applicationProperties;
        this.factory = factory;
        this.kuFlowSyncActivities = kuFlowSyncActivities;
        this.kuFlowAsyncActivities = kuFlowAsyncActivities;
        //Add 3
        this.uiVisionActivities = uiVisionActivities;
        this.sampleEngineWorkerUiVisionProperties = sampleEngineWorkerUiVisionProperties;
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
        //worker.registerWorkflowImplementationTypes(SampleWorkflowImpl.class);
        worker.registerActivitiesImplementations(this.kuFlowSyncActivities);
        worker.registerActivitiesImplementations(this.kuFlowAsyncActivities);
        //Add 4
        worker.registerWorkflowImplementationTypes(UIVisionSampleWorkflowImpl.class);
        worker.registerActivitiesImplementations(this.uiVisionActivities);

        this.factory.start();
    }
}
