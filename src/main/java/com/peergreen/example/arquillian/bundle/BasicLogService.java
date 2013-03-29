/**
 * Copyright 2013 Peergreen S.A.S.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.peergreen.example.arquillian.bundle;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;


public class BasicLogService implements LogService {

    public void log(int level, String message) {
        log(null, level, message, null);
    }

    public void log(int level, String message, Throwable exception) {
        log(null, level, message, exception);
    }

    public void log(ServiceReference sr, int level, String message) {
        log(sr, level, message, null);
    }

    public void log(ServiceReference serviceReference, int level, String message, Throwable exception) {
        String userLevel = null;

        if (exception != null) {
            message += ", exception:" + exception.getCause();
        }

        switch (level) {
        case LogService.LOG_ERROR:
            userLevel = "ERROR";
            break;
        case LogService.LOG_WARNING:
            userLevel = "WARNING";
            break;
        case LogService.LOG_INFO:
            userLevel = "INFO";
            break;
        case LogService.LOG_DEBUG:
            userLevel = "DEBUG";
            break;
        }

        System.out.println("[" + userLevel + "] " + message);
    }

}
