package com.reimagineafrica.dividend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SharesServiceClient {

    @Value("${services.shares-service-url:http://saccos-shares-service:8084}")
    private String sharesServiceUrl;

    private WebClient webClient() {
        return WebClient.builder().baseUrl(sharesServiceUrl).build();
    }

    /**
     * Get all members with their share counts
     */
    public List<Map<String, Object>> getAllMembersWithShares() {
        try {
            return webClient().get()
                .uri("/shares-service/api/v1/shares/members-summary")
                .retrieve()
                .bodyToFlux(Map.class)
                .cast(Map.class)
                .map(m -> (Map<String, Object>) m)
                .collectList()
                .block();
        } catch (Exception e) {
            log.error("Failed to fetch members from shares service: {}", e.getMessage());
            throw new RuntimeException("Shares service unavailable: " + e.getMessage());
        }
    }

    /**
     * Get member's primary savings account ID from Fineract (via shares service)
     */
    public Long getMemberSavingsAccountId(Long memberId) {
        try {
            Map<String, Object> response = webClient().get()
                .uri("/shares-service/api/v1/shares/member/" + memberId + "/savings-account")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            if (response != null && response.containsKey("savingsAccountId")) {
                return ((Number) response.get("savingsAccountId")).longValue();
            }
            return null;
        } catch (Exception e) {
            log.warn("Could not get savings account for member {}: {}", memberId, e.getMessage());
            return null;
        }
    }
}
