/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ci.web.model;

@SuppressWarnings("PublicField") public class Version {
    /** TC Helper Version. */
    public String version = "20180817";

    /** Ignite version. */
    public String ignVer;

    /** Ignite version. */
    public String ignVerFull;

    /** TC Helper Source web. */
    public String srcWebUrl = "https://github.com/apache/ignite-teamcity-bot";

    public Version() {}
}
