package gr.ekpa.citizens.client;

import gr.ekpa.citizens.domain.Citizen;
import gr.ekpa.citizens.domain.CitizenUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class CitizenApiClient {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public CitizenApiClient(RestClient.Builder restClientBuilder,
			ObjectMapper objectMapper,
			@Value("${citizens.service.url}") String serviceUrl) {
		this.restClient = restClientBuilder.baseUrl(serviceUrl).build();
		this.objectMapper = objectMapper;
	}

	public ApiResult<Citizen> create(Citizen citizen) {
		return execute(() -> restClient.post()
				.uri("/citizens")
				.body(citizen)
				.retrieve()
				.body(Citizen.class));
	}

	public ApiResult<Citizen> get(String at) {
		return execute(() -> restClient.get()
				.uri("/citizens/{at}", at)
				.retrieve()
				.body(Citizen.class));
	}

	public ApiResult<Citizen> update(String at, CitizenUpdate update) {
		return execute(() -> restClient.put()
				.uri("/citizens/{at}", at)
				.body(update)
				.retrieve()
				.body(Citizen.class));
	}

	public ApiResult<Void> delete(String at) {
		return execute(() -> {
			restClient.delete()
					.uri("/citizens/{at}", at)
					.retrieve()
					.toBodilessEntity();
			return null;
		});
	}

	public ApiResult<List<Citizen>> search(Map<String, String> params) {
		return execute(() -> restClient.get()
				.uri(uriBuilder -> {
					var builder = uriBuilder.path("/citizens");
					params.forEach(builder::queryParam);
					return builder.build();
				})
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				}));
	}

	private <T> ApiResult<T> execute(Supplier<T> request) {
		try {
			return new ApiResult<>(request.get(), null);
		} catch (HttpClientErrorException exception) {
			return new ApiResult<>(null, errorMessage(exception));
		}
	}

	private String errorMessage(HttpClientErrorException exception) {
		try {
			JsonNode error = objectMapper.readTree(exception.getResponseBodyAsString()).get("error");
			if (error == null || !error.isString()) {
				throw new IllegalStateException("Client error response did not contain a string 'error' message");
			}
			return error.asString();
		} catch (JacksonException exceptionCause) {
			throw new IllegalStateException("Could not read the client error response", exceptionCause);
		}
	}

	public record ApiResult<T>(T body, String error) {
		public boolean isSuccessful() {
			return error == null;
		}
	}
}
