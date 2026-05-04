package com.enerlytics.insights.services;

import com.enerlytics.insights.clients.UsageClient;
import com.enerlytics.insights.dtos.DeviceResponse;
import com.enerlytics.insights.dtos.UsageResponse;
import com.enerlytics.insights.dtos.responses.InsightResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {

    private final UsageClient usageClient;
    private final OllamaChatModel chatModel;

    public InsightResponse getSavingTips(Long userId) {
        log.debug("Fetching usage data for saving tips for userId: {}", userId);
        UsageResponse usageData = usageClient.getXDaysUsageForUser(userId, 3);

        double totalEnergyConsumed = usageData.devices().stream()
                .mapToDouble(DeviceResponse::energyConsumed)
                .sum();

        log.info(
                "Generating saving tips via AI for userId: {} (Total energy consumed: {} kWh)",
                userId,
                totalEnergyConsumed);

        String savingTipsPrompt = "This is my total consumption over past 3 days. "
                + "How can I reduce my energy consumption? How does it compare to average households? "
                + "Total energy used: \n"
                + usageData.devices();

        ChatResponse aiResponse =
                chatModel.call(Prompt.builder().content(savingTipsPrompt).build());

        log.debug("AI generated saving tips successfully for userId: {}", userId);
        return new InsightResponse(userId, aiResponse.getResult().getOutput().getText(), totalEnergyConsumed);
    }

    public InsightResponse getOverview(Long userId) {
        log.debug("Fetching usage data for overview for userId: {}", userId);
        UsageResponse usageData = usageClient.getXDaysUsageForUser(userId, 3);

        double totalEnergyConsumed = usageData.devices().stream()
                .mapToDouble(DeviceResponse::energyConsumed)
                .sum();

        log.info(
                "Generating usage overview via AI for userId: {} (Total energy consumed: {} kWh)",
                userId,
                totalEnergyConsumed);

        String overviewPrompt =
                "Analyze the following energy usage data and provide a concise overview with actionable insights. "
                        + "This data is the aggregate data for the past 3 days. "
                        + "Usage Data: \n"
                        + usageData.devices();

        ChatResponse aiResponse =
                chatModel.call(Prompt.builder().content(overviewPrompt).build());

        log.debug("AI generated usage overview successfully for userId: {}", userId);
        return new InsightResponse(userId, aiResponse.getResult().getOutput().getText(), totalEnergyConsumed);
    }
}
