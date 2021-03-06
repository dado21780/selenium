// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.internal.BuildInfo;

public class WebDriverException extends RuntimeException {

  public static final String SESSION_ID = "Session ID";
  public static final String DRIVER_INFO = "Driver info";
  protected static final String BASE_SUPPORT_URL = "http://seleniumhq.org/exceptions/";
  protected Integer statusCode;

  private Map<String, String> extraInfo = new HashMap<>();

  public WebDriverException() {
    super();
  }

  public WebDriverException(String message) {
    super(message);
  }

  public WebDriverException(Throwable cause) {
    super(cause);
  }

  public WebDriverException(String message, Throwable cause) {
    super(message, cause);
  }

  public WebDriverException(String message, Throwable cause, Integer statusCode) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  /**
   * DO NOT USE -
   * This is to help the standalone server map exceptions from remote ends back to clients without
   * losing the original status code provide (if one is). This will be going away in 4.0 when status
   * codes are only string messages
   * @return status code from the response of the remote end driver.
   */
  @Deprecated
  public Integer getStatusCode() {
    return statusCode;
  }

  @Override
  public String getMessage() {
    return createMessage(super.getMessage());
  }

  private String createMessage(String originalMessageString) {
    String supportMessage = getSupportUrl() == null ?
        "" : "For documentation on this error, please visit: " + getSupportUrl() + "\n";

    return (originalMessageString == null ? "" : originalMessageString + "\n")
        + supportMessage
        + getBuildInformation() + "\n"
        + getSystemInformation()
        + getAdditionalInformation();
  }

  public String getSystemInformation() {
    String host = "N/A";
    String ip   = "N/A";

    try{
      host = InetAddress.getLocalHost().getHostName();
      ip   = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException throw_away) {}

    return String.format("System info: host: '%s', ip: '%s', os.name: '%s', os.arch: '%s', os.version: '%s', java.version: '%s'",
      host,
      ip,
      System.getProperty("os.name"),
      System.getProperty("os.arch"),
      System.getProperty("os.version"),
      System.getProperty("java.version"));
  }

  public String getSupportUrl() {
    return null;
  }

  public BuildInfo getBuildInformation() {
    return new BuildInfo();
  }

  public static String getDriverName(StackTraceElement[] stackTraceElements) {
    String driverName = "unknown";
    for (StackTraceElement e : stackTraceElements) {
      if (e.getClassName().endsWith("Driver")) {
        String[] bits = e.getClassName().split("\\.");
        driverName = bits[bits.length - 1];
      }
    }

    return driverName;
  }

  public void addInfo(String key, String value) {
    extraInfo.put(key, value);
  }

  public String getAdditionalInformation() {
    if (! extraInfo.containsKey(DRIVER_INFO)) {
      extraInfo.put(DRIVER_INFO, "driver.version: " + getDriverName(getStackTrace()));
    }

    String result = "";
    for (Map.Entry<String, String> entry : extraInfo.entrySet()) {
      if (entry.getValue() != null && entry.getValue().startsWith(entry.getKey())) {
        result += "\n" + entry.getValue();
      } else {
        result += "\n" + entry.getKey() + ": " + entry.getValue();
      }
    }
    return result;
  }
}
