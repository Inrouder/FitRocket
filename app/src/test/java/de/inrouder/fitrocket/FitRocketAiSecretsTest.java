package de.inrouder.fitrocket;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Synthetic keys only; never insert real credentials into the repository.
 */
public class FitRocketAiSecretsTest {
    @Test public void newGeminiAuthKeysWithDotsAreAccepted() {
        String synthetic = "AQ." + "XyZ0123456789".repeat(3);
        assertEquals(synthetic, FitRocketAiSecrets.validateKey(
            FitRocketAiSecrets.GEMINI, synthetic));
    }

    @Test public void olderGeminiKeyFormatRemainsAccepted() {
        String synthetic = "AIza" + "XyZ0123456789".repeat(3);
        assertEquals(synthetic, FitRocketAiSecrets.validateKey(
            FitRocketAiSecrets.GEMINI, synthetic));
    }

    @Test public void surroundingPasteWhitespaceIsTrimmed() {
        String synthetic = "AQ." + "XyZ0123456789".repeat(2);
        assertEquals(synthetic, FitRocketAiSecrets.validateKey(
            FitRocketAiSecrets.GEMINI, "   " + synthetic + "\n"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsEmbeddedWhitespace() {
        FitRocketAiSecrets.validateKey(FitRocketAiSecrets.GEMINI,
            "AQ." + "XyZ0123456789".repeat(2) + " more");
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsHeaderInjection() {
        FitRocketAiSecrets.validateKey(FitRocketAiSecrets.GEMINI,
            "AQ." + "XyZ0123456789".repeat(2) + "\r\nInjected: test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsShortTokens() {
        FitRocketAiSecrets.validateKey(FitRocketAiSecrets.GEMINI, "AQ.fake");
    }

    @Test public void existingOpenAIFormatRemainsValid() {
        String synthetic = "sk-" + "XyZ0123456789".repeat(3);
        assertEquals(synthetic, FitRocketAiSecrets.validateKey(
            FitRocketAiSecrets.OPENAI, synthetic));
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsUnknownProvider() {
        FitRocketAiSecrets.validateKey("unexpected", "AQ." + "x".repeat(33));
    }
}
