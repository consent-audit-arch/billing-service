package com.tcc.billing_service.infrastructure.client;

import com.tcc.billing_service.application.dto.UserProfileResponse;
import com.tcc.billing_service.application.dto.UserServiceBatchResponse;
import com.tcc.billing_service.domain.exception.UserServiceCommunicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class UserServiceClient {

    private static final String REGISTRATION_ID = "keycloak";

    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public UserServiceClient(@Value("${user-service.url}") String userServiceUrl,
                             OAuth2AuthorizedClientManager authorizedClientManager) {
        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
        this.authorizedClientManager = authorizedClientManager;
    }

    public UserProfileResponse fetchUserProfile(String dataSubjectId, String purpose, List<String> dataCategories, String correlationId) {
        String token = fetchClientCredentialsToken()
                .orElseThrow(() -> new UserServiceCommunicationException(
                        "Failed to obtain client credentials token", null));

        String dataCategoriesHeader = dataCategories.stream()
                .collect(Collectors.joining(","));

        return restClient.get()
                .uri("/api/v1/users/{id}", dataSubjectId)
                .header("Authorization", "Bearer " + token)
                .header("X-Purpose", purpose)
                .header("X-Data-Category", dataCategories.isEmpty() ? "" : dataCategories.get(0))
                .header("X-Data-Categories", dataCategoriesHeader)
                .header("X-Data-Subject-Id", dataSubjectId)
                .header("X-Correlation-Id", correlationId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new UserServiceCommunicationException(
                            "User service returned " + response.getStatusCode() + " for subject " + dataSubjectId,
                            null);
                })
                .body(UserProfileResponse.class);
    }

    public UserServiceBatchResponse fetchBatchUserProfiles(List<Long> ids, String purpose,
                                                            List<String> dataCategories, String correlationId) {
        String token = fetchClientCredentialsToken()
                .orElseThrow(() -> new UserServiceCommunicationException(
                        "Failed to obtain client credentials token", null));

        List<String> userServiceCategories = new ArrayList<>(dataCategories);
        if (!userServiceCategories.contains("PERSONAL_DATA")) {
            userServiceCategories.add("PERSONAL_DATA");
        }
        String dataCategoriesHeader = userServiceCategories.stream()
                .collect(Collectors.joining(","));

        String dataSubjectIdsHeader = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return restClient.post()
                .uri("/api/v1/users/batch")
                .header("Authorization", "Bearer " + token)
                .header("X-Purpose", purpose)
                .header("X-Data-Category", userServiceCategories.get(0))
                .header("X-Data-Categories", dataCategoriesHeader)
                .header("X-Data-Subject-Ids", dataSubjectIdsHeader)
                .header("X-Correlation-Id", correlationId)
                .body(java.util.Map.of("ids", ids))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new UserServiceCommunicationException(
                            "User service batch returned " + response.getStatusCode(), null);
                })
                .body(UserServiceBatchResponse.class);
    }

    private Optional<String> fetchClientCredentialsToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(REGISTRATION_ID)
                .principal("billing-service")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            return Optional.empty();
        }

        return Optional.of(authorizedClient.getAccessToken().getTokenValue());
    }
}
