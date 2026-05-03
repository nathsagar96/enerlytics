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
        UsageResponse response = usageClient.getXDaysUsageForUser(userId, 3);

        double totalUsage = response.devices().stream()
                .mapToDouble(DeviceResponse::energyConsumed)
                .sum();

        log.info("Calling ollama for userId {} with total usage {}", userId, totalUsage);

        String prompt = "This is my total consumption over past 3 days."
                + "How can I reduce my energy consumption? How does it compare to average households?"
                + "Total energy used: \n"
                + response.devices();

        ChatResponse chatResponse =
                chatModel.call(Prompt.builder().content(prompt).build());

        return new InsightResponse(userId, chatResponse.getResult().getOutput().getText(), totalUsage);
    }

    public InsightResponse getOverview(Long userId) {
        UsageResponse response = usageClient.getXDaysUsageForUser(userId, 3);

        log.info("response: {}", response);

        double totalUsage = response.devices().stream()
                .mapToDouble(DeviceResponse::energyConsumed)
                .sum();

        log.info("Calling ollama for userId {} with total usage {}", userId, totalUsage);

        String prompt =
                "Analyze the following energy usage data and provide a concise overview with actionable insights."
                        + "This data is the aggregate data for the past 3 days."
                        + "Usage Data: \n"
                        + response.devices();

        ChatResponse chatResponse =
                chatModel.call(Prompt.builder().content(prompt).build());

        return new InsightResponse(userId, chatResponse.getResult().getOutput().getText(), totalUsage);
    }
}
