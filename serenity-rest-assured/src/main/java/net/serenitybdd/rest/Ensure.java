package net.serenitybdd.rest;

import io.restassured.response.ValidatableResponse;
import net.thucydides.core.steps.ExecutedStepDescription;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.steps.StepFailure;
import net.thucydides.core.steps.events.StepFailedEvent;
import net.thucydides.core.steps.events.StepFinishedEvent;
import net.thucydides.core.steps.events.StepStartedEvent;
import net.thucydides.core.steps.session.TestSession;

import java.util.function.Consumer;

public class Ensure {

    /**
     * A helper method to make a RestAssured assertion appear as a separate step.
     * e.g
     * Ensure.that("Commany name should be returned",
     * response -> response.body("companyName", equalTo("Apple Inc.s")));
     *
     * @param description
     * @param check
     */
    public static Ensure that(String description, Consumer<ValidatableResponse> check) {
        Ensure ensure = new Ensure();
        ensure.performCheck(description, check, "Ensure that ");
        return ensure;
    }

    public Ensure andThat(String description, Consumer<ValidatableResponse> check) {
        performCheck(description, check, "And that ");
        return this;
    }

    private void performCheck(String description, Consumer<ValidatableResponse> check, String prefix) {
        if (TestSession.isSessionStarted()) {
            doPerformAsynchronousCheck(description, check, prefix);
        } else {
            doPerformSynchronousCheck(description, check, prefix);
        }
    }

    private static void doPerformAsynchronousCheck(String description, Consumer<ValidatableResponse> check, String prefix) {
        TestSession.addEvent(new StepStartedEvent(ExecutedStepDescription.withTitle(prefix + description)));
        try {
            check.accept(SerenityRest.then());
        } catch (Throwable e) {
            TestSession.addEvent(new StepFailedEvent(new StepFailure(ExecutedStepDescription.withTitle(description), e)));
            return;
        }
        TestSession.addEvent(new StepFinishedEvent());
    }

    private static void doPerformSynchronousCheck(String description, Consumer<ValidatableResponse> check, String prefix) {
        StepEventBus.getEventBus().stepStarted(ExecutedStepDescription.withTitle(prefix + description));
        try {
            check.accept(SerenityRest.then());
        } catch (Throwable e) {
            StepEventBus.getEventBus().stepFailed(new StepFailure(ExecutedStepDescription.withTitle(description), e));
            return;
        }
        StepEventBus.getEventBus().stepFinished();
    }

}
