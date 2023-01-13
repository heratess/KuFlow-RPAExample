/*
 * Copyright (c) 2023-present KuFlow S.L.
 *
 * All rights reserved.
 */

 package com.kuflow.engine.samples.worker;

import javax.validation.constraints.NotNull;

import java.time.Duration;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "uivision")
 @Validated
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