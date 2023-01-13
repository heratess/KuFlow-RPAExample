/*
 * Copyright (c) 2023-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker;

import java.time.Duration;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class SampleEngineWorkerUiVisionProperties {

    private final TemporalProperties temporal = new TemporalProperties();

    private final UIVisionProperties uivision = new UIVisionProperties();

    public TemporalProperties getTemporal() {
        return this.temporal;
    }

    public UIVisionProperties getUivision() {
        return this.uivision;
    }

    public class UIVisionProperties {

        @NotBlank
        private String command;

        @NotBlank
        private String logDirectory;

        @NotBlank
        private String autoRunHtml;

        @NotBlank
        private String macro;

        private boolean closeBrowser = true;

        private boolean closeRpa = true;

        @NotNull
        private Duration executionTimeout;

        public String getCommand() {
            return this.command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getLogDirectory() {
            return this.logDirectory;
        }

        public void setLogDirectory(String logDirectory) {
            this.logDirectory = logDirectory;
        }

        public String getAutoRunHtml() {
            return this.autoRunHtml;
        }

        public void setAutoRunHtml(String autoRunHtml) {
            this.autoRunHtml = autoRunHtml;
        }

        public String getMacro() {
            return this.macro;
        }

        public void setMacro(String macro) {
            this.macro = macro;
        }

        public boolean isCloseBrowser() {
            return this.closeBrowser;
        }

        public int getCloseBrowserAsInt() {
            return BooleanUtils.toInteger(this.closeBrowser);
        }

        public void setCloseBrowser(boolean closeBrowser) {
            this.closeBrowser = closeBrowser;
        }

        public boolean isCloseRpa() {
            return this.closeRpa;
        }

        public int getCloseRpaAsInt() {
            return BooleanUtils.toInteger(this.closeRpa);
        }

        public void setCloseRpa(boolean closeRPA) {
            this.closeRpa = closeRPA;
        }

        public Duration getExecutionTimeout() {
            return this.executionTimeout;
        }

        public void setExecutionTimeout(Duration executionTimeout) {
            this.executionTimeout = executionTimeout;
        }
    }

    public static final class TemporalProperties {

        private String namespace;

        private String kuflowQueue;

        private String target;

        private MutualTlsProperties mutualTls = new MutualTlsProperties();

        public MutualTlsProperties getMutualTls() {
            return this.mutualTls;
        }

        public String getNamespace() {
            return this.namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getTarget() {
            return this.target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getKuflowQueue() {
            return this.kuflowQueue;
        }

        public void setKuflowQueue(String kuflowQueue) {
            this.kuflowQueue = kuflowQueue;
        }

        public static final class MutualTlsProperties {

            private String ca;

            private String caData;

            private String cert;

            private String certData;

            private String key;

            private String keyData;

            public String getCa() {
                return this.ca;
            }

            public void setCa(String ca) {
                this.ca = ca;
            }

            public String getCaData() {
                return this.caData;
            }

            public void setCaData(String caData) {
                this.caData = caData;
            }

            public String getCert() {
                return this.cert;
            }

            public void setCert(String cert) {
                this.cert = cert;
            }

            public String getCertData() {
                return this.certData;
            }

            public void setCertData(String certData) {
                this.certData = certData;
            }

            public String getKey() {
                return this.key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getKeyData() {
                return this.keyData;
            }

            public void setKeyData(String keyData) {
                this.keyData = keyData;
            }
        }
    }
}