/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker;

import com.kuflow.engine.samples.worker.ApplicationProperties.TemporalProperties.MutualTlsProperties;
import com.kuflow.rest.KuFlowRestClient;
import com.kuflow.temporal.common.authorization.KuFlowAuthorizationTokenSupplier;
import com.kuflow.temporal.common.ssl.SslContextBuilder;
import com.kuflow.temporal.common.tracing.MDCContextPropagator;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.authorization.AuthorizationGrpcMetadataProvider;
import io.temporal.client.ActivityCompletionClient;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.serviceclient.WorkflowServiceStubsOptions.Builder;
import io.temporal.worker.WorkerFactory;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TemporalConfiguration {

    //Add 1 
    private final SampleEngineWorkerUiVisionProperties sampleEngineWorkerUiVisionProperties;
    // remove: private final ApplicationProperties applicationProperties;

    private final KuFlowRestClient kuFlowRestClient;

    //Add 2 and remove: ApplicationProperties applicationProperties
    public TemporalConfiguration(SampleEngineWorkerUiVisionProperties sampleEngineWorkerUiVisionProperties, ApplicationProperties applicationProperties, KuFlowRestClient kuFlowRestClient) {
        //Add 3 
        this.sampleEngineWorkerUiVisionProperties = sampleEngineWorkerUiVisionProperties;
        //remove: this.applicationProperties = applicationProperties;
        this.kuFlowRestClient = kuFlowRestClient;
    }

    @Bean(destroyMethod = "shutdown")
    public WorkflowServiceStubs workflowServiceStubs() {
        Builder builder = WorkflowServiceStubsOptions.newBuilder();
        builder.setTarget(this.sampleEngineWorkerUiVisionProperties.getTemporal().getTarget());
        builder.setSslContext(this.createSslContext());
        builder.addGrpcMetadataProvider(
            new AuthorizationGrpcMetadataProvider(new KuFlowAuthorizationTokenSupplier(this.kuFlowRestClient))
        );

        WorkflowServiceStubsOptions options = builder.validateAndBuildWithDefaults();

        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
        WorkflowClientOptions options = WorkflowClientOptions
            .newBuilder()
            .setNamespace(this.sampleEngineWorkerUiVisionProperties.getTemporal().getNamespace())
            .setContextPropagators(Collections.singletonList(new MDCContextPropagator()))
            .build();

        return WorkflowClient.newInstance(service, options);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    @Bean
    public ActivityCompletionClient activityCompletionClient(WorkflowClient workflowClient) {
        return workflowClient.newActivityCompletionClient();
    }

    private SslContext createSslContext() {
        //change to : 
        com.kuflow.engine.samples.worker.SampleEngineWorkerUiVisionProperties.TemporalProperties.MutualTlsProperties mutualTls = this.sampleEngineWorkerUiVisionProperties.getTemporal().getMutualTls();

        return SslContextBuilder
            .builder()
            .withCa(mutualTls.getCa())
            .withCaData(mutualTls.getCaData())
            .withCert(mutualTls.getCert())
            .withCertData(mutualTls.getCertData())
            .withKey(mutualTls.getKey())
            .withKeyData(mutualTls.getKeyData())
            .build();
    }
}
