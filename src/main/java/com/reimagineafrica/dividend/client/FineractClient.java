package com.reimagineafrica.dividend.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FineractClient {

    @Value("${fineract.base-url}")
    private String baseUrl;

    @Value("${fineract.username}")
    private String username;

    @Value("${fineract.password}")
    private String password;

    @Value("${fineract.tenant}")
    private String tenant;

    private WebClient webClient() {
        String credentials = Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
            .defaultHeader("Fineract-Platform-TenantId", tenant)
            .build();
    }

    /**
     * Fetch all members with their share counts from shares service via Fineract savings accounts
     */
    public List<Map<String, Object>> getMembersWithShares() {
        try {
            return webClient().get()
                .uri("/clients?fields=id,displayName,externalId&limit=10000")
                .retrieve()
                .bodyToMono(Map.class)
                .map(r -> (List<Map<String, Object>>) r.get("pageItems"))
                .block();
        } catch (Exception e) {
            log.error("Failed to fetch members from Fineract: {}", e.getMessage());
            throw new RuntimeException("Fineract API error: " + e.getMessage());
        }
    }

    /**
     * Post dividend credit to member's savings account in Fineract
     */
    public boolean postDividendToSavings(Long savingsAccountId, BigDecimal amount, String description) {
        try {
            Map<String, Object> payload = Map.of(
                "transactionDate", java.time.LocalDate.now().toString(),
                "transactionAmount", amount,
                "paymentTypeId", 1,
                "note", description,
                "locale", "en",
                "dateFormat", "yyyy-MM-dd"
            );

            webClient().post()
                .uri("/savingsaccounts/" + savingsAccountId + "/transactions?command=deposit")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            return true;
        } catch (Exception e) {
            log.error("Failed to post dividend to savings account {}: {}", savingsAccountId, e.getMessage());
            return false;
        }
    }

    /**
     * Get savings account balance for a member
     */
    public Map<String, Object> getSavingsAccount(Long savingsAccountId) {
        return webClient().get()
            .uri("/savingsaccounts/" + savingsAccountId)
            .retrieve()
            .bodyToMono(Map.class)
            .block();
    }
}
