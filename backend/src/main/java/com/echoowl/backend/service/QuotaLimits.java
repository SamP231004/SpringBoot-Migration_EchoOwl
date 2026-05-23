package com.echoowl.backend.service;

import com.echoowl.backend.model.Plan;

public final class QuotaLimits {
    public static final int FREE_EVENTS_PER_MONTH = 100;
    public static final int FREE_EVENT_CATEGORIES = 3;
    public static final int PRO_EVENTS_PER_MONTH = 1000;
    public static final int PRO_EVENT_CATEGORIES = 10;

    private QuotaLimits() {
    }

    public static int eventsFor(Plan plan) {
        return plan == Plan.PRO ? PRO_EVENTS_PER_MONTH : FREE_EVENTS_PER_MONTH;
    }

    public static int categoriesFor(Plan plan) {
        return plan == Plan.PRO ? PRO_EVENT_CATEGORIES : FREE_EVENT_CATEGORIES;
    }
}
