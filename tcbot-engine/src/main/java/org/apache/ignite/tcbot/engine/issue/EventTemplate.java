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

package org.apache.ignite.tcbot.engine.issue;

import org.apache.ignite.tcignited.history.IEventTemplate;

public class EventTemplate implements IEventTemplate {
    private final int[] beforeEvent;
    private final int[] eventAndAfter;
    private boolean shouldBeFirst;

    public EventTemplate(int[] beforeEvent, int[] eventAndAfter) {
        this.beforeEvent = beforeEvent;
        this.eventAndAfter = eventAndAfter;
    }

    public int[] beforeEvent() {
        return beforeEvent;
    }

    public int[] eventAndAfter() {
        return eventAndAfter;
    }

    public int cntEvents() {
        return beforeEvent.length + eventAndAfter.length;
    }

    public EventTemplate setShouldBeFirst(boolean shouldBeFirst) {
        this.shouldBeFirst = shouldBeFirst;

        return this;
    }

    public boolean shouldBeFirst() {
        return shouldBeFirst;
    }
}
